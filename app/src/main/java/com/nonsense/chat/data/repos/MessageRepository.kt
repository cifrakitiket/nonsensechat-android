package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.data.cache.DocCache
import com.nonsense.chat.model.Message
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.model.ReplyTo
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
    private val cache: DocCache,
) {
    private fun decode(row: DocRow): Message = decodeDoc<Message>(row).copy(id = row.id)

    /**
     * Live, time-ordered message list for a chat, windowed to the newest [limit] messages so a long
     * thread doesn't load (and parse) its whole history up front. Emitting a larger value on [limit]
     * grows the window — the older page is fetched and merged in. [Int.MAX_VALUE] loads everything
     * (the default, used by callers like group-info photo grid that want the full history).
     */
    fun observeMessages(
        chatId: String,
        limit: Flow<Int> = flowOf(Int.MAX_VALUE),
    ): Flow<List<Message>> = channelFlow {
        val byId = HashMap<String, Message>()
        val lock = Mutex()

        // Window load: (re)fetch the newest [n] messages whenever the requested window grows, merging
        // into byId so realtime-delivered newer messages survive. throwOnFailure: if the INITIAL load
        // can't reach the server, propagate so ChatViewModel's retryWhen restarts and re-loads —
        // soft-failing to [] would leave the thread permanently blank (realtime only carries changes).
        launch {
            var seeded = false
            limit.collect { n ->
                // Offline-first: on the first window, paint cached messages instantly before the
                // network responds (so opening a chat shows history immediately, even offline).
                if (!seeded) {
                    seeded = true
                    val cached = cache.messages(chatId, n)
                    if (cached.isNotEmpty()) lock.withLock {
                        cached.forEach { byId[it.id] = decode(it) }
                        send(sorted(byId.values))
                    }
                }
                val rows = docs.whereEq(
                    Tables.MESSAGES, "chat_id", chatId,
                    orderBy = "at", ascending = false, limit = n, throwOnFailure = true,
                )
                lock.withLock {
                    rows.forEach { byId[it.id] = decode(it) }
                    send(sorted(byId.values))
                }
                cache.putMessages(rows) // write-through so next open is instant
            }
        }

        realtime.changes(Tables.MESSAGES).collect { change ->
            lock.withLock {
                when (change) {
                    is RowChange.Upsert -> {
                        val msg = decode(change.toRow())
                        if (msg.chatId == chatId) {
                            byId[msg.id] = msg
                            cache.putMessages(listOf(change.toRow()))
                        }
                    }
                    is RowChange.Removed -> {
                        byId.remove(change.id)
                        cache.removeMessage(change.id)
                    }
                }
                send(sorted(byId.values))
            }
        }
    }

    private fun sorted(msgs: Collection<Message>): List<Message> =
        msgs.sortedWith(compareBy({ it.at_?.toEpochMilliseconds() ?: 0L }, { it.id }))

    // ── Sending ──────────────────────────────────────────────────────────────

    private suspend fun post(
        chatId: String,
        uid: String,
        nick: String,
        preview: String,
        build: JsonObjectBuilder.() -> Unit,
    ): String {
        val id = UUID.randomUUID().toString()
        val doc = buildJsonObject {
            put("uid", uid)
            put("author", nick)
            put("chat_id", chatId)
            build()
        }
        val msgOps = DocOps().setWhole(doc).serverNow("at").build()
        val chatOps = DocOps()
            .set(listOf("lastMsg"), JsonPrimitive(preview))
            .set(listOf("lastMsgUid"), JsonPrimitive(uid))
            .serverNow("lastMsgAt")
            .build()
        docs.applyBatch(
            listOf(
                DocRepository.BatchItem(Tables.MESSAGES, id, msgOps),
                DocRepository.BatchItem(Tables.CHATS, chatId, chatOps),
            ),
        )
        return id
    }

    suspend fun sendText(
        chatId: String, uid: String, nick: String, text: String,
        replyTo: ReplyTo? = null, forwardFrom: String? = null,
    ) = post(chatId, uid, nick, text) {
        put("type", MsgType.TEXT)
        put("text", text)
        replyTo?.let { put("replyTo", buildJsonObject {
            put("id", it.id); put("author", it.author); put("text", it.text); put("uid", it.uid)
        }) }
        forwardFrom?.let { put("forwardFrom", it) }
    }

    suspend fun sendPhoto(
        chatId: String, uid: String, nick: String,
        photoUrl: String, fileName: String, fileSize: Long, caption: String?, isSpoiler: Boolean,
        photoThumbUrl: String? = null,
    ) = post(chatId, uid, nick, caption?.ifBlank { "📷 Photo" } ?: "📷 Photo") {
        put("type", MsgType.PHOTO)
        put("photoUrl", photoUrl)
        if (!photoThumbUrl.isNullOrBlank()) put("photoThumbUrl", photoThumbUrl)
        put("fileName", fileName)
        put("fileSize", fileSize)
        if (!caption.isNullOrBlank()) put("caption", caption)
        put("isSpoiler", isSpoiler)
    }

    suspend fun sendFile(
        chatId: String, uid: String, nick: String,
        fileUrl: String, fileName: String, fileSize: Long,
    ) = post(chatId, uid, nick, "📎 $fileName") {
        put("type", MsgType.FILE)
        put("fileUrl", fileUrl)
        put("fileName", fileName)
        put("fileSize", fileSize)
        put("uploadedVia", "supabase")
    }

    suspend fun sendSticker(
        chatId: String, uid: String, nick: String, stickerUrl: String, emoji: String, packId: String,
    ) = post(chatId, uid, nick, "$emoji Sticker") {
        put("type", MsgType.STICKER)
        put("stickerUrl", stickerUrl)
        put("emoji", emoji)
        put("packId", packId)
    }

    suspend fun sendPoll(chatId: String, uid: String, nick: String, poll: JsonElement, question: String) =
        post(chatId, uid, nick, "📊 $question") {
            put("type", MsgType.POLL)
            put("text", "")
            put("poll", poll)
        }

    /** Re-post [original] into [targetChatId] preserving its type, tagged with forwardFrom. */
    suspend fun forward(targetChatId: String, uid: String, nick: String, original: Message) {
        val from = original.author.ifBlank { "Unknown" }
        when (original.type) {
            MsgType.PHOTO -> post(targetChatId, uid, nick, original.caption?.ifBlank { "📷 Photo" } ?: "📷 Photo") {
                put("type", MsgType.PHOTO)
                original.photoUrl?.let { put("photoUrl", it) }
                original.photoThumbUrl?.let { put("photoThumbUrl", it) }
                original.fileName?.let { put("fileName", it) }
                original.fileSize?.let { put("fileSize", it) }
                original.caption?.let { if (it.isNotBlank()) put("caption", it) }
                put("isSpoiler", original.isSpoiler)
                put("forwardFrom", from)
            }
            MsgType.FILE -> post(targetChatId, uid, nick, "📎 ${original.fileName ?: "File"}") {
                put("type", MsgType.FILE)
                original.fileUrl?.let { put("fileUrl", it) }
                original.fileName?.let { put("fileName", it) }
                original.fileSize?.let { put("fileSize", it) }
                put("uploadedVia", original.uploadedVia ?: "supabase")
                put("forwardFrom", from)
            }
            MsgType.STICKER -> post(targetChatId, uid, nick, "${original.emoji.orEmpty()} Sticker") {
                put("type", MsgType.STICKER)
                original.stickerUrl?.let { put("stickerUrl", it) }
                put("emoji", original.emoji.orEmpty())
                put("packId", original.packId.orEmpty())
                put("forwardFrom", from)
            }
            else -> sendText(targetChatId, uid, nick, original.text, forwardFrom = from)
        }
    }

    // ── Edits / reactions / receipts ───────────────────────────────────────────

    suspend fun toggleReaction(messageId: String, emoji: String, uid: String, add: Boolean) {
        // Reactions are keyed by user: reactions[uid] = emoji (one per user), matching the web client.
        val ops = DocOps()
        if (add) ops.set(listOf("reactions", uid), JsonPrimitive(emoji))
        else ops.delete("reactions", uid)
        docs.apply(Tables.MESSAGES, messageId, ops.build())
    }

    suspend fun markRead(messageId: String, uid: String) {
        docs.apply(Tables.MESSAGES, messageId, DocOps().serverNow("readAt", uid).build())
    }

    suspend fun editText(messageId: String, text: String) {
        docs.apply(
            Tables.MESSAGES, messageId,
            DocOps().set(listOf("text"), JsonPrimitive(text)).set("edited", value = true).build(),
        )
    }

    suspend fun softDelete(messageId: String) {
        docs.apply(Tables.MESSAGES, messageId, DocOps().set("_deleted", value = true).build())
    }

    /** Set the poll vote for a user (int for single-choice, array for multiple). */
    suspend fun votePoll(messageId: String, uid: String, value: JsonElement) {
        docs.apply(Tables.MESSAGES, messageId, DocOps().set(listOf("poll", "votes", uid), value).build())
    }

    suspend fun retractVote(messageId: String, uid: String) {
        docs.apply(Tables.MESSAGES, messageId, DocOps().delete("poll", "votes", uid).build())
    }
}
