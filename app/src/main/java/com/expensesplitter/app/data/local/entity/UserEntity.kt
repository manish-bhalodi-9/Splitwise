package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String, // Google user ID
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val authToken: String?,
    val lastSyncTime: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
