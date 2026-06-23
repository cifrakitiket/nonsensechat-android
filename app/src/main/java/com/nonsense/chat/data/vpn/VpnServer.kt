package com.nonsense.chat.data.vpn

import kotlinx.serialization.Serializable

@Serializable
data class VpnServer(
    val id: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val source: String,
    val outboundJson: String,
)
