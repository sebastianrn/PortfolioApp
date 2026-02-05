package dev.sebastianrn.portfolioapp.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class DriveFolder(
    val id: String,
    val name: String
)

class GoogleDriveService(private val context: Context) {

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _selectedFolder = MutableStateFlow<DriveFolder?>(null)
    val selectedFolder: StateFlow<DriveFolder?> = _selectedFolder.asStateFlow()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    private var driveService: Drive? = null

    init {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        updateSignInState(account)
        loadSavedFolder()
    }

    private fun updateSignInState(account: GoogleSignInAccount?) {
        _isSignedIn.value = account != null && GoogleSignIn.hasPermissions(
            account,
            Scope(DriveScopes.DRIVE_FILE)
        )
        _userEmail.value = account?.email

        if (_isSignedIn.value && account != null) {
            initializeDriveService(account)
        } else {
            driveService = null
        }
    }

    private fun initializeDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = account.account
        }

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Portfolio App")
            .build()
    }

    private fun loadSavedFolder() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val folderId = prefs.getString(KEY_FOLDER_ID, null)
        val folderName = prefs.getString(KEY_FOLDER_NAME, null)
        if (folderId != null && folderName != null) {
            _selectedFolder.value = DriveFolder(folderId, folderName)
        }
    }

    private fun saveFolder(folder: DriveFolder?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (folder != null) {
                putString(KEY_FOLDER_ID, folder.id)
                putString(KEY_FOLDER_NAME, folder.name)
            } else {
                remove(KEY_FOLDER_ID)
                remove(KEY_FOLDER_NAME)
            }
            apply()
        }
    }

    fun setSelectedFolder(folder: DriveFolder?) {
        _selectedFolder.value = folder
        saveFolder(folder)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun handleSignInResult(data: Intent?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.await()
                updateSignInState(account)
                Result.success(account.email ?: "Unknown")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                googleSignInClient.signOut().await()
            } catch (_: Exception) {
                // Ignore errors during sign out
            }
            _isSignedIn.value = false
            _userEmail.value = null
            driveService = null
        }
    }

    suspend fun listFolders(): Result<List<DriveFolder>> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                val result = drive.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .setOrderBy("name")
                    .execute()

                val folders = result.files?.map { file ->
                    DriveFolder(id = file.id, name = file.name)
                } ?: emptyList()

                Result.success(folders)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createFolder(folderName: String): Result<DriveFolder> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                }

                val folder = drive.files().create(fileMetadata)
                    .setFields("id, name")
                    .execute()

                Result.success(DriveFolder(id = folder.id, name = folder.name))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun uploadBackup(content: String, fileName: String): Result<CloudBackupFile> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                val folderId = _selectedFolder.value?.id ?: return@withContext Result.failure(
                    Exception("No folder selected. Please select a folder first.")
                )

                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = fileName
                    parents = listOf(folderId)
                }

                val mediaContent = ByteArrayContent.fromString(
                    "application/json",
                    content
                )

                val file = drive.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, size, createdTime, modifiedTime")
                    .execute()

                Result.success(
                    CloudBackupFile(
                        id = file.id,
                        name = file.name,
                        size = file.getSize()?.toLong() ?: content.length.toLong(),
                        createdTime = file.createdTime?.value ?: System.currentTimeMillis(),
                        modifiedTime = file.modifiedTime?.value ?: System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun listBackups(): Result<List<CloudBackupFile>> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                val folderId = _selectedFolder.value?.id ?: return@withContext Result.failure(
                    Exception("No folder selected")
                )

                val result = drive.files().list()
                    .setQ("'$folderId' in parents and trashed=false")
                    .setSpaces("drive")
                    .setFields("files(id, name, size, createdTime, modifiedTime)")
                    .setOrderBy("modifiedTime desc")
                    .execute()

                val backupFiles = result.files
                    ?.filter { it.name.startsWith(BackupManager.BACKUP_FILE_PREFIX) }
                    ?.map { file ->
                        CloudBackupFile(
                            id = file.id,
                            name = file.name,
                            size = file.getSize()?.toLong() ?: 0,
                            createdTime = file.createdTime?.value ?: 0,
                            modifiedTime = file.modifiedTime?.value ?: 0
                        )
                    } ?: emptyList()

                Result.success(backupFiles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun downloadBackup(fileId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                val outputStream = ByteArrayOutputStream()
                drive.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream)

                Result.success(outputStream.toString(Charsets.UTF_8.name()))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteBackup(fileId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val drive = driveService ?: return@withContext Result.failure(
                    Exception("Not signed in to Google Drive")
                )

                drive.files().delete(fileId).execute()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "google_drive_prefs"
        private const val KEY_FOLDER_ID = "selected_folder_id"
        private const val KEY_FOLDER_NAME = "selected_folder_name"
    }
}
