package com.example.portfolioapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.ui.components.ModernTextField
import com.example.portfolioapp.ui.theme.GoldStart
import com.example.portfolioapp.ui.theme.LossRed
import com.example.portfolioapp.ui.theme.ProfitGreen
import com.example.portfolioapp.ui.theme.SurfaceGray
import com.example.portfolioapp.ui.theme.TextGray
import com.example.portfolioapp.ui.theme.TextWhite
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
    // 1. Observe Coin Details & History
    val coin by viewModel.getCoinById(coinId).collectAsState(initial = null)
    val history by viewModel.getHistoryForCoin(coinId).collectAsState(initial = emptyList())

    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(coinName, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Update Value") },
                icon = { Icon(Icons.Default.Add, "Add") },
                onClick = { showSheet = true },
                containerColor = GoldStart,
                contentColor = Color.Black
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {

            // SECTION 1: Coin Stats Card
            item {
                coin?.let { c ->
                    CoinStatsHeader(c)
                }
            }

            // SECTION 2: The Chart
            item {
                if (history.isNotEmpty()) {
                    Text(
                        "Performance",
                        color = TextGray,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    // We need to reverse history (Database returns DESC, Chart needs ASC)
                    val chartPoints = history.reversed().map { it.dateTimestamp to it.price }
                    SingleCoinChart(chartPoints)
                }
            }

            // SECTION 3: History List Title
            item {
                Text(
                    "Price History",
                    color = TextGray,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )
            }

            // SECTION 4: History Items
            items(history) { record ->
                HistoryItem(date = record.dateTimestamp, price = record.price)
                HorizontalDivider(color = SurfaceGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
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
}

/**
 * COMPONENT: Detailed Stats Card (Quantity, Value, Profit)
 */
@Composable
fun CoinStatsHeader(coin: GoldCoin) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceGray),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Row 1: Quantity and Current Value label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Quantity", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Text("${coin.quantity} units", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Value", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Text(coin.totalCurrentValue.toCurrencyString, color = GoldStart, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = TextGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            // Row 2: Bought At and Profit/Loss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Bought At", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    Text(coin.originalPrice.toCurrencyString, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Return", color = TextGray, style = MaterialTheme.typography.bodySmall)

                    val isProfit = coin.totalProfitOrLoss >= 0
                    val sign = if (isProfit) "+" else "-"
                    val color = if (isProfit) ProfitGreen else LossRed

                    Text(
                        text = "$sign${abs(coin.totalProfitOrLoss).toCurrencyString}",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * COMPONENT: Chart for Single Coin
 */
@Composable
fun SingleCoinChart(points: List<Pair<Long, Double>>) {
    // If only 1 point, Vico needs a duplicate to draw a line, or we handle it gracefully
    val chartPoints = if (points.size == 1) {
        listOf(points[0], points[0].copy(first = points[0].first + 1))
    } else {
        points
    }

    val chartModel = entryModelOf(*chartPoints.map { it.second.toFloat() }.toTypedArray())

    val horizontalFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in chartPoints.indices) {
            val dateMs = chartPoints[index].first
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(dateMs))
        } else {
            ""
        }
    }

    val verticalFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        if (value >= 1000) "${String.format("%.1f", value / 1000f)}k" else "${value.toInt()}"
    }

    val labelColor = TextGray.toArgb()
    val axisLabelStyle = textComponent {
        color = labelColor
        textSizeSp = 10f
    }

    val lineSpec = LineChart.LineSpec(
        lineColor = GoldStart.toArgb(),
        lineThicknessDp = 3f,
        lineBackgroundShader = verticalGradient(
            colors = arrayOf(GoldStart.copy(alpha = 0.4f), Color.Transparent)
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
            .height(200.dp)
            .padding(horizontal = 16.dp)
    )
}

@Composable
fun HistoryItem(date: Long, price: Double) {
    val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sdf.format(Date(date)),
            color = TextGray,
            fontSize = 14.sp
        )
        Text(
            text = price.toCurrencyString,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

// ... UpdatePriceSheet and ModernTextField remain the same as previous steps ...
// (I will omit them here to save space as they haven't changed, but you should keep them in the file)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePriceSheet(onDismiss: () -> Unit, onSave: (Double, Long) -> Unit) {
    var price by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
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
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceGray,
        contentColor = TextWhite
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Update Value", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldStart),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldStart)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Date: ${sdf.format(Date(selectedDate))}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            ModernTextField(value = price, onValueChange = { price = it }, label = "New Price (CHF)", isNumber = true)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { if (price.isNotEmpty()) onSave(price.toDouble(), selectedDate) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldStart, contentColor = Color.Black)
            ) { Text("Save Record") }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}