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

// Перечисление для периодов
enum class StatsPeriod {
    WEEK, MONTH, YEAR, ALL_TIME
}

// Режимы отображения статистики
enum class StatsMode {
    SINGLE, COMPARE
}

class FinanceViewModel(
    private val transactionDao: TransactionDao,
    private val regularPaymentDao: RegularPaymentDao,
    private val savingDao: SavingDao,
    private val userProfileDao: UserProfileDao,
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
    
    // ========== ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ ==========
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    // ========== ДАННЫЕ ДЛЯ АНАЛИЗА ДОХОДОВ ==========
    private val _averageMonthlyIncome = MutableStateFlow(0.0)
    val averageMonthlyIncome: StateFlow<Double> = _averageMonthlyIncome.asStateFlow()
    
    private val _mainIncomeSource = MutableStateFlow("Зарплата")
    val mainIncomeSource: StateFlow<String> = _mainIncomeSource.asStateFlow()
    
    private val _incomeDays = MutableStateFlow<List<Int>>(emptyList())
    val incomeDays: StateFlow<List<Int>> = _incomeDays.asStateFlow()
    
    private val _typicalIncomeDay = MutableStateFlow(5)
    val typicalIncomeDay: StateFlow<Int> = _typicalIncomeDay.asStateFlow()
    
    private val _incomeStability = MutableStateFlow(1.0)
    val incomeStability: StateFlow<Double> = _incomeStability.asStateFlow()
    
    private val _nextIncomeDate = MutableStateFlow<Long?>(null)
    val nextIncomeDate: StateFlow<Long?> = _nextIncomeDate.asStateFlow()
    
    private val _daysToNextIncome = MutableStateFlow(0)
    val daysToNextIncome: StateFlow<Int> = _daysToNextIncome.asStateFlow()
    
    // ========== ДАННЫЕ ДЛЯ СТАТИСТИКИ ==========
    private val _statsMode = MutableStateFlow(StatsMode.SINGLE)
    val statsMode: StateFlow<StatsMode> = _statsMode.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val selectedPeriod: StateFlow<StatsPeriod> = _selectedPeriod.asStateFlow()
    
    private val _singleStart = MutableStateFlow(getStartOfMonth())
    private val _singleEnd = MutableStateFlow(System.currentTimeMillis())
    
    private val _periodAStart = MutableStateFlow(getStartOfPreviousMonth())
    private val _periodAEnd = MutableStateFlow(getEndOfPreviousMonth())
    private val _periodBStart = MutableStateFlow(getStartOfMonth())
    private val _periodBEnd = MutableStateFlow(System.currentTimeMillis())
    
    val singleStart: StateFlow<Long> = _singleStart.asStateFlow()
    val singleEnd: StateFlow<Long> = _singleEnd.asStateFlow()
    val periodAStart: StateFlow<Long> = _periodAStart.asStateFlow()
    val periodAEnd: StateFlow<Long> = _periodAEnd.asStateFlow()
    val periodBStart: StateFlow<Long> = _periodBStart.asStateFlow()
    val periodBEnd: StateFlow<Long> = _periodBEnd.asStateFlow()
    
    // Данные для обычного режима
    private val _periodIncome = MutableStateFlow(0.0)
    val periodIncome: StateFlow<Double> = _periodIncome.asStateFlow()
    
    private val _periodExpenses = MutableStateFlow(0.0)
    val periodExpenses: StateFlow<Double> = _periodExpenses.asStateFlow()
    
    private val _periodBalance = MutableStateFlow(0.0)
    val periodBalance: StateFlow<Double> = _periodBalance.asStateFlow()
    
    private val _periodExpenseStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodExpenseStats: StateFlow<List<CategoryStat>> = _periodExpenseStats.asStateFlow()
    
    private val _periodIncomeStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodIncomeStats: StateFlow<List<CategoryStat>> = _periodIncomeStats.asStateFlow()
    
    private val _averageDailyExpensePeriod = MutableStateFlow(0.0)
    val averageDailyExpensePeriod: StateFlow<Double> = _averageDailyExpensePeriod.asStateFlow()
    
    private val _topExpenseCategoryPeriod = MutableStateFlow<Pair<String, Double>?>(null)
    val topExpenseCategoryPeriod: StateFlow<Pair<String, Double>?> = _topExpenseCategoryPeriod.asStateFlow()
    
    // Данные для режима сравнения
    private val _periodAIncome = MutableStateFlow(0.0)
    val periodAIncome: StateFlow<Double> = _periodAIncome.asStateFlow()
    
    private val _periodAExpenses = MutableStateFlow(0.0)
    val periodAExpenses: StateFlow<Double> = _periodAExpenses.asStateFlow()
    
    private val _periodABalance = MutableStateFlow(0.0)
    val periodABalance: StateFlow<Double> = _periodABalance.asStateFlow()
    
    private val _periodAExpenseStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodAExpenseStats: StateFlow<List<CategoryStat>> = _periodAExpenseStats.asStateFlow()
    
    private val _periodAIncomeStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodAIncomeStats: StateFlow<List<CategoryStat>> = _periodAIncomeStats.asStateFlow()
    
    private val _periodBIncome = MutableStateFlow(0.0)
    val periodBIncome: StateFlow<Double> = _periodBIncome.asStateFlow()
    
    private val _periodBExpenses = MutableStateFlow(0.0)
    val periodBExpenses: StateFlow<Double> = _periodBExpenses.asStateFlow()
    
    private val _periodBBalance = MutableStateFlow(0.0)
    val periodBBalance: StateFlow<Double> = _periodBBalance.asStateFlow()
    
    private val _periodBExpenseStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodBExpenseStats: StateFlow<List<CategoryStat>> = _periodBExpenseStats.asStateFlow()
    
    private val _periodBIncomeStats = MutableStateFlow<List<CategoryStat>>(emptyList())
    val periodBIncomeStats: StateFlow<List<CategoryStat>> = _periodBIncomeStats.asStateFlow()
    
    // График
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
    
    // Совет дня (обновленный)
    val adviceMessage = combine(
        topExpenseCategory,
        expenseComparison,
        totalSavings,
        availableBalance,
        userProfile
    ) { topCategory, comparison, savings, available, profile ->
        generateAdvice(topCategory, comparison, savings, available, profile)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Добавьте транзакции и заполните профиль для персональных рекомендаций"
    )
    
    init {
        viewModelScope.launch {
            regularPaymentDao.getAllActivePayments()
                .collect { payments ->
                    _regularPayments.value = payments
                }
        }
        
        viewModelScope.launch {
            savingDao.getAllSavings()
                .collect { savings ->
                    _allSavings.value = savings
                    val total = savings.sumOf { it.amount }
                    _totalSavings.value = total
                    
                    val byCurrency = savings
                        .groupBy { it.currency }
                        .mapValues { it.value.sumOf { it.amount } }
                    _savingsByCurrency.value = byCurrency
                }
        }
        
        // Загружаем профиль
        viewModelScope.launch {
            userProfileDao.getUserProfile()
                .collect { profile ->
                    _userProfile.value = profile
                }
        }
        
        // Создаем профиль по умолчанию, если его нет
        viewModelScope.launch {
            val existing = userProfileDao.getUserProfileSync()
            if (existing == null) {
                val defaultProfile = UserProfile()
                userProfileDao.insert(defaultProfile)
                _userProfile.value = defaultProfile
            }
        }
        
        // Анализируем доходы
        viewModelScope.launch {
            analyzeIncome()
        }
        
        viewModelScope.launch {
            loadStats()
        }
        
        setupReminderWorker()
    }
    
    // ========== МЕТОДЫ ДЛЯ АНАЛИЗА ДОХОДОВ ==========
    
    private suspend fun analyzeIncome() {
        // Получаем все доходы за последние 6 месяцев
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -6)
        val startDate = calendar.timeInMillis
        
        val incomes = transactionDao.getTransactionsBetweenDates(startDate, endDate)
            .filter { it.type == TransactionType.INCOME }
        
        if (incomes.isEmpty()) return
        
        // Средний доход за месяц
        val totalIncome6Months = incomes.sumOf { it.amount }
        _averageMonthlyIncome.value = totalIncome6Months / 6
        
        // Основной источник дохода
        val incomeByCategory = incomes.groupBy { it.category }
            .mapValues { it.value.sumOf { it.amount } }
        _mainIncomeSource.value = incomeByCategory.maxByOrNull { it.value }?.key ?: "Зарплата"
        
        // Анализ дней дохода
        val incomeDaysList = incomes.map { 
            Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.DAY_OF_MONTH)
        }
        _incomeDays.value = incomeDaysList
        
        // Типичный день дохода (наиболее частый)
        val dayFrequency = incomeDaysList.groupingBy { it }.eachCount()
        _typicalIncomeDay.value = dayFrequency.maxByOrNull { it.value }?.key ?: 5
        
        // Стабильность дохода (стандартное отклонение)
        if (incomes.size > 1) {
            val amounts = incomes.map { it.amount }
            val mean = amounts.average()
            val variance = amounts.map { (it - mean) * (it - mean) }.average()
            val stdDev = kotlin.math.sqrt(variance)
            _incomeStability.value = 1.0 - (stdDev / mean).coerceIn(0.0, 1.0)
        }
        
        // Следующая дата дохода
        calculateNextIncomeDate()
        
        // Обновляем профиль с аналитикой
        _userProfile.value?.let { profile ->
            val updatedProfile = profile.copy(
                averageMonthlyIncome = _averageMonthlyIncome.value,
                mainIncomeSource = _mainIncomeSource.value,
                mainIncomeDay = _typicalIncomeDay.value,
                incomeStability = _incomeStability.value
            )
            userProfileDao.update(updatedProfile)
        }
    }
    
    private suspend fun calculateNextIncomeDate() {
        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)
        
        val typicalDay = _typicalIncomeDay.value
        
        val nextIncome = Calendar.getInstance()
        
        if (typicalDay > currentDay) {
            // В этом месяце
            nextIncome.set(currentYear, currentMonth, typicalDay, 12, 0, 0)
        } else {
            // В следующем месяце
            nextIncome.add(Calendar.MONTH, 1)
            nextIncome.set(nextIncome.get(Calendar.YEAR), nextIncome.get(Calendar.MONTH), typicalDay, 12, 0, 0)
        }
        
        _nextIncomeDate.value = nextIncome.timeInMillis
        
        val daysDiff = ((nextIncome.timeInMillis - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
        _daysToNextIncome.value = daysDiff.coerceAtLeast(0)
    }
    
    // ========== МЕТОДЫ ДЛЯ ПРОФИЛЯ ==========
    
    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            userProfileDao.update(profile)
        }
    }
    
    // ========== МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ РЕЖИМАМИ ==========
    
    fun setStatsMode(mode: StatsMode) {
        _statsMode.value = mode
        if (mode == StatsMode.SINGLE) {
            resetSingleDates()
        } else {
            resetCompareDates()
        }
        loadStats()
    }
    
    fun setStatsPeriod(period: StatsPeriod) {
        _selectedPeriod.value = period
        updateSingleDatesFromPeriod(period)
        loadStats()
    }
    
    fun setSingleDates(start: Long, end: Long) {
        _singleStart.value = start
        _singleEnd.value = end
        loadStats()
    }
    
    fun setPeriodADates(start: Long, end: Long) {
        _periodAStart.value = start
        _periodAEnd.value = end
        loadStats()
    }
    
    fun setPeriodBDates(start: Long, end: Long) {
        _periodBStart.value = start
        _periodBEnd.value = end
        loadStats()
    }
    
    private fun resetSingleDates() {
        _singleStart.value = getStartOfMonth()
        _singleEnd.value = System.currentTimeMillis()
    }
    
    private fun resetCompareDates() {
        _periodAStart.value = getStartOfPreviousMonth()
        _periodAEnd.value = getEndOfPreviousMonth()
        _periodBStart.value = getStartOfMonth()
        _periodBEnd.value = System.currentTimeMillis()
    }
    
    private fun updateSingleDatesFromPeriod(period: StatsPeriod) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        val startDate = when (period) {
            StatsPeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            StatsPeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
            StatsPeriod.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }
            StatsPeriod.ALL_TIME -> 0L
        }
        
        _singleStart.value = startDate
        _singleEnd.value = endDate
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            if (_statsMode.value == StatsMode.SINGLE) {
                loadSingleStats()
            } else {
                loadCompareStats()
            }
        }
    }
    
    private suspend fun loadSingleStats() {
        val startDate = _singleStart.value
        val endDate = _singleEnd.value
        
        try {
            val income = transactionDao.getIncomeForPeriod(startDate, endDate)
            val expenses = transactionDao.getExpensesForPeriod(startDate, endDate)
            val balance = transactionDao.getBalanceForPeriod(startDate, endDate)
            val expenseStats = transactionDao.getCategoryStatsForPeriod(TransactionType.EXPENSE, startDate, endDate)
            val incomeStats = transactionDao.getCategoryStatsForPeriod(TransactionType.INCOME, startDate, endDate)
            val avgDailyExpense = transactionDao.getAverageDailyExpenseForPeriod(startDate, endDate)
            
            _periodIncome.value = income
            _periodExpenses.value = expenses
            _periodBalance.value = balance
            _periodExpenseStats.value = expenseStats
            _periodIncomeStats.value = incomeStats
            _averageDailyExpensePeriod.value = avgDailyExpense
            _topExpenseCategoryPeriod.value = expenseStats.maxByOrNull { it.total }?.let {
                it.category to it.total
            }
            
        } catch (e: Exception) {
            Log.e("FinanceViewModel", "Ошибка загрузки статистики", e)
            resetSingleStats()
        }
    }
    
    private suspend fun loadCompareStats() {
        val aStart = _periodAStart.value
        val aEnd = _periodAEnd.value
        val bStart = _periodBStart.value
        val bEnd = _periodBEnd.value
        
        try {
            val aIncome = transactionDao.getIncomeForPeriod(aStart, aEnd)
            val aExpenses = transactionDao.getExpensesForPeriod(aStart, aEnd)
            val aBalance = transactionDao.getBalanceForPeriod(aStart, aEnd)
            val aExpenseStats = transactionDao.getCategoryStatsForPeriod(TransactionType.EXPENSE, aStart, aEnd)
            val aIncomeStats = transactionDao.getCategoryStatsForPeriod(TransactionType.INCOME, aStart, aEnd)
            
            _periodAIncome.value = aIncome
            _periodAExpenses.value = aExpenses
            _periodABalance.value = aBalance
            _periodAExpenseStats.value = aExpenseStats
            _periodAIncomeStats.value = aIncomeStats
            
            val bIncome = transactionDao.getIncomeForPeriod(bStart, bEnd)
            val bExpenses = transactionDao.getExpensesForPeriod(bStart, bEnd)
            val bBalance = transactionDao.getBalanceForPeriod(bStart, bEnd)
            val bExpenseStats = transactionDao.getCategoryStatsForPeriod(TransactionType.EXPENSE, bStart, bEnd)
            val bIncomeStats = transactionDao.getCategoryStatsForPeriod(TransactionType.INCOME, bStart, bEnd)
            
            _periodBIncome.value = bIncome
            _periodBExpenses.value = bExpenses
            _periodBBalance.value = bBalance
            _periodBExpenseStats.value = bExpenseStats
            _periodBIncomeStats.value = bIncomeStats
            
        } catch (e: Exception) {
            Log.e("FinanceViewModel", "Ошибка загрузки статистики сравнения", e)
            resetCompareStats()
        }
    }
    
    private fun resetSingleStats() {
        _periodIncome.value = 0.0
        _periodExpenses.value = 0.0
        _periodBalance.value = 0.0
        _periodExpenseStats.value = emptyList()
        _periodIncomeStats.value = emptyList()
        _averageDailyExpensePeriod.value = 0.0
        _topExpenseCategoryPeriod.value = null
    }
    
    private fun resetCompareStats() {
        _periodAIncome.value = 0.0
        _periodAExpenses.value = 0.0
        _periodABalance.value = 0.0
        _periodAExpenseStats.value = emptyList()
        _periodAIncomeStats.value = emptyList()
        
        _periodBIncome.value = 0.0
        _periodBExpenses.value = 0.0
        _periodBBalance.value = 0.0
        _periodBExpenseStats.value = emptyList()
        _periodBIncomeStats.value = emptyList()
    }
    
    // ========== ОБНОВЛЕННЫЙ СОВЕТНИК ==========
    
    private fun generateAdvice(
        topCategory: Pair<String, Double>?,
        comparison: Double,
        totalSavings: Double,
        availableBalance: Double,
        profile: UserProfile?
    ): String {
        // Если профиль не заполнен
        if (profile == null) {
            return "👤 Заполните профиль в настройках для персональных советов"
        }
        
        val statusEmojis = profile.getActiveStatusEmojis()
        val daysToIncome = _daysToNextIncome.value
        
        // 1. КРАСНЫЙ УРОВЕНЬ - критично
        if (periodExpenses.value > periodIncome.value && periodIncome.value > 0) {
            val deficit = periodExpenses.value - periodIncome.value
            return "⚠️ КРИТИЧНО: Расходы превышают доходы на ${"%.0f".format(deficit)} ₽! Срочно сократите траты"
        }
        
        // 2. ОРАНЖЕВЫЙ УРОВЕНЬ - важно
        val monthlyObligations = profile.housingPayment + profile.carPayment + profile.totalLoanPayment
        if (monthlyObligations > 0 && availableBalance < monthlyObligations * 1.5) {
            return "⚠️ Свободных средств (${"%.0f".format(availableBalance)} ₽) едва хватает на обязательные платежи (${"%.0f".format(monthlyObligations)} ₽). Будьте осторожны"
        }
        
        // 3. ЖЕЛТЫЙ УРОВЕНЬ - рекомендации по доходу
        if (daysToIncome in 1..7) {
            val dailyBudget = availableBalance / daysToIncome
            return "$statusEmojis До ${getIncomeSourceName(profile.mainIncomeSource)} $daysToIncome дн. Остаток: ${"%.0f".format(availableBalance)} ₽. Лимит на день: ${"%.0f".format(dailyBudget)} ₽"
        }
        
        // 4. ЖЕЛТЫЙ УРОВЕНЬ - рекомендации по статусам
        val adviceList = mutableListOf<String>()
        
        if (profile.isRetiree && daysToIncome in 1..10) {
            adviceList.add("👴 До пенсии $daysToIncome дней")
        }
        
        if (profile.isStudent) {
            topCategory?.let { (cat, amount) ->
                if (cat == "Кафе" || cat == "Рестораны") {
                    adviceList.add("🎓 На кафе уходит ${"%.0f".format(amount)} ₽. Готовка дома сэкономит ${"%.0f".format(amount * 0.4)} ₽")
                }
            }
        }
        
        if (profile.isWorker) {
            topCategory?.let { (cat, amount) ->
                if (cat == "Доставка еды") {
                    adviceList.add("💼 На доставку уходит ${"%.0f".format(amount)} ₽. Самовывоз сэкономит 20%")
                }
            }
        }
        
        if (profile.hasMortgage) {
            adviceList.add("🏠 Платеж по ипотеке ${"%.0f".format(profile.housingPayment)} ₽")
        }
        
        if (profile.hasCarLoan) {
            adviceList.add("🚗 Кредит за авто ${"%.0f".format(profile.carPayment)} ₽/мес")
        }
        
        if (profile.hasChildren) {
            adviceList.add("👶 На детей (${profile.dependents}) запланируйте бюджет")
        }
        
        if (adviceList.isNotEmpty()) {
            return "$statusEmojis ${adviceList.first()}"
        }
        
        // 5. ЗЕЛЕНЫЙ УРОВЕНЬ - мотивация
        if (totalSavings > 100000) {
            return "🏆 Отличные накопления! ${"%.0f".format(totalSavings)} ₽"
        }
        
        if (comparison < -10) {
            return "📉 Отлично! Расходы снизились на ${"%.0f".format(-comparison)}%"
        }
        
        // 6. СИНИЙ УРОВЕНЬ - информация
        return "💡 Свободно ${"%.0f".format(availableBalance)} ₽. Рекомендуем отложить 10% (${"%.0f".format(availableBalance * 0.1)} ₽)"
    }
    
    private fun getIncomeSourceName(source: String): String {
        return when (source) {
            "Зарплата" -> "зарплаты"
            "Пенсия" -> "пенсии"
            else -> "дохода"
        }
    }
    
    // ========== СТАНДАРТНЫЕ МЕТОДЫ ==========
    
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
            loadStats()
            analyzeIncome() // Пересчитываем анализ доходов
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.update(transaction)
            updateWidget()
            loadStats()
            analyzeIncome()
        }
    }
    
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionDao.delete(transaction)
            updateWidget()
            loadStats()
            analyzeIncome()
        }
    }
    
    fun deleteAllTransactions() {
        viewModelScope.launch {
            transactionDao.deleteAll()
            updateWidget()
            loadStats()
            analyzeIncome()
        }
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
            val transaction = Transaction(
                type = TransactionType.EXPENSE,
                category = payment.category,
                amount = payment.amount,
                description = "Регулярный платеж: ${payment.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis
            
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, payment.dayOfMonth)
            val nextDue = calendar.timeInMillis
            
            val updatedPayment = payment.copy(
                lastPaidDate = today,
                nextDueDate = nextDue
            )
            
            regularPaymentDao.update(updatedPayment)
            
            updateWidget()
            loadStats()
            analyzeIncome()
        }
    }
    
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
            
            val transaction = Transaction(
                type = TransactionType.SAVING,
                category = category,
                amount = amount,
                description = "Накопление: $name",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            updateWidget()
            loadStats()
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
            
            val transaction = Transaction(
                type = TransactionType.INCOME,
                category = saving.category,
                amount = saving.amount,
                description = "Возврат из накоплений: ${saving.name}",
                date = System.currentTimeMillis()
            )
            transactionDao.insert(transaction)
            
            updateWidget()
            loadStats()
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
                
                val transaction = Transaction(
                    type = TransactionType.SAVING,
                    category = saving.category,
                    amount = amount,
                    description = "Пополнение: ${saving.name}",
                    date = System.currentTimeMillis()
                )
                transactionDao.insert(transaction)
                
                updateWidget()
                loadStats()
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
                
                val transaction = Transaction(
                    type = TransactionType.INCOME,
                    category = saving.category,
                    amount = amount,
                    description = "Снятие из накоплений: ${saving.name}",
                    date = System.currentTimeMillis()
                )
                transactionDao.insert(transaction)
                
                updateWidget()
                loadStats()
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
                    
                    updateWidget()
                    loadStats()
                }
            } else {
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
                }
            }
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
                TransactionType.SAVING -> -transaction.amount
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
        
        workManager.cancelUniqueWork("payment_reminders")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
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
    
    companion object {
        private fun getStartOfMonth(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
        
        private fun getStartOfPreviousMonth(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.MONTH, -1)
            return calendar.timeInMillis
        }
        
        private fun getEndOfPreviousMonth(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }
    }
}