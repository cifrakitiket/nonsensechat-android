package com.nonsense.chat.push

import com.google.firebase.messaging.FirebaseMessaging
import com.nonsense.chat.data.DocOps
import com.nonsense.chat.data.DocRepository
import com.nonsense.chat.data.Tables
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Stores this device's FCM token in users.doc.fcmTokens so the server can target it. */
@Singleton
class PushTokenManager @Inject constructor(
    private val docs: DocRepository,
) {
    /** Fetch the current token and attach it to the user's doc. Call after sign-in. */
    suspend fun register(uid: String) {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
        saveToken(uid, token)
    }

    suspend fun saveToken(uid: String, token: String) {
        docs.apply(Tables.USERS, uid, DocOps().arrayUnion(listOf("fcmTokens"), token).build())
    }

    /** Remove this device's token on sign-out so it stops receiving pushes. */
    suspend fun unregister(uid: String) {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
        docs.apply(Tables.USERS, uid, DocOps().arrayRemove(listOf("fcmTokens"), token).build())
    }
}
