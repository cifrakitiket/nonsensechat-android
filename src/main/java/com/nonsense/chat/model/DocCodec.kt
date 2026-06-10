package com.nonsense.chat.model

import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RowChange
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

/** Lenient JSON used to map a row's `doc` jsonb onto model classes (extra keys ignored). */
val DocJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
}

inline fun <reified T> decodeDoc(row: DocRow): T = DocJson.decodeFromJsonElement(row.doc)

/** Realtime upsert → a DocRow, so the same decoder works for fetch and live updates. */
fun RowChange.Upsert.toRow(): DocRow = DocRow(id, doc)
