package dev.sebastianrn.portfolioapp.util

import java.text.NumberFormat
import java.util.Locale

fun Double.formatCurrency(includeSymbol: Boolean = true, short: Boolean = false): String {
    val value = if (short) {
        val formatter = NumberFormat.getInstance(Locale.GERMAN).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        when {
            //this >= 1_000_000 -> "${(this / 1_000_000).roundToInt()}M"
            //this >= 1_000 -> "${(this / 1_000).roundToInt()}k"
            else -> formatter.format(this)
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