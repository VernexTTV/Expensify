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
                try {
                    android.util.Log.d("ExpenseTracker", "Creating database instance...")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ExpenseDatabase::class.java,
                        "expense_database_v2"  // Changed name to force new database
                    )
                    .fallbackToDestructiveMigration()  // Recreate if any issues
                    .build()
                    INSTANCE = instance
                    android.util.Log.d("ExpenseTracker", "=== Database getDatabase SUCCESS ===")
                    instance
                } catch (e: Exception) {
                    android.util.Log.e("ExpenseTracker", "=== Database getDatabase FAILED ===", e)
                    throw e
                }
            }
        }
    }
}
