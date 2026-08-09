package com.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: ExpenseRepository
    private val categoryRepository: CategoryRepository
    private val bankUpiRepository: BankUpiRepository
    private val expenseSourceRepository: ExpenseSourceRepository
    private val paymentMethodRepository: PaymentMethodRepository
    
    val allExpenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    val allCategories: LiveData<List<Category>>
    val allBankUpi: LiveData<List<BankUpi>>
    val allExpenseSources: LiveData<List<ExpenseSource>>
    val allPaymentMethods: LiveData<List<PaymentMethod>>
    
    // Error handling
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Filter state - persists across configuration changes
    data class FilterState(
        val minAmount: Double? = null,
        val maxAmount: Double? = null,
        val startDate: Long? = null,
        val endDate: Long? = null,
        val paymentMethodIds: List<Long> = emptyList(),
        val categoryIds: List<Long> = emptyList(),
        val expenseSourceIds: List<Long> = emptyList(),
        val selectedMonth: Int? = null, // 0 = All, 1-12 = Jan-Dec
        val selectedYear: Int? = null   // Year number or null for "All"
    )
    
    private val _filterState = MutableLiveData<FilterState>(FilterState())
    val filterState: LiveData<FilterState> = _filterState
    
    // Filtered expenses - switches between all expenses and filtered based on state
    private val _currentExpenses = MutableLiveData<LiveData<List<Expense>>>()
    val currentExpenses: LiveData<List<Expense>> = _currentExpenses.switchMap { it }
    
    private val _currentTotal = MutableLiveData<LiveData<Double?>>()
    val currentTotal: LiveData<Double?> = _currentTotal.switchMap { it }
    
    // Category summary with caching
    private val _categorySummary = MutableLiveData<List<CategorySummary>>()
    val categorySummary: LiveData<List<CategorySummary>> = _categorySummary
    
    // Automatically update category summary when either expenses or categories change
    init {
        android.util.Log.d("ExpenseTracker", "=== ViewModel init START ===")
        try {
            android.util.Log.d("ExpenseTracker", "Getting database instance...")
            val database = ExpenseDatabase.getDatabase(application)
            
            android.util.Log.d("ExpenseTracker", "Getting DAOs...")
            val expenseDao = database.expenseDao()
            val categoryDao = database.categoryDao()
            val bankUpiDao = database.bankUpiDao()
            val expenseSourceDao = database.expenseSourceDao()
            val paymentMethodDao = database.paymentMethodDao()
            
            android.util.Log.d("ExpenseTracker", "Creating repositories...")
            repository = ExpenseRepository(expenseDao)
            categoryRepository = CategoryRepository(categoryDao)
            bankUpiRepository = BankUpiRepository(bankUpiDao)
            expenseSourceRepository = ExpenseSourceRepository(expenseSourceDao)
            paymentMethodRepository = PaymentMethodRepository(paymentMethodDao)
            
            android.util.Log.d("ExpenseTracker", "Setting up LiveData...")
            allExpenses = repository.allExpenses
            totalExpenses = repository.totalExpenses
            allCategories = categoryRepository.allCategories
            allBankUpi = bankUpiRepository.allBankUpi
            allExpenseSources = expenseSourceRepository.allExpenseSources
            allPaymentMethods = paymentMethodRepository.allPaymentMethods
            
            // Initialize with all expenses by default
            _currentExpenses.value = allExpenses
            _currentTotal.value = totalExpenses
            
            // Auto-update category summary when expenses or categories change
            setupCategorySummaryAutoUpdate()
            
            android.util.Log.d("ExpenseTracker", "=== ViewModel init SUCCESS ===")
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "=== ViewModel init FAILED ===", e)
            _errorMessage.postValue("Failed to initialize database: ${e.message}")
            throw e
        }
    }
    
    private fun setupCategorySummaryAutoUpdate() {
        // Combine currentExpenses and allCategories to auto-update summary
        val mediator = androidx.lifecycle.MediatorLiveData<Pair<List<Expense>, List<Category>>>()
        
        mediator.addSource(currentExpenses) { expenses ->
            val categories = allCategories.value ?: emptyList()
            mediator.value = Pair(expenses ?: emptyList(), categories)
        }
        
        mediator.addSource(allCategories) { categories ->
            val expenses = currentExpenses.value ?: emptyList()
            mediator.value = Pair(expenses, categories ?: emptyList())
        }
        
        // Whenever either changes, recalculate summary
        mediator.observeForever { (expenses, categories) ->
            calculateCategorySummary(expenses, categories)
        }
    }
    
    private fun calculateCategorySummary(expenses: List<Expense>, categories: List<Category>) {
        viewModelScope.launch(Dispatchers.Default) {
            if (expenses.isEmpty()) {
                _categorySummary.postValue(emptyList())
                return@launch
            }
            
            val categoryTotals = mutableMapOf<String, Double>()
            
            expenses.forEach { expense ->
                val categoryName = expense.categoryId?.let { id ->
                    categories.find { it.id == id }?.name
                } ?: "Uncategorized"
                
                categoryTotals[categoryName] = (categoryTotals[categoryName] ?: 0.0) + expense.amount
            }
            
            val summaryList = categoryTotals.map { (name, amount) ->
                CategorySummary(name, amount)
            }.sortedByDescending { it.totalAmount }
            
            _categorySummary.postValue(summaryList)
        }
    }
    
    // Expense operations with error handling
    fun insert(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            repository.insert(expense)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error inserting expense", e)
            _errorMessage.postValue("Failed to add expense: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    fun update(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            repository.update(expense)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error updating expense", e)
            _errorMessage.postValue("Failed to update expense: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }
    
    fun delete(expense: Expense) = viewModelScope.launch(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            repository.delete(expense)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error deleting expense", e)
            _errorMessage.postValue("Failed to delete expense: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
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
    
    // Category operations with error handling
    fun insertCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        try {
            categoryRepository.insert(category)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error inserting category", e)
            _errorMessage.postValue("Failed to add category: ${e.message}")
        }
    }
    
    fun updateCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        try {
            categoryRepository.update(category)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error updating category", e)
            _errorMessage.postValue("Failed to update category: ${e.message}")
        }
    }
    
    fun deleteCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        try {
            categoryRepository.delete(category)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error deleting category", e)
            _errorMessage.postValue("Failed to delete category: ${e.message}")
        }
    }
    
    // BankUpi operations with error handling
    fun insertBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        try {
            bankUpiRepository.insert(bankUpi)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error inserting bank/UPI", e)
            _errorMessage.postValue("Failed to add bank/UPI: ${e.message}")
        }
    }
    
    fun updateBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        try {
            bankUpiRepository.update(bankUpi)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error updating bank/UPI", e)
            _errorMessage.postValue("Failed to update bank/UPI: ${e.message}")
        }
    }
    
    fun deleteBankUpi(bankUpi: BankUpi) = viewModelScope.launch(Dispatchers.IO) {
        try {
            bankUpiRepository.delete(bankUpi)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error deleting bank/UPI", e)
            _errorMessage.postValue("Failed to delete bank/UPI: ${e.message}")
        }
    }
    
    fun getBankUpiByType(type: BankUpiType): LiveData<List<BankUpi>> {
        return bankUpiRepository.getBankUpiByType(type)
    }
    
    // ExpenseSource operations with error handling
    fun insertExpenseSource(expenseSource: ExpenseSource) = viewModelScope.launch(Dispatchers.IO) {
        try {
            expenseSourceRepository.insert(expenseSource)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error inserting expense source", e)
            _errorMessage.postValue("Failed to add expense source: ${e.message}")
        }
    }
    
    fun updateExpenseSource(expenseSource: ExpenseSource) = viewModelScope.launch(Dispatchers.IO) {
        try {
            expenseSourceRepository.update(expenseSource)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error updating expense source", e)
            _errorMessage.postValue("Failed to update expense source: ${e.message}")
        }
    }
    
    fun deleteExpenseSource(expenseSource: ExpenseSource) = viewModelScope.launch(Dispatchers.IO) {
        try {
            expenseSourceRepository.delete(expenseSource)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error deleting expense source", e)
            _errorMessage.postValue("Failed to delete expense source: ${e.message}")
        }
    }
    
    // PaymentMethod operations with error handling
    fun insertPaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch(Dispatchers.IO) {
        try {
            paymentMethodRepository.insert(paymentMethod)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error inserting payment method", e)
            _errorMessage.postValue("Failed to add payment method: ${e.message}")
        }
    }
    
    fun updatePaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch(Dispatchers.IO) {
        try {
            paymentMethodRepository.update(paymentMethod)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error updating payment method", e)
            _errorMessage.postValue("Failed to update payment method: ${e.message}")
        }
    }
    
    fun deletePaymentMethod(paymentMethod: PaymentMethod) = viewModelScope.launch(Dispatchers.IO) {
        try {
            paymentMethodRepository.delete(paymentMethod)
            _errorMessage.postValue(null)
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "Error deleting payment method", e)
            _errorMessage.postValue("Failed to delete payment method: ${e.message}")
        }
    }
    
    // Multi-criteria filter with client-side filtering for complex scenarios
    fun applyFilters(
        minAmount: Double?,
        maxAmount: Double?,
        startDate: Long?,
        endDate: Long?,
        paymentMethodIds: List<Long>,
        categoryIds: List<Long>,
        expenseSourceIds: List<Long>,
        selectedMonth: Int?,
        selectedYear: Int?
    ) {
        _isLoading.value = true
        
        // Save filter state including month/year and expense sources
        _filterState.value = FilterState(minAmount, maxAmount, startDate, endDate, paymentMethodIds, categoryIds, expenseSourceIds, selectedMonth, selectedYear)
        
        // For complex multi-select filters, we need client-side filtering
        // because Room doesn't support IN clauses with LiveData easily
        val baseExpenses = if (startDate != null || endDate != null) {
            // Use date range query if dates are specified
            repository.getExpensesByDateRange(
                startDate ?: 0L,
                endDate ?: Long.MAX_VALUE
            )
        } else {
            allExpenses
        }
        
        // Apply client-side filters using Transformations
        val filteredExpenses = baseExpenses.map { expenses ->
            var filtered = expenses
            
            // Filter by amount range
            if (minAmount != null) {
                filtered = filtered.filter { it.amount >= minAmount }
            }
            if (maxAmount != null) {
                filtered = filtered.filter { it.amount <= maxAmount }
            }
            
            // Filter by payment modes
            if (paymentMethodIds.isNotEmpty()) {
                filtered = filtered.filter { it.paymentMethodId in paymentMethodIds }
            }
            
            // Filter by categories
            if (categoryIds.isNotEmpty()) {
                filtered = filtered.filter { it.categoryId in categoryIds }
            }
            
            // Filter by expense sources
            if (expenseSourceIds.isNotEmpty()) {
                filtered = filtered.filter { it.expenseSourceId in expenseSourceIds }
            }
            
            filtered
        }
        
        _currentExpenses.value = filteredExpenses
        
        // Calculate filtered total
        val filteredTotal = filteredExpenses.map { expenses ->
            expenses.sumOf { it.amount } as Double?
        }
        _currentTotal.value = filteredTotal
        
        _isLoading.value = false
    }
    
    fun clearFilters() {
        _filterState.value = FilterState()
        _currentExpenses.value = allExpenses
        _currentTotal.value = totalExpenses
    }
    
    fun isFiltering(): Boolean {
        val state = _filterState.value ?: return false
        return state.minAmount != null || 
               state.maxAmount != null || 
               state.startDate != null || 
               state.endDate != null || 
               state.paymentMethodIds.isNotEmpty() || 
               state.categoryIds.isNotEmpty() ||
               state.expenseSourceIds.isNotEmpty()
    }
    
    data class CategorySummary(
        val categoryName: String,
        val totalAmount: Double
    )
    
    fun clearError() {
        _errorMessage.value = null
    }
}
