package com.expensetracker

import android.app.Application
import android.util.Log
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Sentry FIRST - before anything else
        // This ensures we catch ALL crashes, even instant ones
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
        
        Log.d("ExpenseTracker", "=== Application onCreate SUCCESS ===")
    }
}
