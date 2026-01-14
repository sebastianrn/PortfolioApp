package dev.sebastianrn.portfolioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.sebastianrn.portfolioapp.data.AppDatabase
import dev.sebastianrn.portfolioapp.data.NetworkModule
import dev.sebastianrn.portfolioapp.data.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.UserPreferences
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.ui.navigation.AppNavigation
import dev.sebastianrn.portfolioapp.ui.theme.PortfolioAppTheme
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val userPreferences = UserPreferences(applicationContext)

        val apiService = NetworkModule.api
        val scraper = PhiloroScrapingService()

        // This is the single source of truth for the app's data
        val repository = GoldRepository(
            dao = database.goldAssetDao(),
            apiService = apiService,
            scraper = scraper
        )

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

            val goldViewModel: GoldViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return GoldViewModel(application, repository, userPreferences) as T
                    }
                }
            )

            PortfolioAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        goldViewModel = goldViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}