package com.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Timestamp
    val amount: Double,
    val paymentMode: PaymentMode,
    val categoryId: Long? = null, // Foreign key to Category
    val bankName: String? = null, // For Credit Card or Debit Card
    val upiApp: String? = null, // For UPI
    val description: String? = null // Optional note
)

enum class PaymentMode {
    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    CASH
}
