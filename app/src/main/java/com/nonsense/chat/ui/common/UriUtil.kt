package com.nonsense.chat.ui.common

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

data class PickedFile(val bytes: ByteArray, val name: String, val mime: String?)

/** Read a content:// URI into bytes plus its display name and mime type. */
fun readUri(context: Context, uri: Uri): PickedFile? {
    val resolver = context.contentResolver
    val bytes = runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull() ?: return null
    var name = "file"
    runCatching {
        resolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) name = c.getString(idx) ?: name
        }
    }
    return PickedFile(bytes, name, resolver.getType(uri))
}
