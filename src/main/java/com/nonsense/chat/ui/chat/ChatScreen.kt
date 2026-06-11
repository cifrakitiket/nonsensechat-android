package com.nonsense.chat.ui.chat

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.push.Notifications
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.readUri
import com.nonsense.chat.ui.theme.TgAccent
import com.nonsense.chat.ui.theme.TgAccent2
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
    var attachMenu by remember { mutableStateOf(false) }
    var showStickers by remember { mutableStateOf(false) }
    var showPoll by remember { mutableStateOf(false) }
    var viewerUrl by remember { mutableStateOf<String?>(null) }
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
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(0)
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            readUri(context, it)?.let { f -> viewModel.sendPhoto(f.bytes, f.name, null, false) }
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            readUri(context, it)?.let { f -> viewModel.sendFile(f.bytes, f.name, f.mime) }
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
                        Avatar(state.title, state.avatarUrl, size = 40.dp)
                        Box(Modifier.width(10.dp))
                        Column {
                            Text(state.title, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                            if (state.subtitle.isNotBlank()) {
                                Text(state.subtitle, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
                state.editing?.let { EditOrReplyBar("Editing message", it.text) { viewModel.cancelEdit() } }
                state.replyingTo?.let { EditOrReplyBar("Reply to ${it.author}", it.text) { viewModel.cancelReply() } }
                Composer(
                    value = input,
                    onValueChange = { input = it; viewModel.onComposerChanged(it) },
                    onSend = { viewModel.sendText(input); input = "" },
                    onAttachClick = { attachMenu = true },
                    onStickerClick = { showStickers = true },
                    attachMenu = attachMenu,
                    onAttachDismiss = { attachMenu = false },
                    onPickPhoto = {
                        attachMenu = false
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onPickFile = { attachMenu = false; filePicker.launch("*/*") },
                    onCreatePoll = { attachMenu = false; showPoll = true },
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

    if (showStickers) {
        StickerPicker(
            onPick = { viewModel.sendSticker(it.url, it.emoji, it.packId) },
            onDismiss = { showStickers = false },
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
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(body, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            }
            IconButton(onClick = onCancel) { Icon(Icons.Default.Close, "Cancel") }
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
    attachMenu: Boolean,
    onAttachDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onPickFile: () -> Unit,
    onCreatePoll: () -> Unit,
) {
    val accentBrush = Brush.linearGradient(listOf(TgAccent, TgAccent2))
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
            Box {
                IconButton(onClick = onAttachClick) {
                    Icon(Icons.Default.AttachFile, "Attach",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = attachMenu, onDismissRequest = onAttachDismiss) {
                    DropdownMenuItem(text = { Text("Photo") },
                        leadingIcon = { Icon(Icons.Default.Image, null) }, onClick = onPickPhoto)
                    DropdownMenuItem(text = { Text("File") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null) }, onClick = onPickFile)
                    DropdownMenuItem(text = { Text("Poll") },
                        leadingIcon = { Icon(Icons.Default.Poll, null) }, onClick = onCreatePoll)
                }
            }
            // Rounded input pill (darker than the bar), with the sticker button tucked inside.
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.weight(1f),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = { Text("Message") },
                        modifier = Modifier.weight(1f),
                        maxLines = 5,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                    )
                    IconButton(onClick = onStickerClick) {
                        Icon(Icons.Default.EmojiEmotions, "Stickers",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Box(Modifier.width(6.dp))
            // Circular gradient send button (web .send-btn).
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentBrush)
                    .clickable(enabled = value.isNotBlank(), onClick = onSend)
                    .alpha(if (value.isNotBlank()) 1f else 0.45f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(22.dp))
            }
        }
    }
}
