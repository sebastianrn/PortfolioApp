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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sebastianrn.portfolioapp.data.AppDatabase
import dev.sebastianrn.portfolioapp.data.NetworkModule
import dev.sebastianrn.portfolioapp.data.PhiloroScrapingService
import dev.sebastianrn.portfolioapp.data.UserPreferences
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.ui.screens.DetailScreen
import dev.sebastianrn.portfolioapp.ui.screens.MainScreen
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

            PortfolioAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val goldViewModel: GoldViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                if (modelClass.isAssignableFrom(GoldViewModel::class.java)) {
                                    @Suppress("UNCHECKED_CAST")
                                    return GoldViewModel(
                                        application = application,
                                        repository = repository,
                                        prefs = userPreferences
                                    ) as T
                                }
                                throw IllegalArgumentException("Unknown ViewModel class")
                            }
                        }
                    )

                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                viewModel = goldViewModel,
                                themeViewModel = themeViewModel,
                                onCoinClick = { assetId ->
                                    navController.navigate("detail/$assetId")
                                }
                            )
                        }

                        composable("detail/{assetId}") { backStackEntry ->
                            val assetId = backStackEntry.arguments?.getString("assetId")?.toIntOrNull()

                            if (assetId != null) {
                                DetailScreen(
                                    assetId = assetId,
                                    viewModel = goldViewModel,
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}