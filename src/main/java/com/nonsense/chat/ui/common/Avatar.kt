package com.nonsense.chat.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.absoluteValue

private val AvatarColors = listOf(
    Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784), Color(0xFFFFB74D),
    Color(0xFFBA68C8), Color(0xFF4DB6AC), Color(0xFFF06292), Color(0xFF7986CB),
)

fun colorForKey(key: String): Color =
    AvatarColors[(key.hashCode().absoluteValue) % AvatarColors.size]

fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/** Circular avatar: shows the image if [url] is set, otherwise coloured initials. */
@Composable
fun Avatar(
    name: String,
    url: String?,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    // Only treat real http(s) URLs as images — some docs hold placeholder junk like "⏳ Загрузка…"
    // in the avatar field, which must fall back to initials instead of a broken image.
    val isRealUrl = url != null && (url.startsWith("http://") || url.startsWith("https://"))
    if (isRealUrl) {
        AsyncImage(
            model = url,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape),
        )
    } else {
        Box(
            modifier = modifier.size(size).clip(shape).background(colorForKey(name)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initialsOf(name),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value / 2.4).sp,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
