package com.example.portfolioapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.ui.theme.*
import com.example.portfolioapp.util.toCurrencyString
import com.example.portfolioapp.viewmodel.GoldViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
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
fun MainScreen(viewModel: GoldViewModel, onCoinClick: (GoldCoin) -> Unit) {
    val coins by viewModel.allCoins.collectAsState()
    val totalInvestment by viewModel.totalInvestment.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        // 1. Top Bar (Restored & Modernized)
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Gold Portfolio",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },

        // 2. FAB (Add Button)
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 3. Header: Total Balance Card
            // Added padding top to separate from AppBar
            Box(modifier = Modifier.padding(top = 8.dp)) {
                TotalBalanceHeader(totalInvestment)
            }

            // 4. Chart Section
            if (portfolioPoints.isNotEmpty()) {
                PortfolioChart(points = portfolioPoints)
            } else {
                // Placeholder if no data
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add assets to see performance", color = TextGray)
                }
            }

            // 5. Section Title
            Text(
                text = "Your Assets",
                style = MaterialTheme.typography.titleMedium,
                color = TextGray,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )

            // 6. Coin List
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(coins) { coin ->
                    CoinItem(coin, onClick = { onCoinClick(coin) })
                }
            }
        }
    }

    if (showDialog) {
        AddCoinDialog(
            onDismiss = { showDialog = false },
            onAdd = { name, price, qty ->
                viewModel.insert(name, price, qty)
                showDialog = false
            }
        )
    }
}

/**
 * COMPONENT: The Gradient Line Chart
 */
@Composable
fun PortfolioChart(points: List<Pair<Long, Double>>) {
    // Convert Y-values to EntryModel
    val chartModel = entryModelOf(*points.map { it.second.toFloat() }.toTypedArray())

    // X-Axis Formatter (Converts index 0,1,2 -> Date String)
    val horizontalFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in points.indices) {
            val dateMs = points[index].first
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(dateMs))
        } else {
            ""
        }
    }

    // Y-Axis Formatter (Converts value -> "1.5k" or "1500")
    val verticalFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        if (value >= 1000) {
            "${String.format("%.1f", value / 1000f)}k"
        } else {
            "${value.toInt()}"
        }
    }

    // Styles
    val labelColor = TextGray.toArgb()
    val axisLabelStyle = textComponent {
        color = labelColor
        textSizeSp = 10f
    }

    val lineSpec = LineChart.LineSpec(
        lineColor = GoldStart.toArgb(),
        lineBackgroundShader = verticalGradient(
            colors = arrayOf(GoldStart.copy(alpha = 0.5f), Color.Transparent)
        )
    )

    Chart(
        chart = lineChart(lines = listOf(lineSpec)),
        model = chartModel,
        startAxis = rememberStartAxis(
            label = axisLabelStyle,
            valueFormatter = verticalFormatter,
            guideline = null,
            tickLength = 0.dp
        ),
        bottomAxis = rememberBottomAxis(
            label = axisLabelStyle,
            valueFormatter = horizontalFormatter,
            guideline = null,
            tickLength = 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * COMPONENT: Total Balance Gradient Card
 */
@Composable
fun TotalBalanceHeader(total: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Charcoal, Color(0xFF252525))
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Total Portfolio Value",
                color = TextGray,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = total.toCurrencyString,
                color = GoldStart,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * COMPONENT: Single Coin List Item
 */
@Composable
fun CoinItem(coin: GoldCoin, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin Icon Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Charcoal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coin.name.take(1).uppercase(),
                    color = GoldStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name & Qty
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${coin.quantity} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            // Value & Profit
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = coin.totalCurrentValue.toCurrencyString,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )

                val isProfit = coin.totalProfitOrLoss >= 0
                val sign = if (isProfit) "+" else "-"
                val color = if (isProfit) ProfitGreen else LossRed

                Text(
                    text = "$sign${abs(coin.totalProfitOrLoss).toCurrencyString}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * COMPONENT: Add Coin Dialog
 */
@Composable
fun AddCoinDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = SurfaceGray,
        titleContentColor = TextWhite,
        textContentColor = TextGray,
        onDismissRequest = onDismiss,
        title = { Text("Add Investment") },
        text = {
            Column {
                ModernTextField(value = name, onValueChange = { name = it }, label = "Coin Name")
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = qty, onValueChange = { qty = it }, label = "Quantity", isNumber = true)
                Spacer(modifier = Modifier.height(12.dp))
                ModernTextField(value = price, onValueChange = { price = it }, label = "Price (CHF)", isNumber = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(name.isNotEmpty()) {
                        onAdd(name, price.toDoubleOrNull() ?: 0.0, qty.toDoubleOrNull() ?: 0.0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)
            ) { Text("Add Asset") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
            ) { Text("Cancel") }
        }
    )
}

/**
 * COMPONENT: Styled Text Field
 */
@Composable
fun ModernTextField(value: String, onValueChange: (String) -> Unit, label: String, isNumber: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldStart,
            unfocusedBorderColor = TextGray.copy(alpha = 0.5f),
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = GoldStart
        ),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth()
    )
}