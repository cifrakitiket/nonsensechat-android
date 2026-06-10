package com.nonsense.chat.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.FriendRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.FriendRequest
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val account: AccountManager,
    private val users: UserRepository,
    private val friends: FriendRepository,
    private val chats: ChatRepository,
) : ViewModel() {

    private val myUid get() = account.uid.orEmpty()

    val incoming: StateFlow<List<FriendRequest>> =
        friends.observeIncoming(myUid).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _results = MutableStateFlow<List<User>>(emptyList())
    val results: StateFlow<List<User>> = _results

    /** Set by VM when a DM is ready to open; the screen observes and navigates. */
    val openChat = MutableStateFlow<String?>(null)

    fun search(query: String) {
        viewModelScope.launch {
            _results.value = users.searchByNick(query).filter { it.id != myUid }
        }
    }

    fun openDm(uid: String) {
        viewModelScope.launch { openChat.value = chats.getOrCreateDm(myUid, uid) }
    }

    fun sendRequest(target: User) {
        viewModelScope.launch {
            val me = account.me.value ?: return@launch
            friends.send(myUid, me.nick, target.id, target.nick)
        }
    }

    fun accept(request: FriendRequest) {
        viewModelScope.launch {
            val chatId = chats.getOrCreateDm(myUid, request.from)
            friends.remove(request.id)
            openChat.value = chatId
        }
    }

    fun decline(request: FriendRequest) {
        viewModelScope.launch { friends.remove(request.id) }
    }

    fun consumeOpenChat() { openChat.value = null }
}
