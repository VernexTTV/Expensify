package com.expensetracker

import android.app.Application
import android.util.Log
import io.sentry.android.core.SentryAndroid

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        // Initialize Sentry FIRST - before anything else
        // This ensures we catch ALL crashes, even instant ones
        SentryAndroid.init(this) { options ->
            // DSN will be loaded from sentry.properties
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
