package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.ui.shared.AnimatedFloatingActionButton
import dev.sebastianrn.portfolioapp.ui.shared.AnimatedHoldingCard
import dev.sebastianrn.portfolioapp.ui.shared.ExpressiveAssetSheet
import dev.sebastianrn.portfolioapp.ui.shared.PortfolioChartCard
import dev.sebastianrn.portfolioapp.ui.shared.ExpressiveColors
import dev.sebastianrn.portfolioapp.ui.shared.PortfolioHeader
import dev.sebastianrn.portfolioapp.ui.shared.PortfolioTopBar
import dev.sebastianrn.portfolioapp.ui.shared.QuickStatsRow
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    onCoinClick: (Int) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val dailyChange by viewModel.portfolioChange.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Animation for shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Scaffold(
        containerColor = ExpressiveColors.SurfaceContainer,
        topBar = {
            PortfolioTopBar(
                viewModel = viewModel,
                onMenuClick = { showMenu = true }
            )
        },
        floatingActionButton = {
            AnimatedFloatingActionButton(
                onClick = { showDialog = true }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                PortfolioHeader(
                    totalValue = stats.totalValue,
                    totalInvested = stats.totalInvested,
                    dailyChange = dailyChange.first,
                    dailyChangePercent = dailyChange.second,
                    shimmerAlpha = shimmerAlpha
                )
            }

            item {
                PortfolioChartCard(
                    points = portfolioPoints
                )
            }

            item {
                QuickStatsRow(
                    totalInvested = stats.totalInvested,
                    assetCount = assets.size,
                    bestPerformer = assets.maxByOrNull { it.totalProfitOrLoss }
                )
            }

            item {
                Text(
                    text = stringResource(R.string.your_assets),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpressiveColors.OnSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(items = assets, key = { it.id }) { asset ->
                AnimatedHoldingCard(
                    asset = asset,
                    onClick = { onCoinClick(asset.id) }
                )
            }
        }
    }

    // Keep your existing dialogs
    if (showDialog) {
        ExpressiveAssetSheet(
            onDismiss = { showDialog = false },
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
                showDialog = false
            }
        )
    }

    // Add menu dropdown
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        containerColor = ExpressiveColors.SurfaceHigh
    ) {
        // Your existing menu items
    }
}
