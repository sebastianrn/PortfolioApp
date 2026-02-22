package dev.sebastianrn.portfolioapp.ui.components.chart

import dev.sebastianrn.portfolioapp.util.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ChartDataProcessor {

    /**
     * Downsamples a list of chart points if it exceeds [Constants.MAX_CHART_POINTS].
     * Always preserves the first and last points.
     */
    fun downsample(points: List<Pair<Long, Double>>): List<Pair<Long, Double>> {
        if (points.size <= Constants.MAX_CHART_POINTS) return points
        val step = points.size / Constants.MAX_CHART_POINTS
        return points.filterIndexed { index, _ ->
            index % step == 0 || index == points.lastIndex
        }
    }

    /**
     * Filters points based on selected time range and ensures only one data point per day.
     */
    fun filterPointsByTimeRange(
        points: List<Pair<Long, Double>>,
        timeRange: TimeRange
    ): List<Pair<Long, Double>> {
        if (points.isEmpty()) return emptyList()

        val cutoffTime = if (timeRange == TimeRange.ALL) {
            0L
        } else {
            System.currentTimeMillis() - (timeRange.days * 24 * 60 * 60 * 1000L)
        }

        val filtered = points.filter { it.first >= cutoffTime }

        // Process to ensure only one data point per day (take the last one)
        val calendar = Calendar.getInstance()
        return filtered.groupBy { (timestamp, _) ->
            calendar.timeInMillis = timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        }.map { (_, dayPoints) ->
            dayPoints.maxByOrNull { it.first }!!
        }.sortedBy { it.first }
    }

    /**
     * Calculates Y-axis range with appropriate padding based on time range.
     */
    fun calculateYAxisRange(
        points: List<Pair<Long, Double>>,
        timeRange: TimeRange
    ): Pair<Double, Double> {
        if (points.isEmpty()) return 0.0 to 100.0

        val values = points.map { it.second }
        val minValue = values.minOrNull() ?: 0.0
        val maxValue = values.maxOrNull() ?: 100.0
        val range = maxValue - minValue

        // Padding factor varies by time range for better visualization
        val paddingFactor = when (timeRange) {
            TimeRange.ONE_WEEK -> 0.10
            TimeRange.ONE_MONTH -> 0.08
            TimeRange.SIX_MONTHS -> 0.05
            TimeRange.ONE_YEAR, TimeRange.ALL -> 0.03
        }

        val padding = range * paddingFactor
        // Ensure minimum range of at least 1% of average value to avoid flat lines
        val minRange = ((minValue + maxValue) / 2) * 0.01
        val adjustedPadding = maxOf(padding, minRange / 2)

        val adjustedMin = maxOf(0.0, minValue - adjustedPadding)
        val adjustedMax = maxValue + adjustedPadding

        return adjustedMin to adjustedMax
    }

    /**
     * Returns appropriate date formatter based on time range.
     */
    fun getDateFormatter(timeRange: TimeRange): SimpleDateFormat {
        val pattern = when (timeRange) {
            TimeRange.ONE_WEEK -> "EEE"        // Mon, Tue
            TimeRange.ONE_MONTH -> "MMM dd"    // Jan 15
            TimeRange.SIX_MONTHS -> "MMM dd"   // Jan 15
            TimeRange.ONE_YEAR, TimeRange.ALL -> "MMM yy" // Jan 25
        }
        return SimpleDateFormat(pattern, Locale.getDefault())
    }

    /**
     * Calculates axis label spacing based on number of data points.
     */
    fun calculateAxisLabelSpacing(pointCount: Int): Int {
        return when {
            pointCount <= 7 -> 1
            pointCount <= 30 -> maxOf(1, pointCount / 6)
            else -> maxOf(1, pointCount / 5)
        }
    }
}
