package com.nonsense.chat.ui.common

import com.nonsense.chat.data.repos.PresenceRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** HH:mm for a message timestamp. */
fun formatTime(instant: Instant?): String {
    if (instant == null) return ""
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(dt.hour, dt.minute)
}

/** Relative day label for chat-list / message separators. */
fun formatDay(instant: Instant?): String {
    if (instant == null) return ""
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz).date
    val date = instant.toLocalDateTime(tz).date
    val days = now.toEpochDays() - date.toEpochDays()
    return when {
        days == 0 -> "Today"
        days == 1 -> "Yesterday"
        days < 7 -> date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> "%02d.%02d.%d".format(date.dayOfMonth, date.monthNumber, date.year)
    }
}

/** Presence subtitle: "online", "typing…", "last seen 5 min ago", or "offline". */
fun presenceText(lastSeen: Instant?, online: Boolean, hideLastSeen: Boolean, typing: Boolean): String {
    if (typing) return "typing…"
    val now = Clock.System.now()
    val recent = lastSeen != null && now.toEpochMilliseconds() - lastSeen.toEpochMilliseconds() < PresenceRepository.ONLINE_WINDOW_MS
    if (online && recent) return "online"
    if (hideLastSeen || lastSeen == null) return "offline"
    val mins = (now.toEpochMilliseconds() - lastSeen.toEpochMilliseconds()) / 60000
    return when {
        mins < 1 -> "last seen just now"
        mins < 60 -> "last seen $mins min ago"
        mins < 1440 -> "last seen ${mins / 60} h ago"
        else -> "last seen ${formatDay(lastSeen)}"
    }
}
