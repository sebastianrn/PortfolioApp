package dev.sebastianrn.portfolioapp.viewmodel

import androidx.annotation.StringRes

/**
 * Sealed class representing one-time UI events that should be handled once
 * (e.g., showing a toast, navigating, showing an error).
 *
 * Toast messages are string resources (with optional format args) so the
 * ViewModel stays free of both Android UI concerns and hardcoded language.
 */
sealed class UiEvent {
    data class ShowToast(
        @StringRes val messageRes: Int,
        val args: List<Any> = emptyList()
    ) : UiEvent()

    data class ShowError(val error: Throwable) : UiEvent()
}
