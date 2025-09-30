package com.expensesplitter.app.util

import com.expensesplitter.app.data.local.entity.CategoryEntity
import java.util.UUID

object CategoryDefaults {
    
    fun getDefaultCategories(): List<CategoryEntity> {
        return listOf(
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Food & Dining",
                icon = "🍽️",
                color = "#FF6B6B", // Red
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Groceries",
                icon = "🛒",
                color = "#4ECDC4", // Teal
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Transportation",
                icon = "🚗",
                color = "#45B7D1", // Blue
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Entertainment",
                icon = "🎬",
                color = "#96CEB4", // Green
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Utilities",
                icon = "💡",
                color = "#FFEAA7", // Yellow
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Healthcare",
                icon = "🏥",
                color = "#DFE6E9", // Gray
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Shopping",
                icon = "🛍️",
                color = "#A29BFE", // Purple
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Travel",
                icon = "✈️",
                color = "#74B9FF", // Light Blue
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Housing",
                icon = "🏠",
                color = "#FD79A8", // Pink
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Education",
                icon = "📚",
                color = "#FAB1A0", // Peach
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Insurance",
                icon = "🛡️",
                color = "#00B894", // Emerald
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Subscriptions",
                icon = "📱",
                color = "#6C5CE7", // Indigo
                isActive = true,
                isDefault = true
            ),
            CategoryEntity(
                categoryId = UUID.randomUUID().toString(),
                name = "Other",
                icon = "📦",
                color = "#636E72", // Dark Gray
                isActive = true,
                isDefault = true
            )
        )
    }
}
