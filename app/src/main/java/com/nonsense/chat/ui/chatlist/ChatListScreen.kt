package com.nonsense.chat.ui.chatlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.data.ConnectionMonitor
import com.nonsense.chat.model.Folder
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.brandGradient
import com.nonsense.chat.ui.theme.OnlineGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onNewGroup: () -> Unit,
    onOpenFriends: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val accepted by viewModel.acceptedChat.collectAsState()
    val connection by viewModel.connection.collectAsState()
    var searching by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }
    var fabOpen by remember { mutableStateOf(false) }
    var foldersDialog by remember { mutableStateOf(false) }
    var folderTarget by remember { mutableStateOf<ChatRow?>(null) }

    LaunchedEffect(accepted) {
        accepted?.let { onOpenChat(it); viewModel.consumeAccepted() }
    }

    fun closeSearch() { searching = false; queryText = ""; viewModel.setQuery("") }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                title = {
                    if (searching) {
                        SearchPill(
                            value = queryText,
                            onValueChange = { queryText = it; viewModel.setQuery(it) },
                        )
                    } else {
                        ConnectionTitle(
                            base = if (state.showArchived) "Архив" else "Сообщения",
                            connection = connection,
                        )
                    }
                },
                navigationIcon = {
                    if (searching) {
                        IconButton(onClick = { closeSearch() }) { Icon(Icons.Default.Close, "Закрыть поиск") }
                    } else {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.Menu, "Меню") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            state.me?.let { me ->
                                DropdownMenuItem(
                                    text = { Text(me.displayName) },
                                    leadingIcon = { Avatar(me.displayName, me.avatar, size = 28.dp) },
                                    onClick = { menuOpen = false; onOpenProfile(me.id) },
                                )
                            }
                            DropdownMenuItem(text = { Text("Друзья") }, leadingIcon = { Icon(Icons.Default.PersonAdd, null) }, onClick = { menuOpen = false; onOpenFriends() })
                            DropdownMenuItem(text = { Text("Папки") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menuOpen = false; foldersDialog = true })
                            DropdownMenuItem(
                                text = { Text(if (state.showArchived) "Все чаты" else "Архив (${state.archivedCount})") },
                                leadingIcon = { Icon(Icons.Default.Archive, null) },
                                onClick = { menuOpen = false; viewModel.toggleArchivedView() },
                            )
                            DropdownMenuItem(text = { Text("Настройки") }, onClick = { menuOpen = false; onOpenSettings() })
                        }
                    }
                },
                actions = {
                    if (!searching) {
                        IconButton(onClick = { searching = true }) { Icon(Icons.Default.Search, "Поиск") }
                    }
                },
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { fabOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) { Icon(Icons.Default.Create, "Создать") }
                DropdownMenu(expanded = fabOpen, onDismissRequest = { fabOpen = false }) {
                    DropdownMenuItem(text = { Text("Новый чат") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { fabOpen = false; onNewChat() })
                    DropdownMenuItem(text = { Text("Новая группа") }, leadingIcon = { Icon(Icons.Default.Group, null) }, onClick = { fabOpen = false; onNewGroup() })
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (!state.showArchived && !searching) {
                FilterTabs(state, viewModel)
            }

            LazyColumn(Modifier.fillMaxSize()) {
                if (state.filter == ChatFilter.REQUESTS) {
                    if (state.requests.isEmpty()) {
                        item { EmptyHint("Нет входящих заявок") }
                    }
                    items(state.requests, key = { it.id }) { req ->
                        RequestRowItem(req, onAccept = { viewModel.acceptRequest(req) }, onDecline = { viewModel.declineRequest(req) }, onOpenProfile = onOpenProfile)
                    }
                } else {
                    if (state.rows.isEmpty()) {
                        // Don't claim "no chats" while the first load is still retrying on this flaky
                        // network — show a spinner until the connection is actually healthy.
                        if (connection == ConnectionMonitor.State.CONNECTED && !state.loading) {
                            item { EmptyHint(if (state.showArchived) "Архив пуст" else "Чатов пока нет") }
                        } else {
                            item { LoadingHint() }
                        }
                    }
                    items(state.rows, key = { it.chatId }) { row ->
                        ChatRowItem(
                            row = row,
                            archivedView = state.showArchived,
                            onClick = { onOpenChat(row.chatId) },
                            onPin = { viewModel.togglePin(row) },
                            onMute = { viewModel.toggleMute(row) },
                            onArchive = { viewModel.archive(row) },
                            onLeave = { viewModel.leave(row) },
                            onFolder = { folderTarget = row },
                        )
                    }
                }
            }
        }
    }

    if (foldersDialog) {
        FoldersDialog(
            folders = state.folders,
            onCreate = { name, icon -> viewModel.createFolder(name, icon) },
            onDelete = { viewModel.deleteFolder(it) },
            onDismiss = { foldersDialog = false },
        )
    }
    folderTarget?.let { row ->
        AddToFolderDialog(
            folders = state.folders,
            chatId = row.chatId,
            onToggle = { folderId, present -> viewModel.addToFolder(folderId, row.chatId, present) },
            onDismiss = { folderTarget = null },
        )
    }
}

