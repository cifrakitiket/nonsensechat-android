package com.nonsense.chat

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.nonsense.chat.data.SettingsStore
import com.nonsense.chat.ui.navigation.AppNavGraph
import com.nonsense.chat.ui.theme.AppTheme
import com.nonsense.chat.ui.theme.NonsenseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settings: SettingsStore

    // Chat id carried by a notification tap; consumed once by the nav graph.
    private val deepLinkChat = MutableStateFlow<String?>(null)

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignore result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        deepLinkChat.value = intent?.getStringExtra(EXTRA_CHAT_ID)

        setContent {
            val theme by settings.theme.collectAsState(initial = AppTheme.DARK)
            NonsenseTheme(theme = theme) {
                val navController = rememberNavController()
                val pendingChat by deepLinkChat.collectAsState()
                AppNavGraph(
                    navController = navController,
                    deepLinkChatId = pendingChat,
                    onDeepLinkConsumed = { deepLinkChat.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_CHAT_ID)?.let { deepLinkChat.value = it }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_CHAT_ID = "chatId"
    }
}
