package com.nonsense.chat.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks REST connectivity to Supabase so the UI can show a Telegram-style status in the top bar.
 * [DocRepository] reports every read into here (in-flight count + consecutive failures):
 *
 *   CONNECTED  – nothing pending and last reads succeeded → show the normal title
 *   UPDATING   – request(s) in flight, no failures yet     → "Обновление…"
 *   CONNECTING – reads are timing out / failing repeatedly → "Соединение…"
 *
 * On this user's flaky network (VPN/socket timeouts) this makes the app feel alive instead of
 * silently showing an empty list while requests retry in the background.
 */
@Singleton
class ConnectionMonitor @Inject constructor() {

    enum class State { CONNECTED, UPDATING, CONNECTING }

    private val inFlight = AtomicInteger(0)
    private val failures = AtomicInteger(0)

    private val _state = MutableStateFlow(State.UPDATING)
    val state: StateFlow<State> = _state.asStateFlow()

    /** A read has started. */
    fun onStart() {
        inFlight.incrementAndGet()
        recompute()
    }

    /** One attempt of a read failed (timeout etc); more retries may still follow. */
    fun onAttemptFailed() {
        failures.incrementAndGet()
        recompute()
    }

    /** A read completed successfully → connection is healthy again. */
    fun onSuccess() {
        failures.set(0)
        recompute()
    }

    /** A read fully finished (success or final soft-fail). Always called once per read. */
    fun onEnd() {
        if (inFlight.get() > 0) inFlight.decrementAndGet()
        recompute()
    }

    private fun recompute() {
        val f = failures.get()
        val p = inFlight.get()
        _state.value = when {
            f >= 1 -> State.CONNECTING
            p > 0 -> State.UPDATING
            else -> State.CONNECTED
        }
    }
}
