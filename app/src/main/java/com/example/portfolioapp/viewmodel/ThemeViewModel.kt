package com.example.portfolioapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.portfolioapp.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)

    // Observable State
    val isDarkTheme: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun toggleTheme() {
        viewModelScope.launch {
            // Invert current state
            prefs.toggleTheme(!isDarkTheme.value)
        }
    }
}