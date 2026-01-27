package dev.sebastianrn.portfolioapp.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for BackupManager.
 * Tests backup file operations with real file I/O.
 */
@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private lateinit var context: Context
    private lateinit var backupManager: BackupManager
    private lateinit var backupDir: File

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        backupManager = BackupManager(context)
        backupDir = File(context.filesDir, "backups")
        
        // Reset settings to ensure a clean state
        backupManager.resetSettings()

        // Clear any existing backups before each test
        if (backupDir.exists()) {
            backupDir.listFiles()?.forEach { it.delete() }
        }
    }

    @After
    fun cleanup() {
        // Clean up after tests
        if (backupDir.exists()) {
            backupDir.listFiles()?.forEach { it.delete() }
        }
    }

    // --- Save Backup Tests ---

    @Test
    fun saveBackupCreatesFile() = runBlocking {
        val jsonContent = """{"assets":[],"history":[]}"""

        val result = backupManager.saveBackup(jsonContent)

        assertTrue(result.isSuccess)
        val files = backupManager.getBackupFiles()
        assertEquals(1, files.size)
    }

    @Test
    fun saveBackupCreatesBackupDirectory() = runBlocking {
        // Delete directory if it exists
        if (backupDir.exists()) {
            backupDir.deleteRecursively()
        }

        val jsonContent = """{"assets":[],"history":[]}"""

        backupManager.saveBackup(jsonContent)

        assertTrue(backupDir.exists())
        assertTrue(backupDir.isDirectory)
    }

    @Test
    fun saveBackupCreatesUniqueFilenames() = runBlocking {
        val jsonContent = """{"assets":[],"history":[]}"""

        backupManager.saveBackup(jsonContent)
        Thread.sleep(100) // Wait to ensure different timestamp
        backupManager.saveBackup(jsonContent)

        val files = backupManager.getBackupFiles()
        assertEquals(2, files.size)
        assertTrue(files[0].name != files[1].name)
    }

    @Test
    fun saveBackupFilenameFormat() = runBlocking {
        val jsonContent = """{"assets":[],"history":[]}"""

        val result = backupManager.saveBackup(jsonContent)

        assertTrue(result.isSuccess)
        val files = backupManager.getBackupFiles()
        assertTrue(files[0].name.startsWith("portfolio_backup_"))
        assertTrue(files[0].name.endsWith(".json"))
    }

    // --- Read Backup Tests ---

    @Test
    fun readBackupRetrievesContent() = runBlocking {
        val jsonContent = """{"assets":[{"id":1}],"history":[]}"""
        backupManager.saveBackup(jsonContent)
        val files = backupManager.getBackupFiles()

        val result = backupManager.readBackup(files[0].path)

        assertTrue(result.isSuccess)
        assertEquals(jsonContent, result.getOrNull())
    }

    @Test
    fun readBackupFailsForNonexistentFile() = runBlocking {
        val result = backupManager.readBackup("/nonexistent/path/backup.json")

        assertTrue(result.isFailure)
    }

    // --- Delete Backup Tests ---

    @Test
    fun deleteBackupRemovesFile() = runBlocking {
        val jsonContent = """{"assets":[],"history":[]}"""
        backupManager.saveBackup(jsonContent)
        val files = backupManager.getBackupFiles()
        assertEquals(1, files.size)

        val deleted = backupManager.deleteBackup(files[0].path)

        assertTrue(deleted)
        assertEquals(0, backupManager.getBackupFiles().size)
    }

    @Test
    fun deleteBackupReturnsFalseForNonexistent() = runBlocking {
        val deleted = backupManager.deleteBackup("/nonexistent/backup.json")

        assertFalse(deleted)
    }

    // --- Get Backup Files Tests ---

    @Test
    fun getBackupFilesReturnsEmptyWhenNone() = runBlocking {
        val files = backupManager.getBackupFiles()

        assertTrue(files.isEmpty())
    }

    @Test
    fun getBackupFilesReturnsAllBackups() = runBlocking {
        repeat(5) {
            backupManager.saveBackup("""{"test":$it}""")
            Thread.sleep(10)
        }

        val files = backupManager.getBackupFiles()

        assertEquals(5, files.size)
    }

    @Test
    fun getBackupFilesSortedByDateDescending() = runBlocking {
        backupManager.saveBackup("""{"test":1}""")
        Thread.sleep(10)
        backupManager.saveBackup("""{"test":2}""")
        Thread.sleep(10)
        backupManager.saveBackup("""{"test":3}""")

        val files = backupManager.getBackupFiles()

        // Most recent should be first
        for (i in 0 until files.size - 1) {
            assertTrue(files[i].modifiedTime >= files[i + 1].modifiedTime)
        }
    }

    @Test
    fun getBackupFilesIncludesMetadata() = runBlocking {
        val jsonContent = """{"assets":[{"id":1}],"history":[]}"""
        backupManager.saveBackup(jsonContent)

        val files = backupManager.getBackupFiles()
        val file = files[0]

        assertNotNull(file.name)
        assertNotNull(file.path)
        assertTrue(file.size > 0)
        assertTrue(file.modifiedTime > 0)
    }

    // --- Delete Old Backups Tests ---

    @Test
    fun deleteOldBackupsKeepsSpecifiedCount() = runBlocking {
        // Create 15 backups
        repeat(15) {
            backupManager.saveBackup("""{"test":$it}""")
            Thread.sleep(10)
        }
        assertEquals(15, backupManager.getBackupFiles().size)

        backupManager.deleteOldBackups(keepCount = 10)

        val remaining = backupManager.getBackupFiles()
        assertEquals(10, remaining.size)
    }

    @Test
    fun deleteOldBackupsKeepsNewest() = runBlocking {
        // Create backups with different content
        repeat(5) {
            backupManager.saveBackup("""{"index":$it}""")
            Thread.sleep(10)
        }

        backupManager.deleteOldBackups(keepCount = 2)

        val files = backupManager.getBackupFiles()
        assertEquals(2, files.size)

        // Verify the newest ones were kept
        val content1 = backupManager.readBackup(files[0].path).getOrNull()
        val content2 = backupManager.readBackup(files[1].path).getOrNull()

        // Most recent should have higher indices
        assertTrue(content1?.contains("index\":4") == true || content1?.contains("index\":3") == true)
    }

    @Test
    fun deleteOldBackupsDoesNothingWhenUnderLimit() = runBlocking {
        repeat(3) {
            backupManager.saveBackup("""{"test":$it}""")
            Thread.sleep(10)
        }

        backupManager.deleteOldBackups(keepCount = 10)

        assertEquals(3, backupManager.getBackupFiles().size)
    }

    // --- Settings Tests ---

    @Test
    fun setBackupFrequencyUpdatesSettings() = runBlocking {
        backupManager.setBackupFrequency(BackupFrequency.DAILY)

        val settings = backupManager.backupSettings.first()
        assertEquals(BackupFrequency.DAILY, settings.frequency)
    }

    @Test
    fun setBackupEnabledUpdatesSettings() = runBlocking {
        backupManager.setBackupEnabled(true)

        val settings = backupManager.backupSettings.first()
        assertTrue(settings.isEnabled)

        backupManager.setBackupEnabled(false)

        val updatedSettings = backupManager.backupSettings.first()
        assertFalse(updatedSettings.isEnabled)
    }

    @Test
    fun updateLastBackupUpdatesSettings() = runBlocking {
        backupManager.updateLastBackup("Success")

        val settings = backupManager.backupSettings.first()
        assertEquals("Success", settings.lastBackupStatus)
    }

    @Test
    fun defaultSettingsAreCorrect() = runBlocking {
        // Fresh settings
        val settings = backupManager.backupSettings.first()

        assertEquals(BackupFrequency.MANUAL, settings.frequency)
        assertFalse(settings.isEnabled)
    }

    // --- BackupFile Data Class Tests ---

    @Test
    fun backupFileContainsCorrectData() = runBlocking {
        val jsonContent = """{"test":"data"}"""
        backupManager.saveBackup(jsonContent)

        val files = backupManager.getBackupFiles()
        val file = files[0]

        assertTrue(file.name.contains("portfolio_backup"))
        assertTrue(File(file.path).exists())
        assertEquals(jsonContent.length.toLong(), file.size)
        assertTrue(System.currentTimeMillis() - file.modifiedTime < 5000) // Within 5 seconds
    }

    // --- Edge Cases ---

    @Test
    fun saveBackupWithEmptyContent() = runBlocking {
        val result = backupManager.saveBackup("")

        assertTrue(result.isSuccess)
        val files = backupManager.getBackupFiles()
        assertEquals(1, files.size)
        assertEquals(0L, files[0].size)
    }

    @Test
    fun saveBackupWithLargeContent() = runBlocking {
        val largeContent = buildString {
            append("{\"data\":[")
            repeat(1000) { i ->
                if (i > 0) append(",")
                append("{\"id\":$i,\"value\":\"test data item $i\"}")
            }
            append("]}")
        }

        val result = backupManager.saveBackup(largeContent)

        assertTrue(result.isSuccess)
        val readResult = backupManager.readBackup(backupManager.getBackupFiles()[0].path)
        assertEquals(largeContent, readResult.getOrNull())
    }

    @Test
    fun backupFilesFilterOnlyJsonFiles() = runBlocking {
        // Create a valid backup
        backupManager.saveBackup("""{"test":1}""")

        // Create a non-JSON file in the backup directory
        val nonJsonFile = File(backupDir, "not_a_backup.txt")
        nonJsonFile.writeText("This is not a backup")

        val files = backupManager.getBackupFiles()

        // Should only return the JSON backup
        assertEquals(1, files.size)
        assertTrue(files[0].name.endsWith(".json"))
    }
}
