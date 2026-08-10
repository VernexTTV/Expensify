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
import com.google.android.material.card.MaterialCardView
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
    private lateinit var categorySummaryLayout: MaterialCardView
    private lateinit var categorySummaryRecyclerView: RecyclerView
    private lateinit var categorySummaryAdapter: CategorySummaryAdapter
    private lateinit var expensesHeaderView: TextView
    
    private var categories: List<Category> = emptyList()
    private var bankUpiList: List<BankUpi> = emptyList()
    private var expenseSourceList: List<ExpenseSource> = emptyList()
    private var paymentMethodList: List<PaymentMethod> = emptyList()
    
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
            supportActionBar?.title = "Expensify"
            
            android.util.Log.d("ExpenseTracker", "Step 4: Initialize views")
            // Initialize views
            recyclerView = findViewById(R.id.recyclerView)
            emptyView = findViewById(R.id.tvEmpty)
            totalAmountView = findViewById(R.id.tvTotalAmount)
            fabAdd = findViewById(R.id.fabAdd)
            categorySummaryLayout = findViewById(R.id.layoutCategorySummary)
            categorySummaryRecyclerView = findViewById(R.id.rvCategorySummary)
            expensesHeaderView = findViewById(R.id.tvExpensesHeader)
            
            android.util.Log.d("ExpenseTracker", "Step 5: Setup RecyclerView adapter")
            // Setup RecyclerView
            adapter = ExpenseAdapter(
                onEditClick = { expense -> showAddEditDialog(expense) },
                onDeleteClick = { expense -> showDeleteConfirmation(expense) },
                getCategoryName = { categoryId ->
                    categoryId?.let { id ->
                        categories.find { it.id == id }?.name
                    }
                },
                getPaymentMethodName = { paymentMethodId ->
                    paymentMethodId?.let { id ->
                        paymentMethodList.find { it.id == id }?.name
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
            // Observe current expenses (switches between all and filtered)
            viewModel.currentExpenses.observe(this) { expenses ->
                val isFiltering = viewModel.isFiltering()
                
                // Update header text based on filter state
                expensesHeaderView.text = if (isFiltering) "Expenses" else "All Expenses"
                
                if (expenses.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    categorySummaryLayout.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    // Show different message based on whether filtering or not
                    emptyView.text = if (isFiltering) {
                        "No expenses match your filters\nTry adjusting the filter criteria"
                    } else {
                        "No expenses yet\nTap + to add"
                    }
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    adapter.submitList(expenses)
                    // Category summary auto-updates via ViewModel MediatorLiveData
                }
            }
            
            android.util.Log.d("ExpenseTracker", "Step 9: Observe total expenses")
            // Observe current total (switches between all and filtered)
            viewModel.currentTotal.observe(this) { total ->
                val totalText = if (total != null && total > 0) {
                    "Total: ₹${String.format("%.2f", total)}"
                } else {
                    "Total: ₹0.00"
                }
                totalAmountView.text = totalText
            }
            
            android.util.Log.d("ExpenseTracker", "Step 10: Observe loading state")
            // Observe loading state (no progressBar in layout, skipped)
            
            android.util.Log.d("ExpenseTracker", "Step 11: Observe error messages")
            // Observe error messages
            viewModel.errorMessage.observe(this) { errorMsg ->
                if (!errorMsg.isNullOrEmpty()) {
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
            
            android.util.Log.d("ExpenseTracker", "Step 12: Observe category summary")
            // Observe category summary
            viewModel.categorySummary.observe(this) { summaries ->
                if (summaries.isNotEmpty()) {
                    categorySummaryLayout.visibility = View.VISIBLE
                    categorySummaryAdapter.submitList(summaries.map { 
                        CategorySummary(it.categoryName, it.totalAmount) 
                    })
                } else {
                    categorySummaryLayout.visibility = View.GONE
                }
            }
            
            android.util.Log.d("ExpenseTracker", "Step 13: Observe categories")
            // Observe categories
            viewModel.allCategories.observe(this) { categoryList ->
                categories = categoryList
            }
            
            android.util.Log.d("ExpenseTracker", "Step 14: Observe bank/UPI list")
            // Observe bank/UPI list
            viewModel.allBankUpi.observe(this) { list ->
                bankUpiList = list
            }
            
            android.util.Log.d("ExpenseTracker", "Step 14.5: Observe expense sources")
            // Observe expense sources
            viewModel.allExpenseSources.observe(this) { list ->
                expenseSourceList = list
            }
            
            android.util.Log.d("ExpenseTracker", "Step 14.6: Observe payment methods")
            // Observe payment methods
            viewModel.allPaymentMethods.observe(this) { list ->
                paymentMethodList = list
            }
            
            android.util.Log.d("ExpenseTracker", "Step 15: Setup FAB")
            // Setup FAB
            fabAdd.setOnClickListener {
                showAddEditDialog(null)
            }
            
            android.util.Log.d("ExpenseTracker", "Step 16: Setup Filter Button")
            // Setup Filter Button (if exists in layout)
            val btnFilter: android.widget.Button? = findViewById(R.id.btnFilter)
            btnFilter?.setOnClickListener {
                showFilterDialog()
            }
            
            android.util.Log.d("ExpenseTracker", "Step 17: Setup SearchView")
            // Setup SearchView
            val searchView: androidx.appcompat.widget.SearchView = findViewById(R.id.searchView)
            
            // Set hint text programmatically to ensure visibility
            searchView.queryHint = "Search by Amount, Category, Payment Method & Description"
            
            // Customize SearchView appearance - aggressive approach
            try {
                // Remove padding from SearchView itself
                searchView.setPadding(0, searchView.paddingTop, searchView.paddingRight, searchView.paddingBottom)
                
                // Get the search plate (the container of icon and text)
                val searchPlate = searchView.findViewById<android.view.View>(androidx.appcompat.R.id.search_plate)
                searchPlate?.setPadding(0, 0, 0, 0)
                
                // Get the search EditText
                val searchEditText = searchView.findViewById<android.widget.EditText>(androidx.appcompat.R.id.search_src_text)
                searchEditText?.textSize = 9f // Smaller text size
                searchEditText?.setPaddingRelative(4, 0, 0, 0) // Minimal left padding
                
                // Get the search icon and remove all margins/padding
                val searchIcon = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_mag_icon)
                searchIcon?.let {
                    it.setPadding(0, 0, 0, 0)
                    val params = it.layoutParams as? android.view.ViewGroup.MarginLayoutParams
                    params?.let { lp ->
                        lp.leftMargin = 0
                        lp.rightMargin = 0
                        lp.marginStart = 0
                        lp.marginEnd = 4 // Small gap between icon and text
                        it.layoutParams = lp
                    }
                }
                
            } catch (e: Exception) {
                android.util.Log.w("ExpenseTracker", "Could not customize SearchView", e)
            }
            
            searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        if (it.isNotBlank()) {
                            viewModel.searchExpenses(it)
                        }
                    }
                    return true
                }
                
                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText.isNullOrBlank()) {
                        viewModel.clearSearch()
                    } else {
                        viewModel.searchExpenses(newText)
                    }
                    return true
                }
            })
            
            android.util.Log.d("ExpenseTracker", "=== MainActivity onCreate SUCCESS ===")
        } catch (e: Exception) {
            android.util.Log.e("ExpenseTracker", "=== MainActivity onCreate FAILED ===", e)
            throw e
        }
    }
    
    
    private fun clearFilters() {
        viewModel.clearFilters()
        Toast.makeText(this, "Filters cleared", Toast.LENGTH_SHORT).show()
    }
    
    private fun showFilterDialog() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        bottomSheet.setContentView(dialogView)
        
        // Amount range inputs
        val etMinAmount: EditText = dialogView.findViewById(R.id.etMinAmount)
        val etMaxAmount: EditText = dialogView.findViewById(R.id.etMaxAmount)
        
        // Date range buttons
        val btnStartDate: Button = dialogView.findViewById(R.id.btnSelectStartDate)
        val btnEndDate: Button = dialogView.findViewById(R.id.btnSelectEndDate)
        
        // Month/Year spinners
        val spinnerMonth: android.widget.Spinner = dialogView.findViewById(R.id.spinnerMonth)
        val spinnerYear: android.widget.Spinner = dialogView.findViewById(R.id.spinnerYear)
        
        // Dynamic checkbox containers
        val layoutPaymentMethods: LinearLayout = dialogView.findViewById(R.id.layoutPaymentMethods)
        val layoutExpenseSources: LinearLayout = dialogView.findViewById(R.id.layoutExpenseSources)
        val layoutCategories: LinearLayout = dialogView.findViewById(R.id.layoutCategories)
        
        // Action buttons
        val btnClear: Button = dialogView.findViewById(R.id.btnClearFilters)
        val btnApply: Button = dialogView.findViewById(R.id.btnApplyFilter)
        
        // Get current filter state from ViewModel
        val currentFilterState = viewModel.filterState.value
        
        // Pre-populate amount fields with current filter
        currentFilterState?.minAmount?.let {
            etMinAmount.setText(it.toString())
        }
        currentFilterState?.maxAmount?.let {
            etMaxAmount.setText(it.toString())
        }
        
        // Date tracking with explicit timezone
        var selectedStartDate: Long? = currentFilterState?.startDate
        var selectedEndDate: Long? = currentFilterState?.endDate
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeZone = java.util.TimeZone.getDefault()
        dateFormat.timeZone = timeZone
        
        // Setup Year List (declare early so it's available in date picker callbacks)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = mutableListOf("All")
        years.addAll((currentYear - 5..currentYear + 2).map { it.toString() })
        
        // Show current start date if exists
        if (selectedStartDate != null) {
            btnStartDate.text = dateFormat.format(Date(selectedStartDate!!))
        }
        
        // Show current end date if exists
        if (selectedEndDate != null) {
            btnEndDate.text = dateFormat.format(Date(selectedEndDate!!))
        }
        
        // Setup date pickers with explicit timezone
        btnStartDate.setOnClickListener {
            val calendar = Calendar.getInstance(timeZone)
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.clear()
                    calendar.timeZone = timeZone
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    selectedStartDate = calendar.timeInMillis
                    btnStartDate.text = dateFormat.format(Date(selectedStartDate!!))
                    
                    // Clear month/year selection when custom date is selected
                    spinnerMonth.setSelection(0)
                    spinnerYear.setSelection(years.indexOf("All"))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        btnEndDate.setOnClickListener {
            val calendar = Calendar.getInstance(timeZone)
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.clear()
                    calendar.timeZone = timeZone
                    calendar.set(year, month, day, 23, 59, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    selectedEndDate = calendar.timeInMillis
                    btnEndDate.text = dateFormat.format(Date(selectedEndDate!!))
                    
                    // Clear month/year selection when custom date is selected
                    spinnerMonth.setSelection(0)
                    spinnerYear.setSelection(years.indexOf("All"))
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
        val yearAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        
        // Restore month/year selections from filter state
        if (currentFilterState?.selectedMonth != null && currentFilterState.selectedMonth > 0) {
            spinnerMonth.setSelection(currentFilterState.selectedMonth)
        }
        if (currentFilterState?.selectedYear != null) {
            val yearIndex = years.indexOf(currentFilterState.selectedYear.toString())
            if (yearIndex != -1) {
                spinnerYear.setSelection(yearIndex)
            }
        } else if (selectedStartDate == null && selectedEndDate == null) {
            // Set current year as default ONLY if no filter state at all (no custom dates either)
            val currentYearIndex = years.indexOf(currentYear.toString())
            if (currentYearIndex != -1) {
                spinnerYear.setSelection(currentYearIndex)
            }
        } else {
            // If custom dates exist, set year to "All"
            spinnerYear.setSelection(0)
        }
        
        // Add listeners to clear custom dates when month/year is selected
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // If not "All"
                    // Clear custom date selection
                    selectedStartDate = null
                    selectedEndDate = null
                    btnStartDate.text = "Select Start Date"
                    btnEndDate.text = "Select End Date"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // If not "All"
                    // Clear custom date selection
                    selectedStartDate = null
                    selectedEndDate = null
                    btnStartDate.text = "Select Start Date"
                    btnEndDate.text = "Select End Date"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Store checkbox references
        val paymentMethodCheckboxes = mutableMapOf<Long, CheckBox>()
        val expenseSourceCheckboxes = mutableMapOf<Long, CheckBox>()
        val categoryCheckboxes = mutableMapOf<Long, CheckBox>()
        
        // Auto-populate payment methods from database
        viewModel.allPaymentMethods.observe(this) { methodList ->
            layoutPaymentMethods.removeAllViews()
            paymentMethodCheckboxes.clear()
            
            methodList.forEach { method ->
                val checkbox = CheckBox(this)
                checkbox.text = method.name
                checkbox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                
                // Pre-check if this method was previously selected
                checkbox.isChecked = currentFilterState?.paymentMethodIds?.contains(method.id) == true
                
                paymentMethodCheckboxes[method.id] = checkbox
                layoutPaymentMethods.addView(checkbox)
            }
        }
        
        // Auto-populate expense sources from database
        viewModel.allExpenseSources.observe(this) { sourceList ->
            layoutExpenseSources.removeAllViews()
            expenseSourceCheckboxes.clear()
            
            sourceList.forEach { source ->
                val checkbox = CheckBox(this)
                checkbox.text = source.name
                checkbox.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                
                // Pre-check if this source was previously selected
                checkbox.isChecked = currentFilterState?.expenseSourceIds?.contains(source.id) == true
                
                expenseSourceCheckboxes[source.id] = checkbox
                layoutExpenseSources.addView(checkbox)
            }
        }
        
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
                
                // Pre-check if this category was previously selected
                checkbox.isChecked = currentFilterState?.categoryIds?.contains(category.id) == true
                
                categoryCheckboxes[category.id] = checkbox
                layoutCategories.addView(checkbox)
            }
        }
        
        // Clear filters button
        btnClear.setOnClickListener {
            clearFilters()
            bottomSheet.dismiss()
        }
        
        // Cancel button - removed as bottom sheet can be dismissed by swiping
        
        // Apply filters button
        btnApply.setOnClickListener {
            // Get amount range
            val minAmount = etMinAmount.text.toString().toDoubleOrNull()
            val maxAmount = etMaxAmount.text.toString().toDoubleOrNull()
            
            // Get month/year selection
            val selectedMonth = spinnerMonth.selectedItemPosition // 0 = All, 1-12 = Jan-Dec
            val selectedYearStr = spinnerYear.selectedItem.toString()
            val selectedYear = if (selectedYearStr == "All") null else selectedYearStr.toIntOrNull()
            
            // Check if both date selection methods are used (shouldn't happen with auto-clear, but validate anyway)
            val hasCustomDateRange = selectedStartDate != null || selectedEndDate != null
            val hasMonthYearSelection = selectedMonth > 0 && selectedYear != null
            
            if (hasCustomDateRange && hasMonthYearSelection) {
                Toast.makeText(
                    this,
                    "Please use either Custom Date Range OR Month/Year selection, not both",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            
            // Calculate date range from month/year if selected
            var monthStartDate: Long? = null
            var monthEndDate: Long? = null
            
            if (selectedMonth > 0 && selectedYear != null) {
                val calendar = Calendar.getInstance(timeZone)
                calendar.clear()
                calendar.timeZone = timeZone
                calendar.set(selectedYear, selectedMonth - 1, 1, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                monthStartDate = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                monthEndDate = calendar.timeInMillis
            }
            
            // Use month/year dates if selected, otherwise use custom date range
            val finalStartDate = monthStartDate ?: selectedStartDate
            val finalEndDate = monthEndDate ?: selectedEndDate
            
            // Get selected payment methods
            val selectedPaymentMethodIds = paymentMethodCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Get selected expense sources
            val selectedExpenseSourceIds = expenseSourceCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Get selected categories
            val selectedCategoryIds = categoryCheckboxes.filter { it.value.isChecked }.keys.toList()
            
            // Store month/year for filter state (0 = All for month, null = All for year)
            val monthForState = if (selectedMonth > 0) selectedMonth else null
            val yearForState = if (selectedYearStr != "All") selectedYear else null
            
            // Apply filters
            applyFilters(
                minAmount,
                maxAmount,
                finalStartDate,
                finalEndDate,
                selectedPaymentMethodIds,
                selectedCategoryIds,
                selectedExpenseSourceIds,
                monthForState,
                yearForState
            )
            
            bottomSheet.dismiss()
        }
        
        bottomSheet.show()
    }
    
    private fun applyFilters(
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
        // Use ViewModel's applyFilters method - no memory leak!
        viewModel.applyFilters(minAmount, maxAmount, startDate, endDate, paymentMethodIds, categoryIds, expenseSourceIds, selectedMonth, selectedYear)
        
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
        val rbWallet: RadioButton = dialogView.findViewById(R.id.rbWallet)
        val rvBankUpi: RecyclerView = dialogView.findViewById(R.id.rvBankUpi)
        val btnClose: Button = dialogView.findViewById(R.id.btnCloseBankUpi)
        val tvExisting: TextView = dialogView.findViewById(R.id.tvExistingBankUpi)
        
        // Setup RecyclerView for bank/UPI/Wallet
        val bankUpiAdapter = BankUpiAdapter(
            onEditClick = { bankUpi -> showEditBankUpiDialog(bankUpi) },
            onDeleteClick = { bankUpi -> 
                val typeName = when (bankUpi.type) {
                    BankUpiType.BANK -> "Bank"
                    BankUpiType.UPI -> "UPI App"
                    BankUpiType.WALLET -> "Wallet"
                }
                AlertDialog.Builder(this)
                    .setTitle("Delete $typeName")
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
            
            // Update label and hint based on type
            when (type) {
                BankUpiType.BANK -> {
                    tvExisting.text = "Existing Banks"
                    bankUpiNameEdit.hint = "Bank name"
                }
                BankUpiType.UPI -> {
                    tvExisting.text = "Existing UPI Apps"
                    bankUpiNameEdit.hint = "UPI App name"
                }
                BankUpiType.WALLET -> {
                    tvExisting.text = "Existing Wallets"
                    bankUpiNameEdit.hint = "Wallet name"
                }
            }
        }
        
        // Initially show banks
        updateListForType(BankUpiType.BANK)
        
        // Listen for radio button changes
        rgType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBank -> updateListForType(BankUpiType.BANK)
                R.id.rbUpi -> updateListForType(BankUpiType.UPI)
                R.id.rbWallet -> updateListForType(BankUpiType.WALLET)
            }
        }
        
        btnAddBankUpi.setOnClickListener {
            val name = bankUpiNameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val type = when (rgType.checkedRadioButtonId) {
                R.id.rbBank -> BankUpiType.BANK
                R.id.rbUpi -> BankUpiType.UPI
                R.id.rbWallet -> BankUpiType.WALLET
                else -> BankUpiType.BANK
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
        
        val typeName = when (bankUpi.type) {
            BankUpiType.BANK -> "Bank"
            BankUpiType.UPI -> "UPI App"
            BankUpiType.WALLET -> "Wallet"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit $typeName")
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
    
    private fun showManageExpenseSourcesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manage_expense_sources, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val expenseSourceNameEdit: EditText = dialogView.findViewById(R.id.etExpenseSourceName)
        val btnAddExpenseSource: Button = dialogView.findViewById(R.id.btnAddExpenseSource)
        val rvExpenseSources: RecyclerView = dialogView.findViewById(R.id.rvExpenseSources)
        val btnClose: Button = dialogView.findViewById(R.id.btnCloseExpenseSource)
        
        // Setup RecyclerView for expense sources
        val expenseSourceAdapter = ExpenseSourceAdapter(
            onEditClick = { expenseSource -> showEditExpenseSourceDialog(expenseSource) },
            onDeleteClick = { expenseSource -> 
                AlertDialog.Builder(this)
                    .setTitle("Delete Expense Source")
                    .setMessage("Are you sure you want to delete '${expenseSource.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteExpenseSource(expenseSource)
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        
        rvExpenseSources.adapter = expenseSourceAdapter
        rvExpenseSources.layoutManager = LinearLayoutManager(this)
        
        // Observe expense sources
        viewModel.allExpenseSources.observe(this) { list ->
            expenseSourceAdapter.submitList(list)
        }
        
        btnAddExpenseSource.setOnClickListener {
            val name = expenseSourceNameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val expenseSource = ExpenseSource(name = name)
            viewModel.insertExpenseSource(expenseSource)
            expenseSourceNameEdit.text.clear()
            Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show()
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditExpenseSourceDialog(expenseSource: ExpenseSource) {
        val editText = EditText(this)
        editText.setText(expenseSource.name)
        editText.hint = "Name"
        
        AlertDialog.Builder(this)
            .setTitle("Edit Expense Source")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val updated = expenseSource.copy(name = newName)
                viewModel.updateExpenseSource(updated)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showManagePaymentMethodsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_manage_payment_methods, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val paymentMethodNameEdit: EditText = dialogView.findViewById(R.id.etPaymentMethodName)
        val btnAddPaymentMethod: Button = dialogView.findViewById(R.id.btnAddPaymentMethod)
        val rvPaymentMethods: RecyclerView = dialogView.findViewById(R.id.rvPaymentMethods)
        val btnClose: Button = dialogView.findViewById(R.id.btnClosePaymentMethod)
        
        // Setup RecyclerView for payment methods
        val paymentMethodAdapter = PaymentMethodAdapter(
            onEditClick = { paymentMethod -> showEditPaymentMethodDialog(paymentMethod) },
            onDeleteClick = { paymentMethod -> 
                AlertDialog.Builder(this)
                    .setTitle("Delete Payment Method")
                    .setMessage("Are you sure you want to delete '${paymentMethod.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deletePaymentMethod(paymentMethod)
                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        
        rvPaymentMethods.adapter = paymentMethodAdapter
        rvPaymentMethods.layoutManager = LinearLayoutManager(this)
        
        // Observe payment methods
        viewModel.allPaymentMethods.observe(this) { list ->
            paymentMethodAdapter.submitList(list)
        }
        
        btnAddPaymentMethod.setOnClickListener {
            val name = paymentMethodNameEdit.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val paymentMethod = PaymentMethod(name = name)
            viewModel.insertPaymentMethod(paymentMethod)
            paymentMethodNameEdit.text.clear()
            Toast.makeText(this, "Added successfully", Toast.LENGTH_SHORT).show()
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showEditPaymentMethodDialog(paymentMethod: PaymentMethod) {
        val editText = EditText(this)
        editText.setText(paymentMethod.name)
        editText.hint = "Name"
        
        AlertDialog.Builder(this)
            .setTitle("Edit Payment Method")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val updated = paymentMethod.copy(name = newName)
                viewModel.updatePaymentMethod(updated)
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
        val expenseSourceSpinner: Spinner = dialogView.findViewById(R.id.spinnerExpenseSource)
        val btnManageExpenseSources: Button = dialogView.findViewById(R.id.btnManageExpenseSources)
        val descriptionEdit: EditText = dialogView.findViewById(R.id.etDescription)
        val saveButton: Button = dialogView.findViewById(R.id.btnSave)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancel)
        
        var selectedDate = expense?.date ?: System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeZone = java.util.TimeZone.getDefault()
        dateFormat.timeZone = timeZone
        dateButton.text = dateFormat.format(Date(selectedDate))
        
        // Date picker with explicit timezone
        dateButton.setOnClickListener {
            val calendar = Calendar.getInstance(timeZone)
            calendar.timeInMillis = selectedDate
            
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.clear()
                    calendar.timeZone = timeZone
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
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
        
        // Setup payment method spinner with manage button
        val btnManagePaymentMethods: Button = dialogView.findViewById(R.id.btnManagePaymentMethods)
        
        fun refreshPaymentMethodSpinner() {
            val methodNames = paymentMethodList.map { it.name }
            val methodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, methodNames)
            methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            paymentModeSpinner.adapter = methodAdapter
            
            // Pre-select payment method if editing
            if (expense?.paymentMethodId != null) {
                val methodIndex = paymentMethodList.indexOfFirst { it.id == expense.paymentMethodId }
                if (methodIndex >= 0) {
                    paymentModeSpinner.setSelection(methodIndex)
                }
            }
        }
        
        // Initial setup
        refreshPaymentMethodSpinner()
        
        // Observe payment method changes to refresh spinner in real-time
        viewModel.allPaymentMethods.observe(this) { list ->
            paymentMethodList = list
            val currentSelection = paymentModeSpinner.selectedItemPosition
            refreshPaymentMethodSpinner()
            // Try to maintain selection if possible
            if (currentSelection < paymentMethodList.size) {
                paymentModeSpinner.setSelection(currentSelection)
            }
        }
        
        btnManagePaymentMethods.setOnClickListener {
            showManagePaymentMethodsDialog()
        }
        
        // Function to update bank/UPI spinner based on payment method name
        fun updateBankUpiSpinner(paymentMethodName: String) {
            when {
                paymentMethodName.contains("Credit", ignoreCase = true) || paymentMethodName.contains("Debit", ignoreCase = true) -> {
                    bankUpiLayout.visibility = View.VISIBLE
                    bankUpiLabel.text = "🏦 Bank Name"
                    
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
                paymentMethodName.contains("UPI", ignoreCase = true) -> {
                    bankUpiLayout.visibility = View.VISIBLE
                    bankUpiLabel.text = "📱 UPI App"
                    
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
                paymentMethodName.contains("Wallet", ignoreCase = true) -> {
                    bankUpiLayout.visibility = View.VISIBLE
                    bankUpiLabel.text = "👛 Wallet"
                    
                    val wallets = bankUpiList.filter { it.type == BankUpiType.WALLET }
                    val walletNames = mutableListOf("Select Wallet")
                    walletNames.addAll(wallets.map { it.name })
                    
                    val walletAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, walletNames)
                    walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    bankUpiSpinner.adapter = walletAdapter
                    
                    // Pre-select wallet if editing (stored in bankName field)
                    if (expense?.bankName != null) {
                        val walletIndex = wallets.indexOfFirst { it.name == expense.bankName }
                        if (walletIndex >= 0) {
                            bankUpiSpinner.setSelection(walletIndex + 1)
                        }
                    }
                }
                else -> {
                    bankUpiLayout.visibility = View.GONE
                }
            }
        }
        
        // Observe bank/UPI changes to refresh spinner in real-time
        viewModel.allBankUpi.observe(this) { list ->
            bankUpiList = list
            // Refresh current spinner based on selected payment method
            if (paymentMethodList.isNotEmpty() && paymentModeSpinner.selectedItemPosition >= 0 && paymentModeSpinner.selectedItemPosition < paymentMethodList.size) {
                val currentMethod = paymentMethodList[paymentModeSpinner.selectedItemPosition]
                updateBankUpiSpinner(currentMethod.name)
            }
        }
        
        paymentModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < paymentMethodList.size) {
                    updateBankUpiSpinner(paymentMethodList[position].name)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        btnManageBankUpi.setOnClickListener {
            showManageBankUpiDialog()
        }
        
        // Setup expense source spinner
        fun refreshExpenseSourceSpinner() {
            val sourceNames = mutableListOf("None")
            sourceNames.addAll(expenseSourceList.map { it.name })
            val sourceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sourceNames)
            sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            expenseSourceSpinner.adapter = sourceAdapter
            
            // Pre-select expense source if editing
            if (expense?.expenseSourceId != null) {
                val sourceIndex = expenseSourceList.indexOfFirst { it.id == expense.expenseSourceId }
                if (sourceIndex >= 0) {
                    expenseSourceSpinner.setSelection(sourceIndex + 1) // +1 because "None" is at index 0
                }
            }
        }
        
        // Initial setup
        refreshExpenseSourceSpinner()
        
        // Observe expense source changes to refresh spinner in real-time
        viewModel.allExpenseSources.observe(this) { sourceList ->
            expenseSourceList = sourceList
            refreshExpenseSourceSpinner()
        }
        
        btnManageExpenseSources.setOnClickListener {
            showManageExpenseSourcesDialog()
        }
        
        // Pre-fill if editing
        if (expense != null) {
            amountEdit.setText(expense.amount.toString())
            // Payment method selection is handled in refreshPaymentMethodSpinner()
            descriptionEdit.setText(expense.description ?: "")
        }
        
        saveButton.setOnClickListener {
            val amountStr = amountEdit.text.toString().trim()
            
            // Validate amount is not empty
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
                amountEdit.error = "Amount is required"
                return@setOnClickListener
            }
            
            // Validate amount is a valid number
            val amount = amountStr.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                amountEdit.error = "Invalid number format"
                return@setOnClickListener
            }
            
            // Validate amount is positive
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                amountEdit.error = "Must be greater than 0"
                return@setOnClickListener
            }
            
            // Validate amount is not too large
            if (amount > 100000000.0) { // 10 crore limit
                Toast.makeText(this, "Amount is too large (max: ₹10,00,00,000)", Toast.LENGTH_SHORT).show()
                amountEdit.error = "Amount too large"
                return@setOnClickListener
            }
            
            // Validate decimal places (max 2)
            val decimalPlaces = if (amountStr.contains(".")) {
                amountStr.substringAfter(".").length
            } else {
                0
            }
            if (decimalPlaces > 2) {
                Toast.makeText(this, "Amount can have maximum 2 decimal places", Toast.LENGTH_SHORT).show()
                amountEdit.error = "Max 2 decimal places"
                return@setOnClickListener
            }
            
            val categoryId = if (categorySpinner.selectedItemPosition > 0) {
                categories[categorySpinner.selectedItemPosition - 1].id
            } else {
                null
            }
            
            // Get selected payment method
            val paymentMethodId = if (paymentModeSpinner.selectedItemPosition >= 0 && paymentModeSpinner.selectedItemPosition < paymentMethodList.size) {
                paymentMethodList[paymentModeSpinner.selectedItemPosition].id
            } else {
                null
            }
            
            val paymentMethodName = if (paymentModeSpinner.selectedItemPosition >= 0 && paymentModeSpinner.selectedItemPosition < paymentMethodList.size) {
                paymentMethodList[paymentModeSpinner.selectedItemPosition].name
            } else {
                ""
            }
            
            var bankName: String? = null
            var upiApp: String? = null
            
            // Get bank/UPI value if applicable (based on payment method name)
            if (paymentMethodName.contains("Credit", ignoreCase = true) || paymentMethodName.contains("Debit", ignoreCase = true)) {
                if (bankUpiSpinner.selectedItemPosition == 0) {
                    Toast.makeText(this, "Please select a bank", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                bankName = bankUpiSpinner.selectedItem.toString()
            } else if (paymentMethodName.contains("UPI", ignoreCase = true)) {
                if (bankUpiSpinner.selectedItemPosition == 0) {
                    Toast.makeText(this, "Please select a UPI app", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                upiApp = bankUpiSpinner.selectedItem.toString()
            }
            
            // Get expense source if selected
            val expenseSourceId = if (expenseSourceSpinner.selectedItemPosition > 0) {
                expenseSourceList[expenseSourceSpinner.selectedItemPosition - 1].id
            } else {
                null
            }
            
            val newExpense = Expense(
                id = expense?.id ?: 0,
                date = selectedDate,
                amount = amount,
                paymentMethodId = paymentMethodId,
                categoryId = categoryId,
                bankName = bankName,
                upiApp = upiApp,
                expenseSourceId = expenseSourceId,
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
    
    // Category Summary data class
    data class CategorySummary(
        val categoryName: String,
        val totalAmount: Double
    )
    
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
    
    // Adapter for Expense Source list in management dialog
    private inner class ExpenseSourceAdapter(
        private val onEditClick: (ExpenseSource) -> Unit,
        private val onDeleteClick: (ExpenseSource) -> Unit
    ) : RecyclerView.Adapter<ExpenseSourceAdapter.ViewHolder>() {
        
        private var items: List<ExpenseSource> = emptyList()
        
        fun submitList(list: List<ExpenseSource>) {
            items = list
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_expense_source, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount() = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvExpenseSourceName)
            private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditExpenseSource)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExpenseSource)
            
            fun bind(expenseSource: ExpenseSource) {
                tvName.text = expenseSource.name
                btnEdit.setOnClickListener { onEditClick(expenseSource) }
                btnDelete.setOnClickListener { onDeleteClick(expenseSource) }
            }
        }
    }
    
    // Adapter for Payment Method list in management dialog
    private inner class PaymentMethodAdapter(
        private val onEditClick: (PaymentMethod) -> Unit,
        private val onDeleteClick: (PaymentMethod) -> Unit
    ) : RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder>() {
        
        private var items: List<PaymentMethod> = emptyList()
        
        fun submitList(list: List<PaymentMethod>) {
            items = list
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_payment_method, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount() = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvPaymentMethodName)
            private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditPaymentMethod)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePaymentMethod)
            
            fun bind(paymentMethod: PaymentMethod) {
                tvName.text = paymentMethod.name
                btnEdit.setOnClickListener { onEditClick(paymentMethod) }
                btnDelete.setOnClickListener { onDeleteClick(paymentMethod) }
            }
        }
    }
}