/** Telegram-style borderless rounded search field that lives in the top bar. */
@OptIn(ExperimentalMaterial3Api::class)
/** Telegram-style top-bar title: shows a spinner + "Соединение…/Обновление…" while REST is
 *  retrying on this flaky network, and the normal title once connected. */
@Composable
private fun ConnectionTitle(base: String, connection: ConnectionMonitor.State) {
    if (connection == ConnectionMonitor.State.CONNECTED) {
        Text(base, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    } else {
        val label = if (connection == ConnectionMonitor.State.CONNECTING) "Соединение…" else "Обновление…"
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchPill(value: String, onValueChange: (String) -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp)) {
            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Поиск") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }
    }
}

/**
 * Telegram folder tabs (web `.tabs`/`.tab`): equal-width gradient pills — the active tab is filled
 * with the brand accent gradient and white text, inactive tabs are transparent with dim text.
 * Custom folders, if any, scroll as pills below.
 */
@Composable
private fun FilterTabs(state: ChatListUiState, viewModel: ChatListViewModel) {
    val noFolder = state.selectedFolderId == null
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PillTab(Modifier.weight(1f), "Все", noFolder && state.filter == ChatFilter.ALL) { viewModel.setFilter(ChatFilter.ALL) }
            PillTab(Modifier.weight(1f), "Личные", noFolder && state.filter == ChatFilter.DMS) { viewModel.setFilter(ChatFilter.DMS) }
            PillTab(Modifier.weight(1f), "Группы", noFolder && state.filter == ChatFilter.GROUPS) { viewModel.setFilter(ChatFilter.GROUPS) }
            PillTab(Modifier.weight(1f), "Заявки", noFolder && state.filter == ChatFilter.REQUESTS, count = state.requestCount) { viewModel.setFilter(ChatFilter.REQUESTS) }
        }
        if (state.folders.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.folders.forEach { f ->
                    PillTab(Modifier, "${f.icon} ${f.name}", state.selectedFolderId == f.id) { viewModel.selectFolder(f.id) }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)))
    }
}

@Composable
private fun PillTab(modifier: Modifier, label: String, selected: Boolean, count: Int = 0, onClick: () -> Unit) {
    val gradient = brandGradient()
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .then(if (selected) Modifier.background(gradient) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(
                label,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (count > 0) {
                Spacer(Modifier.width(6.dp))
                CountBubble(count, selected)
            }
        }
    }
}

