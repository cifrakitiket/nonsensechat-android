package com.nonsense.chat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

/**
 * A live WebRTC call session, stored as one jsonb doc in `call_sessions` keyed by chatId — the
 * exact analogue of the web client's chats/{id}/callSession/current document.
 *
 *   participants[uid] = {muted, video}
 *   sdp["${from}__${to}"]  = {type, sdp}        (offer / answer addressed from→to)
 *   ice["${from}__${to}"]  = [ {sdpMid, sdpMLineIndex, candidate}, … ]
 */
@Serializable
data class CallSession(
    @Transient val id: String = "",
    val caller: String = "",
    val video: Boolean = false,
    val participants: Map<String, CallParticipant> = emptyMap(),
    val sdp: Map<String, SdpEntry> = emptyMap(),
    val ice: Map<String, List<IceEntry>> = emptyMap(),
)

@Serializable
data class CallParticipant(
    val muted: Boolean = false,
    val video: Boolean = false,
)

@Serializable
data class SdpEntry(
    val type: String = "",
    val sdp: String = "",
)

@Serializable
data class IceEntry(
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = "",
)
