package com.nonsense.chat.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.GenericShape
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.nonsense.chat.model.Message
import com.nonsense.chat.model.MsgType
import com.nonsense.chat.ui.common.Avatar
import com.nonsense.chat.ui.common.formatTime
import com.nonsense.chat.ui.theme.LocalChatColors

private val QUICK_REACTIONS = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

/**
 * Telegram-style bubble outline: a rounded rectangle with a small "tail" flicked out on the
 * bottom-anchored corner of the last message in a group. Inner (non-last/non-first) corners on the
 * anchored side use a tighter radius so a run of messages reads as one cluster. The tail region is
 * reserved on the anchored side for *every* bubble so their bodies stay vertically aligned.
 */
private fun tgBubbleShape(
    mine: Boolean,
    firstInGroup: Boolean,
    lastInGroup: Boolean,
    rPx: Float,
    smallPx: Float,
    tailPx: Float,
): Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    if (mine) {
        val right = w - tailPx                       // body right edge (tail fills [right, w] at bottom)
        val topR = if (firstInGroup) rPx else smallPx
        moveTo(rPx, 0f)
        lineTo(right - topR, 0f)
        arcTo(Rect(right - 2 * topR, 0f, right, 2 * topR), -90f, 90f, false)   // top-right
        if (lastInGroup) {
            lineTo(right, h - rPx)
            quadraticTo(right, h, w, h)        // flare out to the tail tip
            quadraticTo(right, h, right - rPx, h)
        } else {
            lineTo(right, h - smallPx)
            arcTo(Rect(right - 2 * smallPx, h - 2 * smallPx, right, h), 0f, 90f, false) // bottom-right
        }
        lineTo(rPx, h)
        arcTo(Rect(0f, h - 2 * rPx, 2 * rPx, h), 90f, 90f, false)              // bottom-left
        lineTo(0f, rPx)
        arcTo(Rect(0f, 0f, 2 * rPx, 2 * rPx), 180f, 90f, false)               // top-left
        close()
    } else {
        val left = tailPx                            // body left edge (tail fills [0, left] at bottom)
        val topL = if (firstInGroup) rPx else smallPx
        moveTo(left + topL, 0f)
        lineTo(w - rPx, 0f)
        arcTo(Rect(w - 2 * rPx, 0f, w, 2 * rPx), -90f, 90f, false)            // top-right
        lineTo(w, h - rPx)
        arcTo(Rect(w - 2 * rPx, h - 2 * rPx, w, h), 0f, 90f, false)           // bottom-right
        if (lastInGroup) {
            lineTo(left + rPx, h)
            quadraticTo(left, h, 0f, h)        // flare out to the tail tip
            quadraticTo(left, h, left, h - rPx)
        } else {
            lineTo(left + smallPx, h)
            arcTo(Rect(left, h - 2 * smallPx, left + 2 * smallPx, h), 90f, 90f, false) // bottom-left
        }
        lineTo(left, topL)
        arcTo(Rect(left, 0f, left + 2 * topL, 2 * topL), 180f, 90f, false)    // top-left
        close()
    }
}

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
    onOpenPack: (String) -> Unit,
) {
    val msg = ui.message

    if (msg.isSystem || msg.type == MsgType.CALL) {
        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f), shape = RoundedCornerShape(50)) {
                Text(
                    msg.text.ifBlank { if (msg.type == MsgType.CALL) "Звонок" else "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
        return
    }

    // Telegram-style stickers render WITHOUT a bubble — just the image/glyph, tap opens the pack.
    if (msg.type == MsgType.STICKER && !msg.deleted) {
        StickerMessageRow(
            ui = ui, isGroup = isGroup, isPinned = isPinned, readByOther = readByOther,
            onReact = onReact, onOpenPack = onOpenPack, onReply = onReply, onForward = onForward,
            onPin = onPin, onUnpin = onUnpin, onDelete = onDelete, onAuthorClick = onAuthorClick,
        )
        return
    }

    var menu by remember { mutableStateOf(false) }
    val mine = ui.isMine
    val chat = LocalChatColors.current
    // Telegram-style: outgoing = accent gradient with white text; incoming = flat panel.
    val outBrush = Brush.linearGradient(chat.outBubble)
    val inColor = chat.inBubble
    val textColor = if (mine) chat.outText else chat.inText
    val replyAccent = if (mine) chat.outText else chat.accentLight

    val density = LocalDensity.current
    val rPx = with(density) { 18.dp.toPx() }
    val smallPx = with(density) { 6.dp.toPx() }
    val tailPx = with(density) { 6.dp.toPx() }
    val bubbleShape = remember(mine, ui.firstInGroup, ui.lastInGroup, rPx) {
        tgBubbleShape(mine, ui.firstInGroup, ui.lastInGroup, rPx, smallPx, tailPx)
    }
    // Reserve the tail inset on the anchored side and keep a 12dp inner gutter from the body edge.
    val contentPad = if (mine) PaddingValues(start = 12.dp, top = 7.dp, end = 18.dp, bottom = 6.dp)
    else PaddingValues(start = 18.dp, top = 7.dp, end = 12.dp, bottom = 6.dp)

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine && isGroup) {
            // Telegram puts the avatar next to the bottom-most message of a sender's group.
            if (ui.lastInGroup) {
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
                    .shadow(1.5.dp, bubbleShape, clip = false)
                    .clip(bubbleShape)
                    .then(if (mine) Modifier.background(outBrush) else Modifier.background(inColor))
                    .combinedClickable(onClick = {}, onLongClick = { menu = true }),
            ) {
                Column(Modifier.padding(contentPad)) {
                    if (!mine && isGroup && ui.showAuthor) {
                        // Web .msg-author: single light-accent for all sender names.
                        Text(ui.authorName, style = MaterialTheme.typography.labelSmall,
                            color = chat.accentLight, fontWeight = FontWeight.Bold)
                    }
                    msg.forwardFrom?.let {
                        Text("Переслано от $it", style = MaterialTheme.typography.labelSmall,
                            color = replyAccent.copy(alpha = 0.85f), fontStyle = FontStyle.Italic)
                    }
                    msg.replyTo?.let { reply ->
                        // Web .reply-in-msg: left accent bar + faint accent-tinted background.
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(replyAccent.copy(alpha = 0.12f))
                                .height(IntrinsicSize.Min),
                        ) {
                            Box(Modifier.width(3.dp).fillMaxHeight().background(replyAccent))
                            Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(reply.author, style = MaterialTheme.typography.labelSmall,
                                    color = replyAccent, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(reply.text, style = MaterialTheme.typography.bodyMedium,
                                    color = textColor.copy(alpha = 0.75f), maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    // Telegram puts the time on the SAME line as short text instead of a separate
                    // row below it — otherwise a one-word message ("тест") renders as a needlessly
                    // tall bubble. For short single-line text we keep text + time inline.
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
                            TimeMeta(msg.at_, mine, readByOther, textColor, chat.accentLight)
                        }
                    } else {
                        if (msg.deleted) {
                            Text("Сообщение удалено", color = textColor.copy(alpha = 0.7f), fontStyle = FontStyle.Italic)
                        } else {
                            BubbleContent(ui, textColor, onImageClick, onOpenUrl, onVote)
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TimeMeta(msg.at_, mine, readByOther, textColor, chat.accentLight)
                        }
                    }
                }
            }

            ReactionsRow(msg.groupedReactions(), onReact)

            MessageActionsMenu(
                expanded = menu, onDismiss = { menu = false }, mine = mine, msg = msg, isPinned = isPinned,
                onReact = onReact, onReply = onReply, onForward = onForward, onCopy = onCopy,
                onPin = onPin, onUnpin = onUnpin, onEdit = onEdit, onDelete = onDelete,
            )
        }
    }
}

