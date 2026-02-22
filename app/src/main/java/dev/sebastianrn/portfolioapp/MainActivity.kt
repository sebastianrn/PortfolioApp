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
import dev.sebastianrn.portfolioapp.data.UserPreferences
import dev.sebastianrn.portfolioapp.ui.navigation.AppNavigation
import dev.sebastianrn.portfolioapp.ui.theme.PortfolioAppTheme
import dev.sebastianrn.portfolioapp.viewmodel.BackupViewModel
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as PortfolioApplication).container
        val repository = appContainer.repository
        val userPreferences = UserPreferences(applicationContext)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())

            val goldViewModel: GoldViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return GoldViewModel(
                            repository = repository,
                            prefs = userPreferences,
                            calculateStats = appContainer.calculatePortfolioStats,
                            calculateCurve = appContainer.calculatePortfolioCurve,
                            calculateHistoricalStats = appContainer.calculateHistoricalStats,
                            updatePrices = appContainer.updatePrices
                        ) as T
                    }
                }
            )

            val backupViewModel: BackupViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return BackupViewModel(
                            application = application,
                            repository = repository,
                            backupManager = appContainer.backupManager
                        ) as T
                    }
                }
            )

            PortfolioAppTheme(
                darkTheme = isDarkTheme,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        goldViewModel = goldViewModel,
                        backupViewModel = backupViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }
}
