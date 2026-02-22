package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.data.model.HistoricalStats

/**
 * Calculates historical performance stats from the portfolio value curve.
 * Input: List of (timestamp, totalPortfolioValue) pairs, one per day, sorted chronologically.
 */
class CalculateHistoricalStatsUseCase {

    operator fun invoke(curve: List<Pair<Long, Double>>): HistoricalStats {
        if (curve.size < 2) return HistoricalStats()

        val extremes = findExtremes(curve)
        val dayChanges = findBestAndWorstDays(curve)
        val maxDrawdown = calculateMaxDrawdown(curve)
        val totalReturn = calculateTotalReturn(curve)

        return HistoricalStats(
            allTimeHigh = extremes.ath.second,
            allTimeHighDate = extremes.ath.first,
            allTimeLow = extremes.atl.second,
            allTimeLowDate = extremes.atl.first,
            bestDayAbsolute = dayChanges.bestAbsolute,
            bestDayPercent = dayChanges.bestPercent,
            bestDayDate = dayChanges.bestDate,
            worstDayAbsolute = dayChanges.worstAbsolute,
            worstDayPercent = dayChanges.worstPercent,
            worstDayDate = dayChanges.worstDate,
            maxDrawdownPercent = maxDrawdown,
            totalReturnPercent = totalReturn
        )
    }

    private data class Extremes(
        val ath: Pair<Long, Double>,
        val atl: Pair<Long, Double>
    )

    private data class DayChanges(
        val bestAbsolute: Double = 0.0,
        val bestPercent: Double = 0.0,
        val bestDate: Long = 0L,
        val worstAbsolute: Double = 0.0,
        val worstPercent: Double = 0.0,
        val worstDate: Long = 0L
    )

    private fun findExtremes(curve: List<Pair<Long, Double>>): Extremes {
        return Extremes(
            ath = curve.maxBy { it.second },
            atl = curve.minBy { it.second }
        )
    }

    private fun findBestAndWorstDays(curve: List<Pair<Long, Double>>): DayChanges {
        var bestAbsolute = 0.0
        var bestPercent = 0.0
        var bestDate = 0L
        var worstAbsolute = 0.0
        var worstPercent = 0.0
        var worstDate = 0L

        for (i in 1 until curve.size) {
            val prevValue = curve[i - 1].second
            val currValue = curve[i].second
            val change = currValue - prevValue
            val changePercent = if (prevValue != 0.0) (change / prevValue) * 100 else 0.0

            if (change > bestAbsolute) {
                bestAbsolute = change
                bestPercent = changePercent
                bestDate = curve[i].first
            }
            if (change < worstAbsolute) {
                worstAbsolute = change
                worstPercent = changePercent
                worstDate = curve[i].first
            }
        }

        return DayChanges(bestAbsolute, bestPercent, bestDate, worstAbsolute, worstPercent, worstDate)
    }

    private fun calculateMaxDrawdown(curve: List<Pair<Long, Double>>): Double {
        var peak = curve.first().second
        var maxDrawdownPercent = 0.0

        for (i in 1 until curve.size) {
            val currValue = curve[i].second
            if (currValue > peak) peak = currValue
            if (peak > 0.0) {
                val drawdown = (peak - currValue) / peak * 100
                if (drawdown > maxDrawdownPercent) maxDrawdownPercent = drawdown
            }
        }

        return maxDrawdownPercent
    }

    private fun calculateTotalReturn(curve: List<Pair<Long, Double>>): Double {
        val firstValue = curve.first().second
        val lastValue = curve.last().second
        return if (firstValue != 0.0) (lastValue - firstValue) / firstValue * 100 else 0.0
    }
}
