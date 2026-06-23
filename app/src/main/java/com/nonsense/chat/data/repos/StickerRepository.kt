package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.Tables
import com.nonsense.chat.model.Sticker
import com.nonsense.chat.model.StickerPack
import com.nonsense.chat.model.decodeDoc
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StickerRepository @Inject constructor(
    private val docs: DocRepository,
) {
    private fun decode(row: DocRow): StickerPack = decodeDoc<StickerPack>(row).copy(id = row.id)

    suspend fun allPacks(): List<StickerPack> = docs.all(Tables.STICKER_PACKS).map(::decode)

    suspend fun pack(packId: String): StickerPack? = docs.byId(Tables.STICKER_PACKS, packId)?.let(::decode)

    /** Add a pack to the user's installed set so its stickers show in the picker. */
    suspend fun installForUser(uid: String, packId: String) {
        docs.apply(Tables.USERS, uid, DocOps().arrayUnion(listOf("installedPacks"), packId).build())
    }

    suspend fun uninstallForUser(uid: String, packId: String) {
        docs.apply(Tables.USERS, uid, DocOps().arrayRemove(listOf("installedPacks"), packId).build())
    }

    private fun stickersJson(stickers: List<Sticker>) = buildJsonArray {
        stickers.forEach { add(buildJsonObject { put("url", it.url); put("emoji", it.emoji) }) }
    }

    /** Create a new pack authored by [authorUid] and auto-install it for them. */
    suspend fun createPack(authorUid: String, authorNick: String, title: String, stickers: List<Sticker>): String {
        val id = UUID.randomUUID().toString()
        val doc = buildJsonObject {
            put("title", title.trim().take(48))
            put("slug", title.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-'))
            put("authorUid", authorUid)
            put("authorNick", authorNick)
            put("stickers", stickersJson(stickers))
        }
        docs.apply(Tables.STICKER_PACKS, id, DocOps().setWhole(doc).serverNow("createdAt").build())
        installForUser(authorUid, id)
        return id
    }

    suspend fun updatePack(packId: String, title: String, stickers: List<Sticker>) {
        docs.apply(
            Tables.STICKER_PACKS, packId,
            DocOps()
                .set(listOf("title"), kotlinx.serialization.json.JsonPrimitive(title.trim().take(48)))
                .set(listOf("stickers"), stickersJson(stickers))
                .build(),
        )
    }

    suspend fun deletePack(packId: String) = docs.delete(Tables.STICKER_PACKS, packId)
}
