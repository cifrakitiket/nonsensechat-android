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
) {
    private val pg get() = client.postgrest

    // ── Mutations (RPC) ──────────────────────────────────────────────────────

    suspend fun apply(table: String, id: String, ops: JsonArray) {
        pg.rpc("doc_apply", buildJsonObject {
            put("_table", table)
            put("_id", id)
            put("_ops", ops)
        })
    }

    /** Batch many doc_apply calls in one transaction (mirrors db.batch().commit()). */
    suspend fun applyBatch(items: List<BatchItem>) {
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

    suspend fun delete(table: String, id: String) {
        pg.rpc("doc_delete", buildJsonObject {
            put("_table", table)
            put("_id", id)
        })
    }

    data class BatchItem(val table: String, val id: String, val ops: JsonArray)

    // ── Reads ────────────────────────────────────────────────────────────────

    suspend fun byId(table: String, id: String): DocRow? =
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { eq("id", id) }
            limit(1)
        }.decodeSingleOrNull()

    suspend fun all(table: String): List<DocRow> =
        pg.from(table).select(Columns.list("id", "doc")).decodeList()

    /** Equality filter on a derived column (e.g. chat_id, user_id, to_uid). */
    suspend fun whereEq(
        table: String,
        column: String,
        value: String,
        orderBy: String? = null,
        ascending: Boolean = true,
    ): List<DocRow> =
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { eq(column, value) }
            if (orderBy != null) order(orderBy, if (ascending) Order.ASCENDING else Order.DESCENDING)
        }.decodeList()

    /** Array-contains filter (e.g. chats where members @> [uid]). */
    suspend fun whereContains(
        table: String,
        column: String,
        values: List<String>,
    ): List<DocRow> =
        pg.from(table).select(Columns.list("id", "doc")) {
            filter { contains(column, values) }
        }.decodeList()
}
