package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.data.cache.DocCache
import com.nonsense.chat.data.jsonStr
import com.nonsense.chat.model.User
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val client: SupabaseClient,
    private val docs: DocRepository,
    private val realtime: RealtimeBus,
    private val cache: DocCache,
) {
    private fun decode(row: DocRow): User = decodeDoc<User>(row).copy(id = row.id)

    /** Fetch a user profile, retrying a few times so a single transient REST timeout doesn't
     *  permanently leave a name/avatar blank. */
    suspend fun get(uid: String): User? {
        // DocRepository.byId already retries + soft-fails (never throws). Add one light outer retry
        // for the transient case where it soft-failed to null, then give up gracefully.
        repeat(2) { attempt ->
            val row = runCatching { docs.byId(Tables.USERS, uid) }.getOrNull()
            if (row != null) {
                cache.putUsers(listOf(row)) // write-through so names/avatars survive offline
                val result = decode(row)
                android.util.Log.i("NSDIAG", "user.get($uid) -> ${result.nick}")
                return result
            }
            kotlinx.coroutines.delay(800L * (attempt + 1))
        }
        // Network gave up — fall back to the cached profile so the name/avatar still render.
        cache.user(uid)?.let {
            android.util.Log.i("NSDIAG", "user.get($uid) -> from cache")
            return decode(it)
        }
        android.util.Log.i("NSDIAG", "user.get($uid) -> NULL (gave up)")
        return null
    }

    /** Batch-fetch many profiles in a SINGLE request instead of N× [get]. Returns a uid→User map
     *  (missing/unresolved uids are simply absent). Used to load all DM peers / group members at
     *  once so the chat list and chat header don't fire one round trip per person. */
    suspend fun getMany(uids: Collection<String>): Map<String, User> {
        val ids = uids.filter { it.isNotBlank() }.distinct()
        if (ids.isEmpty()) return emptyMap()
        val rows = docs.whereIn(Tables.USERS, "id", ids)
        cache.putUsers(rows) // write-through
        val result = rows.associate { it.id to decode(it) }.toMutableMap()
        // Anyone the server didn't return (offline / timeout — whereIn soft-fails to []) is filled
        // from cache so the chat list still shows known names/avatars.
        val missing = ids.filter { it !in result }
        if (missing.isNotEmpty()) cache.users(missing).forEach { result[it.id] = decode(it) }
        return result
    }

    fun observe(uid: String): Flow<User?> = channelFlow {
        send(get(uid))
        realtime.changes(Tables.USERS).collect { change ->
            if (change.id != uid) return@collect
            when (change) {
                is RowChange.Upsert -> {
                    val row = change.toRow()
                    cache.putUsers(listOf(row))
                    send(decode(row))
                }
                is RowChange.Removed -> send(null)
            }
        }
    }

    suspend fun searchByNick(query: String, limit: Long = 30): List<User> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return client.postgrest.from(Tables.USERS)
            .select(Columns.list("id", "doc")) {
                filter { ilike("nick_lower", "%$q%") }
                limit(limit)
            }
            .decodeList<DocRow>()
            .map(::decode)
    }

    suspend fun updateProfile(uid: String, fields: Map<String, String>) {
        val ops = DocOps()
        fields.forEach { (key, value) ->
            ops.set(listOf(key), jsonStr(value))
            if (key == "nick") ops.set(listOf("nickLower"), jsonStr(value.lowercase()))
        }
        if (!ops.isEmpty()) docs.apply(Tables.USERS, uid, ops.build())
    }

    suspend fun setHideLastSeen(uid: String, hide: Boolean) {
        docs.apply(Tables.USERS, uid, DocOps().set("hideLastSeen", value = hide).build())
    }

    /** Creator-only: toggle a user's verified badge. */
    suspend fun setVerified(uid: String, verified: Boolean) {
        docs.apply(Tables.USERS, uid, DocOps().set("verified", value = verified).build())
    }

    companion object {
        /** Account allowed to award verified badges / shown the dev badge (web CREATOR_UID). */
        const val CREATOR_UID = ""
    }
}
