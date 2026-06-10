package com.nonsense.chat.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/** Result of an upload: the public URL plus the (possibly recompressed) byte size. */
data class UploadResult(val url: String, val fileName: String, val size: Long)

/**
 * Uploads to the public `uploads` bucket and returns the public URL — same path scheme and URL
 * shape as the web client's uploadToSupabase() (index.html:4066).  Images are compressed first.
 */
@Singleton
class StorageRepository @Inject constructor(
    private val client: SupabaseClient,
    @ApplicationContext private val context: Context,
) {
    private val bucket get() = client.storage.from(SupabaseConfig.BUCKET)

    suspend fun upload(
        bytes: ByteArray,
        fileName: String,
        contentType: String? = null,
        compressIfImage: Boolean = true,
    ): UploadResult = withContext(Dispatchers.IO) {
        val isImage = (contentType?.startsWith("image/") == true) || looksLikeImage(fileName)
        val payload = if (compressIfImage && isImage) compressImage(bytes) else bytes

        val uid = client.auth.currentUserOrNull()?.id ?: "anon"
        val clean = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val rand = Random.nextInt(0, 0xFFFFFF).toString(16)
        val path = "$uid/${System.currentTimeMillis()}_${rand}_$clean"

        bucket.upload(path, payload) { upsert = false }
        UploadResult(
            url = bucket.publicUrl(path),
            fileName = fileName,
            size = payload.size.toLong(),
        )
    }

    private fun looksLikeImage(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

    /** Scale to ≤2048px on the long edge and JPEG-compress to roughly ≤2MB. */
    private fun compressImage(bytes: ByteArray, maxDim: Int = 2048, maxBytes: Int = 2_000_000): ByteArray {
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val longEdge = maxOf(bmp.width, bmp.height)
        if (longEdge > maxDim) {
            val scale = maxDim.toFloat() / longEdge
            bmp = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * scale).toInt().coerceAtLeast(1),
                (bmp.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        }
        var quality = 90
        var out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
        while (out.size() > maxBytes && quality > 40) {
            quality -= 10
            out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        return out.toByteArray()
    }
}
