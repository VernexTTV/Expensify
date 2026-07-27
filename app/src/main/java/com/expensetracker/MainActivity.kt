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
import com.expensetracker.data.Expense
import com.expensetracker.data.PaymentMode
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        emptyView = findViewById(R.id.tvEmpty)
        totalAmountView = findViewById(R.id.tvTotalAmount)
        fabAdd = findViewById(R.id.fabAdd)
        
        // Setup RecyclerView
        adapter = ExpenseAdapter(
            onEditClick = { expense -> showAddEditDialog(expense) },
            onDeleteClick = { expense -> showDeleteConfirmation(expense) }
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
        
        // Setup FAB
        fabAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }
    
    private fun showAddEditDialog(expense: Expense?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(if (expense == null) "Add Expense" else "Edit Expense")
            .create()
        
        val dateButton: Button = dialogView.findViewById(R.id.btnSelectDate)
        val amountEdit: EditText = dialogView.findViewById(R.id.etAmount)
        val paymentModeSpinner: Spinner = dialogView.findViewById(R.id.spinnerPaymentMode)
        val bankUpiLayout: LinearLayout = dialogView.findViewById(R.id.layoutBankUpi)
        val bankUpiEdit: EditText = dialogView.findViewById(R.id.etBankUpi)
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
        
        // Setup payment mode spinner
        val paymentModes = arrayOf("Credit Card", "Debit Card", "UPI", "Cash")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentModes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentModeSpinner.adapter = spinnerAdapter
        
        paymentModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0, 1 -> { // Credit Card or Debit Card
                        bankUpiLayout.visibility = View.VISIBLE
                        bankUpiLabel.text = "Bank Name"
                        bankUpiEdit.hint = "Enter bank name"
                    }
                    2 -> { // UPI
                        bankUpiLayout.visibility = View.VISIBLE
                        bankUpiLabel.text = "UPI App"
                        bankUpiEdit.hint = "Enter UPI app name"
                    }
                    3 -> { // Cash
                        bankUpiLayout.visibility = View.GONE
                    }
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Pre-fill if editing
        if (expense != null) {
            amountEdit.setText(expense.amount.toString())
            paymentModeSpinner.setSelection(expense.paymentMode.ordinal)
            when (expense.paymentMode) {
                PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD -> 
                    bankUpiEdit.setText(expense.bankName ?: "")
                PaymentMode.UPI -> 
                    bankUpiEdit.setText(expense.upiApp ?: "")
                PaymentMode.CASH -> {}
            }
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
            
            val paymentMode = PaymentMode.values()[paymentModeSpinner.selectedItemPosition]
            val bankUpiValue = bankUpiEdit.text.toString().trim()
            
            // Validate bank/UPI field if required
            if (paymentMode != PaymentMode.CASH && bankUpiValue.isEmpty()) {
                val fieldName = if (paymentMode == PaymentMode.UPI) "UPI app" else "Bank name"
                Toast.makeText(this, "Please enter $fieldName", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val newExpense = Expense(
                id = expense?.id ?: 0,
                date = selectedDate,
                amount = amount,
                paymentMode = paymentMode,
                bankName = if (paymentMode in arrayOf(PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD)) 
                    bankUpiValue else null,
                upiApp = if (paymentMode == PaymentMode.UPI) bankUpiValue else null,
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
}
