package com.nonsense.chat.ui.chat

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.push.Notifications
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.brandGradient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenGroupInfo: (String) -> Unit,
    onOpenCall: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val forwardTargets by viewModel.forwardTargets.collectAsState()
    val callActive by viewModel.callActive.collectAsState()
    val context = LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    var input by remember { mutableStateOf("") }
    var showAttach by remember { mutableStateOf(false) }
    var showStickers by remember { mutableStateOf(false) }
    var showPoll by remember { mutableStateOf(false) }
    var viewerUrl by remember { mutableStateOf<String?>(null) }
    var openPackId by remember { mutableStateOf<String?>(null) }
    var forwardMsg by remember { mutableStateOf<com.nonsense.chat.model.Message?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Suppress notifications for the open chat.
    DisposableEffect(viewModel.chatId) {
        Notifications.activeChatId = viewModel.chatId
        onDispose { Notifications.activeChatId = null }
    }
    LaunchedEffect(state.messages.size) {
        viewModel.markRead(state.messages.map { it.message })
    }
    // Auto-scroll to the bottom only when a NEW message arrives (newest id changes) or on first load —
    // NOT when older pages are prepended on scroll-up (which would yank the user back down).
    val newestId = state.messages.lastOrNull()?.message?.id
    LaunchedEffect(newestId) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(0)
    }
    // Lazy "load older": when the oldest loaded item nears the top of the (reversed) list, grow window.
    LaunchedEffect(listState, state.canLoadOlder, state.messages.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastIdx ->
                if (state.canLoadOlder && lastIdx >= state.messages.size - 3) viewModel.loadOlder()
            }
    }

    // Calls need RECORD_AUDIO (+ CAMERA for video); run the chosen action once granted.
    var pendingCall by remember { mutableStateOf<(() -> Unit)?>(null) }
    val callPerms = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[android.Manifest.permission.RECORD_AUDIO] == true) pendingCall?.invoke()
        pendingCall = null
    }
    fun requestCall(action: () -> Unit) {
        pendingCall = action
        callPerms.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                title = {
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (state.isGroup) onOpenGroupInfo(viewModel.chatId)
                            else state.peerUid?.let(onOpenProfile)
                        },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(state.title, state.avatarUrl, size = 40.dp,
                            ringColor = MaterialTheme.colorScheme.primary)
                        Box(Modifier.width(10.dp))
                        Column {
                            Text(state.title, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                            if (state.subtitle.isNotBlank()) {
                                // Highlight live status (online / typing) in the accent, like Telegram.
                                val live = state.subtitle == "в сети" || state.subtitle.contains("печатает")
                                Text(state.subtitle, style = MaterialTheme.typography.labelSmall,
                                    color = if (live) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                actions = {
                    IconButton(onClick = { requestCall { viewModel.startCall(false); onOpenCall(viewModel.chatId) } }) {
                        Icon(Icons.Default.Call, "Звонок")
                    }
                    IconButton(onClick = { requestCall { viewModel.startCall(true); onOpenCall(viewModel.chatId) } }) {
                        Icon(Icons.Default.Videocam, "Видеозвонок")
                    }
                },
            )
        },
        bottomBar = {
            Column {
                state.editing?.let { EditOrReplyBar("Редактирование", it.text) { viewModel.cancelEdit() } }
                state.replyingTo?.let { EditOrReplyBar("Ответ · ${it.author}", it.text) { viewModel.cancelReply() } }
                Composer(
                    value = input,
                    onValueChange = { input = it; viewModel.onComposerChanged(it) },
                    onSend = { viewModel.sendText(input); input = "" },
                    onAttachClick = { showAttach = true },
                    onStickerClick = { showStickers = true },
                )
            }
        },
    ) { padding ->
        CompositionLocalProvider(LocalCurrentUid provides viewModel.myUid) {
            val wallpaper = com.nonsense.chat.ui.theme.chatWallpaperBrush()
            Column(Modifier.fillMaxSize().background(wallpaper).padding(padding)) {
                if (callActive) {
                    Surface(color = MaterialTheme.colorScheme.primary) {
                        Row(
                            Modifier.fillMaxWidth().clickable { requestCall { viewModel.joinCall(false); onOpenCall(viewModel.chatId) } }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Call, null, tint = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text("Идёт звонок — нажмите, чтобы присоединиться", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                state.pinned.lastOrNull()?.let { pin ->
                    PinnedBar(
                        count = state.pinned.size,
                        author = pin.author,
                        text = pin.text,
                        onUnpin = { viewModel.unpinMessage(pin.id) },
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // reverseLayout = true → render newest-first.
                        items(
                            items = state.messages.asReversed(),
                            key = { it.message.id },
                        ) { ui ->
                            Column {
                                ui.dayHeader?.let { DayHeader(it) }
                                MessageBubble(
                                    ui = ui,
                                    isGroup = state.isGroup,
                                    readByOther = ui.isMine && ui.message.readByOther(viewModel.myUid),
                                    isPinned = state.pinned.any { it.id == ui.message.id },
                                    onReply = { viewModel.startReply(ui.message) },
                                    onEdit = { viewModel.startEdit(ui.message); input = ui.message.text },
                                    onDelete = { viewModel.deleteMessage(ui.message) },
                                    onForward = { viewModel.loadForwardTargets(); forwardMsg = ui.message },
                                    onCopy = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(ui.message.text)) },
                                    onPin = { viewModel.pinMessage(ui.message) },
                                    onUnpin = { viewModel.unpinMessage(ui.message.id) },
                                    onReact = { viewModel.toggleReaction(ui.message, it) },
                                    onImageClick = { viewerUrl = it },
                                    onOpenUrl = { url ->
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                                        }
                                    },
                                    onVote = { viewModel.votePoll(ui.message, it) },
                                    onAuthorClick = onOpenProfile,
                                    onOpenPack = { packId ->
                                        // Built-in emoji stickers have no real pack — ignore those.
                                        if (packId.isNotBlank() && packId != BUILTIN_STICKER_PACK_ID) openPackId = packId
                                    },
                                )
                            }
                        }
                    }
                    // Scroll-to-bottom button (visible when scrolled up).
                    val showScrollDown by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }
                    if (showScrollDown) {
                        SmallFloatingActionButton(
                            onClick = { scope.launch { listState.animateScrollToItem(0) } },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) { Icon(Icons.Default.KeyboardArrowDown, "Вниз") }
                    }
                }
            }
        }
    }

    if (showAttach) {
        AttachSheet(
            onDismiss = { showAttach = false },
            onSendPhoto = { f -> viewModel.sendPhoto(f.bytes, f.name, null, false) },
            onSendVideo = { f -> viewModel.sendVideo(f.bytes, f.name, f.mime) },
            onSendAudio = { f -> viewModel.sendAudio(f.bytes, f.name, f.mime) },
            onSendFile = { f -> viewModel.sendAttachment(f.bytes, f.name, f.mime) },
            onCreatePoll = { showPoll = true },
        )
    }
    if (showStickers) {
        EmojiStickerSheet(
            onPickEmoji = { input += it },
            onPickSticker = { viewModel.sendSticker(it.url, it.emoji, it.packId); showStickers = false },
            onDismiss = { showStickers = false },
        )
    }
    openPackId?.let { packId ->
        StickerPackSheet(
            packId = packId,
            onDismiss = { openPackId = null },
            onSend = { s -> viewModel.sendSticker(s.url, s.emoji, s.packId); openPackId = null },
        )
    }
    if (showPoll) {
        PollDialog(onDismiss = { showPoll = false }, onCreate = { poll, q ->
            viewModel.sendPoll(poll, q)
        })
    }
    viewerUrl?.let { ImageViewer(url = it, onDismiss = { viewerUrl = null }) }
    forwardMsg?.let { msg ->
        ForwardDialog(
            targets = forwardTargets,
            onPick = { target -> viewModel.forwardMessage(msg, target.chatId); forwardMsg = null },
            onDismiss = { forwardMsg = null },
        )
    }
}

@Composable
private fun PinnedBar(count: Int, author: String, text: String, onUnpin: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(width = 3.dp, height = 32.dp).background(MaterialTheme.colorScheme.primary))
            Box(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (count > 1) "Закреплённое · $count" else "Закреплённое сообщение",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                )
                Text("$author: $text", style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
            IconButton(onClick = onUnpin) { Icon(Icons.Default.Close, "Открепить") }
        }
    }
}

