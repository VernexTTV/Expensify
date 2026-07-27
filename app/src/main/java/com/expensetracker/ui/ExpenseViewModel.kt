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
    private val categoryRepository: CategoryRepository
    private val bankUpiRepository: BankUpiRepository
    
    val allExpenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    val allCategories: LiveData<List<Category>>
    val allBankUpi: LiveData<List<BankUpi>>
    
    init {
        android.util.Log.d("ExpenseTracker", "=== ViewModel init START ===")
        try {
            android.util.Log.d("ExpenseTracker", "Getting database instance...")
            val database = ExpenseDatabase.getDatabase(application)
            
            android.util.Log.d("ExpenseTracker", "Getting DAOs...")
            val expenseDao = database.expenseDao()
            val categoryDao = database.categoryDao()
            val bankUpiDao = database.bankUpiDao()
            
            android.util.Log.d("ExpenseTracker", "Creating repositories...")
            repository = ExpenseRepository(expenseDao)
            categoryRepository = CategoryRepository(categoryDao)
            bankUpiRepository = BankUpiRepository(bankUpiDao)
            
            android.util.Log.d("ExpenseTracker", "Setting up LiveData...")
            allExpenses = repository.allExpenses
            totalExpenses = repository.totalExpenses
            allCategories = categoryRepository.allCategories
            allBankUpi = bankUpiRepository.allBankUpi
            
            android.util.Log.d("ExpenseTracker", "=== ViewModel init SUCCESS ===")
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "=== ViewModel init FAILED ===", e)
            throw e
        }
    }
    
    // Expense operations
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
    
    // Category operations
    fun insertCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.insert(category)
    }
    
    fun updateCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.update(category)
    }
    
    fun deleteCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        categoryRepository.delete(category)
    }
    
    // BankUpi operations
    fun insertBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        bankUpiRepository.insert(bankUpi)
    }
    
    fun updateBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        bankUpiRepository.update(bankUpi)
    }
    
    fun deleteBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        bankUpiRepository.delete(bankUpi)
    }
    
    fun getBankUpiByType(type: BankUpiType): LiveData<List<BankUpi>> {
        return bankUpiRepository.getBankUpiByType(type)
    }
    
    // Single-select filter operations
    fun getFilteredExpenses(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<List<Expense>> {
        return repository.getFilteredExpenses(startDate, endDate, categoryId, paymentMode, bankName, upiApp)
    }
    
    fun getFilteredTotal(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<Double?> {
        return repository.getFilteredTotal(startDate, endDate, categoryId, paymentMode, bankName, upiApp)
    }
}
