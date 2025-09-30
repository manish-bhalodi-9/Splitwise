package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.AuditAction
import com.expensesplitter.app.data.local.entity.AuditLogEntity
import com.expensesplitter.app.data.local.entity.EntityType
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_log WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsByUser(userId: String): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_log WHERE entityType = :entityType AND entityId = :entityId ORDER BY timestamp DESC")
    suspend fun getLogsByEntity(entityType: EntityType, entityId: String): List<AuditLogEntity>
    
    @Query("SELECT * FROM audit_log WHERE action = :action ORDER BY timestamp DESC")
    fun getLogsByAction(action: AuditAction): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_log WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<AuditLogEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AuditLogEntity>)
    
    @Query("DELETE FROM audit_log WHERE timestamp < :olderThan")
    suspend fun deleteOldLogs(olderThan: Long)
    
    @Query("SELECT COUNT(*) FROM audit_log WHERE userId = :userId AND action = :action AND timestamp > :since")
    suspend fun getActionCount(userId: String, action: AuditAction, since: Long): Int
}
