package com.nonsense.chat.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val account: AccountManager,
    private val users: UserRepository,
) : ViewModel() {
    val me: StateFlow<User?> = account.me
    fun save(fields: Map<String, String>, onDone: () -> Unit) {
        val uid = account.uid ?: return
        viewModelScope.launch { users.updateProfile(uid, fields); onDone() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, viewModel: EditProfileViewModel = hiltViewModel()) {
    val me by viewModel.me.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                title = { Text("Редактировать профиль", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
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
