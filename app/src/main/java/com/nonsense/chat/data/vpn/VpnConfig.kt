package com.nonsense.chat.data.vpn

import com.nonsense.chat.data.proxy.ProxyEntry

data class VpnConfig(
    val enabled: Boolean = false,
    val outboundConfig: String = "",
    val useProxyList: Boolean = false,
    val proxyEntries: List<ProxyEntry> = emptyList(),
    val splitApps: Set<String> = emptySet(),
    val ipv6: Boolean = false,
    val dns: String = "1.1.1.1",
) {
    val hasOutbound: Boolean
        get() = outboundConfig.isNotBlank() || (useProxyList && proxyEntries.isNotEmpty())
}
