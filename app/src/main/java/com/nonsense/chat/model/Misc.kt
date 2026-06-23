package com.nonsense.chat.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Folder(
    @Transient val id: String = "",
    val name: String = "",
    val icon: String = "📁",
    val chats: List<String> = emptyList(),
    val order: Double = 0.0,
    @SerialName("user_id") val userId: String = "",
)

@Serializable
data class FriendRequest(
    @Transient val id: String = "",
    val from: String = "",
    val fromNick: String = "",
    val to: String = "",
    val toNick: String = "",
)

@Serializable
data class Sticker(
    val url: String = "",
    val emoji: String = "",
)

@Serializable
data class StickerPack(
    @Transient val id: String = "",
    val title: String = "",
    val slug: String = "",
    val authorUid: String = "",
    val authorNick: String = "",
    val stickers: List<Sticker> = emptyList(),
)
