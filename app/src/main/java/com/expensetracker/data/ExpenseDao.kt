package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {
    
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>
    
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
    
    // Multi-select filtering method - returns expenses matching ANY of the selected criteria in each category
    @Query("""
        SELECT * FROM expenses 
        WHERE (:startDates IS NULL OR date >= :minDate)
        AND (:endDates IS NULL OR date <= :maxDate)
        AND (:categoryIds IS NULL OR categoryId IN (:categoryIds))
        AND (:paymentModes IS NULL OR paymentMode IN (:paymentModes))
        AND (:bankNames IS NULL OR bankName IN (:bankNames))
        AND (:upiApps IS NULL OR upiApp IN (:upiApps))
        ORDER BY date DESC
    """)
    fun getMultiFilteredExpenses(
        startDates: List<Long>?,
        endDates: List<Long>?,
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<List<Expense>>
    
    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE (:startDates IS NULL OR date >= :minDate)
        AND (:endDates IS NULL OR date <= :maxDate)
        AND (:categoryIds IS NULL OR categoryId IN (:categoryIds))
        AND (:paymentModes IS NULL OR paymentMode IN (:paymentModes))
        AND (:bankNames IS NULL OR bankName IN (:bankNames))
        AND (:upiApps IS NULL OR upiApp IN (:upiApps))
    """)
    fun getMultiFilteredTotal(
        startDates: List<Long>?,
        endDates: List<Long>?,
        minDate: Long?,
        maxDate: Long?,
        categoryIds: List<Long>?,
        paymentModes: List<PaymentMode>?,
        bankNames: List<String>?,
        upiApps: List<String>?
    ): LiveData<Double?>
    
    // Legacy single-filter methods (keeping for backward compatibility)
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
