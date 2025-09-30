package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.expensesplitter.app.data.local.converter.StringListConverter

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["fromUser"]),
        Index(value = ["toUser"]),
        Index(value = ["date"])
    ]
)
@TypeConverters(StringListConverter::class)
data class SettlementEntity(
    @PrimaryKey
    val settlementId: String,
    val groupId: String,
    val fromUser: String, // User ID or email who pays
    val toUser: String, // User ID or email who receives
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val relatedExpenseIds: List<String> = emptyList(),
    val status: SettlementStatus = SettlementStatus.COMPLETED,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val lastSyncTime: Long = 0L
)

enum class SettlementStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
