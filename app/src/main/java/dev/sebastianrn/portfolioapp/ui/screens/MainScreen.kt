package dev.sebastianrn.portfolioapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import dev.sebastianrn.portfolioapp.backup.CloudBackupFile
import dev.sebastianrn.portfolioapp.backup.DriveFolder
import dev.sebastianrn.portfolioapp.ui.components.cards.AssetCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PerformanceCard
import dev.sebastianrn.portfolioapp.ui.components.cards.PortfolioSummaryCard
import dev.sebastianrn.portfolioapp.ui.components.common.AddAssetFab
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
    onAssetClick: (Int) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val assets by viewModel.allAssets.collectAsState()
    val stats by viewModel.portfolioStats.collectAsState()
    val portfolioPoints by viewModel.portfolioCurve.collectAsState()
    val dailyChange by viewModel.portfolioChange.collectAsState()

    // Backup state
    val backupSettings by backupViewModel.backupSettings.collectAsState()
    val backupFiles by backupViewModel.backupFiles.collectAsState()
    val cloudBackupFiles by backupViewModel.cloudBackupFiles.collectAsState()
    val isSignedIn by backupViewModel.isSignedIn.collectAsState()
    val userEmail by backupViewModel.userEmail.collectAsState()
    val isCloudLoading by backupViewModel.isCloudLoading.collectAsState()
    val selectedFolder by backupViewModel.selectedFolder.collectAsState()
    val availableFolders by backupViewModel.availableFolders.collectAsState()

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

    var showAssetSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showBackupSettings by remember { mutableStateOf(false) }
    var showBackupList by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf<BackupFile?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<BackupFile?>(null) }
    var showImportConfirm by remember { mutableStateOf<Uri?>(null) }
    var showCloudRestoreConfirm by remember { mutableStateOf<CloudBackupFile?>(null) }
    var showCloudDeleteConfirm by remember { mutableStateOf<CloudBackupFile?>(null) }

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
            AddAssetFab(onClick = { showAssetSheet = true })
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
            isSignedIn = isSignedIn,
            userEmail = userEmail,
            selectedFolder = selectedFolder,
            availableFolders = availableFolders,
            isCloudLoading = isCloudLoading,
            onFrequencyChange = { frequency ->
                backupViewModel.setBackupFrequency(frequency)
            },
            onBackupNow = {
                backupViewModel.backupNow()
            },
            onViewBackups = {
                backupViewModel.loadBackupFiles()
                if (isSignedIn) {
                    backupViewModel.loadCloudBackups()
                }
                showBackupList = true
            },
            onGoogleSignIn = onGoogleSignIn,
            onGoogleSignOut = { backupViewModel.signOut() },
            onUploadToCloud = { backupViewModel.uploadToCloud() },
            onLoadFolders = { backupViewModel.loadFolders() },
            onSelectFolder = { folder -> backupViewModel.selectFolder(folder) },
            onCreateFolder = { folderName -> backupViewModel.createAndSelectFolder(folderName) },
            onDismiss = { showBackupSettings = false }
        )
    }

    // Backup List Sheet
    if (showBackupList) {
        BackupListSheet(
            backupFiles = backupFiles,
            cloudBackupFiles = cloudBackupFiles,
            isSignedIn = isSignedIn,
            isCloudLoading = isCloudLoading,
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
            onFileUploadToCloud = { file ->
                backupViewModel.uploadLocalBackupToCloud(file)
            },
            onCloudFileRestore = { cloudFile ->
                showBackupList = false
                showCloudRestoreConfirm = cloudFile
            },
            onCloudFileDownload = { cloudFile ->
                backupViewModel.downloadFromCloud(cloudFile)
            },
            onCloudFileDelete = { cloudFile ->
                showCloudDeleteConfirm = cloudFile
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

    // Cloud Restore Confirm Dialog
    showCloudRestoreConfirm?.let { cloudFile ->
        RestoreConfirmDialog(
            fileName = cloudFile.name,
            onConfirm = {
                backupViewModel.restoreFromCloud(cloudFile)
                showCloudRestoreConfirm = null
            },
            onDismiss = { showCloudRestoreConfirm = null }
        )
    }

    // Cloud Delete Confirm Dialog
    showCloudDeleteConfirm?.let { cloudFile ->
        DeleteConfirmDialog(
            onConfirm = {
                backupViewModel.deleteFromCloud(cloudFile)
                showCloudDeleteConfirm = null
            },
            onDismiss = { showCloudDeleteConfirm = null }
        )
    }
}
