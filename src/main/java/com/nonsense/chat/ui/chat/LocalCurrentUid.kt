package com.nonsense.chat.ui.chat

import androidx.compose.runtime.compositionLocalOf

/** Current user's uid, provided by ChatScreen so leaf composables (e.g. PollView) can read it. */
val LocalCurrentUid = compositionLocalOf { "" }
