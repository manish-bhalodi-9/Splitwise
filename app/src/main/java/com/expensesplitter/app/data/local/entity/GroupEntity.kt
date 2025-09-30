package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.expensesplitter.app.data.local.converter.StringListConverter

@Entity(tableName = "groups")
@TypeConverters(StringListConverter::class)
data class GroupEntity(
    @PrimaryKey
    val groupId: String,
    val groupName: String,
    val description: String? = null,
    val sheetId: String, // Google Sheets file ID
    val driveFileId: String, // Google Drive file ID
    val driveFolderId: String, // Parent folder ID
    val createdBy: String, // User ID
    val createdDate: Long = System.currentTimeMillis(),
    val members: List<String> = emptyList(), // List of user IDs/emails
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val lastSyncTime: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis()
)
