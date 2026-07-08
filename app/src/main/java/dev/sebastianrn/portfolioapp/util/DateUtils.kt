package dev.sebastianrn.portfolioapp.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Shared, thread-safe date formatters (java.time replaces SimpleDateFormat).
 */
object DateFormats {
    /** "05 Jul, 14:30" — hero card live stamp */
    val dayMonthTime: DateTimeFormatter = pattern("dd MMM, HH:mm")

    /** "05 Jul 2026" — records grid dates */
    val fullDate: DateTimeFormatter = pattern("dd MMM yyyy")

    /** "Jul 05, 2026 · 14:30" — history list rows */
    val listRow: DateTimeFormatter = pattern("MMM dd, yyyy · HH:mm")

    /** "Jul 05, 2026" — date picker field */
    val shortDate: DateTimeFormatter = pattern("MMM dd, yyyy")

    /** "Jul 5, 2026 at 14:30" — backup timestamps */
    val backupTimestamp: DateTimeFormatter = pattern("MMM d, yyyy 'at' HH:mm")

    /** "Jul 5, 2026" — backup list dates */
    val backupDate: DateTimeFormatter = pattern("MMM d, yyyy")

    private fun pattern(pattern: String): DateTimeFormatter =
        DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
}

/** Formats an epoch-millis timestamp in the system time zone. */
fun Long.formatDate(formatter: DateTimeFormatter): String =
    formatter.format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))

/**
 * Combines the date portion of [dateMillis] with the current local time,
 * so backdated entries keep a meaningful time-of-day ordering.
 */
fun mergeTimeIntoDate(dateMillis: Long): Long {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
    return date.atTime(LocalTime.now(zone)).atZone(zone).toInstant().toEpochMilli()
}
