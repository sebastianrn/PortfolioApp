package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.*
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ModernDetailTopBar(
                title = asset?.name ?: "",
                onBackClick = onBackClick,
                onEditClick = { showEditDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    Icons.Filled.Add,
                    "Add Record",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                asset?.let { a ->
                    ModernAssetSummaryCard(a)
                }
            }

            item {
                if (chartPoints.isNotEmpty()) {
                    ModernPerformanceCard(chartPoints)
                }
            }

            item {
                Text(
                    "Price History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(history) { record ->
                ModernHistoryCard(
                    record = record,
                    onEditClick = {
                        if (record.isManual) {
                            historyRecordToEdit = record
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
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
fun ModernDetailTopBar(
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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Icon(
                    Icons.Filled.Edit,
                    "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun ModernAssetSummaryCard(asset: GoldAsset) {
    val isPositive = asset.totalProfitOrLoss >= 0
    val totalInvested = asset.purchasePrice * asset.quantity
    val percentage = if (totalInvested > 0) {
        (asset.totalProfitOrLoss / totalInvested) * 100
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                // Asset badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = asset.type.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${asset.weightInGrams}g",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Current Value
                Text(
                    "Current Value",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(asset.totalCurrentValue.toInt())}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Performance badge
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
                            if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "${if (isPositive) "+" else ""}${String.format("%.1f", percentage)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            "total return",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(20.dp))

                // Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailItem("Quantity", "${asset.quantity}")
                    DetailItem(
                        "Purchase Price",
                        "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(asset.purchasePrice.toInt())}"
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModernPerformanceCard(points: List<Pair<Long, Double>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Performance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                PortfolioChart(
                    points = points,
                    showTimeRangeSelector = true,
                    goldColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ModernHistoryCard(
    record: PriceHistory,
    onEditClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeSdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        onClick = if (record.isManual) onEditClick else { {} },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        enabled = record.isManual
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (record.isManual)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (record.isManual) Icons.Filled.Edit else Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = if (record.isManual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        sdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        timeSdf.format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "CHF ${NumberFormat.getInstance(Locale.GERMAN).format(record.sellPrice.toInt())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}