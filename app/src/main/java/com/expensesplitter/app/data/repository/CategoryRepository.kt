package com.expensesplitter.app.data.repository

import com.expensesplitter.app.data.local.dao.CategoryDao
import com.expensesplitter.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllActiveCategories()
    }
    
    fun getActiveCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllActiveCategories()
    }
    
    suspend fun getCategoryById(categoryId: String): CategoryEntity? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun insertCategories(categories: List<CategoryEntity>) {
        categoryDao.insertCategories(categories)
    }
    
    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(categoryId: String) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null) {
            categoryDao.deleteCategory(category)
        }
    }
    
    suspend fun deactivateCategory(categoryId: String) {
        val category = categoryDao.getCategoryById(categoryId)
        if (category != null) {
            categoryDao.updateCategory(category.copy(isActive = false))
        }
    }
}
