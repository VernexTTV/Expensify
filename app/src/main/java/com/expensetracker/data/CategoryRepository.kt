package com.expensetracker.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun insert(category: Category): Long {
        return withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }
    
    suspend fun update(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.updateCategory(category)
        }
    }
    
    suspend fun delete(category: Category) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategory(category)
        }
    }
    
    suspend fun getCategoryById(categoryId: Long): Category? {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryById(categoryId)
        }
    }
}
