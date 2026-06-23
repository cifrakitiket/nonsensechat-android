package com.nonsense.chat.ui.vpn

import android.content.Context
import android.net.VpnService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.SettingsStore
import com.nonsense.chat.data.vpn.NonsenseVpnService
import com.nonsense.chat.data.vpn.VpnConfig
import com.nonsense.chat.data.vpn.VpnState
import com.nonsense.chat.data.vpn.VpnStateRepository
import com.nonsense.chat.data.vpn.VpnStage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VpnViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsStore,
    states: VpnStateRepository,
) : ViewModel() {
    val state: StateFlow<VpnState> = states.state
    val config: Flow<VpnConfig> = settings.vpnConfig

    private val _prepareEvents = Channel<Unit>(Channel.BUFFERED)
    val prepareEvents = _prepareEvents.receiveAsFlow()

    fun connect() {
        viewModelScope.launch {
            settings.setVpnEnabled(true)
            if (VpnService.prepare(context) != null) _prepareEvents.send(Unit)
            else NonsenseVpnService.start(context)
        }
    }

    fun onVpnPrepared() {
        NonsenseVpnService.start(context)
    }

    fun disconnect() {
        viewModelScope.launch { settings.setVpnEnabled(false) }
        NonsenseVpnService.stop(context)
    }

    fun toggle() {
        if (state.value.stage == VpnStage.CONNECTED || state.value.stage == VpnStage.CONNECTING) {
            disconnect()
        } else {
            connect()
        }
    }

    fun setOutbound(text: String) {
        viewModelScope.launch { settings.setVpnOutboundConfig(text) }
    }

    fun setUseProxyList(use: Boolean) {
        viewModelScope.launch { settings.setVpnUseProxyList(use) }
    }

    fun setIpv6(enabled: Boolean) {
        viewModelScope.launch { settings.setVpnIpv6(enabled) }
    }

    fun setDns(dns: String) {
        viewModelScope.launch { settings.setVpnDns(dns) }
    }
}
