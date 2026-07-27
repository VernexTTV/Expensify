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
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var adapter: ExpenseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var totalAmountView: TextView
    private lateinit var fabAdd: FloatingActionButton
    
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Setup toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Expense Tracker"
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.tvEmpty)
        totalAmountView = findViewById(R.id.tvTotalAmount)
        fabAdd = findViewById(R.id.fabAdd)
        
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
        
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup ViewModel
        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        
        // Observe expenses
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
        
        // Observe total expenses
        viewModel.totalExpenses.observe(this) { total ->
            val totalText = if (total != null) {
                "Total: ₹${String.format("%.2f", total)}"
            } else {
                "Total: ₹0.00"
            }
            totalAmountView.text = totalText
        }
        
        // Observe categories
        viewModel.allCategories.observe(this) { categoryList ->
            categories = categoryList
        }
        
        // Observe bank/UPI list
        viewModel.allBankUpi.observe(this) { list ->
            bankUpiList = list
        }
        
        // Setup FAB
        fabAdd.setOnClickListener {
            showAddEditDialog(null)
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val monthContainer: LinearLayout = dialogView.findViewById(R.id.monthCheckboxContainer)
        val categoryContainer: LinearLayout = dialogView.findViewById(R.id.categoryCheckboxContainer)
        val cbCreditCard: CheckBox = dialogView.findViewById(R.id.cbCreditCard)
        val cbDebitCard: CheckBox = dialogView.findViewById(R.id.cbDebitCard)
        val cbUpi: CheckBox = dialogView.findViewById(R.id.cbUpi)
        val cbCash: CheckBox = dialogView.findViewById(R.id.cbCash)
        val bankUpiContainer: LinearLayout = dialogView.findViewById(R.id.bankUpiCheckboxContainer)
        val btnClear: Button = dialogView.findViewById(R.id.btnClearFilters)
        val btnApply: Button = dialogView.findViewById(R.id.btnApplyFilters)
        
        // Data structures to hold checkbox references
        val monthCheckboxes = mutableListOf<Pair<CheckBox, Pair<Long, Long>>>()
        val categoryCheckboxes = mutableListOf<Pair<CheckBox, Long>>()
        val bankUpiCheckboxes = mutableListOf<Pair<CheckBox, BankUpi>>()
        
        // 1. Create Month Checkboxes (Last 12 months)
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        
        for (i in 0..11) {
            val checkbox = CheckBox(this)
            checkbox.text = monthFormat.format(calendar.time)
            
            // Calculate start and end of this month
            val startOfMonth = getStartOfMonth(calendar.clone() as Calendar)
            val endOfMonth = getEndOfMonth(calendar.clone() as Calendar)
            
            monthCheckboxes.add(Pair(checkbox, Pair(startOfMonth, endOfMonth)))
            monthContainer.addView(checkbox)
            
            calendar.add(Calendar.MONTH, -1)
        }
        
        // 2. Create Category Checkboxes (Synced with current categories)
        for (category in categories) {
            val checkbox = CheckBox(this)
            checkbox.text = category.name
            categoryCheckboxes.add(Pair(checkbox, category.id))
            categoryContainer.addView(checkbox)
        }
        
        // 3. Payment Mode Checkboxes (Already in XML)
        // cbCreditCard, cbDebitCard, cbUpi, cbCash
        
        // 4. Create Bank/UPI Checkboxes (Synced with current bank/UPI list)
        for (bankUpi in bankUpiList) {
            val checkbox = CheckBox(this)
            checkbox.text = "${bankUpi.name} (${if (bankUpi.type == BankUpiType.BANK) "Bank" else "UPI"})"
            bankUpiCheckboxes.add(Pair(checkbox, bankUpi))
            bankUpiContainer.addView(checkbox)
        }
        
        // Clear Filters Button
        btnClear.setOnClickListener {
            clearFilters()
            dialog.dismiss()
        }
        
        // Apply Filters Button
        btnApply.setOnClickListener {
            // Collect checked months
            val selectedMonthRanges = monthCheckboxes
                .filter { it.first.isChecked }
                .map { it.second }
            
            val startDates = if (selectedMonthRanges.isNotEmpty()) {
                selectedMonthRanges.map { it.first }
            } else null
            
            val endDates = if (selectedMonthRanges.isNotEmpty()) {
                selectedMonthRanges.map { it.second }
            } else null
            
            val minDate = startDates?.minOrNull()
            val maxDate = endDates?.maxOrNull()
            
            // Collect checked categories
            val selectedCategoryIds = categoryCheckboxes
                .filter { it.first.isChecked }
                .map { it.second }
                .ifEmpty { null }
            
            // Collect checked payment modes
            val selectedPaymentModes = mutableListOf<PaymentMode>()
            if (cbCreditCard.isChecked) selectedPaymentModes.add(PaymentMode.CREDIT_CARD)
            if (cbDebitCard.isChecked) selectedPaymentModes.add(PaymentMode.DEBIT_CARD)
            if (cbUpi.isChecked) selectedPaymentModes.add(PaymentMode.UPI)
            if (cbCash.isChecked) selectedPaymentModes.add(PaymentMode.CASH)
            
            val paymentModes = selectedPaymentModes.ifEmpty { null }
            
            // Collect checked banks and UPI apps
            val selectedBankNames = mutableListOf<String>()
            val selectedUpiApps = mutableListOf<String>()
            
            bankUpiCheckboxes
                .filter { it.first.isChecked }
                .forEach {
                    if (it.second.type == BankUpiType.BANK) {
                        selectedBankNames.add(it.second.name)
                    } else {
                        selectedUpiApps.add(it.second.name)
                    }
                }
            
            val bankNames = selectedBankNames.ifEmpty { null }
            val upiApps = selectedUpiApps.ifEmpty { null }
            
            // Apply multi-filters
            if (startDates == null && selectedCategoryIds == null && paymentModes == null && 
                bankNames == null && upiApps == null) {
                // No filters selected - show all
                Toast.makeText(this, "No filters selected. Showing all expenses.", Toast.LENGTH_SHORT).show()
                clearFilters()
            } else {
                applyMultiFilters(
                    startDates, endDates, minDate, maxDate,
                    selectedCategoryIds, paymentModes, bankNames, upiApps
                )
            }
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun applyMultiFilters(
        startDates: List<Long>?,
        endDates: List<Long>?,
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ) {
        isFiltering = true
        
        // Apply multi-filters using the new ViewModel methods
        val filteredExpenses = viewModel.getMultiFilteredExpenses(
            startDates, endDates, minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
        
        filteredExpenses.observe(this) { expenses ->
            if (expenses.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                adapter.submitList(expenses)
            }
        }
        
        val filteredTotal = viewModel.getMultiFilteredTotal(
            startDates, endDates, minDate, maxDate,
            categoryIds, paymentModes, bankNames, upiApps
        )
        
        filteredTotal.observe(this) { total ->
            val totalText = if (total != null) {
                "Total: ₹${String.format("%.2f", total)}"
            } else {
                "Total: ₹0.00"
            }
            totalAmountView.text = totalText
        }
        
        // Build and show filter summary
        val summary = buildFilterSummary(startDates, categoryIds, paymentModes, bankNames, upiApps)
        Toast.makeText(this, "Filters applied: $summary", Toast.LENGTH_LONG).show()
    }
    
    private fun buildFilterSummary(
        startDates: List<Long>?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): String {
        val parts = mutableListOf<String>()
        
        startDates?.let { if (it.isNotEmpty()) parts.add("${it.size} month(s)") }
        categoryIds?.let { if (it.isNotEmpty()) parts.add("${it.size} category(ies)") }
        paymentModes?.let { if (it.isNotEmpty()) parts.add("${it.size} payment mode(s)") }
        
        val bankUpiCount = (bankNames?.size ?: 0) + (upiApps?.size ?: 0)
        if (bankUpiCount > 0) parts.add("$bankUpiCount bank(s)/UPI app(s)")
        
        return if (parts.isEmpty()) "No filters" else parts.joinToString(", ")
    }
    
    private fun getStartOfMonth(calendar: Calendar): Long {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getEndOfMonth(calendar: Calendar): Long {
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
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
        val rvBankUpi: RecyclerView = dialogView.findViewById(R.id.rvBankUpi)
        val btnClose: Button = dialogView.findViewById(R.id.btnCloseBankUpi)
        
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
        
        viewModel.allBankUpi.observe(this) { list ->
            bankUpiAdapter.submitList(list)
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
}
