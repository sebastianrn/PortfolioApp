package dev.sebastianrn.portfolioapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Constants object.
 * Verifies constant values are correct and within expected ranges.
 */
class ConstantsTest {

    // --- GOLD_FINENESS_24K Tests ---

    @Test
    fun `GOLD_FINENESS_24K is approximately 1`() {
        assertTrue(Constants.GOLD_FINENESS_24K > 0.99)
        assertTrue(Constants.GOLD_FINENESS_24K <= 1.0)
    }

    @Test
    fun `GOLD_FINENESS_24K is exactly 0_9999`() {
        assertEquals(0.9999, Constants.GOLD_FINENESS_24K, 0.0001)
    }

    @Test
    fun `GOLD_FINENESS_24K represents 99_99 percent purity`() {
        // 24k gold is 99.99% pure
        val purityPercent = Constants.GOLD_FINENESS_24K * 100
        assertEquals(99.99, purityPercent, 0.01)
    }

    // --- FUTURE_DATE_TOLERANCE_MS Tests ---

    @Test
    fun `FUTURE_DATE_TOLERANCE_MS is positive`() {
        assertTrue(Constants.FUTURE_DATE_TOLERANCE_MS > 0)
    }

    @Test
    fun `FUTURE_DATE_TOLERANCE_MS is 1 minute in milliseconds`() {
        assertEquals(60_000L, Constants.FUTURE_DATE_TOLERANCE_MS)
    }

    @Test
    fun `FUTURE_DATE_TOLERANCE_MS is reasonable for clock drift`() {
        // Should be between 30 seconds and 5 minutes
        assertTrue(Constants.FUTURE_DATE_TOLERANCE_MS >= 30_000L)
        assertTrue(Constants.FUTURE_DATE_TOLERANCE_MS <= 300_000L)
    }

    // --- MAX_CHART_POINTS Tests ---

    @Test
    fun `MAX_CHART_POINTS is positive`() {
        assertTrue(Constants.MAX_CHART_POINTS > 0)
    }

    @Test
    fun `MAX_CHART_POINTS is exactly 100`() {
        assertEquals(100, Constants.MAX_CHART_POINTS)
    }

    @Test
    fun `MAX_CHART_POINTS is reasonable for chart display`() {
        // Should be between 50 and 500 for good visualization
        assertTrue(Constants.MAX_CHART_POINTS >= 50)
        assertTrue(Constants.MAX_CHART_POINTS <= 500)
    }

    // --- Constant Stability Tests ---

    @Test
    fun `constants maintain expected relationships`() {
        // Future date tolerance should be less than a day
        val oneDayMs = 24 * 60 * 60 * 1000L
        assertTrue(Constants.FUTURE_DATE_TOLERANCE_MS < oneDayMs)

        // Chart points should allow reasonable detail
        assertTrue(Constants.MAX_CHART_POINTS >= 7) // At least a week of daily points
    }

    @Test
    fun `constants are not accidentally zero or negative`() {
        assertTrue(Constants.GOLD_FINENESS_24K > 0)
        assertTrue(Constants.FUTURE_DATE_TOLERANCE_MS > 0)
        assertTrue(Constants.MAX_CHART_POINTS > 0)
    }
}
