package com.stvalentin.finance.ui

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
import com.stvalentin.finance.data.TransactionType
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    // Общие данные (за всё время)
    val balance by viewModel.balance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    
    // Данные за выбранный период
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val periodIncome by viewModel.periodIncome.collectAsState()
    val periodExpenses by viewModel.periodExpenses.collectAsState()
    val periodBalance by viewModel.periodBalance.collectAsState()
    val periodExpenseStats by viewModel.periodExpenseStats.collectAsState()
    val periodIncomeStats by viewModel.periodIncomeStats.collectAsState()
    val averageDailyExpense by viewModel.averageDailyExpensePeriod.collectAsState()
    val topExpenseCategory by viewModel.topExpenseCategoryPeriod.collectAsState()
    
    // График
    val balanceHistory by viewModel.balanceHistory.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    // Состояние для выпадающего меню
    var expanded by remember { mutableStateOf(false) }
    
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
                            style = MaterialTheme.typography.titleSmall.copy(  // Изменено на titleSmall
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
                                style = MaterialTheme.typography.titleSmall.copy(  // Изменено на titleSmall
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
                                style = MaterialTheme.typography.titleSmall.copy(  // Изменено на titleSmall
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
                        style = MaterialTheme.typography.titleSmall.copy(  // Изменено на titleSmall
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
                                val percentage = (stat.total / periodExpenses * 100).toInt()
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
            
            // МИНИМАЛЬНЫЙ ОТСТУП МЕЖДУ РАСХОДАМИ И ДОХОДАМИ
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
                        style = MaterialTheme.typography.titleSmall.copy(  // Изменено на titleSmall
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
                                val percentage = (stat.total / periodIncome * 100).toInt()
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
            
            // ПУСТОЙ ЭКРАН
            if (periodExpenseStats.isEmpty() && periodIncomeStats.isEmpty()) {
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
}

// Компонент для отображения категории
@Composable
fun CategoryStatItem(
    category: String,
    amount: Double,
    percentage: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp), // Минимальный отступ
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium, // Изменено на bodyMedium (14sp)
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${currencyFormat.format(amount)} ($percentage%)",
            style = MaterialTheme.typography.bodyMedium.copy( // Изменено на bodyMedium (14sp) жирный
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
    }
}