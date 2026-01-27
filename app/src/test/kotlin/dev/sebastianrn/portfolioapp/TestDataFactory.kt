package dev.sebastianrn.portfolioapp

import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.model.PortfolioSummary
import java.util.Calendar

/**
 * Factory for creating consistent test data across all tests.
 */
object TestDataFactory {

    // --- GoldAsset Factories ---

    fun createGoldAsset(
        id: Int = 1,
        name: String = "Test Gold Bar",
        type: AssetType = AssetType.BAR,
        purchasePrice: Double = 1000.0,
        currentSellPrice: Double = 1100.0,
        currentBuyPrice: Double = 1150.0,
        quantity: Int = 1,
        weightInGrams: Double = 31.1,
        philoroId: Int = 0
    ) = GoldAsset(
        id = id,
        name = name,
        type = type,
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        currentBuyPrice = currentBuyPrice,
        quantity = quantity,
        weightInGrams = weightInGrams,
        philoroId = philoroId
    )

    fun createGoldCoin(
        id: Int = 1,
        name: String = "Test Gold Coin",
        purchasePrice: Double = 500.0,
        currentSellPrice: Double = 550.0,
        currentBuyPrice: Double = 575.0,
        quantity: Int = 2,
        weightInGrams: Double = 7.78, // 1/4 oz
        philoroId: Int = 0
    ) = createGoldAsset(
        id = id,
        name = name,
        type = AssetType.COIN,
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        currentBuyPrice = currentBuyPrice,
        quantity = quantity,
        weightInGrams = weightInGrams,
        philoroId = philoroId
    )

    fun createGoldBar(
        id: Int = 1,
        name: String = "Test Gold Bar",
        purchasePrice: Double = 5000.0,
        currentSellPrice: Double = 5500.0,
        currentBuyPrice: Double = 5750.0,
        quantity: Int = 1,
        weightInGrams: Double = 100.0,
        philoroId: Int = 1991
    ) = createGoldAsset(
        id = id,
        name = name,
        type = AssetType.BAR,
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        currentBuyPrice = currentBuyPrice,
        quantity = quantity,
        weightInGrams = weightInGrams,
        philoroId = philoroId
    )

    /**
     * Create an asset with a loss (currentSellPrice < purchasePrice)
     */
    fun createLosingAsset(
        id: Int = 1,
        purchasePrice: Double = 1000.0,
        currentSellPrice: Double = 800.0,
        quantity: Int = 1
    ) = createGoldAsset(
        id = id,
        name = "Losing Asset",
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        quantity = quantity
    )

    /**
     * Create an asset with a profit (currentSellPrice > purchasePrice)
     */
    fun createProfitableAsset(
        id: Int = 1,
        purchasePrice: Double = 1000.0,
        currentSellPrice: Double = 1200.0,
        quantity: Int = 1
    ) = createGoldAsset(
        id = id,
        name = "Profitable Asset",
        purchasePrice = purchasePrice,
        currentSellPrice = currentSellPrice,
        quantity = quantity
    )

    /**
     * Create a break-even asset (currentSellPrice == purchasePrice)
     */
    fun createBreakEvenAsset(
        id: Int = 1,
        purchasePrice: Double = 1000.0,
        quantity: Int = 1
    ) = createGoldAsset(
        id = id,
        name = "Break Even Asset",
        purchasePrice = purchasePrice,
        currentSellPrice = purchasePrice,
        quantity = quantity
    )

    // --- PriceHistory Factories ---

    fun createPriceHistory(
        historyId: Int = 1,
        assetId: Int = 1,
        dateTimestamp: Long = System.currentTimeMillis(),
        sellPrice: Double = 1100.0,
        buyPrice: Double = 1150.0,
        isManual: Boolean = false
    ) = PriceHistory(
        historyId = historyId,
        assetId = assetId,
        dateTimestamp = dateTimestamp,
        sellPrice = sellPrice,
        buyPrice = buyPrice,
        isManual = isManual
    )

    /**
     * Create a list of price history entries spanning days.
     */
    fun createPriceHistoryList(
        assetId: Int = 1,
        count: Int = 10,
        startPrice: Double = 1000.0,
        priceIncrement: Double = 10.0,
        daysApart: Int = 1
    ): List<PriceHistory> {
        val now = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return (0 until count).map { index ->
            createPriceHistory(
                historyId = index + 1,
                assetId = assetId,
                dateTimestamp = now - (count - index - 1) * daysApart * dayInMillis,
                sellPrice = startPrice + (index * priceIncrement),
                buyPrice = (startPrice + (index * priceIncrement)) * 1.05
            )
        }
    }

    /**
     * Create price history with specific timestamps for chart testing.
     */
    fun createPriceHistoryForDays(
        assetId: Int = 1,
        days: Int = 7,
        basePrice: Double = 1000.0,
        priceVariation: Double = 50.0
    ): List<PriceHistory> {
        val calendar = Calendar.getInstance()
        return (0 until days).map { dayOffset ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            // Set to noon to avoid DST issues
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val price = basePrice + (dayOffset * priceVariation / days) - (priceVariation / 2)
            createPriceHistory(
                historyId = dayOffset + 1,
                assetId = assetId,
                dateTimestamp = calendar.timeInMillis,
                sellPrice = price,
                buyPrice = price * 1.05
            )
        }.reversed() // Oldest first
    }

    // --- PortfolioSummary Factories ---

    fun createPortfolioSummary(
        totalValue: Double = 10000.0,
        totalProfit: Double = 500.0,
        totalInvested: Double = 9500.0
    ) = PortfolioSummary(
        totalValue = totalValue,
        totalProfit = totalProfit,
        totalInvested = totalInvested
    )

    // --- Test Scenarios ---

    /**
     * Creates a portfolio with mixed profit/loss assets.
     */
    fun createMixedPortfolio(): List<GoldAsset> = listOf(
        createProfitableAsset(id = 1, purchasePrice = 100.0, currentSellPrice = 150.0, quantity = 2),
        createLosingAsset(id = 2, purchasePrice = 500.0, currentSellPrice = 400.0, quantity = 1),
        createBreakEvenAsset(id = 3, purchasePrice = 200.0, quantity = 3)
    )

    /**
     * Creates multiple history entries for the same timestamp (same day).
     */
    fun createSameDayHistory(
        assetIds: List<Int>,
        timestamp: Long = System.currentTimeMillis()
    ): List<PriceHistory> = assetIds.mapIndexed { index, assetId ->
        createPriceHistory(
            historyId = index + 1,
            assetId = assetId,
            dateTimestamp = timestamp,
            sellPrice = 1000.0 + (index * 100)
        )
    }

    // --- Timestamp Helpers ---

    fun daysAgo(days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.timeInMillis
    }

    fun hoursAgo(hours: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -hours)
        return calendar.timeInMillis
    }

    fun startOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
