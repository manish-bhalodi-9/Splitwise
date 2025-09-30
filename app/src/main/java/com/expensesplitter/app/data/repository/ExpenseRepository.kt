package com.expensesplitter.app.data.repository

import com.expensesplitter.app.data.local.dao.ExpenseDao
import com.expensesplitter.app.data.local.dao.ExpenseSplitDao
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.data.local.entity.ExpenseSplitEntity
import com.expensesplitter.app.data.local.entity.ExpenseStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao
) {
    
    // Get expenses
    fun getExpensesByGroup(groupId: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByGroup(groupId)
    }
    
    fun getExpenseById(expenseId: String): Flow<ExpenseEntity?> {
        return expenseDao.getExpenseByIdFlow(expenseId)
    }
    
    fun getExpensesByDateRange(
        groupId: String, 
        startDate: Long, 
        endDate: Long
    ): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(groupId, startDate, endDate)
    }
    
    fun getExpensesByCategory(
        groupId: String, 
        categoryId: String
    ): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(groupId, categoryId)
    }
    
    suspend fun getExpensesByCategoryAndDateRange(
        groupId: String,
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): List<ExpenseEntity> {
        return expenseDao.getExpensesByCategoryAndDateRange(
            groupId, 
            categoryId, 
            startDate, 
            endDate
        )
    }
    
    // Add/Update expense
    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }
    
    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }
    
    // Delete expense (soft delete)
    suspend fun deleteExpense(expenseId: String) {
        expenseDao.softDeleteExpense(expenseId)
    }
    
    suspend fun markExpenseAsDeleted(expenseId: String) {
        expenseDao.softDeleteExpense(expenseId)
    }
    
    // Expense splits
    suspend fun getSplitsByExpenseId(expenseId: String): List<ExpenseSplitEntity> {
        return expenseSplitDao.getSplitsByExpenseId(expenseId)
    }
    
    fun getSplitsByExpenseIdFlow(expenseId: String): Flow<List<ExpenseSplitEntity>> {
        return expenseSplitDao.getSplitsByExpenseIdFlow(expenseId)
    }
    
    suspend fun insertSplit(split: ExpenseSplitEntity) {
        expenseSplitDao.insertSplit(split)
    }
    
    suspend fun insertSplits(splits: List<ExpenseSplitEntity>) {
        expenseSplitDao.insertSplits(splits)
    }
    
    suspend fun updateSplit(split: ExpenseSplitEntity) {
        expenseSplitDao.updateSplit(split)
    }
    
    suspend fun deleteSplitsByExpenseId(expenseId: String) {
        expenseSplitDao.deleteSplitsByExpenseId(expenseId)
    }
    
    // Statistics
    suspend fun getTotalExpensesByGroup(groupId: String): Double {
        val now = System.currentTimeMillis()
        val monthStart = com.expensesplitter.app.util.FormatUtils.getMonthStartTimestamp(now)
        val monthEnd = com.expensesplitter.app.util.FormatUtils.getMonthEndTimestamp(now)
        return expenseDao.getTotalExpensesByDateRange(groupId, monthStart, monthEnd) ?: 0.0
    }
    
    suspend fun getTotalExpensesByGroupAndDateRange(
        groupId: String,
        startDate: Long,
        endDate: Long
    ): Double {
        return expenseDao.getTotalExpensesByDateRange(groupId, startDate, endDate) ?: 0.0
    }
    
    suspend fun getTotalExpensesByCategory(
        groupId: String,
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): Double {
        return expenseDao.getTotalExpensesByCategory(groupId, categoryId, startDate, endDate) ?: 0.0
    }
    
    // Expense with splits (transaction)
    suspend fun insertExpenseWithSplits(
        expense: ExpenseEntity,
        splits: List<ExpenseSplitEntity>
    ) {
        expenseDao.insertExpense(expense)
        expenseSplitDao.insertSplits(splits)
    }
    
    suspend fun updateExpenseWithSplits(
        expense: ExpenseEntity,
        splits: List<ExpenseSplitEntity>
    ) {
        expenseDao.updateExpense(expense)
        expenseSplitDao.deleteSplitsByExpenseId(expense.expenseId)
        expenseSplitDao.insertSplits(splits)
    }
}
