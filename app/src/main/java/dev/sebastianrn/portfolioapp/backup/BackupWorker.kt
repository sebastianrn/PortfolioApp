package dev.sebastianrn.portfolioapp.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dev.sebastianrn.portfolioapp.data.local.AppDatabase
import dev.sebastianrn.portfolioapp.data.model.BackupData
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

            // Get backup data
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.goldAssetDao()
            val assets = dao.getAllAssetsOnce()
            val history = dao.getAllHistoryOnce()

            val backupData = BackupData(
                assets = assets,
                history = history
            )

            val jsonContent = Gson().toJson(backupData)

            // Save to local storage
            val result = backupManager.saveBackup(jsonContent)

            if (result.isSuccess) {
                backupManager.updateLastBackup("Success")
                // Clean up old backups, keep last 10
                backupManager.deleteOldBackups(10)
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
