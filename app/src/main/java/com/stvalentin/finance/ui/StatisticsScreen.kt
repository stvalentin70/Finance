package com.stvalentin.finance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stvalentin.finance.data.CategoryStat
import com.stvalentin.finance.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    // Общие данные
    val balance by viewModel.balance.collectAsState()
    
    // Режим и периоды
    val statsMode by viewModel.statsMode.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val periodAStart by viewModel.periodAStart.collectAsState()
    val periodAEnd by viewModel.periodAEnd.collectAsState()
    val periodBStart by viewModel.periodBStart.collectAsState()
    val periodBEnd by viewModel.periodBEnd.collectAsState()
    
    // Данные для обычного режима
    val periodIncome by viewModel.periodIncome.collectAsState()
    val periodExpenses by viewModel.periodExpenses.collectAsState()
    val periodBalance by viewModel.periodBalance.collectAsState()
    val periodExpenseStats by viewModel.periodExpenseStats.collectAsState()
    val periodIncomeStats by viewModel.periodIncomeStats.collectAsState()
    val averageDailyExpense by viewModel.averageDailyExpensePeriod.collectAsState()
    val topExpenseCategory by viewModel.topExpenseCategoryPeriod.collectAsState()
    
    // Данные для сравнения
    val periodAIncome by viewModel.periodAIncome.collectAsState()
    val periodAExpenses by viewModel.periodAExpenses.collectAsState()
    val periodABalance by viewModel.periodABalance.collectAsState()
    val periodAExpenseStats by viewModel.periodAExpenseStats.collectAsState()
    val periodAIncomeStats by viewModel.periodAIncomeStats.collectAsState()
    
    val periodBIncome by viewModel.periodBIncome.collectAsState()
    val periodBExpenses by viewModel.periodBExpenses.collectAsState()
    val periodBBalance by viewModel.periodBBalance.collectAsState()
    val periodBExpenseStats by viewModel.periodBExpenseStats.collectAsState()
    val periodBIncomeStats by viewModel.periodBIncomeStats.collectAsState()
    
    // График
    val balanceHistory by viewModel.balanceHistory.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    
    // Состояния UI
    var expanded by remember { mutableStateOf(false) }
    var showDatePickerA by remember { mutableStateOf(false) }
    var showDatePickerB by remember { mutableStateOf(false) }
    
    // Названия периодов
    val periodNames = mapOf(
        StatsPeriod.WEEK to "Неделя",
        StatsPeriod.MONTH to "Месяц",
        StatsPeriod.YEAR to "Год",
        StatsPeriod.ALL_TIME to "Всё время"
    )
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Статистика",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Кнопка выбора периода
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать период"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Режимы
                            DropdownMenuItem(
                                text = { Text("Обычный режим") },
                                onClick = {
                                    viewModel.setStatsMode(StatsMode.SINGLE)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Режим сравнения") },
                                onClick = {
                                    viewModel.setStatsMode(StatsMode.COMPARE)
                                    expanded = false
                                }
                            )
                            Divider()
                            // Периоды для обычного режима
                            StatsPeriod.values().forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(periodNames[period] ?: "") },
                                    onClick = {
                                        viewModel.setStatsPeriod(period)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (statsMode == StatsMode.SINGLE) {
                // БАЛАНС ЗА ПЕРИОД
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
                                text = "БАЛАНС ЗА ${periodNames[selectedPeriod]?.uppercase()}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currencyFormat.format(periodBalance),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (periodBalance >= 0) IncomeGreen else ExpenseRed
                            )
                            
                            if (selectedPeriod != StatsPeriod.ALL_TIME) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "За всё время: ${currencyFormat.format(balance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                // ДОХОДЫ И РАСХОДЫ ЗА ПЕРИОД
                if (periodIncome > 0 || periodExpenses > 0) {
                    item {
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
                                Text(
                                    text = "ДОХОДЫ И РАСХОДЫ ЗА ${periodNames[selectedPeriod]?.uppercase()}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    // Доходы
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
                                                style = MaterialTheme.typography.labelMedium,
                                                color = IncomeGreen
                                            )
                                        }
                                        Text(
                                            text = currencyFormat.format(periodIncome),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = IncomeGreen
                                        )
                                    }
                                    
                                    VerticalDivider(
                                        modifier = Modifier.height(40.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    
                                    // Расходы
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
                                                style = MaterialTheme.typography.labelMedium,
                                                color = ExpenseRed
                                            )
                                        }
                                        Text(
                                            text = currencyFormat.format(periodExpenses),
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
                }
                
                // АНАЛИТИКА ЗА ПЕРИОД
                if (periodExpenses > 0) {
                    item {
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
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📊 АНАЛИТИКА ЗА ${periodNames[selectedPeriod]?.uppercase()}",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Divider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )
                                
                                if (averageDailyExpense > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Средний расход в день:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = currencyFormat.format(averageDailyExpense),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ExpenseRed
                                        )
                                    }
                                }
                                
                                topExpenseCategory?.let { (category, amount) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Самая затратная категория:",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$category • ${currencyFormat.format(amount)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // ДИНАМИКА БАЛАНСА
                if (balanceHistory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "ДИНАМИКА БАЛАНСА (30 ДНЕЙ)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                BalanceChart(
                                    data = balanceHistory,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                // РАЗДЕЛИТЕЛЬ
                if (periodExpenseStats.isNotEmpty() || periodIncomeStats.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                // РАСХОДЫ ПО КАТЕГОРИЯМ
                if (periodExpenseStats.isNotEmpty()) {
                    item {
                        Text(
                            text = "РАСХОДЫ ПО КАТЕГОРИЯМ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                periodExpenseStats.forEach { stat ->
                                    val percentage = if (periodExpenses > 0) {
                                        (stat.total / periodExpenses * 100).toInt()
                                    } else 0
                                    CategoryStatItem(
                                        category = stat.category,
                                        amount = stat.total,
                                        percentage = percentage,
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
                
                // МИНИМАЛЬНЫЙ ОТСТУП
                if (periodExpenseStats.isNotEmpty() && periodIncomeStats.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // ДОХОДЫ ПО КАТЕГОРИЯМ
                if (periodIncomeStats.isNotEmpty()) {
                    item {
                        Text(
                            text = "ДОХОДЫ ПО КАТЕГОРИЯМ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                periodIncomeStats.forEach { stat ->
                                    val percentage = if (periodIncome > 0) {
                                        (stat.total / periodIncome * 100).toInt()
                                    } else 0
                                    CategoryStatItem(
                                        category = stat.category,
                                        amount = stat.total,
                                        percentage = percentage,
                                        color = IncomeGreen
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // РЕЖИМ СРАВНЕНИЯ
                
                // ЗАГОЛОВОК СРАВНЕНИЯ
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
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "СРАВНЕНИЕ ПЕРИОДОВ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Период А
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePickerA = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Период А: ${dateFormat.format(Date(periodAStart))} - ${dateFormat.format(Date(periodAEnd))}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Изменить",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            // Период Б
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePickerB = true }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Период Б: ${dateFormat.format(Date(periodBStart))} - ${dateFormat.format(Date(periodBEnd))}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Изменить",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                
                // СРАВНЕНИЕ БАЛАНСА
                item {
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
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Период А
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Период А",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyFormat.format(periodABalance),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (periodABalance >= 0) IncomeGreen else ExpenseRed
                                    )
                                }
                                
                                // Процент изменения
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val percentChange = if (periodABalance != 0.0) {
                                        ((periodBBalance - periodABalance) / periodABalance * 100).toInt()
                                    } else 0
                                    val sign = if (percentChange > 0) "+" else ""
                                    val color = when {
                                        percentChange > 0 -> IncomeGreen
                                        percentChange < 0 -> ExpenseRed
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    
                                    Text(
                                        text = "Изменение",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$sign$percentChange%",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = color
                                    )
                                }
                                
                                // Период Б
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Период Б",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currencyFormat.format(periodBBalance),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (periodBBalance >= 0) IncomeGreen else ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
                
                // СРАВНЕНИЕ ДОХОДОВ И РАСХОДОВ
                item {
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
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CompareRow(
                                label = "Доходы",
                                valueA = periodAIncome,
                                valueB = periodBIncome,
                                colorA = IncomeGreen,
                                colorB = IncomeGreen,
                                currencyFormat = currencyFormat
                            )
                            
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                            
                            CompareRow(
                                label = "Расходы",
                                valueA = periodAExpenses,
                                valueB = periodBExpenses,
                                colorA = ExpenseRed,
                                colorB = ExpenseRed,
                                currencyFormat = currencyFormat
                            )
                        }
                    }
                }
                
                // ДИНАМИКА БАЛАНСА
                if (balanceHistory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "ДИНАМИКА БАЛАНСА (30 ДНЕЙ)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                BalanceChart(
                                    data = balanceHistory,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                // РАЗДЕЛИТЕЛЬ
                if (periodAExpenseStats.isNotEmpty() || periodBExpenseStats.isNotEmpty() ||
                    periodAIncomeStats.isNotEmpty() || periodBIncomeStats.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                // РАСХОДЫ ПО КАТЕГОРИЯМ
                if (periodAExpenseStats.isNotEmpty() || periodBExpenseStats.isNotEmpty()) {
                    item {
                        Text(
                            text = "РАСХОДЫ ПО КАТЕГОРИЯМ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    val allCategories = (periodAExpenseStats.map { it.category } + 
                                        periodBExpenseStats.map { it.category }).toSet()
                    
                    allCategories.forEach { category ->
                        val statA = periodAExpenseStats.find { it.category == category }
                        val statB = periodBExpenseStats.find { it.category == category }
                        val amountA = statA?.total ?: 0.0
                        val amountB = statB?.total ?: 0.0
                        
                        if (amountA > 0 || amountB > 0) {
                            item {
                                CompareCategoryItem(
                                    category = category,
                                    amountA = amountA,
                                    amountB = amountB,
                                    color = ExpenseRed,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }
                }
                
                // МИНИМАЛЬНЫЙ ОТСТУП
                if ((periodAExpenseStats.isNotEmpty() || periodBExpenseStats.isNotEmpty()) &&
                    (periodAIncomeStats.isNotEmpty() || periodBIncomeStats.isNotEmpty())) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // ДОХОДЫ ПО КАТЕГОРИЯМ
                if (periodAIncomeStats.isNotEmpty() || periodBIncomeStats.isNotEmpty()) {
                    item {
                        Text(
                            text = "ДОХОДЫ ПО КАТЕГОРИЯМ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    val allCategories = (periodAIncomeStats.map { it.category } + 
                                        periodBIncomeStats.map { it.category }).toSet()
                    
                    allCategories.forEach { category ->
                        val statA = periodAIncomeStats.find { it.category == category }
                        val statB = periodBIncomeStats.find { it.category == category }
                        val amountA = statA?.total ?: 0.0
                        val amountB = statB?.total ?: 0.0
                        
                        if (amountA > 0 || amountB > 0) {
                            item {
                                CompareCategoryItem(
                                    category = category,
                                    amountA = amountA,
                                    amountB = amountB,
                                    color = IncomeGreen,
                                    currencyFormat = currencyFormat
                                )
                            }
                        }
                    }
                }
            }
            
            // ПУСТОЙ ЭКРАН
            if (periodExpenseStats.isEmpty() && periodIncomeStats.isEmpty() && 
                periodAExpenseStats.isEmpty() && periodAIncomeStats.isEmpty() &&
                periodBExpenseStats.isEmpty() && periodBIncomeStats.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет данных за выбранный период",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Добавьте транзакции или выберите другой период",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Диалоги выбора дат
    if (showDatePickerA) {
        DateTimePickerDialog(
            onDateTimeSelected = { timestamp ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                
                viewModel.setPeriodADates(start, end)
                showDatePickerA = false
            },
            onDismiss = { showDatePickerA = false },
            initialDateTime = periodAStart
        )
    }
    
    if (showDatePickerB) {
        DateTimePickerDialog(
            onDateTimeSelected = { timestamp ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                
                viewModel.setPeriodBDates(start, end)
                showDatePickerB = false
            },
            onDismiss = { showDatePickerB = false },
            initialDateTime = periodBStart
        )
    }
}

@Composable
fun CompareRow(
    label: String,
    valueA: Double,
    valueB: Double,
    colorA: androidx.compose.ui.graphics.Color,
    colorB: androidx.compose.ui.graphics.Color,
    currencyFormat: NumberFormat
) {
    val percentChange = if (valueA != 0.0) {
        ((valueB - valueA) / valueA * 100).toInt()
    } else 0
    val sign = if (percentChange > 0) "+" else ""
    val changeColor = when {
        percentChange > 0 -> colorB
        percentChange < 0 -> colorA
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = currencyFormat.format(valueA),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colorA,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Text(
            text = "$sign$percentChange%",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = changeColor,
            modifier = Modifier.width(60.dp)
        )
        
        Text(
            text = currencyFormat.format(valueB),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = colorB,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun CompareCategoryItem(
    category: String,
    amountA: Double,
    amountB: Double,
    color: androidx.compose.ui.graphics.Color,
    currencyFormat: NumberFormat
) {
    val percentChange = if (amountA != 0.0) {
        ((amountB - amountA) / amountA * 100).toInt()
    } else 0
    val sign = if (percentChange > 0) "+" else ""
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = currencyFormat.format(amountA),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Text(
                text = "$sign$percentChange%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                modifier = Modifier.width(60.dp)
            )
            
            Text(
                text = currencyFormat.format(amountB),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun CategoryStatItem(
    category: String,
    amount: Double,
    percentage: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${currencyFormat.format(amount)} ($percentage%)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}