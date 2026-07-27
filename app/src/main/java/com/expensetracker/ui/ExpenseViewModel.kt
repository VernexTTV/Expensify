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
        val database = ExpenseDatabase.getDatabase(application)
        val expenseDao = database.expenseDao()
        val categoryDao = database.categoryDao()
        val bankUpiDao = database.bankUpiDao()
        
        repository = ExpenseRepository(expenseDao)
        categoryRepository = CategoryRepository(categoryDao)
        bankUpiRepository = BankUpiRepository(bankUpiDao)
        
        allExpenses = repository.allExpenses
        totalExpenses = repository.totalExpenses
        allCategories = categoryRepository.allCategories
        allBankUpi = bankUpiRepository.allBankUpi
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
    
    // Multi-select filter operations
    fun getMultiFilteredExpenses(
        startDates: List<Long>?,
        endDates: List<Long>?,
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<List<Expense>> {
        return repository.getMultiFilteredExpenses(
            startDates, endDates, minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
    }
    
    fun getMultiFilteredTotal(
        startDates: List<Long>?,
        endDates: List<Long>?,
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<Double?> {
        return repository.getMultiFilteredTotal(
            startDates, endDates, minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
    }
    
    // Single-select filter operations (legacy)
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
