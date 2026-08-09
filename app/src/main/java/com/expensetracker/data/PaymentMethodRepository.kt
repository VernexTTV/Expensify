package com.expensetracker.data

import androidx.lifecycle.LiveData

class PaymentMethodRepository(private val paymentMethodDao: PaymentMethodDao) {
    
    val allPaymentMethods: LiveData<List<PaymentMethod>> = paymentMethodDao.getAllPaymentMethods()
    
    fun insert(paymentMethod: PaymentMethod): Long {
        return paymentMethodDao.insertPaymentMethod(paymentMethod)
    }
    
    fun update(paymentMethod: PaymentMethod) {
        paymentMethodDao.updatePaymentMethod(paymentMethod)
    }
    
    fun delete(paymentMethod: PaymentMethod) {
        paymentMethodDao.deletePaymentMethod(paymentMethod)
    }
    
    fun getPaymentMethodById(id: Long): PaymentMethod? {
        return paymentMethodDao.getPaymentMethodById(id)
    }
}
