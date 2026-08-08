package com.expensetracker

import android.app.Application
import android.util.Log
import io.sentry.android.core.SentryAndroid

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        // Initialize Sentry FIRST - before anything else
        // This ensures we catch ALL crashes, even instant ones
        SentryAndroid.init(this) { options ->
            options.dsn = "https://ed21a268279cdf3e8271af859ee531c3@o4511715743105024.ingest.us.sentry.io/4511875213361152"
            options.isDebug = true // Enable debug logs to verify it's working
            options.enableAutoSessionTracking = true
            options.sessionTrackingIntervalMillis = 5000
        }
        
        Log.d("ExpenseTracker", "=== Sentry initialized ===")
        
        Log.d("ExpenseTracker", "=== Application onCreate START ===")
        try {
            super.onCreate()
            Log.d("ExpenseTracker", "=== Application onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e("ExpenseTracker", "=== Application onCreate FAILED ===", e)
            throw e
        }
    }
}
