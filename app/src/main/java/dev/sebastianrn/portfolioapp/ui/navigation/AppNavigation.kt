package dev.sebastianrn.portfolioapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sebastianrn.portfolioapp.ui.screens.DetailScreen
import dev.sebastianrn.portfolioapp.ui.screens.MainScreen
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel

@Composable
fun AppNavigation(
    goldViewModel: GoldViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = modifier
    ) {
        composable("main") {
            MainScreen(
                viewModel = goldViewModel,
                onAssetClick = { assetId ->
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