package com.nonsense.chat.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.formatTime
import com.nonsense.chat.ui.theme.TgAccent3
import com.nonsense.chat.ui.theme.TgOutGradBottom
import com.nonsense.chat.ui.theme.TgOutGradTop

private val QUICK_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    ui: MessageUi,
    isGroup: Boolean,
    readByOther: Boolean,
    isPinned: Boolean,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onReact: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onVote: (Int) -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    val msg = ui.message

    if (msg.isSystem || msg.type == MsgType.CALL) {
        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                Text(
                    msg.text.ifBlank { if (msg.type == MsgType.CALL) "Call" else "" },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        return
    }

    var menu by remember { mutableStateOf(false) }
    val mine = ui.isMine
    // Telegram-style: outgoing = blue gradient (#2B5278→#1E3A5F) with white text; incoming = flat panel.
    val outBrush = Brush.linearGradient(listOf(TgOutGradTop, TgOutGradBottom))
    val inColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (mine) Color.White else MaterialTheme.colorScheme.onSurface
    // Telegram bubble (web .msg): 18px radius with a small 4px "tail" corner on the anchored side.
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (mine) 18.dp else 4.dp, bottomEnd = if (mine) 4.dp else 18.dp,
    )

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine && isGroup) {
            if (ui.showAuthor) {
                Avatar(ui.authorName, ui.authorAvatar, size = 32.dp,
                    modifier = Modifier.clickable { onAuthorClick(msg.uid) })
            } else {
                Box(Modifier.size(32.dp))
            }
            Box(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .shadow(2.dp, bubbleShape, clip = false)
                    .clip(bubbleShape)
                    .then(if (mine) Modifier.background(outBrush) else Modifier.background(inColor))
                    .combinedClickable(onClick = {}, onLongClick = { menu = true }),
            ) {
                Column(Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
                    if (!mine && isGroup && ui.showAuthor) {
                        // Web .msg-author: single light-blue accent for all sender names.
                        Text(ui.authorName, style = MaterialTheme.typography.labelSmall,
                            color = TgAccent3, fontWeight = FontWeight.Bold)
                    }
                    msg.forwardFrom?.let {
                        Text("Forwarded from $it", style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f), fontStyle = FontStyle.Italic)
                    }
                    msg.replyTo?.let { reply ->
                        Surface(
                            color = textColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        ) {
                            Column(Modifier.padding(6.dp)) {
                                Text(reply.author, style = MaterialTheme.typography.labelSmall,
                                    color = textColor, fontWeight = FontWeight.Bold)
                                Text(reply.text, style = MaterialTheme.typography.bodyMedium,
                                    color = textColor.copy(alpha = 0.8f), maxLines = 1)
                            }
                        }
                    }

                    // Telegram puts the time on the SAME line as short text instead of a separate
                    // row below it — otherwise a one-word message ("тест") renders as a needlessly
                    // tall bubble (text line + empty space + time line). For short single-line text
                    // we keep text + time inline; everything else keeps the time on its own row.
                    val shortInline = !msg.deleted &&
                        msg.type == MsgType.TEXT &&
                        msg.text.isNotBlank() &&
                        !msg.text.contains('\n') &&
                        msg.text.length <= 24 &&
                        previewUrl(msg.text) == null

                    if (shortInline) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            RichMessageText(msg.text, textColor, onImageClick, onOpenUrl)
                            Box(Modifier.width(8.dp))
                            TimeMeta(msg.at_, mine, readByOther, textColor)
                        }
                    } else {
                        if (msg.deleted) {
                            Text("Message deleted", color = textColor.copy(alpha = 0.7f), fontStyle = FontStyle.Italic)
                        } else {
                            BubbleContent(ui, textColor, onImageClick, onOpenUrl, onVote)
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TimeMeta(msg.at_, mine, readByOther, textColor)
                        }
                    }
                }
            }

            ReactionsRow(msg.groupedReactions(), onReact)

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                Row(Modifier.padding(horizontal = 8.dp)) {
                    QUICK_REACTIONS.forEach { emoji ->
                        Text(emoji, modifier = Modifier
                            .clickable { menu = false; onReact(emoji) }
                            .padding(6.dp))
                    }
                }
                DropdownMenuItem(text = { Text("Ответить") }, onClick = { menu = false; onReply() })
                DropdownMenuItem(text = { Text("Переслать") }, onClick = { menu = false; onForward() })
                if (msg.type == MsgType.TEXT && msg.text.isNotBlank()) {
                    DropdownMenuItem(text = { Text("Копировать") }, onClick = { menu = false; onCopy() })
                }
                DropdownMenuItem(
                    text = { Text(if (isPinned) "Открепить" else "Закрепить") },
                    onClick = { menu = false; if (isPinned) onUnpin() else onPin() },
                )
                if (mine && msg.type == MsgType.TEXT) {
                    DropdownMenuItem(text = { Text("Изменить") }, onClick = { menu = false; onEdit() })
                }
                if (mine) {
                    DropdownMenuItem(text = { Text("Удалить") }, onClick = { menu = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun TimeMeta(at: kotlinx.datetime.Instant?, mine: Boolean, readByOther: Boolean, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(formatTime(at), style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.6f))
        if (mine) {
            Box(Modifier.width(4.dp))
            Icon(
                if (readByOther) Icons.Default.DoneAll else Icons.Default.Done,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.8f),
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/** Bounded placeholder for a photo that is loading or failed to load. Without this an
 *  unloaded photo gives the bubble no intrinsic size and it balloons into a big empty box. */
@Composable
private fun PhotoStatusBox(textColor: Color, loading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(textColor.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = textColor.copy(alpha = 0.6f),
                strokeWidth = 2.dp,
                modifier = Modifier.size(28.dp),
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.BrokenImage, contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(28.dp))
                Text("Фото не загрузилось", style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun BubbleContent(
    ui: MessageUi,
    textColor: Color,
    onImageClick: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onVote: (Int) -> Unit,
) {
    val msg = ui.message
    when (msg.type) {
        MsgType.PHOTO -> {
            var revealed by remember { mutableStateOf(!msg.isSpoiler) }
            val url = msg.photoUrl
            if (!url.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { if (!revealed) revealed = true else onImageClick(url) },
                    contentAlignment = Alignment.Center,
                ) {
                    // SubcomposeAsyncImage gives explicit Loading/Error states so an unloaded or
                    // failed photo shows a small bounded placeholder instead of a giant empty bubble.
                    SubcomposeAsyncImage(
                        model = url,
                        contentDescription = msg.caption,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!revealed) Modifier.blur(24.dp) else Modifier),
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Loading -> PhotoStatusBox(textColor, loading = true)
                            is AsyncImagePainter.State.Error -> PhotoStatusBox(textColor, loading = false)
                            else -> SubcomposeAsyncImageContent()
                        }
                    }
                    if (!revealed) {
                        Box(Modifier.align(Alignment.Center)) {
                            Text("👁 Spoiler", color = Color.White)
                        }
                    }
                }
            }
            if (!msg.caption.isNullOrBlank()) {
                Text(msg.caption!!, color = textColor, modifier = Modifier.padding(top = 4.dp))
            }
        }

        MsgType.FILE -> {
            Row(
                Modifier.clickable { msg.fileUrl?.let(onOpenUrl) }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.InsertDriveFile, null, tint = textColor)
                Box(Modifier.width(8.dp))
                Column {
                    Text(msg.fileName ?: "File", color = textColor, fontWeight = FontWeight.Medium, maxLines = 1)
                    msg.fileSize?.let {
                        Text(formatBytes(it), style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.7f))
                    }
                }
            }
        }

        MsgType.STICKER -> {
            val url = msg.stickerUrl
            if (!url.isNullOrBlank()) {
                AsyncImage(model = url, contentDescription = msg.emoji, modifier = Modifier.size(128.dp))
            } else {
                // Built-in emoji stickers carry no image URL — render the glyph large, like Telegram.
                Text(msg.emoji.orEmpty(), fontSize = 80.sp)
            }
        }

        MsgType.POLL -> {
            msg.poll?.let { PollView(it, currentUid = currentUidFromUi(ui), onVote = onVote) }
        }

        else -> {
            if (msg.text.isNotBlank()) RichMessageText(msg.text, textColor, onImageClick, onOpenUrl)
        }
    }
}

