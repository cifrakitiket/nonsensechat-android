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

/**
 * Emoji set for the "Эмодзи" tab of the panel — a real emoji keyboard whose taps are INSERTED into
 * the text input (unlike [BuiltInStickers], which are sent as big standalone emoji stickers). Grouped
 * by category so the grid reads naturally; the picker flattens it for display.
 */
val EmojiKeyboard: List<Pair<String, List<String>>> = listOf(
    "Смайлы" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "😊",
        "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙",
        "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎",
        "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁",
        "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠",
        "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥",
        "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬",
        "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪",
        "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑",
        "🤠", "😈", "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️",
    ),
    "Жесты" to listOf(
        "👍", "👎", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤘", "🤙",
        "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋",
        "🤝", "🙏", "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲",
        "💪", "🦾", "✍️", "💅", "🤳", "❤️", "🧠",
    ),
    "Сердца" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
        "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "❤️‍🔥",
    ),
    "Животные" to listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
        "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🐤", "🦄",
        "🐝", "🦋", "🐢", "🐙", "🦀", "🐳", "🐬", "🐟", "🦖", "🐉",
    ),
    "Еда" to listOf(
        "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
        "🍒", "🍑", "🥭", "🍍", "🥝", "🍅", "🥑", "🍆", "🌽", "🌶️",
        "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🍝", "🍣", "🍰", "🍩",
        "🍪", "🍫", "🍬", "🍭", "🍦", "☕", "🍵", "🍺", "🍻", "🥂",
    ),
    "Символы" to listOf(
        "🔥", "✨", "🌟", "⭐", "💫", "⚡", "💥", "💯", "✅", "❌",
        "❓", "❗", "💤", "💢", "💦", "🎉", "🎊", "🎁", "🏆", "🎈",
        "👀", "👑", "💎", "🔔", "🎵", "🎶", "💡", "🚀", "🌈", "☀️",
    ),
)

/** Flat list of all keyboard emoji, in category order. */
val EmojiKeyboardFlat: List<String> = EmojiKeyboard.flatMap { it.second }
