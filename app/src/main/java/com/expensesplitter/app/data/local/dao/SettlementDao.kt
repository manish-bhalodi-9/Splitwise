package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.SettlementEntity
import com.expensesplitter.app.data.local.entity.SettlementStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    
    @Query("SELECT * FROM settlements WHERE settlementId = :settlementId")
    suspend fun getSettlementById(settlementId: String): SettlementEntity?
    
    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY date DESC")
    fun getSettlementsByGroup(groupId: String): Flow<List<SettlementEntity>>
    
    @Query("SELECT * FROM settlements WHERE groupId = :groupId AND status = :status ORDER BY date DESC")
    fun getSettlementsByStatus(groupId: String, status: SettlementStatus): Flow<List<SettlementEntity>>
    
    @Query("SELECT * FROM settlements WHERE groupId = :groupId AND (fromUser = :userId OR toUser = :userId) ORDER BY date DESC")
    fun getSettlementsByUser(groupId: String, userId: String): Flow<List<SettlementEntity>>
    
    @Query("SELECT * FROM settlements WHERE groupId = :groupId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSettlementsByDateRange(groupId: String, startDate: Long, endDate: Long): Flow<List<SettlementEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlements(settlements: List<SettlementEntity>)
    
    @Update
    suspend fun updateSettlement(settlement: SettlementEntity)
    
    @Delete
    suspend fun deleteSettlement(settlement: SettlementEntity)
    
    @Query("UPDATE settlements SET status = :status WHERE settlementId = :settlementId")
    suspend fun updateSettlementStatus(settlementId: String, status: SettlementStatus)
    
    @Query("SELECT SUM(amount) FROM settlements WHERE groupId = :groupId AND fromUser = :userId AND status = 'COMPLETED'")
    suspend fun getTotalPaidByUser(groupId: String, userId: String): Double?
    
    @Query("SELECT SUM(amount) FROM settlements WHERE groupId = :groupId AND toUser = :userId AND status = 'COMPLETED'")
    suspend fun getTotalReceivedByUser(groupId: String, userId: String): Double?
}
