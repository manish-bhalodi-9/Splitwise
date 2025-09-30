package com.expensesplitter.app.data.repository

import com.expensesplitter.app.data.local.dao.GroupDao
import com.expensesplitter.app.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDao: GroupDao
) {
    
    fun getAllGroups(): Flow<List<GroupEntity>> {
        return groupDao.getAllActiveGroups()
    }
    
    suspend fun getAllGroupsList(): List<GroupEntity> {
        return groupDao.getAllActiveGroupsOnce()
    }
    
    fun getGroupById(groupId: String): Flow<GroupEntity?> {
        return groupDao.getGroupByIdFlow(groupId)
    }
    
    suspend fun getGroupByIdSuspend(groupId: String): GroupEntity? {
        return groupDao.getGroupById(groupId)
    }
    
    suspend fun getActiveGroup(): GroupEntity? {
        return groupDao.getDefaultGroup()
    }
    
    suspend fun insertGroup(group: GroupEntity) {
        groupDao.insertGroup(group)
    }
    
    suspend fun updateGroup(group: GroupEntity) {
        groupDao.updateGroup(group)
    }
    
    suspend fun deleteGroup(groupId: String) {
        groupDao.archiveGroup(groupId)
    }
    
    suspend fun setActiveGroup(groupId: String) {
        groupDao.setAsDefaultGroup(groupId)
    }
}
