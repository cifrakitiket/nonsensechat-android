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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val me by viewModel.me.collectAsState()
    val theme by viewModel.theme.collectAsState(initial = AppTheme.DYNAMIC)
    val notifications by viewModel.notifications.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
            )
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
                Icon(Icons.Default.Edit, "Edit profile")
            }
            HorizontalDivider()

            SectionTitle("Theme")
            val themes = listOf(
                AppTheme.DYNAMIC to "Material You (dynamic)",
                AppTheme.DARK to "Dark",
                AppTheme.COFFEE to "Coffee dark",
                AppTheme.LIGHT to "Light",
                AppTheme.GRADIENT to "Gradient",
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

            SectionTitle("Privacy & notifications")
            ToggleRow("Enable notifications", notifications) { viewModel.setNotifications(it) }
            ToggleRow("Hide last seen", me?.hideLastSeen == true) { viewModel.setHideLastSeen(it) }
            HorizontalDivider()

            Row(
                Modifier.fillMaxWidth().clickable { viewModel.signOut() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                Text("Sign out", Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
        }
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
