package com.expensetracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["paymentMethodId"]),
        Index(value = ["date", "categoryId"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Timestamp
    val amount: Double,
    val paymentMethodId: Long? = null, // Foreign key to PaymentMethod
    val categoryId: Long? = null, // Foreign key to Category
    val bankName: String? = null, // For Credit Card or Debit Card
    val upiApp: String? = null, // For UPI
    val expenseSourceId: Long? = null, // Foreign key to ExpenseSource (Restaurant, Shop, Online Store, etc.)
    val description: String? = null // Optional note
)

// Keep enum for migration purposes only - will be removed after migration
enum class PaymentMode {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    CASH
}
