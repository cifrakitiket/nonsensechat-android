package com.nonsense.chat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.ConnectionMonitor
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.Tables
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.FolderRepository
import com.nonsense.chat.data.repos.FriendRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.Chat
import com.nonsense.chat.model.ChatType
import com.nonsense.chat.model.Folder
import com.nonsense.chat.model.FriendRequest
import com.nonsense.chat.model.User
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Primary chat-list filter, mirrors the web tabs ВСЕ / ЛС / ГРУППЫ / ЗАЯВКИ. */
enum class ChatFilter { ALL, DMS, GROUPS, REQUESTS }

data class ChatRow(
    val chatId: String,
    val title: String,
    val avatar: String?,
    val subtitle: String,
    val time: String,
    val unread: Boolean,
    val unreadCount: Int,
    val online: Boolean,
    val pinned: Boolean,
    val muted: Boolean,
    val isGroup: Boolean,
    val lastMsgMine: Boolean,
)

data class RequestRow(val id: String, val fromUid: String, val fromNick: String)

data class ChatListUiState(
    val me: User? = null,
    val filter: ChatFilter = ChatFilter.ALL,
    val folders: List<Folder> = emptyList(),
    val selectedFolderId: String? = null,
    val rows: List<ChatRow> = emptyList(),
    val requests: List<RequestRow> = emptyList(),
    val requestCount: Int = 0,
    val archivedCount: Int = 0,
    val showArchived: Boolean = false,
    val loading: Boolean = true,
)

