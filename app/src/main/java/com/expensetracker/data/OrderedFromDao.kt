package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface OrderedFromDao {
    
    @Query("SELECT * FROM ordered_from ORDER BY name ASC")
    fun getAllOrderedFrom(): LiveData<List<OrderedFrom>>
    
    @Query("SELECT * FROM ordered_from WHERE id = :id")
    fun getOrderedFromById(id: Long): OrderedFrom?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrderedFrom(orderedFrom: OrderedFrom): Long
    
    @Update
    fun updateOrderedFrom(orderedFrom: OrderedFrom)
    
    @Delete
    fun deleteOrderedFrom(orderedFrom: OrderedFrom)
    
    @Query("DELETE FROM ordered_from")
    fun deleteAllOrderedFrom()
}
