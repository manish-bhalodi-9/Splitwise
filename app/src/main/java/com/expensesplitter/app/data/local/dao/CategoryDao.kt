package com.expensesplitter.app.data.local.dao

import androidx.room.*
import com.expensesplitter.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    fun getCategoryByIdFlow(categoryId: String): Flow<CategoryEntity?>
    
    @Query("SELECT * FROM categories WHERE (groupId = :groupId OR groupId IS NULL) AND isActive = 1 ORDER BY displayOrder ASC, name ASC")
    fun getCategoriesByGroup(groupId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE groupId IS NULL AND isDefault = 1 AND isActive = 1 ORDER BY displayOrder ASC")
    fun getDefaultCategories(): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE groupId IS NULL AND isDefault = 1 AND isActive = 1 ORDER BY displayOrder ASC")
    suspend fun getDefaultCategoriesOnce(): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY displayOrder ASC, name ASC")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("UPDATE categories SET isActive = 0 WHERE categoryId = :categoryId")
    suspend fun deactivateCategory(categoryId: String)
    
    @Query("UPDATE categories SET displayOrder = :order WHERE categoryId = :categoryId")
    suspend fun updateDisplayOrder(categoryId: String, order: Int)
}
