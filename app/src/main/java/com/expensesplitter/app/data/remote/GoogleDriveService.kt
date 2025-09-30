package com.expensesplitter.app.data.remote

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveService @Inject constructor(
    private val googleApiClient: GoogleApiClient
) {
    
    companion object {
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
        private const val ROOT_FOLDER_NAME = "ExpenseSplitter"
        private const val GROUPS_FOLDER_NAME = "Groups"
    }
    
    /**
     * Get or create the root ExpenseSplitter folder
     */
    suspend fun getOrCreateRootFolder(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            // Search for existing folder
            val result = service.files().list()
                .setQ("name='$ROOT_FOLDER_NAME' and mimeType='$FOLDER_MIME_TYPE' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            
            if (result.files.isNotEmpty()) {
                return@withContext Result.success(result.files[0].id)
            }
            
            // Create folder if doesn't exist
            val folderMetadata = File().apply {
                name = ROOT_FOLDER_NAME
                mimeType = FOLDER_MIME_TYPE
            }
            
            val folder = service.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            Result.success(folder.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get or create the Groups folder inside root folder
     */
    suspend fun getOrCreateGroupsFolder(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            val rootFolderResult = getOrCreateRootFolder()
            if (rootFolderResult.isFailure) {
                return@withContext rootFolderResult
            }
            
            val rootFolderId = rootFolderResult.getOrNull()!!
            
            // Search for Groups folder
            val result = service.files().list()
                .setQ("name='$GROUPS_FOLDER_NAME' and '$rootFolderId' in parents and mimeType='$FOLDER_MIME_TYPE' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()
            
            if (result.files.isNotEmpty()) {
                return@withContext Result.success(result.files[0].id)
            }
            
            // Create Groups folder
            val folderMetadata = File().apply {
                name = GROUPS_FOLDER_NAME
                mimeType = FOLDER_MIME_TYPE
                parents = listOf(rootFolderId)
            }
            
            val folder = service.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            Result.success(folder.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Move spreadsheet to Groups folder
     */
    suspend fun moveSpreadsheetToGroupsFolder(spreadsheetId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            val groupsFolderResult = getOrCreateGroupsFolder()
            if (groupsFolderResult.isFailure) {
                return@withContext Result.failure(groupsFolderResult.exceptionOrNull()!!)
            }
            
            val groupsFolderId = groupsFolderResult.getOrNull()!!
            
            // Get current parents
            val file = service.files().get(spreadsheetId)
                .setFields("parents")
                .execute()
            
            val previousParents = file.parents?.joinToString(",") ?: ""
            
            // Move to Groups folder
            service.files().update(spreadsheetId, null)
                .setAddParents(groupsFolderId)
                .setRemoveParents(previousParents)
                .setFields("id, parents")
                .execute()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload receipt image to Drive
     */
    suspend fun uploadReceipt(
        fileContent: java.io.File,
        fileName: String,
        mimeType: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            val groupsFolderResult = getOrCreateGroupsFolder()
            if (groupsFolderResult.isFailure) {
                return@withContext Result.failure(groupsFolderResult.exceptionOrNull()!!)
            }
            
            val groupsFolderId = groupsFolderResult.getOrNull()!!
            
            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(groupsFolderId)
            }
            
            val mediaContent = FileContent(mimeType, fileContent)
            
            val file = service.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute()
            
            Result.success(file.webViewLink ?: file.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete file from Drive
     */
    suspend fun deleteFile(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            service.files().delete(fileId).execute()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download file content
     */
    suspend fun downloadFile(fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            val outputStream = ByteArrayOutputStream()
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            
            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * List all spreadsheets in Groups folder
     */
    suspend fun listGroupSpreadsheets(): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val service = googleApiClient.driveService
                ?: return@withContext Result.failure(Exception("Drive service not initialized"))
            
            val groupsFolderResult = getOrCreateGroupsFolder()
            if (groupsFolderResult.isFailure) {
                return@withContext Result.failure(groupsFolderResult.exceptionOrNull()!!)
            }
            
            val groupsFolderId = groupsFolderResult.getOrNull()!!
            
            val result = service.files().list()
                .setQ("'$groupsFolderId' in parents and mimeType='application/vnd.google-apps.spreadsheet' and trashed=false")
                .setSpaces("drive")
                .setFields("files(id, name, createdTime, modifiedTime)")
                .setOrderBy("modifiedTime desc")
                .execute()
            
            Result.success(result.files ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
