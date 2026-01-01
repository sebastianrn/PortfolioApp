package dev.sebastianrn.portfolioapp.util

import java.text.NumberFormat
import java.util.Locale
fun Double.formatCurrency(includeSymbol: Boolean = true): String {
    val formatter = NumberFormat.getInstance(Locale.GERMAN).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val formattedValue = formatter.format(this)
    return if (includeSymbol) "CHF $formattedValue" else formattedValue
}