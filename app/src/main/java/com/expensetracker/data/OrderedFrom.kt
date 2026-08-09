package com.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ordered_from")
data class OrderedFrom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String // Restaurant, Shop, Store name, etc.
)
