package com.nonsense.chat.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.MessageRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharedFile(val name: String, val url: String, val size: Long)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val account: AccountManager,
    private val users: UserRepository,
    private val chats: ChatRepository,
    private val messages: MessageRepository,
) : ViewModel() {

    val uid: String = savedState.get<String>("uid").orEmpty()
    val isMe: Boolean get() = uid == account.uid
    val isCreator: Boolean get() = account.uid == UserRepository.CREATOR_UID && UserRepository.CREATOR_UID.isNotBlank()
    fun isUserDev(u: User?) = u != null && u.id == UserRepository.CREATOR_UID && UserRepository.CREATOR_UID.isNotBlank()

    val user: StateFlow<User?> =
        users.observe(uid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val openChat = MutableStateFlow<String?>(null)
    val sharedPhotos = MutableStateFlow<List<String>>(emptyList())
    val sharedFiles = MutableStateFlow<List<SharedFile>>(emptyList())

    init { loadShared() }

    private fun loadShared() {
        val myUid = account.uid ?: return
        if (uid == myUid) return
        viewModelScope.launch {
            // Only show shared media if a DM already exists (don't create one just from viewing).
            val dmId = chats.findDm(myUid, uid) ?: return@launch
            val msgs = messages.observeMessages(dmId).first().filter { !it.deleted }
            sharedPhotos.value = msgs.filter { it.type == MsgType.PHOTO }.mapNotNull { it.photoUrl }.reversed().take(30)
            sharedFiles.value = msgs.filter { it.type == MsgType.FILE }
                .mapNotNull { m -> m.fileUrl?.let { SharedFile(m.fileName ?: "Файл", it, m.fileSize ?: 0L) } }
                .reversed().take(20)
        }
    }

    fun openDm() {
        val myUid = account.uid ?: return
        viewModelScope.launch { openChat.value = chats.getOrCreateDm(myUid, uid) }
    }

    fun toggleVerified() {
        val current = user.value?.verified ?: false
        viewModelScope.launch { users.setVerified(uid, !current) }
    }

    fun consumeOpenChat() { openChat.value = null }
}
