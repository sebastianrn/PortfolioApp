package com.example.portfolioapp.util

import java.util.Locale

// Extension property to convert any Double to a CHF string
val Double.toCurrencyString: String
    get() = "CHF %.2f".format(Locale.US, this)