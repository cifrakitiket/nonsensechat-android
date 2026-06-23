package com.nonsense.chat.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** The four themes ported 1:1 from the web app. */
enum class AppTheme { DARK, LIGHT, COFFEE, GRADIENT }

/**
 * Chat / brand colour tokens that don't fit Material's [androidx.compose.material3.ColorScheme]:
 * the message-bubble gradients, the chat wallpaper, the brand accent gradient (web `--grad`) and
 * the lighter accent (web `--acc3`). Exposed via [LocalChatColors] so any screen can paint the
 * Telegram-style wallpaper, bubbles, gradient pills/badges and avatars consistently per theme.
 */
data class ChatColors(
    val wallpaper: List<Color>,         // vertical gradient stops for the chat background
    val inBubble: Color,                // incoming message bubble
    val outBubble: List<Color>,         // outgoing bubble gradient (top→bottom)
    val outTail: Color,                 // tail colour for outgoing bubbles (web --out-tail)
    val outText: Color,
    val inText: Color,
    val bubbleMeta: Color,              // timestamp / check colour on bubbles
    val gradient: List<Color>,          // brand accent gradient (web --grad): buttons / badges / tabs
    val accentLight: Color,             // web --acc3: author names / active tab / read checks
    val light: Boolean,                 // true → dark text/icons on bubbles & status bar
)

val LocalChatColors = staticCompositionLocalOf {
    ChatColors(
        wallpaper = listOf(TgChatWallTop, TgChatWallBottom),
        inBubble = TgInBubble,
        outBubble = listOf(TgOutGradTop, TgOutGradBottom),
        outTail = TgOutTail,
        outText = Color.White,
        inText = TgText,
        bubbleMeta = TgTextDim,
        gradient = listOf(TgAccent, TgGradEnd),
        accentLight = TgAccent3,
        light = false,
    )
}

/** Vertical wallpaper brush for the current theme. */
@Composable
fun chatWallpaperBrush(): Brush = Brush.verticalGradient(LocalChatColors.current.wallpaper)

private val DarkScheme = darkColorScheme(
    primary = TgAccent,
    onPrimary = Color.White,
    primaryContainer = TgOutGradTop,
    onPrimaryContainer = Color.White,
    secondary = TgAccent3,
    onSecondary = Color.White,
    background = TgBg,
    onBackground = TgText,
    surface = TgSide,
    onSurface = TgText,
    surfaceVariant = TgInBubble,
    onSurfaceVariant = TgTextDim,
    outline = TgBorder,
    error = DangerRed,
)

private val DarkChat = ChatColors(
    wallpaper = listOf(TgChatWallTop, TgChatWallBottom),
    inBubble = TgInBubble,
    outBubble = listOf(TgOutGradTop, TgOutGradBottom),
    outTail = TgOutTail,
    outText = Color.White,
    inText = TgText,
    bubbleMeta = Color(0xCCCFE3F5),
    gradient = listOf(TgAccent, TgGradEnd),
    accentLight = TgAccent3,
    light = false,
)

private val LightScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightOutTop,
    onPrimaryContainer = Color.White,
    secondary = LightAccent3,
    background = LightBg,
    onBackground = LightText,
    surface = LightSide,
    onSurface = LightText,
    surfaceVariant = LightCard2,
    onSurfaceVariant = LightTextDim,
    outline = LightBorder,
    error = DangerRed,
)

private val LightChat = ChatColors(
    wallpaper = listOf(LightWallTop, LightWallBottom),
    inBubble = LightInBubble,
    outBubble = listOf(LightOutTop, LightOutBottom),
    outTail = LightOutTail,
    outText = Color.White,
    inText = LightText,
    bubbleMeta = Color(0xE6FFF1E6),
    gradient = listOf(LightAccent3, LightAccent),
    accentLight = LightAccent,
    light = true,
)

private val CoffeeScheme = darkColorScheme(
    primary = CoffeeAccent,
    onPrimary = Color(0xFF1A0E06),
    primaryContainer = CoffeeOutTop,
    secondary = CoffeeAccent3,
    background = CoffeeBg,
    onBackground = CoffeeText,
    surface = CoffeeSide,
    onSurface = CoffeeText,
    surfaceVariant = CoffeeInBubble,
    onSurfaceVariant = CoffeeTextDim,
    outline = CoffeeBorder,
    error = DangerRed,
)

private val CoffeeChat = ChatColors(
    wallpaper = listOf(CoffeeWallTop, CoffeeWallBottom),
    inBubble = CoffeeInBubble,
    outBubble = listOf(CoffeeOutTop, CoffeeOutBottom),
    outTail = CoffeeOutTail,
    outText = Color(0xFFF7ECE0),
    inText = CoffeeText,
    bubbleMeta = Color(0xCCE9D6C2),
    gradient = listOf(CoffeeAccent, CoffeeGradEnd),
    accentLight = CoffeeAccent3,
    light = false,
)

private val GradientScheme = darkColorScheme(
    primary = GradAccent,
    onPrimary = Color.White,
    primaryContainer = GradOutTop,
    onPrimaryContainer = Color.White,
    secondary = GradAccent3,
    background = GradBg,
    onBackground = GradText,
    surface = GradSide,
    onSurface = GradText,
    surfaceVariant = GradInBubble,
    onSurfaceVariant = GradTextDim,
    outline = GradBorder,
    error = DangerRed,
)

private val GradientChat = ChatColors(
    wallpaper = listOf(GradWallTop, GradWallBottom),
    inBubble = GradInBubble,
    outBubble = listOf(GradOutTop, GradOutBottom),
    outTail = GradOutTail,
    outText = Color.White,
    inText = GradText,
    bubbleMeta = Color(0xCCDDD2FF),
    gradient = listOf(GradAccent, GradGradEnd),
    accentLight = GradAccent3,
    light = false,
)

@Composable
fun NonsenseTheme(
    theme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        AppTheme.DARK -> DarkScheme
        AppTheme.LIGHT -> LightScheme
        AppTheme.COFFEE -> CoffeeScheme
        AppTheme.GRADIENT -> GradientScheme
    }

    val chatColors = when (theme) {
        AppTheme.DARK -> DarkChat
        AppTheme.LIGHT -> LightChat
        AppTheme.COFFEE -> CoffeeChat
        AppTheme.GRADIENT -> GradientChat
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = chatColors.light
        }
    }

    CompositionLocalProvider(LocalChatColors provides chatColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