@Composable
private fun RichMessageText(
    text: String,
    textColor: Color,
    onImageClick: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    var revealSpoilers by remember { mutableStateOf(false) }
    val linkColor = if (textColor == Color.White) Color(0xFFBFE0FF) else MaterialTheme.colorScheme.primary
    val annotated = parseRich(text, baseColor = textColor, linkColor = linkColor, revealSpoilers = revealSpoilers)
    Text(
        annotated,
        modifier = if (hasSpoiler(text) && !revealSpoilers)
            Modifier.clickable { revealSpoilers = true } else Modifier,
    )
    // Inline image / YouTube preview for the first previewable URL.
    previewUrl(text)?.let { url ->
        val thumb = youtubeThumb(url) ?: url
        Box(
            Modifier.padding(top = 6.dp).widthIn(max = 280.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { if (youtubeThumb(url) != null) onOpenUrl(url) else onImageClick(url) },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(model = thumb, contentDescription = null, modifier = Modifier.fillMaxWidth())
            if (youtubeThumb(url) != null) {
                Surface(color = Color(0xAA000000), shape = RoundedCornerShape(50)) {
                    Text("▶", color = Color.White, fontSize = 26.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
                }
            }
        }
    }
}

// The poll needs the current uid; it travels with the message's read state. We pass it through
// the UI model implicitly: MessageUi.isMine tells us "me", but poll needs my uid for vote state.
// Simplest: PollView receives uid via a CompositionLocal set by ChatScreen.
@Composable
private fun currentUidFromUi(ui: MessageUi): String = LocalCurrentUid.current

@Composable
private fun ReactionsRow(reactions: Map<String, List<String>>, onReact: (String) -> Unit) {
    if (reactions.isEmpty()) return
    Row(Modifier.padding(top = 2.dp).wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        reactions.filterValues { it.isNotEmpty() }.forEach { (emoji, uids) ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onReact(emoji) }) {
                Text("$emoji ${uids.size}", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
