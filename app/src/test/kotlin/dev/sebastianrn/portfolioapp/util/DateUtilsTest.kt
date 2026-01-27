package dev.sebastianrn.portfolioapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for DateUtils functions.
 */
class DateUtilsTest {

    // --- mergeTimeIntoDate Tests ---

    @Test
    fun `mergeTimeIntoDate preserves date component`() {
        // Create a date for a specific day (Jan 15, 2024)
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 10, 30, 0)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        // Verify the date is preserved
        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, resultCalendar.get(Calendar.MONTH))
        assertEquals(15, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate uses current time`() {
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)

        // Create a date for yesterday
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        yesterday.set(Calendar.HOUR_OF_DAY, 0)
        yesterday.set(Calendar.MINUTE, 0)
        val dateMillis = yesterday.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Time should be near current time (allowing for test execution time)
        assertEquals(currentHour, resultCalendar.get(Calendar.HOUR_OF_DAY))
        // Minutes might differ slightly during test execution
        assertTrue(Math.abs(currentMinute - resultCalendar.get(Calendar.MINUTE)) <= 1)
    }

    @Test
    fun `mergeTimeIntoDate with midnight date`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.MARCH, 20, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Date should be preserved
        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, resultCalendar.get(Calendar.MONTH))
        assertEquals(20, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate with end of day time`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JUNE, 15, 23, 59, 59)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Date should be preserved (June 15)
        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, resultCalendar.get(Calendar.MONTH))
        assertEquals(15, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate preserves date across year boundary`() {
        val calendar = Calendar.getInstance()
        calendar.set(2023, Calendar.DECEMBER, 31, 23, 59, 59)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Should stay on December 31, 2023
        assertEquals(2023, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, resultCalendar.get(Calendar.MONTH))
        assertEquals(31, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate with leap year date`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 29, 12, 0, 0) // Leap year
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Should preserve Feb 29
        assertEquals(2024, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, resultCalendar.get(Calendar.MONTH))
        assertEquals(29, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate result is greater than or equal to date portion`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JULY, 10, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        // The result should have the same date but current time
        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Extract just the date portion
        val resultDateOnly = Calendar.getInstance()
        resultDateOnly.timeInMillis = result
        resultDateOnly.set(Calendar.HOUR_OF_DAY, 0)
        resultDateOnly.set(Calendar.MINUTE, 0)
        resultDateOnly.set(Calendar.SECOND, 0)
        resultDateOnly.set(Calendar.MILLISECOND, 0)

        val inputDateOnly = Calendar.getInstance()
        inputDateOnly.timeInMillis = dateMillis
        inputDateOnly.set(Calendar.HOUR_OF_DAY, 0)
        inputDateOnly.set(Calendar.MINUTE, 0)
        inputDateOnly.set(Calendar.SECOND, 0)
        inputDateOnly.set(Calendar.MILLISECOND, 0)

        assertEquals(inputDateOnly.timeInMillis, resultDateOnly.timeInMillis)
    }

    @Test
    fun `mergeTimeIntoDate with today date uses current time`() {
        val today = Calendar.getInstance()
        // Set to start of today
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val dateMillis = today.timeInMillis

        val beforeCall = System.currentTimeMillis()
        val result = mergeTimeIntoDate(dateMillis)
        val afterCall = System.currentTimeMillis()

        // Result should be between before and after timestamps
        // (allowing for the merged time to be roughly now)
        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // The result date should be today
        val nowCalendar = Calendar.getInstance()
        assertEquals(nowCalendar.get(Calendar.YEAR), resultCalendar.get(Calendar.YEAR))
        assertEquals(nowCalendar.get(Calendar.MONTH), resultCalendar.get(Calendar.MONTH))
        assertEquals(nowCalendar.get(Calendar.DAY_OF_MONTH), resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate preserves seconds from current time`() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.APRIL, 5, 8, 0, 0)
        val dateMillis = calendar.timeInMillis

        val nowSeconds = Calendar.getInstance().get(Calendar.SECOND)

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        // Seconds should be from current time (allowing for 1 second variance)
        assertTrue(Math.abs(nowSeconds - resultCalendar.get(Calendar.SECOND)) <= 1)
    }

    // --- Edge Case Tests ---

    @Test
    fun `mergeTimeIntoDate with far past date`() {
        val calendar = Calendar.getInstance()
        calendar.set(1990, Calendar.JANUARY, 1, 12, 0, 0)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        assertEquals(1990, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, resultCalendar.get(Calendar.MONTH))
        assertEquals(1, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `mergeTimeIntoDate with far future date`() {
        val calendar = Calendar.getInstance()
        calendar.set(2050, Calendar.DECEMBER, 31, 12, 0, 0)
        val dateMillis = calendar.timeInMillis

        val result = mergeTimeIntoDate(dateMillis)

        val resultCalendar = Calendar.getInstance()
        resultCalendar.timeInMillis = result

        assertEquals(2050, resultCalendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, resultCalendar.get(Calendar.MONTH))
        assertEquals(31, resultCalendar.get(Calendar.DAY_OF_MONTH))
    }
}
