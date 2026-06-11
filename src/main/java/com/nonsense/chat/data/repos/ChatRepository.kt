package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.model.Chat
import com.nonsense.chat.model.ChatType
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
) {
    private fun decode(row: DocRow): Chat = decodeDoc<Chat>(row).copy(id = row.id)

    suspend fun get(chatId: String): Chat? = docs.byId(Tables.CHATS, chatId)?.let(::decode)

    /** Live list of all chats the user is a member of, newest activity first. */
    fun observeMyChats(uid: String): Flow<List<Chat>> = channelFlow {
        val byId = LinkedHashMap<String, Chat>()
        // Initial load: on this flaky network the REST call can time out. Retry a few times instead
        // of throwing (which would crash the collecting ViewModel); emit whatever we have meanwhile.
        var loaded = false
        var attempt = 0
        while (!loaded && attempt < 6) {
            runCatching {
                docs.whereContains(Tables.CHATS, "members", listOf(uid)).forEach { byId[it.id] = decode(it) }
            }.onSuccess { loaded = true }
                .onFailure { android.util.Log.e("NSDIAG", "observeMyChats load attempt=$attempt failed: ${it.message}") }
            if (!loaded) {
                attempt++
                kotlinx.coroutines.delay(800L * attempt)
            }
        }
        send(sorted(byId.values))

        realtime.changes(Tables.CHATS).collect { change ->
            when (change) {
                is RowChange.Upsert -> {
                    val chat = decode(change.toRow())
                    if (uid in chat.members) byId[chat.id] = chat else byId.remove(chat.id)
                }
                is RowChange.Removed -> byId.remove(change.id)
            }
            send(sorted(byId.values))
        }
    }

    fun observe(chatId: String): Flow<Chat?> = channelFlow {
        send(get(chatId))
        realtime.changes(Tables.CHATS).collect { change ->
            if (change.id != chatId) return@collect
            when (change) {
                is RowChange.Upsert -> send(decode(change.toRow()))
                is RowChange.Removed -> send(null)
            }
        }
    }

    private fun sorted(chats: Collection<Chat>): List<Chat> =
        chats.sortedByDescending { it.lastMsgAtInstant?.toEpochMilliseconds() ?: 0L }

    // ── Creation ─────────────────────────────────────────────────────────────

    /** Returns the existing DM with [otherUid] if any, else creates one. */
    suspend fun getOrCreateDm(myUid: String, otherUid: String): String {
        findDm(myUid, otherUid)?.let { return it }

        val id = UUID.randomUUID().toString()
        val doc = buildJsonObject {
            put("type", ChatType.DM)
            put("members", buildJsonArray { add(myUid); add(otherUid) })
            put("lastMsg", "")
            put("lastMsgUid", "")
        }
        docs.apply(Tables.CHATS, id, DocOps().setWhole(doc).build())
        return id
    }

    /** Existing DM id between the two users, or null — never creates. */
    suspend fun findDm(myUid: String, otherUid: String): String? =
        docs.whereContains(Tables.CHATS, "members", listOf(myUid, otherUid))
            .map(::decode)
            .firstOrNull { it.isDm && it.members.size == 2 && otherUid in it.members }
            ?.id

    suspend fun createGroup(myUid: String, name: String, memberUids: List<String>, privacy: String = "private"): String {
        val id = UUID.randomUUID().toString()
        val members = (listOf(myUid) + memberUids).distinct()
        val doc = buildJsonObject {
            put("type", ChatType.GROUP)
            put("name", name.trim())
            put("admin", myUid)
            put("privacy", privacy)
            put("members", buildJsonArray { members.forEach { add(it) } })
            put("lastMsg", "")
        }
        docs.apply(Tables.CHATS, id, DocOps().setWhole(doc).build())
        return id
    }

    // ── State updates ──────────────────────────────────────────────────────────

    suspend fun markRead(chatId: String, uid: String) {
        docs.apply(Tables.CHATS, chatId, DocOps().serverNow("readBy", uid).build())
    }

    suspend fun setPinned(chatId: String, uid: String, pinned: Boolean) {
        val ops = DocOps()
        if (pinned) ops.arrayUnion(listOf("pinnedBy"), uid) else ops.arrayRemove(listOf("pinnedBy"), uid)
        docs.apply(Tables.CHATS, chatId, ops.build())
    }

    suspend fun setArchived(chatId: String, uid: String, archived: Boolean) {
        val ops = DocOps()
        if (archived) ops.arrayUnion(listOf("archivedBy"), uid) else ops.arrayRemove(listOf("archivedBy"), uid)
        docs.apply(Tables.CHATS, chatId, ops.build())
    }

    suspend fun setMuted(chatId: String, uid: String, muted: Boolean) {
        val ops = DocOps()
        if (muted) ops.arrayUnion(listOf("mutedBy"), uid) else ops.arrayRemove(listOf("mutedBy"), uid)
        docs.apply(Tables.CHATS, chatId, ops.build())
    }

    /** Pin/unpin a message inside a chat (chats.pinnedMsgs[]). */
    suspend fun setMessagePinned(chatId: String, pinned: List<com.nonsense.chat.model.PinnedMessage>) {
        val arr = buildJsonArray {
            pinned.forEach { pm ->
                add(buildJsonObject {
                    put("id", pm.id); put("text", pm.text); put("author", pm.author)
                })
            }
        }
        docs.apply(Tables.CHATS, chatId, DocOps().set(listOf("pinnedMsgs"), arr).build())
    }

    /** Group typing: chats.typing[uid] = true/false. */
    suspend fun setGroupTyping(chatId: String, uid: String, typing: Boolean) {
        docs.apply(Tables.CHATS, chatId, DocOps().set(listOf("typing", uid), JsonPrimitive(typing)).build())
    }

    suspend fun updateGroup(chatId: String, fields: Map<String, String>) {
        val ops = DocOps()
        fields.forEach { (k, v) -> ops.set(listOf(k), JsonPrimitive(v)) }
        if (!ops.isEmpty()) docs.apply(Tables.CHATS, chatId, ops.build())
    }

    suspend fun addMembers(chatId: String, uids: List<String>) {
        docs.apply(Tables.CHATS, chatId, DocOps().arrayUnion(listOf("members"), uids.map { JsonPrimitive(it) }).build())
    }

    suspend fun removeMember(chatId: String, uid: String) {
        docs.apply(Tables.CHATS, chatId, DocOps().arrayRemove(listOf("members"), uid).build())
    }

    suspend fun promoteMod(chatId: String, uid: String) {
        docs.apply(Tables.CHATS, chatId, DocOps().arrayUnion(listOf("mods"), uid).build())
    }

    suspend fun demoteMod(chatId: String, uid: String) {
        docs.apply(Tables.CHATS, chatId, DocOps().arrayRemove(listOf("mods"), uid).build())
    }

    suspend fun leave(chatId: String, uid: String) = removeMember(chatId, uid)

    suspend fun deleteChat(chatId: String) = docs.delete(Tables.CHATS, chatId)
}
