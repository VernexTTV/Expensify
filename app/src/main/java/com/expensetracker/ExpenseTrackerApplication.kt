package com.expensetracker

import android.app.Application

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("ExpenseTracker", "App starting...")
    }
}
