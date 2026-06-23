package com.nonsense.chat.data.cache

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Offline cache rows. Each mirrors a Supabase doc-table row: the primary key + the raw jsonb
// `doc` stored verbatim as a JSON string (decoded with the same DocJson the network layer uses).
// A denormalized `sortKey` (epoch ms) is stored so the DB can order without parsing every doc.

@Entity(tableName = "cached_chats")
data class CachedChat(
    @PrimaryKey val id: String,
    val doc: String,
    val sortKey: Long, // chat.lastMsgAt epoch ms (newest first)
)

@Entity(
    tableName = "cached_messages",
    indices = [Index("chatId"), Index("sortKey")],
)
data class CachedMessage(
    @PrimaryKey val id: String,
    val chatId: String,
    val doc: String,
    val sortKey: Long, // message.at epoch ms
)

@Entity(tableName = "cached_users")
data class CachedUser(
    @PrimaryKey val id: String,
    val doc: String,
)
