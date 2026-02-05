package dev.sebastianrn.portfolioapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.backup.BackupFile
import dev.sebastianrn.portfolioapp.backup.BackupFrequency
import dev.sebastianrn.portfolioapp.backup.BackupManager
import dev.sebastianrn.portfolioapp.backup.BackupSettings
import dev.sebastianrn.portfolioapp.backup.BackupWorker
import dev.sebastianrn.portfolioapp.backup.CloudBackupFile
import dev.sebastianrn.portfolioapp.backup.DriveFolder
import dev.sebastianrn.portfolioapp.backup.GoogleDriveService
import dev.sebastianrn.portfolioapp.data.local.AppDatabase
import dev.sebastianrn.portfolioapp.data.model.BackupData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class BackupViewModel(
    application: Application,
    private val googleDriveService: GoogleDriveService
) : AndroidViewModel(application) {

    private val backupManager = BackupManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.goldAssetDao()

    val backupSettings: StateFlow<BackupSettings> = backupManager.backupSettings
        .stateIn(viewModelScope, SharingStarted.Lazily, BackupSettings())

    // Backup files state
    private val _backupFiles = MutableStateFlow<List<BackupFile>>(emptyList())
    val backupFiles: StateFlow<List<BackupFile>> = _backupFiles.asStateFlow()

    // Operation state
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    // Google Drive state
    val isSignedIn: StateFlow<Boolean> = googleDriveService.isSignedIn
    val userEmail: StateFlow<String?> = googleDriveService.userEmail

    private val _cloudBackupFiles = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackupFiles: StateFlow<List<CloudBackupFile>> = _cloudBackupFiles.asStateFlow()

    private val _isCloudLoading = MutableStateFlow(false)
    val isCloudLoading: StateFlow<Boolean> = _isCloudLoading.asStateFlow()

    val selectedFolder: StateFlow<DriveFolder?> = googleDriveService.selectedFolder

    private val _availableFolders = MutableStateFlow<List<DriveFolder>>(emptyList())
    val availableFolders: StateFlow<List<DriveFolder>> = _availableFolders.asStateFlow()

    fun setBackupFrequency(frequency: BackupFrequency) {
        viewModelScope.launch {
            backupManager.setBackupFrequency(frequency)
            if (frequency != BackupFrequency.MANUAL) {
                backupManager.setBackupEnabled(true)
                BackupWorker.schedule(getApplication(), frequency)
            } else {
                backupManager.setBackupEnabled(false)
                BackupWorker.cancel(getApplication())
            }
        }
    }

    fun backupNow() {
        viewModelScope.launch(Dispatchers.IO) {
            _isBackingUp.value = true
            try {
                val assets = dao.getAllAssetsOnce()
                val history = dao.getAllHistoryOnce()

                val backupData = BackupData(
                    assets = assets,
                    history = history
                )

                val jsonContent = Gson().toJson(backupData)
                val result = backupManager.saveBackup(jsonContent)

                result.onSuccess {
                    backupManager.updateLastBackup("Success")
                    backupManager.deleteOldBackups(10)
                    showToast("Backup completed successfully")
                    loadBackupFiles()
                }.onFailure { error ->
                    backupManager.updateLastBackup("Failed: ${error.message}")
                    showToast("Backup failed: ${error.message}")
                }
            } catch (e: Exception) {
                backupManager.updateLastBackup("Failed: ${e.message}")
                showToast("Backup failed: ${e.message}")
            } finally {
                _isBackingUp.value = false
            }
        }
    }

    fun loadBackupFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _backupFiles.value = backupManager.getBackupFiles()
        }
    }

    fun restoreBackup(file: BackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRestoring.value = true
            try {
                val result = backupManager.readBackup(file.path)
                result.onSuccess { jsonContent ->
                    val backup = Gson().fromJson(jsonContent, BackupData::class.java)
                    if (backup.assets.isNotEmpty()) {
                        dao.restoreDatabase(backup.assets, backup.history)
                        showToast("Backup restored successfully")
                    } else {
                        showToast("Backup file is empty or invalid")
                    }
                }.onFailure { error ->
                    showToast("Failed to restore backup: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Failed to restore backup: ${e.message}")
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun restoreFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRestoring.value = true
            try {
                val result = backupManager.readBackupFromUri(uri)
                result.onSuccess { jsonContent ->
                    val backup = Gson().fromJson(jsonContent, BackupData::class.java)
                    if (backup != null && backup.assets.isNotEmpty()) {
                        dao.restoreDatabase(backup.assets, backup.history)
                        showToast("Backup restored successfully")
                    } else {
                        showToast("Backup file is empty or invalid")
                    }
                }.onFailure { error ->
                    showToast("Failed to restore backup: ${error.message}")
                }
            } catch (e: Exception) {
                showToast("Failed to restore backup: ${e.message}")
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun deleteBackup(file: BackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (backupManager.deleteBackup(file.path)) {
                showToast("Backup deleted")
                loadBackupFiles()
            } else {
                showToast("Failed to delete backup")
            }
        }
    }

    fun shareBackup(file: BackupFile): Intent? {
        return try {
            val backupFile = File(file.path)
            if (!backupFile.exists()) {
                showToast("Backup file not found")
                return null
            }

            val context = getApplication<Application>()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Portfolio Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            showToast("Failed to share backup: ${e.message}")
            null
        }
    }

    // Google Drive operations

    fun getSignInIntent(): Intent = googleDriveService.getSignInIntent()

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch(Dispatchers.IO) {
            googleDriveService.handleSignInResult(data)
                .onSuccess { email ->
                    showToast("Signed in as $email")
                    loadCloudBackups()
                }
                .onFailure { error ->
                    showToast("Sign in failed: ${error.message}")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            googleDriveService.signOut()
            _cloudBackupFiles.value = emptyList()
            _availableFolders.value = emptyList()
            showToast("Signed out from Google Drive")
        }
    }

    fun loadFolders() {
        if (!isSignedIn.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                googleDriveService.listFolders()
                    .onSuccess { folders ->
                        _availableFolders.value = folders
                    }
                    .onFailure { error ->
                        showToast("Failed to load folders: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun selectFolder(folder: DriveFolder) {
        googleDriveService.setSelectedFolder(folder)
        loadCloudBackups()
    }

    fun createAndSelectFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                googleDriveService.createFolder(folderName)
                    .onSuccess { folder ->
                        googleDriveService.setSelectedFolder(folder)
                        loadFolders()
                        showToast("Folder created: ${folder.name}")
                    }
                    .onFailure { error ->
                        showToast("Failed to create folder: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun loadCloudBackups() {
        if (!isSignedIn.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                googleDriveService.listBackups()
                    .onSuccess { files ->
                        _cloudBackupFiles.value = files
                    }
                    .onFailure { error ->
                        showToast("Failed to load cloud backups: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun uploadToCloud() {
        if (!isSignedIn.value) {
            showToast("Please sign in to Google Drive first")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                val assets = dao.getAllAssetsOnce()
                val history = dao.getAllHistoryOnce()

                val backupData = BackupData(
                    assets = assets,
                    history = history
                )

                val jsonContent = Gson().toJson(backupData)
                val fileName = backupManager.generateBackupFileName()

                googleDriveService.uploadBackup(jsonContent, fileName)
                    .onSuccess {
                        backupManager.updateLastCloudBackup("Success")
                        showToast("Uploaded to Google Drive")
                        loadCloudBackups()
                    }
                    .onFailure { error ->
                        backupManager.updateLastCloudBackup("Failed: ${error.message}")
                        showToast("Cloud upload failed: ${error.message}")
                    }
            } catch (e: Exception) {
                backupManager.updateLastCloudBackup("Failed: ${e.message}")
                showToast("Cloud upload failed: ${e.message}")
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun restoreFromCloud(cloudFile: CloudBackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _isRestoring.value = true
            try {
                googleDriveService.downloadBackup(cloudFile.id)
                    .onSuccess { jsonContent ->
                        val backup = Gson().fromJson(jsonContent, BackupData::class.java)
                        if (backup != null && backup.assets.isNotEmpty()) {
                            dao.restoreDatabase(backup.assets, backup.history)
                            showToast("Backup restored from cloud")
                        } else {
                            showToast("Cloud backup is empty or invalid")
                        }
                    }
                    .onFailure { error ->
                        showToast("Failed to restore from cloud: ${error.message}")
                    }
            } catch (e: Exception) {
                showToast("Failed to restore from cloud: ${e.message}")
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun downloadFromCloud(cloudFile: CloudBackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                googleDriveService.downloadBackup(cloudFile.id)
                    .onSuccess { jsonContent ->
                        backupManager.saveBackup(jsonContent)
                        showToast("Downloaded to local storage")
                        loadBackupFiles()
                    }
                    .onFailure { error ->
                        showToast("Download failed: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun deleteFromCloud(cloudFile: CloudBackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                googleDriveService.deleteBackup(cloudFile.id)
                    .onSuccess {
                        showToast("Deleted from Google Drive")
                        loadCloudBackups()
                    }
                    .onFailure { error ->
                        showToast("Failed to delete: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    fun uploadLocalBackupToCloud(file: BackupFile) {
        if (!isSignedIn.value) {
            showToast("Please sign in to Google Drive first")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isCloudLoading.value = true
            try {
                backupManager.readBackup(file.path)
                    .onSuccess { jsonContent ->
                        googleDriveService.uploadBackup(jsonContent, file.name)
                            .onSuccess {
                                showToast("Uploaded to Google Drive")
                                loadCloudBackups()
                            }
                            .onFailure { error ->
                                showToast("Upload failed: ${error.message}")
                            }
                    }
                    .onFailure { error ->
                        showToast("Failed to read backup: ${error.message}")
                    }
            } finally {
                _isCloudLoading.value = false
            }
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
