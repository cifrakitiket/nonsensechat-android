package com.nonsense.chat.data.cache

import androidx.room.Database
import androidx.room.RoomDatabase

/** Local offline cache. It is a disposable mirror of server state — never a source of truth — so a
 *  schema change can safely wipe and rebuild it (fallbackToDestructiveMigration in AppModule). */
@Database(
    entities = [CachedChat::class, CachedMessage::class, CachedUser::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatCacheDao(): ChatCacheDao
    abstract fun messageCacheDao(): MessageCacheDao
    abstract fun userCacheDao(): UserCacheDao
}
