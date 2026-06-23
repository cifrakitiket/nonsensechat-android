package com.nonsense.chat.data

import com.nonsense.chat.di.AppScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/** A realtime row event already reduced to id + doc (or just id for deletes). */
sealed interface RowChange {
    val id: String
    data class Upsert(override val id: String, val doc: JsonObject) : RowChange
    data class Removed(override val id: String) : RowChange
}

/**
 * Subscribes to Postgres change events per table (mirrors the web shim's one-channel-per-table
 * design).  Tables are published with `replica identity full`, so deletes/updates carry the row.
 */
@Singleton
class RealtimeBus @Inject constructor(
    private val client: SupabaseClient,
    @AppScope private val scope: CoroutineScope,
) {
    private val seq = AtomicLong(0)

    // One hot, shared subscription per table. Previously every collector (chat list + each open
    // chat + the two user-presence watchers …) opened its OWN channel, so the same table was often
    // subscribed 2–3× at once — each a fresh subscribe round-trip. shareIn collapses them: the first
    // collector opens the channel, later ones attach instantly, and it closes 5 s after the last
    // collector leaves (so screen transitions don't churn the socket). Every consumer already
    // filters the change stream by id, so a shared firehose is exactly what they expect.
    private val shared = ConcurrentHashMap<String, Flow<RowChange>>()

    fun changes(table: String): Flow<RowChange> = shared.getOrPut(table) {
        rawChanges(table).shareIn(scope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), replay = 0)
    }

    private fun rawChanges(table: String): Flow<RowChange> = channelFlow {
        val channel = client.channel("rt_${table}_${seq.incrementAndGet()}")
        val actions = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = table
        }
        try {
            channel.subscribe()
            actions.collect { action ->
                action.toRowChange()?.let { send(it) }
            }
        } finally {
            withContext(NonCancellable) {
                runCatching { channel.unsubscribe() }
                runCatching { client.realtime.removeChannel(channel) }
            }
        }
    }.mapNotNull { it }

    private fun PostgresAction.toRowChange(): RowChange? = when (this) {
        is PostgresAction.Insert -> record.toUpsert()
        is PostgresAction.Update -> record.toUpsert()
        is PostgresAction.Delete -> oldRecord.idOrNull()?.let { RowChange.Removed(it) }
        else -> null
    }

    private fun JsonObject.toUpsert(): RowChange? {
        val id = idOrNull() ?: return null
        val doc = (this["doc"] as? JsonObject) ?: JsonObject(emptyMap())
        return RowChange.Upsert(id, doc)
    }

    private fun JsonObject.idOrNull(): String? =
        (this["id"] as? kotlinx.serialization.json.JsonPrimitive)?.content
}
