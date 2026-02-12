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
    
    // ПОЛНЫЙ МЕТОД с параметром date
    fun addTransaction(
        type: TransactionType,
        category: String,
        amount: Double,
        description: String = "",
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                category = category,
                amount = amount,
                description = description,
                date = date
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
    
    val balanceHistory = allTransactions.combine(allTransactions) { transactions, _ ->
        calculateDailyBalance(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val averageDailyExpense = allTransactions.combine(allTransactions) { transactions, _ ->
        calculateAverageDailyExpense(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val topExpenseCategory = allTransactions.combine(allTransactions) { transactions, _ ->
        findTopExpenseCategory(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val expenseComparison = allTransactions.combine(allTransactions) { transactions, _ ->
        compareWithPreviousMonth(transactions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val adviceMessage = combine(
        topExpenseCategory,
        expenseComparison
    ) { topCategory, comparison ->
        generateAdvice(topCategory, comparison)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Добавьте транзакции, чтобы получить рекомендации"
    )
    
    private fun calculateDailyBalance(transactions: List<Transaction>): List<Pair<Long, Double>> {
        if (transactions.isEmpty()) return emptyList()
        
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.timeInMillis
        
        val recentTransactions = transactions.filter { it.date >= startDate }
        val dailyBalances = mutableMapOf<Long, Double>()
        var runningBalance = 0.0
        
        recentTransactions.sortedBy { it.date }.forEach { transaction ->
            runningBalance += if (transaction.type == TransactionType.INCOME) {
                transaction.amount
            } else {
                -transaction.amount
            }
            val dayStart = getStartOfDay(transaction.date)
            dailyBalances[dayStart] = runningBalance
        }
        
        val result = mutableListOf<Pair<Long, Double>>()
        var currentDate = startDate
        var lastBalance = 0.0
        
        while (currentDate <= endDate) {
            val dayStart = getStartOfDay(currentDate)
            lastBalance = dailyBalances[dayStart] ?: lastBalance
            result.add(dayStart to lastBalance)
            currentDate += 24 * 60 * 60 * 1000
        }
        
        return result
    }
    
    private fun calculateAverageDailyExpense(transactions: List<Transaction>): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        
        val expensesThisMonth = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date >= startOfMonth && it.date <= now
        }
        
        val totalExpensesThisMonth = expensesThisMonth.sumOf { it.amount }
        
        val daysInMonth = calendar.get(Calendar.DAY_OF_MONTH)
        
        return if (daysInMonth > 0) {
            totalExpensesThisMonth / daysInMonth
        } else 0.0
    }
    
    private fun findTopExpenseCategory(transactions: List<Transaction>): Pair<String, Double>? {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        val now = System.currentTimeMillis()
        
        val expensesThisMonth = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date >= startOfMonth && it.date <= now
        }
        
        val categorySums = expensesThisMonth.groupBy { it.category }
            .mapValues { it.value.sumOf { it.amount } }
        
        return categorySums.maxByOrNull { it.value }?.let {
            it.key to it.value
        }
    }
    
    private fun compareWithPreviousMonth(transactions: List<Transaction>): Double {
        val calendar = Calendar.getInstance()
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfThisMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, -1)
        val startOfLastMonth = calendar.timeInMillis
        
        val endOfLastMonth = startOfThisMonth
        
        val now = System.currentTimeMillis()
        
        val expensesThisMonth = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date >= startOfThisMonth && it.date <= now
        }.sumOf { it.amount }
        
        val expensesLastMonth = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date >= startOfLastMonth && it.date < endOfLastMonth
        }.sumOf { it.amount }
        
        return if (expensesLastMonth > 0) {
            ((expensesThisMonth - expensesLastMonth) / expensesLastMonth * 100)
        } else 0.0
    }
    
    private fun generateAdvice(
        topCategory: Pair<String, Double>?,
        comparison: Double
    ): String {
        return when {
            topCategory == null -> "Добавьте транзакции, чтобы получить персональные рекомендации"
            
            comparison > 20 -> {
                "🔴 Вы тратите на ${topCategory.first} на ${"%.0f".format(comparison)}% больше, " +
                        "чем в прошлом месяце. Попробуйте установить лимит на эту категорию."
            }
            comparison > 5 -> {
                "🟡 Расходы на ${topCategory.first} выросли на ${"%.0f".format(comparison)}% " +
                        "по сравнению с прошлым месяцем. Следите за этой категорией."
            }
            comparison < -5 -> {
                "🟢 Отличная работа! Расходы на ${topCategory.first} снизились на ${"%.0f".format(-comparison)}% " +
                        "по сравнению с прошлым месяцем."
            }
            else -> {
                "💡 Самая затратная категория: ${topCategory.first} - ${"%.0f".format(topCategory.second)} ₽. " +
                        "Ваши расходы стабильны по сравнению с прошлым месяцем."
            }
        }
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