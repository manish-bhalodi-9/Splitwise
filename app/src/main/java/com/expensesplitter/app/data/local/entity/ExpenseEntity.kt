package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.expensesplitter.app.data.local.converter.StringListConverter

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["categoryId"]),
        Index(value = ["date"]),
        Index(value = ["status"])
    ]
)
@TypeConverters(StringListConverter::class)
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String,
    val groupId: String,
    val date: Long, // Unix timestamp
    val description: String,
    val amount: Double,
    val currency: String = "INR",
    val categoryId: String,
    val paidBy: String, // User ID or email
    val splitType: SplitType,
    val splitWith: List<String> = emptyList(), // List of user IDs/emails
    val splitDetails: String? = null, // JSON string with split details
    val notes: String? = null,
    val receiptUrls: List<String> = emptyList(), // Google Drive URLs
    val tags: List<String> = emptyList(),
    val status: ExpenseStatus = ExpenseStatus.ACTIVE,
    val settledDate: Long? = null,
    val settlementNotes: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedBy: String? = null,
    val lastEditedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncTime: Long = 0L
)

enum class SplitType {
    EQUAL,
    EXACT_AMOUNTS,
    PERCENTAGES,
    SHARES
}

enum class ExpenseStatus {
    ACTIVE,
    SETTLED,
    DELETED
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    ERROR
}
