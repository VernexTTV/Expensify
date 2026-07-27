package com.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BankUpiDao {
    
    @Query("SELECT * FROM bank_upi ORDER BY name ASC")
    fun getAllBankUpi(): LiveData<List<BankUpi>>
    
    @Query("SELECT * FROM bank_upi WHERE type = :type ORDER BY name ASC")
    fun getBankUpiByType(type: BankUpiType): LiveData<List<BankUpi>>
    
    @Query("SELECT * FROM bank_upi WHERE id = :bankUpiId")
    fun getBankUpiById(bankUpiId: Long): BankUpi?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBankUpi(bankUpi: BankUpi): Long
    
    @Update
    fun updateBankUpi(bankUpi: BankUpi)
    
    @Delete
    fun deleteBankUpi(bankUpi: BankUpi)
    
    @Query("DELETE FROM bank_upi")
    fun deleteAllBankUpi()
}
