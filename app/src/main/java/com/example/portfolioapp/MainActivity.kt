package com.example.portfolioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.portfolioapp.ui.theme.PortfolioAppTheme
import com.example.portfolioapp.ui.screens.DetailScreen
import com.example.portfolioapp.ui.screens.MainScreen
import com.example.portfolioapp.viewmodel.GoldViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PortfolioAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: GoldViewModel = viewModel() // Shared ViewModel

    NavHost(navController = navController, startDestination = "main") {

        // 1. Main Screen
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onCoinClick = { coin ->
                    // Navigate to detail passing ID and Name
                    navController.navigate("detail/${coin.id}/${coin.name}")
                }
            )
        }

        // 2. Detail Screen (with arguments)
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
                viewModel = viewModel,
                coinId = coinId,
                coinName = coinName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}