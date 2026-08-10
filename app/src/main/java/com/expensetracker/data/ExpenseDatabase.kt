package com.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Expense::class, Category::class, BankUpi::class, ExpenseSource::class, PaymentMethod::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun bankUpiDao(): BankUpiDao
    abstract fun expenseSourceDao(): ExpenseSourceDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    
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
        
        // Migration from version 2 to version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indexes for better query performance with large datasets
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paymentMode` ON `expenses` (`paymentMode`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date_categoryId` ON `expenses` (`date`, `categoryId`)")
            }
        }
        
        // Migration from version 3 to version 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create expense_sources table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expense_sources` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """)
                
                // Add expenseSourceId column to expenses table (nullable)
                database.execSQL("""
                    ALTER TABLE expenses ADD COLUMN expenseSourceId INTEGER
                """)
            }
        }
        
        // Migration from version 4 to version 5
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create payment_methods table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_methods` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL
                    )
                """)
                
                // Insert default payment methods with specific IDs matching the enum ordinals
                database.execSQL("INSERT INTO `payment_methods` (`id`, `name`) VALUES (1, 'Credit Card')")
                database.execSQL("INSERT INTO `payment_methods` (`id`, `name`) VALUES (2, 'Debit Card')")
                database.execSQL("INSERT INTO `payment_methods` (`id`, `name`) VALUES (3, 'UPI')")
                database.execSQL("INSERT INTO `payment_methods` (`id`, `name`) VALUES (4, 'Cash')")
                
                // Add paymentMethodId column to expenses table (nullable initially)
                database.execSQL("ALTER TABLE expenses ADD COLUMN paymentMethodId INTEGER")
                
                // Migrate existing paymentMode enum values to paymentMethodId
                // CREDIT_CARD (0) -> 1, DEBIT_CARD (1) -> 2, UPI (2) -> 3, CASH (3) -> 4
                database.execSQL("UPDATE expenses SET paymentMethodId = CASE paymentMode WHEN 0 THEN 1 WHEN 1 THEN 2 WHEN 2 THEN 3 WHEN 3 THEN 4 END")
                
                // Create a new expenses table without paymentMode column
                database.execSQL("""
                    CREATE TABLE expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        paymentMethodId INTEGER,
                        categoryId INTEGER,
                        bankName TEXT,
                        upiApp TEXT,
                        expenseSourceId INTEGER,
                        description TEXT
                    )
                """)
                
                // Copy data from old table to new table
                database.execSQL("""
                    INSERT INTO expenses_new (id, date, amount, paymentMethodId, categoryId, bankName, upiApp, expenseSourceId, description)
                    SELECT id, date, amount, paymentMethodId, categoryId, bankName, upiApp, expenseSourceId, description
                    FROM expenses
                """)
                
                // Drop old table
                database.execSQL("DROP TABLE expenses")
                
                // Rename new table to expenses
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
                
                // Recreate indexes
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_categoryId` ON `expenses` (`categoryId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_paymentMethodId` ON `expenses` (`paymentMethodId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_expenses_date_categoryId` ON `expenses` (`date`, `categoryId`)")
            }
        }
        
        // Migration from version 5 to version 6
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add Wallets as a new payment method
                database.execSQL("INSERT INTO `payment_methods` (`id`, `name`) VALUES (5, 'Wallets')")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
