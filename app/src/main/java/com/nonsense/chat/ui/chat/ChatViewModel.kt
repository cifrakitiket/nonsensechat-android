package com.nonsense.chat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.call.CallManager
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.RealtimeBus
import com.nonsense.chat.data.RowChange
import com.nonsense.chat.data.StorageRepository
import com.nonsense.chat.data.Tables
import com.nonsense.chat.data.repos.CallRepository
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.MessageRepository
import com.nonsense.chat.data.repos.PresenceRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.Chat
import com.nonsense.chat.model.Message
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.model.PinnedMessage
import com.nonsense.chat.model.ReplyTo
import com.nonsense.chat.model.User
import com.nonsense.chat.model.decodeDoc
import com.nonsense.chat.model.toRow
import com.nonsense.chat.ui.common.formatDay
import com.nonsense.chat.ui.common.presenceText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

data class MessageUi(
    val message: Message,
    val authorName: String,
    val authorAvatar: String?,
    val isMine: Boolean,
    val showAuthor: Boolean,
    val dayHeader: String?,
    // Consecutive messages from the same sender on the same day form a "group". The first gets the
    // author header/avatar; the last gets the Telegram bubble tail; inner ones use tighter corners.
    val firstInGroup: Boolean,
    val lastInGroup: Boolean,
)

data class ForwardTarget(val chatId: String, val title: String, val avatar: String?, val isGroup: Boolean)

