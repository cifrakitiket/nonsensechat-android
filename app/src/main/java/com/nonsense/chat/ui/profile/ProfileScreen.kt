package com.nonsense.chat.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.TgTopAppBar
import com.nonsense.chat.ui.common.presenceText
import com.nonsense.chat.ui.theme.DevPurple
import com.nonsense.chat.ui.theme.VerifiedBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenDm: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsState()
    val openChat by viewModel.openChat.collectAsState()
    val photos by viewModel.sharedPhotos.collectAsState()
    val files by viewModel.sharedFiles.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(openChat) { openChat?.let { onOpenDm(it); viewModel.consumeOpenChat() } }

    Scaffold(
        topBar = {
            TgTopAppBar(title = "Профиль", onBack = onBack)
        },
    ) { padding ->
        val u = user
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(u?.displayName ?: "…", u?.avatar, 96.dp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(u?.displayName ?: "…", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (u?.verified == true) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, "Verified", tint = VerifiedBlue, modifier = Modifier.size(22.dp))
                }
                if (viewModel.isUserDev(u)) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.Code, "Developer", tint = DevPurple, modifier = Modifier.size(22.dp))
                }
            }
            u?.let {
                Text(presenceText(it.lastSeenAt, it.online, it.hideLastSeen, it.typingIn != null),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))

            u?.let { user ->
                if (user.username.isNotBlank()) InfoRow("Имя пользователя", "@${user.username}")
                if (user.bio.isNotBlank()) InfoRow("О себе", user.bio)
                if (user.phone.isNotBlank()) InfoRow("Телефон", user.phone)
                if (user.bday.isNotBlank()) InfoRow("День рождения", user.bday)
                Row(
                    Modifier.fillMaxWidth().clickable {
                        clipboard.setText(AnnotatedString(user.id)); }.padding(vertical = 6.dp),
                ) {
                    Column {
                        Text("UID (нажмите, чтобы скопировать)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(user.id, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (photos.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionLabel("Общие фото")
                LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(photos) { url ->
                        AsyncImage(url, null, contentScale = ContentScale.Crop, modifier = Modifier.size(84.dp).clip(RoundedCornerShape(8.dp)))
                    }
                }
            }
            if (files.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionLabel("Файлы")
                files.forEach { f ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, f.url.toUri())) }
                        }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null)
                        Spacer(Modifier.width(8.dp))
                        Text(f.name, Modifier.weight(1f), maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            if (!viewModel.isMe) {
                Button(onClick = viewModel::openDm, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Chat, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Написать")
                }
                if (viewModel.isCreator) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::toggleVerified, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Verified, null, tint = VerifiedBlue)
                        Spacer(Modifier.width(8.dp))
                        Text(if (u?.verified == true) "Снять верификацию" else "Верифицировать")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(bottom = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
