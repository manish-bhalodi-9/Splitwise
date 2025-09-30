package com.expensesplitter.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["expenseId"]), Index(value = ["userId"])]
)
data class ExpenseSplitEntity(
    @PrimaryKey(autoGenerate = true)
    val splitId: Long = 0,
    val expenseId: String,
    val userId: String, // User ID or email
    val amount: Double,
    val percentage: Double? = null,
    val shares: Int? = null,
    val isPaid: Boolean = false,
    val paidDate: Long? = null
)
