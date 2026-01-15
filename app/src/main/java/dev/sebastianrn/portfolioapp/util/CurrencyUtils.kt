package dev.sebastianrn.portfolioapp.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Double.formatCurrency(includeSymbol: Boolean = true, short: Boolean = false): String {
    val value = if (short) {
        when {
            this >= 1_000_000 -> "${(this / 1_000_000).roundToInt()}M"
            this >= 1_000 -> "${(this / 1_000).roundToInt()}k"
            else -> this.roundToInt().toString()
        }
    } else {
        val formatter = NumberFormat.getInstance(Locale.GERMAN).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        formatter.format(this)
    }
    return if (includeSymbol && !short) "CHF $value" else if (short) "CHF $value" else value
}

fun String.cleanCurrencyOrNull(): Double? {
    if (this.isBlank()) return null

    val cleanString = this
        .replace("CHF", "", ignoreCase = true)
        .replace("EUR", "", ignoreCase = true)
        .trim()
        // 1. Remove thousands separator (Dots in German)
        // We simply remove them so "1.234" becomes "1234"
        .replace(".", "")
        // 2. Convert decimal separator (Comma in German) to Dot (Code Standard)
        // "1234,56" becomes "1234.56"
        .replace(",", ".")

    return cleanString.toDoubleOrNull()
}