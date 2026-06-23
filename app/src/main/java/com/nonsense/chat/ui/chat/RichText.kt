package com.nonsense.chat.ui.chat

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

/**
 * Lightweight markdown-style formatter mirroring the web client's inline syntax:
 *   **bold**  *italic*  __underline__  ~~strike~~  `code`  ||spoiler||  and bare URLs.
 * Links open via the platform UriHandler. Spoiler runs are masked with █ until [revealSpoilers].
 */
private val URL_REGEX = Regex("""https?://[^\s<>()]+""")

// Ordered so longer fences (**, __, ~~, ||) are tried before single-char ones (*).
// NOTE: `apply` is the LAST parameter so the trailing-lambda syntax `Fence("**") { ... }` works.
private data class Fence(
    val token: String,
    val spoiler: Boolean = false,
    val apply: (SpanStyle) -> SpanStyle,
)

private val FENCES = listOf(
    Fence("**") { it.copy(fontWeight = FontWeight.Bold) },
    Fence("__") { it.copy(textDecoration = TextDecoration.Underline) },
    Fence("~~") { it.copy(textDecoration = TextDecoration.LineThrough) },
    Fence("||", spoiler = true) { it },
    Fence("*") { it.copy(fontStyle = FontStyle.Italic) },
    Fence("`") { it.copy(fontFamily = FontFamily.Monospace) },
)

fun parseRich(
    text: String,
    baseColor: Color,
    linkColor: Color,
    revealSpoilers: Boolean,
): AnnotatedString = buildAnnotatedString {
    var i = 0
    val n = text.length
    // Tracks active styles by nesting; we render a flat scan (no true nesting) which covers the
    // common cases (one fence at a time) just like the web regex pipeline.
    fun appendStyled(segment: String) {
        var j = 0
        while (j < segment.length) {
            val m = URL_REGEX.find(segment, j)
            if (m == null) { append(segment.substring(j)); break }
            if (m.range.first > j) append(segment.substring(j, m.range.first))
            withLink(
                LinkAnnotation.Url(
                    m.value,
                    TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                ),
            ) { append(m.value) }
            j = m.range.last + 1
        }
    }

    while (i < n) {
        val fence = FENCES.firstOrNull { text.startsWith(it.token, i) }
        if (fence != null) {
            val close = text.indexOf(fence.token, i + fence.token.length)
            if (close > 0) {
                val inner = text.substring(i + fence.token.length, close)
                if (fence.spoiler) {
                    if (revealSpoilers) {
                        withStyle(SpanStyle(color = baseColor)) { appendStyled(inner) }
                    } else {
                        withStyle(SpanStyle(color = baseColor.copy(alpha = 0.18f), background = baseColor.copy(alpha = 0.18f))) {
                            append("█".repeat(inner.length.coerceIn(1, 40)))
                        }
                    }
                } else {
                    withStyle(fence.apply(SpanStyle(color = baseColor))) { appendStyled(inner) }
                }
                i = close + fence.token.length
                continue
            }
        }
        // No fence here: append a single char (or run up to the next fence/url).
        val next = (i + 1).coerceAtMost(n)
        withStyle(SpanStyle(color = baseColor)) { appendStyled(text.substring(i, next)) }
        i = next
    }
}

fun hasSpoiler(text: String): Boolean = text.contains("||")

/** First URL that can be shown as an inline preview (image or YouTube), or null. */
fun previewUrl(text: String): String? = URL_REGEX.find(text)?.value?.takeIf {
    it.contains("youtube.com/watch") || it.contains("youtu.be/") ||
        it.endsWith(".jpg", true) || it.endsWith(".jpeg", true) ||
        it.endsWith(".png", true) || it.endsWith(".gif", true) || it.endsWith(".webp", true)
}

/** YouTube thumbnail URL for a watch/short link, or null if not YouTube. */
fun youtubeThumb(url: String): String? {
    val id = when {
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
        url.contains("watch?v=") -> url.substringAfter("watch?v=").substringBefore("&")
        else -> null
    } ?: return null
    return "https://img.youtube.com/vi/$id/hqdefault.jpg"
}
