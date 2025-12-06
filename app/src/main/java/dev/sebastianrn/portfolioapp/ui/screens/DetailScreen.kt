package dev.sebastianrn.portfolioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.GoldAsset
import dev.sebastianrn.portfolioapp.data.PriceHistory
import dev.sebastianrn.portfolioapp.ui.components.AssetSheet
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
fun DetailScreen(viewModel: GoldViewModel, coinId: Int, coinName: String, onBackClick: () -> Unit) {
    val asset by viewModel.getAssetById(coinId).collectAsState(initial = null)
    val history by viewModel.getHistoryForAsset(coinId).collectAsState(initial = emptyList())
    val currency by viewModel.currentCurrency.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var historyRecordToEdit by remember { mutableStateOf<PriceHistory?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(coinName, style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = { showEditDialog = true }) { Icon(Icons.Default.Edit, "Edit") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }, containerColor = GoldStart, contentColor = Color.Black, shape = MaterialTheme.shapes.large) {
                Icon(Icons.Default.Add, "Update Value")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
            item { asset?.let { AssetStatsHeader(it, currency) } }
            item { if (history.isNotEmpty()) { val chartPoints = history.reversed().map { it.dateTimestamp to it.price }; PerformanceCard(chartPoints) } }
            item { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(24.dp)) { Icon(Icons.Default.DateRange, null, tint = GoldStart, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp)); Text(stringResource(R.string.price_history_title), style = MaterialTheme.typography.headlineSmall) } }
            items(history) { record -> HistoryItemCard(record, currency, onEditClick = { if (record.isManual) historyRecordToEdit = record }) }
        }
    }

    if (showSheet) UpdatePriceSheet(onDismiss = { showSheet = false }, onSave = { price, date -> viewModel.addDailyRate(coinId, price, date); showSheet = false })
    if (historyRecordToEdit != null) UpdatePriceSheet(onDismiss = { historyRecordToEdit = null }, initialPrice = historyRecordToEdit!!.price, initialDate = historyRecordToEdit!!.dateTimestamp, isEditMode = true, onSave = { price, date -> viewModel.updateHistoryRecord(historyRecordToEdit!!.historyId, historyRecordToEdit!!.assetId, price, date, true); historyRecordToEdit = null })
    if (showEditDialog && asset != null) AssetSheet(asset = asset, onDismiss = { showEditDialog = false }, onSave = { name, type, price, qty, weight, premium -> viewModel.updateAsset(asset!!.id, name, type, price, qty, weight, premium); showEditDialog = false })
}

@Composable
fun AssetStatsHeader(asset: GoldAsset, currency: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.Center) {
                listOf(asset.type.name, "${asset.weightInGrams}g", "+${asset.premiumPercent}% Prem.").forEach { label ->
                    Box(modifier = Modifier.padding(horizontal = 4.dp).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small).padding(horizontal = 12.dp, vertical = 6.dp)) { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(stringResource(R.string.quantity_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("${asset.quantity}", style = MaterialTheme.typography.headlineMedium) }
                Column(horizontalAlignment = Alignment.End) { Text(stringResource(R.string.current_value_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(asset.totalCurrentValue.toCurrencyString(currency), style = MaterialTheme.typography.headlineMedium, color = GoldStart) }
            }
            Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f)); Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(stringResource(R.string.bought_at_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(asset.originalPrice.toCurrencyString(currency), style = MaterialTheme.typography.titleMedium) }
                Column(horizontalAlignment = Alignment.End) {
                    val isProfit = asset.totalProfitOrLoss >= 0
                    val color = if (isProfit) ProfitGreen else LossRed
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = color, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(abs(asset.totalProfitOrLoss).toCurrencyString(currency), color = color, style = MaterialTheme.typography.titleMedium) }
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(points: List<Pair<Long, Double>>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ShowChart, null, tint = GoldStart); Spacer(modifier = Modifier.width(8.dp)); Text(stringResource(R.string.performance_title), style = MaterialTheme.typography.headlineSmall) }
            Spacer(modifier = Modifier.height(16.dp))
            val chartModel = entryModelOf(*points.map { it.second.toFloat() }.toTypedArray())
            val lineSpec = com.patrykandpatrick.vico.compose.chart.line.lineSpec(lineColor = GoldStart, lineBackgroundShader = verticalGradient(colors = arrayOf(GoldStart.copy(0.5f), Color.Transparent)))
            Chart(chart = lineChart(lines = listOf(lineSpec)), model = chartModel, startAxis = rememberStartAxis(label = textComponent { color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(); textSizeSp = 10f }, valueFormatter = { value, _ -> if (value >= 1000) "${String.format("%.1f", value / 1000f)}k" else "${value.toInt()}" }, tickLength = 0.dp), bottomAxis = rememberBottomAxis(label = textComponent { color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(); textSizeSp = 10f }, valueFormatter = { value, _ -> val index = value.toInt(); if (index in points.indices) SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(points[index].first)) else "" }, tickLength = 0.dp), marker = rememberMarker(), modifier = Modifier.fillMaxWidth().height(200.dp).padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun HistoryItemCard(record: PriceHistory, currency: String, onEditClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(enabled = record.isManual) { onEditClick() }, elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.dateTimestamp)), style = MaterialTheme.typography.titleMedium)

                // NEW: Icon Only (Edit vs Cloud)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.dateTimestamp)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Icon logic: Manual -> Edit, API -> Cloud
                    val icon = if (record.isManual) Icons.Default.Edit else Icons.Default.Cloud
                    val description = if (record.isManual) "Manual" else "API"
                    val tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                    Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(14.dp), tint = tint)
                }
            }
            Text(record.price.toCurrencyString(currency), color = GoldStart, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePriceSheet(onDismiss: () -> Unit, initialPrice: Double? = null, initialDate: Long? = null, isEditMode: Boolean = false, onSave: (Double, Long) -> Unit) {
    var price by remember { mutableStateOf(initialPrice?.toString() ?: "") }
    var selectedDate by remember { mutableLongStateOf(initialDate ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate, selectableDates = object : SelectableDates { override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= System.currentTimeMillis() })
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text(stringResource(R.string.ok_action)) } }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel_action)) } }) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Visible color
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
            Text(if (isEditMode) "Edit Record" else stringResource(R.string.update_value_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldStart), border = androidx.compose.foundation.BorderStroke(1.dp, GoldStart)) { Icon(Icons.Default.DateRange, null); Spacer(modifier = Modifier.width(8.dp)); Text(stringResource(R.string.date_label, sdf.format(Date(selectedDate)))) }
            Spacer(modifier = Modifier.height(16.dp))
            ModernTextField(value = price, onValueChange = { price = it }, label = "Price", isNumber = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { if (price.isNotEmpty()) onSave(price.toDouble(), selectedDate) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)) { Text(if (isEditMode) "Update" else stringResource(R.string.save_action), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
        }
    }
}