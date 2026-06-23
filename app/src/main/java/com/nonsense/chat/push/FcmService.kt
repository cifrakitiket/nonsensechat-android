package com.nonsense.chat.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nonsense.chat.data.SettingsStore
import com.nonsense.chat.data.repos.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Receives FCM data messages from the push-on-message Edge Function and posts a notification.
 * The server sends a data-only payload {chatId, title, body} so display is controlled here
 * (works whether the app is foreground, background, or killed).
 */
@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var pushTokens: PushTokenManager
    @Inject lateinit var auth: AuthRepository
    @Inject lateinit var settings: SettingsStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch {
            auth.currentUid()?.let { pushTokens.saveToken(it, token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val chatId = data["chatId"] ?: return
        val title = data["title"] ?: message.notification?.title ?: "New message"
        val body = data["body"] ?: message.notification?.body ?: ""
        // Gate on offline-readable DataStore settings even when FCM starts this service cold:
        // global notification toggle, per-chat mute mirror, and hidden previews.
        runBlocking(Dispatchers.IO) {
            if (!settings.notificationsEnabled.first()) return@runBlocking
            if (chatId in settings.mutedChatIds.first()) return@runBlocking
            val shownBody = if (settings.notifPreview.first()) body else "Новое сообщение"
            Notifications.showMessage(applicationContext, chatId, title, shownBody)
        }
    }
}
