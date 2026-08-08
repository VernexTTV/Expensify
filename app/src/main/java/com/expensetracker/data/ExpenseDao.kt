package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {
    
    // Regular queries now return only recent expenses (last 90 days by default)
    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT 1000")
    fun getAllExpenses(): LiveData<List<Expense>>
    
    // Add method to get all expenses without limit (use carefully)
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesUnlimited(): LiveData<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE paymentMode = :mode ORDER BY date DESC")
    fun getExpensesByPaymentMode(mode: PaymentMode): LiveData<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: Long): Expense?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpense(expense: Expense): Long
    
    @Update
    fun updateExpense(expense: Expense)
    
    @Delete
    fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses")
    fun deleteAllExpenses()
    
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): LiveData<Double?>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?>
    
    // Single-select filtering methods
    @Query("""
        SELECT * FROM expenses 
        WHERE (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:paymentMode IS NULL OR paymentMode = :paymentMode)
        AND (:bankName IS NULL OR bankName = :bankName)
        AND (:upiApp IS NULL OR upiApp = :upiApp)
        ORDER BY date DESC
    """)
    fun getFilteredExpenses(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<List<Expense>>
    
    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        AND (:categoryId IS NULL OR categoryId = :categoryId)
        AND (:paymentMode IS NULL OR paymentMode = :paymentMode)
        AND (:bankName IS NULL OR bankName = :bankName)
        AND (:upiApp IS NULL OR upiApp = :upiApp)
    """)
    fun getFilteredTotal(
        startDate: Long?,
        endDate: Long?,
        categoryId: Long?,
        paymentMode: PaymentMode?,
        bankName: String?,
        upiApp: String?
    ): LiveData<Double?>
}