@Composable
private fun CountBubble(count: Int, selected: Boolean) {
    // On an active (gradient) pill the badge is white with accent text; otherwise accent-filled.
    val bg = if (selected) Color.White else MaterialTheme.colorScheme.primary
    val fg = if (selected) MaterialTheme.colorScheme.primary else Color.White
    Box(
        Modifier.heightIn(min = 18.dp).widthIn(min = 18.dp).clip(RoundedCornerShape(50)).background(bg)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("$count", color = fg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatRowItem(
    row: ChatRow,
    archivedView: Boolean,
    onClick: () -> Unit,
    onPin: () -> Unit,
    onMute: () -> Unit,
    onArchive: () -> Unit,
    onLeave: () -> Unit,
    onFolder: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    // Web `.chat-item` on mobile: rounded card with side margins, no dividers, rounded ripple.
    Box(Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(onClick = onClick, onLongClick = { menuOpen = true })
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Avatar(name = row.title, url = row.avatar, size = 48.dp)
                if (row.online) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen),
                    )
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (row.pinned) {
                        Icon(Icons.Default.PushPin, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        row.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (row.unread) FontWeight.Bold else FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (row.muted) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.VolumeOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    (if (row.lastMsgMine) "Вы: " else "") + row.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    row.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (row.unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                if (row.unread) {
                    // Muted chats get a flat grey badge; active ones the brand accent gradient.
                    val badgeBg =
                        if (row.muted) Modifier.background(MaterialTheme.colorScheme.onSurfaceVariant)
                        else Modifier.background(brandGradient())
                    Box(
                        Modifier.heightIn(min = 20.dp).widthIn(min = 20.dp).clip(RoundedCornerShape(50))
                            .then(badgeBg).padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (row.unreadCount > 0) "${row.unreadCount}" else "",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                } else {
                    Box(Modifier.size(20.dp))
                }
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(text = { Text(if (row.pinned) "Открепить" else "Закрепить") }, onClick = { menuOpen = false; onPin() })
            DropdownMenuItem(text = { Text(if (row.muted) "Включить уведомления" else "Без звука") }, onClick = { menuOpen = false; onMute() })
            DropdownMenuItem(text = { Text("В папку") }, onClick = { menuOpen = false; onFolder() })
            DropdownMenuItem(text = { Text(if (archivedView) "Из архива" else "В архив") }, onClick = { menuOpen = false; onArchive() })
            DropdownMenuItem(text = { Text("Покинуть чат") }, onClick = { menuOpen = false; onLeave() })
        }
    }
}

@Composable
private fun RequestRowItem(
    req: RequestRow,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenProfile: (String) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable { onOpenProfile(req.fromUid) }.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = req.fromNick, url = null, size = 48.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(req.fromNick, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Хочет добавить вас", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onAccept) { Icon(Icons.Default.Done, "Принять", tint = OnlineGreen) }
        IconButton(onClick = onDecline) { Icon(Icons.Default.Create, "Отклонить", tint = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Centered spinner shown in the list body while the first chat load is still connecting. */
@Composable
private fun LoadingHint() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
    }
}

@Composable
private fun FoldersDialog(
    folders: List<Folder>,
    onCreate: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("📁") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
        title = { Text("Папки") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = icon, onValueChange = { icon = it.take(2) }, modifier = Modifier.width(64.dp), singleLine = true, label = { Text("Знак") })
                    OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Название") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onCreate(name, icon); name = "" }, enabled = name.isNotBlank()) { Text("Создать папку") }
                Spacer(Modifier.height(12.dp))
                folders.forEach { f ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${f.icon} ${f.name}", Modifier.weight(1f))
                        TextButton(onClick = { onDelete(f.id) }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        },
    )
}

@Composable
private fun AddToFolderDialog(
    folders: List<Folder>,
    chatId: String,
    onToggle: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
        title = { Text("Добавить в папку") },
        text = {
            Column {
                if (folders.isEmpty()) Text("Сначала создайте папку в меню «Папки».")
                folders.forEach { f ->
                    val present = chatId in f.chats
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onToggle(f.id, !present) }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${f.icon} ${f.name}", Modifier.weight(1f))
                        if (present) Icon(Icons.Default.Done, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
    )
}
