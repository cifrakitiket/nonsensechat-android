package com.nonsense.chat.ui.chat

/**
 * A built-in pack of emoji "stickers" so the picker is never empty even when the server has no
 * sticker_packs rows.  These carry no image URL — they're sent as STICKER messages with only an
 * emoji, and rendered as a large glyph (see MessageBubble's STICKER branch and the picker grid).
 */
const val BUILTIN_STICKER_PACK_ID = "builtin"

val BuiltInStickers: List<PickerSticker> = listOf(
    "😀", "😂", "🥹", "😍", "😎", "🤩", "🥳", "😅", "😭", "😡",
    "👍", "👎", "🙏", "👏", "🤝", "💪", "🔥", "❤️", "💔", "💯",
    "🎉", "✨", "🌟", "⚡", "💀", "👀", "🤔", "😉", "😜", "🤯",
    "🥰", "😱", "🤗", "😴", "🤤", "🤡", "👻", "🎁", "☕", "🍕",
).map { PickerSticker(url = "", emoji = it, packId = BUILTIN_STICKER_PACK_ID) }
