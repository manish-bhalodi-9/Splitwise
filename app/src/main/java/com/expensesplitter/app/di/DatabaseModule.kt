package com.expensesplitter.app.di

import android.content.Context
import com.expensesplitter.app.data.local.ExpenseSplitterDatabase
import com.expensesplitter.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ExpenseSplitterDatabase {
        // In production, use a secure passphrase from Android Keystore
        // For now, using a simple passphrase
        val passphrase = "expense_splitter_secure_key_${context.packageName}"
        return ExpenseSplitterDatabase.getInstance(context, passphrase)
    }
    
    @Provides
    fun provideUserDao(database: ExpenseSplitterDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideGroupDao(database: ExpenseSplitterDatabase): GroupDao {
        return database.groupDao()
    }
    
    @Provides
    fun provideExpenseDao(database: ExpenseSplitterDatabase): ExpenseDao {
        return database.expenseDao()
    }
    
    @Provides
    fun provideExpenseSplitDao(database: ExpenseSplitterDatabase): ExpenseSplitDao {
        return database.expenseSplitDao()
    }
    
    @Provides
    fun provideCategoryDao(database: ExpenseSplitterDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    fun provideSettlementDao(database: ExpenseSplitterDatabase): SettlementDao {
        return database.settlementDao()
    }
    
    @Provides
    fun provideSyncQueueDao(database: ExpenseSplitterDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
    
    @Provides
    fun provideAuditLogDao(database: ExpenseSplitterDatabase): AuditLogDao {
        return database.auditLogDao()
    }
}
