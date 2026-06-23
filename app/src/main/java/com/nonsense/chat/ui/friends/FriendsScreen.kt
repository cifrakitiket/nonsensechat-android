package com.nonsense.chat.ui.friends

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val results by viewModel.results.collectAsState()
    val incoming by viewModel.incoming.collectAsState()
    val openChat by viewModel.openChat.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(openChat) {
        openChat?.let { onOpenChat(it); viewModel.consumeOpenChat() }
    }

    Scaffold(
        topBar = {
            TgTopAppBar(title = "Новый чат", onBack = onBack)
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.search(it) },
                placeholder = { Text("Поиск по нику") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )

            LazyColumn(Modifier.fillMaxSize()) {
                if (incoming.isNotEmpty()) {
                    item {
                        Text("Заявки в друзья", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    items(incoming, key = { it.id }) { req ->
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(req.fromNick, null, 44.dp)
                            Text(req.fromNick, Modifier.weight(1f).padding(start = 12.dp),
                                style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.accept(req) }) { Icon(Icons.Default.Check, "Принять", tint = com.nonsense.chat.ui.theme.OnlineGreen) }
                            IconButton(onClick = { viewModel.decline(req) }) { Icon(Icons.Default.Close, "Отклонить", tint = MaterialTheme.colorScheme.error) }
                        }
                    }
                    item { HorizontalDivider() }
                }

                items(results, key = { it.id }) { user ->
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.openDm(user.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(user.displayName, user.avatar, 44.dp,
                            Modifier.clickable { onOpenProfile(user.id) })
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                            if (user.username.isNotBlank()) {
                                Text("@${user.username}", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { viewModel.sendRequest(user) }) {
                            Icon(Icons.Default.PersonAdd, "Добавить", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