/** Long-press context menu shared by normal bubbles and bubble-less sticker messages. */
@Composable
private fun MessageActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    mine: Boolean,
    msg: Message,
    isPinned: Boolean,
    onReact: (String) -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onCopy: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Row(Modifier.padding(horizontal = 8.dp)) {
            QUICK_REACTIONS.forEach { emoji ->
                Text(emoji, modifier = Modifier
                    .clickable { onDismiss(); onReact(emoji) }
                    .padding(6.dp))
            }
        }
        DropdownMenuItem(text = { Text("Ответить") }, onClick = { onDismiss(); onReply() })
        DropdownMenuItem(text = { Text("Переслать") }, onClick = { onDismiss(); onForward() })
        if (msg.type == MsgType.TEXT && msg.text.isNotBlank()) {
            DropdownMenuItem(text = { Text("Копировать") }, onClick = { onDismiss(); onCopy() })
        }
        DropdownMenuItem(
            text = { Text(if (isPinned) "Открепить" else "Закрепить") },
            onClick = { onDismiss(); if (isPinned) onUnpin() else onPin() },
        )
        if (mine && msg.type == MsgType.TEXT) {
            DropdownMenuItem(text = { Text("Изменить") }, onClick = { onDismiss(); onEdit() })
        }
        if (mine) {
            DropdownMenuItem(text = { Text("Удалить") }, onClick = { onDismiss(); onDelete() })
        }
    }
}

