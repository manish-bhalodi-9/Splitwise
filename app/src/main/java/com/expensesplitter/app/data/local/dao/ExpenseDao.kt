package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.ExpenseEntity
import com.expensesplitter.app.data.local.entity.ExpenseStatus
import com.expensesplitter.app.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntity?
    
    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    fun getExpenseByIdFlow(expenseId: String): Flow<ExpenseEntity?>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND status != 'DELETED' ORDER BY date DESC")
    fun getExpensesByGroup(groupId: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND status = :status ORDER BY date DESC")
    fun getExpensesByGroupAndStatus(groupId: String, status: ExpenseStatus): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND date BETWEEN :startDate AND :endDate AND status != 'DELETED' ORDER BY date DESC")
    fun getExpensesByDateRange(groupId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND categoryId = :categoryId AND status != 'DELETED' ORDER BY date DESC")
    fun getExpensesByCategory(groupId: String, categoryId: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate AND status != 'DELETED' ORDER BY date DESC")
    suspend fun getExpensesByCategoryAndDateRange(
        groupId: String,
        categoryId: String,
        startDate: Long,
        endDate: Long
    ): List<ExpenseEntity>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND paidBy = :userId AND status != 'DELETED' ORDER BY date DESC")
    fun getExpensesByPaidBy(groupId: String, userId: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND (description LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%') AND status != 'DELETED' ORDER BY date DESC")
    fun searchExpenses(groupId: String, query: String): Flow<List<ExpenseEntity>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND status != 'DELETED' ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(groupId: String, limit: Int = 20): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId AND status = 'ACTIVE'")
    fun getTotalActiveExpenses(groupId: String): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId AND date BETWEEN :startDate AND :endDate AND status != 'DELETED'")
    suspend fun getTotalExpensesByDateRange(groupId: String, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate AND status != 'DELETED'")
    suspend fun getTotalExpensesByCategory(groupId: String, categoryId: String, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT categoryId, SUM(amount) as total FROM expenses WHERE groupId = :groupId AND date BETWEEN :startDate AND :endDate AND status != 'DELETED' GROUP BY categoryId ORDER BY total DESC")
    suspend fun getCategoryTotals(groupId: String, startDate: Long, endDate: Long): Map<String, Double>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    @Query("UPDATE expenses SET status = 'DELETED' WHERE expenseId = :expenseId")
    suspend fun softDeleteExpense(expenseId: String)
    
    @Query("UPDATE expenses SET status = :status, settledDate = :settledDate, settlementNotes = :notes WHERE expenseId = :expenseId")
    suspend fun updateExpenseStatus(expenseId: String, status: ExpenseStatus, settledDate: Long?, notes: String?)
    
    @Query("UPDATE expenses SET syncStatus = :syncStatus, lastSyncTime = :syncTime WHERE expenseId = :expenseId")
    suspend fun updateSyncStatus(expenseId: String, syncStatus: SyncStatus, syncTime: Long)
    
    @Query("SELECT * FROM expenses WHERE syncStatus = 'PENDING' OR syncStatus = 'ERROR'")
    suspend fun getUnsyncedExpenses(): List<ExpenseEntity>
    
    @Query("DELETE FROM expenses WHERE status = 'DELETED' AND date < :olderThan")
    suspend fun permanentlyDeleteOldExpenses(olderThan: Long)
}
