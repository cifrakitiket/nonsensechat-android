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

/** Result of an upload: the public URL plus the (possibly recompressed) byte size. [thumbUrl] is a
 *  small (~320px) preview, present only when [StorageRepository.upload] was asked for a thumbnail. */
data class UploadResult(val url: String, val fileName: String, val size: Long, val thumbUrl: String? = null)

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
        thumbnail: Boolean = false,
    ): UploadResult = withContext(Dispatchers.IO) {
        val isImage = (contentType?.startsWith("image/") == true) || looksLikeImage(fileName)
        val payload = if (compressIfImage && isImage) compressImage(bytes) else bytes

        val uid = client.auth.currentUserOrNull()?.id ?: "anon"
        val clean = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val rand = Random.nextInt(0, 0xFFFFFF).toString(16)
        val stem = "$uid/${System.currentTimeMillis()}_$rand"
        val path = "${stem}_$clean"

        bucket.upload(path, payload) { upsert = false }

        // Upload a tiny preview alongside the full image so lists/bubbles can load a ~30KB thumb
        // instead of a 1–2MB original — the difference between "loads on a throttled pipe" and "times
        // out". A thumb failure must never fail the main upload, so it's best-effort.
        val thumbUrl = if (thumbnail && isImage) runCatching {
            val thumb = compressImage(bytes, maxDim = THUMB_DIM, maxBytes = THUMB_MAX_BYTES)
            val thumbPath = "${stem}_thumb_$clean"
            bucket.upload(thumbPath, thumb) { upsert = false }
            bucket.publicUrl(thumbPath)
        }.getOrNull() else null

        UploadResult(
            url = bucket.publicUrl(path),
            fileName = fileName,
            size = payload.size.toLong(),
            thumbUrl = thumbUrl,
        )
    }

    /** Uploads a deliberately small avatar (≤256px, ~25KB) and returns its public URL. Avatars are
     *  shown at 48–88dp, so a small image is plenty — and small enough to load on a blocked/throttled
     *  network where a full-size photo would never arrive. */
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String): String = withContext(Dispatchers.IO) {
        val small = compressImage(bytes, maxDim = AVATAR_DIM, maxBytes = AVATAR_MAX_BYTES)
        val uid = client.auth.currentUserOrNull()?.id ?: "anon"
        val clean = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val rand = Random.nextInt(0, 0xFFFFFF).toString(16)
        val path = "$uid/${System.currentTimeMillis()}_${rand}_avatar_$clean"
        bucket.upload(path, small) { upsert = false }
        bucket.publicUrl(path)
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

    private companion object {
        const val THUMB_DIM = 320          // long-edge px for chat-photo previews
        const val THUMB_MAX_BYTES = 50_000
        const val AVATAR_DIM = 256         // long-edge px for avatars (shown at 48–88dp)
        const val AVATAR_MAX_BYTES = 40_000
    }
}
