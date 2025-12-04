package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.AssetType
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.data.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.ModernTextField
import dev.sebastianrn.portfolioapp.ui.components.rememberMarker
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import dev.sebastianrn.portfolioapp.ui.theme.LossRed
import dev.sebastianrn.portfolioapp.ui.theme.ProfitGreen
import dev.sebastianrn.portfolioapp.util.toCurrencyString
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: GoldViewModel,
    coinId: Int,
    coinName: String,
    onBackClick: () -> Unit
) {
    val asset by viewModel.getAssetById(coinId).collectAsState(initial = null)
    val history by viewModel.getHistoryForAsset(coinId)
        .collectAsState(initial = emptyList<PriceHistory>())
    val currency by viewModel.currentCurrency.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var historyRecordToEdit by remember { mutableStateOf<PriceHistory?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        coinName,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // NEW: Edit Button
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Asset",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = GoldStart,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Update Value")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Stats Header
            item {
                asset?.let { a -> AssetStatsHeader(a, currency) }
            }

            // 2. Performance Chart
            item {
                if (history.isNotEmpty()) {
                    val chartPoints = history.reversed().map { it.dateTimestamp to it.price }
                    PerformanceCard(chartPoints)
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
                HistoryItemCard(
                    record = record, // Pass the whole object
                    currency = currency,
                    onEditClick = {
                        // Only allow edit if it is manual
                        if (record.isManual) {
                            historyRecordToEdit = record
                        }
                    }
                )
            }
        }
    }

    if (showSheet) {
        UpdatePriceSheet(
            onDismiss = { showSheet = false },
            onSave = { price, date ->
                viewModel.addDailyRate(coinId, price, date)
                showSheet = false
            }
        )
    }

    if (showEditDialog && asset != null) {
        EditAssetDialog(
            asset = asset!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { name, type, price, qty, weight, premium ->
                viewModel.updateAsset(asset!!.id, name, type, price, qty, weight, premium)
                showEditDialog = false
            }
        )
    }

    if (historyRecordToEdit != null) {
        UpdatePriceSheet(
            onDismiss = { historyRecordToEdit = null },
            initialPrice = historyRecordToEdit!!.price,
            initialDate = historyRecordToEdit!!.dateTimestamp,
            isEditMode = true, // Change title to "Edit Record"
            onSave = { price, date ->
                viewModel.updateHistoryRecord(
                    historyId = historyRecordToEdit!!.historyId,
                    assetId = historyRecordToEdit!!.assetId,
                    newPrice = price,
                    newDate = date,
                    isManual = true
                )
                historyRecordToEdit = null
            }
        )
    }
}

@Composable
fun AssetStatsHeader(asset: GoldAsset, currency: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Badges Row (Type, Weight, Premium)
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
                        text = asset.type.name, // COIN or BAR
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${asset.weightInGrams}g",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${asset.premiumPercent}% Prem.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Row 1: Quantity & Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.quantity_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${asset.quantity}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.current_value_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        asset.totalCurrentValue.toCurrencyString(currency),
                        color = GoldStart,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            // Row 2: Bought & Profit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.bought_at_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        asset.originalPrice.toCurrencyString(currency),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // UPDATED PROFIT/LOSS SECTION
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.total_return),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    val isProfit = asset.totalProfitOrLoss >= 0
                    val color = if (isProfit) ProfitGreen else LossRed

                    // Calculate Percentage
                    val totalInvested = asset.originalPrice * asset.quantity
                    val percentage =
                        if (totalInvested > 0) (asset.totalProfitOrLoss / totalInvested) * 100 else 0.0

                    // 1. Percentage with Arrow
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isProfit) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.2f", abs(percentage))}%",
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // 2. Money Value
                    Text(
                        text = abs(asset.totalProfitOrLoss).toCurrencyString(currency),
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
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
                    Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = GoldStart
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.performance_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            SingleCoinChart(points)
        }
    }
}

