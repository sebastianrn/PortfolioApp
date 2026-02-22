package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.data.model.HistoricalStats

/**
 * Calculates historical performance stats from the portfolio value curve.
 * Input: List of (timestamp, totalPortfolioValue) pairs, one per day, sorted chronologically.
 */
class CalculateHistoricalStatsUseCase {

    operator fun invoke(curve: List<Pair<Long, Double>>): HistoricalStats {
        if (curve.size < 2) return HistoricalStats()

        // All-time high and low
        val athPoint = curve.maxBy { it.second }
        val atlPoint = curve.minBy { it.second }

        // Day-over-day changes
        var bestDayAbsolute = 0.0
        var bestDayPercent = 0.0
        var bestDayDate = 0L
        var worstDayAbsolute = 0.0
        var worstDayPercent = 0.0
        var worstDayDate = 0L

        // Max drawdown tracking
        var peak = curve.first().second
        var maxDrawdownPercent = 0.0

        for (i in 1 until curve.size) {
            val prevValue = curve[i - 1].second
            val currValue = curve[i].second
            val timestamp = curve[i].first

            // Day-over-day change
            val change = currValue - prevValue
            val changePercent = if (prevValue != 0.0) (change / prevValue) * 100 else 0.0

            if (change > bestDayAbsolute) {
                bestDayAbsolute = change
                bestDayPercent = changePercent
                bestDayDate = timestamp
            }
            if (change < worstDayAbsolute) {
                worstDayAbsolute = change
                worstDayPercent = changePercent
                worstDayDate = timestamp
            }

            // Max drawdown
            if (currValue > peak) {
                peak = currValue
            }
            if (peak > 0.0) {
                val drawdown = (peak - currValue) / peak * 100
                if (drawdown > maxDrawdownPercent) {
                    maxDrawdownPercent = drawdown
                }
            }
        }

        // Total return
        val firstValue = curve.first().second
        val lastValue = curve.last().second
        val totalReturnPercent = if (firstValue != 0.0) {
            (lastValue - firstValue) / firstValue * 100
        } else 0.0

        return HistoricalStats(
            allTimeHigh = athPoint.second,
            allTimeHighDate = athPoint.first,
            allTimeLow = atlPoint.second,
            allTimeLowDate = atlPoint.first,
            bestDayAbsolute = bestDayAbsolute,
            bestDayPercent = bestDayPercent,
            bestDayDate = bestDayDate,
            worstDayAbsolute = worstDayAbsolute,
            worstDayPercent = worstDayPercent,
            worstDayDate = worstDayDate,
            maxDrawdownPercent = maxDrawdownPercent,
            totalReturnPercent = totalReturnPercent
        )
    }
}
