package com.nonsense.chat.data

import com.nonsense.chat.data.repos.AuthRepository
import com.nonsense.chat.data.repos.PresenceRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.di.AppScope
import com.nonsense.chat.model.User
import com.nonsense.chat.push.PushTokenManager
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Auth states the UI cares about. */
enum class AuthState { LOADING, AUTHENTICATED, UNAUTHENTICATED }

/**
 * Central session hub: tracks the signed-in user, streams their profile (`me`), runs the presence
 * heartbeat, and registers the FCM push token. One instance for the whole app.
 */
@Singleton
class AccountManager @Inject constructor(
    private val auth: AuthRepository,
    private val users: UserRepository,
    private val presence: PresenceRepository,
    private val pushTokens: PushTokenManager,
    @AppScope private val scope: CoroutineScope,
) {
    private val _me = MutableStateFlow<User?>(null)
    val me: StateFlow<User?> = _me

    val authState: StateFlow<AuthState> = run {
        val flow = MutableStateFlow(AuthState.LOADING)
        scope.launch {
            auth.sessionStatus.collect { status ->
                when (status) {
                    // A failed token *refresh* is always transient: supabase-kt only emits
                    // RefreshFailure for a NetworkError or a 5xx (a genuinely invalid/revoked
                    // refresh token makes it sign out → NotAuthenticated instead). The stored
                    // session is still held and auto-refresh keeps retrying. So treat it as
                    // AUTHENTICATED and let the user into the app with the existing token —
                    // mapping it to LOADING (the old `else` branch) left returning users stuck
                    // on the splash spinner forever whenever the startup refresh hit this flaky /
                    // VPN network. That was the "nothing loads" bug.
                    is SessionStatus.Authenticated,
                    is SessionStatus.RefreshFailure -> {
                        flow.value = AuthState.AUTHENTICATED
                        onAuthenticated()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        flow.value = AuthState.UNAUTHENTICATED
                        onSignedOut()
                    }
                    SessionStatus.Initializing -> flow.value = AuthState.LOADING
                }
            }
        }
        flow
    }

    val uid: String? get() = auth.currentUid()

    private var sessionJobs: Job? = null
    private var runningUid: String? = null
    private var heartbeatJob: Job? = null
    // The app starts in the foreground; ProcessLifecycleOwner toggles this (enterForeground/Background).
    @Volatile private var foreground = true

    private fun onAuthenticated() {
        val uid = auth.currentUid() ?: return
        // sessionStatus re-emits on every token refresh (and possibly several RefreshFailure
        // retries). Don't tear down and restart the profile stream / heartbeat each time — only
        // (re)start when it's a different user or nothing is running yet.
        if (runningUid == uid && sessionJobs?.isActive == true) return
        runningUid = uid
        sessionJobs?.cancel()
        sessionJobs = scope.launch {
            // Keep the profile stream alive across transient network failures (e.g. request
            // timeouts) — retry quietly instead of letting the exception crash the app.
            launch {
                while (isActive) {
                    runCatching { users.observe(uid).collect { _me.value = it } }
                    delay(3_000)
                }
            }
            launch { runCatching { pushTokens.register(uid) } }
        }
        // Only heartbeat (advertise "online") while the app is actually in the foreground.
        if (foreground) startHeartbeat(uid)
    }

    private fun onSignedOut() {
        runningUid = null
        sessionJobs?.cancel()
        sessionJobs = null
        stopHeartbeat()
        _me.value = null
    }

    private fun startHeartbeat(uid: String) {
        if (heartbeatJob?.isActive == true) return
        heartbeatJob = scope.launch {
            while (isActive) {
                runCatching { presence.heartbeat(uid) }
                delay(PresenceRepository.HEARTBEAT_MS)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    /** App entered the foreground → resume advertising presence. */
    fun enterForeground() {
        foreground = true
        runningUid?.let { startHeartbeat(it) }
    }

    /** App went to the background → stop the heartbeat and mark offline so peers see it within the
     *  presence window instead of a stale-forever green dot. */
    fun enterBackground() {
        foreground = false
        stopHeartbeat()
        runningUid?.let { uid -> scope.launch { runCatching { presence.setOffline(uid) } } }
    }

    suspend fun signOut() {
        stopHeartbeat()
        uid?.let { runCatching { pushTokens.unregister(it) }; runCatching { presence.setOffline(it) } }
        auth.signOut()
    }
}
