package com.nonsense.chat.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar
import com.nonsense.chat.ui.common.readUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.StorageRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val account: AccountManager,
    private val users: UserRepository,
    private val storage: StorageRepository,
) : ViewModel() {
    val me: StateFlow<User?> = account.me

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    /** Uploads a small avatar image and hands the resulting public URL back to the screen. */
    fun uploadAvatar(bytes: ByteArray, fileName: String, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            _uploading.value = true
            runCatching { storage.uploadAvatar(bytes, fileName) }
                .onSuccess(onUrl)
            _uploading.value = false
        }
    }

    fun save(fields: Map<String, String>, onDone: () -> Unit) {
        val uid = account.uid ?: return
        viewModelScope.launch { users.updateProfile(uid, fields); onDone() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: EditProfileViewModel = hiltViewModel()) {
    val me by viewModel.me.collectAsState()
    val uploading by viewModel.uploading.collectAsState()
    val context = LocalContext.current

    var nick by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var fname by remember { mutableStateOf("") }
    var lname by remember { mutableStateOf("") }
    var bday by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("") }

    LaunchedEffect(me) {
        me?.let {
            nick = it.nick; username = it.username; bio = it.bio; fname = it.fname
            lname = it.lname; bday = it.bday; phone = it.phone; avatar = it.avatar
        }
    }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { readUri(context, it)?.let { f -> viewModel.uploadAvatar(f.bytes, f.name) { url -> avatar = url } } }
    }

    Scaffold(
        topBar = {
            TgTopAppBar(
                title = "Редактировать профиль",
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        viewModel.save(
                            mapOf(
                                "nick" to nick, "username" to username, "bio" to bio,
                                "fname" to fname, "lname" to lname, "bday" to bday,
                                "phone" to phone, "avatar" to avatar,
                            ),
                            onBack,
                        )
                    }) { Icon(Icons.Default.Check, "Сохранить", tint = MaterialTheme.colorScheme.primary) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
        ) {
            Field("Никнейм", nick) { nick = it }
            Field("Имя пользователя (без @)", username) { username = it }

            // Avatar: upload a small image (loads even on a throttled network) or paste a URL.
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(nick.ifBlank { fname }.ifBlank { "?" }, avatar.ifBlank { null }, 64.dp)
                Spacer(Modifier.width(16.dp))
                OutlinedButton(
                    enabled = !uploading,
                    onClick = {
                        avatarPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                ) {
                    if (uploading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Загрузка…")
                    } else {
                        Text("Загрузить фото")
                    }
                }
            }
            Field("Ссылка на аватар", avatar) { avatar = it }
            Field("О себе", bio) { bio = it }
            Field("Имя", fname) { fname = it }
            Field("Фамилия", lname) { lname = it }
            Field("День рождения (ГГГГ-ММ-ДД)", bday) { bday = it }
            Field("Телефон", phone) { phone = it }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    )
}
