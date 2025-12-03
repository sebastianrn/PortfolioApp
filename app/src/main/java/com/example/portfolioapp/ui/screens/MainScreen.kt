package com.example.portfolioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.ui.components.ModernTextField
import com.example.portfolioapp.ui.components.rememberMarker
import com.example.portfolioapp.ui.theme.GoldStart
import com.example.portfolioapp.ui.theme.LossRed
import com.example.portfolioapp.ui.theme.ProfitGreen
import com.example.portfolioapp.ui.theme.TextGray
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
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    themeViewModel: ThemeViewModel,
    onCoinClick: (GoldCoin) -> Unit
) {
    val coins by viewModel.allCoins.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

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
                Icon(Icons.Default.Add, contentDescription = "Add Coin")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            PortfolioSummaryCard(stats)

            if (portfolioPoints.isNotEmpty()) {
                PortfolioPerformanceCard(portfolioPoints)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("Add assets to see performance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldStart, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Your Assets", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp), modifier = Modifier.weight(1f)) {
                items(coins) { coin ->
                    CoinItem(coin, onClick = { onCoinClick(coin) })
                }
            }
        }
    }

    if (showDialog) {
        AddCoinDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, price, qty, weight, premium ->
                viewModel.insert(name, price, qty, weight, premium)
                showDialog = false
            }
        )
    }
}

@Composable
fun AddCoinDialog(onDismiss: () -> Unit, onAdd: (String, Double, Int, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
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
                ModernTextField(value = name, onValueChange = { name = it }, label = "Coin Name")
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = qty, onValueChange = { if (it.all { char -> char.isDigit() }) qty = it }, label = "Quantity", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = weight, onValueChange = { weight = it }, label = "Weight (g) [31.1 = 1oz]", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = premium, onValueChange = { premium = it }, label = "Premium (%)", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = price, onValueChange = { price = it }, label = "Paid Price (CHF)", isNumber = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(name.isNotEmpty() && qty.isNotEmpty() && weight.isNotEmpty()) {
                        onAdd(
                            name,
                            price.toDoubleOrNull() ?: 0.0,
                            qty.toIntOrNull() ?: 1,
                            weight.toDoubleOrNull() ?: 31.1,
                            premium.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)
            ) { Text("Add Asset") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextGray) } }
    )
}

// -- HELPER COMPONENTS --

@Composable
fun PortfolioSummaryCard(stats: PortfolioSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(24.dp)) {
            Column { Text(text = "Total Portfolio Value", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge); Text(text = stats.totalValue.toCurrencyString, color = GoldStart, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold) }
            Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)); Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Invested Capital", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); Text(text = stats.totalInvested.toCurrencyString, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                Column(horizontalAlignment = Alignment.End) { Text("Total Return", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall); val isProfit = stats.totalProfit >= 0; val color = if (isProfit) ProfitGreen else LossRed; val sign = if (isProfit) "+" else "-"; Text(text = "$sign${abs(stats.totalProfit).toCurrencyString}", color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    }
}

@Composable
fun PortfolioPerformanceCard(points: List<Pair<Long, Double>>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DateRange, contentDescription = null, tint = GoldStart); Spacer(modifier = Modifier.width(8.dp)); Text("Performance", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }; Spacer(modifier = Modifier.height(16.dp)); PortfolioChart(points = points) }
    }
}

@Composable
fun CoinItem(coin: GoldCoin, onClick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onClick() }, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) { Text(text = coin.name.take(1).uppercase(), color = GoldStart, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) { Text(coin.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold); Text("${coin.quantity} coins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Column(horizontalAlignment = Alignment.End) { Text(coin.totalCurrentValue.toCurrencyString, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); val isProfit = coin.totalProfitOrLoss >= 0; val color = if (isProfit) ProfitGreen else LossRed; val sign = if (isProfit) "+" else "-"; Text(text = "$sign${abs(coin.totalProfitOrLoss).toCurrencyString}", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold) }
        }
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