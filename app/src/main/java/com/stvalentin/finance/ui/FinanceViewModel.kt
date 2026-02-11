package com.stvalentin.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionDao
import com.stvalentin.finance.data.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(private val transactionDao: TransactionDao) : ViewModel() {
    
    val allTransactions = transactionDao.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val totalIncome = transactionDao.getTotalIncome()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    val totalExpenses = transactionDao.getTotalExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    val balance = transactionDao.getBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    
    fun addTransaction(
        type: TransactionType,
        category: String,
        amount: Double,
        description: String = ""
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                category = category,
                amount = amount,
                description = description
            )
            transactionDao.insert(transaction)
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
        }
    }
    
    fun deleteAllTransactions() {
        viewModelScope.launch {
            transactionDao.deleteAll()
        }
    }
    
    fun getIncomeStats() = transactionDao.getCategoryStats(TransactionType.INCOME)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun getExpenseStats() = transactionDao.getCategoryStats(TransactionType.EXPENSE)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}