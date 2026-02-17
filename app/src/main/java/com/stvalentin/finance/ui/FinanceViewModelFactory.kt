package com.stvalentin.finance.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stvalentin.finance.data.RegularPaymentDao
import com.stvalentin.finance.data.SavingDao
import com.stvalentin.finance.data.TransactionDao

class FinanceViewModelFactory(
    private val transactionDao: TransactionDao,
    private val regularPaymentDao: RegularPaymentDao,
    private val savingDao: SavingDao,  // ← ДОБАВЛЕНО
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(
                transactionDao, 
                regularPaymentDao, 
                savingDao,  // ← ПЕРЕДАЕМ
                context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}