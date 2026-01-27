package dev.sebastianrn.portfolioapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CurrencyUtils extension functions.
 */
class CurrencyUtilsTest {

    // --- formatCurrency with default parameters ---

    @Test
    fun `formatCurrency with defaults includes CHF symbol`() {
        val result = 1000.0.formatCurrency()

        assertTrue(result.startsWith("CHF"))
    }

    @Test
    fun `formatCurrency formats with two decimal places`() {
        val result = 1234.56.formatCurrency()

        // German locale uses comma as decimal separator
        assertTrue(result.contains("1.234,56") || result.contains("1234,56"))
    }

    @Test
    fun `formatCurrency rounds correctly`() {
        val result = 1234.567.formatCurrency()

        // Should round to 1234.57
        assertTrue(result.contains("1.234,57") || result.contains("1234,57"))
    }

    @Test
    fun `formatCurrency handles zero`() {
        val result = 0.0.formatCurrency()

        assertTrue(result.contains("0,00"))
    }

    @Test
    fun `formatCurrency handles negative values`() {
        val result = (-1234.56).formatCurrency()

        assertTrue(result.contains("-") || result.contains("−"))
        assertTrue(result.contains("1.234,56") || result.contains("1234,56"))
    }

    // --- formatCurrency with includeSymbol parameter ---

    @Test
    fun `formatCurrency without symbol excludes CHF`() {
        val result = 1000.0.formatCurrency(includeSymbol = false)

        // Should not contain CHF when short is false
        assertEquals("1.000,00", result)
    }

    @Test
    fun `formatCurrency with includeSymbol true includes CHF`() {
        val result = 1000.0.formatCurrency(includeSymbol = true)

        assertTrue(result.startsWith("CHF"))
    }

    // --- formatCurrency with short parameter ---

    @Test
    fun `formatCurrency short mode no decimal places`() {
        val result = 1234.56.formatCurrency(short = true)

        // Short mode should not have decimal places
        assertTrue(!result.contains(",56"))
        assertTrue(result.contains("CHF"))
    }

    @Test
    fun `formatCurrency short mode rounds to integer`() {
        val result = 1234.5.formatCurrency(short = true)

        // German locale uses period for thousands
        assertTrue(result.contains("1.234") || result.contains("1.235") || result.contains("1234") || result.contains("1235"))
    }

    @Test
    fun `formatCurrency short mode with large number`() {
        val result = 12345678.0.formatCurrency(short = true)

        assertTrue(result.contains("CHF"))
        // Should have thousand separators
        assertTrue(result.contains("12.345.678") || result.contains("12345678"))
    }

    // --- German Locale Formatting ---

    @Test
    fun `formatCurrency uses German thousands separator`() {
        val result = 1000000.0.formatCurrency()

        // German locale uses period for thousands separator
        assertTrue(result.contains("1.000.000"))
    }

    @Test
    fun `formatCurrency uses German decimal separator`() {
        val result = 123.45.formatCurrency()

        // German locale uses comma for decimal separator
        assertTrue(result.contains("123,45"))
    }

    // --- Edge Cases ---

    @Test
    fun `formatCurrency handles very small values`() {
        val result = 0.01.formatCurrency()

        assertTrue(result.contains("0,01"))
    }

    @Test
    fun `formatCurrency handles very large values`() {
        val result = 999999999.99.formatCurrency()

        assertTrue(result.contains("CHF"))
        assertTrue(result.contains("999.999.999,99"))
    }

    @Test
    fun `formatCurrency rounds up correctly`() {
        val result = 1.999.formatCurrency()

        // 1.999 should round to 2.00
        assertTrue(result.contains("2,00"))
    }

    @Test
    fun `formatCurrency rounds down correctly`() {
        val result = 1.001.formatCurrency()

        // 1.001 should round to 1.00
        assertTrue(result.contains("1,00"))
    }

    @Test
    fun `formatCurrency handles midpoint rounding`() {
        // Standard rounding: 1.005 should round to 1.01 (round half up)
        val result = 1.005.formatCurrency()

        // Note: Due to floating point precision, 1.005 might be stored as slightly less
        assertTrue(result.contains("1,00") || result.contains("1,01"))
    }

    // --- Combination Tests ---

    @Test
    fun `formatCurrency short mode always includes symbol`() {
        val resultWithSymbol = 1000.0.formatCurrency(includeSymbol = true, short = true)
        val resultWithoutSymbol = 1000.0.formatCurrency(includeSymbol = false, short = true)

        // Based on the implementation, short mode always includes CHF
        assertTrue(resultWithSymbol.contains("CHF"))
        assertTrue(resultWithoutSymbol.contains("CHF"))
    }

    @Test
    fun `formatCurrency normal mode respects includeSymbol`() {
        val withSymbol = 1000.0.formatCurrency(includeSymbol = true, short = false)
        val withoutSymbol = 1000.0.formatCurrency(includeSymbol = false, short = false)

        assertTrue(withSymbol.startsWith("CHF"))
        assertEquals("1.000,00", withoutSymbol)
    }
}
