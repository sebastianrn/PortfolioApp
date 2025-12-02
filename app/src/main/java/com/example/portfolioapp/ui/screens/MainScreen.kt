package com.example.portfolioapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.portfolioapp.data.GoldCoin
import com.example.portfolioapp.util.toCurrencyString // <--- Import this
import com.example.portfolioapp.viewmodel.GoldViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: GoldViewModel, onCoinClick: (GoldCoin) -> Unit) {
    val coins by viewModel.allCoins.collectAsState()
    val totalInvestment by viewModel.totalInvestment.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gold Portfolio") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Coin")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 1. Total Investment Header (UPDATED)
            Text(
                text = "Total Invested: ${totalInvestment.toCurrencyString}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // 2. Chart
            if (portfolioPoints.isNotEmpty()) {
                val entries = portfolioPoints.map { it.second.toFloat() }.toTypedArray()
                val chartModel = entryModelOf(*entries)

                Card(modifier = Modifier.fillMaxWidth().height(250.dp).padding(16.dp)) {
                    Chart(
                        chart = lineChart(),
                        model = chartModel,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 3. List of Coins
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
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

@Composable
fun CoinItem(coin: GoldCoin, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(coin.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                // Profit/Loss (UPDATED)
                val profitString = if(coin.totalProfitOrLoss >= 0)
                    "+${coin.totalProfitOrLoss.toCurrencyString}"
                else
                    "-${abs(coin.totalProfitOrLoss).toCurrencyString}"

                Text(
                    text = profitString,
                    color = if(coin.totalProfitOrLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Coins: ${coin.quantity}")

            // Prices (UPDATED)
            Text("Bought at: ${coin.originalPrice.toCurrencyString}")
            Text(
                text = "Current Value: ${coin.totalCurrentValue.toCurrencyString}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ... AddCoinDialog remains the same ...
@Composable
fun AddCoinDialog(onDismiss: () -> Unit, onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Investment") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Coin Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (CHF)") }, // Label updated
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if(name.isNotEmpty() && price.isNotEmpty() && qty.isNotEmpty()) {
                    onAdd(name, price.toDouble(), qty.toDouble())
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}