package com.example.portfolioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolioapp.data.AssetType
import com.example.portfolioapp.data.GoldAsset
import com.example.portfolioapp.ui.components.ModernTextField
import com.example.portfolioapp.ui.components.rememberMarker
import com.example.portfolioapp.ui.theme.*
import com.example.portfolioapp.util.toCurrencyString
import com.example.portfolioapp.viewmodel.GoldViewModel
import com.example.portfolioapp.viewmodel.PortfolioSummary
import com.example.portfolioapp.viewmodel.ThemeViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    themeViewModel: ThemeViewModel,
    onCoinClick: (GoldAsset) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var assetToDelete by remember { mutableStateOf<GoldAsset?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Gold Portfolio", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                },
                actions = {
                    IconButton(onClick = { viewModel.updateAllPricesFromApi() }) {
                        Icon(Icons.Default.Refresh, "Update Prices", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = GoldStart,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                item { PortfolioSummaryCard(stats) }

                item {
                    if (portfolioPoints.isNotEmpty()) {
                        PortfolioPerformanceCard(portfolioPoints)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("Add assets to see performance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldStart, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Your Assets", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                items(items = assets, key = { it.id }) { asset ->
                    @Suppress("DEPRECATION")
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                assetToDelete = asset
                                showDeleteDialog = true
                                false
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) LossRed else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(color)
                                    .padding(end = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        content = { AssetItem(asset, onClick = { onCoinClick(asset) }) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddAssetDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, type, price, qty, weight, premium ->
                viewModel.insert(name, type, price, qty, weight, premium)
                showDialog = false
            }
        )
    }

    if (showDeleteDialog && assetToDelete != null) {
        DeleteConfirmationDialog(
            asset = assetToDelete!!,
            onConfirm = {
                viewModel.deleteAsset(assetToDelete!!)
                showDeleteDialog = false
                assetToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                assetToDelete = null
            }
        )
    }
}

@Composable
fun AssetItem(asset: GoldAsset, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shape = if (asset.type == AssetType.COIN) CircleShape else RoundedCornerShape(4.dp)
            val iconText = if (asset.type == AssetType.COIN) asset.name.take(1).uppercase() else "B"

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, color = GoldStart, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(asset.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text("${asset.type.name} • ${asset.quantity} units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(asset.totalCurrentValue.toCurrencyString, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)

                val isProfit = asset.totalProfitOrLoss >= 0
                val color = if (isProfit) ProfitGreen else LossRed
                // FIX: No sign for negative, only "+" for positive
                val sign = if (isProfit) "+" else ""

                Text(
                    text = "$sign${abs(asset.totalProfitOrLoss).toCurrencyString}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PortfolioSummaryCard(stats: PortfolioSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Total Value
            Column {
                Text(text = "Total Portfolio Value", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                Text(text = stats.totalValue.toCurrencyString, color = GoldStart, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Invested Capital
                Column {
                    Text("Invested Capital", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = stats.totalInvested.toCurrencyString,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                // Total Return
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Return", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

                    val isProfit = stats.totalProfit >= 0
                    val color = if (isProfit) ProfitGreen else LossRed
                    // Removed sign "-" for loss, as color handles it, but kept "+" for profit
                    val sign = if (isProfit) "+" else ""

                    val percentage = if (stats.totalInvested > 0) (stats.totalProfit / stats.totalInvested) * 100 else 0.0

                    // 1. Percentage with Triangle Indicator (Now displayed first)
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Absolute Value (Now displayed below percentage)
                    Text(
                        text = "$sign${abs(stats.totalProfit).toCurrencyString}",
                        color = color,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ... (AddAssetDialog, DeleteConfirmationDialog, PortfolioPerformanceCard, PortfolioChart remain the same as previous)
@Composable
fun AddAssetDialog(onDismiss: () -> Unit, onAdd: (String, AssetType, Double, Int, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AssetType.COIN) }
    var qty by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var premium by remember { mutableStateOf("5.0") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onDismissRequest = onDismiss,
        title = { Text("Add Investment") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = type == AssetType.COIN, onClick = { type = AssetType.COIN }, label = { Text("Coin") }, leadingIcon = { if (type == AssetType.COIN) Icon(Icons.Default.Check, null) })
                    FilterChip(selected = type == AssetType.BAR, onClick = { type = AssetType.BAR }, label = { Text("Bar") }, leadingIcon = { if (type == AssetType.BAR) Icon(Icons.Default.Check, null) })
                }
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = name, onValueChange = { name = it }, label = "Name (e.g. Vreneli / 100g Valcambi)")
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = qty, onValueChange = { if (it.all { char -> char.isDigit() }) qty = it }, label = "Quantity", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = weight, onValueChange = { weight = it }, label = "Weight (g)", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = premium, onValueChange = { premium = it }, label = "Premium (%)", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = price, onValueChange = { price = it }, label = "Paid Price (Total or Per Unit?)", isNumber = true)
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotEmpty()) onAdd(name, type, price.toDoubleOrNull() ?: 0.0, qty.toIntOrNull() ?: 1, weight.toDoubleOrNull() ?: 31.1, premium.toDoubleOrNull() ?: 0.0) }, colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) { Text("Cancel") } }
    )
}

@Composable
fun DeleteConfirmationDialog(asset: GoldAsset, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface, textContentColor = MaterialTheme.colorScheme.onSurfaceVariant, onDismissRequest = onDismiss, title = { Text("Delete Asset") }, text = { Text("Delete '${asset.name}'? This action cannot be undone.") }, confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = LossRed, contentColor = Color.White)) { Text("Delete") } }, dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = TextGray)) { Text("Cancel") } })
}

@Composable
fun PortfolioPerformanceCard(points: List<Pair<Long, Double>>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DateRange, contentDescription = null, tint = GoldStart); Spacer(modifier = Modifier.width(8.dp)); Text("Performance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }; Spacer(modifier = Modifier.height(16.dp)); PortfolioChart(points = points) }
    }
}

@Composable
fun PortfolioChart(points: List<Pair<Long, Double>>) {
    val chartModel = entryModelOf(*points.map { it.second.toFloat() }.toTypedArray())
    val horizontalFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ -> val index = value.toInt(); if (index in points.indices) SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(points[index].first)) else "" }
    val verticalFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ -> if (value >= 1000) "${String.format("%.1f", value / 1000f)}k" else "${value.toInt()}" }
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(); val axisLabelStyle = textComponent { color = labelColor; textSizeSp = 10f }
    val lineSpec = LineChart.LineSpec(lineColor = GoldStart.toArgb(), lineBackgroundShader = verticalGradient(colors = arrayOf(GoldStart.copy(alpha = 0.5f), Color.Transparent)))
    Chart(chart = lineChart(lines = listOf(lineSpec)), model = chartModel, startAxis = rememberStartAxis(label = axisLabelStyle, valueFormatter = verticalFormatter, guideline = null, tickLength = 0.dp, itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 5)), bottomAxis = rememberBottomAxis(label = axisLabelStyle, valueFormatter = horizontalFormatter, guideline = null, tickLength = 0.dp), marker = rememberMarker(), modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 16.dp, vertical = 8.dp))
}