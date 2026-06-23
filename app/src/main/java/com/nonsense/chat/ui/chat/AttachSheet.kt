package com.nonsense.chat.ui.chat

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.nonsense.chat.ui.common.PickedFile
import com.nonsense.chat.ui.common.TgGradientButton
import com.nonsense.chat.ui.common.brandGradient
import com.nonsense.chat.ui.common.readUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** A single device gallery entry (image or video) surfaced from MediaStore. */
private data class MediaItem(val uri: Uri, val isVideo: Boolean)

/**
 * Telegram-style attachment sheet: a bottom sheet with a built-in grid of the device's own photos
 * and videos (queried straight from MediaStore — NOT the Google Photos / system picker), plus a row
 * of actions (camera, file, poll). Multi-select photos and send them in one tap.
 *
 * Sending reuses the existing pipeline: [readUri] -> [onSendPhoto]/[onSendFile] (which upload via
 * Storage.upload with compression + thumbnails). Images go through onSendPhoto; videos and other
 * files go through onSendFile (there is no dedicated video message type).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachSheet(
    onDismiss: () -> Unit,
    onSendPhoto: (PickedFile) -> Unit,
    onSendVideo: (PickedFile) -> Unit,
    onSendAudio: (PickedFile) -> Unit,
    onSendFile: (PickedFile) -> Unit,
    onCreatePoll: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hasPermission by remember { mutableStateOf(hasMediaPermission(context)) }
    var media by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    val selected = remember { mutableStateListOf<Uri>() }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        // Any granted read (full or, on 34+, user-selected) lets MediaStore return something.
        hasPermission = result.values.any { it } || hasMediaPermission(context)
    }

    // File picker (anything) — keeps the system content picker for arbitrary files only.
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val f = withContext(Dispatchers.IO) { readUri(context, uri) } ?: return@launch
            onSendFile(f)
            onDismiss()
        }
    }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val f = withContext(Dispatchers.IO) { readUri(context, uri) } ?: return@launch
            onSendAudio(f)
            onDismiss()
        }
    }

    // Camera capture into a FileProvider-backed temp file, then send the captured image.
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = cameraUri
        if (ok && uri != null) {
            scope.launch {
                val f = withContext(Dispatchers.IO) { readUri(context, uri) } ?: return@launch
                onSendPhoto(f)
                onDismiss()
            }
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            media = withContext(Dispatchers.IO) { queryDeviceMedia(context) }
        }
    }

    fun sendSelected() {
        if (selected.isEmpty()) return
        val byUri = media.associateBy { it.uri }
        val toSend = selected.toList()
        scope.launch {
            for (uri in toSend) {
                val f = withContext(Dispatchers.IO) { readUri(context, uri) } ?: continue
                if (byUri[uri]?.isVideo == true) onSendVideo(f) else onSendPhoto(f)
            }
            onDismiss()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            // Action row (camera / file / poll), like Telegram's top attach row.
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                AttachAction(Icons.Default.PhotoCamera, "Камера") {
                    val uri = createCameraUri(context)
                    cameraUri = uri
                    cameraLauncher.launch(uri)
                }
                AttachAction(Icons.AutoMirrored.Filled.InsertDriveFile, "Файл") {
                    filePicker.launch("*/*")
                }
                AttachAction(Icons.Default.Mic, "Audio") {
                    audioPicker.launch("audio/*")
                }
                AttachAction(Icons.Default.Poll, "Опрос") {
                    onDismiss(); onCreatePoll()
                }
            }

            Spacer(Modifier.height(8.dp))

            if (!hasPermission) {
                PermissionPrompt { permLauncher.launch(mediaPermissions()) }
            } else if (media.isEmpty()) {
                Box(Modifier.fillMaxWidth().heightIn(min = 160.dp), contentAlignment = Alignment.Center) {
                    Text("Нет фото на устройстве", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(96.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp),
                ) {
                    items(media, key = { it.uri.toString() }) { item ->
                        val index = selected.indexOf(item.uri)
                        MediaCell(
                            item = item,
                            selectionOrder = if (index >= 0) index + 1 else 0,
                            onClick = {
                                if (index >= 0) selected.removeAt(index) else selected.add(item.uri)
                            },
                        )
                    }
                }
            }

            if (selected.isNotEmpty()) {
                TgGradientButton(
                    text = "Отправить (${selected.size})",
                    onClick = { sendSelected() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AttachAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(brandGradient()),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, label, tint = Color.White) }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MediaCell(item: MediaItem, selectionOrder: Int, onClick: () -> Unit) {
    val selected = selectionOrder > 0
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = item.uri,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            Icon(
                Icons.Default.PlayCircle, null, tint = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp).size(20.dp),
            )
        }
        // Selection badge: numbered accent circle (multi-select order) in the top-right corner.
        Box(
            Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clip(CircleShape)
                .then(
                    if (selected) Modifier.background(MaterialTheme.colorScheme.primary)
                    else Modifier.border(2.dp, Color.White, CircleShape),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text("$selectionOrder", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 160.dp).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Photo, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text("Разрешите доступ к фото, чтобы выбрать их прямо здесь",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        TgGradientButton(text = "Разрешить", onClick = onGrant)
    }
}

// MediaStore / permissions helpers.

private fun mediaPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        )
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

private fun hasMediaPermission(context: Context): Boolean =
    mediaPermissions().any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

/** Newest-first list of device images + videos via MediaStore (no system picker UI). */
private fun queryDeviceMedia(context: Context): List<MediaItem> {
    val out = ArrayList<MediaItem>(256)
    val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATE_ADDED)
    // Images
    runCatching {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                out.add(MediaItem(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), false))
            }
        }
    }
    // Videos
    runCatching {
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC",
        )?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                out.add(MediaItem(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id), true))
            }
        }
    }
    return out
}

/** A FileProvider URI for the camera to write a freshly captured photo into. */
private fun createCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    // Stable-ish name without Date/Random (both restricted): nanoTime is allowed and unique enough.
    val file = File(dir, "cam_${System.nanoTime()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
