package com.expensetracker.data

import androidx.lifecycle.LiveData

class ExpenseSourceRepository(private val expenseSourceDao: ExpenseSourceDao) {
    
    val allExpenseSources: LiveData<List<ExpenseSource>> = expenseSourceDao.getAllExpenseSources()
    
    fun insert(expenseSource: ExpenseSource): Long {
        return expenseSourceDao.insertExpenseSource(expenseSource)
    }
    
    fun update(expenseSource: ExpenseSource) {
        expenseSourceDao.updateExpenseSource(expenseSource)
    }
    
    fun delete(expenseSource: ExpenseSource) {
        expenseSourceDao.deleteExpenseSource(expenseSource)
    }
    
    fun getExpenseSourceById(id: Long): ExpenseSource? {
        return expenseSourceDao.getExpenseSourceById(id)
    }
}
