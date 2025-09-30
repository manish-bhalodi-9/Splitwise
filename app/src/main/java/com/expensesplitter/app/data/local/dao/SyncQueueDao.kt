package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    
    @Query("SELECT * FROM sync_queue WHERE queueId = :queueId")
    suspend fun getQueueItemById(queueId: Long): SyncQueueEntity?
    
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE status = 'FAILED' AND retryCount < maxRetries ORDER BY timestamp ASC")
    suspend fun getRetryableItems(): List<SyncQueueEntity>
    
    @Query("SELECT * FROM sync_queue WHERE status IN ('PENDING', 'PROCESSING') ORDER BY timestamp ASC")
    fun getActiveSyncItems(): Flow<List<SyncQueueEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: SyncQueueEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<SyncQueueEntity>)
    
    @Update
    suspend fun updateQueueItem(item: SyncQueueEntity)
    
    @Delete
    suspend fun deleteQueueItem(item: SyncQueueEntity)
    
    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED' AND timestamp < :olderThan")
    suspend fun deleteCompletedItems(olderThan: Long)
    
    @Query("UPDATE sync_queue SET status = 'PROCESSING', lastAttempt = :timestamp WHERE queueId = :queueId")
    suspend fun markAsProcessing(queueId: Long, timestamp: Long)
    
    @Query("UPDATE sync_queue SET status = 'COMPLETED' WHERE queueId = :queueId")
    suspend fun markAsCompleted(queueId: Long)
    
    @Query("UPDATE sync_queue SET status = 'FAILED', retryCount = retryCount + 1, lastAttempt = :timestamp, errorMessage = :error WHERE queueId = :queueId")
    suspend fun markAsFailed(queueId: Long, timestamp: Long, error: String)
    
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING' OR status = 'PROCESSING'")
    fun getPendingCount(): Flow<Int>
}
