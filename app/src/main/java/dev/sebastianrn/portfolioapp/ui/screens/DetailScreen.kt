package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.ui.components.PortfolioChart
import dev.sebastianrn.portfolioapp.ui.components.AssetSheet
import dev.sebastianrn.portfolioapp.ui.components.EditHistorySheet
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveError
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveOnSurface
import dev.sebastianrn.portfolioapp.ui.theme.ExpressivePrimaryStart
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSecondary
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSurfaceContainer
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveSurfaceHigh
import dev.sebastianrn.portfolioapp.ui.theme.ExpressiveTertiary
import dev.sebastianrn.portfolioapp.util.formatCurrency
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: GoldViewModel,
    assetId: Int,
    onBackClick: () -> Unit
) {
    val asset by viewModel.getAssetById(assetId).collectAsState(initial = null)
    val history by viewModel.getHistoryForAsset(assetId).collectAsState(initial = emptyList())
    val chartPoints by viewModel.getChartPointsForAsset(assetId).collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var historyRecordToEdit by remember { mutableStateOf<PriceHistory?>(null) }

    // Shimmer animation
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
        containerColor = ExpressiveSurfaceContainer,
        topBar = {
            ExpressiveDetailTopBar(
                title = asset?.name ?: "No name available",
                onBackClick = onBackClick,
                onEditClick = { showEditDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = ExpressivePrimaryStart,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Price Record",
                    modifier = Modifier.size(28.dp)
                )
            }
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
                asset?.let { a ->
                    ExpressiveAssetSummaryCard(a, shimmerAlpha)
                }
            }

            item {
                if (chartPoints.isNotEmpty()) {
                    ExpressivePerformanceCard(chartPoints)
                }
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.History,
                        contentDescription = null,
                        tint = ExpressivePrimaryStart,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.price_history_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpressiveOnSurface
                    )
                }
            }

            items(history) { record ->
                PriceHistoryCard(
                    record = record,
                    onEditClick = {
                        if (record.isManual) {
                            historyRecordToEdit = record
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showSheet) {
        EditHistorySheet(
            onDismiss = { showSheet = false },
            onSave = { sellPrice, buyPrice, date ->
                viewModel.addDailyRate(assetId, sellPrice, buyPrice, date, true)
                showSheet = false
            }
        )
    }

    if (historyRecordToEdit != null) {
        EditHistorySheet(
            onDismiss = { historyRecordToEdit = null },
            initialSellPrice = historyRecordToEdit!!.sellPrice,
            initialBuyPrice = historyRecordToEdit!!.buyPrice,
            initialDate = historyRecordToEdit!!.dateTimestamp,
            isEditMode = true,
            onSave = { sellPrice, buyPrice, date ->
                viewModel.updateHistoryRecord(
                    historyId = historyRecordToEdit!!.historyId,
                    assetId = historyRecordToEdit!!.assetId,
                    newSellPrice = sellPrice,
                    newBuyPrice = buyPrice,
                    newDate = date,
                    isManual = true
                )
                historyRecordToEdit = null
            }
        )
    }

    if (showEditDialog && asset != null) {
        AssetSheet(
            asset = asset,
            onDismiss = { showEditDialog = false },
            onSave = { updatedAsset ->
                viewModel.updateAsset(
                    id = updatedAsset.id,
                    name = updatedAsset.name,
                    type = updatedAsset.type,
                    purchasePrice = updatedAsset.purchasePrice,
                    currentSellPrice = updatedAsset.purchasePrice,
                    quantity = updatedAsset.quantity,
                    weight = updatedAsset.weightInGrams,
                    philoroId = updatedAsset.philoroId
                )
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveDetailTopBar(
    title: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit Asset"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ExpressiveSurfaceHigh,
            titleContentColor = ExpressiveOnSurface,
            navigationIconContentColor = ExpressiveOnSurface,
            actionIconContentColor = ExpressiveOnSurface
        )
    )
}

@Composable
fun ExpressiveAssetSummaryCard(
    asset: GoldAsset,
    shimmerAlpha: Float
) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
    } else 0.0

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveSurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ExpressivePrimaryStart.copy(alpha = 0.15f),
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
                // Asset Type Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExpressivePrimaryStart.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = asset.type.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressivePrimaryStart
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ExpressiveSecondary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${asset.weightInGrams}g",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressiveSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Value Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.current_value_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asset.totalCurrentValue.formatCurrency(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpressiveOnSurface,
                            fontSize = 36.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .scale(1f + shimmerAlpha * 0.1f)
                            .clip(CircleShape)
                            .background(
                                if (isPositive)
                                    ExpressiveTertiary.copy(alpha = shimmerAlpha)
                                else
                                    ExpressiveError.copy(alpha = shimmerAlpha)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Performance Chip
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPositive)
                        ExpressiveTertiary.copy(alpha = 0.2f)
                    else
                        ExpressiveError.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (isPositive) ExpressiveTertiary else ExpressiveError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.2f", percentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) ExpressiveTertiary else ExpressiveError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.total_return),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ExpressiveOnSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))

                // Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.quantity_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${asset.quantity}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressiveOnSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.bought_at_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpressiveOnSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = asset.purchasePrice.formatCurrency(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpressivePrimaryStart
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressivePerformanceCard(points: List<Pair<Long, Double>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveSurfaceHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = ExpressivePrimaryStart,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.performance_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ExpressiveOnSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                PortfolioChart(
                    points = points,
                    showTimeRangeSelector = true,
                    goldColor = ExpressivePrimaryStart
                )
            }
        }
    }
}

@Composable
fun PriceHistoryCard(
    record: PriceHistory,
    onEditClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        onClick = if (record.isManual) onEditClick else { {} },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpressiveSurfaceHigh
        ),
        enabled = record.isManual
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (record.isManual)
                                ExpressivePrimaryStart.copy(alpha = 0.2f)
                            else
                                ExpressiveSecondary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (record.isManual) Icons.Filled.Edit else Icons.Filled.Cloud,
                        contentDescription = null,
                        tint = if (record.isManual)
                            ExpressivePrimaryStart
                        else
                            ExpressiveSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = sdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpressiveOnSurface
                    )
                    Text(
                        text = timeSdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ExpressiveOnSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Text(
                text = record.sellPrice.formatCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ExpressivePrimaryStart
            )
        }
    }
}