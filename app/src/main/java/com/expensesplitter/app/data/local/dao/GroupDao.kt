package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    fun getGroupByIdFlow(groupId: String): Flow<GroupEntity?>
    
    @Query("SELECT * FROM groups WHERE isActive = 1 ORDER BY lastUpdated DESC")
    fun getAllActiveGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE isActive = 1 ORDER BY lastUpdated DESC")
    suspend fun getAllActiveGroupsOnce(): List<GroupEntity>
    
    @Query("SELECT * FROM groups WHERE isDefault = 1 AND isActive = 1 LIMIT 1")
    suspend fun getDefaultGroup(): GroupEntity?
    
    @Query("SELECT * FROM groups WHERE isDefault = 1 AND isActive = 1 LIMIT 1")
    fun getDefaultGroupFlow(): Flow<GroupEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)
    
    @Update
    suspend fun updateGroup(group: GroupEntity)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("UPDATE groups SET isActive = 0 WHERE groupId = :groupId")
    suspend fun archiveGroup(groupId: String)
    
    @Query("UPDATE groups SET isDefault = 0")
    suspend fun clearDefaultGroup()
    
    @Query("UPDATE groups SET isDefault = 1 WHERE groupId = :groupId")
    suspend fun setDefaultGroup(groupId: String)
    
    @Transaction
    suspend fun setAsDefaultGroup(groupId: String) {
        clearDefaultGroup()
        setDefaultGroup(groupId)
    }
    
    @Query("UPDATE groups SET lastSyncTime = :syncTime WHERE groupId = :groupId")
    suspend fun updateSyncTime(groupId: String, syncTime: Long)
    
    @Query("SELECT COUNT(*) FROM groups WHERE isActive = 1")
    fun getActiveGroupCount(): Flow<Int>
}
