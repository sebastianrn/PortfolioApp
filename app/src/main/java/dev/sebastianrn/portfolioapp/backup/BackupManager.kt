package dev.sebastianrn.portfolioapp.backup

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.backupDataStore by preferencesDataStore(name = "backup_settings")

class BackupManager(private val context: Context) {

    companion object {
        private val KEY_BACKUP_ENABLED = booleanPreferencesKey("backup_enabled")
        private val KEY_BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        private val KEY_LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        private val KEY_LAST_BACKUP_STATUS = stringPreferencesKey("last_backup_status")

        const val BACKUP_FILE_PREFIX = "portfolio_backup_"
        const val BACKUP_FILE_EXTENSION = ".json"
        const val BACKUP_FOLDER_NAME = "backups"
    }

    private val backupDir: File
        get() = File(context.filesDir, BACKUP_FOLDER_NAME).also {
            if (!it.exists()) it.mkdirs()
        }

    val backupSettings: Flow<BackupSettings> = context.backupDataStore.data.map { prefs ->
        BackupSettings(
            isEnabled = prefs[KEY_BACKUP_ENABLED] ?: false,
            frequency = BackupFrequency.fromString(prefs[KEY_BACKUP_FREQUENCY]),
            lastBackupTime = prefs[KEY_LAST_BACKUP_TIME],
            lastBackupStatus = prefs[KEY_LAST_BACKUP_STATUS]
        )
    }

    suspend fun setBackupEnabled(enabled: Boolean) {
        context.backupDataStore.edit { prefs ->
            prefs[KEY_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun setBackupFrequency(frequency: BackupFrequency) {
        context.backupDataStore.edit { prefs ->
            prefs[KEY_BACKUP_FREQUENCY] = frequency.name
        }
    }

    suspend fun updateLastBackup(status: String) {
        context.backupDataStore.edit { prefs ->
            prefs[KEY_LAST_BACKUP_TIME] = System.currentTimeMillis()
            prefs[KEY_LAST_BACKUP_STATUS] = status
        }
    }
    
    suspend fun resetSettings() {
        context.backupDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_SSS", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "$BACKUP_FILE_PREFIX$timestamp$BACKUP_FILE_EXTENSION"
    }

    fun saveBackup(jsonContent: String): Result<File> {
        return try {
            val fileName = generateBackupFileName()
            val file = File(backupDir, fileName)
            file.writeText(jsonContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBackupFiles(): List<BackupFile> {
        return backupDir.listFiles()
            ?.filter { it.name.startsWith(BACKUP_FILE_PREFIX) && it.name.endsWith(BACKUP_FILE_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                BackupFile(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    modifiedTime = file.lastModified()
                )
            } ?: emptyList()
    }

    fun readBackup(filePath: String): Result<String> {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                Result.success(file.readText())
            } else {
                Result.failure(Exception("Backup file not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readBackupFromUri(uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open file"))
            val content = inputStream.bufferedReader().use { it.readText() }
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteBackup(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun deleteOldBackups(keepCount: Int = 10) {
        val files = backupDir.listFiles()
            ?.filter { it.name.startsWith(BACKUP_FILE_PREFIX) }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (files.size > keepCount) {
            files.drop(keepCount).forEach { it.delete() }
        }
    }
}

data class BackupSettings(
    val isEnabled: Boolean = false,
    val frequency: BackupFrequency = BackupFrequency.MANUAL,
    val lastBackupTime: Long? = null,
    val lastBackupStatus: String? = null
)

data class BackupFile(
    val name: String,
    val path: String,
    val size: Long,
    val modifiedTime: Long
)

enum class BackupFrequency(val displayName: String, val intervalHours: Long) {
    MANUAL("Manual", 0),
    DAILY("Daily", 24),
    WEEKLY("Weekly", 168);

    companion object {
        fun fromString(value: String?): BackupFrequency {
            return entries.find { it.name == value } ?: MANUAL
        }
    }
}