private data class Selection(
    val filter: ChatFilter = ChatFilter.ALL,
    val folderId: String? = null,
    val query: String = "",
    val showArchived: Boolean = false,
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val account: AccountManager,
    private val chats: ChatRepository,
    private val folders: FolderRepository,
    private val users: UserRepository,
    private val friends: FriendRepository,
    private val realtime: RealtimeBus,
    connectionMonitor: ConnectionMonitor,
) : ViewModel() {

    private val myUid = account.uid.orEmpty()

    /** Telegram-style connection status for the top bar (Соединение… / Обновление… / normal). */
    val connection: StateFlow<ConnectionMonitor.State> = connectionMonitor.state

    private val selection = MutableStateFlow(Selection())
    private val peers = MutableStateFlow<Map<String, User>>(emptyMap())

    /** Shared, crash-proof stream of my chats. On this flaky network REST calls intermittently time
     *  out; retry the whole flow with backoff instead of letting the exception reach viewModelScope
     *  and crash the app. Shared (one subscription) between the UI combine and peer-loading. */
    private val myChats: StateFlow<List<Chat>> =
        chats.observeMyChats(myUid)
            .retryWhen { e, attempt ->
                android.util.Log.e("NSDIAG", "observeMyChats failed (attempt=$attempt): ${e.message}")
                delay((1_000L * (attempt + 1)).coerceAtMost(8_000L))
                true
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Resolve DM peer profiles (title/avatar) off the main thread and cache them. Runs as a
        // top-level collector instead of launching from inside `combine`'s transform.
        viewModelScope.launch {
            myChats
                .map { list -> list.filter { it.isDm }.mapNotNull { it.otherMember(myUid) }.toSet() }
                .distinctUntilChanged()
                .collect { peerIds ->
                    peerIds.filter { it !in peers.value }.forEach { uid ->
                        launch(Dispatchers.IO) {
                            android.util.Log.i("NSDIAG", "fetching peer $uid …")
                            users.get(uid)?.let { u -> peers.update { it + (uid to u) } }
                        }
                    }
                }
        }

        // Keep a cache of peer profiles fresh (presence/avatar) from the users table.
        viewModelScope.launch {
            realtime.changes(Tables.USERS)
                .retryWhen { e, attempt ->
                    android.util.Log.e("NSDIAG", "users realtime failed (attempt=$attempt): ${e.message}")
                    delay((1_000L * (attempt + 1)).coerceAtMost(8_000L))
                    true
                }
                .collect { change ->
                    if (change is RowChange.Upsert && peers.value.containsKey(change.id)) {
                        val user = decodeDoc<User>(change.toRow()).copy(id = change.id)
                        peers.update { it + (change.id to user) }
                    }
                }
        }
    }

    val uiState: StateFlow<ChatListUiState> =
        combine(
            myChats,
            folders.observe(myUid),
            peers,
            friends.observeIncoming(myUid),
            selection,
        ) { chatList, folderList, peerMap, requests, sel ->
            val archivedCount = chatList.count { it.isArchivedBy(myUid) }
            val inScope = chatList.filter { it.isArchivedBy(myUid) == sel.showArchived }

            val visible = inScope
                .filter { c ->
                    when (sel.filter) {
                        ChatFilter.ALL -> true
                        ChatFilter.DMS -> c.isDm
                        ChatFilter.GROUPS -> c.isGroup || c.isChannel
                        ChatFilter.REQUESTS -> false
                    }
                }
                .filter { c ->
                    sel.folderId == null ||
                        folderList.find { it.id == sel.folderId }?.chats?.contains(c.id) == true
                }
                .map { toRow(it, peerMap) }
                .filter { sel.query.isBlank() || it.title.contains(sel.query, ignoreCase = true) }
                .sortedWith(compareByDescending<ChatRow> { it.pinned })

            ChatListUiState(
                me = account.me.value,
                filter = sel.filter,
                folders = folderList,
                selectedFolderId = sel.folderId,
                rows = if (sel.filter == ChatFilter.REQUESTS) emptyList() else visible,
                requests = requests.map { RequestRow(it.id, it.from, it.fromNick) },
                requestCount = requests.size,
                archivedCount = archivedCount,
                showArchived = sel.showArchived,
                loading = false,
            )
        }
            // folders/friends REST calls can also time out on this flaky network — retry the whole
            // combine with backoff and never let an exception crash the screen.
            .retryWhen { e, attempt ->
                android.util.Log.e("NSDIAG", "uiState combine failed (attempt=$attempt): ${e.message}")
                delay((1_000L * (attempt + 1)).coerceAtMost(8_000L))
                true
            }
            .catch { e -> android.util.Log.e("NSDIAG", "uiState gave up: ${e.message}"); emit(ChatListUiState(loading = false)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListUiState())

    private fun toRow(chat: Chat, peerMap: Map<String, User>): ChatRow {
        val peer = if (chat.isDm) peerMap[chat.otherMember(myUid)] else null
        val title = when {
            chat.isDm -> peer?.displayName ?: "…"
            else -> chat.name.ifBlank { "Group" }
        }
        val lastReadMs = chat.lastReadAt(myUid)?.toEpochMilliseconds() ?: 0L
        val lastMsgMs = chat.lastMsgAtInstant?.toEpochMilliseconds() ?: 0L
        val unread = chat.lastMsgUid != myUid && lastMsgMs > lastReadMs
        val preview = when {
            chat.lastMsg.isNotBlank() -> chat.lastMsg
            chat.isGroup -> "${chat.members.size} members"
            else -> ""
        }
        return ChatRow(
            chatId = chat.id,
            title = title,
            avatar = if (chat.isDm) peer?.avatar else chat.avatar.ifBlank { null },
            subtitle = preview,
            time = com.nonsense.chat.ui.common.formatTime(chat.lastMsgAtInstant),
            unread = unread,
            unreadCount = if (unread) 1 else 0,
            online = peer?.let { it.online && !it.hideLastSeen } ?: false,
            pinned = chat.isPinnedBy(myUid),
            muted = chat.isMutedBy(myUid),
            isGroup = chat.type != ChatType.DM,
            lastMsgMine = chat.lastMsgUid == myUid,
        )
    }

    // ── Selection ────────────────────────────────────────────────────────────
    fun setQuery(q: String) = selection.update { it.copy(query = q) }
    fun setFilter(f: ChatFilter) = selection.update { it.copy(filter = f, folderId = null) }
    fun selectFolder(id: String?) = selection.update { it.copy(folderId = id, filter = ChatFilter.ALL) }
    fun toggleArchivedView() = selection.update { it.copy(showArchived = !it.showArchived) }

    // ── Per-chat context actions ───────────────────────────────────────────────
    fun togglePin(row: ChatRow) = viewModelScope.launch { chats.setPinned(row.chatId, myUid, !row.pinned) }
    fun toggleMute(row: ChatRow) = viewModelScope.launch { chats.setMuted(row.chatId, myUid, !row.muted) }
    fun archive(row: ChatRow) = viewModelScope.launch { chats.setArchived(row.chatId, myUid, !uiState.value.showArchived) }
    fun leave(row: ChatRow) = viewModelScope.launch { chats.leave(row.chatId, myUid) }
    fun addToFolder(folderId: String, chatId: String, present: Boolean) =
        viewModelScope.launch { folders.setChatInFolder(folderId, chatId, present) }

    // ── Folder management ──────────────────────────────────────────────────────
    fun createFolder(name: String, icon: String) =
        viewModelScope.launch { if (name.isNotBlank()) folders.create(myUid, name, icon.ifBlank { "📁" }) }
    fun deleteFolder(folderId: String) = viewModelScope.launch { folders.delete(folderId) }

    // ── Friend requests ────────────────────────────────────────────────────────
    val acceptedChat = MutableStateFlow<String?>(null)
    fun acceptRequest(req: RequestRow) = viewModelScope.launch {
        val chatId = chats.getOrCreateDm(myUid, req.fromUid)
        friends.remove(req.id)
        acceptedChat.value = chatId
    }
    fun declineRequest(req: RequestRow) = viewModelScope.launch { friends.remove(req.id) }
    fun consumeAccepted() { acceptedChat.value = null }
}
