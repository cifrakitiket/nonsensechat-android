package com.nonsense.chat.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.nonsense.chat.data.AccountManager
import com.nonsense.chat.data.StorageRepository
import com.nonsense.chat.data.repos.StickerRepository
import com.nonsense.chat.model.Sticker
import com.nonsense.chat.model.StickerPack
import com.nonsense.chat.ui.common.readUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickerSticker(val url: String, val emoji: String, val packId: String)

@HiltViewModel
class StickerPickerViewModel @Inject constructor(
    private val stickers: StickerRepository,
    private val storage: StorageRepository,
    private val account: AccountManager,
) : ViewModel() {
    private val _items = MutableStateFlow<List<PickerSticker>>(emptyList())
    val items: StateFlow<List<PickerSticker>> = _items

    private val _packs = MutableStateFlow<List<StickerPack>>(emptyList())
    val packs: StateFlow<List<StickerPack>> = _packs

    private val _installed = MutableStateFlow<Set<String>>(emptySet())
    val installed: StateFlow<Set<String>> = _installed

    private val _drafts = MutableStateFlow<List<Sticker>>(emptyList())
    val drafts: StateFlow<List<Sticker>> = _drafts

    val uploading = MutableStateFlow(false)

    val myUid get() = account.uid

    init { refresh() }

    fun refresh() {
        _items.value = BuiltInStickers
        viewModelScope.launch {
            val installedIds = account.me.value?.installedPacks?.toSet().orEmpty()
            _installed.value = installedIds
            val all = runCatching { stickers.allPacks() }.getOrDefault(emptyList())
            _packs.value = all
            val fromServer = all
                .filter { it.id in installedIds || it.authorUid == account.uid }
                .flatMap { pack -> pack.stickers.map { PickerSticker(it.url, it.emoji, pack.id) } }
            _items.value = BuiltInStickers + fromServer
        }
    }

    fun install(packId: String) = viewModelScope.launch {
        account.uid?.let { stickers.installForUser(it, packId); _installed.update(packId, true); refresh() }
    }

    fun uninstall(packId: String) = viewModelScope.launch {
        account.uid?.let { stickers.uninstallForUser(it, packId); _installed.update(packId, false); refresh() }
    }

    private fun MutableStateFlow<Set<String>>.update(id: String, present: Boolean) {
        value = if (present) value + id else value - id
    }

    fun addDraft(bytes: ByteArray, fileName: String) = viewModelScope.launch {
        uploading.value = true
        runCatching { storage.upload(bytes, fileName, compressIfImage = true) }
            .onSuccess { _drafts.value = _drafts.value + Sticker(it.url, "😀") }
        uploading.value = false
    }

    fun setDraftEmoji(index: Int, emoji: String) {
        _drafts.value = _drafts.value.toMutableList().also { if (index in it.indices) it[index] = it[index].copy(emoji = emoji) }
    }

    fun removeDraft(index: Int) {
        _drafts.value = _drafts.value.toMutableList().also { if (index in it.indices) it.removeAt(index) }
    }

    fun clearDrafts() { _drafts.value = emptyList() }

    fun publishPack(title: String, onDone: () -> Unit) = viewModelScope.launch {
        val uid = account.uid ?: return@launch
        val nick = account.me.value?.nick ?: "User"
        if (title.isNotBlank() && _drafts.value.isNotEmpty()) {
            stickers.createPack(uid, nick, title, _drafts.value)
            clearDrafts(); refresh(); onDone()
        }
    }

    fun deletePack(packId: String) = viewModelScope.launch { stickers.deletePack(packId); refresh() }
}

private enum class StickerMode { SEND, PACKS, CREATE }
private enum class PanelTab { EMOJI, STICKERS }

