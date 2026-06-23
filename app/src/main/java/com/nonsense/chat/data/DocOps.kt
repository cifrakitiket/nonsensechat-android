package com.nonsense.chat.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Builds the `_ops` array consumed by the doc_apply() RPC (supabase_schema.sql:151) — the exact
 * mirror of the web shim's mutation API.  An op is {op, path:[seg,...], value?}.
 *
 *   op ∈ set | serverNow | delete | increment | arrayUnion | arrayRemove
 *   path = []  ⇒  replace the whole document (used by create / .set()).
 */
class DocOps {
    private val ops = mutableListOf<JsonObject>()

    private fun pathArray(path: List<String>): JsonArray =
        buildJsonArray { path.forEach { add(JsonPrimitive(it)) } }

    private fun op(name: String, path: List<String>, value: JsonElement? = null): DocOps {
        ops += buildJsonObject {
            put("op", name)
            put("path", pathArray(path))
            if (value != null) put("value", value)
        }
        return this
    }

    /** Replace the entire document (path == []). Used for create/full-overwrite. */
    fun setWhole(value: JsonElement) = op("set", emptyList(), value)

    fun set(path: List<String>, value: JsonElement) = op("set", path, value)
    fun set(vararg path: String, value: JsonElement) = set(path.toList(), value)
    fun set(vararg path: String, value: String) = set(path.toList(), JsonPrimitive(value))
    fun set(vararg path: String, value: Boolean) = set(path.toList(), JsonPrimitive(value))
    fun set(vararg path: String, value: Number) = set(path.toList(), JsonPrimitive(value))

    /** Write the server timestamp sentinel {"__ts__": now()} at path. */
    fun serverNow(vararg path: String) = op("serverNow", path.toList())

    fun delete(vararg path: String) = op("delete", path.toList())

    fun increment(path: List<String>, by: Number = 1) =
        op("increment", path, JsonPrimitive(by))

    fun arrayUnion(path: List<String>, values: List<JsonElement>) =
        op("arrayUnion", path, JsonArray(values))

    fun arrayUnion(path: List<String>, vararg values: String) =
        arrayUnion(path, values.map { JsonPrimitive(it) })

    fun arrayRemove(path: List<String>, values: List<JsonElement>) =
        op("arrayRemove", path, JsonArray(values))

    fun arrayRemove(path: List<String>, vararg values: String) =
        arrayRemove(path, values.map { JsonPrimitive(it) })

    fun build(): JsonArray = JsonArray(ops.toList())

    fun isEmpty() = ops.isEmpty()

    companion object {
        /** Convenience: a single whole-document replace. */
        fun replace(doc: JsonElement): JsonArray = DocOps().setWhole(doc).build()
    }
}

/** Null-safe JSON primitive helpers used when building op values. */
fun jsonStr(s: String?): JsonElement = s?.let { JsonPrimitive(it) } ?: JsonNull
