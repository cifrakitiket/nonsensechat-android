package com.nonsense.chat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isSignUp: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun toggleMode() = _state.update { it.copy(isSignUp = !it.isSignUp, error = null) }

    fun submit(email: String, password: String, nick: String) {
        if (email.isBlank() || password.isBlank() || (_state.value.isSignUp && nick.isBlank())) {
            _state.update { it.copy(error = "Fill in all fields") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = runCatching {
                if (_state.value.isSignUp) auth.signUp(email, password, nick)
                else auth.signIn(email, password)
            }
            _state.update {
                it.copy(loading = false, error = result.exceptionOrNull()?.localizedMessage)
            }
            // On success the session changes and the nav graph swaps to the chat list automatically.
        }
    }
}
