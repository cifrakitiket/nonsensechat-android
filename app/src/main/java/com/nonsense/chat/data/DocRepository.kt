package com.nonsense.chat.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** A row of any doc-table: just the primary key + the jsonb document. */
@Serializable
data class DocRow(
    val id: String,
    val doc: JsonObject = JsonObject(emptyMap()),
)

/**
 * Central access to the doc-jsonb tables: mutations go through the doc_apply* RPCs (atomic,
 * never lose concurrent array writes), reads pull `id,doc` and let repos map `doc` to models.
 */
@Singleton
class DocRepository @Inject constructor(
    private val client: SupabaseClient,
    private val connection: ConnectionMonitor,
) {
    private val pg get() = client.postgrest

    // ── Mutations (RPC) ──────────────────────────────────────────────────────
    //
    // Writes are fired from viewModelScope.launch {} blocks with NO try/catch (sendText, markRead,
    // setTyping, reactions, pins, …). On this flaky network a write that hangs the full timeout used
    // to THROW and crash the app — e.g. entering a chat fired markRead, which timed out after ~6s and
    // killed the process. So every mutation now RETRIES and, if it still fails, SOFT-FAILS (logs +
    // swallows) instead of throwing. This is safe because every doc_apply op is IDEMPOTENT: messages
    // use a fixed client-side UUID (re-applying the same id can't duplicate), and set/serverNow/delete/
    // arrayUnion/arrayRemove all converge to the same state. (increment is never used for writes.)
    // Realtime + the next re-subscription reconcile anything that didn't land.

    private suspend fun retryWrite(label: String, block: suspend () -> Unit) {
        connection.onStart()
        try {
            var last: Throwable? = null
            repeat(3) { attempt ->
                try {
                    block()
                    connection.onSuccess()
                    return
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    last = e
                    connection.onAttemptFailed()
                    android.util.Log.e("NSDIAG", "DocRepository write[$label] failed (attempt=$attempt): ${e.message}")
                    kotlinx.coroutines.delay(600L * (attempt + 1))
                }
            }
            android.util.Log.e("NSDIAG", "DocRepository write[$label] gave up, soft-failing: ${last?.message}")
        } finally {
            connection.onEnd()
        }
    }

    suspend fun apply(table: String, id: String, ops: JsonArray) = retryWrite("apply:$table") {
        pg.rpc("doc_apply", buildJsonObject {
            put("_table", table)
            put("_id", id)
            put("_ops", ops)
        })
    }

    /** Batch many doc_apply calls in one transaction (mirrors db.batch().commit()). */
    suspend fun applyBatch(items: List<BatchItem>) = retryWrite("applyBatch") {
        val arr = buildJsonArray {
            items.forEach { item ->
                add(buildJsonObject {
                    put("table", item.table)
                    put("id", item.id)
                    put("ops", item.ops)
                })
            }
        }
        pg.rpc("doc_apply_batch", buildJsonObject { put("_items", arr) })
    }

    suspend fun delete(table: String, id: String) = retryWrite("delete:$table") {
        pg.rpc("doc_delete", buildJsonObject {
            put("_table", table)
            put("_id", id)
        })
    }

    data class BatchItem(val table: String, val id: String, val ops: JsonArray)

    // ── Reads ────────────────────────────────────────────────────────────────
    //
    // This user's device/network intermittently hangs REST requests until they time out. A thrown
    // timeout here used to propagate up through channelFlow `observe*` collectors into a
    // viewModelScope and CRASH the app (chat list, messages, …). So every read RETRIES a few times
    // and, if it still fails, SOFT-FAILS to a safe default (null / emptyList) instead of throwing.
    // Realtime + the next re-subscription refill the data once the network recovers.

    //
    // `throwOnFailure`: by default a read that exhausts its retries SOFT-FAILS to `default`
    // (null / emptyList) so a one-shot read can't crash the app. But for a flow's INITIAL load of
    // primary content (chats, messages) that's wrong: an empty list is indistinguishable from "the
    // network failed", so the flow would send `[]`, mark itself loaded, and then sit on realtime —
    // which only carries *changes*, never backfills existing rows. The list would stay empty until
    // the app restarts, even after the network recovers. Those callers pass throwOnFailure=true so
    // the failure propagates to their flow's retryWhen, which re-runs the load until it truly works.
    private suspend fun <T> retryRead(default: T, throwOnFailure: Boolean = false, block: suspend () -> T): T {
        connection.onStart()
        try {
            var last: Throwable? = null
            repeat(4) { attempt ->
                try {
                    val result = block()
                    connection.onSuccess()
                    return result
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    last = e
                    connection.onAttemptFailed()
                    android.util.Log.e("NSDIAG", "DocRepository read failed (attempt=$attempt): ${e.message}")
                    kotlinx.coroutines.delay(600L * (attempt + 1))
                }
            }
            if (throwOnFailure) {
                android.util.Log.e("NSDIAG", "DocRepository read gave up, rethrowing: ${last?.message}")
                throw (last ?: IllegalStateException("DocRepository read failed"))
            }
            android.util.Log.e("NSDIAG", "DocRepository read gave up, soft-failing: ${last?.message}")
            return default
        } finally {
            connection.onEnd()
        }
    }

    suspend fun byId(table: String, id: String): DocRow? = retryRead<DocRow?>(null) {
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { eq("id", id) }
            limit(1)
        }.decodeSingleOrNull()
    }

    suspend fun all(table: String): List<DocRow> = retryRead(emptyList()) {
        pg.from(table).select(Columns.list("id", "doc")).decodeList()
    }

    /** Equality filter on a derived column (e.g. chat_id, user_id, to_uid). */
    suspend fun whereEq(
        table: String,
        column: String,
        value: String,
        orderBy: String? = null,
        ascending: Boolean = true,
        limit: Int? = null,
        throwOnFailure: Boolean = false,
    ): List<DocRow> = retryRead(emptyList(), throwOnFailure) {
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { eq(column, value) }
            if (orderBy != null) order(orderBy, if (ascending) Order.ASCENDING else Order.DESCENDING)
            // Int.MAX_VALUE means "no cap" (load everything) — skip the clause entirely.
            if (limit != null && limit != Int.MAX_VALUE) limit(limit.toLong())
        }.decodeList()
    }

    /** Array-contains filter (e.g. chats where members @> [uid]). */
    suspend fun whereContains(
        table: String,
        column: String,
        values: List<String>,
        throwOnFailure: Boolean = false,
    ): List<DocRow> = retryRead(emptyList(), throwOnFailure) {
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { contains(column, values) }
        }.decodeList()
    }

    /** `column IN (values)` — fetch many rows by key in ONE round trip (avoids N+1 per-id reads). */
    suspend fun whereIn(
        table: String,
        column: String,
        values: List<String>,
        throwOnFailure: Boolean = false,
    ): List<DocRow> {
        if (values.isEmpty()) return emptyList()
        return retryRead(emptyList(), throwOnFailure) {
            pg.from(table).select(Columns.list("id", "doc")) {
                filter { isIn(column, values) }
            }.decodeList()
        }
    }
}
