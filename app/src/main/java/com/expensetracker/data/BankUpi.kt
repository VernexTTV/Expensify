package com.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_upi")
data class BankUpi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: BankUpiType // BANK or UPI
)

enum class BankUpiType {
    BANK,
    UPI
}
