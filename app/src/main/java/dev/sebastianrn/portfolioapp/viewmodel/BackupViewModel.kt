package dev.sebastianrn.portfolioapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.sebastianrn.portfolioapp.backup.BackupFile
import dev.sebastianrn.portfolioapp.backup.BackupFrequency
import dev.sebastianrn.portfolioapp.backup.BackupManager
import dev.sebastianrn.portfolioapp.backup.BackupSerializer
import dev.sebastianrn.portfolioapp.backup.BackupSettings
import dev.sebastianrn.portfolioapp.backup.BackupWorker
import dev.sebastianrn.portfolioapp.data.repository.GoldRepository
import dev.sebastianrn.portfolioapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class BackupViewModel(
    application: Application,
    private val repository: GoldRepository,
    private val backupManager: BackupManager
) : AndroidViewModel(application) {

    // One-time UI events channel (consistent with GoldViewModel pattern)
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
                val assets = repository.getAllAssetsOnce()
                val history = repository.getAllHistoryOnce()

                val jsonContent = BackupSerializer.serialize(assets, history)
                val result = backupManager.saveBackup(jsonContent)

                result.onSuccess {
                    backupManager.updateLastBackup("Success")
                    backupManager.deleteOldBackups(Constants.MAX_BACKUP_FILES)
                    sendEvent(UiEvent.ShowToast("Backup completed successfully"))
                    loadBackupFiles()
                }.onFailure { error ->
                    backupManager.updateLastBackup("Failed: ${error.message}")
                    sendEvent(UiEvent.ShowToast("Backup failed: ${error.message}"))
                }
            } catch (e: Exception) {
                backupManager.updateLastBackup("Failed: ${e.message}")
                sendEvent(UiEvent.ShowToast("Backup failed: ${e.message}"))
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
                    restoreFromJson(jsonContent)
                }.onFailure { error ->
                    sendEvent(UiEvent.ShowToast("Failed to restore backup: ${error.message}"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowToast("Failed to restore backup: ${e.message}"))
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
                    restoreFromJson(jsonContent)
                }.onFailure { error ->
                    sendEvent(UiEvent.ShowToast("Failed to restore backup: ${error.message}"))
                }
            } catch (e: Exception) {
                sendEvent(UiEvent.ShowToast("Failed to restore backup: ${e.message}"))
            } finally {
                _isRestoring.value = false
            }
        }
    }

    fun deleteBackup(file: BackupFile) {
        viewModelScope.launch(Dispatchers.IO) {
            if (backupManager.deleteBackup(file.path)) {
                sendEvent(UiEvent.ShowToast("Backup deleted"))
                loadBackupFiles()
            } else {
                sendEvent(UiEvent.ShowToast("Failed to delete backup"))
            }
        }
    }

    fun shareBackup(file: BackupFile): Intent? {
        return try {
            val backupFile = File(file.path)
            if (!backupFile.exists()) {
                sendEvent(UiEvent.ShowToast("Backup file not found"))
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
            sendEvent(UiEvent.ShowToast("Failed to share backup: ${e.message}"))
            null
        }
    }

    // --- Private Helpers ---

    /**
     * Shared restore logic used by both [restoreBackup] and [restoreFromUri].
     */
    private suspend fun restoreFromJson(jsonContent: String) {
        val backup = BackupSerializer.deserialize(jsonContent)
        if (backup != null && backup.assets.isNotEmpty()) {
            repository.restoreDatabase(backup.assets, backup.history)
            sendEvent(UiEvent.ShowToast("Backup restored successfully"))
        } else {
            sendEvent(UiEvent.ShowToast("Backup file is empty or invalid"))
        }
    }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
