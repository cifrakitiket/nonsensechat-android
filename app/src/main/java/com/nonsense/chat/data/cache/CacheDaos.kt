package com.nonsense.chat.data.cache

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ChatCacheDao {
    @Query("SELECT * FROM cached_chats")
    suspend fun all(): List<CachedChat>

    @Upsert
    suspend fun upsert(items: List<CachedChat>)

    @Query("DELETE FROM cached_chats WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM cached_chats")
    suspend fun clear()

    /** Drop cached chats no longer in the authoritative membership set (e.g. user left/was removed). */
    @Query("DELETE FROM cached_chats WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)
}

@Dao
interface MessageCacheDao {
    @Query("SELECT * FROM cached_messages WHERE chatId = :chatId ORDER BY sortKey DESC, id DESC LIMIT :limit")
    suspend fun newest(chatId: String, limit: Int): List<CachedMessage>

    @Upsert
    suspend fun upsert(items: List<CachedMessage>)

    @Query("DELETE FROM cached_messages WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface UserCacheDao {
    @Query("SELECT * FROM cached_users WHERE id IN (:ids)")
    suspend fun get(ids: List<String>): List<CachedUser>

    @Upsert
    suspend fun upsert(items: List<CachedUser>)
}
