package dev.sebastianrn.portfolioapp.ui.components.chart

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for ChartDataProcessor.
 */
class ChartDataProcessorTest {

    // --- filterPointsByTimeRange Tests ---

    @Test
    fun `filterPointsByTimeRange with empty list returns empty list`() {
        val result = ChartDataProcessor.filterPointsByTimeRange(emptyList(), TimeRange.ONE_WEEK)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterPointsByTimeRange with ALL returns all points`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            (now - 365 * dayMs) to 100.0,
            (now - 180 * dayMs) to 150.0,
            (now - 30 * dayMs) to 200.0,
            now to 250.0
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ALL)

        // ALL should keep all points (though may consolidate by day)
        assertTrue(result.isNotEmpty())
        assertEquals(4, result.size)
    }

    @Test
    fun `filterPointsByTimeRange with ONE_WEEK filters correctly`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            (now - 30 * dayMs) to 100.0,  // 30 days ago - excluded
            (now - 7 * dayMs) to 150.0,   // 7 days ago - borderline
            (now - 3 * dayMs) to 200.0,   // 3 days ago - included
            now to 250.0                   // today - included
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_WEEK)

        // Should exclude 30 days ago
        assertTrue(result.all { it.first >= now - 7 * dayMs })
    }

    @Test
    fun `filterPointsByTimeRange with ONE_MONTH filters correctly`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            (now - 60 * dayMs) to 100.0,  // 60 days ago - excluded
            (now - 30 * dayMs) to 150.0,  // 30 days ago - borderline
            (now - 15 * dayMs) to 200.0,  // 15 days ago - included
            now to 250.0                   // today - included
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_MONTH)

        // Should exclude 60 days ago
        assertTrue(result.all { it.first >= now - 30 * dayMs })
    }

    @Test
    fun `filterPointsByTimeRange with SIX_MONTHS filters correctly`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            (now - 365 * dayMs) to 100.0, // 1 year ago - excluded
            (now - 180 * dayMs) to 150.0, // 6 months ago - borderline
            (now - 90 * dayMs) to 200.0,  // 3 months ago - included
            now to 250.0                   // today - included
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.SIX_MONTHS)

        // Should exclude 365 days ago
        assertTrue(result.all { it.first >= now - 180 * dayMs })
    }

    @Test
    fun `filterPointsByTimeRange with ONE_YEAR filters correctly`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            (now - 400 * dayMs) to 100.0, // > 1 year ago - excluded
            (now - 365 * dayMs) to 150.0, // 1 year ago - borderline
            (now - 180 * dayMs) to 200.0, // 6 months ago - included
            now to 250.0                   // today - included
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_YEAR)

        // Should exclude 400 days ago
        assertTrue(result.all { it.first >= now - 365 * dayMs })
    }

    @Test
    fun `filterPointsByTimeRange keeps one point per day`() {
        val now = System.currentTimeMillis()
        // Multiple points on the same day
        val points = listOf(
            now - 1000 to 100.0,
            now - 500 to 150.0,
            now to 200.0
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_WEEK)

        // Should consolidate to one point per day
        assertEquals(1, result.size)
    }

    @Test
    fun `filterPointsByTimeRange takes last value for each day`() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = calendar.timeInMillis

        val points = listOf(
            yesterday to 100.0,
            yesterday + 1000 to 150.0, // Later same day - should be used
            today to 200.0
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_WEEK)

        // Find yesterday's point
        val yesterdayPoint = result.find {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.first
            val todayCal = Calendar.getInstance()
            todayCal.timeInMillis = today
            cal.get(Calendar.DAY_OF_YEAR) != todayCal.get(Calendar.DAY_OF_YEAR)
        }

        if (yesterdayPoint != null) {
            assertEquals(150.0, yesterdayPoint.second, 0.001)
        }
    }

    @Test
    fun `filterPointsByTimeRange returns sorted by timestamp`() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val points = listOf(
            now to 300.0,
            (now - 3 * dayMs) to 100.0,
            (now - 1 * dayMs) to 200.0
        )

        val result = ChartDataProcessor.filterPointsByTimeRange(points, TimeRange.ONE_WEEK)

        // Should be sorted by timestamp ascending
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].first <= result[i + 1].first)
        }
    }

    // --- calculateYAxisRange Tests ---

    @Test
    fun `calculateYAxisRange with empty list returns default range`() {
        val result = ChartDataProcessor.calculateYAxisRange(emptyList(), TimeRange.ONE_WEEK)

        assertEquals(0.0, result.first, 0.001)
        assertEquals(100.0, result.second, 0.001)
    }

    @Test
    fun `calculateYAxisRange calculates min and max with padding`() {
        val points = listOf(
            1L to 1000.0,
            2L to 1100.0,
            3L to 1050.0
        )

        val result = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_WEEK)

        // Min is 1000, Max is 1100, Range is 100
        // ONE_WEEK padding factor is 0.10, so padding is 10
        // Adjusted min should be less than 1000, max should be more than 1100
        assertTrue(result.first < 1000.0)
        assertTrue(result.second > 1100.0)
    }

    @Test
    fun `calculateYAxisRange varies padding by time range`() {
        val points = listOf(
            1L to 1000.0,
            2L to 1100.0
        )

        val weekRange = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_WEEK)
        val yearRange = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_YEAR)

        // ONE_WEEK has larger padding factor (0.10) than ONE_YEAR (0.03)
        val weekPadding = (weekRange.second - 1100) + (1000 - weekRange.first)
        val yearPadding = (yearRange.second - 1100) + (1000 - yearRange.first)

        assertTrue(weekPadding > yearPadding)
    }

    @Test
    fun `calculateYAxisRange does not go below zero`() {
        val points = listOf(
            1L to 10.0, // Small value near zero
            2L to 20.0
        )

        val result = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_WEEK)

        assertTrue(result.first >= 0.0)
    }

    @Test
    fun `calculateYAxisRange handles single point`() {
        val points = listOf(1L to 1000.0)

        val result = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_WEEK)

        // With a single point, range is 0, so minimum padding should kick in
        assertTrue(result.first >= 0.0)
        assertTrue(result.second > result.first)
    }

    @Test
    fun `calculateYAxisRange handles identical values`() {
        val points = listOf(
            1L to 1000.0,
            2L to 1000.0,
            3L to 1000.0
        )

        val result = ChartDataProcessor.calculateYAxisRange(points, TimeRange.ONE_WEEK)

        // Range is 0, so minimum padding should ensure some spread
        assertTrue(result.second > result.first)
        // Min should be around 1000 (possibly slightly below)
        assertTrue(result.first <= 1000.0)
        assertTrue(result.second >= 1000.0)
    }

    // --- getDateFormatter Tests ---

    @Test
    fun `getDateFormatter for ONE_WEEK uses day abbreviation format`() {
        val formatter = ChartDataProcessor.getDateFormatter(TimeRange.ONE_WEEK)
        val pattern = formatter.toPattern()

        // Should use "EEE" for day abbreviation
        assertEquals("EEE", pattern)
    }

    @Test
    fun `getDateFormatter for ONE_MONTH uses month day format`() {
        val formatter = ChartDataProcessor.getDateFormatter(TimeRange.ONE_MONTH)
        val pattern = formatter.toPattern()

        assertEquals("MMM dd", pattern)
    }

    @Test
    fun `getDateFormatter for SIX_MONTHS uses month day format`() {
        val formatter = ChartDataProcessor.getDateFormatter(TimeRange.SIX_MONTHS)
        val pattern = formatter.toPattern()

        assertEquals("MMM dd", pattern)
    }

    @Test
    fun `getDateFormatter for ONE_YEAR uses month year format`() {
        val formatter = ChartDataProcessor.getDateFormatter(TimeRange.ONE_YEAR)
        val pattern = formatter.toPattern()

        assertEquals("MMM yy", pattern)
    }

    @Test
    fun `getDateFormatter for ALL uses month year format`() {
        val formatter = ChartDataProcessor.getDateFormatter(TimeRange.ALL)
        val pattern = formatter.toPattern()

        assertEquals("MMM yy", pattern)
    }

    // --- calculateAxisLabelSpacing Tests ---

    @Test
    fun `calculateAxisLabelSpacing for 7 or less points returns 1`() {
        assertEquals(1, ChartDataProcessor.calculateAxisLabelSpacing(1))
        assertEquals(1, ChartDataProcessor.calculateAxisLabelSpacing(5))
        assertEquals(1, ChartDataProcessor.calculateAxisLabelSpacing(7))
    }

    @Test
    fun `calculateAxisLabelSpacing for medium point count`() {
        // 30 points / 6 = 5
        val spacing = ChartDataProcessor.calculateAxisLabelSpacing(30)
        assertEquals(5, spacing)
    }

    @Test
    fun `calculateAxisLabelSpacing for large point count`() {
        // 100 points / 5 = 20
        val spacing = ChartDataProcessor.calculateAxisLabelSpacing(100)
        assertEquals(20, spacing)
    }

    @Test
    fun `calculateAxisLabelSpacing never returns zero`() {
        assertEquals(1, ChartDataProcessor.calculateAxisLabelSpacing(0))
        assertEquals(1, ChartDataProcessor.calculateAxisLabelSpacing(1))
    }

    @Test
    fun `calculateAxisLabelSpacing scales appropriately`() {
        val spacing7 = ChartDataProcessor.calculateAxisLabelSpacing(7)
        val spacing30 = ChartDataProcessor.calculateAxisLabelSpacing(30)
        val spacing100 = ChartDataProcessor.calculateAxisLabelSpacing(100)

        // More points should have larger spacing
        assertTrue(spacing7 <= spacing30)
        assertTrue(spacing30 <= spacing100)
    }

    // --- TimeRange Tests ---

    @Test
    fun `TimeRange ONE_WEEK has 7 days`() {
        assertEquals(7, TimeRange.ONE_WEEK.days)
        assertEquals("1W", TimeRange.ONE_WEEK.label)
    }

    @Test
    fun `TimeRange ONE_MONTH has 30 days`() {
        assertEquals(30, TimeRange.ONE_MONTH.days)
        assertEquals("1M", TimeRange.ONE_MONTH.label)
    }

    @Test
    fun `TimeRange SIX_MONTHS has 180 days`() {
        assertEquals(180, TimeRange.SIX_MONTHS.days)
        assertEquals("6M", TimeRange.SIX_MONTHS.label)
    }

    @Test
    fun `TimeRange ONE_YEAR has 365 days`() {
        assertEquals(365, TimeRange.ONE_YEAR.days)
        assertEquals("1Y", TimeRange.ONE_YEAR.label)
    }

    @Test
    fun `TimeRange ALL has MAX_VALUE days`() {
        assertEquals(Int.MAX_VALUE, TimeRange.ALL.days)
        assertEquals("ALL", TimeRange.ALL.label)
    }
}
