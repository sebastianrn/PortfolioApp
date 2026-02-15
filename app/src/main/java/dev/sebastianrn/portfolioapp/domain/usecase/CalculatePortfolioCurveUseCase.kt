package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import java.util.Calendar

/**
 * Use case for calculating the portfolio value curve over time.
 * Takes price history and assets, returns a list of (timestamp, totalValue) pairs.
 */
class CalculatePortfolioCurveUseCase {

    operator fun invoke(
        history: List<PriceHistory>,
        assets: List<GoldAsset>
    ): List<Pair<Long, Double>> {
        if (history.isEmpty() || assets.isEmpty()) return emptyList()

        val assetMap = assets.associateBy { it.id }
        val latestPrices = mutableMapOf<Int, Double>()
        val cal = Calendar.getInstance()

        // Group entries by minute (truncate seconds/ms) so one price update batch = one point
        return history.sortedBy { it.dateTimestamp }
            .groupBy { entry ->
                cal.timeInMillis = entry.dateTimestamp
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            .map { (dayTimestamp, entriesForDay) ->
                // 1. Update latest known prices for all assets in these entries
                entriesForDay.forEach { latestPrices[it.assetId] = it.sellPrice }

                // 2. Calculate total portfolio value using latest known price for EVERY asset
                val totalValue = latestPrices.entries.sumOf { (id, price) ->
                    val asset = assetMap[id]
                    (asset?.quantity ?: 0).toDouble() * price
                }

                dayTimestamp to totalValue
            }
    }

    /**
     * Calculate daily change (absolute and percentage) from the portfolio curve.
     */
    fun calculateDailyChange(curve: List<Pair<Long, Double>>): Pair<Double, Double> {
        if (curve.isEmpty()) return 0.0 to 0.0

        // Get current values (Latest point)
        val currentPoint = curve.last()
        val currentValue = currentPoint.second
        val currentTimestamp = currentPoint.first

        // Find "Start of Day" timestamp (Midnight today relative to current point)
        val cal = Calendar.getInstance().apply {
            timeInMillis = currentTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        // Find last point recorded BEFORE today started (yesterday's closing price)
        val previousPoint = curve.lastOrNull { it.first < startOfDay }

        return if (previousPoint != null) {
            val previousValue = previousPoint.second
            val diff = currentValue - previousValue
            val percent = if (previousValue != 0.0) (diff / previousValue) * 100 else 0.0
            diff to percent
        } else {
            // No history before today (new user)
            0.0 to 0.0
        }
    }
}
