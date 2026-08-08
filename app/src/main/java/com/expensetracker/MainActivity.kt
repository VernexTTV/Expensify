package com.expensetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.expensetracker.data.*
import com.expensetracker.ui.ExpenseAdapter
import com.expensetracker.ui.ExpenseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.sentry.Sentry
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var totalAmountView: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var categorySummaryLayout: LinearLayout
    private lateinit var categorySummaryRecyclerView: RecyclerView
    private lateinit var categorySummaryAdapter: CategorySummaryAdapter
    
    private var categories: List<Category> = emptyList()
    private var bankUpiList: List<BankUpi> = emptyList()
    
    // Filter state
    private var isFiltering = false
    private var currentFilterStartDate: Long? = null
    private var currentFilterEndDate: Long? = null
    private var currentFilterCategoryId: Long? = null
    private var currentFilterPaymentMode: PaymentMode? = null
    private var currentFilterBankName: String? = null
    private var currentFilterUpiApp: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        android.util.Log.d("ExpenseTracker", "=== MainActivity onCreate START ===")
        
        try {
            android.util.Log.d("ExpenseTracker", "Step 1: super.onCreate")
            super.onCreate(savedInstanceState)
            
            android.util.Log.d("ExpenseTracker", "Step 2: setContentView")
            setContentView(R.layout.activity_main)
            
            android.util.Log.d("ExpenseTracker", "Step 3: Setup toolbar")
            // Setup toolbar
            val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "Expense Tracker"
            
            android:util.Log.d("ExpenseTracker", "Step 4: Initialize views")
            // Initialize views
            recyclerView = findViewById(R.id.recyclerView)
            emptyView = findViewById(R.id.tvEmpty)
            totalAmountView = findViewById(R.id.tvTotalAmount)
            fabAdd = findViewById(R.id.fabAdd)
            categorySummaryLayout = findViewById(R.id.layoutCategorySummary)
            categorySummaryRecyclerView = findViewById(R.id.rvCategorySummary)
            
            android.util.Log.d("ExpenseTracker", "Step 5: Setup RecyclerView adapter")
            // Setup RecyclerView
            adapter = ExpenseAdapter(
                onEditClick = { expense -> showAddEditDialog(expense) },
                onDeleteClick = { expense -> showDeleteConfirmation(expense) },
                getCategoryName = { categoryId ->
                    categoryId?.let { id ->
                        categories.find { it.id == id }?.name
                    }
                }
            )
            
            android.util.Log.d("ExpenseTracker", "Step 6: Set adapter and layout manager")
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            
            // Setup Category Summary RecyclerView
            categorySummaryAdapter = CategorySummaryAdapter()
            categorySummaryRecyclerView.adapter = categorySummaryAdapter
            categorySummaryRecyclerView.layoutManager = LinearLayoutManager(this)
            
            android.util.Log.d("ExpenseTracker", "Step 7: Setup ViewModel")
            // Setup ViewModel
            viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
            
            android.util.Log.d("ExpenseTracker", "Step 8: Observe expenses")
            // Observe expenses
            viewModel.allExpenses.observe(this) { expenses ->
                if (expenses.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    categorySummaryLayout.visibility = View.GONE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    adapter.submitList(expenses)
                    
                    // Calculate and show category summary
                    updateCategorySummary(expenses)
                }
            }
            
            android.util.Log.d("ExpenseTracker", "Step 9: Observe total expenses")
            // Observe total expenses
            viewModel.totalExpenses.observe(this) { total ->
                val totalText = if (total != null) {
                    "Total: ₹${String.format("%.2f", total)}"
                } else {
                    "Total: ₹0.00"
                }
                totalAmountView.text = totalText
            }
            
            android.util.Log.d("ExpenseTracker", "Step 10: Observe categories")
            // Observe categories
            viewModel.allCategories.observe(this) { categoryList ->
                categories = categoryList
            }
            
            android.util.Log.d("ExpenseTracker", "Step 11: Observe bank/UPI list")
            // Observe bank/UPI list
            viewModel.allBankUpi.observe(this) { list ->
                bankUpiList = list
            }
            
            android.util.Log.d("ExpenseTracker", "Step 12: Setup FAB")
            // Setup FAB
            fabAdd.setOnClickListener {
                showAddEditDialog(null)
            }
            
            android.util.Log.d("ExpenseTracker", "=== MainActivity onCreate SUCCESS ===")
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "=== MainActivity onCreate FAILED ===", e)
            throw e
        }
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun clearFilters() {
        isFiltering = false
        currentFilterStartDate = null
        currentFilterEndDate = null
        currentFilterCategoryId = null
        currentFilterPaymentMode = null
        currentFilterBankName = null
        currentFilterUpiApp = null
        
        // Re-observe all expenses
        viewModel.allExpenses.observe(this) { expenses ->
            if (expenses.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                adapter.submitList(expenses)
            }
        }
        
        viewModel.totalExpenses.observe(this) { total ->
            val totalText = if (total != null) {
                "Total: ₹${String.format("%.2f", total)}"
            } else {
                "Total: ₹0.00"
            }
            totalAmountView.text = totalText
        }
        
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show()
    }
    
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Filter Expenses")
            .create()
        
        // Amount range inputs
        val etMinAmount: EditText = dialogView.findViewById(R.id.etMinAmount)
        val etMaxAmount: EditText = dialogView.findViewById(R.id.etMaxAmount)
        
        // Date range buttons
        val btnStartDate: Button = dialogView.findViewById(R.id.btnSelectStartDate)
        val btnEndDate: Button = dialogView.findViewById(R.id.btnSelectEndDate)
        
        // Month/Year spinners
        val spinnerMonth: android.widget.Spinner = dialogView.findViewById(R.id.spinnerMonth)
        val spinnerYear: android.widget.Spinner = dialogView.findViewById(R.id.spinnerYear)
        
        // Payment method checkboxes
        val cbCreditCard: CheckBox = dialogView.findViewById(R.id.cbCreditCard)
        val cbDebitCard: CheckBox = dialogView.findViewById(R.id.cbDebitCard)
        val cbUpi: CheckBox = dialogView.findViewById(R.id.cbUpi)
        val cbCash: CheckBox = dialogView.findViewById(R.id.cbCash)
        
        // Dynamic checkbox containers
        val layoutCategories: LinearLayout = dialogView.findViewById(R.id.layoutCategories)
        val layoutBanks: LinearLayout = dialogView.findViewById(R.id.layoutBanks)
        val layoutUpiApps: LinearLayout = dialogView.findViewById(R.id.layoutUpiApps)
        
        // Action buttons
        val btnClear: Button = dialogView.findViewById(R.id.btnClearFilters)
        val btnCancel: Button = dialogView.findViewById(R.id.btnCancelFilter)
        val btnApply: Button = dialogView.findViewById(R.id.btnApplyFilter)
        
        // Date tracking
        var selectedStartDate: Long? = null
        var selectedEndDate: Long? = null
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        
        // Setup date pickers
        btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day, 0, 0, 0)
                    selectedStartDate = calendar.timeInMillis
                    btnStartDate.text = dateFormat.format(Date(selectedStartDate!!))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59, 59)
                    selectedEndDate = calendar.timeInMillis
                    btnEndDate.text = dateFormat.format(Date(selectedEndDate!!))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        // Setup Month Spinner
        val months = listOf("All", "January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December")
        val monthAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter
        
        // Setup Year Spinner (last 5 years to next 2 years)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf("All")
        years.addAll((currentYear - 5..currentYear + 2).map { it.toString() })
        val yearAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        
        // Set current year as default
        val currentYearIndex = years.indexOf(currentYear.toString())
        if (currentYearIndex != -1) {
            spinnerYear.setSelection(currentYearIndex)
        }
        
        // Store checkbox references
        val categoryCheckboxes = mutableMapOf<Long, CheckBox>()
        val bankCheckboxes = mutableMapOf<String, CheckBox>()
        val upiCheckboxes = mutableMapOf<String, CheckBox>()
        
        // Auto-populate categories from database
        viewModel.allCategories.observe(this) { categoryList ->
            layoutCategories.removeAllViews()
            categoryCheckboxes.clear()
            
            categoryList.forEach { category ->
                val checkbox = CheckBox(this)
                checkbox.text = category.name
                checkbox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                categoryCheckboxes[category.id] = checkbox
                layoutCategories.addView(checkbox)
            }
        }
        
        // Auto-populate banks and UPI apps from database
        viewModel.allBankUpi.observe(this) { bankUpiList ->
            // Clear existing views
            layoutBanks.removeAllViews()
            layoutUpiApps.removeAllViews()
            bankCheckboxes.clear()
            upiCheckboxes.clear()
            
            // Separate banks and UPI apps
            val banks = bankUpiList.filter { it.type == BankUpiType.BANK }
            val upiApps = bankUpiList.filter { it.type == BankUpiType.UPI }
            
            // Add bank checkboxes
            banks.forEach { bank ->
                val checkbox = CheckBox(this)
                checkbox.text = bank.name
                checkbox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                bankCheckboxes[bank.name] = checkbox
                layoutBanks.addView(checkbox)
            }
            
            // Add UPI app checkboxes
            upiApps.forEach { upi ->
                val checkbox = CheckBox(this)
                checkbox.text = upi.name
                checkbox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                upiCheckboxes[upi.name] = checkbox
                layoutUpiApps.addView(checkbox)
            }
        }
        
        // Clear filters button
        btnClear.setOnClickListener {
            clearFilters()
            dialog.dismiss()
        }
        
        // Cancel button
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // Apply filters button
        btnApply.setOnClickListener {
            // Get amount range
            val minAmount = etMinAmount.text.toString().toDoubleOrNull()
            val maxAmount = etMaxAmount.text.toString().toDoubleOrNull()
            
            // Get month/year selection
            val selectedMonth = spinnerMonth.selectedItemPosition // 0 = All, 1-12 = Jan-Dec
            val selectedYearStr = spinnerYear.selectedItem.toString()
            val selectedYear = if (selectedYearStr == "All") null else selectedYearStr.toIntOrNull()
            
            // Calculate date range from month/year if selected
            var monthStartDate: Long? = null
            var monthEndDate: Long? = null
            
            if (selectedMonth > 0 && selectedYear != null) {
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                monthStartDate = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                monthEndDate = calendar.timeInMillis
            }
            
            // Use month/year dates if selected, otherwise use date range
            val finalStartDate = monthStartDate ?: selectedStartDate
            val finalEndDate = monthEndDate ?: selectedEndDate
            
            // Get selected payment modes
            val selectedPaymentModes = mutableListOf<PaymentMode>()
            if (cbCreditCard.isChecked) selectedPaymentModes.add(PaymentMode.CREDIT_CARD)
            if (cbDebitCard.isChecked) selectedPaymentModes.add(PaymentMode.DEBIT_CARD)
            if (cbUpi.isChecked) selectedPaymentModes.add(PaymentMode.UPI)
            if (cbCash.isChecked) selectedPaymentModes.add(PaymentMode.CASH)
            
            // Get selected categories
            val selectedCategoryIds = categoryCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Get selected banks
            val selectedBanks = bankCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Get selected UPI apps
            val selectedUpiApps = upiCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Apply filters
            applyFilters(
                minAmount,
                maxAmount,
                finalStartDate,
                finalEndDate,
                selectedPaymentModes,
                selectedCategoryIds,
                selectedBanks,
                selectedUpiApps
            )
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun applyFilters(
        minAmount: Double?,
        maxAmount: Double?,
        startDate: Long?,
        endDate: Long?,
        paymentModes: List<PaymentMode>,
        categoryIds: List<Long>,
        banks: List<String>,
        upiApps: List<String>
    ) {
        isFiltering = true
        
        // Get all expenses and filter them
        viewModel.allExpenses.observe(this) { allExpenses ->
            var filteredExpenses = allExpenses
            
            // Filter by amount range
            if (minAmount != null) {
                filteredExpenses = filteredExpenses.filter { it.amount >= minAmount }
            }
            if (maxAmount != null) {
                filteredExpenses = filteredExpenses.filter { it.amount <= maxAmount }
            }
            
            // Filter by date range
            if (startDate != null) {
                filteredExpenses = filteredExpenses.filter { it.date >= startDate }
            }
            if (endDate != null) {
                filteredExpenses = filteredExpenses.filter { it.date <= endDate }
            }
            
            // Filter by payment modes
            if (paymentModes.isNotEmpty()) {
                filteredExpenses = filteredExpenses.filter { it.paymentMode in paymentModes }
            }
            
            // Filter by categories
            if (categoryIds.isNotEmpty()) {
                filteredExpenses = filteredExpenses.filter { it.categoryId in categoryIds }
            }
            
            // Filter by banks
            if (banks.isNotEmpty()) {
                filteredExpenses = filteredExpenses.filter { it.bankName in banks }
            }
            
            // Filter by UPI apps
            if (upiApps.isNotEmpty()) {
                filteredExpenses = filteredExpenses.filter { it.upiApp in upiApps }
            }
            
            // Update UI
            if (filteredExpenses.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                adapter.submitList(filteredExpenses)
            }
            
            // Update total
            val total = filteredExpenses.sumOf { it.amount }
            totalAmountView.text = "Total: ₹${String.format("%.2f", total)}"
        }
        
        Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show()
    }
    
    private fun showManageCategoriesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manage_categories, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val categoryNameEdit: EditText = dialogView.findViewById(R.id.etCategoryName)
        val btnAddCategory: Button = dialogView.findViewById(R.id.btnAddCategory)
        val rvCategories: RecyclerView = dialogView.findViewById(R.id.rvCategories)
        val btnClose: Button = dialogView.findViewById(R.id.btnCloseCategories)
        
        // Setup RecyclerView for categories
        val categoryAdapter = CategoryAdapter(
            onEditClick = { category -> showEditCategoryDialog(category) },
            onDeleteClick = { category -> 
                AlertDialog.Builder(this)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete '${category.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteCategory(category)
                        Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        
        rvCategories.adapter = categoryAdapter
        rvCategories.layoutManager = LinearLayoutManager(this)
        
        viewModel.allCategories.observe(this) { categoryList ->
            categoryAdapter.submitList(categoryList)
        }
        
        btnAddCategory.setOnClickListener {
            val name = categoryNameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val category = Category(name = name)
            viewModel.insertCategory(category)
            categoryNameEdit.text.clear()
            Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditCategoryDialog(category: Category) {
        val editText = EditText(this)
        editText.setText(category.name)
        editText.hint = "Category name"
        
        AlertDialog.Builder(this)
            .setTitle("Edit Category")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val updatedCategory = category.copy(name = newName)
                viewModel.updateCategory(updatedCategory)
                Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showManageBankUpiDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manage_banks, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val bankUpiNameEdit: EditText = dialogView.findViewById(R.id.etBankUpiName)
        val btnAddBankUpi: Button = dialogView.findViewById(R.id.btnAddBankUpi)
        val rgType: RadioGroup = dialogView.findViewById(R.id.rgBankUpiType)
        val rbBank: RadioButton = dialogView.findViewById(R.id.rbBank)
        val rbUpi: RadioButton = dialogView.findViewById(R.id.rbUpi)
        val rvBankUpi: RecyclerView = dialogView.findViewById(R.id.rvBankUpi)
        val btnClose: Button = dialogView.findViewById(R.id.btnCloseBankUpi)
        val tvExisting: TextView = dialogView.findViewById(R.id.tvExistingBankUpi)
        
        // Setup RecyclerView for bank/UPI
        val bankUpiAdapter = BankUpiAdapter(
            onEditClick = { bankUpi -> showEditBankUpiDialog(bankUpi) },
            onDeleteClick = { bankUpi -> 
                AlertDialog.Builder(this)
                    .setTitle("Delete ${if (bankUpi.type == BankUpiType.BANK) "Bank" else "UPI App"}")
                    .setMessage("Are you sure you want to delete '${bankUpi.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteBankUpi(bankUpi)
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        
        rvBankUpi.adapter = bankUpiAdapter
        rvBankUpi.layoutManager = LinearLayoutManager(this)
        
        // Function to update the list based on selected type
        fun updateListForType(type: BankUpiType) {
            viewModel.allBankUpi.observe(this) { list ->
                val filteredList = list.filter { it.type == type }
                bankUpiAdapter.submitList(filteredList)
            }
            
            // Update label
            tvExisting.text = if (type == BankUpiType.BANK) "Existing Banks" else "Existing UPI Apps"
            
            // Update hint
            bankUpiNameEdit.hint = if (type == BankUpiType.BANK) "Bank name" else "UPI App name"
        }
        
        // Initially show banks
        updateListForType(BankUpiType.BANK)
        
        // Listen for radio button changes
        rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBank -> updateListForType(BankUpiType.BANK)
                R.id.rbUpi -> updateListForType(BankUpiType.UPI)
            }
        }
        
        btnAddBankUpi.setOnClickListener {
            val name = bankUpiNameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val type = if (rgType.checkedRadioButtonId == R.id.rbBank) {
                BankUpiType.BANK
            } else {
                BankUpiType.UPI
            }
            
            val bankUpi = BankUpi(name = name, type = type)
            viewModel.insertBankUpi(bankUpi)
            bankUpiNameEdit.text.clear()
            Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show()
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditBankUpiDialog(bankUpi: BankUpi) {
        val editText = EditText(this)
        editText.setText(bankUpi.name)
        editText.hint = "Name"
        
        AlertDialog.Builder(this)
            .setTitle("Edit ${if (bankUpi.type == BankUpiType.BANK) "Bank" else "UPI App"}")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val updated = bankUpi.copy(name = newName)
                viewModel.updateBankUpi(updated)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAddEditDialog(expense: Expense?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (expense == null) "Add Expense" else "Edit Expense")
            .create()
        
        val dateButton: Button = dialogView.findViewById(R.id.btnSelectDate)
        val amountEdit: EditText = dialogView.findViewById(R.id.etAmount)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.spinnerCategory)
        val btnManageCategories: Button = dialogView.findViewById(R.id.btnManageCategories)
        val paymentModeSpinner: Spinner = dialogView.findViewById(R.id.spinnerPaymentMode)
        val bankUpiLayout: LinearLayout = dialogView.findViewById(R.id.layoutBankUpi)
        val bankUpiSpinner: Spinner = dialogView.findViewById(R.id.spinnerBankUpi)
        val btnManageBankUpi: Button = dialogView.findViewById(R.id.btnManageBankUpi)
        val bankUpiLabel: TextView = dialogView.findViewById(R.id.tvBankUpiLabel)
        val descriptionEdit: EditText = dialogView.findViewById(R.id.etDescription)
        val saveButton: Button = dialogView.findViewById(R.id.btnSave)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancel)
        
        var selectedDate = expense?.date ?: System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateButton.text = dateFormat.format(Date(selectedDate))
        
        // Date picker
        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.timeInMillis
                    dateButton.text = dateFormat.format(Date(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        // Setup category spinner
        fun refreshCategorySpinner() {
            val categoryNames = mutableListOf("None")
            categoryNames.addAll(categories.map { it.name })
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = categoryAdapter
            
            // Pre-select category if editing
            if (expense?.categoryId != null) {
                val categoryIndex = categories.indexOfFirst { it.id == expense.categoryId }
                if (categoryIndex >= 0) {
                    categorySpinner.setSelection(categoryIndex + 1) // +1 because "None" is at index 0
                }
            }
        }
        
        // Initial setup
        refreshCategorySpinner()
        
        // Observe category changes to refresh spinner in real-time
        viewModel.allCategories.observe(this) { categoryList ->
            categories = categoryList
            refreshCategorySpinner()
        }
        
        btnManageCategories.setOnClickListener {
            showManageCategoriesDialog()
        }
        
        // Setup payment mode spinner
        val paymentModes = arrayOf("Credit Card", "Debit Card", "UPI", "Cash")
        val paymentModeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentModes)
        paymentModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentModeSpinner.adapter = paymentModeAdapter
        
        // Function to update bank/UPI spinner based on payment mode
        fun updateBankUpiSpinner(paymentMode: PaymentMode) {
            when (paymentMode) {
                PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD -> {
                    bankUpiLayout.visibility = View.VISIBLE
                    bankUpiLabel.text = "Bank Name"
                    
                    val banks = bankUpiList.filter { it.type == BankUpiType.BANK }
                    val bankNames = mutableListOf("Select Bank")
                    bankNames.addAll(banks.map { it.name })
                    
                    val bankAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, bankNames)
                    bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    bankUpiSpinner.adapter = bankAdapter
                    
                    // Pre-select bank if editing
                    if (expense?.bankName != null) {
                        val bankIndex = banks.indexOfFirst { it.name == expense.bankName }
                        if (bankIndex >= 0) {
                            bankUpiSpinner.setSelection(bankIndex + 1)
                        }
                    }
                }
                PaymentMode.UPI -> {
                    bankUpiLayout.visibility = View.VISIBLE
                    bankUpiLabel.text = "UPI App"
                    
                    val upiApps = bankUpiList.filter { it.type == BankUpiType.UPI }
                    val upiNames = mutableListOf("Select UPI App")
                    upiNames.addAll(upiApps.map { it.name })
                    
                    val upiAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, upiNames)
                    upiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    bankUpiSpinner.adapter = upiAdapter
                    
                    // Pre-select UPI if editing
                    if (expense?.upiApp != null) {
                        val upiIndex = upiApps.indexOfFirst { it.name == expense.upiApp }
                        if (upiIndex >= 0) {
                            bankUpiSpinner.setSelection(upiIndex + 1)
                        }
                    }
                }
                PaymentMode.CASH -> {
                    bankUpiLayout.visibility = View.GONE
                }
            }
        }
        
        // Observe bank/UPI changes to refresh spinner in real-time
        viewModel.allBankUpi.observe(this) { list ->
            bankUpiList = list
            // Refresh current spinner based on selected payment mode
            val currentPaymentMode = PaymentMode.values()[paymentModeSpinner.selectedItemPosition]
            updateBankUpiSpinner(currentPaymentMode)
        }
        
        paymentModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateBankUpiSpinner(PaymentMode.values()[position])
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        btnManageBankUpi.setOnClickListener {
            showManageBankUpiDialog()
        }
        
        // Pre-fill if editing
        if (expense != null) {
            amountEdit.setText(expense.amount.toString())
            paymentModeSpinner.setSelection(expense.paymentMode.ordinal)
            descriptionEdit.setText(expense.description ?: "")
        }
        
        saveButton.setOnClickListener {
            val amountStr = amountEdit.text.toString()
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val categoryId = if (categorySpinner.selectedItemPosition > 0) {
                categories[categorySpinner.selectedItemPosition - 1].id
            } else {
                null
            }
            
            val paymentMode = PaymentMode.values()[paymentModeSpinner.selectedItemPosition]
            var bankName: String? = null
            var upiApp: String? = null
            
            // Get bank/UPI value if applicable
            if (paymentMode in arrayOf(PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD)) {
                if (bankUpiSpinner.selectedItemPosition == 0) {
                    Toast.makeText(this, "Please select a bank", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                bankName = bankUpiSpinner.selectedItem.toString()
            } else if (paymentMode == PaymentMode.UPI) {
                if (bankUpiSpinner.selectedItemPosition == 0) {
                    Toast.makeText(this, "Please select a UPI app", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                upiApp = bankUpiSpinner.selectedItem.toString()
            }
            
            val newExpense = Expense(
                id = expense?.id ?: 0,
                date = selectedDate,
                amount = amount,
                paymentMode = paymentMode,
                categoryId = categoryId,
                bankName = bankName,
                upiApp = upiApp,
                description = descriptionEdit.text.toString().trim().ifEmpty { null }
            )
            
            if (expense == null) {
                viewModel.insert(newExpense)
                Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.update(newExpense)
                Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
            }
            
            dialog.dismiss()
        }
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.delete(expense)
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    // Adapter for Category list in management dialog
    private inner class CategoryAdapter(
        private val onEditClick: (Category) -> Unit,
        private val onDeleteClick: (Category) -> Unit
    ) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
        
        private var categories: List<Category> = emptyList()
        
        fun submitList(list: List<Category>) {
            categories = list
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_category, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(categories[position])
        }
        
        override fun getItemCount() = categories.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
            private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditCategory)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
            
            fun bind(category: Category) {
                tvName.text = category.name
                btnEdit.setOnClickListener { onEditClick(category) }
                btnDelete.setOnClickListener { onDeleteClick(category) }
            }
        }
    }
    
    // Adapter for BankUpi list in management dialog
    private inner class BankUpiAdapter(
        private val onEditClick: (BankUpi) -> Unit,
        private val onDeleteClick: (BankUpi) -> Unit
    ) : RecyclerView.Adapter<BankUpiAdapter.ViewHolder>() {
        
        private var items: List<BankUpi> = emptyList()
        
        fun submitList(list: List<BankUpi>) {
            items = list
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_bank_upi, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount() = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvBankUpiName)
            private val tvType: TextView = itemView.findViewById(R.id.tvBankUpiType)
            private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditBankUpi)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteBankUpi)
            
            fun bind(bankUpi: BankUpi) {
                tvName.text = bankUpi.name
                tvType.text = if (bankUpi.type == BankUpiType.BANK) "Bank" else "UPI App"
                btnEdit.setOnClickListener { onEditClick(bankUpi) }
                btnDelete.setOnClickListener { onDeleteClick(bankUpi) }
            }
        }
    }
    
    // Category Summary functionality
    data class CategorySummary(
        val categoryName: String,
        val totalAmount: Double
    )
    
    private fun updateCategorySummary(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            categorySummaryLayout.visibility = View.GONE
            return
        }
        
        // Group expenses by category and calculate totals
        val categoryTotals = mutableMapOf<String, Double>()
        
        expenses.forEach { expense ->
            val categoryName = expense.categoryId?.let { id ->
                categories.find { it.id == id }?.name
            } ?: "Uncategorized"
            
            categoryTotals[categoryName] = (categoryTotals[categoryName] ?: 0.0) + expense.amount
        }
        
        // Convert to list and sort by amount descending
        val summaryList = categoryTotals.map { (name, amount) ->
            CategorySummary(name, amount)
        }.sortedByDescending { it.totalAmount }
        
        // Show summary
        if (summaryList.isNotEmpty()) {
            categorySummaryLayout.visibility = View.VISIBLE
            categorySummaryAdapter.submitList(summaryList)
        } else {
            categorySummaryLayout.visibility = View.GONE
        }
    }
    
    // Adapter for Category Summary
    private inner class CategorySummaryAdapter : RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {
        
        private var summaries: List<CategorySummary> = emptyList()
        
        fun submitList(list: List<CategorySummary>) {
            summaries = list
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_category_summary, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(summaries[position])
        }
        
        override fun getItemCount() = summaries.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvCategorySummaryName)
            private val tvAmount: TextView = itemView.findViewById(R.id.tvCategorySummaryAmount)
            
            fun bind(summary: CategorySummary) {
                tvName.text = summary.categoryName
                tvAmount.text = "₹${String.format("%.2f", summary.totalAmount)}"
            }
        }
    }
}
