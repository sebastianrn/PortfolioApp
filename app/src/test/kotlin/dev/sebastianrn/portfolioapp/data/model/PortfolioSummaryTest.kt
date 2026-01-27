package dev.sebastianrn.portfolioapp.data.model

import dev.sebastianrn.portfolioapp.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for PortfolioSummary data class.
 */
class PortfolioSummaryTest {

    // --- Default Values ---

    @Test
    fun `default constructor creates empty summary`() {
        val summary = PortfolioSummary()

        assertEquals(0.0, summary.totalValue, 0.001)
        assertEquals(0.0, summary.totalProfit, 0.001)
        assertEquals(0.0, summary.totalInvested, 0.001)
    }

    // --- Constructor Tests ---

    @Test
    fun `constructor with values creates correct summary`() {
        val summary = PortfolioSummary(
            totalValue = 10000.0,
            totalProfit = 500.0,
            totalInvested = 9500.0
        )

        assertEquals(10000.0, summary.totalValue, 0.001)
        assertEquals(500.0, summary.totalProfit, 0.001)
        assertEquals(9500.0, summary.totalInvested, 0.001)
    }

    @Test
    fun `constructor handles negative profit`() {
        val summary = PortfolioSummary(
            totalValue = 8000.0,
            totalProfit = -2000.0,
            totalInvested = 10000.0
        )

        assertEquals(8000.0, summary.totalValue, 0.001)
        assertEquals(-2000.0, summary.totalProfit, 0.001)
        assertEquals(10000.0, summary.totalInvested, 0.001)
    }

    @Test
    fun `constructor handles zero values`() {
        val summary = PortfolioSummary(
            totalValue = 0.0,
            totalProfit = 0.0,
            totalInvested = 0.0
        )

        assertEquals(0.0, summary.totalValue, 0.001)
        assertEquals(0.0, summary.totalProfit, 0.001)
        assertEquals(0.0, summary.totalInvested, 0.001)
    }

    @Test
    fun `constructor handles large values`() {
        val summary = PortfolioSummary(
            totalValue = 999999999.99,
            totalProfit = 1000000.00,
            totalInvested = 998999999.99
        )

        assertEquals(999999999.99, summary.totalValue, 0.01)
        assertEquals(1000000.00, summary.totalProfit, 0.01)
        assertEquals(998999999.99, summary.totalInvested, 0.01)
    }

    // --- Equality Tests ---

    @Test
    fun `equal summaries are equal`() {
        val summary1 = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )
        val summary2 = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )

        assertEquals(summary1, summary2)
    }

    @Test
    fun `different totalValue means not equal`() {
        val summary1 = PortfolioSummary(totalValue = 1000.0)
        val summary2 = PortfolioSummary(totalValue = 2000.0)

        assertNotEquals(summary1, summary2)
    }

    @Test
    fun `different totalProfit means not equal`() {
        val summary1 = PortfolioSummary(totalProfit = 100.0)
        val summary2 = PortfolioSummary(totalProfit = 200.0)

        assertNotEquals(summary1, summary2)
    }

    @Test
    fun `different totalInvested means not equal`() {
        val summary1 = PortfolioSummary(totalInvested = 900.0)
        val summary2 = PortfolioSummary(totalInvested = 800.0)

        assertNotEquals(summary1, summary2)
    }

    // --- Copy Tests ---

    @Test
    fun `copy preserves unchanged values`() {
        val original = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )
        val copy = original.copy()

        assertEquals(original, copy)
    }

    @Test
    fun `copy can modify totalValue`() {
        val original = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )
        val copy = original.copy(totalValue = 2000.0)

        assertEquals(2000.0, copy.totalValue, 0.001)
        assertEquals(100.0, copy.totalProfit, 0.001)
        assertEquals(900.0, copy.totalInvested, 0.001)
    }

    @Test
    fun `copy can modify totalProfit`() {
        val original = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )
        val copy = original.copy(totalProfit = -50.0)

        assertEquals(1000.0, copy.totalValue, 0.001)
        assertEquals(-50.0, copy.totalProfit, 0.001)
        assertEquals(900.0, copy.totalInvested, 0.001)
    }

    @Test
    fun `copy can modify totalInvested`() {
        val original = PortfolioSummary(
            totalValue = 1000.0,
            totalProfit = 100.0,
            totalInvested = 900.0
        )
        val copy = original.copy(totalInvested = 500.0)

        assertEquals(1000.0, copy.totalValue, 0.001)
        assertEquals(100.0, copy.totalProfit, 0.001)
        assertEquals(500.0, copy.totalInvested, 0.001)
    }

    // --- Factory Method Tests ---

    @Test
    fun `TestDataFactory creates valid summary`() {
        val summary = TestDataFactory.createPortfolioSummary(
            totalValue = 10000.0,
            totalProfit = 500.0,
            totalInvested = 9500.0
        )

        assertEquals(10000.0, summary.totalValue, 0.001)
        assertEquals(500.0, summary.totalProfit, 0.001)
        assertEquals(9500.0, summary.totalInvested, 0.001)
    }

    // --- Business Logic Verification ---

    @Test
    fun `summary values are internally consistent when profit equals value minus invested`() {
        // This is more of a documentation test - PortfolioSummary doesn't enforce this
        // but our UseCase should produce consistent values
        val invested = 1000.0
        val value = 1200.0
        val profit = value - invested // 200

        val summary = PortfolioSummary(
            totalValue = value,
            totalProfit = profit,
            totalInvested = invested
        )

        assertEquals(summary.totalValue - summary.totalInvested, summary.totalProfit, 0.001)
    }
}
