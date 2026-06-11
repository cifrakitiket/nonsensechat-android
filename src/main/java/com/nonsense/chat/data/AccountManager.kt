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
                    is SessionStatus.Authenticated -> {
                        flow.value = AuthState.AUTHENTICATED
                        onAuthenticated()
                    }
                    is SessionStatus.NotAuthenticated -> {
                        flow.value = AuthState.UNAUTHENTICATED
                        onSignedOut()
                    }
                    else -> flow.value = AuthState.LOADING
                }
            }
        }
        flow
    }

    val uid: String? get() = auth.currentUid()

    private var sessionJobs: Job? = null

    private fun onAuthenticated() {
        val uid = auth.currentUid() ?: return
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
            launch { heartbeatLoop(uid) }
            launch { runCatching { pushTokens.register(uid) } }
        }
    }

    private fun onSignedOut() {
        sessionJobs?.cancel()
        sessionJobs = null
        _me.value = null
    }

    private suspend fun heartbeatLoop(uid: String) {
        while (scope.isActive) {
            runCatching { presence.heartbeat(uid) }
            delay(PresenceRepository.HEARTBEAT_MS)
        }
    }

    suspend fun signOut() {
        uid?.let { runCatching { pushTokens.unregister(it) }; runCatching { presence.setOffline(it) } }
        auth.signOut()
    }
}
