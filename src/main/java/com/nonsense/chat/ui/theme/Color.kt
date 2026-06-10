package com.nonsense.chat.ui.theme

import androidx.compose.ui.graphics.Color

// ── Original web palette (Telegram-style dark) — the canonical look ────────────
// Pulled verbatim from public/index.html CSS variables so the app matches the web client.
val TgBg = Color(0xFF0F1923)            // --bg: chat area background
val TgSide = Color(0xFF17212B)          // --side: top bar, composer, panels
val TgText = Color(0xFFE8F0F9)          // --text
val TgTextDim = Color(0xFF8FA6BC)       // muted captions / timestamps
val TgBorder = Color(0x265B8FB9)        // --border: rgba(88,139,179,0.15)
val TgInBubble = Color(0xFF1E2C3A)      // --msg-in-bg
val TgOutGradTop = Color(0xFF2B5278)    // --msg-out-bg start
val TgOutGradBottom = Color(0xFF1E3A5F) // --msg-out-bg end
val TgAccent = Color(0xFF5B8FB9)        // --grad start (send button / accents)
val TgAccent2 = Color(0xFF8B6FC4)       // --grad end
val TgChatWallTop = Color(0xFF11202E)   // chat wallpaper gradient (subtle, around --bg)
val TgChatWallBottom = Color(0xFF0B131C)

// Shared semantic accents (used across all themes).
val OnlineGreen = Color(0xFF4CAF50)
val UnreadBadge = TgAccent
val VerifiedBlue = Color(0xFF3EA6FF)
val DevPurple = Color(0xFF8B6FC4)
val DangerRed = Color(0xFFE5484D)

// Back-compat aliases (still referenced by Notifications/MessageBubble).
val BrandBlue = TgAccent
val BrandBg = TgBg
val OutBubbleDark = TgOutGradTop
val InBubbleDark = TgInBubble

// ── Coffee Dark theme ─────────────────────────────────────────────────────────
val CoffeeBg = Color(0xFF1C1410)
val CoffeeSurface = Color(0xFF271C16)
val CoffeeInBubble = Color(0xFF2E211A)
val CoffeeOutTop = Color(0xFF6B4A2E)
val CoffeeOutBottom = Color(0xFF4A3220)
val CoffeeAccent = Color(0xFFC8915B)
val CoffeeText = Color(0xFFF1E4D6)
val CoffeeTextDim = Color(0xFFB89B82)
val CoffeeWallTop = Color(0xFF221710)
val CoffeeWallBottom = Color(0xFF150E09)

// ── Light theme ──────────────────────────────────────────────────────────────
val LightBg = Color(0xFFE9EEF3)         // chat area
val LightSide = Color(0xFFFFFFFF)       // panels / bars
val LightText = Color(0xFF14202B)
val LightTextDim = Color(0xFF6B7C8C)
val LightInBubble = Color(0xFFFFFFFF)
val LightOutTop = Color(0xFFCDEBC2)     // Telegram-light outgoing green-ish
val LightOutBottom = Color(0xFFB6E3A8)
val LightBorder = Color(0x1A14202B)
val LightWallTop = Color(0xFFDDE6EE)
val LightWallBottom = Color(0xFFC9D6E3)

// ── Gradient theme (vivid purple/blue wallpaper) ──────────────────────────────
val GradWallTop = Color(0xFF2B1055)
val GradWallMid = Color(0xFF23386E)
val GradWallBottom = Color(0xFF0F2A3F)
