package dev.sebastianrn.portfolioapp.util

import java.util.Locale

// Extension function to convert any Double to a localized currency string
fun Double.toCurrencyString(currencyCode: String): String {
    val symbol = when (currencyCode) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        "CHF" -> "CHF "
        else -> "$currencyCode "
    }
    return "$symbol%.2f".format(Locale.US, this)
}