package com.nonsense.chat.data.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VpnStateRepository @Inject constructor() {
    private val _state = MutableStateFlow(VpnState())
    val state: StateFlow<VpnState> = _state.asStateFlow()

    fun setStage(stage: VpnStage, error: String? = null) {
        _state.value = VpnState(stage = stage, lastError = error)
    }

    fun setTraffic(up: Long, down: Long) {
        _state.update { it.copy(upBytes = up, downBytes = down) }
    }
}
