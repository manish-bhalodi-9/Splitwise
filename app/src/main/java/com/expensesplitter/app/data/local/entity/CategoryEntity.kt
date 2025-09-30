package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["name"])]
)
data class CategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val groupId: String? = null, // null for default/global categories
    val name: String,
    val icon: String, // Emoji or icon identifier
    val color: String, // Hex color code
    val isActive: Boolean = true,
    val isDefault: Boolean = false,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
