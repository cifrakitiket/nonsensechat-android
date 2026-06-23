package com.nonsense.chat.data.cache

import com.nonsense.chat.data.DocRow
import com.nonsense.chat.model.Chat
import com.nonsense.chat.model.DocJson
import com.nonsense.chat.model.Message
import com.nonsense.chat.model.decodeDoc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin wrapper that exposes the Room cache in terms of [DocRow] — the same shape the repositories
 * already use for network reads — so offline-first wiring is a couple of lines per repo and Room
 * never leaks into the data layer.
 *
 * Every operation is wrapped in runCatching: the cache is a best-effort speed-up, so a disk error
 * must degrade to "no cache" (fall back to network), never crash the app.
 */
@Singleton
class DocCache @Inject constructor(private val db: AppDatabase) {

    private fun parse(doc: String): JsonObject = DocJson.parseToJsonElement(doc).jsonObject

    // ── Chats ────────────────────────────────────────────────────────────────
    suspend fun chats(): List<DocRow> =
        runCatching { db.chatCacheDao().all().map { DocRow(it.id, parse(it.doc)) } }.getOrDefault(emptyList())

    /** Replace the cached chat set with the authoritative server snapshot. */
    suspend fun replaceChats(rows: List<DocRow>) {
        runCatching {
            if (rows.isEmpty()) {
                db.chatCacheDao().clear()
            } else {
                db.chatCacheDao().upsert(rows.map { it.toChatEntity() })
                db.chatCacheDao().deleteNotIn(rows.map { it.id })
            }
        }
    }

    suspend fun putChat(row: DocRow) {
        runCatching { db.chatCacheDao().upsert(listOf(row.toChatEntity())) }
    }

    suspend fun removeChat(id: String) {
        runCatching { db.chatCacheDao().delete(id) }
    }

    // ── Messages ───────────────────────────────────────────────────────────────
    suspend fun messages(chatId: String, limit: Int): List<DocRow> =
        runCatching { db.messageCacheDao().newest(chatId, limit).map { DocRow(it.id, parse(it.doc)) } }
            .getOrDefault(emptyList())

    suspend fun putMessages(rows: List<DocRow>) {
        if (rows.isEmpty()) return
        runCatching { db.messageCacheDao().upsert(rows.map { it.toMessageEntity() }) }
    }

    suspend fun removeMessage(id: String) {
        runCatching { db.messageCacheDao().delete(id) }
    }

    // ── Users ────────────────────────────────────────────────────────────────
    suspend fun users(ids: List<String>): List<DocRow> {
        if (ids.isEmpty()) return emptyList()
        return runCatching { db.userCacheDao().get(ids).map { DocRow(it.id, parse(it.doc)) } }
            .getOrDefault(emptyList())
    }

    suspend fun user(id: String): DocRow? = users(listOf(id)).firstOrNull()

    suspend fun putUsers(rows: List<DocRow>) {
        if (rows.isEmpty()) return
        runCatching { db.userCacheDao().upsert(rows.map { CachedUser(it.id, it.doc.toString()) }) }
    }

    // ── DocRow → entity (denormalize the sort key from the decoded model) ───────
    private fun DocRow.toChatEntity(): CachedChat {
        val sort = runCatching { decodeDoc<Chat>(this).lastMsgAtInstant?.toEpochMilliseconds() }.getOrNull() ?: 0L
        return CachedChat(id = id, doc = doc.toString(), sortKey = sort)
    }

    private fun DocRow.toMessageEntity(): CachedMessage {
        val msg = decodeDoc<Message>(this)
        val sort = runCatching { msg.at_?.toEpochMilliseconds() }.getOrNull() ?: 0L
        return CachedMessage(id = id, chatId = msg.chatId, doc = doc.toString(), sortKey = sort)
    }
}