/**
 * Telegram-style bottom panel: an "Эмодзи" tab (taps INSERT into the input via [onPickEmoji]) and a
 * "Стикеры" tab (taps SEND a sticker via [onPickSticker]). The stickers tab also reaches pack
 * management (gear → install/create). Built-in emoji live only in the Эмодзи tab now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiStickerSheet(
    onPickEmoji: (String) -> Unit,
    onPickSticker: (PickerSticker) -> Unit,
    onDismiss: () -> Unit,
    viewModel: StickerPickerViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    var tab by remember { mutableStateOf(PanelTab.EMOJI) }
    var mode by remember { mutableStateOf(StickerMode.SEND) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxWidth().heightIn(min = 280.dp)) {
            if (tab == PanelTab.EMOJI) {
                EmojiGrid(onPick = onPickEmoji)
            } else when (mode) {
                StickerMode.SEND -> StickerGrid(
                    // Only real (image) pack stickers here; emoji moved to the Эмодзи tab.
                    stickers = items.filter { it.packId != BUILTIN_STICKER_PACK_ID && it.url.isNotBlank() },
                    onPick = { onPickSticker(it); onDismiss() },
                    onManage = { mode = StickerMode.PACKS },
                )
                StickerMode.PACKS -> PacksView(viewModel, onCreate = { mode = StickerMode.CREATE })
                StickerMode.CREATE -> CreatePackView(viewModel, onDone = { mode = StickerMode.PACKS })
            }
        }
        PanelTabBar(tab) { tab = it; if (it == PanelTab.STICKERS) mode = StickerMode.SEND }
    }
}

@Composable
private fun PanelTabBar(tab: PanelTab, onSelect: (PanelTab) -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PanelTabButton("😀", "Эмодзи", tab == PanelTab.EMOJI, Modifier.weight(1f)) { onSelect(PanelTab.EMOJI) }
            PanelTabButton("🖼", "Стикеры", tab == PanelTab.STICKERS, Modifier.weight(1f)) { onSelect(PanelTab.STICKERS) }
        }
    }
}

@Composable
private fun PanelTabButton(glyph: String, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    else androidx.compose.ui.graphics.Color.Transparent
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, fontSize = 18.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun EmojiGrid(onPick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(44.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(horizontal = 10.dp),
    ) {
        EmojiKeyboard.forEach { (category, emojis) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 2.dp),
                )
            }
            items(emojis) { e ->
                Box(Modifier.size(44.dp).clickable { onPick(e) }, contentAlignment = Alignment.Center) {
                    Text(e, fontSize = 26.sp)
                }
            }
        }
    }
}

@Composable
private fun StickerGrid(stickers: List<PickerSticker>, onPick: (PickerSticker) -> Unit, onManage: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Стикеры", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onManage) { Icon(Icons.Default.Settings, "Наборы") }
        }
        if (stickers.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().heightIn(min = 200.dp).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Пока нет стикеров", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onManage) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Наборы") }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(72.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(8.dp),
            ) {
                items(stickers) { s ->
                    Box(
                        Modifier.size(72.dp).padding(4.dp).clickable { onPick(s) },
                        contentAlignment = Alignment.Center,
                    ) { AsyncImage(s.url, s.emoji, modifier = Modifier.size(64.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PacksView(viewModel: StickerPickerViewModel, onCreate: () -> Unit) {
    val packs by viewModel.packs.collectAsState()
    val installed by viewModel.installed.collectAsState()
    val myUid = viewModel.myUid
    Column(Modifier.fillMaxWidth().heightIn(max = 380.dp).padding(12.dp)) {
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Создать набор")
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(packs, key = { it.id }) { pack ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    pack.stickers.firstOrNull()?.url?.let { AsyncImage(it, null, modifier = Modifier.size(44.dp)) }
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(pack.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text("${pack.stickers.size} · @${pack.authorNick}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    when {
                        pack.authorUid == myUid -> IconButton(onClick = { viewModel.deletePack(pack.id) }) {
                            Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                        }
                        pack.id in installed -> TextButton(onClick = { viewModel.uninstall(pack.id) }) { Text("Удалить") }
                        else -> Button(onClick = { viewModel.install(pack.id) }) { Text("Добавить") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePackView(viewModel: StickerPickerViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val drafts by viewModel.drafts.collectAsState()
    val uploading by viewModel.uploading.collectAsState()
    var title by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { readUri(context, it)?.let { f -> viewModel.addDraft(f.bytes, f.name) } }
    }

    Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(12.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it.take(48) }, label = { Text("Название набора") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Добавить стикер")
            }
            if (uploading) { Spacer(Modifier.width(12.dp)); CircularProgressIndicator(Modifier.size(20.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Adaptive(80.dp), modifier = Modifier.weight(1f)) {
            items(drafts.size) { i ->
                val s = drafts[i]
                Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        AsyncImage(s.url, null, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                        Box(Modifier.align(Alignment.TopEnd).clickable { viewModel.removeDraft(i) }) {
                            Icon(Icons.Default.Delete, "Убрать", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                    OutlinedTextField(
                        value = s.emoji, onValueChange = { viewModel.setDraftEmoji(i, it.take(2)) },
                        singleLine = true, modifier = Modifier.width(72.dp),
                    )
                }
            }
        }
        Button(
            onClick = { viewModel.publishPack(title) { onDone() } },
            enabled = title.isNotBlank() && drafts.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) { Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(6.dp)); Text("Опубликовать") }
    }
}

// ── Sticker pack preview (opened by tapping a sticker in chat) ────────────────────────────────

@HiltViewModel
class StickerPackViewModel @Inject constructor(
    private val stickers: StickerRepository,
    private val account: AccountManager,
) : ViewModel() {
    private val _pack = MutableStateFlow<StickerPack?>(null)
    val pack: StateFlow<StickerPack?> = _pack

    private val _installed = MutableStateFlow(false)
    val installed: StateFlow<Boolean> = _installed

    val isMine: Boolean get() = _pack.value?.authorUid == account.uid

    fun load(packId: String) = viewModelScope.launch {
        _installed.value = account.me.value?.installedPacks?.contains(packId) == true
        _pack.value = runCatching { stickers.pack(packId) }.getOrNull()
    }

    fun install() = viewModelScope.launch {
        val id = _pack.value?.id ?: return@launch
        account.uid?.let { stickers.installForUser(it, id); _installed.value = true }
    }

    fun uninstall() = viewModelScope.launch {
        val id = _pack.value?.id ?: return@launch
        account.uid?.let { stickers.uninstallForUser(it, id); _installed.value = false }
    }
}

/** Telegram-style pack preview shown when a sticker message is tapped: grid + add/remove. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPackSheet(
    packId: String,
    onDismiss: () -> Unit,
    onSend: (PickerSticker) -> Unit,
    viewModel: StickerPackViewModel = hiltViewModel(),
) {
    LaunchedEffect(packId) { viewModel.load(packId) }
    val pack by viewModel.pack.collectAsState()
    val installed by viewModel.installed.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        val p = pack
        if (p == null) {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${p.stickers.size} · @${p.authorNick}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(72.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                ) {
                    items(p.stickers) { s ->
                        Box(
                            Modifier.size(72.dp).padding(4.dp)
                                .clickable { onSend(PickerSticker(s.url, s.emoji, p.id)) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (s.url.isNotBlank()) AsyncImage(s.url, s.emoji, modifier = Modifier.size(64.dp))
                            else Text(s.emoji, fontSize = 36.sp)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                when {
                    viewModel.isMine -> {} // own pack — managed from the picker's "Наборы"
                    installed -> OutlinedButton(onClick = { viewModel.uninstall() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Удалить набор")
                    }
                    else -> Button(onClick = { viewModel.install() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Добавить набор")
                    }
                }
                Spacer(Modifier.navigationBarsPadding().height(4.dp))
            }
        }
    }
}
