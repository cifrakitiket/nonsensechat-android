package com.nonsense.chat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** The four themes ported from the web app, plus a Material You (dynamic) option. */
enum class AppTheme { DYNAMIC, DARK, COFFEE, LIGHT, GRADIENT }

/**
 * Chat-specific colour tokens that don't fit Material's [androidx.compose.material3.ColorScheme]:
 * message-bubble gradients and the chat wallpaper. Exposed via [LocalChatColors] so any screen can
 * paint the Telegram-style wallpaper and bubbles consistently per theme.
 */
data class ChatColors(
    val wallpaper: List<Color>,         // vertical gradient stops for the chat background
    val inBubble: Color,                // incoming message bubble
    val outBubble: List<Color>,         // outgoing bubble gradient (top→bottom)
    val outText: Color,
    val inText: Color,
    val bubbleMeta: Color,              // timestamp / check colour on bubbles
    val light: Boolean,                 // true → dark text/icons on bubbles & status bar
)

val LocalChatColors = staticCompositionLocalOf {
    ChatColors(
        wallpaper = listOf(TgChatWallTop, TgChatWallBottom),
        inBubble = TgInBubble,
        outBubble = listOf(TgOutGradTop, TgOutGradBottom),
        outText = Color.White,
        inText = TgText,
        bubbleMeta = TgTextDim,
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
    secondary = TgAccent2,
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
    outText = Color.White,
    inText = TgText,
    bubbleMeta = Color(0xCCCFE3F5),
    light = false,
)

private val CoffeeScheme = darkColorScheme(
    primary = CoffeeAccent,
    onPrimary = Color(0xFF1C1410),
    primaryContainer = CoffeeOutTop,
    secondary = CoffeeAccent,
    background = CoffeeBg,
    onBackground = CoffeeText,
    surface = CoffeeSurface,
    onSurface = CoffeeText,
    surfaceVariant = CoffeeInBubble,
    onSurfaceVariant = CoffeeTextDim,
    outline = TgBorder,
    error = DangerRed,
)

private val CoffeeChat = ChatColors(
    wallpaper = listOf(CoffeeWallTop, CoffeeWallBottom),
    inBubble = CoffeeInBubble,
    outBubble = listOf(CoffeeOutTop, CoffeeOutBottom),
    outText = Color(0xFFF7ECE0),
    inText = CoffeeText,
    bubbleMeta = Color(0xCCE9D6C2),
    light = false,
)

private val LightScheme = lightColorScheme(
    primary = TgAccent,
    onPrimary = Color.White,
    primaryContainer = LightOutTop,
    onPrimaryContainer = LightText,
    secondary = TgAccent2,
    background = LightBg,
    onBackground = LightText,
    surface = LightSide,
    onSurface = LightText,
    surfaceVariant = Color(0xFFDDE6EE),
    onSurfaceVariant = LightTextDim,
    outline = LightBorder,
    error = DangerRed,
)

private val LightChat = ChatColors(
    wallpaper = listOf(LightWallTop, LightWallBottom),
    inBubble = LightInBubble,
    outBubble = listOf(LightOutTop, LightOutBottom),
    outText = Color(0xFF0B2912),
    inText = LightText,
    bubbleMeta = Color(0x99355A3E),
    light = true,
)

// "Gradient" theme reuses the dark scheme; only the wallpaper differs (vivid purple/blue).
private val GradientChat = DarkChat.copy(
    wallpaper = listOf(GradWallTop, GradWallMid, GradWallBottom),
)

@Composable
fun NonsenseTheme(
    theme: AppTheme = AppTheme.DYNAMIC,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme = when (theme) {
        AppTheme.DYNAMIC ->
            if (supportsDynamic) {
                if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else if (systemDark) DarkScheme else LightScheme
        AppTheme.DARK -> DarkScheme
        AppTheme.COFFEE -> CoffeeScheme
        AppTheme.LIGHT -> LightScheme
        AppTheme.GRADIENT -> DarkScheme
    }

    val chatColors = when (theme) {
        AppTheme.DYNAMIC -> if (systemDark || !supportsDynamic) DarkChat else LightChat
        AppTheme.DARK -> DarkChat
        AppTheme.COFFEE -> CoffeeChat
        AppTheme.LIGHT -> LightChat
        AppTheme.GRADIENT -> GradientChat
    }

    val lightStatusBar = theme == AppTheme.LIGHT ||
        (theme == AppTheme.DYNAMIC && supportsDynamic && !systemDark)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightStatusBar
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
