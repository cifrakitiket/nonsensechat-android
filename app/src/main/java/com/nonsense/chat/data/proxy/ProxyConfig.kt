package com.nonsense.chat.data.proxy

/**
 * Anti-censorship proxy config. Under a "white list" regime (only a handful of approved
 * domains/IPs reachable, everything else — including Supabase — blocked) the app must route its
 * traffic through a reachable proxy. The user pastes one or more proxies (distributed out-of-band)
 * and the network layer fails over between them.
 */

enum class ProxyType { SOCKS5, HTTP }

/** A single proxy endpoint. [user]/[pass] are only honored for HTTP proxies (OkHttp can't do
 *  SOCKS5 username/password auth). */
data class ProxyEntry(
    val type: ProxyType,
    val host: String,
    val port: Int,
    val user: String? = null,
    val pass: String? = null,
)

/**
 * Resolved proxy state read by the network layer on every connection.
 *
 * Note on DNS: OkHttp already sends the target hostname to the proxy for resolution (unresolved
 * for SOCKS5, full request line for HTTP), so the app's local DNS only ever resolves the proxy
 * host itself when proxying — the target host is never leaked/resolved locally. There is therefore
 * no separate "remote DNS" switch: it's inherent once a proxy is active.
 */
data class ProxyConfig(
    val enabled: Boolean = false,
    val entries: List<ProxyEntry> = emptyList(),
    /** Allow a direct (no-proxy) connection as the last resort after all proxies fail. */
    val allowDirect: Boolean = false,
) {
    /** True only when proxying should actually happen. */
    val active: Boolean get() = enabled && entries.isNotEmpty()
}

/**
 * Parses one user-entered line into a [ProxyEntry], or null if it's blank/garbage (soft parsing —
 * a typo on one line never breaks the others). Accepts:
 *   socks5://[user:pass@]host:port
 *   http://[user:pass@]host:port
 *   host:port            (bare → defaults to SOCKS5)
 */
fun parseProxyLine(line: String): ProxyEntry? {
    val raw = line.trim()
    if (raw.isEmpty()) return null

    var rest = raw
    var type = ProxyType.SOCKS5
    val schemeIdx = rest.indexOf("://")
    if (schemeIdx >= 0) {
        type = when (rest.substring(0, schemeIdx).lowercase()) {
            "socks5", "socks", "socks5h" -> ProxyType.SOCKS5
            "http", "https" -> ProxyType.HTTP
            else -> return null
        }
        rest = rest.substring(schemeIdx + 3)
    }

    var user: String? = null
    var pass: String? = null
    val atIdx = rest.lastIndexOf('@')
    if (atIdx >= 0) {
        val creds = rest.substring(0, atIdx)
        rest = rest.substring(atIdx + 1)
        val colon = creds.indexOf(':')
        if (colon >= 0) {
            user = creds.substring(0, colon).ifBlank { null }
            pass = creds.substring(colon + 1).ifBlank { null }
        } else {
            user = creds.ifBlank { null }
        }
    }

    val portColon = rest.lastIndexOf(':')
    if (portColon <= 0 || portColon == rest.length - 1) return null
    val host = rest.substring(0, portColon).trim()
    val port = rest.substring(portColon + 1).trim().toIntOrNull() ?: return null
    if (host.isEmpty() || port !in 1..65535) return null

    return ProxyEntry(type, host, port, user, pass)
}

/** Parses a whole multi-line text block (one proxy per line) into entries, dropping bad lines. */
fun parseProxyLines(text: String): List<ProxyEntry> =
    text.lineSequence().mapNotNull(::parseProxyLine).toList()
