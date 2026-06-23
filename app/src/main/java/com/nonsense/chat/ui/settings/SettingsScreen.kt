package com.nonsense.chat.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.data.proxy.parseProxyLines
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar
import com.nonsense.chat.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenVpn: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val me by viewModel.me.collectAsState()
    val theme by viewModel.theme.collectAsState(initial = AppTheme.DARK)
    val notifications by viewModel.notifications.collectAsState(initial = true)
    val notifPreview by viewModel.notifPreview.collectAsState(initial = true)
    val proxyEnabled by viewModel.proxyEnabled.collectAsState(initial = false)
    val proxyAllowDirect by viewModel.proxyAllowDirect.collectAsState(initial = false)
    val savedProxyEndpoints by viewModel.proxyEndpoints.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TgTopAppBar(title = "Настройки", onBack = onBack)
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Profile header
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onEditProfile).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(me?.displayName ?: "…", me?.avatar, 64.dp)
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(me?.displayName ?: "…", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (!me?.username.isNullOrBlank()) {
                        Text("@${me?.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.Edit, "Редактировать", tint = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider()

            SectionTitle("Тема")
            val themes = listOf(
                AppTheme.DARK to "Тёмная",
                AppTheme.LIGHT to "Светлая",
                AppTheme.COFFEE to "Кофейная",
                AppTheme.GRADIENT to "Градиент",
            )
            themes.forEach { (value, label) ->
                Row(
                    Modifier.fillMaxWidth().clickable { viewModel.setTheme(value) }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = theme == value, onClick = { viewModel.setTheme(value) })
                    Text(label, Modifier.padding(start = 8.dp))
                }
            }
            HorizontalDivider()

            SectionTitle("Приватность и уведомления")
            ToggleRow("Уведомления", notifications) { viewModel.setNotifications(it) }
            ToggleRow("Показывать текст в уведомлении", notifPreview) { viewModel.setNotifPreview(it) }
            ToggleRow("Скрывать «был(а) в сети»", me?.hideLastSeen == true) { viewModel.setHideLastSeen(it) }
            HorizontalDivider()

            // Anti-censorship proxy: route all traffic through user-supplied proxies with failover.
            SectionTitle("Обход блокировок (прокси)")
            ActionRow(
                icon = { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary) },
                label = "VPN (системный тоннель)",
                onClick = onOpenVpn,
            )
            ToggleRow("Использовать прокси", proxyEnabled) { viewModel.setProxyEnabled(it) }

            // Local mirror of the persisted list so typing stays smooth; seeded once from storage.
            var endpoints by rememberSaveable { mutableStateOf("") }
            LaunchedEffect(savedProxyEndpoints) {
                if (endpoints.isEmpty() && savedProxyEndpoints.isNotEmpty()) endpoints = savedProxyEndpoints
            }
            OutlinedTextField(
                value = endpoints,
                onValueChange = { endpoints = it; viewModel.setProxyEndpoints(it) },
                label = { Text("Список прокси, по одному в строке") },
                placeholder = { Text("socks5://1.2.3.4:1080\nhttp://user:pass@5.6.7.8:8080") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            )
            val recognized = remember(endpoints) { parseProxyLines(endpoints).size }
            Text(
                "Распознано прокси: $recognized · SOCKS5 без логина/пароля; логин поддерживается для HTTP.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            ToggleRow("Прямое подключение, если прокси недоступны", proxyAllowDirect) {
                viewModel.setProxyAllowDirect(it)
            }
            HorizontalDivider()

            Row(
                Modifier.fillMaxWidth().clickable { viewModel.signOut() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                Text("Выйти", Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionRow(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(label, Modifier.weight(1f).padding(start = 16.dp), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
