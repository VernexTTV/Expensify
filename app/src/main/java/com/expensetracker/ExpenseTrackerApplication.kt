package com.expensetracker

import android.app.Application
import android.util.Log
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.expensetracker.data.*

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        // Initialize Sentry FIRST - before super.onCreate()
        try {
            SentryAndroid.init(this) { options ->
                options.dsn = "https://ed21a268279cdf3e8271af859ee531c3@o4511715743105024.ingest.us.sentry.io/4511875213361152"
                
                // Enable debug mode to see what's happening
                options.isDebug = BuildConfig.DEBUG
                
                // Environment
                options.environment = if (BuildConfig.DEBUG) "debug" else "production"
                
                // Release version
                options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}"
                
                // Sample rate - capture 100% of errors in debug
                options.sampleRate = 1.0
                
                // Enable automatic breadcrumbs
                options.isEnableAutoSessionTracking = true
                options.isEnableActivityLifecycleBreadcrumbs = true
                options.isEnableAppLifecycleBreadcrumbs = true
                options.isEnableSystemEventBreadcrumbs = true
                
                // Send crashes immediately (before restart)
                options.isEnableUncaughtExceptionHandler = true
                
                Log.d("ExpenseTracker", "Sentry DSN configured: ${options.dsn}")
            }
            
            // Set user context
            Sentry.configureScope { scope ->
                val user = User()
                user.id = "test-user"
                scope.user = user
            }
            
            Log.d("ExpenseTracker", "=== Sentry initialized successfully ===")
            
            // Send a test message to verify Sentry is working
            Sentry.captureMessage("Expense Tracker app started - Sentry is active!")
            Log.d("ExpenseTracker", "=== Test message sent to Sentry ===")
            
        } catch (e: Exception) {
            Log.e("ExpenseTracker", "=== Sentry initialization FAILED ===", e)
            // Don't crash the app if Sentry fails
        }
        
        // Now call super.onCreate() with error handling
        try {
            super.onCreate()
            Log.d("ExpenseTracker", "=== Application onCreate SUCCESS ===")
            
            // Initialize default categories and banks/UPI on first run
            initializeDefaultData()
            
        } catch (e: Exception) {
            Log.e("ExpenseTracker", "=== Application onCreate FAILED ===", e)
            // Report to Sentry if it's initialized
            try {
                Sentry.captureException(e)
            } catch (sentryError: Exception) {
                Log.e("ExpenseTracker", "Could not report to Sentry", sentryError)
            }
            // Re-throw to let the system handle it
            throw e
        }
    }
    
    private fun initializeDefaultData() {
        val prefs = getSharedPreferences("ExpenseTrackerPrefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)
        
        if (isFirstRun) {
            Log.d("ExpenseTracker", "First run detected - initializing default categories and banks")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = ExpenseDatabase.getDatabase(this@ExpenseTrackerApplication)
                    val categoryDao = database.categoryDao()
                    val bankUpiDao = database.bankUpiDao()
                    
                    // Add default categories
                    val defaultCategories = listOf(
                        "Food & Dining",
                        "Groceries",
                        "Transportation",
                        "Fuel",
                        "Shopping",
                        "Entertainment",
                        "Bills & Utilities",
                        "Healthcare",
                        "Education",
                        "Travel",
                        "Rent",
                        "Insurance",
                        "Phone & Internet",
                        "Gifts & Donations",
                        "Personal Care",
                        "Fitness & Sports",
                        "Home Maintenance",
                        "Clothing",
                        "Electronics",
                        "Subscriptions",
                        "Investment",
                        "EMI",
                        "Pets",
                        "Books & Magazines",
                        "Hobbies",
                        "Salon & Spa",
                        "Restaurants",
                        "Coffee & Tea",
                        "Fast Food",
                        "Movies & Shows",
                        "Gaming",
                        "Music",
                        "Office Expenses",
                        "Taxes",
                        "Miscellaneous"
                    )
                    
                    defaultCategories.forEach { name ->
                        categoryDao.insertCategory(Category(name = name))
                    }
                    
                    // Add common banks
                    val defaultBanks = listOf(
                        "HDFC Bank",
                        "ICICI Bank",
                        "SBI",
                        "Axis Bank",
                        "Kotak Mahindra Bank",
                        "Bank of Baroda",
                        "Punjab National Bank",
                        "Canara Bank",
                        "IDFC First Bank",
                        "Yes Bank",
                        "IndusInd Bank",
                        "Union Bank"
                    )
                    
                    defaultBanks.forEach { name ->
                        bankUpiDao.insertBankUpi(BankUpi(name = name, type = BankUpiType.BANK))
                    }
                    
                    // Add common UPI apps
                    val defaultUpiApps = listOf(
                        "Google Pay",
                        "PhonePe",
                        "Paytm",
                        "Amazon Pay",
                        "BHIM",
                        "WhatsApp Pay",
                        "Mobikwik",
                        "Freecharge",
                        "CRED"
                    )
                    
                    defaultUpiApps.forEach { name ->
                        bankUpiDao.insertBankUpi(BankUpi(name = name, type = BankUpiType.UPI))
                    }
                    
                    // Mark first run as complete
                    prefs.edit().putBoolean("isFirstRun", false).apply()
                    
                    Log.d("ExpenseTracker", "Default data initialized successfully")
                    
                } catch (e: Exception) {
                    Log.e("ExpenseTracker", "Failed to initialize default data", e)
                    Sentry.captureException(e)
                }
            }
        }
    }
}
