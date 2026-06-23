package com.nonsense.chat.data.vpn

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import com.nonsense.chat.data.SettingsStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NonsenseVpnService : VpnService() {
    @Inject lateinit var settings: SettingsStore
    @Inject lateinit var states: VpnStateRepository
    @Inject lateinit var configBuilder: SingBoxConfigBuilder
    @Inject lateinit var notification: VpnNotification
    @Inject lateinit var engine: TunnelEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        states.setStage(VpnStage.CONNECTING)
        val startingNotification = notification.build(this, connected = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                VpnNotification.NOTIFICATION_ID,
                startingNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(VpnNotification.NOTIFICATION_ID, startingNotification)
        }

        scope.launch {
            runCatching { startTunnel() }
                .onFailure { e ->
                    states.setStage(VpnStage.ERROR, e.message ?: "VPN failed")
                    stopSelf()
                }
        }
        return START_STICKY
    }

    private suspend fun startTunnel() {
        val config = settings.vpnConfig.first()
        val tun = Builder()
            .setSession("Nonsense VPN")
            .setMtu(1500)
            .addAddress("10.77.0.2", 30)
            .addRoute("0.0.0.0", 0)
            .addDnsServer(config.dns.ifBlank { "1.1.1.1" })
            .apply {
                if (config.ipv6) {
                    addAddress("fdfe:dcba:9876::2", 126)
                    addRoute("::", 0)
                }
                addDisallowedApplication(packageName)
                config.splitApps.forEach { pkg ->
                    if (pkg != packageName) runCatching { addDisallowedApplication(pkg) }
                }
            }
            .establish()
            ?: error("VPN permission was not granted")

        val json = configBuilder.build(config)
        engine.start(tun, json, ProtectSocket { socket -> protect(socket) })
        states.setStage(VpnStage.CONNECTED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                VpnNotification.NOTIFICATION_ID,
                notification.build(this, connected = true),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(VpnNotification.NOTIFICATION_ID, notification.build(this, connected = true))
        }
    }

    override fun onDestroy() {
        engine.stop()
        states.setStage(VpnStage.DISCONNECTED)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_STOP = "com.nonsense.chat.vpn.STOP"

        fun start(context: Context) {
            val intent = Intent(context, NonsenseVpnService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NonsenseVpnService::class.java).setAction(ACTION_STOP))
        }
    }
}
