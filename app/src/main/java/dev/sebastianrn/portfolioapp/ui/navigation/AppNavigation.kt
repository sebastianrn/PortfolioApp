package dev.sebastianrn.portfolioapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sebastianrn.portfolioapp.ui.components.bottombar.FloatingNavBar
import dev.sebastianrn.portfolioapp.ui.components.bottombar.MainTab
import dev.sebastianrn.portfolioapp.ui.screens.DetailScreen
import dev.sebastianrn.portfolioapp.ui.screens.MainScreen
import dev.sebastianrn.portfolioapp.viewmodel.BackupViewModel
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel

@Composable
fun AppNavigation(
    goldViewModel: GoldViewModel,
    backupViewModel: BackupViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(MainTab.Portfolio) }

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("main") {
                MainScreen(
                    viewModel = goldViewModel,
                    backupViewModel = backupViewModel,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
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

        FloatingNavBar(
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                selectedTab = tab
                // If we're on the detail screen, navigate back to main
                navController.popBackStack("main", inclusive = false)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
