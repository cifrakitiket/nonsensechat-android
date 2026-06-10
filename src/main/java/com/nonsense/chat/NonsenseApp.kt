package com.nonsense.chat

import android.app.Application
import com.nonsense.chat.push.Notifications
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NonsenseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Register notification channels up-front so FCM (which may arrive in the background)
        // always has a valid channel to post into.
        Notifications.ensureChannels(this)
    }
}
