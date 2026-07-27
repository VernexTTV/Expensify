package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: Long): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: Category): Long
    
    @Update
    fun updateCategory(category: Category)
    
    @Delete
    fun deleteCategory(category: Category)
    
    @Query("DELETE FROM categories")
    fun deleteAllCategories()
}
