package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.model.FriendRequest
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
) {
    private fun decode(row: DocRow): FriendRequest = decodeDoc<FriendRequest>(row).copy(id = row.id)

    fun observeIncoming(uid: String): Flow<List<FriendRequest>> = channelFlow {
        val byId = HashMap<String, FriendRequest>()
        docs.whereEq(Tables.FRIEND_REQUESTS, "to_uid", uid).forEach { byId[it.id] = decode(it) }
        send(byId.values.toList())

        realtime.changes(Tables.FRIEND_REQUESTS).collect { change ->
            when (change) {
                is RowChange.Upsert -> decode(change.toRow()).let { if (it.to == uid) byId[it.id] = it }
                is RowChange.Removed -> byId.remove(change.id)
            }
            send(byId.values.toList())
        }
    }

    suspend fun send(fromUid: String, fromNick: String, toUid: String, toNick: String): String {
        val id = UUID.randomUUID().toString()
        val doc = buildJsonObject {
            put("from", fromUid); put("fromNick", fromNick)
            put("to", toUid); put("toNick", toNick)
        }
        docs.apply(Tables.FRIEND_REQUESTS, id, DocOps().setWhole(doc).build())
        return id
    }

    suspend fun remove(requestId: String) = docs.delete(Tables.FRIEND_REQUESTS, requestId)
}
