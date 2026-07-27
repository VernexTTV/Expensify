package com.expensetracker.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromPaymentMode(value: PaymentMode): String {
        return value.name
    }
    
    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode {
        return PaymentMode.valueOf(value)
    }
    
    @TypeConverter
    fun fromBankUpiType(value: BankUpiType): String {
        return value.name
    }
    
    @TypeConverter
    fun toBankUpiType(value: String): BankUpiType {
        return BankUpiType.valueOf(value)
    }
}
