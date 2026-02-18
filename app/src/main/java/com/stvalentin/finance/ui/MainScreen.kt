package com.stvalentin.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.stvalentin.finance.data.RegularPayment
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.UserProfile
import java.text.NumberFormat
import java.util.*

// ⚠️ УБЕДИТЕСЬ, ЧТО ЭТИ ЦВЕТА НЕ ОПРЕДЕЛЕНЫ В ДРУГОМ ФАЙЛЕ
// Если они уже есть в другом месте, удалите эти строки
// val IncomeGreen = Color(0xFF4CAF50)
// val ExpenseRed = Color(0xFFF44336)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    viewModel: FinanceViewModel,
    navController: NavController
) {
    val balance by viewModel.balance.collectAsState()
    val income by viewModel.totalIncome.collectAsState()
    val expenses by viewModel.totalExpenses.collectAsState()
    val adviceMessage by viewModel.adviceMessage.collectAsState()
    val payments by viewModel.regularPayments.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val daysToIncome by viewModel.daysToNextIncome.collectAsState()
    val averageIncome by viewModel.averageMonthlyIncome.collectAsState()
    val incomeStability by viewModel.incomeStability.collectAsState()
    val availableBalance by viewModel.availableBalance.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val periodExpenses by viewModel.periodExpenses.collectAsState()
    val periodIncome by viewModel.periodIncome.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    currencyFormat.maximumFractionDigits = 0
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Плюс на цветном кружке
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { onAddTransactionClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить транзакцию",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = "Трекер финансов",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        // Шестеренка справа от названия
                        IconButton(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Настройки",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ФИНАНСОВЫЙ ОБЗОР
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ФИНАНСОВЫЙ ОБЗОР",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = currencyFormat.format(balance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (balance >= 0) IncomeGreen else ExpenseRed,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Доходы",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = currencyFormat.format(income),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = IncomeGreen
                                )
                            }
                            
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Расходы",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = currencyFormat.format(expenses),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
            
            // 2. БЛИЖАЙШИЕ ПЛАТЕЖИ (из календаря)
            if (payments.isNotEmpty()) {
                item {
                    UpcomingPaymentsSection(
                        payments = payments,
                        onViewAllClick = {
                            navController.navigate("payment_calendar")
                        },
                        onPayNow = { payment ->
                            viewModel.markPaymentAsPaid(payment)
                        }
                    )
                }
            }
            
            // 3. УМНЫЙ СОВЕТНИК (обновленная версия)
            item {
                // Генерируем структурированный совет на основе данных
                val smartAdvice = createSmartAdvice(
                    rawMessage = adviceMessage,
                    profile = profile,
                    daysToIncome = daysToIncome,
                    averageIncome = averageIncome,
                    incomeStability = incomeStability,
                    availableBalance = availableBalance,
                    totalSavings = totalSavings,
                    periodExpenses = periodExpenses,
                    periodIncome = periodIncome
                )
                
                SmartAdviceCard(
                    advice = smartAdvice,
                    profile = profile,
                    onClick = { route ->
                        when (route) {
                            "payment_calendar" -> navController.navigate("payment_calendar")
                            "savings" -> navController.navigate("savings")
                            "statistics" -> navController.navigate("statistics")
                            "user_profile" -> navController.navigate("user_profile")
                            "income_analysis" -> navController.navigate("income_analysis")
                            "add_transaction" -> onAddTransactionClick()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UpcomingPaymentsSection(
    payments: List<RegularPayment>,
    onViewAllClick: () -> Unit,
    onPayNow: (RegularPayment) -> Unit
) {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Фильтруем неоплаченные платежи на ближайшие дни
    val upcomingPayments = payments
        .filter { !it.isPaidThisMonth() && it.dayOfMonth >= today }
        .sortedBy { it.dayOfMonth }
        .take(3)
    
    if (upcomingPayments.isEmpty()) return
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    currencyFormat.maximumFractionDigits = 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 БЛИЖАЙШИЕ ПЛАТЕЖИ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(onClick = onViewAllClick) {
                    Text("Все →", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            upcomingPayments.forEach { payment ->
                val dayDiff = payment.dayOfMonth - today
                val dayText = when (dayDiff) {
                    0 -> "Сегодня"
                    1 -> "Завтра"
                    else -> "Через $dayDiff дн."
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = payment.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = dayText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencyFormat.format(payment.amount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ExpenseRed,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        Button(
                            onClick = { onPayNow(payment) },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IncomeGreen
                            )
                        ) {
                            Text("Оплатить", fontSize = 10.sp)
                        }
                    }
                }
                
                if (payment != upcomingPayments.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

// Функция создания структурированного совета (НЕ @Composable)
fun createSmartAdvice(
    rawMessage: String,
    profile: UserProfile?,
    daysToIncome: Int,
    averageIncome: Double,
    incomeStability: Double,
    availableBalance: Double,
    totalSavings: Double,
    periodExpenses: Double,
    periodIncome: Double
): SmartAdvice {
    
    // 1. КРИТИЧЕСКИЙ УРОВЕНЬ - проверяем просрочки и отрицательный баланс
    if (availableBalance < -1000) {
        return SmartAdvice(
            id = 1,
            title = "⚠️ Критический минус!",
            description = "Баланс отрицательный: ${formatAmount(availableBalance)} ₽. Срочно пополните счет.",
            priority = AdvicePriority.CRITICAL,
            icon = Icons.Default.Warning,
            action = "Пополнить счет",
            route = "add_transaction"
        )
    }
    
    // 2. ПРОВЕРКА НА ПРЕВЫШЕНИЕ РАСХОДОВ
    if (periodIncome > 0 && periodExpenses > periodIncome * 1.5) {
        return SmartAdvice(
            id = 2,
            title = "📊 Расходы зашкаливают!",
            description = "В этом месяце расходы в 1.5 раза выше дохода. Срочно пересмотрите бюджет.",
            priority = AdvicePriority.CRITICAL,
            icon = Icons.Default.TrendingDown,
            action = "Анализ расходов",
            route = "statistics"
        )
    }
    
    // 3. ОРАНЖЕВЫЙ УРОВЕНЬ - скоро зарплата и мало денег
    if (daysToIncome in 1..7 && availableBalance < averageIncome * 0.3 && averageIncome > 0) {
        val daysLeft = if (daysToIncome == 1) "завтра" else "через $daysToIncome дней"
        val dailyBudget = if (daysToIncome > 0) availableBalance / daysToIncome else availableBalance
        
        return SmartAdvice(
            id = 3,
            title = "💰 До зарплаты $daysLeft",
            description = "Осталось ${formatAmount(availableBalance)} ₽. Лимит на день: ${formatAmount(dailyBudget)} ₽",
            priority = AdvicePriority.HIGH,
            icon = Icons.Default.Timer,
            action = "Планирование бюджета",
            route = "statistics"
        )
    }
    
    // 4. ОРАНЖЕВЫЙ УРОВЕНЬ - нестабильный доход
    if (incomeStability < 0.5 && profile?.isWorker == true) {
        return SmartAdvice(
            id = 4,
            title = "📉 Доход нестабильный",
            description = "Стабильность дохода ${(incomeStability * 100).toInt()}%. Рекомендуем создать подушку безопасности.",
            priority = AdvicePriority.HIGH,
            icon = Icons.Default.ShowChart,
            action = "Как повысить стабильность",
            route = null
        )
    }
    
    // 5. ЖЕЛТЫЙ УРОВЕНЬ - советы по статусам
    profile?.let {
        // Для студентов
        if (it.isStudent && periodExpenses > 0) {
            val cafeExpense = periodExpenses * 0.2
            return SmartAdvice(
                id = 5,
                title = "🎓 Студенческий совет",
                description = "В этом месяце на кафе потрачено ${formatAmount(cafeExpense)} ₽. Готовка дома сэкономит 30%",
                priority = AdvicePriority.MEDIUM,
                icon = Icons.Default.School,
                action = "Посчитать экономию",
                route = "statistics"
            )
        }
        
        // Для пенсионеров
        if (it.isRetiree && daysToIncome in 1..10) {
            return SmartAdvice(
                id = 6,
                title = "👴 До пенсии $daysToIncome дней",
                description = "Запланируйте крупные покупки на день после пенсии",
                priority = AdvicePriority.MEDIUM,
                icon = Icons.Default.Elderly,
                action = "Планировщик",
                route = "payment_calendar"
            )
        }
        
        // Для семей с детьми
        if (it.hasChildren && it.dependents > 0) {
            return SmartAdvice(
                id = 7,
                title = "👶 Семейный бюджет",
                description = "На ${it.dependents} ${getChildWord(it.dependents)} рекомендуется откладывать ${formatAmount(averageIncome * 0.15)} ₽ в месяц",
                priority = AdvicePriority.MEDIUM,
                icon = Icons.Default.FamilyRestroom,
                action = "Настроить накопления",
                route = "savings"
            )
        }
        
        // Для ипотечников
        if (it.hasMortgage && it.housingPayment > 0) {
            return SmartAdvice(
                id = 8,
                title = "🏠 Ипотека ${formatAmount(it.housingPayment)} ₽/мес",
                description = "Досрочное погашение сэкономит годы выплат",
                priority = AdvicePriority.MEDIUM,
                icon = Icons.Default.Home,
                action = "Рассчитать досрочно",
                route = null
            )
        }
    }
    
    // 6. ЗЕЛЕНЫЙ УРОВЕНЬ - всё хорошо
    if (totalSavings > 100000 && averageIncome > 0) {
        val months = (totalSavings / averageIncome).toInt()
        return SmartAdvice(
            id = 9,
            title = "🏆 Финансовая подушка!",
            description = "Накоплено ${formatAmount(totalSavings)} ₽ (${months} ${getMonthWord(months)} жизни). Отличный результат!",
            priority = AdvicePriority.NORMAL,
            icon = Icons.Default.Star,
            action = "Посмотреть накопления",
            route = "savings"
        )
    }
    
    // 7. СИНИЙ УРОВЕНЬ - позитивные советы
    if (availableBalance > averageIncome * 0.5 && averageIncome > 0) {
        val recommendToSave = availableBalance * 0.1
        return SmartAdvice(
            id = 10,
            title = "💪 Свободные средства",
            description = "Свободно ${formatAmount(availableBalance)} ₽. Рекомендуем отложить 10% (${formatAmount(recommendToSave)} ₽) в копилку",
            priority = AdvicePriority.ACHIEVEMENT,
            icon = Icons.Default.Savings,
            action = "Пополнить копилку",
            route = "savings"
        )
    }
    
    // 8. По умолчанию
    return SmartAdvice(
        id = 0,
        title = "",
        description = rawMessage,
        priority = AdvicePriority.NORMAL,
        icon = Icons.Default.Lightbulb,
        action = null,
        route = null
    )
}

fun formatAmount(amount: Double): String {
    return String.format("%,.0f", amount).replace(",", " ")
}

fun getChildWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "ребенка"
        count % 10 in 2..4 && (count % 100 !in 12..14) -> "детей"
        else -> "детей"
    }
}

fun getMonthWord(months: Int): String {
    return when {
        months % 10 == 1 && months % 100 != 11 -> "месяц"
        months % 10 in 2..4 && (months % 100 !in 12..14) -> "месяца"
        else -> "месяцев"
    }
}