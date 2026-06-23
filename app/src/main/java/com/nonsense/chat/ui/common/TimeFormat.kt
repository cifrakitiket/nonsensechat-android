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

private val RU_WEEKDAYS = listOf(
    "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье",
)

/** Relative day label for chat-list / message separators. */
fun formatDay(instant: Instant?): String {
    if (instant == null) return ""
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz).date
    val date = instant.toLocalDateTime(tz).date
    val days = now.toEpochDays() - date.toEpochDays()
    return when {
        days == 0 -> "Сегодня"
        days == 1 -> "Вчера"
        days < 7 -> RU_WEEKDAYS[date.dayOfWeek.ordinal]
        else -> "%02d.%02d.%d".format(date.dayOfMonth, date.monthNumber, date.year)
    }
}

/**
 * Single source of truth for "is this user online right now". A stale `online` flag isn't enough:
 * the flag is only cleared on explicit sign-out, so a force-killed app would otherwise read as online
 * forever. Require the flag AND a recent heartbeat (lastSeen within the window). hideLastSeen users
 * never count as online. Used by both the green dot and the presence subtitle so they always agree.
 */
fun isOnline(lastSeen: Instant?, online: Boolean, hideLastSeen: Boolean): Boolean {
    if (hideLastSeen || !online || lastSeen == null) return false
    return Clock.System.now().toEpochMilliseconds() - lastSeen.toEpochMilliseconds() < PresenceRepository.ONLINE_WINDOW_MS
}

/** Presence subtitle: "в сети", "печатает…", "был(а) 5 мин назад", or "не в сети". */
fun presenceText(lastSeen: Instant?, online: Boolean, hideLastSeen: Boolean, typing: Boolean): String {
    if (typing) return "печатает…"
    if (isOnline(lastSeen, online, hideLastSeen)) return "в сети"
    if (hideLastSeen || lastSeen == null) return "не в сети"
    val now = Clock.System.now()
    val mins = (now.toEpochMilliseconds() - lastSeen.toEpochMilliseconds()) / 60000
    return when {
        mins < 1 -> "был(а) недавно"
        mins < 60 -> "был(а) $mins мин назад"
        mins < 1440 -> "был(а) ${mins / 60} ч назад"
        else -> "был(а) ${formatDay(lastSeen)}"
    }
}
