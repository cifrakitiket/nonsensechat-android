package com.nonsense.chat.data

import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Server timestamps are stored as the sentinel object {"__ts__": "<iso>"} — the doc_apply
 * `serverNow` op writes now() into that shape, and trg_messages reads doc#>>'{at,__ts__}'.
 * (see supabase_schema.sql:93, :177).  These helpers convert that shape to/from Instant.
 */
object Timestamps {

    const val SENTINEL_KEY = "__ts__"

    /** Parse a jsonb value that may be {"__ts__":"iso"}, a bare iso string, or epoch millis. */
    fun parse(element: JsonElement?): Instant? {
        if (element == null) return null
        return when (element) {
            is JsonObject -> element[SENTINEL_KEY]?.let { parsePrimitive(it) }
            is JsonPrimitive -> parsePrimitive(element)
            else -> null
        }
    }

    private fun parsePrimitive(p: JsonElement): Instant? {
        val prim = (p as? JsonPrimitive) ?: return null
        prim.longOrNull?.let { return Instant.fromEpochMilliseconds(it) }
        val s = prim.contentOrNull ?: return null
        return runCatching { Instant.parse(s) }.getOrNull()
    }
}
