package dev.sebastianrn.portfolioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.sebastianrn.portfolioapp.ui.screens.DetailScreen
import dev.sebastianrn.portfolioapp.ui.screens.MainScreen
import dev.sebastianrn.portfolioapp.ui.theme.PortfolioAppTheme
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Get ViewModels
            val themeViewModel: ThemeViewModel = viewModel()

            // 2. Collect Theme State (Requires the import above)
            val isDark by themeViewModel.isDarkTheme.collectAsState()

            // 3. Apply Theme
            PortfolioAppTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(themeViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    // Shared instance of GoldViewModel for the scope of this graph
    val goldViewModel: GoldViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {

        // Main Screen
        composable("main") {
            MainScreen(
                viewModel = goldViewModel,
                themeViewModel = themeViewModel,
                onCoinClick = { coin ->
                    navController.navigate("detail/${coin.id}/${coin.name}")
                }
            )
        }

        // Detail Screen
        composable(
            route = "detail/{coinId}/{coinName}",
            arguments = listOf(
                navArgument("coinId") { type = NavType.IntType },
                navArgument("coinName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getInt("coinId") ?: 0
            val coinName = backStackEntry.arguments?.getString("coinName") ?: ""

            DetailScreen(
                viewModel = goldViewModel,
                coinId = coinId,
                coinName = coinName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}