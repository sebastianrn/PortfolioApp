package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.ui.components.*
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

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
                viewModel = viewModel,
                onMenuClick = { showMenu = true }
            )
        },
        floatingActionButton = {
            ModernFAB(onClick = { showAssetSheet = true })
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
                ModernQuickStats(
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

@Composable
fun HeroValueCard(
    totalValue: Double,
    dailyChange: Double,
    dailyChangePercent: Double,
    pulseAlpha: Float
) {
    val isPositive = dailyChange >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Total Value",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(totalValue.toInt())}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Animated indicator
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    MaterialTheme.colorScheme.secondary.copy(alpha = pulseAlpha * 0.3f)
                                else
                                    MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha * 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Change indicator
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isPositive)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(abs(dailyChange).toInt())}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "(${String.format("%.1f", abs(dailyChangePercent))}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernQuickStats(
    totalInvested: Double,
    totalProfit: Double
) {
    val profitPercent = if (totalInvested > 0) {
        (totalProfit / totalInvested) * 100
    } else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Invested",
            value = totalInvested,
            icon = Icons.Filled.AccountBalanceWallet,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Total Gain",
            value = totalProfit,
            percentage = profitPercent,
            icon = Icons.Filled.TrendingUp,
            color = if (totalProfit >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    percentage: Double? = null
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(value.toInt())}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (percentage != null) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "${if (percentage >= 0) "+" else ""}${String.format("%.1f", percentage)}%",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    viewModel: GoldViewModel,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Gold Portfolio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Track your investments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(
                onClick = { viewModel.updatePricesFromScraper() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Default.Refresh,
                    "Update",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Filled.MoreVert,
                    "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ModernFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.Black,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            Icons.Filled.Add,
            "Add Asset",
            modifier = Modifier.size(28.dp)
        )
    }
}