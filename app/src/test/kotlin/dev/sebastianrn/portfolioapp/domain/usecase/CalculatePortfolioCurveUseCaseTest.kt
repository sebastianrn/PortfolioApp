package dev.sebastianrn.portfolioapp.domain.usecase

import dev.sebastianrn.portfolioapp.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for CalculatePortfolioCurveUseCase.
 * Tests curve generation and daily change calculations.
 */
class CalculatePortfolioCurveUseCaseTest {

    private lateinit var useCase: CalculatePortfolioCurveUseCase

    @Before
    fun setup() {
        useCase = CalculatePortfolioCurveUseCase()
    }

    // --- invoke() Tests ---

    @Test
    fun `invoke with empty history returns empty list`() {
        val assets = listOf(TestDataFactory.createGoldAsset())

        val result = useCase(emptyList(), assets)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke with empty assets returns empty list`() {
        val history = listOf(TestDataFactory.createPriceHistory())

        val result = useCase(history, emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke with both empty returns empty list`() {
        val result = useCase(emptyList(), emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke with single history entry returns single point`() {
        val asset = TestDataFactory.createGoldAsset(id = 1, quantity = 2)
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L)
        )

        val result = useCase(history, listOf(asset))

        assertEquals(1, result.size)
        assertEquals(1000L, result[0].first)
        assertEquals(200.0, result[0].second, 0.001) // 100 * 2 = 200
    }

    @Test
    fun `invoke calculates cumulative value for multiple assets`() {
        val asset1 = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val asset2 = TestDataFactory.createGoldAsset(id = 2, quantity = 2)
        val assets = listOf(asset1, asset2)

        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 2, sellPrice = 50.0, dateTimestamp = 1000L)
        )

        val result = useCase(history, assets)

        assertEquals(1, result.size)
        // Asset 1: 100 * 1 = 100, Asset 2: 50 * 2 = 100, Total = 200
        assertEquals(200.0, result[0].second, 0.001)
    }

    @Test
    fun `invoke groups entries by date and returns one point per day`() {
        val asset = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 110.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 120.0, dateTimestamp = 1000L)
        )

        val result = useCase(history, listOf(asset))

        // All have same timestamp, should be grouped
        assertEquals(1, result.size)
        // Last entry's price should be used (120)
        assertEquals(120.0, result[0].second, 0.001)
    }

    @Test
    fun `invoke maintains latest prices across different timestamps`() {
        val asset1 = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val asset2 = TestDataFactory.createGoldAsset(id = 2, quantity = 1)
        val assets = listOf(asset1, asset2)

        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 2, sellPrice = 50.0, dateTimestamp = 2000L),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 120.0, dateTimestamp = 3000L)
        )

        val result = useCase(history, assets)

        assertEquals(3, result.size)
        // At timestamp 1000: Only asset1 has price (100)
        assertEquals(100.0, result[0].second, 0.001)
        // At timestamp 2000: asset1 still 100, asset2 now 50 = 150
        assertEquals(150.0, result[1].second, 0.001)
        // At timestamp 3000: asset1 now 120, asset2 still 50 = 170
        assertEquals(170.0, result[2].second, 0.001)
    }

    @Test
    fun `invoke sorts results by timestamp`() {
        val asset = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 300.0, dateTimestamp = 3000L),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 200.0, dateTimestamp = 2000L)
        )

        val result = useCase(history, listOf(asset))

        assertEquals(3, result.size)
        assertEquals(1000L, result[0].first)
        assertEquals(2000L, result[1].first)
        assertEquals(3000L, result[2].first)
    }

    @Test
    fun `invoke ignores history for non-existent assets`() {
        val asset = TestDataFactory.createGoldAsset(id = 1, quantity = 1)
        val history = listOf(
            TestDataFactory.createPriceHistory(assetId = 1, sellPrice = 100.0, dateTimestamp = 1000L),
            TestDataFactory.createPriceHistory(assetId = 999, sellPrice = 500.0, dateTimestamp = 2000L) // Non-existent
        )

        val result = useCase(history, listOf(asset))

        assertEquals(2, result.size)
        // At timestamp 1000: asset1 = 100
        assertEquals(100.0, result[0].second, 0.001)
        // At timestamp 2000: asset1 still 100, asset 999 contributes 0 (no quantity)
        assertEquals(100.0, result[1].second, 0.001)
    }

    // --- calculateDailyChange() Tests ---

    @Test
    fun `calculateDailyChange with empty curve returns zeros`() {
        val result = useCase.calculateDailyChange(emptyList())

        assertEquals(0.0, result.first, 0.001) // absolute change
        assertEquals(0.0, result.second, 0.001) // percentage change
    }

    @Test
    fun `calculateDailyChange with single point returns zeros`() {
        val curve = listOf(System.currentTimeMillis() to 1000.0)

        val result = useCase.calculateDailyChange(curve)

        // No history before today, so returns zeros
        assertEquals(0.0, result.first, 0.001)
        assertEquals(0.0, result.second, 0.001)
    }

    @Test
    fun `calculateDailyChange calculates positive change correctly`() {
        val now = System.currentTimeMillis()
        val yesterdayMidnight = getStartOfDay(now) - 1000 // Just before midnight

        val curve = listOf(
            yesterdayMidnight to 1000.0, // Yesterday's closing
            now to 1100.0 // Current
        )

        val result = useCase.calculateDailyChange(curve)

        assertEquals(100.0, result.first, 0.001) // 1100 - 1000 = 100
        assertEquals(10.0, result.second, 0.001) // (100 / 1000) * 100 = 10%
    }

    @Test
    fun `calculateDailyChange calculates negative change correctly`() {
        val now = System.currentTimeMillis()
        val yesterdayMidnight = getStartOfDay(now) - 1000

        val curve = listOf(
            yesterdayMidnight to 1000.0,
            now to 900.0
        )

        val result = useCase.calculateDailyChange(curve)

        assertEquals(-100.0, result.first, 0.001) // 900 - 1000 = -100
        assertEquals(-10.0, result.second, 0.001) // (-100 / 1000) * 100 = -10%
    }

    @Test
    fun `calculateDailyChange with no change returns zero percentage`() {
        val now = System.currentTimeMillis()
        val yesterdayMidnight = getStartOfDay(now) - 1000

        val curve = listOf(
            yesterdayMidnight to 1000.0,
            now to 1000.0
        )

        val result = useCase.calculateDailyChange(curve)

        assertEquals(0.0, result.first, 0.001)
        assertEquals(0.0, result.second, 0.001)
    }

    @Test
    fun `calculateDailyChange with zero previous value returns zero percentage`() {
        val now = System.currentTimeMillis()
        val yesterdayMidnight = getStartOfDay(now) - 1000

        val curve = listOf(
            yesterdayMidnight to 0.0,
            now to 100.0
        )

        val result = useCase.calculateDailyChange(curve)

        assertEquals(100.0, result.first, 0.001) // 100 - 0 = 100
        assertEquals(0.0, result.second, 0.001) // Division by zero protection
    }

    @Test
    fun `calculateDailyChange only uses last point before today`() {
        val now = System.currentTimeMillis()
        val startOfToday = getStartOfDay(now)
        val yesterday = startOfToday - 1000 // Just before midnight
        val twoDaysAgo = startOfToday - (2 * 24 * 60 * 60 * 1000L)

        val curve = listOf(
            twoDaysAgo to 800.0, // Two days ago
            yesterday to 1000.0, // Yesterday (this should be used)
            now to 1100.0 // Today
        )

        val result = useCase.calculateDailyChange(curve)

        // Should compare against yesterday's value (1000), not two days ago (800)
        assertEquals(100.0, result.first, 0.001)
        assertEquals(10.0, result.second, 0.001)
    }

    @Test
    fun `calculateDailyChange with all points today returns zeros`() {
        val now = System.currentTimeMillis()
        val hourAgo = now - (60 * 60 * 1000L)
        val twoHoursAgo = now - (2 * 60 * 60 * 1000L)

        // All within today - no "yesterday" reference point
        val curve = listOf(
            twoHoursAgo to 1000.0,
            hourAgo to 1050.0,
            now to 1100.0
        )

        // Check if all are actually today
        val startOfToday = getStartOfDay(now)
        val allToday = curve.all { it.first >= startOfToday }

        if (allToday) {
            val result = useCase.calculateDailyChange(curve)
            // No history before today, should return zeros
            assertEquals(0.0, result.first, 0.001)
            assertEquals(0.0, result.second, 0.001)
        }
    }

    // --- Helper Methods ---

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
