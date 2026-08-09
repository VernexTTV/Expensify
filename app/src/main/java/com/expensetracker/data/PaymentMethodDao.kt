package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PaymentMethodDao {
    
    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethods(): LiveData<List<PaymentMethod>>
    
    @Query("SELECT * FROM payment_methods WHERE id = :id")
    fun getPaymentMethodById(id: Long): PaymentMethod?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPaymentMethod(paymentMethod: PaymentMethod): Long
    
    @Update
    fun updatePaymentMethod(paymentMethod: PaymentMethod)
    
    @Delete
    fun deletePaymentMethod(paymentMethod: PaymentMethod)
    
    @Query("DELETE FROM payment_methods")
    fun deleteAllPaymentMethods()
}
