package dev.sebastianrn.portfolioapp.ui.components.chart

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val numberFormat = NumberFormat.getInstance(Locale.GERMAN)

object ChartFormatters {

    /**
     * Formats Y-axis values with k/M suffixes for readability.
     */
    fun formatYAxisValue(value: Double): String {
        return when {
            value >= 1_000_000 -> "${(value / 1_000_000).roundToInt()}M"
            value >= 1_000 -> "${(value / 1_000).roundToInt()}k"
            else -> value.toInt().toString()
        }
    }

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
        dateFormatter: SimpleDateFormat
    ): String {
        return if (index in points.indices) {
            dateFormatter.format(Date(points[index].first))
        } else {
            "–"
        }
    }
}
