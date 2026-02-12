package com.stvalentin.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionDao
import com.stvalentin.finance.data.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

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
    
    fun getTransactionById(id: Long): Flow<Transaction?> {
        return transactionDao.getTransactionById(id)
    }
    
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
    
    // ИСПРАВЛЕНО: Получение баланса по дням для графика
    val balanceHistory = allTransactions.combine(allTransactions) { transactions, _ ->
        calculateDailyBalance(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private fun calculateDailyBalance(transactions: List<Transaction>): List<Pair<Long, Double>> {
        if (transactions.isEmpty()) return emptyList()
        
        // Берем последние 30 дней
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.timeInMillis
        
        // Фильтруем транзакции за последние 30 дней
        val recentTransactions = transactions.filter { it.date >= startDate }
        
        // Группируем по дням и считаем баланс на конец дня
        val dailyBalances = mutableMapOf<Long, Double>()
        var runningBalance = 0.0
        
        // Сортируем по дате
        recentTransactions.sortedBy { it.date }.forEach { transaction ->
            runningBalance += if (transaction.type == TransactionType.INCOME) {
                transaction.amount
            } else {
                -transaction.amount
            }
            
            // Округляем дату до начала дня
            val dayStart = getStartOfDay(transaction.date)
            dailyBalances[dayStart] = runningBalance
        }
        
        // Заполняем пропущенные дни предыдущим значением
        val result = mutableListOf<Pair<Long, Double>>()
        var currentDate = startDate
        var lastBalance = 0.0
        
        while (currentDate <= endDate) {
            val dayStart = getStartOfDay(currentDate)
            lastBalance = dailyBalances[dayStart] ?: lastBalance
            result.add(dayStart to lastBalance)
            currentDate += 24 * 60 * 60 * 1000 // +1 день
        }
        
        return result
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}