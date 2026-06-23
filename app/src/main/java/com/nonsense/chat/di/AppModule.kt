package com.nonsense.chat.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import androidx.room.Room
import com.nonsense.chat.data.SupabaseConfig
import com.nonsense.chat.data.cache.AppDatabase
import com.nonsense.chat.data.proxy.ProxyController
import com.nonsense.chat.data.vpn.NoopTunnelEngine
import com.nonsense.chat.data.vpn.TunnelEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import java.io.IOException
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @AppScope
    fun provideAppScope(): CoroutineScope = CoroutineScope(
        // A CoroutineExceptionHandler here means a failed background job (e.g. a Supabase request
        // timing out) is logged and swallowed instead of killing the whole app process.
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e -> e.printStackTrace() },
    )

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "nonsense_cache.db")
            // The DB is a disposable cache mirroring the server, so on a schema bump just rebuild it
            // rather than shipping migrations — no user data lives only here.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideTunnelEngine(engine: NoopTunnelEngine): TunnelEngine = engine

    @Provides
    @Singleton
    fun provideSupabase(proxyController: ProxyController): SupabaseClient = createSupabaseClient(
        supabaseUrl = SupabaseConfig.URL,
        supabaseKey = SupabaseConfig.ANON_KEY,
    ) {
        // This device's network is flaky: a fresh connection to Supabase sometimes hangs the whole
        // request while other requests are instant. So FAIL FAST (a real request takes <1s) and let
        // the app-level retry open a fresh connection, which usually succeeds.
        requestTimeout = 12.seconds
        // Tuned OkHttp engine. Two symptoms fixed here:
        //  1) Broken-IPv6: OkHttp tries an IPv6 address first and stalls → resolve IPv4 first.
        //  2) Intermittent REST hangs: a request freezes for the full readTimeout while others
        //     are instant — the signature of an HTTP/2 stream stalling on a flaky mobile network
        //     (the socket stays "alive" so connect-retry never fires). Forcing HTTP/1.1 gives each
        //     request its own connection from the pool, so a stall can't block other requests and
        //     retryOnConnectionFailure can actually recover. The browser survives by racing
        //     connections; OkHttp+HTTP/2 just waits. Short connection keep-alive avoids reusing a
        //     half-dead socket.
        httpEngine = OkHttp.create {
            config {
                protocols(listOf(Protocol.HTTP_1_1))
                retryOnConnectionFailure(true)
                connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
                connectTimeout(8, TimeUnit.SECONDS)
                readTimeout(10, TimeUnit.SECONDS)
                writeTimeout(10, TimeUnit.SECONDS)
                callTimeout(12, TimeUnit.SECONDS)
                dns(Ipv4FirstDns)
                // Anti-censorship: route through user-configured proxies with automatic failover.
                // No-op (DIRECT) when the user hasn't enabled a proxy. Realtime's WebSocket rides
                // this same engine, so it's tunneled too.
                proxySelector(proxyController.proxySelector)
                proxyAuthenticator(proxyController.proxyAuthenticator)
            }
        }
        install(Auth) {
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }

    /**
     * Coil image loader on the SAME hardened network path as Supabase (IPv4-first DNS, HTTP/1.1,
     * retryOnConnectionFailure). Images here are big (avatars ~1–2 MB) and this user's network is
     * slow/VPN-throttled, so: generous-but-bounded timeouts (read/write 30s, call 60s) + a persistent
     * DISK CACHE so once an image loads it stays loaded and survives reconnects. Coil's default loader
     * used its own un-tuned OkHttp with no cache — that's why images "didn't load at all" on his phone.
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        proxyController: ProxyController,
    ): ImageLoader {
        val http = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 30, TimeUnit.SECONDS))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .dns(Ipv4FirstDns)
            // Same anti-censorship proxy path as REST so avatars/photos load under blocking too.
            .proxySelector(proxyController.proxySelector)
            .proxyAuthenticator(proxyController.proxyAuthenticator)
            // Coil makes ONE attempt per image and shows nothing on failure. On this flaky network
            // that means avatars almost never appear (half the sockets time out). This interceptor
            // gives each image the same retry resilience the REST reads have, opening a fresh
            // connection each time, so a timed-out avatar recovers instead of silently failing.
            .addInterceptor(ImageRetryInterceptor(maxRetries = 3))
            .build()
        return ImageLoader.Builder(context)
            .okHttpClient(http)
            .crossfade(true)
            .respectCacheHeaders(false) // cache aggressively even if the server omits cache headers
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    // 25% of the cache dir (was 5%): on this slow network re-downloading avatars/
                    // photos is the expensive part, so keep far more of them on disk between runs.
                    .maxSizePercent(0.25)
                    .build()
            }
            .build()
    }

    /** Retries an image request a few times on network failure / 5xx, with short backoff — mirrors
     *  the REST read retry so flaky-network avatars recover instead of failing on the first timeout.
     *  4xx is returned as-is (a real "not found" shouldn't be retried). Runs on OkHttp dispatcher
     *  threads, so a blocking sleep here is fine. */
    private class ImageRetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var lastError: IOException? = null
            for (attempt in 0..maxRetries) {
                try {
                    val response = chain.proceed(request)
                    if (response.isSuccessful || response.code in 400..499) return response
                    response.close() // 5xx / other → retry
                } catch (e: IOException) {
                    lastError = e
                    android.util.Log.e("NSDIAG", "image retry (attempt=$attempt): ${e.message}")
                }
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep((500L * (attempt + 1)).coerceAtMost(2_000L))
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw IOException("image retry interrupted", e)
                    }
                }
            }
            throw lastError ?: IOException("image request failed after $maxRetries retries")
        }
    }

    /** Resolves IPv4 before IPv6 (broken-IPv6 path stalls), and caches a good result *briefly* so a
     *  later system-DNS hang (common on this flaky device) can't re-stall every request in a burst.
     *  The cache is TTL'd (60 s) and self-healing: an entry expires, and a failed lookup is never
     *  cached. The previous version cached the FIRST result for the whole process lifetime, which on
     *  a VPN/changing network could pin a now-dead IP forever — every request then failed until the
     *  app was killed. A short TTL keeps the burst-protection but lets the app recover on its own. */
    private val Ipv4FirstDns = object : Dns {
        // value = (resolvedAtMs, addresses); a plain Pair avoids a nested class (not allowed in an
        // anonymous object expression).
        private val cache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, List<InetAddress>>>()
        private val ttlMs = 60_000L
        override fun lookup(hostname: String): List<InetAddress> {
            val now = System.currentTimeMillis()
            cache[hostname]?.let { (atMs, addrs) -> if (now - atMs < ttlMs) return addrs else cache.remove(hostname) }
            val resolved = Dns.SYSTEM.lookup(hostname).sortedBy { if (it is Inet4Address) 0 else 1 }
            if (resolved.isNotEmpty()) cache[hostname] = now to resolved
            return resolved
        }
    }
}
