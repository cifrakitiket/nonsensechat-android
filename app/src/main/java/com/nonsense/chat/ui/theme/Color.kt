package com.nonsense.chat.ui.theme

import androidx.compose.ui.graphics.Color

// ── Palettes ported verbatim from public/index.html CSS variables ──────────────
// The web client ships four themes (dark / light / coffee-dark / gradient). Each block below
// mirrors the `--bg / --side / --card / --text / --text2 / --acc / --acc2 / --acc3 / --grad /
// --msg-in-bg / --msg-out-bg / --out-tail / --border` tokens so the app matches the web 1:1.

// ── Dark (default, web `.theme-dark`) ──────────────────────────────────────────
val TgBg = Color(0xFF0E1621)            // --bg: chat area background
val TgBg2 = Color(0xFF111B26)           // --bg2
val TgSide = Color(0xFF17212B)          // --side: top bar, composer, panels
val TgCard = Color(0xFF182533)          // --card
val TgCard2 = Color(0xFF213044)         // --card2
val TgText = Color(0xFFE8F0F9)          // --text
val TgTextDim = Color(0xFF7F93A5)       // --text2: muted captions / timestamps
val TgBorder = Color(0x244C6A85)        // --border
val TgInBubble = Color(0xFF182533)      // --msg-in-bg
val TgOutGradTop = Color(0xFF2AABEE)    // --msg-out-bg start
val TgOutGradBottom = Color(0xFF229ED9) // --msg-out-bg end
val TgOutTail = Color(0xFF229ED9)       // --out-tail
val TgAccent = Color(0xFF2AABEE)        // --acc: Telegram blue
val TgAccent2 = Color(0xFF229ED9)       // --acc2
val TgAccent3 = Color(0xFF64C9FF)       // --acc3
val TgGradEnd = Color(0xFF229ED9)       // --grad end
val TgChatWallTop = Color(0xFF0E1621)   // chat wallpaper gradient
val TgChatWallBottom = Color(0xFF0A121B)

// ── Light (web `.theme-light`, warm beige + brown bubbles) ─────────────────────
val LightBg = Color(0xFFF5F0EB)         // --bg
val LightBg2 = Color(0xFFEDE7DF)        // --bg2
val LightSide = Color(0xFFE8DFD5)       // --side
val LightCard = Color(0xFFFFF8F2)       // --card
val LightCard2 = Color(0xFFF0E8DE)      // --card2
val LightText = Color(0xFF2C1F14)       // --text
val LightTextDim = Color(0xFF8A7060)    // --text2
val LightBorder = Color(0x2E8B643C)     // --border: rgba(139,100,60,0.18)
val LightInBubble = Color(0xFFFFF8F2)   // --msg-in-bg
val LightOutTop = Color(0xFFC8956A)     // --msg-out-bg start
val LightOutBottom = Color(0xFFA0724A)  // --msg-out-bg end
val LightOutTail = Color(0xFFA0724A)    // --out-tail
val LightAccent = Color(0xFF9B6B3A)     // --acc
val LightAccent2 = Color(0xFF7A5530)    // --acc2
val LightAccent3 = Color(0xFFC8956A)    // --acc3
val LightGradEnd = Color(0xFF9B6B3A)    // --grad end (135° --acc3 → --acc)
val LightWallTop = Color(0xFFEDE7DF)
val LightWallBottom = Color(0xFFF5F0EB)

// ── Coffee Dark (web `.theme-coffee-dark`) ─────────────────────────────────────
val CoffeeBg = Color(0xFF1A0E06)        // --bg
val CoffeeBg2 = Color(0xFF221308)       // --bg2
val CoffeeSide = Color(0xFF2A1810)      // --side
val CoffeeCard = Color(0xFF321E12)      // --card
val CoffeeCard2 = Color(0xFF3C2518)     // --card2
val CoffeeText = Color(0xFFF0E0CC)      // --text
val CoffeeTextDim = Color(0xFFA08060)   // --text2
val CoffeeBorder = Color(0x2EA08050)    // --border: rgba(160,128,80,0.18)
val CoffeeInBubble = Color(0xFF321E12)  // --msg-in-bg
val CoffeeOutTop = Color(0xFF5C3820)    // --msg-out-bg start
val CoffeeOutBottom = Color(0xFF3C2010) // --msg-out-bg end
val CoffeeOutTail = Color(0xFF3C2010)   // --out-tail
val CoffeeAccent = Color(0xFFC8906A)    // --acc
val CoffeeAccent2 = Color(0xFFA07050)   // --acc2
val CoffeeAccent3 = Color(0xFFE0B090)   // --acc3
val CoffeeGradEnd = Color(0xFF7A5040)   // --grad end (135° --acc → this)
val CoffeeWallTop = Color(0xFF221308)
val CoffeeWallBottom = Color(0xFF150B05)

// ── Gradient (web `.theme-gradient`, vivid purple) ─────────────────────────────
val GradBg = Color(0xFF0D0D1F)          // --bg
val GradBg2 = Color(0xFF13122A)         // --bg2
val GradSide = Color(0xFF17163A)        // --side
val GradCard = Color(0xFF1E1C42)        // --card
val GradCard2 = Color(0xFF242250)       // --card2
val GradText = Color(0xFFE8E4FF)        // --text
val GradTextDim = Color(0xFF8882CC)     // --text2
val GradBorder = Color(0x2E7864C8)      // --border: rgba(120,100,200,0.18)
val GradInBubble = Color(0xFF1E1C42)    // --msg-in-bg
val GradOutTop = Color(0xFF4A3898)      // --msg-out-bg start
val GradOutBottom = Color(0xFF2D1F6E)   // --msg-out-bg end
val GradOutTail = Color(0xFF2D1F6E)     // --out-tail
val GradAccent = Color(0xFF9B77EE)      // --acc
val GradAccent2 = Color(0xFF7A56CC)     // --acc2
val GradAccent3 = Color(0xFFC8B0FF)     // --acc3
val GradGradEnd = Color(0xFFEE77BB)     // --grad end (135° --acc → this)
val GradWallTop = Color(0xFF13122A)
val GradWallBottom = Color(0xFF0A0A18)

// ── Shared semantic accents (used across all themes) ───────────────────────────
val OnlineGreen = Color(0xFF4CAF50)     // --online
val UnreadBadge = TgAccent
val VerifiedBlue = Color(0xFF3EA6FF)
val DevPurple = Color(0xFF8B6FC4)
val DangerRed = Color(0xFFE53935)       // --red

// Back-compat aliases (still referenced by Notifications/MessageBubble fallbacks).
val BrandBlue = TgAccent
val BrandBg = TgBg
val OutBubbleDark = TgOutGradTop
val InBubbleDark = TgInBubble
