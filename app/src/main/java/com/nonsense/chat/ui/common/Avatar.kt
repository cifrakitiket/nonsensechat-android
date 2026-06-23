package com.nonsense.chat.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.absoluteValue

// Telegram-style two-tone letter-avatar gradients (135°, lighter → darker). One per hashed name,
// so different people get distinct, vivid avatars while keeping the original web app's gradient feel.
private val AvatarGradients = listOf(
    listOf(Color(0xFFFF8A80), Color(0xFFE5484D)), // red
    listOf(Color(0xFFFFB74D), Color(0xFFF57C00)), // orange
    listOf(Color(0xFFB388FF), Color(0xFF7C4DFF)), // violet
    listOf(Color(0xFF80D8A0), Color(0xFF43A047)), // green
    listOf(Color(0xFF6FD3E8), Color(0xFF0097A7)), // cyan
    listOf(Color(0xFF74A8E8), Color(0xFF3D6FB4)), // blue
    listOf(Color(0xFFF48FB1), Color(0xFFD81B60)), // pink
    listOf(Color(0xFF9FA8DA), Color(0xFF5C6BC0)), // indigo
)

fun gradientForKey(key: String): List<Color> =
    AvatarGradients[(key.hashCode().absoluteValue) % AvatarGradients.size]

/** Back-compat: the dominant (darker) colour of a name's avatar gradient. */
fun colorForKey(key: String): Color = gradientForKey(key).last()

fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

/**
 * Circular avatar: shows the image if [url] is set, otherwise a two-tone gradient with white
 * initials. Pass [ringColor] for the accent ring used on the chat header / open profile.
 */
@Composable
fun Avatar(
    name: String,
    url: String?,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    modifier: Modifier = Modifier,
    ringColor: Color? = null,
) {
    val shape = CircleShape
    val ring = if (ringColor != null) Modifier.border(2.dp, ringColor, shape) else Modifier
    // Only treat real http(s) URLs as images — some docs hold placeholder junk like "⏳ Загрузка…"
    // in the avatar field, which must fall back to initials instead of a broken image.
    val isRealUrl = url != null && (url.startsWith("http://") || url.startsWith("https://"))
    if (isRealUrl) {
        // Decode at the avatar's actual pixel size, not the source resolution — a pasted 4000px
        // external avatar then costs ~size² px of memory instead of megabytes for a 48dp circle.
        val px = with(LocalDensity.current) { size.roundToPx() }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(url).size(px).crossfade(true).build(),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(shape).then(ring),
        )
    } else {
        val gradient = Brush.linearGradient(
            colors = gradientForKey(name),
            start = Offset.Zero,
            end = Offset.Infinite, // 135°-ish diagonal
        )
        Box(
            modifier = modifier.size(size).clip(shape).background(gradient).then(ring),
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
