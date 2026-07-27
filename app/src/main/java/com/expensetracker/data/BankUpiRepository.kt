package com.expensetracker.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BankUpiRepository(private val bankUpiDao: BankUpiDao) {
    
    val allBankUpi: LiveData<List<BankUpi>> = bankUpiDao.getAllBankUpi()
    
    fun getBankUpiByType(type: BankUpiType): LiveData<List<BankUpi>> {
        return bankUpiDao.getBankUpiByType(type)
    }
    
    suspend fun insert(bankUpi: BankUpi): Long {
        return withContext(Dispatchers.IO) {
            bankUpiDao.insertBankUpi(bankUpi)
        }
    }
    
    suspend fun update(bankUpi: BankUpi) {
        withContext(Dispatchers.IO) {
            bankUpiDao.updateBankUpi(bankUpi)
        }
    }
    
    suspend fun delete(bankUpi: BankUpi) {
        withContext(Dispatchers.IO) {
            bankUpiDao.deleteBankUpi(bankUpi)
        }
    }
    
    suspend fun getBankUpiById(bankUpiId: Long): BankUpi? {
        return withContext(Dispatchers.IO) {
            bankUpiDao.getBankUpiById(bankUpiId)
        }
    }
}
