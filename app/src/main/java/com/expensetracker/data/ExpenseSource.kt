package com.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_sources")
data class ExpenseSource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String // Restaurant, Shop, Store, Amazon, Flipkart, etc.
)
