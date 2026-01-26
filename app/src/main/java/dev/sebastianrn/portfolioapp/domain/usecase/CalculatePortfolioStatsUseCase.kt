package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PortfolioSummary

/**
 * Use case for calculating portfolio statistics from a list of assets.
 * Follows the Single Responsibility Principle - only handles stats calculation.
 */
class CalculatePortfolioStatsUseCase {

    operator fun invoke(assets: List<GoldAsset>): PortfolioSummary {
        if (assets.isEmpty()) return PortfolioSummary()

        val totalValue = assets.sumOf { it.totalCurrentValue }
        val totalProfit = assets.sumOf { it.totalProfitOrLoss }
        val totalInvested = assets.sumOf { it.purchasePrice * it.quantity }

        return PortfolioSummary(
            totalValue = totalValue,
            totalProfit = totalProfit,
            totalInvested = totalInvested
        )
    }
}
