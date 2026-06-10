package com.nonsense.chat.model

import com.nonsense.chat.data.Timestamps
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class PinnedMessage(
    val id: String = "",
    val text: String = "",
    val author: String = "",
)

object ChatType {
    const val DM = "dm"
    const val GROUP = "group"
    const val CHANNEL = "channel"
}

@Serializable
data class Chat(
    @Transient val id: String = "",
    val type: String = ChatType.DM,
    val members: List<String> = emptyList(),
    val lastMsg: String = "",
    val lastMsgAt: JsonElement? = null,
    val lastMsgUid: String = "",
    val readBy: Map<String, JsonElement> = emptyMap(),
    val pinnedBy: List<String> = emptyList(),
    val pinnedMsgs: List<PinnedMessage> = emptyList(),
    val archivedBy: List<String> = emptyList(),
    val mutedBy: List<String> = emptyList(),
    val typing: Map<String, Boolean> = emptyMap(),
    // Group / channel fields:
    val name: String = "",
    val desc: String = "",
    val avatar: String = "",
    val privacy: String = "private",
    val admin: String = "",
    val mods: List<String> = emptyList(),
) {
    val isGroup get() = type == ChatType.GROUP
    val isChannel get() = type == ChatType.CHANNEL
    val isDm get() = type == ChatType.DM
    val lastMsgAtInstant: Instant? get() = Timestamps.parse(lastMsgAt)

    /** The other participant's uid in a DM (for resolving title/avatar from their user doc). */
    fun otherMember(myUid: String): String? = members.firstOrNull { it != myUid }

    fun lastReadAt(uid: String): Instant? = Timestamps.parse(readBy[uid])
    fun isPinnedBy(uid: String) = uid in pinnedBy
    fun isArchivedBy(uid: String) = uid in archivedBy
    fun isMutedBy(uid: String) = uid in mutedBy
}
