package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.ui.components.*
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    onAssetClick: (Int) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val dailyChange by viewModel.portfolioChange.collectAsState()

    var showAssetSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Animated value for progress indicators
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ModernTopBar(
                onRefreshClick = { viewModel.updatePricesFromScraper() },
                onMenuClick = { showMenu = true }
            )
        },
        floatingActionButton = {
            FAB(onClick = { showAssetSheet = true })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Value Card
            item {
                HeroValueCard(
                    totalValue = stats.totalValue,
                    dailyChange = dailyChange.first,
                    dailyChangePercent = dailyChange.second,
                    pulseAlpha = pulseAlpha
                )
            }

            // Quick Stats Row
            item {
                QuickStats(
                    totalInvested = stats.totalInvested,
                    totalProfit = stats.totalValue - stats.totalInvested
                )
            }

            // Performance Chart
            item {
                ChartCard(points = portfolioPoints)
            }

            // Section Header
            item {
                Text(
                    "Your Assets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Asset Cards
            items(items = assets, key = { it.id }) { asset ->
                AssetCard(
                    asset = asset,
                    onAssetClick = { onAssetClick(asset.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAssetSheet) {
        AssetSheet(
            onDismiss = { showAssetSheet = false },
            onSave = { asset ->
                viewModel.insertAsset(
                    name = asset.name,
                    type = asset.type,
                    purchasePrice = asset.purchasePrice,
                    buyPrice = asset.currentBuyPrice,
                    qty = asset.quantity,
                    weight = asset.weightInGrams,
                    philoroId = asset.philoroId
                )
                showAssetSheet = false
            }
        )
    }
}
