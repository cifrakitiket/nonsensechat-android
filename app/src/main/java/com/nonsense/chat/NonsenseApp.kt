package com.nonsense.chat

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.push.Notifications
import dagger.hilt.android.HiltAndroidApp
import java.io.IOException
import javax.inject.Inject

@HiltAndroidApp
class NonsenseApp : Application(), ImageLoaderFactory {

    /** Hardened, disk-cached Coil loader (see AppModule.provideImageLoader). Coil calls
     *  [newImageLoader] lazily on first image load, by which point Hilt has injected this. */
    @Inject lateinit var imageLoader: ImageLoader

    @Inject lateinit var account: AccountManager

    override fun onCreate() {
        super.onCreate()
        installNetworkCrashGuard()
        // Register notification channels up-front so FCM (which may arrive in the background)
        // always has a valid channel to post into.
        Notifications.ensureChannels(this)
        // Presence: only advertise "online" while the app is actually in the foreground. ON_STOP
        // (app backgrounded) marks the user offline; ON_START resumes the heartbeat.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) { account.enterForeground() }
            override fun onStop(owner: LifecycleOwner) { account.enterBackground() }
        })
    }

    override fun newImageLoader(): ImageLoader = imageLoader

    /**
     * Process-wide safety net for the flaky backend. Reads are already retried with backoff at the
     * Flow layer (`observeMyChats`, `uiState` combine, message stream), but fire-and-forget writes
     * (`doc_apply` / storage uploads launched from `viewModelScope`) can still throw a Ktor/Supabase
     * timeout that escapes to the main thread and kills the app with `FATAL EXCEPTION: main`.
     *
     * A transient network/IO/timeout error is not a bug — it just means the request should be retried
     * later. We log and swallow those, and delegate everything else to the platform handler so real
     * crashes still surface normally.
     */
    private fun installNetworkCrashGuard() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isNetworkError(throwable)) {
                android.util.Log.w("NSDIAG", "Swallowed network error on ${thread.name}: ${throwable.message}")
            } else {
                previous?.uncaughtException(thread, throwable)
            }
        }
    }

    /** True if [t] or any of its causes is a transient network/IO/timeout failure. */
    private fun isNetworkError(t: Throwable?): Boolean {
        var cur = t
        var depth = 0
        while (cur != null && depth < 12) {
            if (cur is IOException) return true
            val name = cur.javaClass.name
            if (name.contains("HttpRequestException") ||      // io.github.jan.supabase.exceptions.*
                name.contains("HttpRequestTimeoutException") || // io.ktor.client.plugins.*
                name.contains("Timeout") ||
                name.contains("supabase.exceptions") ||
                name.contains("UnknownHost") ||
                name.contains("ConnectException")
            ) return true
            cur = cur.cause
            depth++
        }
        return false
    }
}
