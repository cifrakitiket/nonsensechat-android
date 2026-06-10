package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.model.Folder
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
) {
    private fun decode(row: DocRow): Folder = decodeDoc<Folder>(row).copy(id = row.id)

    fun observe(uid: String): Flow<List<Folder>> = channelFlow {
        val byId = HashMap<String, Folder>()
        docs.whereEq(Tables.FOLDERS, "user_id", uid, orderBy = "ord", ascending = true)
            .forEach { byId[it.id] = decode(it) }
        send(byId.values.sortedBy { it.order })

        realtime.changes(Tables.FOLDERS).collect { change ->
            when (change) {
                is RowChange.Upsert -> decode(change.toRow()).let { if (it.userId == uid) byId[it.id] = it }
                is RowChange.Removed -> byId.remove(change.id)
            }
            send(byId.values.sortedBy { it.order })
        }
    }

    suspend fun create(uid: String, name: String, icon: String): String {
        val id = UUID.randomUUID().toString()
        val doc = buildJsonObject {
            put("user_id", uid)
            put("name", name.trim())
            put("icon", icon)
            put("order", System.currentTimeMillis().toDouble())
            put("chats", buildJsonArray { })
        }
        docs.apply(Tables.FOLDERS, id, DocOps().setWhole(doc).build())
        return id
    }

    suspend fun rename(folderId: String, name: String, icon: String) {
        docs.apply(
            Tables.FOLDERS, folderId,
            DocOps().set(listOf("name"), JsonPrimitive(name)).set(listOf("icon"), JsonPrimitive(icon)).build(),
        )
    }

    suspend fun setChatInFolder(folderId: String, chatId: String, present: Boolean) {
        val ops = DocOps()
        if (present) ops.arrayUnion(listOf("chats"), chatId) else ops.arrayRemove(listOf("chats"), chatId)
        docs.apply(Tables.FOLDERS, folderId, ops.build())
    }

    suspend fun delete(folderId: String) = docs.delete(Tables.FOLDERS, folderId)
}
