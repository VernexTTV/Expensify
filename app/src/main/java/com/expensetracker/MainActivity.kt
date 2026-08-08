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
            
            android.util.Log.d("ExpenseTracker", "Step 4: Initialize views")
            // Initialize views
            recyclerView = findViewById(R.id.recyclerView)
            emptyView = findViewById(R.id.tvEmpty)
            totalAmountView = findViewById(R.id.tvTotalAmount)
            fabAdd = findViewById(R.id.fabAdd)
            
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
            
            android.util.Log.d("ExpenseTracker", "Step 7: Setup ViewModel")
            // Setup ViewModel
            viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
            
            android.util.Log.d("ExpenseTracker", "Step 8: Observe expenses")
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
            R.id.action_test_crash -> {
                // Test crash for Sentry
                Toast.makeText(this, "Triggering test crash in 2 seconds...", Toast.LENGTH_SHORT).show()
                android.os.Handler(mainLooper).postDelayed({
                    throw RuntimeException("Test Crash for Sentry - Intentional!")
                }, 2000)
                true
            }
            R.id.action_test_message -> {
                // Test message for Sentry
                Sentry.captureMessage("Test message from MainActivity - User clicked test button")
                Toast.makeText(this, "Test message sent to Sentry! Check your dashboard.", Toast.LENGTH_LONG).show()
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
        // SIMPLIFIED - Using single-select spinners instead of multi-select checkboxes
        Toast.makeText(this, "Simple filter dialog - to be implemented", Toast.LENGTH_SHORT).show()
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
