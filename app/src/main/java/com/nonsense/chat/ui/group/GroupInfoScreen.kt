package com.nonsense.chat.ui.group

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.StorageRepository
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.MessageRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.Chat
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.model.User
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar
import com.nonsense.chat.ui.common.readUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupInfoViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val account: AccountManager,
    private val chats: ChatRepository,
    private val users: UserRepository,
    private val messages: MessageRepository,
    private val storage: StorageRepository,
) : ViewModel() {
    val chatId: String = savedState.get<String>("chatId").orEmpty()
    val myUid get() = account.uid.orEmpty()

    private val memberUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    val left = MutableStateFlow(false)
    val deleted = MutableStateFlow(false)
    val photos = MutableStateFlow<List<String>>(emptyList())
    val searchResults = MutableStateFlow<List<User>>(emptyList())

    val state: StateFlow<Pair<Chat?, List<User>>> =
        combine(chats.observe(chatId), memberUsers) { chat, map ->
            chat?.let { ensure(it) }
            chat to (chat?.members?.mapNotNull { map[it] } ?: emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null to emptyList())

    init { loadPhotos() }

    private fun ensure(chat: Chat) {
        val missing = chat.members.filter { it !in memberUsers.value }
        if (missing.isEmpty()) return
        viewModelScope.launch { missing.forEach { uid -> users.get(uid)?.let { u -> memberUsers.update { it + (uid to u) } } } }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            val msgs = messages.observeMessages(chatId).first()
            photos.value = msgs.filter { it.type == MsgType.PHOTO && !it.deleted }.mapNotNull { it.photoUrl }.reversed().take(30)
        }
    }

    fun isAdmin(chat: Chat?) = chat?.admin == myUid
    fun isMod(chat: Chat?, uid: String) = chat?.mods?.contains(uid) == true
    fun roleOf(chat: Chat?, uid: String): String = when {
        chat?.admin == uid -> "Админ"
        chat?.mods?.contains(uid) == true -> "Модератор"
        else -> "Участник"
    }

    fun removeMember(uid: String) { viewModelScope.launch { chats.removeMember(chatId, uid) } }
    fun promote(uid: String) { viewModelScope.launch { chats.promoteMod(chatId, uid) } }
    fun demote(uid: String) { viewModelScope.launch { chats.demoteMod(chatId, uid) } }

    fun saveInfo(name: String, desc: String, privacy: String) {
        viewModelScope.launch {
            chats.updateGroup(chatId, mapOf("name" to name.trim(), "desc" to desc.trim(), "privacy" to privacy))
        }
    }

    fun setAvatar(bytes: ByteArray, fileName: String) {
        viewModelScope.launch {
            // Small avatar (≈256px, ~25KB) so it loads even on a throttled/blocked network.
            val url = storage.uploadAvatar(bytes, fileName)
            chats.updateGroup(chatId, mapOf("avatar" to url))
        }
    }

    fun search(q: String) {
        viewModelScope.launch {
            val existing = state.value.first?.members.orEmpty()
            searchResults.value = users.searchByNick(q).filter { it.id !in existing }
        }
    }

    fun addMember(uid: String) {
        viewModelScope.launch { chats.addMembers(chatId, listOf(uid)); searchResults.value = emptyList() }
    }

    fun leave() { viewModelScope.launch { chats.leave(chatId, myUid); left.value = true } }
    fun deleteGroup() { viewModelScope.launch { chats.deleteChat(chatId); deleted.value = true } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
    onBack: () -> Unit,
    onLeft: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: GroupInfoViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val left by viewModel.left.collectAsState()
    val deleted by viewModel.deleted.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val (chat, members) = state
    val admin = viewModel.isAdmin(chat)

    var editDialog by remember { mutableStateOf(false) }
    var addDialog by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(left, deleted) { if (left || deleted) onLeft() }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { readUri(context, it)?.let { f -> viewModel.setAvatar(f.bytes, f.name) } }
    }

    Scaffold(
        topBar = {
            TgTopAppBar(
                title = "О группе",
                onBack = onBack,
                actions = {
                    if (admin) IconButton(onClick = { editDialog = true }) { Icon(Icons.Default.Edit, "Изменить") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Avatar(chat?.name ?: "Группа", chat?.avatar?.ifBlank { null }, 88.dp)
                    if (admin) {
                        Box(
                            Modifier.align(Alignment.BottomEnd).size(30.dp).clip(CircleShape)
                                .clickable { avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.CameraAlt, "Аватар", tint = MaterialTheme.colorScheme.primary) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(chat?.name ?: "Группа", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (!chat?.desc.isNullOrBlank()) Text(chat!!.desc, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${members.size} участников · ${if (chat?.privacy == "public") "Публичная" else "Приватная"}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (photos.isNotEmpty()) {
                Text("Медиа", Modifier.padding(start = 16.dp, bottom = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(photos) { url ->
                        AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(RoundedCornerShape(8.dp)))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            HorizontalDivider()

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Участники", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (admin) TextButton(onClick = { addDialog = true }) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Добавить")
                }
            }

            LazyColumn(Modifier.weight(1f)) {
                items(members, key = { it.id }) { user ->
                    MemberRow(
                        user = user,
                        role = viewModel.roleOf(chat, user.id),
                        canManage = admin && user.id != viewModel.myUid && chat?.admin != user.id,
                        isMod = viewModel.isMod(chat, user.id),
                        onClick = { onOpenProfile(user.id) },
                        onPromote = { viewModel.promote(user.id) },
                        onDemote = { viewModel.demote(user.id) },
                        onRemove = { viewModel.removeMember(user.id) },
                    )
                }
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().clickable { viewModel.leave() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                Text("Покинуть группу", Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.error)
            }
            if (admin) {
                Row(Modifier.fillMaxWidth().clickable { deleteConfirm = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Text("Удалить группу", Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (editDialog && chat != null) {
        EditGroupDialog(
            initialName = chat.name, initialDesc = chat.desc, initialPrivacy = chat.privacy,
            onSave = { n, d, p -> viewModel.saveInfo(n, d, p); editDialog = false },
            onDismiss = { editDialog = false },
        )
    }
    if (addDialog) {
        AddMemberDialog(
            results = viewModel.searchResults.collectAsState().value,
            onSearch = viewModel::search,
            onAdd = { viewModel.addMember(it) },
            onDismiss = { addDialog = false },
        )
    }
    if (deleteConfirm) {
        AlertDialog(
            onDismissRequest = { deleteConfirm = false },
            confirmButton = { TextButton(onClick = { deleteConfirm = false; viewModel.deleteGroup() }) { Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteConfirm = false }) { Text("Отмена") } },
            title = { Text("Удалить группу?") },
            text = { Text("Группа и её сообщения будут удалены безвозвратно.") },
        )
    }
}

@Composable
private fun MemberRow(
    user: User,
    role: String,
    canManage: Boolean,
    isMod: Boolean,
    onClick: () -> Unit,
    onPromote: () -> Unit,
    onDemote: () -> Unit,
    onRemove: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(user.displayName, user.avatar, 44.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(user.displayName, style = MaterialTheme.typography.titleMedium)
            Text(role, style = MaterialTheme.typography.labelSmall,
                color = if (role == "Участник") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
        }
        if (canManage) {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.Shield, "Управление") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    if (isMod) DropdownMenuItem(text = { Text("Снять модератора") }, onClick = { menu = false; onDemote() })
                    else DropdownMenuItem(text = { Text("Сделать модератором") }, onClick = { menu = false; onPromote() })
                    DropdownMenuItem(text = { Text("Удалить из группы", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onRemove() })
                }
            }
        }
    }
}

@Composable
private fun EditGroupDialog(
    initialName: String,
    initialDesc: String,
    initialPrivacy: String,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDesc) }
    var privacy by remember { mutableStateOf(initialPrivacy) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onSave(name, desc, privacy) }, enabled = name.isNotBlank()) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Изменить группу") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = privacy == "private", onClick = { privacy = "private" }, label = { Text("Приватная") })
                    FilterChip(selected = privacy == "public", onClick = { privacy = "public" }, label = { Text("Публичная") })
                }
            }
        },
    )
}

@Composable
private fun AddMemberDialog(
    results: List<User>,
    onSearch: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Готово") } },
        title = { Text("Добавить участника") },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; onSearch(it) },
                    label = { Text("Поиск по нику") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.fillMaxWidth().height(260.dp)) {
                    items(results, key = { it.id }) { u ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onAdd(u.id) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(u.displayName, u.avatar, 40.dp)
                            Text(u.displayName, Modifier.weight(1f).padding(start = 12.dp))
                            Icon(Icons.Default.Add, "Добавить", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
    )
}
