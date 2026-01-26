package dev.sebastianrn.portfolioapp.viewmodel

/**
 * Sealed class representing one-time UI events that should be handled once
 * (e.g., showing a toast, navigating, showing an error).
 *
 * This decouples the ViewModel from Android UI concerns like Toast.
 */
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowError(val error: Throwable) : UiEvent()
}
