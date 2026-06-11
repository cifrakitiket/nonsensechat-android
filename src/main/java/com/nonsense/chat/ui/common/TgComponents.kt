package com.nonsense.chat.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nonsense.chat.ui.theme.TgAccent
import com.nonsense.chat.ui.theme.TgAccent2

/** Telegram send-button gradient (web `--grad`: 135° #5B8FB9 → #8B6FC4). */
fun tgGradient(): Brush = Brush.linearGradient(listOf(TgAccent, TgAccent2))

/**
 * Telegram-style pill button filled with the accent gradient. Use for prominent primary actions
 * (sign in, "Написать", create). Keeps logic in the caller — just pass [onClick].
 */
@Composable
fun TgGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .clip(CircleShape)
            .background(tgGradient())
            .clickable(enabled = enabled, onClick = onClick)
            .heightIn(min = 50.dp)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}
