package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.data.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.AssetSheet
import dev.sebastianrn.portfolioapp.ui.components.ModernTextField
import dev.sebastianrn.portfolioapp.ui.components.PortfolioChart
import dev.sebastianrn.portfolioapp.ui.components.PriceChangeIndicator
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import dev.sebastianrn.portfolioapp.util.formatCurrency
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: GoldViewModel, coinId: Int, coinName: String, onBackClick: () -> Unit
) {
    val asset by viewModel.getAssetById(coinId).collectAsState(initial = null)
    val history by viewModel.getHistoryForAsset(coinId).collectAsState(initial = emptyList())
    val currency by viewModel.currentCurrency.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var historyRecordToEdit by remember { mutableStateOf<PriceHistory?>(null) }

    val chartPoints by viewModel.getChartPointsForAsset(coinId).collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    coinName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }, actions = {
                // Edit Asset Button
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Asset",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )
    }, floatingActionButton = {
        FloatingActionButton(
            onClick = { showSheet = true },
            containerColor = GoldStart,
            contentColor = Color.Black
        ) {
            Icon(Icons.Default.Add, contentDescription = "Update Value")
        }
    }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding), contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Stats Header
            item {
                asset?.let { a -> AssetSummaryCard(a, currency) }
            }

            // 2. Performance Chart
            item {
                if (chartPoints.isNotEmpty()) {
                    PerformanceCard(chartPoints) // PerformanceCard now gets pre-processed data
                }
            }

            // 3. History Title
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = GoldStart,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.price_history_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. History Items
            items(history) { record ->
                PriceHistoryCard(
                    record = record, currency = currency, onEditClick = {
                        if (record.isManual) {
                            historyRecordToEdit = record
                        }
                    })
            }
        }
    }

    if (showSheet) {
        UpdatePriceSheet(onDismiss = { showSheet = false }, onSave = { sellPrice, buyPrice, date ->
            viewModel.addDailyRate(coinId, sellPrice, buyPrice, date, true)
            showSheet = false
        })
    }

    if (historyRecordToEdit != null) {
        UpdatePriceSheet(
            onDismiss = { historyRecordToEdit = null },
            initialSellPrice = historyRecordToEdit!!.sellPrice,
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
            })
    }

    if (showEditDialog && asset != null) {
        AssetSheet(
            asset = asset,
            onDismiss = { showEditDialog = false },
            onSave = { updatedAsset ->
                // FIX: Unpack the object properties
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

@Composable
fun AssetSummaryCard(asset: GoldAsset, currency: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        asset.type.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp, 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${asset.weightInGrams}g",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp, 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.quantity_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        "${asset.quantity}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.current_value_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        asset.totalCurrentValue.formatCurrency(),
                        color = GoldStart,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.bought_at_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        asset.purchasePrice.formatCurrency(),
                        color = GoldStart,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }

                val totalInvested = asset.purchasePrice * asset.quantity
                val percentage =
                    if (totalInvested > 0) (asset.totalProfitOrLoss / totalInvested) * 100 else 0.0

                PriceChangeIndicator(
                    amount = totalInvested,
                    percent = percentage,
                    priceTypeString = stringResource(R.string.total_return)
                )
            }
        }
    }
}

@Composable
fun PerformanceCard(points: List<Pair<Long, Double>>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.InsertChart,
                    contentDescription = null,
                    tint = GoldStart
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.performance_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            PortfolioChart(points)
        }
    }
}

@Composable
fun PriceHistoryCard(record: PriceHistory, currency: String, onEditClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = record.isManual) { onEditClick() },
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    SimpleDateFormat(
                        "MMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date(record.dateTimestamp)),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        ).format(Date(record.dateTimestamp)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (record.isManual) {
                        Spacer(modifier = Modifier.width(8.dp));
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editable",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp));
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Not Editable",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
            Text(
                text = record.sellPrice.formatCurrency(),
                color = GoldStart,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePriceSheet(
    onDismiss: () -> Unit,
    initialSellPrice: Double? = null,
    initialBuyPrice: Double? = null,
    initialDate: Long? = null,
    isEditMode: Boolean = false,
    onSave: (Double, Double, Long) -> Unit
) {

    var isSellError by remember { mutableStateOf(false) }
    var isBuyError by remember { mutableStateOf(false) }
    var sellPrice by remember { mutableStateOf(initialSellPrice?.toString() ?: "") }
    var buyPrice by remember { mutableStateOf(initialBuyPrice?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Use the same sheet state strategy as AssetSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()
            })
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = it
                    }; showDatePicker = false
                }) { Text(stringResource(R.string.ok_action)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) { Text(stringResource(R.string.cancel_action)) }
            }) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // Match AssetSheet colors
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // Handle navigation bars and keyboard like AssetSheet
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                )
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEditMode) "Edit Record" else stringResource(R.string.update_value_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Date Picker Field (Styled to match TextFields)
            Box(modifier = Modifier.fillMaxWidth()) {
                ModernTextField(
                    value = sdf.format(Date(selectedDate)),
                    onValueChange = {},
                    label = stringResource(R.string.date_label, "").replace(": ", ""),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = GoldStart
                        )
                    }
                )
                // Invisible box to capture clicks for the Date Picker
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModernTextField(
                value = sellPrice,
                onValueChange = {
                    sellPrice = it
                    isSellError = false
                },
                label = if (isEditMode) "Sell Price" else stringResource(R.string.new_sell_price_label),
                isNumber = true,
                isError = isSellError,
                errorMessage = "Enter a Sell Price"
            )

            ModernTextField(
                value = buyPrice,
                onValueChange = {
                    buyPrice = it
                    isBuyError = false
                },
                label = if (isEditMode) "Buy Price" else stringResource(R.string.new_buy_price_label),
                isNumber = true,
                isError = isBuyError,
                errorMessage = "Enter a Buy Price"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // 1. Validate inputs independently
                    val validSell = sellPrice.toDoubleOrNull()
                    val validBuy = buyPrice.toDoubleOrNull()

                    // 2. Update error states based on validation results
                    isSellError = (validSell == null)
                    isBuyError = (validBuy == null)

                    // 3. Only proceed if BOTH are valid
                    if (validSell != null && validBuy != null) {
                        onSave(validSell, validBuy, selectedDate)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldStart,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isEditMode) "Update" else stringResource(R.string.save_action),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}