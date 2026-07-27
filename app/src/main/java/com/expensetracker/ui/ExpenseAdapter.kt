package com.expensetracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.expensetracker.R
import com.expensetracker.data.Expense
import com.expensetracker.data.PaymentMode
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onEditClick: (Expense) -> Unit,
    private val onDeleteClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = getItem(position)
        holder.bind(expense, onEditClick, onDeleteClick)
    }
    
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.tvDate)
        private val amountText: TextView = itemView.findViewById(R.id.tvAmount)
        private val paymentModeText: TextView = itemView.findViewById(R.id.tvPaymentMode)
        private val detailsText: TextView = itemView.findViewById(R.id.tvDetails)
        private val descriptionText: TextView = itemView.findViewById(R.id.tvDescription)
        private val editButton: View = itemView.findViewById(R.id.btnEdit)
        private val deleteButton: View = itemView.findViewById(R.id.btnDelete)
        
        fun bind(expense: Expense, onEditClick: (Expense) -> Unit, onDeleteClick: (Expense) -> Unit) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            dateText.text = dateFormat.format(Date(expense.date))
            amountText.text = "₹${String.format("%.2f", expense.amount)}"
            
            val paymentModeDisplay = when(expense.paymentMode) {
                PaymentMode.CREDIT_CARD -> "Credit Card"
                PaymentMode.DEBIT_CARD -> "Debit Card"
                PaymentMode.UPI -> "UPI"
                PaymentMode.CASH -> "Cash"
            }
            paymentModeText.text = paymentModeDisplay
            
            // Show bank or UPI details
            val details = when(expense.paymentMode) {
                PaymentMode.CREDIT_CARD, PaymentMode.DEBIT_CARD -> expense.bankName ?: ""
                PaymentMode.UPI -> expense.upiApp ?: ""
                PaymentMode.CASH -> ""
            }
            
            if (details.isNotEmpty()) {
                detailsText.visibility = View.VISIBLE
                detailsText.text = details
            } else {
                detailsText.visibility = View.GONE
            }
            
            // Show description if available
            if (!expense.description.isNullOrEmpty()) {
                descriptionText.visibility = View.VISIBLE
                descriptionText.text = expense.description
            } else {
                descriptionText.visibility = View.GONE
            }
            
            editButton.setOnClickListener { onEditClick(expense) }
            deleteButton.setOnClickListener { onDeleteClick(expense) }
        }
    }
    
    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}
