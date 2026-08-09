package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseSourceDao {
    
    @Query("SELECT * FROM expense_sources ORDER BY name ASC")
    fun getAllExpenseSources(): LiveData<List<ExpenseSource>>
    
    @Query("SELECT * FROM expense_sources WHERE id = :id")
    fun getExpenseSourceById(id: Long): ExpenseSource?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExpenseSource(expenseSource: ExpenseSource): Long
    
    @Update
    fun updateExpenseSource(expenseSource: ExpenseSource)
    
    @Delete
    fun deleteExpenseSource(expenseSource: ExpenseSource)
    
    @Query("DELETE FROM expense_sources")
    fun deleteAllExpenseSources()
}
