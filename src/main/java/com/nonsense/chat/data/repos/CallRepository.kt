package com.nonsense.chat.data.repos

import com.nonsense.chat.data.CallSession
import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.IceEntry
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** Signalling layer for WebRTC calls over the `call_sessions` doc-table. */
@Singleton
class CallRepository @Inject constructor(
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
) {
    private fun decode(row: DocRow): CallSession = decodeDoc<CallSession>(row).copy(id = row.id)

    suspend fun get(chatId: String): CallSession? = docs.byId(Tables.CALL_SESSIONS, chatId)?.let(::decode)

    fun observe(chatId: String): Flow<CallSession?> = channelFlow {
        send(get(chatId))
        realtime.changes(Tables.CALL_SESSIONS).collect { change ->
            if (change.id != chatId) return@collect
            when (change) {
                is RowChange.Upsert -> send(decode(change.toRow()))
                is RowChange.Removed -> send(null)
            }
        }
    }

    /** Add/refresh my participant entry, creating the session (and setting caller) if new. */
    suspend fun join(chatId: String, uid: String, video: Boolean, asCaller: Boolean) {
        val ops = DocOps()
            .set(listOf("participants", uid), buildJsonObject {
                put("muted", false); put("video", video)
            })
        if (asCaller) ops.set(listOf("caller"), JsonPrimitive(uid)).set(listOf("video"), JsonPrimitive(video))
        docs.apply(Tables.CALL_SESSIONS, chatId, ops.build())
    }

    suspend fun setMuted(chatId: String, uid: String, muted: Boolean) {
        docs.apply(Tables.CALL_SESSIONS, chatId, DocOps().set(listOf("participants", uid, "muted"), JsonPrimitive(muted)).build())
    }

    suspend fun setVideo(chatId: String, uid: String, video: Boolean) {
        docs.apply(Tables.CALL_SESSIONS, chatId, DocOps().set(listOf("participants", uid, "video"), JsonPrimitive(video)).build())
    }

    suspend fun setSdp(chatId: String, from: String, to: String, type: String, sdp: String) {
        docs.apply(
            Tables.CALL_SESSIONS, chatId,
            DocOps().set(listOf("sdp", "${from}__${to}"), buildJsonObject { put("type", type); put("sdp", sdp) }).build(),
        )
    }

    suspend fun addIce(chatId: String, from: String, to: String, ice: IceEntry) {
        val obj = buildJsonObject {
            put("sdpMid", ice.sdpMid); put("sdpMLineIndex", ice.sdpMLineIndex); put("candidate", ice.candidate)
        }
        docs.apply(Tables.CALL_SESSIONS, chatId, DocOps().arrayUnion(listOf("ice", "${from}__${to}"), listOf(obj)).build())
    }

    /** Remove my participant + my signalling entries. The last leaver deletes the whole session. */
    suspend fun leave(chatId: String, uid: String, lastOne: Boolean) {
        if (lastOne) { docs.delete(Tables.CALL_SESSIONS, chatId); return }
        docs.apply(Tables.CALL_SESSIONS, chatId, DocOps().delete("participants", uid).build())
    }

    suspend fun end(chatId: String) = docs.delete(Tables.CALL_SESSIONS, chatId)
}
