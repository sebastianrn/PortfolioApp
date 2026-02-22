package dev.sebastianrn.portfolioapp.util

/**
 * Application-wide constants.
 */
object Constants {
    /** Gold fineness for 24k gold (99.99% pure) */
    const val GOLD_FINENESS_24K = 0.9999

    /** Maximum time tolerance for future dates in milliseconds (1 minute) */
    const val FUTURE_DATE_TOLERANCE_MS = 60_000L

    /** Maximum number of chart points before downsampling */
    const val MAX_CHART_POINTS = 100

    /** Default currency symbol */
    const val DEFAULT_CURRENCY = "CHF"

    /** Maximum number of backup files to keep */
    const val MAX_BACKUP_FILES = 10

    // --- API Base URLs ---
    const val GOLD_API_BASE_URL = "https://www.goldapi.io/"
    const val PHILORO_API_BASE_URL = "https://philoro.ch/api/prices/products?country=CH&currency=CHF&skus="
}
