package com.expensetracker.data

import androidx.lifecycle.LiveData

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses()
    val totalExpenses: LiveData<Double?> = expenseDao.getTotalExpenses()
    
    fun insert(expense: Expense): Long {
        return expenseDao.insertExpense(expense)
    }
    
    fun update(expense: Expense) {
        expenseDao.updateExpense(expense)
    }
    
    fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
    
    fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)
    }
    
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return expenseDao.getExpensesByDateRange(startDate, endDate)
    }
    
    fun getExpensesByPaymentMode(mode: PaymentMode): LiveData<List<Expense>> {
        return expenseDao.getExpensesByPaymentMode(mode)
    }
    
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate)
    }
    
    fun getMultiFilteredExpenses(
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<List<Expense>> {
        return expenseDao.getMultiFilteredExpenses(
            minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
    }
    
    fun getMultiFilteredTotal(
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<Double?> {
        return expenseDao.getMultiFilteredTotal(
            minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
    }
    
    fun getFilteredExpenses(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<List<Expense>> {
        return expenseDao.getFilteredExpenses(startDate, endDate, categoryId, paymentMode, bankName, upiApp)
    }
    
    fun getFilteredTotal(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<Double?> {
        return expenseDao.getFilteredTotal(startDate, endDate, categoryId, paymentMode, bankName, upiApp)
    }
}
