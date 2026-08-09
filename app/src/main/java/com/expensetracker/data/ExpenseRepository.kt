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
    
    fun getExpensesByPaymentMethodId(paymentMethodId: Long): LiveData<List<Expense>> {
        return expenseDao.getExpensesByPaymentMethodId(paymentMethodId)
    }
    
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return expenseDao.getTotalExpensesByDateRange(startDate, endDate)
    }
    
    fun getFilteredExpenses(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMethodId: Long?,
        bankName: String?,
        upiApp: String?
    ): LiveData<List<Expense>> {
        return expenseDao.getFilteredExpenses(startDate, endDate, categoryId, paymentMethodId, bankName, upiApp)
    }
    
    fun getFilteredTotal(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMethodId: Long?,
        bankName: String?,
        upiApp: String?
    ): LiveData<Double?> {
        return expenseDao.getFilteredTotal(startDate, endDate, categoryId, paymentMethodId, bankName, upiApp)
    }
}
