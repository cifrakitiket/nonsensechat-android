package com.nonsense.chat.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nonsense.chat.data.repos.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        Notifications.showMessage(applicationContext, chatId, title, body)
    }
}