/** Bubble-less sticker message (Telegram style): plain image/glyph, aligned, tap → open pack. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StickerMessageRow(
    ui: MessageUi,
    isGroup: Boolean,
    isPinned: Boolean,
    readByOther: Boolean,
    onReact: (String) -> Unit,
    onOpenPack: (String) -> Unit,
    onReply: () -> Unit,
    onForward: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onDelete: () -> Unit,
    onAuthorClick: (String) -> Unit,
) {
    val msg = ui.message
    val mine = ui.isMine
    val chat = LocalChatColors.current
    var menu by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine && isGroup) {
            if (ui.lastInGroup) {
                Avatar(ui.authorName, ui.authorAvatar, size = 32.dp,
                    modifier = Modifier.clickable { onAuthorClick(msg.uid) })
            } else {
                Box(Modifier.size(32.dp))
            }
            Box(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
            Box(
                Modifier.combinedClickable(
                    onClick = { onOpenPack(msg.packId.orEmpty()) },
                    onLongClick = { menu = true },
                ),
            ) {
                val url = msg.stickerUrl
                if (!url.isNullOrBlank()) {
                    AsyncImage(model = url, contentDescription = msg.emoji, modifier = Modifier.size(128.dp))
                } else {
                    // Built-in emoji stickers carry no image URL — render the glyph large, like Telegram.
                    Text(msg.emoji.orEmpty(), fontSize = 80.sp)
                }
            }
            Row(Modifier.padding(top = 2.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                // onSurfaceVariant (not bubble text colour) so the time reads on the chat background.
                TimeMeta(msg.at_, mine, readByOther, MaterialTheme.colorScheme.onSurfaceVariant, chat.accentLight)
            }
            ReactionsRow(msg.groupedReactions(), onReact)
            MessageActionsMenu(
                expanded = menu, onDismiss = { menu = false }, mine = mine, msg = msg, isPinned = isPinned,
                onReact = onReact, onReply = onReply, onForward = onForward, onCopy = {},
                onPin = onPin, onUnpin = onUnpin, onEdit = {}, onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun TimeMeta(
    at: kotlinx.datetime.Instant?,
    mine: Boolean,
    readByOther: Boolean,
    textColor: Color,
    readColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(formatTime(at), style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.65f))
        if (mine) {
            Box(Modifier.width(4.dp))
            Icon(
                if (readByOther) Icons.Default.DoneAll else Icons.Default.Done,
                contentDescription = null,
                tint = if (readByOther) readColor else textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(15.dp),
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
            // Load the small preview in the bubble (≈30KB, survives a throttled network); the
            // full-size original is fetched only when the user taps to open it. Old messages have no
            // thumb → fall back to the full URL.
            val preview = msg.photoThumbUrl?.ifBlank { null } ?: url
            if (!url.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (!revealed) revealed = true else onImageClick(url) },
                    contentAlignment = Alignment.Center,
                ) {
                    // SubcomposeAsyncImage gives explicit Loading/Error states so an unloaded or
                    // failed photo shows a small bounded placeholder instead of a giant empty bubble.
                    SubcomposeAsyncImage(
                        model = preview,
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
                            Text("👁 Спойлер", color = Color.White)
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
                    Text(msg.fileName ?: "Файл", color = textColor, fontWeight = FontWeight.Medium, maxLines = 1)
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
// the UI model implicitly via a CompositionLocal set by ChatScreen.
@Composable
private fun currentUidFromUi(ui: MessageUi): String = LocalCurrentUid.current

@Composable
private fun ReactionsRow(reactions: Map<String, List<String>>, onReact: (String) -> Unit) {
    if (reactions.isEmpty()) return
    val me = LocalCurrentUid.current
    val accent = MaterialTheme.colorScheme.primary
    Row(Modifier.padding(top = 3.dp).wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        reactions.filterValues { it.isNotEmpty() }.forEach { (emoji, uids) ->
            val reacted = me in uids
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = if (reacted) 0.28f else 0.14f))
                    .border(1.dp, accent.copy(alpha = if (reacted) 0.9f else 0.3f), RoundedCornerShape(12.dp))
                    .clickable { onReact(emoji) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text("$emoji ${uids.size}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
