package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.Tables
import kotlinx.serialization.json.JsonNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Online presence: a 45s heartbeat (matching the web client) writes online=true + lastSeen=now.
 * Typing-in for DMs is stored on the user doc (users.typingIn); group typing lives on the chat.
 */
@Singleton
class PresenceRepository @Inject constructor(
    private val docs: DocRepository,
) {
    suspend fun heartbeat(uid: String) {
        docs.apply(
            Tables.USERS, uid,
            DocOps().set("online", value = true).serverNow("lastSeen").build(),
        )
    }

    suspend fun setOffline(uid: String) {
        docs.apply(
            Tables.USERS, uid,
            DocOps().set("online", value = false).serverNow("lastSeen").build(),
        )
    }

    /** DM typing indicator (users.typingIn = chatId, or null to clear). */
    suspend fun setTypingIn(uid: String, chatId: String?) {
        val ops = DocOps()
        if (chatId == null) ops.set(listOf("typingIn"), JsonNull) else ops.set("typingIn", value = chatId)
        docs.apply(Tables.USERS, uid, ops.build())
    }

    companion object {
        const val HEARTBEAT_MS = 45_000L
        /** A user is "online" if seen within this window (web uses 2 minutes). */
        const val ONLINE_WINDOW_MS = 120_000L
    }
}
