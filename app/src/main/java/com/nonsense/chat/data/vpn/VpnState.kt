package com.nonsense.chat.data.vpn

enum class VpnStage {
    DISCONNECTED,
    PREPARING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR,
}

data class VpnState(
    val stage: VpnStage = VpnStage.DISCONNECTED,
    val sinceMs: Long = System.currentTimeMillis(),
    val lastError: String? = null,
    val upBytes: Long = 0L,
    val downBytes: Long = 0L,
)
