package com.nonsense.chat.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nonsense.chat.MainActivity
import com.nonsense.chat.R

/** Notification channel setup + posting per-chat message notifications that deep-link to the chat. */
object Notifications {
    const val CHANNEL_MESSAGES = "messages"

    /** Currently-open chat id; messages for it are not shown as notifications. */
    @Volatile
    var activeChatId: String? = null

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_MESSAGES,
            "Messages",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "New chat messages"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 120, 60, 120)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun showMessage(context: Context, chatId: String, title: String, body: String) {
        if (chatId == activeChatId) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_CHAT_ID, chatId)
        }
        val pending = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(chatId.hashCode(), notification)
        }
    }
}
