package com.expensetracker

import android.app.Application
import io.sentry.android.core.SentryAndroid

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Sentry for crash reporting
        SentryAndroid.init(this) { options ->
            options.dsn = "https://2e3a4f5b6c7d8e9f0a1b2c3d4e5f6a7b@o4506123456789012.ingest.sentry.io/4506123456789013"
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = 1.0
            // Enable detailed logging in debug mode
            options.isDebug = BuildConfig.DEBUG
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
            // Set release name
            options.release = "expense-tracker@${BuildConfig.VERSION_NAME}"
        }
    }
}
