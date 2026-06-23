package com.nonsense.chat.model

import com.nonsense.chat.data.Timestamps
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    @Transient val id: String = "",
    val uid: String = "",
    val nick: String = "",
    val nickLower: String = "",
    val avatar: String = "",
    val bio: String = "",
    val fname: String = "",
    val lname: String = "",
    val bday: String = "",
    val phone: String = "",
    val username: String = "",
    val online: Boolean = false,
    val lastSeen: JsonElement? = null,
    val hideLastSeen: Boolean = false,
    val verified: Boolean = false,
    val typingIn: String? = null,
    val installedPacks: List<String> = emptyList(),
    val fcmTokens: List<String> = emptyList(),
) {
    val lastSeenAt: Instant? get() = Timestamps.parse(lastSeen)

    val displayName: String
        get() = nick.ifBlank { listOf(fname, lname).filter { it.isNotBlank() }.joinToString(" ") }
            .ifBlank { "User" }
}
