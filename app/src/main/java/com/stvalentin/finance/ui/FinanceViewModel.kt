package com.stvalentin.finance.ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.stvalentin.finance.data.*
import com.stvalentin.finance.widget.FinanceWidget
import com.stvalentin.finance.workers.PaymentReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class FinanceViewModel(
    private val transactionDao: TransactionDao,
    private val regularPaymentDao: RegularPaymentDao,
    private val savingDao: SavingDao,  // ← Добавлен SavingDao
    private val context: Context
) : ViewModel() {
    
    // Транзакции
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
    
    // ========== НАКОПЛЕНИЯ (SAVINGS) ==========
    private val _allSavings = MutableStateFlow<List<Saving>>(emptyList())
    val allSavings: StateFlow<List<Saving>> = _allSavings.asStateFlow()
    
    private val _totalSavings = MutableStateFlow(0.0)
    val totalSavings: StateFlow<Double> = _totalSavings.asStateFlow()
    
    private val _savingsByCurrency = MutableStateFlow<Map<String, Double>>(emptyMap())
    val savingsByCurrency: StateFlow<Map<String, Double>> = _savingsByCurrency.asStateFlow()
    
    // Баланс с учетом накоплений (свободные средства)
    val availableBalance = combine(
        balance,
        totalSavings
    ) { totalBalance, savings ->
        totalBalance - savings
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    init {
        viewModelScope.launch {
            regularPaymentDao.getAllActivePayments()
                .collect { payments ->
                    _regularPayments.value = payments
                }
        }
        
        // Загружаем накопления
        viewModelScope.launch {
            savingDao.getAllSavings()
                .collect { savings ->
                    _allSavings.value = savings
                    
                    // Общая сумма всех накоплений
                    val total = savings.sumOf { it.amount }
                    _totalSavings.value = total
                    
                    // Сумма по валютам
                    val byCurrency = savings
                        .groupBy { it.currency }
                        .mapValues { it.value.sumOf { it.amount } }
                    _savingsByCurrency.value = byCurrency
                }
        }
        
        // Запускаем Worker при создании ViewModel
        setupReminderWorker()
    }
    
    // Транзакции
    fun getTransactionById(id: Long): Flow<Transaction?> {
        return transactionDao.getTransactionById(id)
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
    
    // Regular Payments
    fun getRegularPaymentById(id: Long): Flow<RegularPayment?> {
        return regularPaymentDao.getAllActivePayments()
            .map { payments -> payments.find { it.id == id } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
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
    
    // ========== МЕТОДЫ ДЛЯ НАКОПЛЕНИЙ ==========
    
    fun getSavingById(id: Long): Flow<Saving?> {
        return allSavings.map { savings -> savings.find { it.id == id } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )
    }
    
    fun addSaving(
        name: String,
        category: String,
        amount: Double,
        currency: String = "RUB",
        note: String = "",
        targetAmount: Double? = null
    ) {
        viewModelScope.launch {
            val saving = Saving(
                name = name,
                category = category,
                amount = amount,
                currency = currency,
                note = note,
                targetAmount = targetAmount,
                dateCreated = System.currentTimeMillis(),
                dateUpdated = System.currentTimeMillis(),
                isActive = true
            )
            savingDao.insert(saving)
            
            // Создаем транзакцию накопления (чтобы отслеживать движение денег)
            val transaction = Transaction(
                type = TransactionType.SAVING,
                category = category,
                amount = amount,
                description = "Накопление: $name",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            updateWidget()
        }
    }
    
    fun updateSaving(saving: Saving) {
        viewModelScope.launch {
            val updatedSaving = saving.copy(
                dateUpdated = System.currentTimeMillis()
            )
            savingDao.update(updatedSaving)
        }
    }
    
    fun deleteSaving(saving: Saving) {
        viewModelScope.launch {
            savingDao.delete(saving)
            
            // Можно создать обратную транзакцию (возврат денег)
            val transaction = Transaction(
                type = TransactionType.INCOME,
                category = saving.category,
                amount = saving.amount,
                description = "Возврат из накоплений: ${saving.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            updateWidget()
        }
    }
    
    fun archiveSaving(id: Long) {
        viewModelScope.launch {
            savingDao.archiveSaving(id)
        }
    }
    
    fun addMoneyToSaving(savingId: Long, amount: Double) {
        viewModelScope.launch {
            val saving = savingDao.getSavingById(savingId)
            if (saving != null) {
                val updatedSaving = saving.copy(
                    amount = saving.amount + amount,
                    dateUpdated = System.currentTimeMillis()
                )
                savingDao.update(updatedSaving)
                
                // Создаем транзакцию накопления
                val transaction = Transaction(
                    type = TransactionType.SAVING,
                    category = saving.category,
                    amount = amount,
                    description = "Пополнение: ${saving.name}",
                    date = System.currentTimeMillis()
                )
                transactionDao.insert(transaction)
                
                updateWidget()
            }
        }
    }
    
    fun withdrawFromSaving(savingId: Long, amount: Double) {
        viewModelScope.launch {
            val saving = savingDao.getSavingById(savingId)
            if (saving != null && saving.amount >= amount) {
                val updatedSaving = saving.copy(
                    amount = saving.amount - amount,
                    dateUpdated = System.currentTimeMillis()
                )
                savingDao.update(updatedSaving)
                
                // Создаем транзакцию дохода (возврат денег)
                val transaction = Transaction(
                    type = TransactionType.INCOME,
                    category = saving.category,
                    amount = amount,
                    description = "Снятие из накоплений: ${saving.name}",
                    date = System.currentTimeMillis()
                )
                transactionDao.insert(transaction)
                
                updateWidget()
            }
        }
    }
    
    fun transferToSaving(
        fromSavingId: Long?,
        toSavingId: Long,
        amount: Double,
        description: String = "Перевод между накоплениями"
    ) {
        viewModelScope.launch {
            val toSaving = savingDao.getSavingById(toSavingId)
            
            if (fromSavingId == null) {
                // Перевод из свободных средств
                if (toSaving != null) {
                    val updatedToSaving = toSaving.copy(
                        amount = toSaving.amount + amount,
                        dateUpdated = System.currentTimeMillis()
                    )
                    savingDao.update(updatedToSaving)
                    
                    val transaction = Transaction(
                        type = TransactionType.SAVING,
                        category = toSaving.category,
                        amount = amount,
                        description = description,
                        date = System.currentTimeMillis()
                    )
                    transactionDao.insert(transaction)
                }
            } else {
                // Перевод между счетами
                val fromSaving = savingDao.getSavingById(fromSavingId)
                if (fromSaving != null && toSaving != null && fromSaving.amount >= amount) {
                    val updatedFromSaving = fromSaving.copy(
                        amount = fromSaving.amount - amount,
                        dateUpdated = System.currentTimeMillis()
                    )
                    val updatedToSaving = toSaving.copy(
                        amount = toSaving.amount + amount,
                        dateUpdated = System.currentTimeMillis()
                    )
                    
                    savingDao.update(updatedFromSaving)
                    savingDao.update(updatedToSaving)
                    
                    // Можно создать транзакцию, но это не обязательно
                }
            }
            updateWidget()
        }
    }
    
    // ========== МЕТОДЫ ДЛЯ СТАТИСТИКИ ==========
    
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
    
    fun getSavingStats() = allSavings
        .map { savings ->
            savings.groupBy { it.category }
                .mapValues { it.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // ========== МЕТОДЫ ДЛЯ РАСЧЕТОВ И СОВЕТОВ ==========
    
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
        expenseComparison,
        totalSavings,
        availableBalance
    ) { topCategory, comparison, savings, available ->
        generateAdvice(topCategory, comparison, savings, available)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Добавьте транзакции, чтобы получить рекомендации"
    )
    
    private fun generateAdvice(
        topCategory: Pair<String, Double>?,
        comparison: Double,
        totalSavings: Double,
        availableBalance: Double
    ): String {
        return when {
            topCategory == null && totalSavings == 0.0 -> 
                "Добавьте транзакции и накопления для персональных рекомендаций"
            
            totalSavings == 0.0 -> {
                "💰 Начните копить! У вас свободно ${"%.0f".format(availableBalance)} ₽. " +
                "Отложите хотя бы 10% от этой суммы."
            }
            
            totalSavings < 50000 -> {
                "🏦 У вас ${"%.0f".format(totalSavings)} ₽ в накоплениях. " +
                "До подушки безопасности (50 000 ₽) осталось ${"%.0f".format(50000 - totalSavings)} ₽"
            }
            
            totalSavings < 100000 -> {
                "💰 Хорошая подушка! У вас ${"%.0f".format(totalSavings)} ₽. " +
                "Можно рассмотреть открытие вклада."
            }
            
            else -> {
                "📈 Отличные накопления! ${"%.0f".format(totalSavings)} ₽. " +
                "Пора изучать инвестиционные инструменты."
            }
        }
    }
    
    // Остальные методы без изменений
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
            runningBalance += when (transaction.type) {
                TransactionType.INCOME -> transaction.amount
                TransactionType.EXPENSE -> -transaction.amount
                TransactionType.SAVING -> -transaction.amount // Накопления уменьшают свободный баланс
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