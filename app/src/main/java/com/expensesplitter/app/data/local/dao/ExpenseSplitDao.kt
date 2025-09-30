package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.ExpenseSplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseSplitDao {
    
    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getSplitsByExpenseId(expenseId: String): List<ExpenseSplitEntity>
    
    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    fun getSplitsByExpenseIdFlow(expenseId: String): Flow<List<ExpenseSplitEntity>>
    
    @Query("SELECT * FROM expense_splits WHERE userId = :userId")
    fun getSplitsByUserId(userId: String): Flow<List<ExpenseSplitEntity>>
    
    @Query("SELECT * FROM expense_splits WHERE userId = :userId AND isPaid = 0")
    fun getUnpaidSplitsByUserId(userId: String): Flow<List<ExpenseSplitEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplit(split: ExpenseSplitEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplits(splits: List<ExpenseSplitEntity>)
    
    @Update
    suspend fun updateSplit(split: ExpenseSplitEntity)
    
    @Delete
    suspend fun deleteSplit(split: ExpenseSplitEntity)
    
    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteSplitsByExpenseId(expenseId: String)
    
    @Query("UPDATE expense_splits SET isPaid = 1, paidDate = :paidDate WHERE splitId = :splitId")
    suspend fun markSplitAsPaid(splitId: Long, paidDate: Long)
    
    @Query("SELECT SUM(amount) FROM expense_splits WHERE userId = :userId AND isPaid = 0")
    fun getTotalUnpaidAmount(userId: String): Flow<Double?>
}
