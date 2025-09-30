package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audit_log",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["timestamp"]),
        Index(value = ["entityType"])
    ]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val logId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val action: AuditAction,
    val entityType: EntityType,
    val entityId: String,
    val details: String? = null, // JSON with before/after values
    val deviceInfo: String? = null
)

enum class AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    SETTLE,
    UNSETTLE,
    SYNC,
    LOGIN,
    LOGOUT
}
