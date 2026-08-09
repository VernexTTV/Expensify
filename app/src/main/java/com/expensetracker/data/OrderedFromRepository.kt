package com.expensetracker.data

import androidx.lifecycle.LiveData

class OrderedFromRepository(private val orderedFromDao: OrderedFromDao) {
    
    val allOrderedFrom: LiveData<List<OrderedFrom>> = orderedFromDao.getAllOrderedFrom()
    
    fun insert(orderedFrom: OrderedFrom): Long {
        return orderedFromDao.insertOrderedFrom(orderedFrom)
    }
    
    fun update(orderedFrom: OrderedFrom) {
        orderedFromDao.updateOrderedFrom(orderedFrom)
    }
    
    fun delete(orderedFrom: OrderedFrom) {
        orderedFromDao.deleteOrderedFrom(orderedFrom)
    }
    
    fun getOrderedFromById(id: Long): OrderedFrom? {
        return orderedFromDao.getOrderedFromById(id)
    }
}
