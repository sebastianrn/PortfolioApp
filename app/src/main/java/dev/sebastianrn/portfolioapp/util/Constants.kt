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
}
