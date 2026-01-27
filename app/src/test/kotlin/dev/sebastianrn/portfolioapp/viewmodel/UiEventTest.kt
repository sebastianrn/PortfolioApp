package dev.sebastianrn.portfolioapp.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for UiEvent sealed class.
 */
class UiEventTest {

    // --- ShowToast Tests ---

    @Test
    fun `ShowToast contains message`() {
        val event = UiEvent.ShowToast("Test message")

        assertEquals("Test message", event.message)
    }

    @Test
    fun `ShowToast equality works correctly`() {
        val event1 = UiEvent.ShowToast("Same message")
        val event2 = UiEvent.ShowToast("Same message")
        val event3 = UiEvent.ShowToast("Different message")

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    @Test
    fun `ShowToast handles empty message`() {
        val event = UiEvent.ShowToast("")

        assertEquals("", event.message)
    }

    @Test
    fun `ShowToast is instance of UiEvent`() {
        val event: UiEvent = UiEvent.ShowToast("Test")

        assertTrue(event is UiEvent.ShowToast)
    }

    // --- ShowError Tests ---

    @Test
    fun `ShowError contains throwable`() {
        val exception = RuntimeException("Test error")
        val event = UiEvent.ShowError(exception)

        assertEquals(exception, event.error)
        assertEquals("Test error", event.error.message)
    }

    @Test
    fun `ShowError equality works correctly`() {
        val exception = RuntimeException("Error")
        val event1 = UiEvent.ShowError(exception)
        val event2 = UiEvent.ShowError(exception)
        val event3 = UiEvent.ShowError(RuntimeException("Other error"))

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
    }

    @Test
    fun `ShowError handles different exception types`() {
        val runtimeException = UiEvent.ShowError(RuntimeException("Runtime"))
        val illegalStateException = UiEvent.ShowError(IllegalStateException("Illegal"))
        val ioException = UiEvent.ShowError(java.io.IOException("IO"))

        assertEquals("Runtime", runtimeException.error.message)
        assertEquals("Illegal", illegalStateException.error.message)
        assertEquals("IO", ioException.error.message)
    }

    @Test
    fun `ShowError is instance of UiEvent`() {
        val event: UiEvent = UiEvent.ShowError(RuntimeException())

        assertTrue(event is UiEvent.ShowError)
    }

    // --- Pattern Matching Tests ---

    @Test
    fun `when expression handles all UiEvent cases`() {
        val events = listOf(
            UiEvent.ShowToast("Toast"),
            UiEvent.ShowError(RuntimeException("Error"))
        )

        val results = events.map { event ->
            when (event) {
                is UiEvent.ShowToast -> "toast:${event.message}"
                is UiEvent.ShowError -> "error:${event.error.message}"
            }
        }

        assertEquals("toast:Toast", results[0])
        assertEquals("error:Error", results[1])
    }

    @Test
    fun `sealed class ensures exhaustive when`() {
        // This test verifies the sealed class pattern works
        // If a new subclass were added, this would fail to compile
        // until the new case is handled
        val event: UiEvent = UiEvent.ShowToast("Test")

        val handled = when (event) {
            is UiEvent.ShowToast -> true
            is UiEvent.ShowError -> true
        }

        assertTrue(handled)
    }

    // --- Copy Tests ---

    @Test
    fun `ShowToast copy works correctly`() {
        val original = UiEvent.ShowToast("Original")
        val copy = original.copy(message = "Modified")

        assertEquals("Original", original.message)
        assertEquals("Modified", copy.message)
    }

    @Test
    fun `ShowError copy works correctly`() {
        val originalError = RuntimeException("Original")
        val newError = RuntimeException("New")
        val original = UiEvent.ShowError(originalError)
        val copy = original.copy(error = newError)

        assertEquals(originalError, original.error)
        assertEquals(newError, copy.error)
    }
}
