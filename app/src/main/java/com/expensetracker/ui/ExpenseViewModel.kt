package com.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ExpenseRepository
    val allExpenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    
    init {
        val expenseDao = ExpenseDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(expenseDao)
        allExpenses = repository.allExpenses
        totalExpenses = repository.totalExpenses
    }
    
    fun insert(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(expense)
    }
    
    fun update(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(expense)
    }
    
    fun delete(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(expense)
    }
    
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>> {
        return repository.getExpensesByDateRange(startDate, endDate)
    }
    
    fun getExpensesByPaymentMode(mode: PaymentMode): LiveData<List<Expense>> {
        return repository.getExpensesByPaymentMode(mode)
    }
    
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return repository.getTotalExpensesByDateRange(startDate, endDate)
    }
}
