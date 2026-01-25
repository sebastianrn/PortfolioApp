package dev.sebastianrn.portfolioapp.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
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
import dev.sebastianrn.portfolioapp.ui.components.*
import dev.sebastianrn.portfolioapp.viewmodel.BackupViewModel
import dev.sebastianrn.portfolioapp.viewmodel.GoldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GoldViewModel,
    backupViewModel: BackupViewModel,
    onAssetClick: (Int) -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val dailyChange by viewModel.portfolioChange.collectAsState()

    // Backup state
    val backupSettings by backupViewModel.backupSettings.collectAsState()
    val backupFiles by backupViewModel.backupFiles.collectAsState()

    val context = LocalContext.current

    var showAssetSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBackupSettings by remember { mutableStateOf(false) }
    var showBackupList by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf<BackupFile?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<BackupFile?>(null) }

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
                ModernTopBar(
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
            FAB(onClick = { showAssetSheet = true })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Value Card
            item {
                PortfolioSummaryCard(
                    totalValue = stats.totalValue,
                    totalInvested = stats.totalInvested,
                    totalProfit = stats.totalValue - stats.totalInvested,
                    dailyChange = dailyChange.first,
                    dailyChangePercent = dailyChange.second,
                    pulseAlpha = pulseAlpha
                )
            }

            // Performance Chart
            item {
                PerformanceCard(points = portfolioPoints)
            }

            // Section Header
            item {
                Text(
                    "Your Assets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Asset Cards
            items(items = assets, key = { it.id }) { asset ->
                AssetCard(
                    asset = asset,
                    onAssetClick = { onAssetClick(asset.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
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
}