@Composable
private fun ForwardDialog(
    targets: List<ForwardTarget>,
    onPick: (ForwardTarget) -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Переслать в…") },
        text = {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(targets, key = { it.chatId }) { t ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onPick(t) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(t.title, t.avatar, size = 40.dp)
                        Box(Modifier.width(12.dp))
                        Text(t.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    }
                }
            }
        },
    )
}

@Composable
private fun DayHeader(label: String) {
    // Telegram-style floating, pill-shaped, semi-transparent date chip centred over the wallpaper.
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
            shape = RoundedCornerShape(50),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun EditOrReplyBar(title: String, body: String, onCancel: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            // Accent stripe mirrors the web `.reply-bar` left border.
            Box(Modifier.size(width = 3.dp, height = 32.dp).clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary))
            Box(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancel) { Icon(Icons.Default.Close, "Отмена") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachClick: () -> Unit,
    onStickerClick: () -> Unit,
) {
    val accentBrush = brandGradient()
    val glow = MaterialTheme.colorScheme.primary
    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            // Sit above the phone's nav buttons (and rise with the keyboard): pad by whichever
            // of the navigation-bar / IME insets is larger at the bottom.
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(Icons.Default.AttachFile, "Вложение",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Rounded input pill (web `#minp`: --card fill + --border), sticker button tucked inside.
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = { Text("Сообщение") },
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    )
                    IconButton(onClick = onStickerClick) {
                        Icon(Icons.Default.EmojiEmotions, "Стикеры",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box(Modifier.width(6.dp))
            // Circular gradient send button with an accent glow (web .send-btn drop shadow).
            Box(
                Modifier
                    .size(46.dp)
                    .alpha(if (value.isNotBlank()) 1f else 0.45f)
                    .shadow(8.dp, CircleShape, spotColor = glow, ambientColor = glow)
                    .clip(CircleShape)
                    .background(accentBrush)
                    .clickable(enabled = value.isNotBlank(), onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Отправить", tint = Color.White,
                    modifier = Modifier.size(22.dp))
            }
        }
    }
}
