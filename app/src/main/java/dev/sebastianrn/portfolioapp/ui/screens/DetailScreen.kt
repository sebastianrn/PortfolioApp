package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.data.model.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.cards.AssetSummaryCard
import dev.sebastianrn.portfolioapp.ui.components.cards.HistoryCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PerformanceCard
import dev.sebastianrn.portfolioapp.ui.components.common.AddAssetFab
import dev.sebastianrn.portfolioapp.ui.components.common.EntranceFade
import dev.sebastianrn.portfolioapp.ui.components.common.SectionHeader
import dev.sebastianrn.portfolioapp.ui.components.sheets.AssetSheet
import dev.sebastianrn.portfolioapp.ui.components.sheets.EditHistorySheet
import dev.sebastianrn.portfolioapp.ui.components.topbar.DetailTopBar
import dev.sebastianrn.portfolioapp.ui.theme.AppGradients
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
            AddAssetFab(
                onClick = { showSheet = true },
                contentDescription = "Add price record"
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Soft gold halo bleeding down from the header
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppGradients.goldHalo(centerX = 400f))
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    asset?.let { a ->
                        EntranceFade(index = 0) {
                            AssetSummaryCard(a)
                        }
                    }
                }

                item {
                    if (chartPoints.isNotEmpty()) {
                        EntranceFade(index = 1) {
                            PerformanceCard(
                                points = chartPoints,
                                referenceValue = asset?.purchasePrice?.takeIf { it > 0 },
                                referenceLabel = "Paid"
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Price History",
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

                item { Spacer(modifier = Modifier.height(120.dp)) }
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
                    currentSellPrice = updatedAsset.currentSellPrice,
                    quantity = updatedAsset.quantity,
                    weight = updatedAsset.weightInGrams,
                    philoroId = updatedAsset.philoroId
                )
                showEditDialog = false
            }
        )
    }
}