@Composable
fun SingleCoinChart(points: List<Pair<Long, Double>>) {
    val chartPoints = if (points.size == 1) {
        listOf(points[0], points[0].copy(first = points[0].first + 1))
    } else {
        points
    }

    val chartEntryModel = entryModelOf(*chartPoints.map { it.second.toFloat() }.toTypedArray())

    val horizontalFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in chartPoints.indices) {
            SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(chartPoints[index].first))
        } else ""
    }

    val verticalFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        if (value >= 1000) "${String.format("%.1f", value / 1000f)}k" else "${value.toInt()}"
    }

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
    val axisLabelStyle = textComponent {
        color = labelColor
        textSizeSp = 10f
    }

    val lineSpec = com.patrykandpatrick.vico.compose.chart.line.lineSpec(
        lineColor = GoldStart,
        lineBackgroundShader = verticalGradient(
            colors = arrayOf(GoldStart.copy(alpha = 0.5f), Color.Transparent)
        )
    )

    Chart(
        chart = lineChart(lines = listOf(lineSpec)),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            label = axisLabelStyle,
            valueFormatter = verticalFormatter,
            guideline = null,
            tickLength = 0.dp,
            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)
        ),
        bottomAxis = rememberBottomAxis(
            label = axisLabelStyle,
            valueFormatter = horizontalFormatter,
            guideline = null,
            tickLength = 0.dp
        ),
        marker = rememberMarker(),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun HistoryItemCard(
    record: PriceHistory,
    currency: String,
    onEditClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            // Add click listener only if Manual
            .clickable(enabled = record.isManual) { onEditClick() },
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val sdfDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

                Text(
                    text = sdfDate.format(Date(record.dateTimestamp)),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sdfTime.format(Date(record.dateTimestamp)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    // Visual indicator that this is editable
                    if (record.isManual) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editable",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Text(
                text = record.price.toCurrencyString(currency),
                color = GoldStart,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePriceSheet(
    onDismiss: () -> Unit,
    initialPrice: Double? = null,
    initialDate: Long? = null,
    isEditMode: Boolean = false,
    onSave: (Double, Long) -> Unit
) {
    // Initialize with passed values or defaults
    var price by remember { mutableStateOf(initialPrice?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel_action)) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                // Dynamic Title
                text = if (isEditMode) "Edit Record" else stringResource(R.string.update_value_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldStart),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldStart)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.date_label, sdf.format(Date(selectedDate))))
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModernTextField(
                value = price,
                onValueChange = { price = it },
                label = "Price",
                isNumber = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (price.isNotEmpty()) onSave(price.toDouble(), selectedDate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)
            ) { Text(if (isEditMode) "Update" else stringResource(R.string.save_action)) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EditAssetDialog(
    asset: GoldAsset,
    onDismiss: () -> Unit,
    onUpdate: (String, AssetType, Double, Int, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf(asset.name) }
    var type by remember { mutableStateOf(asset.type) }
    var qty by remember { mutableStateOf(asset.quantity.toString()) }
    var weight by remember { mutableStateOf(asset.weightInGrams.toString()) }
    var premium by remember { mutableStateOf(asset.premiumPercent.toString()) }
    var price by remember { mutableStateOf(asset.originalPrice.toString()) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onDismissRequest = onDismiss,
        title = { Text("Edit Asset") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = type == AssetType.COIN,
                        onClick = { type = AssetType.COIN },
                        label = { Text(stringResource(R.string.type_coin)) },
                        leadingIcon = {
                            if (type == AssetType.COIN) Icon(
                                Icons.Default.Check,
                                null
                            )
                        }
                    )
                    FilterChip(
                        selected = type == AssetType.BAR,
                        onClick = { type = AssetType.BAR },
                        label = { Text(stringResource(R.string.type_bar)) },
                        leadingIcon = { if (type == AssetType.BAR) Icon(Icons.Default.Check, null) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = name, onValueChange = { name = it }, label = "Name")
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(
                    value = qty,
                    onValueChange = { if (it.all { c -> c.isDigit() }) qty = it },
                    label = "Quantity",
                    isNumber = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Weight (g)",
                    isNumber = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(
                    value = premium,
                    onValueChange = { premium = it },
                    label = "Premium (%)",
                    isNumber = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Bought At (Total)",
                    isNumber = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onUpdate(
                            name,
                            type,
                            price.toDoubleOrNull() ?: 0.0,
                            qty.toIntOrNull() ?: 1,
                            weight.toDoubleOrNull() ?: 0.0,
                            premium.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldStart,
                    contentColor = Color.Black
                )
            ) { Text("Update") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = dev.sebastianrn.portfolioapp.ui.theme.TextGray)
            ) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    )
}