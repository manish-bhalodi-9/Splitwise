package com.expensesplitter.app.data.repository

import com.expensesplitter.app.data.local.dao.UserDao
import com.expensesplitter.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }
    
    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
    
    suspend fun getCurrentUser(): UserEntity? {
        // Get the first user (since we only support single user for now)
        val users = userDao.getAllUsersOnce()
        return users.firstOrNull()
    }
    
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(userId: String) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            userDao.deleteUser(user)
        }
    }
    
    suspend fun setCurrentUser(userId: String) {
        // No-op for now since we only support single user
    }
}
