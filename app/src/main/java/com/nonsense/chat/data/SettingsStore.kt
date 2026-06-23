package com.nonsense.chat.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nonsense.chat.data.proxy.ProxyConfig
import com.nonsense.chat.data.proxy.parseProxyLines
import com.nonsense.chat.data.vpn.VpnConfig
import com.nonsense.chat.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

/** App-level preferences: chosen theme and notification toggle. */
@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val THEME = stringPreferencesKey("theme")
    private val NOTIFS = booleanPreferencesKey("notifications_enabled")
    // Show the message text in the notification, or just "Новое сообщение" when previews are off.
    private val NOTIF_PREVIEW = booleanPreferencesKey("notif_preview")
    // Local mirror of the chats this user muted (source of truth is the chat doc, but the push
    // handler — which may run with the app killed — needs a synchronous, offline-readable copy).
    private val MUTED_CHATS = stringSetPreferencesKey("muted_chat_ids")

    // Anti-censorship proxy (see ProxyConfig / ProxyController).
    private val PROXY_ENABLED = booleanPreferencesKey("proxy_enabled")
    private val PROXY_ENDPOINTS = stringPreferencesKey("proxy_endpoints")
    private val PROXY_ALLOW_DIRECT = booleanPreferencesKey("proxy_allow_direct")

    // System VPN shell. The native traffic engine is intentionally pluggable: the Kotlin app can
    // build/launch the VpnService without libbox.aar, then a real engine can be bound later.
    private val VPN_ENABLED = booleanPreferencesKey("vpn_enabled")
    private val VPN_OUTBOUND_CONFIG = stringPreferencesKey("vpn_outbound_config")
    private val VPN_USE_PROXY_LIST = booleanPreferencesKey("vpn_use_proxy_list")
    private val VPN_SPLIT_APPS = stringSetPreferencesKey("vpn_split_apps")
    private val VPN_IPV6 = booleanPreferencesKey("vpn_ipv6")
    private val VPN_DNS = stringPreferencesKey("vpn_dns")

    val theme: Flow<AppTheme> = context.dataStore.data.map { prefs ->
        runCatching { AppTheme.valueOf(prefs[THEME] ?: AppTheme.DARK.name) }
            .getOrDefault(AppTheme.DARK)
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFS] ?: true }
    val notifPreview: Flow<Boolean> = context.dataStore.data.map { it[NOTIF_PREVIEW] ?: true }
    val mutedChatIds: Flow<Set<String>> = context.dataStore.data.map { it[MUTED_CHATS] ?: emptySet() }

    /** Raw multi-line proxy list as the user typed it (one proxy per line). */
    val proxyEndpoints: Flow<String> = context.dataStore.data.map { it[PROXY_ENDPOINTS] ?: "" }
    val proxyEnabled: Flow<Boolean> = context.dataStore.data.map { it[PROXY_ENABLED] ?: false }
    val proxyAllowDirect: Flow<Boolean> = context.dataStore.data.map { it[PROXY_ALLOW_DIRECT] ?: false }

    /** Fully resolved proxy config (parsed entries included), consumed by ProxyController. */
    val proxyConfig: Flow<ProxyConfig> = context.dataStore.data.map { prefs ->
        ProxyConfig(
            enabled = prefs[PROXY_ENABLED] ?: false,
            entries = parseProxyLines(prefs[PROXY_ENDPOINTS] ?: ""),
            allowDirect = prefs[PROXY_ALLOW_DIRECT] ?: false,
        )
    }

    val vpnConfig: Flow<VpnConfig> = context.dataStore.data.map { prefs ->
        VpnConfig(
            enabled = prefs[VPN_ENABLED] ?: false,
            outboundConfig = prefs[VPN_OUTBOUND_CONFIG] ?: "",
            useProxyList = prefs[VPN_USE_PROXY_LIST] ?: false,
            proxyEntries = parseProxyLines(prefs[PROXY_ENDPOINTS] ?: ""),
            splitApps = prefs[VPN_SPLIT_APPS] ?: emptySet(),
            ipv6 = prefs[VPN_IPV6] ?: false,
            dns = prefs[VPN_DNS] ?: "1.1.1.1",
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[THEME] = theme.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFS] = enabled }
    }

    suspend fun setNotifPreview(show: Boolean) {
        context.dataStore.edit { it[NOTIF_PREVIEW] = show }
    }

    suspend fun setMutedChatIds(ids: Set<String>) {
        context.dataStore.edit { it[MUTED_CHATS] = ids }
    }

    suspend fun setProxyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PROXY_ENABLED] = enabled }
    }

    suspend fun setProxyEndpoints(text: String) {
        context.dataStore.edit { it[PROXY_ENDPOINTS] = text }
    }

    suspend fun setProxyAllowDirect(allow: Boolean) {
        context.dataStore.edit { it[PROXY_ALLOW_DIRECT] = allow }
    }

    suspend fun setVpnEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VPN_ENABLED] = enabled }
    }

    suspend fun setVpnOutboundConfig(text: String) {
        context.dataStore.edit { it[VPN_OUTBOUND_CONFIG] = text }
    }

    suspend fun setVpnUseProxyList(use: Boolean) {
        context.dataStore.edit { it[VPN_USE_PROXY_LIST] = use }
    }

    suspend fun setVpnSplitApps(ids: Set<String>) {
        context.dataStore.edit { it[VPN_SPLIT_APPS] = ids }
    }

    suspend fun setVpnIpv6(enabled: Boolean) {
        context.dataStore.edit { it[VPN_IPV6] = enabled }
    }

    suspend fun setVpnDns(dns: String) {
        context.dataStore.edit { it[VPN_DNS] = dns }
    }
}
