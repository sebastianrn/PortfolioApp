package dev.sebastianrn.portfolioapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sebastianrn.portfolioapp.backup.BackupFile
import dev.sebastianrn.portfolioapp.ui.components.bottombar.MainTab
import dev.sebastianrn.portfolioapp.ui.components.cards.AssetCard
import dev.sebastianrn.portfolioapp.ui.components.cards.HistoricalStatsCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PerformanceCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PortfolioHistoryCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PortfolioSummaryCard
import dev.sebastianrn.portfolioapp.ui.components.common.AddAssetFab
import dev.sebastianrn.portfolioapp.util.formatCurrency
import dev.sebastianrn.portfolioapp.ui.components.dialogs.DeleteConfirmDialog
import dev.sebastianrn.portfolioapp.ui.components.dialogs.RestoreConfirmDialog
import dev.sebastianrn.portfolioapp.ui.components.sheets.AssetSheet
import dev.sebastianrn.portfolioapp.ui.components.sheets.BackupListSheet
import dev.sebastianrn.portfolioapp.ui.components.sheets.BackupSettingsSheet
import dev.sebastianrn.portfolioapp.ui.components.topbar.MainTopBar
import dev.sebastianrn.portfolioapp.viewmodel.BackupViewModel
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel
import dev.sebastianrn.portfolioapp.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    backupViewModel: BackupViewModel,
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onAssetClick: (Int) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val dailyChange by viewModel.portfolioChange.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val historicalStats by viewModel.historicalStats.collectAsState()

    // Backup state
    val backupSettings by backupViewModel.backupSettings.collectAsState()
    val backupFiles by backupViewModel.backupFiles.collectAsState()

    val context = LocalContext.current

    // Handle UI events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.ShowError -> {
                    Toast.makeText(context, "Error: ${event.error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var showAllHistory by remember { mutableStateOf(false) }
    var showAssetSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBackupSettings by remember { mutableStateOf(false) }
    var showBackupList by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf<BackupFile?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<BackupFile?>(null) }
    var showImportConfirm by remember { mutableStateOf<Uri?>(null) }

    // File picker for importing backup files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            showBackupList = false
            showImportConfirm = it
        }
    }

    // Animated value for progress indicators
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                MainTopBar(
                    title = selectedTab.label,
                    onRefreshClick = { viewModel.updatePricesFromScraper() },
                    onMenuClick = { showMenu = true }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 4.dp)
                ) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Backup Settings") },
                            onClick = {
                                showMenu = false
                                showBackupSettings = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Backup Now") },
                            onClick = {
                                showMenu = false
                                backupViewModel.backupNow()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Backup, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Restore Backup") },
                            onClick = {
                                showMenu = false
                                backupViewModel.loadBackupFiles()
                                showBackupList = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Restore, contentDescription = null)
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedTab == MainTab.Assets,
                enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                AddAssetFab(onClick = { showAssetSheet = true })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(300),
                label = "tab_content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { tab ->
                when (tab) {
                    MainTab.Portfolio -> {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                PortfolioSummaryCard(
                                    totalValue = stats.totalValue,
                                    totalInvested = stats.totalInvested,
                                    totalProfit = stats.totalValue - stats.totalInvested,
                                    dailyChange = dailyChange.first,
                                    dailyChangePercent = dailyChange.second,
                                    pulseAlpha = pulseAlpha,
                                    lastUpdated = lastUpdated
                                )
                            }
                            item {
                                PerformanceCard(points = portfolioPoints)
                            }
                            item {
                                HistoricalStatsCard(stats = historicalStats)
                            }

                            // Portfolio Value History
                            if (portfolioPoints.size >= 2) {
                                val reversed = portfolioPoints.reversed()
                                val maxPreview = 20
                                val hasMore = reversed.size > maxPreview
                                val displayList = if (showAllHistory) reversed else reversed.take(maxPreview)

                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Portfolio History",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        if (hasMore) {
                                            TextButton(onClick = { showAllHistory = !showAllHistory }) {
                                                Text(
                                                    if (showAllHistory) "Show less" else "Show all (${reversed.size})",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                items(displayList.size) { index ->
                                    val point = displayList[index]
                                    // Find the previous point in the full reversed list
                                    val fullIndex = if (showAllHistory) index else index
                                    val previousValue = if (fullIndex < reversed.size - 1) {
                                        reversed[fullIndex + 1].second
                                    } else {
                                        point.second
                                    }
                                    val change = point.second - previousValue
                                    val changePercent = if (previousValue != 0.0) {
                                        (change / previousValue) * 100
                                    } else 0.0

                                    PortfolioHistoryCard(
                                        timestamp = point.first,
                                        value = point.second,
                                        change = change,
                                        changePercent = changePercent
                                    )
                                }
                            }
                        }
                    }

                    MainTab.Assets -> {
                        val totalProfit = stats.totalValue - stats.totalInvested
                        val profitPercent = if (stats.totalInvested > 0) {
                            (totalProfit / stats.totalInvested) * 100
                        } else 0.0
                        val isProfitPositive = totalProfit >= 0

                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Compact portfolio summary
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Total Value",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                stats.totalValue.formatCurrency(),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = if (isProfitPositive)
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                            else
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${if (isProfitPositive) "+" else ""}${String.format("%.1f", profitPercent)}%",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isProfitPositive)
                                                    MaterialTheme.colorScheme.secondary
                                                else
                                                    MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }

                            // Section header + count
                            item {
                                Text(
                                    "Your Assets (${assets.size})",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                )
                            }
                            items(items = assets, key = { it.id }) { asset ->
                                AssetCard(
                                    asset = asset,
                                    onAssetClick = { onAssetClick(asset.id) }
                                )
                            }
                        }
                    }
                }
            }

        }
    }

    // Asset Sheet
    if (showAssetSheet) {
        AssetSheet(
            onDismiss = { showAssetSheet = false },
            onSave = { asset ->
                viewModel.insertAsset(
                    name = asset.name,
                    type = asset.type,
                    purchasePrice = asset.purchasePrice,
                    buyPrice = asset.currentBuyPrice,
                    qty = asset.quantity,
                    weight = asset.weightInGrams,
                    philoroId = asset.philoroId
                )
                showAssetSheet = false
            }
        )
    }

    // Backup Settings Sheet
    if (showBackupSettings) {
        BackupSettingsSheet(
            settings = backupSettings,
            onFrequencyChange = { frequency ->
                backupViewModel.setBackupFrequency(frequency)
            },
            onBackupNow = {
                backupViewModel.backupNow()
            },
            onViewBackups = {
                backupViewModel.loadBackupFiles()
                showBackupList = true
            },
            onDismiss = { showBackupSettings = false }
        )
    }

    // Backup List Sheet
    if (showBackupList) {
        BackupListSheet(
            backupFiles = backupFiles,
            onFileSelect = { file ->
                showBackupList = false
                showRestoreConfirm = file
            },
            onFileShare = { file ->
                backupViewModel.shareBackup(file)?.let { intent ->
                    context.startActivity(Intent.createChooser(intent, "Share Backup"))
                }
            },
            onFileDelete = { file ->
                showDeleteConfirm = file
            },
            onImportFromFile = {
                filePickerLauncher.launch(arrayOf("application/json", "*/*"))
            },
            onDismiss = { showBackupList = false }
        )
    }

    // Restore Confirm Dialog
    showRestoreConfirm?.let { file ->
        RestoreConfirmDialog(
            fileName = file.name,
            onConfirm = {
                backupViewModel.restoreBackup(file)
                showRestoreConfirm = null
            },
            onDismiss = { showRestoreConfirm = null }
        )
    }

    // Delete Confirm Dialog
    showDeleteConfirm?.let { file ->
        DeleteConfirmDialog(
            onConfirm = {
                backupViewModel.deleteBackup(file)
                showDeleteConfirm = null
            },
            onDismiss = { showDeleteConfirm = null }
        )
    }

    // Import Confirm Dialog
    showImportConfirm?.let { uri ->
        RestoreConfirmDialog(
            fileName = "imported file",
            onConfirm = {
                backupViewModel.restoreFromUri(uri)
                showImportConfirm = null
            },
            onDismiss = { showImportConfirm = null }
        )
    }
}
