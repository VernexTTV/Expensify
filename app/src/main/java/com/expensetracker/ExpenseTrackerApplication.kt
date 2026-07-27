package com.expensetracker

import android.app.Application
import io.sentry.android.core.SentryAndroid

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Sentry for crash reporting
        SentryAndroid.init(this) { options ->
            options.dsn = "https://06101fd1c53817ef65fee98c1fd001d7@o4511715743105024.ingest.us.sentry.io/4511807824920576"
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
