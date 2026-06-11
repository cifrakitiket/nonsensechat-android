package com.nonsense.chat

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.nonsense.chat.push.Notifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NonsenseApp : Application(), ImageLoaderFactory {

    /** Hardened, disk-cached Coil loader (see AppModule.provideImageLoader). Coil calls
     *  [newImageLoader] lazily on first image load, by which point Hilt has injected this. */
    @Inject lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()
        // Register notification channels up-front so FCM (which may arrive in the background)
        // always has a valid channel to post into.
        Notifications.ensureChannels(this)
    }

    override fun newImageLoader(): ImageLoader = imageLoader
}
