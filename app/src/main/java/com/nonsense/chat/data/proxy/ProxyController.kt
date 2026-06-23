package com.nonsense.chat.data.proxy

import android.util.Log
import com.nonsense.chat.data.SettingsStore
import com.nonsense.chat.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anti-censorship proxy layer. Holds the current [ProxyConfig] (kept in sync with [SettingsStore])
 * and exposes a [ProxySelector] + [Authenticator] that the OkHttp clients in AppModule plug in.
 *
 * Failover is OkHttp's own job: [ProxySelector.select] returns the proxies in order, OkHttp tries
 * each route and calls [ProxySelector.connectFailed] when one can't connect, then moves to the next.
 * So adding N proxies gives automatic round-robin failover with no extra logic here.
 *
 * The OkHttp clients are singletons created once, so we never recreate them — the selector reads
 * [current] live, and a settings change simply takes effect on the next new connection.
 */
@Singleton
class ProxyController @Inject constructor(
    settings: SettingsStore,
    @AppScope scope: CoroutineScope,
) {
    @Volatile
    private var current: ProxyConfig = ProxyConfig()

    init {
        // Keep the live config in sync with what the user saved. Runs for the app's whole lifetime.
        scope.launch {
            settings.proxyConfig.collect { current = it }
        }
    }

    /** Ordered proxy list for OkHttp. When no proxy is active, behaves like the default (DIRECT). */
    val proxySelector: ProxySelector = object : ProxySelector() {
        override fun select(uri: URI?): List<Proxy> {
            val cfg = current
            if (!cfg.active) return listOf(Proxy.NO_PROXY)

            val proxies = cfg.entries.mapNotNull { entry ->
                runCatching {
                    val type = when (entry.type) {
                        ProxyType.SOCKS5 -> Proxy.Type.SOCKS
                        ProxyType.HTTP -> Proxy.Type.HTTP
                    }
                    // Resolves the PROXY host locally (needed to connect to it). The TARGET host is
                    // still resolved on the proxy side by OkHttp, so the blocked target is never
                    // resolved on-device.
                    Proxy(type, InetSocketAddress(entry.host, entry.port))
                }.getOrNull()
            }.toMutableList()

            if (proxies.isEmpty() || cfg.allowDirect) proxies.add(Proxy.NO_PROXY)
            return proxies
        }

        override fun connectFailed(uri: URI?, sa: SocketAddress?, e: IOException?) {
            // Just diagnostics — OkHttp already advances to the next proxy in the list on its own.
            Log.w("NSDIAG", "proxy connectFailed: $sa (${e?.message}) → trying next")
        }
    }

    /**
     * Supplies credentials for HTTP proxies that require auth (responds to 407). SOCKS5
     * username/password auth is NOT supported by OkHttp, so SOCKS creds are ignored — the UI says so.
     */
    val proxyAuthenticator: Authenticator = Authenticator { route: Route?, response: Response ->
        // Only answer proxy-auth challenges, and only once (avoid retry loops on bad creds).
        if (response.code != 407) return@Authenticator null
        if (response.request.header("Proxy-Authorization") != null) return@Authenticator null

        val host = (route?.proxy?.address() as? InetSocketAddress)?.hostString
        val entry = current.entries.matchingHttpProxy(host)
        if (entry?.user == null) return@Authenticator null

        val credential = Credentials.basic(entry.user, entry.pass ?: "")
        response.request.newBuilder()
            .header("Proxy-Authorization", credential)
            .build()
    }
}

/** Finds the configured HTTP proxy entry matching the proxy host that issued the 407 challenge. */
private fun List<ProxyEntry>.matchingHttpProxy(host: String?): ProxyEntry? =
    firstOrNull { it.type == ProxyType.HTTP && (host == null || it.host == host) }
