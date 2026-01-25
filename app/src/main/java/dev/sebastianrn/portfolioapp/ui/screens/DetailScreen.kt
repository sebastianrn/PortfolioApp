package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.*
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel

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
            DetailTopBar(
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
                    AssetSummaryCard(a)
                }
            }

            item {
                if (chartPoints.isNotEmpty()) {
                    PerformanceCard(chartPoints)
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
                HistoryCard(
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
