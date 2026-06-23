package com.nonsense.chat.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    val manager: CallManager,
    private val users: UserRepository,
    private val account: AccountManager,
) : ViewModel() {

    val myUid get() = account.uid.orEmpty()
    val eglBase get() = manager.eglBase

    private val _names = MutableStateFlow<Map<String, User>>(emptyMap())
    val names: StateFlow<Map<String, User>> = _names

    init {
        viewModelScope.launch {
            manager.peers.collect { peers ->
                val missing = peers.keys.filter { it !in _names.value }
                missing.forEach { uid -> users.get(uid)?.let { u -> _names.value = _names.value + (uid to u) } }
            }
        }
    }

    fun nameFor(uid: String): String = _names.value[uid]?.displayName ?: "…"
    fun avatarFor(uid: String): String? = _names.value[uid]?.avatar

    fun toggleMute() = manager.toggleMute()
    fun toggleVideo() = manager.toggleVideo()
    fun switchCamera() = manager.switchCamera()
    fun toggleSpeaker() = manager.toggleSpeaker()
    fun hangUp() = manager.hangUp()
}
