package com.nonsense.chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.SettingsStore
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.User
import com.nonsense.chat.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val account: AccountManager,
    private val settings: SettingsStore,
    private val users: UserRepository,
) : ViewModel() {

    val me: StateFlow<User?> = account.me
    val theme: Flow<AppTheme> = settings.theme
    val notifications: Flow<Boolean> = settings.notificationsEnabled
    val notifPreview: Flow<Boolean> = settings.notifPreview

    val proxyEnabled: Flow<Boolean> = settings.proxyEnabled
    val proxyEndpoints: Flow<String> = settings.proxyEndpoints
    val proxyAllowDirect: Flow<Boolean> = settings.proxyAllowDirect

    fun setTheme(theme: AppTheme) { viewModelScope.launch { settings.setTheme(theme) } }
    fun setNotifications(on: Boolean) { viewModelScope.launch { settings.setNotificationsEnabled(on) } }
    fun setNotifPreview(on: Boolean) { viewModelScope.launch { settings.setNotifPreview(on) } }

    fun setProxyEnabled(on: Boolean) { viewModelScope.launch { settings.setProxyEnabled(on) } }
    fun setProxyEndpoints(text: String) { viewModelScope.launch { settings.setProxyEndpoints(text) } }
    fun setProxyAllowDirect(on: Boolean) { viewModelScope.launch { settings.setProxyAllowDirect(on) } }

    fun setHideLastSeen(hide: Boolean) {
        val uid = account.uid ?: return
        viewModelScope.launch { users.setHideLastSeen(uid, hide) }
    }

    fun signOut() { viewModelScope.launch { account.signOut() } }
}
