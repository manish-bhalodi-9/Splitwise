package com.expensesplitter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.expensesplitter.app.data.local.converter.StringListConverter
import com.expensesplitter.app.data.local.dao.*
import com.expensesplitter.app.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        CategoryEntity::class,
        SettlementEntity::class,
        SyncQueueEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class ExpenseSplitterDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settlementDao(): SettlementDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun auditLogDao(): AuditLogDao
    
    companion object {
        private const val DATABASE_NAME = "expense_splitter_db"
        
        @Volatile
        private var INSTANCE: ExpenseSplitterDatabase? = null
        
        fun getInstance(context: Context, passphrase: String): ExpenseSplitterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context, passphrase)
                INSTANCE = instance
                instance
            }
        }
        
        private fun buildDatabase(context: Context, passphrase: String): ExpenseSplitterDatabase {
            val passphrase = SQLiteDatabase.getBytes(passphrase.toCharArray())
            val factory = SupportFactory(passphrase)
            
            return Room.databaseBuilder(
                context.applicationContext,
                ExpenseSplitterDatabase::class.java,
                DATABASE_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // For development only, remove for production
                .build()
        }
    }
}
