package com.expensetracker

import android.app.Application
import android.util.Log

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
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
