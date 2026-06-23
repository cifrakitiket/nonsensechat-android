package com.nonsense.chat.data.vpn

import com.nonsense.chat.data.proxy.ProxyEntry
import com.nonsense.chat.data.proxy.ProxyType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

class SingBoxConfigBuilder @Inject constructor(
    private val json: Json,
) {
    fun build(config: VpnConfig): String {
        val outbound = buildOutbound(config)
        val root = buildJsonObject {
            putJsonObject("log") {
                put("level", "warn")
                put("timestamp", true)
            }
            putJsonArray("dns") {
                add(buildJsonObject {
                    put("tag", "main")
                    put("address", config.dns.ifBlank { "1.1.1.1" })
                })
            }
            putJsonArray("inbounds") {
                add(buildJsonObject {
                    put("type", "tun")
                    put("tag", "tun-in")
                    put("interface_name", "nonsense0")
                    put("mtu", 9000)
                    put("auto_route", false)
                    put("strict_route", true)
                    putJsonArray("inet4_address") { add("10.77.0.2/30") }
                    if (config.ipv6) putJsonArray("inet6_address") { add("fdfe:dcba:9876::2/126") }
                })
            }
            putJsonArray("outbounds") {
                add(outbound)
                add(buildJsonObject {
                    put("type", "direct")
                    put("tag", "direct")
                })
            }
            putJsonObject("route") {
                put("final", "proxy")
                putJsonArray("rules") {
                    add(buildJsonObject {
                        put("action", "route")
                        put("outbound", "direct")
                        putJsonArray("package_name") { add("com.nonsensechat.app") }
                    })
                }
            }
        }
        return root.toString()
    }

    private fun buildOutbound(config: VpnConfig) = when {
        config.outboundConfig.trim().startsWith("{") -> runCatching {
            json.parseToJsonElement(config.outboundConfig).jsonObject
        }.getOrDefault(fallbackOutbound(config.proxyEntries.firstOrNull()))

        config.useProxyList -> fallbackOutbound(config.proxyEntries.firstOrNull())

        else -> buildJsonObject {
            put("type", "direct")
            put("tag", "proxy")
        }
    }

    private fun fallbackOutbound(proxy: ProxyEntry?) = buildJsonObject {
        put("tag", "proxy")
        if (proxy == null) {
            put("type", "direct")
        } else {
            put("type", if (proxy.type == ProxyType.HTTP) "http" else "socks")
            put("server", proxy.host)
            put("server_port", proxy.port)
            proxy.user?.let { put("username", it) }
            proxy.pass?.let { put("password", it) }
        }
    }
}
