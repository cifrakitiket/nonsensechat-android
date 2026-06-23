package com.nonsense.chat.ui.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.repos.ChatRepository
import com.nonsense.chat.data.repos.UserRepository
import com.nonsense.chat.model.User
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewGroupViewModel @Inject constructor(
    private val account: AccountManager,
    private val users: UserRepository,
    private val chats: ChatRepository,
) : ViewModel() {
    private val _results = MutableStateFlow<List<User>>(emptyList())
    val results: StateFlow<List<User>> = _results
    val selected = MutableStateFlow<Set<String>>(emptySet())
    val created = MutableStateFlow<String?>(null)

    fun search(q: String) { viewModelScope.launch { _results.value = users.searchByNick(q).filter { it.id != account.uid } } }

    fun toggle(uid: String) {
        selected.value = if (uid in selected.value) selected.value - uid else selected.value + uid
    }

    fun create(name: String) {
        val myUid = account.uid ?: return
        if (name.isBlank()) return
        viewModelScope.launch { created.value = chats.createGroup(myUid, name, selected.value.toList()) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: NewGroupViewModel = hiltViewModel(),
) {
    val results by viewModel.results.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val created by viewModel.created.collectAsState()
    var name by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(created) { created?.let(onCreated) }

    Scaffold(
        topBar = {
            TgTopAppBar(title = "Новая группа", onBack = onBack)
        },
        floatingActionButton = {
            if (name.isNotBlank()) {
                FloatingActionButton(onClick = { viewModel.create(name) }, containerColor = MaterialTheme.colorScheme.primary, contentColor = androidx.compose.ui.graphics.Color.White) { Icon(Icons.Default.Check, "Создать") }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(name, { name = it }, label = { Text("Название группы") },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(12.dp))
            OutlinedTextField(query, { query = it; viewModel.search(it) }, placeholder = { Text("Добавить участников") },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp))
            LazyColumn(Modifier.fillMaxSize()) {
                items(results, key = { it.id }) { user ->
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.toggle(user.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(user.displayName, user.avatar, 44.dp)
                        Text(user.displayName, Modifier.weight(1f).padding(start = 12.dp),
                            style = MaterialTheme.typography.titleMedium)
                        Checkbox(checked = user.id in selected, onCheckedChange = { viewModel.toggle(user.id) })
                    }
                }
            }
        }
    }
}
