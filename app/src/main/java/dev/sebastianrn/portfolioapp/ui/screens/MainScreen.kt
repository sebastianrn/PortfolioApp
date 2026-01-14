package dev.sebastianrn.portfolioapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sebastianrn.portfolioapp.R
import dev.sebastianrn.portfolioapp.data.model.AssetType
import dev.sebastianrn.portfolioapp.data.model.GoldAsset
import dev.sebastianrn.portfolioapp.data.model.PortfolioSummary
import dev.sebastianrn.portfolioapp.ui.shared.AssetSheet
import dev.sebastianrn.portfolioapp.ui.shared.PerformanceChartCard
import dev.sebastianrn.portfolioapp.ui.shared.PricePercentageChangeIndicator
import dev.sebastianrn.portfolioapp.ui.theme.GoldStart
import dev.sebastianrn.portfolioapp.ui.theme.LossRed
import dev.sebastianrn.portfolioapp.ui.theme.ProfitGreen
import dev.sebastianrn.portfolioapp.ui.theme.TextGray
import dev.sebastianrn.portfolioapp.util.formatCurrency
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.ThemeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    themeViewModel: ThemeViewModel,
    onCoinClick: (Int) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val isDark by themeViewModel.isDarkTheme.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val jsonString = viewModel.createBackupJson()
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(jsonString.toByteArray())
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup saved successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    // 2. Import Launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val sb = StringBuilder()
                    context.contentResolver.openInputStream(it)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).forEachLine { line ->
                            sb.append(line)
                        }
                    }

                    val success = viewModel.restoreFromBackupJson(sb.toString())

                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(
                                context,
                                "Database restored successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to parse backup file.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.updatePricesFromScraper() }) {
                        Icon(
                            Icons.Default.Refresh,
                            "Update Prices",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                "More Options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Export Backup",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    exportLauncher.launch("gold_portfolio_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Import Backup",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        "Generate Test Data",
//                                        color = MaterialTheme.colorScheme.onSurface
//                                    )
//                                },
//                                onClick = {
//                                    showMenu = false
//                                    viewModel.generateTestData(
//                                        assetCount = 12,
//                                        historyPerAsset = 45
//                                    )
//                                },
//                                leadingIcon = {
//                                    Icon(
//                                        imageVector = Icons.Default.Build,
//                                        contentDescription = null,
//                                        tint = GoldStart
//                                    )
//                                }
//                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Update Prices (Scrape)",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.updatePricesFromScraper()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            )
                        }
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.weight(1f)
            ) {
                item { PortfolioSummaryCard(stats, viewModel) }

                item {
                    PerformanceChartCard(
                        title = stringResource(R.string.performance_title),
                        portfolioPoints,
                        fallbackText = stringResource(R.string.empty_assets_list)
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldStart,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.your_assets),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(items = assets, key = { it.id }) { asset ->
                    AssetItem(
                        asset,
                        onClick = { onCoinClick(asset.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AssetSheet(
            onDismiss = { showDialog = false },
            onSave = { asset ->
                // CHANGE: Use 'insert' instead of 'addAsset' to ensure history is created
                viewModel.insertAsset(
                    name = asset.name,
                    type = asset.type,
                    purchasePrice = asset.purchasePrice,
                    buyPrice = asset.currentBuyPrice,
                    qty = asset.quantity,
                    weight = asset.weightInGrams,
                    philoroId = asset.philoroId
                )
                showDialog = false
            },
        )
    }
}

@Composable
fun PortfolioSummaryCard(stats: PortfolioSummary, viewModel: GoldViewModel) {
    val dailyChange by viewModel.portfolioChange.collectAsState()

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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Value
                Column {
                    Text(
                        text = stringResource(R.string.total_portfolio_value),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = stats.totalValue.formatCurrency(),
                        color = GoldStart,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                }
                PricePercentageChangeIndicator(
                    amount = dailyChange.first,
                    percent = dailyChange.second,
                    priceTypeString = stringResource(R.string.daily_change)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.invested_capital),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = stats.totalInvested.formatCurrency(),
                        color = GoldStart,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                val returnPercentage = if (stats.totalInvested > 0) {
                    (stats.totalProfit / stats.totalInvested) * 100
                } else {
                    0.0
                }

                PricePercentageChangeIndicator(
                    amount = stats.totalProfit,
                    percent = returnPercentage,
                    priceTypeString = stringResource(R.string.total_return)
                )
            }
        }
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
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
                Text(
                    text = iconText,
                    color = GoldStart,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    asset.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${asset.type.name} • ${asset.quantity} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    asset.totalCurrentValue.formatCurrency(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val isProfit = asset.totalProfitOrLoss >= 0
                val color = if (isProfit) ProfitGreen else LossRed

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = color
                    )
                    Text(
                        text = abs(asset.totalProfitOrLoss).formatCurrency(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(asset: GoldAsset, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_title)) },
        text = { Text(stringResource(R.string.delete_message, asset.name)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LossRed,
                    contentColor = Color.White
                )
            ) { Text(stringResource(R.string.delete_action)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
            ) { Text(stringResource(R.string.cancel_action)) }
        })
}