package com.nonsense.chat.data.repos

import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.DocRow
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
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
) {
    private fun decode(row: DocRow): User = decodeDoc<User>(row).copy(id = row.id)

    /** Fetch a user profile, retrying a few times so a single transient REST timeout doesn't
     *  permanently leave a name/avatar blank. */
    suspend fun get(uid: String): User? {
        // DocRepository.byId already retries + soft-fails (never throws). Add one light outer retry
        // for the transient case where it soft-failed to null, then give up gracefully.
        repeat(2) { attempt ->
            val result = runCatching { docs.byId(Tables.USERS, uid)?.let(::decode) }.getOrNull()
            if (result != null) {
                android.util.Log.i("NSDIAG", "user.get($uid) -> ${result.nick}")
                return result
            }
            kotlinx.coroutines.delay(800L * (attempt + 1))
        }
        android.util.Log.i("NSDIAG", "user.get($uid) -> NULL (gave up)")
        return null
    }

    fun observe(uid: String): Flow<User?> = channelFlow {
        send(get(uid))
        realtime.changes(Tables.USERS).collect { change ->
            if (change.id != uid) return@collect
            when (change) {
                is RowChange.Upsert -> send(decode(change.toRow()))
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
