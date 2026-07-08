package dev.sebastianrn.portfolioapp.ui.components.chart

import dev.sebastianrn.portfolioapp.util.formatDate
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val numberFormat = NumberFormat.getInstance(Locale.GERMAN)

object ChartFormatters {

    /**
     * Formats Y-axis values with K/M suffixes. Values below 10K keep their
     * full form and K/M keep one decimal — whole-thousand rounding collapses
     * nearby axis labels into duplicates (e.g. "3K, 3K, 3K").
     */
    fun formatYAxisValue(value: Double): String {
        return when {
            value >= 1_000_000 -> "${trimOneDecimal(value / 1_000_000)}M"
            value >= 10_000 -> "${trimOneDecimal(value / 1_000)}K"
            else -> numberFormat.format(value.roundToInt())
        }
    }

    private fun trimOneDecimal(value: Double): String =
        String.format(Locale.US, "%.1f", value).removeSuffix(".0")

    /**
     * Formats marker value with CHF prefix.
     */
    fun formatMarkerValue(value: Double): String {
        return "CHF ${numberFormat.format(value.roundToInt())}"
    }

    /**
     * Formats date for marker display.
     */
    fun formatMarkerDate(
        index: Int,
        points: List<Pair<Long, Double>>,
        dateFormatter: DateTimeFormatter
    ): String {
        return if (index in points.indices) {
            points[index].first.formatDate(dateFormatter)
        } else {
            "–"
        }
    }
}
