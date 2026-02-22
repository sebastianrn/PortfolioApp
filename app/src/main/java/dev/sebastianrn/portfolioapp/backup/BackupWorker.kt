package dev.sebastianrn.portfolioapp.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.sebastianrn.portfolioapp.data.local.AppDatabase
import dev.sebastianrn.portfolioapp.util.Constants
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "portfolio_backup_work"

        fun schedule(context: Context, frequency: BackupFrequency) {
            if (frequency == BackupFrequency.MANUAL) {
                cancel(context)
                return
            }

            val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                frequency.intervalHours, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val backupManager = BackupManager(applicationContext)
            val settings = backupManager.backupSettings.first()

            if (!settings.isEnabled) {
                return Result.success()
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.goldAssetDao()
            val assets = dao.getAllAssetsOnce()
            val history = dao.getAllHistoryOnce()

            val jsonContent = BackupSerializer.serialize(assets, history)

            val result = backupManager.saveBackup(jsonContent)

            if (result.isSuccess) {
                backupManager.updateLastBackup("Success")
                backupManager.deleteOldBackups(Constants.MAX_BACKUP_FILES)
                Result.success()
            } else {
                backupManager.updateLastBackup("Failed: ${result.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            BackupManager(applicationContext).updateLastBackup("Failed: ${e.message}")
            Result.failure()
        }
    }
}
