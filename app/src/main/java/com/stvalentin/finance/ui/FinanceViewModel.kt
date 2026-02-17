package com.stvalentin.finance.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.stvalentin.finance.data.RegularPayment
import com.stvalentin.finance.data.RegularPaymentDao
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionDao
import com.stvalentin.finance.data.TransactionType
import com.stvalentin.finance.widget.FinanceWidget
import com.stvalentin.finance.workers.PaymentReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class FinanceViewModel(
    private val transactionDao: TransactionDao,
    private val regularPaymentDao: RegularPaymentDao,
    private val context: Context
) : ViewModel() {
    
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
    
    // Regular Payments
    private val _regularPayments = MutableStateFlow<List<RegularPayment>>(emptyList())
    val regularPayments: StateFlow<List<RegularPayment>> = _regularPayments.asStateFlow()
    
    init {
        viewModelScope.launch {
            regularPaymentDao.getAllActivePayments()
                .collect { payments ->
                    _regularPayments.value = payments
                }
        }
        // Запускаем Worker при создании ViewModel
        setupReminderWorker()
    }
    
    fun getTransactionById(id: Long): Flow<Transaction?> {
        return transactionDao.getTransactionById(id)
    }
    
    fun getRegularPaymentById(id: Long): Flow<RegularPayment?> {
        return regularPaymentDao.getAllActivePayments()
            .map { payments -> payments.find { it.id == id } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }
    
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
            updateWidget()
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
            updateWidget()
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            updateWidget()
        }
    }
    
    fun deleteAllTransactions() {
        viewModelScope.launch {
            transactionDao.deleteAll()
            updateWidget()
        }
    }
    
    fun addRegularPayment(
        name: String,
        category: String,
        amount: Double,
        dayOfMonth: Int,
        reminderDays: Int = 1,
        description: String = ""
    ) {
        viewModelScope.launch {
            val payment = RegularPayment(
                name = name,
                category = category,
                amount = amount,
                dayOfMonth = dayOfMonth,
                reminderDays = reminderDays,
                description = description,
                isActive = true
            )
            regularPaymentDao.insert(payment)
            setupReminderWorker()
        }
    }
    
    fun updateRegularPayment(payment: RegularPayment) {
        viewModelScope.launch {
            regularPaymentDao.update(payment)
        }
    }
    
    fun deleteRegularPayment(payment: RegularPayment) {
        viewModelScope.launch {
            regularPaymentDao.delete(payment)
        }
    }
    
    fun markPaymentAsPaid(payment: RegularPayment) {
        viewModelScope.launch {
            // Создаем транзакцию расхода
            val transaction = Transaction(
                type = TransactionType.EXPENSE,
                category = payment.category,
                amount = payment.amount,
                description = "Регулярный платеж: ${payment.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            // Обновляем дату последнего платежа
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            
            // Вычисляем следующую дату платежа (следующий месяц)
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, payment.dayOfMonth)
            val nextDue = calendar.timeInMillis
            
            // Создаем обновленный платеж с новой датой
            val updatedPayment = payment.copy(
                lastPaidDate = today,
                nextDueDate = nextDue
            )
            
            regularPaymentDao.update(updatedPayment)
            
            // Обновляем виджет
            updateWidget()
        }
    }
    
    private fun setupReminderWorker() {
        val workManager = WorkManager.getInstance(context)
        
        // Отменяем старые Worker'ы
        workManager.cancelUniqueWork("payment_reminders")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        // Запускаем каждые 15 минут для теста
        val reminderRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
         .setInitialDelay(1, TimeUnit.MINUTES)
         .build()
        
        workManager.enqueueUniquePeriodicWork(
            "payment_reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
        
        Log.d("FinanceViewModel", "Worker настроен на запуск каждые 15 минут")
    }
    
    private fun updateWidget() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FinanceWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (appWidgetIds.isNotEmpty()) {
                FinanceWidget().forceUpdate(context, appWidgetManager, appWidgetIds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Методы для статистики и советов
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