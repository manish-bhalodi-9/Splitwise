package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val queueId: Long = 0,
    val operation: SyncOperation,
    val entityType: EntityType,
    val entityId: String,
    val data: String, // JSON serialized entity data
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val lastAttempt: Long = 0L,
    val errorMessage: String? = null,
    val status: QueueStatus = QueueStatus.PENDING
)

enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

enum class EntityType {
    EXPENSE,
    CATEGORY,
    SETTLEMENT,
    GROUP
}

enum class QueueStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
