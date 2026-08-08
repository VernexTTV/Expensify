package com.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Expense::class, Category::class, BankUpi::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bankUpiDao(): BankUpiDao
    
    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null
        
        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """)
                
                // Create bank_upi table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bank_upi` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL
                    )
                """)
                
                // Add categoryId column to expenses table (nullable)
                database.execSQL("""
                    ALTER TABLE expenses ADD COLUMN categoryId INTEGER
                """)
            }
        }
        
        fun getDatabase(context: Context): ExpenseDatabase {
            android.util.Log.d("ExpenseTracker", "=== Database getDatabase START ===")
            return INSTANCE ?: synchronized(this) {
                val existingInstance = INSTANCE
                if (existingInstance != null) {
                    return existingInstance
                }
                
                try {
                    android.util.Log.d("ExpenseTracker", "Creating database instance...")
                    
                    // Try to delete old database files if they exist and are corrupted
                    try {
                        val oldDbFile = context.getDatabasePath("expense_database")
                        if (oldDbFile.exists()) {
                            android.util.Log.d("ExpenseTracker", "Deleting old database file...")
                            context.deleteDatabase("expense_database")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("ExpenseTracker", "Could not delete old database", e)
                    }
                    
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExpenseDatabase::class.java,
                        "expense_database_v2"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    
                    INSTANCE = instance
                    android.util.Log.d("ExpenseTracker", "=== Database getDatabase SUCCESS ===")
                    instance
                } catch (e: Exception) {
                    android.util.Log.e("ExpenseTracker", "=== Database getDatabase FAILED ===", e)
                    
                    // Try one more time with a completely fresh database
                    try {
                        android.util.Log.d("ExpenseTracker", "Attempting recovery with fresh database...")
                        context.deleteDatabase("expense_database_v2")
                        
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            ExpenseDatabase::class.java,
                            "expense_database_v2"
                        )
                        .fallbackToDestructiveMigration()
                        .build()
                        
                        INSTANCE = instance
                        android.util.Log.d("ExpenseTracker", "=== Database recovery SUCCESS ===")
                        instance
                    } catch (recoveryError: Exception) {
                        android.util.Log.e("ExpenseTracker", "=== Database recovery FAILED ===", recoveryError)
                        throw recoveryError
                    }
                }
            }
        }
    }
}