data class ChatUiState(
    val loading: Boolean = true,
    val chat: Chat? = null,
    val title: String = "",
    val subtitle: String = "",
    val isGroup: Boolean = false,
    val peerUid: String? = null,
    val avatarUrl: String? = null,
    val messages: List<MessageUi> = emptyList(),
    val replyingTo: Message? = null,
    val editing: Message? = null,
    val pinned: List<PinnedMessage> = emptyList(),
    /** True when the loaded window is full, so an older page probably exists (drives lazy load-up). */
    val canLoadOlder: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val account: AccountManager,
    private val chats: ChatRepository,
    private val messages: MessageRepository,
    private val users: UserRepository,
    private val storage: StorageRepository,
    private val presence: PresenceRepository,
    private val calls: CallRepository,
    private val callManager: CallManager,
    private val realtime: RealtimeBus,
) : ViewModel() {

    val chatId: String = savedState.get<String>("chatId").orEmpty()
    val myUid: String get() = account.uid.orEmpty()
    private val myNick: String get() = account.me.value?.nick ?: "User"

    // Action launches (sends, uploads, receipts) hit the network and would crash the app if a
    // timeout propagated out of viewModelScope. Most writes soft-fail in DocRepository now, but
    // storage.upload throws on its own — this handler keeps any stray failure from killing the app.
    private val safe = kotlinx.coroutines.CoroutineExceptionHandler { _, e ->
        android.util.Log.e("NSDIAG", "chat action failed (swallowed): ${e.message}")
    }

    private val members = MutableStateFlow<Map<String, User>>(emptyMap())
    private val replyingTo = MutableStateFlow<Message?>(null)
    private val editing = MutableStateFlow<Message?>(null)
    private val pendingUploads = MutableStateFlow<List<Message>>(emptyList())
    // Newest-N message window; grown by [loadOlder] as the user scrolls up.
    private val messageLimit = MutableStateFlow(PAGE_SIZE)
    private var typingJob: Job? = null
    private var typingActive = false

    init {
        viewModelScope.launch {
            realtime.changes(Tables.USERS).collect { c ->
                if (c is RowChange.Upsert && members.value.containsKey(c.id)) {
                    members.update { it + (c.id to decodeDoc<User>(c.toRow()).copy(id = c.id)) }
                }
            }
        }
    }

    private val visibleMessages = combine(
        messages.observeMessages(chatId, messageLimit),
        pendingUploads,
    ) { msgs, pending ->
        (msgs + pending).sortedBy { it.at_?.toEpochMilliseconds() ?: Long.MAX_VALUE }
    }

    val uiState: StateFlow<ChatUiState> = combine(
        chats.observe(chatId),
        visibleMessages,
        members,
        replyingTo,
        editing,
    ) { chat, msgs, memberMap, reply, edit ->
        if (chat != null) ensureMembers(chat)
        val peerUid = chat?.takeIf { it.isDm }?.otherMember(myUid)
        val peer = peerUid?.let { memberMap[it] }
        val title = when {
            chat == null -> ""
            chat.isDm -> peer?.displayName ?: "Chat"
            else -> chat.name.ifBlank { "Group" }
        }
        ChatUiState(
            loading = false,
            chat = chat,
            title = title,
            subtitle = subtitleFor(chat, peer, memberMap),
            isGroup = chat?.isGroup == true || chat?.isChannel == true,
            peerUid = peerUid,
            avatarUrl = if (chat?.isDm == true) peer?.avatar else chat?.avatar?.ifBlank { null },
            messages = buildMessageUis(msgs, memberMap),
            replyingTo = reply,
            editing = edit,
            pinned = chat?.pinnedMsgs ?: emptyList(),
            // Window is full → there are probably older messages still to fetch.
            canLoadOlder = msgs.size >= messageLimit.value,
        )
    }
        // Never let a REST/realtime timeout on this flaky network crash the chat screen.
        .retryWhen { e, attempt ->
            android.util.Log.e("NSDIAG", "chat uiState failed (attempt=$attempt): ${e.message}")
            delay((1_000L * (attempt + 1)).coerceAtMost(8_000L))
            true
        }
        .catch { e -> android.util.Log.e("NSDIAG", "chat uiState gave up: ${e.message}"); emit(ChatUiState(loading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    private fun subtitleFor(chat: Chat?, peer: User?, memberMap: Map<String, User>): String {
        if (chat == null) return ""
        val typingUids = chat.typing.filterValues { it }.keys.filter { it != myUid }
        if (chat.isDm) {
            val typing = peer?.typingIn == chatId
            return presenceText(peer?.lastSeenAt, peer?.online == true, peer?.hideLastSeen == true, typing)
        }
        if (typingUids.isNotEmpty()) {
            val names = typingUids.mapNotNull { memberMap[it]?.displayName }.take(2)
            return if (names.isNotEmpty()) "${names.joinToString(", ")} печатает…" else "печатает…"
        }
        return "${chat.members.size} участников"
    }

    private fun buildMessageUis(msgs: List<Message>, memberMap: Map<String, User>): List<MessageUi> {
        var prevUid: String? = null
        var prevDay: String? = null
        return msgs.mapIndexed { i, m ->
            val author = memberMap[m.uid]
            val day = formatDay(m.at_)
            val header = if (day != prevDay) day else null
            val firstInGroup = m.uid != prevUid || header != null
            // A message ends its group when the next (newer) one starts a new day or a new sender,
            // or when it's the newest message in the thread. System messages never group.
            val next = msgs.getOrNull(i + 1)
            val lastInGroup = m.isSystem ||
                next == null || next.isSystem || next.uid != m.uid || formatDay(next.at_) != day
            prevUid = m.uid
            prevDay = day
            MessageUi(
                message = m,
                authorName = author?.displayName ?: m.author.ifBlank { "User" },
                authorAvatar = author?.avatar,
                isMine = m.uid == myUid,
                showAuthor = firstInGroup && !m.isSystem,
                dayHeader = header,
                firstInGroup = firstInGroup,
                lastInGroup = lastInGroup,
            )
        }
    }

    /** Grow the message window by one page. No-op while the current window isn't full yet (either a
     *  fetch is still in flight, or we've already reached the start of the thread). */
    fun loadOlder() {
        if (uiState.value.messages.size < messageLimit.value) return
        messageLimit.update { it + PAGE_SIZE }
    }

    private fun ensureMembers(chat: Chat) {
        val missing = chat.members.filter { it !in members.value }
        if (missing.isEmpty()) return
        viewModelScope.launch {
            // Load every missing member in one batched request rather than sequentially per uid.
            val fetched = users.getMany(missing)
            if (fetched.isNotEmpty()) members.update { it + fetched }
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private fun pendingAttachment(
        type: String,
        bytes: ByteArray,
        fileName: String,
        contentType: String?,
        caption: String? = null,
        spoiler: Boolean = false,
    ): Message {
        val id = "local-${UUID.randomUUID()}"
        return Message(
            id = id,
            uid = myUid,
            author = myNick,
            at = JsonPrimitive(Clock.System.now().toEpochMilliseconds()),
            type = type,
            photoUrl = if (type == MsgType.PHOTO) "local:$id" else null,
            caption = caption,
            isSpoiler = spoiler,
            fileUrl = if (type != MsgType.PHOTO) "local:$id" else null,
            fileName = fileName,
            fileSize = bytes.size.toLong(),
            mimeType = contentType,
            chatId = chatId,
            localUpload = true,
            localBytes = bytes,
        )
    }

    private fun addPending(message: Message) {
        pendingUploads.update { it + message }
    }

    private fun removePending(id: String) {
        pendingUploads.update { list -> list.filterNot { it.id == id } }
    }

    private fun failPending(id: String) {
        pendingUploads.update { list -> list.map { if (it.id == id) it.copy(localFailed = true) else it } }
    }

    private fun looksLikeImage(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")

    private fun looksLikeVideo(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in setOf("mp4", "mov", "m4v", "webm", "mkv", "avi")

    private fun looksLikeAudio(name: String): Boolean =
        name.substringAfterLast('.', "").lowercase() in setOf("mp3", "m4a", "aac", "wav", "ogg", "opus", "flac")

    fun sendText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val edit = editing.value
            if (edit != null) {
                messages.editText(edit.id, trimmed)
                editing.value = null
            } else {
                val reply = replyingTo.value?.let {
                    ReplyTo(it.id, it.author, it.text.take(120), it.uid)
                }
                messages.sendText(chatId, myUid, myNick, trimmed, replyTo = reply)
                replyingTo.value = null
            }
            stopTyping()
        }
    }

    fun sendPhoto(bytes: ByteArray, fileName: String, caption: String?, spoiler: Boolean) {
        val pending = pendingAttachment(MsgType.PHOTO, bytes, fileName, null, caption = caption, spoiler = spoiler)
        addPending(pending)
        viewModelScope.launch(safe) {
            runCatching {
                val up = storage.upload(bytes, fileName, compressIfImage = true, thumbnail = true)
                messages.sendPhoto(chatId, myUid, myNick, up.url, up.fileName, up.size, caption, spoiler, up.thumbUrl)
            }.onSuccess {
                removePending(pending.id)
            }.onFailure {
                failPending(pending.id)
            }
        }
    }

    fun sendFile(bytes: ByteArray, fileName: String, contentType: String?) {
        val pending = pendingAttachment(MsgType.FILE, bytes, fileName, contentType)
        addPending(pending)
        viewModelScope.launch(safe) {
            runCatching {
                val up = storage.upload(bytes, fileName, contentType, compressIfImage = false)
                messages.sendFile(chatId, myUid, myNick, up.url, up.fileName, up.size, contentType)
            }.onSuccess {
                removePending(pending.id)
            }.onFailure {
                failPending(pending.id)
            }
        }
    }

    fun sendAudio(bytes: ByteArray, fileName: String, contentType: String?) {
        val pending = pendingAttachment(MsgType.AUDIO, bytes, fileName, contentType)
        addPending(pending)
        viewModelScope.launch(safe) {
            runCatching {
                val up = storage.upload(bytes, fileName, contentType, compressIfImage = false)
                messages.sendAudio(chatId, myUid, myNick, up.url, up.fileName, up.size, contentType)
            }.onSuccess {
                removePending(pending.id)
            }.onFailure {
                failPending(pending.id)
            }
        }
    }

    fun sendVideo(bytes: ByteArray, fileName: String, contentType: String?) {
        val pending = pendingAttachment(MsgType.VIDEO, bytes, fileName, contentType)
        addPending(pending)
        viewModelScope.launch(safe) {
            runCatching {
                val up = storage.upload(bytes, fileName, contentType, compressIfImage = false)
                messages.sendVideo(chatId, myUid, myNick, up.url, up.fileName, up.size, contentType)
            }.onSuccess {
                removePending(pending.id)
            }.onFailure {
                failPending(pending.id)
            }
        }
    }

    fun sendAttachment(bytes: ByteArray, fileName: String, contentType: String?) {
        when {
            contentType?.startsWith("image/") == true || looksLikeImage(fileName) ->
                sendPhoto(bytes, fileName, caption = null, spoiler = false)
            contentType?.startsWith("video/") == true || looksLikeVideo(fileName) ->
                sendVideo(bytes, fileName, contentType)
            contentType?.startsWith("audio/") == true || looksLikeAudio(fileName) ->
                sendAudio(bytes, fileName, contentType)
            else -> sendFile(bytes, fileName, contentType)
        }
    }

    fun sendSticker(url: String, emoji: String, packId: String) {
        viewModelScope.launch { messages.sendSticker(chatId, myUid, myNick, url, emoji, packId) }
    }

    fun sendPoll(poll: kotlinx.serialization.json.JsonElement, question: String) {
        viewModelScope.launch { messages.sendPoll(chatId, myUid, myNick, poll, question) }
    }

    fun toggleReaction(message: Message, emoji: String) {
        viewModelScope.launch {
            messages.toggleReaction(message.id, emoji, myUid, add = !message.reactedBy(emoji, myUid))
        }
    }

    fun votePoll(message: Message, optionIndex: Int) {
        val poll = message.poll ?: return
        viewModelScope.launch {
            val current = poll.votesOf(myUid)
            if (poll.isMultiple) {
                val next = if (optionIndex in current) current - optionIndex else current + optionIndex
                if (next.isEmpty()) messages.retractVote(message.id, myUid)
                else messages.votePoll(message.id, myUid, kotlinx.serialization.json.JsonArray(next.map { JsonPrimitive(it) }))
            } else {
                if (optionIndex in current) messages.retractVote(message.id, myUid)
                else messages.votePoll(message.id, myUid, JsonPrimitive(optionIndex))
            }
        }
    }

    fun startReply(message: Message) { replyingTo.value = message; editing.value = null }
    fun cancelReply() { replyingTo.value = null }
    fun startEdit(message: Message) { editing.value = message; replyingTo.value = null }
    fun cancelEdit() { editing.value = null }

    fun deleteMessage(message: Message) {
        viewModelScope.launch { messages.softDelete(message.id) }
    }

    // ── Forwarding ─────────────────────────────────────────────────────────────
    val forwardTargets = MutableStateFlow<List<ForwardTarget>>(emptyList())

    fun loadForwardTargets() {
        viewModelScope.launch {
            val list = chats.observeMyChats(myUid).first()
            forwardTargets.value = list.map { c ->
                if (c.isDm) {
                    val peer = c.otherMember(myUid)?.let { members.value[it] ?: users.get(it) }
                    ForwardTarget(c.id, peer?.displayName ?: "Чат", peer?.avatar, false)
                } else {
                    ForwardTarget(c.id, c.name.ifBlank { "Группа" }, c.avatar.ifBlank { null }, true)
                }
            }
        }
    }

    fun forwardMessage(message: Message, targetChatId: String) {
        viewModelScope.launch { messages.forward(targetChatId, myUid, myNick, message) }
    }

    // ── Pinned messages ────────────────────────────────────────────────────────
    fun pinMessage(message: Message) {
        val current = uiState.value.chat?.pinnedMsgs ?: emptyList()
        if (current.any { it.id == message.id }) return
        val preview = message.text.ifBlank {
            when (message.type) {
                MsgType.PHOTO -> "📷 Фото"; MsgType.FILE -> "📎 ${message.fileName ?: "Файл"}"
                MsgType.STICKER -> "${message.emoji.orEmpty()} Стикер"; MsgType.POLL -> "📊 Опрос"
                else -> "Сообщение"
            }
        }
        viewModelScope.launch {
            chats.setMessagePinned(chatId, current + PinnedMessage(message.id, preview.take(120), message.author))
        }
    }

    fun unpinMessage(id: String) {
        val current = uiState.value.chat?.pinnedMsgs ?: emptyList()
        viewModelScope.launch { chats.setMessagePinned(chatId, current.filterNot { it.id == id }) }
    }

    // ── Calls ──────────────────────────────────────────────────────────────────
    val callActive: StateFlow<Boolean> =
        calls.observe(chatId)
            .map { it != null && it.participants.isNotEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun startCall(video: Boolean) {
        callManager.start(chatId, video, uiState.value.title.ifBlank { "Звонок" }, asCaller = true)
    }

    fun joinCall(video: Boolean) {
        callManager.start(chatId, video, uiState.value.title.ifBlank { "Звонок" }, asCaller = false)
    }

    fun markRead(visible: List<Message>) {
        viewModelScope.launch {
            chats.markRead(chatId, myUid)
            visible.filter { it.uid != myUid && myUid !in it.readAt.keys }
                .takeLast(30)
                .forEach { messages.markRead(it.id, myUid) }
        }
    }

    fun onComposerChanged(text: String) {
        if (text.isBlank()) { stopTyping(); return }
        if (!typingActive) {
            typingActive = true
            setTyping(true)
        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2500)
            stopTyping()
        }
    }

    private fun stopTyping() {
        typingJob?.cancel()
        if (typingActive) { typingActive = false; setTyping(false) }
    }

    private fun setTyping(on: Boolean) {
        viewModelScope.launch {
            val chat = uiState.value.chat
            if (chat?.isDm == true) presence.setTypingIn(myUid, if (on) chatId else null)
            else chats.setGroupTyping(chatId, myUid, on)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTyping()
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
