package com.nonsense.chat.data.vpn

import android.os.ParcelFileDescriptor
import javax.inject.Inject

fun interface ProtectSocket {
    fun protect(socket: Int): Boolean
}

interface TunnelEngine {
    fun start(tun: ParcelFileDescriptor, configJson: String, protect: ProtectSocket)
    fun stop()
}

/**
 * Phase 2A engine: keeps the system VpnService lifecycle working without native code.
 *
 * Real DPI bypass belongs in a native engine (sing-box/libbox.aar) that reads the TUN fd, applies
 * VLESS/Reality or another outbound, and calls protect() for its own sockets to avoid VPN loops.
 */
class NoopTunnelEngine @Inject constructor() : TunnelEngine {
    private var tun: ParcelFileDescriptor? = null

    override fun start(tun: ParcelFileDescriptor, configJson: String, protect: ProtectSocket) {
        this.tun = tun
    }

    override fun stop() {
        runCatching { tun?.close() }
        tun = null
    }
}
