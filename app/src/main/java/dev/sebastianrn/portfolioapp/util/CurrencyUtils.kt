package dev.sebastianrn.portfolioapp.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val currencyFormatter = NumberFormat.getInstance(Locale.GERMAN).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

private val shortCurrencyFormatter = NumberFormat.getInstance(Locale.GERMAN).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 0
}

fun Double.formatCurrency(includeSymbol: Boolean = true, short: Boolean = false): String {
    val formatter = if (short) shortCurrencyFormatter else currencyFormatter
    val value = formatter.format(this)
    return if (includeSymbol || short) "${Constants.DEFAULT_CURRENCY} $value" else value
}

/**
 * Formats a Double as a percentage string (e.g., "12.3%").
 * Uses absolute value by default. Set [showSign] to include +/- prefix.
 */
fun Double.formatAsPercentage(showSign: Boolean = false): String {
    val formatted = String.format("%.1f", abs(this))
    val sign = when {
        showSign && this > 0 -> "+"
        showSign && this < 0 -> "-"
        else -> ""
    }
    return "$sign$formatted%"
}