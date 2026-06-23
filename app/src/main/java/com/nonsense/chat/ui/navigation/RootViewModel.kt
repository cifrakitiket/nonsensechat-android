package com.nonsense.chat.ui.navigation

import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.AuthState
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import androidx.lifecycle.ViewModel

@HiltViewModel
class RootViewModel @Inject constructor(
    account: AccountManager,
) : ViewModel() {
    val authState: StateFlow<AuthState> = account.authState
    val me: StateFlow<User?> = account.me
}
