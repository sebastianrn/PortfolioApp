package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CalculatePortfolioStatsUseCase.
 * Tests pure business logic for portfolio statistics calculation.
 */
class CalculatePortfolioStatsUseCaseTest {

    private lateinit var useCase: CalculatePortfolioStatsUseCase

    @Before
    fun setup() {
        useCase = CalculatePortfolioStatsUseCase()
    }

    // --- Empty/Null Cases ---

    @Test
    fun `invoke with empty list returns empty summary`() {
        val result = useCase(emptyList())

        assertEquals(0.0, result.totalValue, 0.001)
        assertEquals(0.0, result.totalProfit, 0.001)
        assertEquals(0.0, result.totalInvested, 0.001)
    }

    // --- Single Asset Cases ---

    @Test
    fun `invoke with single profitable asset calculates correctly`() {
        val asset = TestDataFactory.createProfitableAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 1200.0,
            quantity = 1
        )

        val result = useCase(listOf(asset))

        assertEquals(1200.0, result.totalValue, 0.001)
        assertEquals(200.0, result.totalProfit, 0.001)
        assertEquals(1000.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with single losing asset calculates correctly`() {
        val asset = TestDataFactory.createLosingAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 800.0,
            quantity = 1
        )

        val result = useCase(listOf(asset))

        assertEquals(800.0, result.totalValue, 0.001)
        assertEquals(-200.0, result.totalProfit, 0.001)
        assertEquals(1000.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with single break-even asset calculates correctly`() {
        val asset = TestDataFactory.createBreakEvenAsset(
            purchasePrice = 1000.0,
            quantity = 1
        )

        val result = useCase(listOf(asset))

        assertEquals(1000.0, result.totalValue, 0.001)
        assertEquals(0.0, result.totalProfit, 0.001)
        assertEquals(1000.0, result.totalInvested, 0.001)
    }

    // --- Multiple Quantity Cases ---

    @Test
    fun `invoke with quantity greater than 1 multiplies correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 150.0,
            quantity = 5
        )

        val result = useCase(listOf(asset))

        // totalValue = 150 * 5 = 750
        assertEquals(750.0, result.totalValue, 0.001)
        // totalProfit = (150 - 100) * 5 = 250
        assertEquals(250.0, result.totalProfit, 0.001)
        // totalInvested = 100 * 5 = 500
        assertEquals(500.0, result.totalInvested, 0.001)
    }

    // --- Multiple Asset Cases ---

    @Test
    fun `invoke with mixed portfolio sums correctly`() {
        // Asset 1: Profitable - Invested: 200, Value: 300, Profit: 100
        val asset1 = TestDataFactory.createProfitableAsset(
            id = 1,
            purchasePrice = 100.0,
            currentSellPrice = 150.0,
            quantity = 2
        )

        // Asset 2: Losing - Invested: 500, Value: 400, Profit: -100
        val asset2 = TestDataFactory.createLosingAsset(
            id = 2,
            purchasePrice = 500.0,
            currentSellPrice = 400.0,
            quantity = 1
        )

        val result = useCase(listOf(asset1, asset2))

        // totalValue = 300 + 400 = 700
        assertEquals(700.0, result.totalValue, 0.001)
        // totalProfit = 100 + (-100) = 0
        assertEquals(0.0, result.totalProfit, 0.001)
        // totalInvested = 200 + 500 = 700
        assertEquals(700.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with all profitable assets sums correctly`() {
        val assets = listOf(
            TestDataFactory.createProfitableAsset(id = 1, purchasePrice = 100.0, currentSellPrice = 120.0, quantity = 1),
            TestDataFactory.createProfitableAsset(id = 2, purchasePrice = 200.0, currentSellPrice = 250.0, quantity = 1),
            TestDataFactory.createProfitableAsset(id = 3, purchasePrice = 300.0, currentSellPrice = 400.0, quantity = 1)
        )

        val result = useCase(assets)

        // totalValue = 120 + 250 + 400 = 770
        assertEquals(770.0, result.totalValue, 0.001)
        // totalProfit = 20 + 50 + 100 = 170
        assertEquals(170.0, result.totalProfit, 0.001)
        // totalInvested = 100 + 200 + 300 = 600
        assertEquals(600.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with all losing assets sums correctly`() {
        val assets = listOf(
            TestDataFactory.createLosingAsset(id = 1, purchasePrice = 100.0, currentSellPrice = 80.0, quantity = 1),
            TestDataFactory.createLosingAsset(id = 2, purchasePrice = 200.0, currentSellPrice = 150.0, quantity = 1),
            TestDataFactory.createLosingAsset(id = 3, purchasePrice = 300.0, currentSellPrice = 250.0, quantity = 1)
        )

        val result = useCase(assets)

        // totalValue = 80 + 150 + 250 = 480
        assertEquals(480.0, result.totalValue, 0.001)
        // totalProfit = -20 + -50 + -50 = -120
        assertEquals(-120.0, result.totalProfit, 0.001)
        // totalInvested = 100 + 200 + 300 = 600
        assertEquals(600.0, result.totalInvested, 0.001)
    }

    // --- Edge Cases ---

    @Test
    fun `invoke with zero purchase price calculates correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 0.0,
            currentSellPrice = 100.0,
            quantity = 1
        )

        val result = useCase(listOf(asset))

        assertEquals(100.0, result.totalValue, 0.001)
        assertEquals(100.0, result.totalProfit, 0.001) // 100 - 0 = 100
        assertEquals(0.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with zero current price calculates correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 0.0,
            quantity = 1
        )

        val result = useCase(listOf(asset))

        assertEquals(0.0, result.totalValue, 0.001)
        assertEquals(-100.0, result.totalProfit, 0.001) // 0 - 100 = -100
        assertEquals(100.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with zero quantity results in zero values`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 150.0,
            quantity = 0
        )

        val result = useCase(listOf(asset))

        assertEquals(0.0, result.totalValue, 0.001)
        assertEquals(0.0, result.totalProfit, 0.001)
        assertEquals(0.0, result.totalInvested, 0.001)
    }

    @Test
    fun `invoke with large values handles precision correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 999999.99,
            currentSellPrice = 1000000.01,
            quantity = 100
        )

        val result = useCase(listOf(asset))

        // totalValue = 1000000.01 * 100 = 100000001.0
        assertEquals(100000001.0, result.totalValue, 0.01)
        // totalProfit = (1000000.01 - 999999.99) * 100 = 2.0
        assertEquals(2.0, result.totalProfit, 0.01)
        // totalInvested = 999999.99 * 100 = 99999999.0
        assertEquals(99999999.0, result.totalInvested, 0.01)
    }

    // --- Asset Type Cases (ensure both COIN and BAR are handled) ---

    @Test
    fun `invoke handles both COIN and BAR types`() {
        val coin = TestDataFactory.createGoldCoin(
            id = 1,
            purchasePrice = 100.0,
            currentSellPrice = 120.0,
            quantity = 2
        )
        val bar = TestDataFactory.createGoldBar(
            id = 2,
            purchasePrice = 500.0,
            currentSellPrice = 550.0,
            quantity = 1
        )

        val result = useCase(listOf(coin, bar))

        // Coin: Value = 240, Invested = 200, Profit = 40
        // Bar: Value = 550, Invested = 500, Profit = 50
        assertEquals(790.0, result.totalValue, 0.001)
        assertEquals(90.0, result.totalProfit, 0.001)
        assertEquals(700.0, result.totalInvested, 0.001)
    }
}
