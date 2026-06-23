package com.nonsense.chat.model

import com.nonsense.chat.data.Timestamps
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

object MsgType {
    const val TEXT = "text"
    const val PHOTO = "photo"
    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val FILE = "file"
    const val STICKER = "sticker"
    const val POLL = "poll"
    const val SYSTEM = "system"
    const val CALL = "call"
}

@Serializable
data class ReplyTo(
    val id: String = "",
    val author: String = "",
    val text: String = "",
    val uid: String = "",
)

@Serializable
data class Poll(
    val question: String = "",
    val desc: String = "",
    val options: List<String> = emptyList(),
    val isQuiz: Boolean = false,
    val isMultiple: Boolean = false,
    val isAnonymous: Boolean = true,
    val votes: Map<String, JsonElement> = emptyMap(),
    val correctIdxs: List<Int> = emptyList(),
) {
    /** Option indices this user voted for (handles both single-int and array vote shapes). */
    fun votesOf(uid: String): Set<Int> = when (val v = votes[uid]) {
        is JsonArray -> v.mapNotNull { (it as? JsonPrimitive)?.intOrNull }.toSet()
        is JsonPrimitive -> v.intOrNull?.let { setOf(it) } ?: emptySet()
        else -> emptySet()
    }

    fun countFor(optionIndex: Int): Int = votes.keys.count { optionIndex in votesOf(it) }
    val totalVoters: Int get() = votes.keys.count { votesOf(it).isNotEmpty() }
    fun hasVoted(uid: String) = votesOf(uid).isNotEmpty()
}

@Serializable
data class Message(
    @Transient val id: String = "",
    val uid: String = "",
    val author: String = "",
    val at: JsonElement? = null,
    val type: String = MsgType.TEXT,
    val text: String = "",
    val reactions: Map<String, String> = emptyMap(), // userId → emoji (one reaction per user)
    val readAt: Map<String, JsonElement> = emptyMap(),
    val replyTo: ReplyTo? = null,
    val forwardFrom: String? = null,
    @SerialName("_deleted") val deleted: Boolean = false,
    // Photo:
    val photoUrl: String? = null,
    val photoThumbUrl: String? = null, // small preview; falls back to photoUrl on old messages
    val caption: String? = null,
    val isSpoiler: Boolean = false,
    // File:
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val mimeType: String? = null,
    val uploadedVia: String? = null,
    // Sticker:
    val stickerUrl: String? = null,
    val emoji: String? = null,
    val packId: String? = null,
    // Poll:
    val poll: Poll? = null,
    // Parent (stored in doc by the shim so trg_messages can derive chat_id):
    @SerialName("chat_id") val chatId: String = "",
    // Local-only optimistic upload state. These fields are never written to Supabase.
    @Transient val localUpload: Boolean = false,
    @Transient val localFailed: Boolean = false,
    @Transient val localBytes: ByteArray? = null,
) {
    val at_: Instant? get() = Timestamps.parse(at)
    val isSystem get() = type == MsgType.SYSTEM
    val isMine get() = false // resolved in UI against current uid

    /** True if at least one user other than [senderUid] has a readAt entry. */
    fun readByOther(senderUid: String): Boolean = readAt.keys.any { it != senderUid }

    /** Regroup the userId→emoji map into emoji→[userIds] for rendering reaction chips. */
    fun groupedReactions(): Map<String, List<String>> {
        val out = LinkedHashMap<String, MutableList<String>>()
        reactions.forEach { (uid, emoji) -> out.getOrPut(emoji) { mutableListOf() }.add(uid) }
        return out
    }

    fun reactionCount(emoji: String): Int = reactions.values.count { it == emoji }
    fun reactedBy(emoji: String, uid: String): Boolean = reactions[uid] == emoji
}
