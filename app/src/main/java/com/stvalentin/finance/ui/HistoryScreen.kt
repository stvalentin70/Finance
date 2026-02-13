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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryPeriod {
    TODAY, WEEK, MONTH, YEAR, ALL_TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    
    // Состояния для фильтров
    var selectedPeriod by remember { mutableStateOf(HistoryPeriod.ALL_TIME) }
    var selectedType by remember { mutableStateOf<TransactionType?>(null) } // null = все операции
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    // Фильтрация транзакций по периоду
    val filteredByPeriod = remember(allTransactions, selectedPeriod) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        when (selectedPeriod) {
            HistoryPeriod.TODAY -> {
                val startOfDay = getStartOfDay(now)
                val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
                allTransactions.filter { it.date in startOfDay..endOfDay }
            }
            HistoryPeriod.WEEK -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val startOfWeek = getStartOfDay(calendar.timeInMillis)
                allTransactions.filter { it.date >= startOfWeek }
            }
            HistoryPeriod.MONTH -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = getStartOfDay(calendar.timeInMillis)
                allTransactions.filter { it.date >= startOfMonth }
            }
            HistoryPeriod.YEAR -> {
                calendar.timeInMillis = now
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val startOfYear = getStartOfDay(calendar.timeInMillis)
                allTransactions.filter { it.date >= startOfYear }
            }
            HistoryPeriod.ALL_TIME -> allTransactions
        }
    }
    
    // Фильтрация по типу (доход/расход)
    val filteredTransactions = remember(filteredByPeriod, selectedType) {
        if (selectedType == null) {
            filteredByPeriod
        } else {
            filteredByPeriod.filter { it.type == selectedType }
        }
    }.sortedByDescending { it.date }
    
    // Подсчет итогов за выбранный период
    val periodIncome = remember(filteredByPeriod) {
        filteredByPeriod
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }
    
    val periodExpenses = remember(filteredByPeriod) {
        filteredByPeriod
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    
    val periodBalance = periodIncome - periodExpenses
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "История операций",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ПЕРИОД (ВСЕГДА ПЕРВЫМ)
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
                            text = "📅 ПЕРИОД",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        // Кнопки выбора периода
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryPeriodButton(
                                text = "Сегодня",
                                isSelected = selectedPeriod == HistoryPeriod.TODAY,
                                onClick = { selectedPeriod = HistoryPeriod.TODAY },
                                modifier = Modifier.weight(1f)
                            )
                            HistoryPeriodButton(
                                text = "Неделя",
                                isSelected = selectedPeriod == HistoryPeriod.WEEK,
                                onClick = { selectedPeriod = HistoryPeriod.WEEK },
                                modifier = Modifier.weight(1f)
                            )
                            HistoryPeriodButton(
                                text = "Месяц",
                                isSelected = selectedPeriod == HistoryPeriod.MONTH,
                                onClick = { selectedPeriod = HistoryPeriod.MONTH },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HistoryPeriodButton(
                                text = "Год",
                                isSelected = selectedPeriod == HistoryPeriod.YEAR,
                                onClick = { selectedPeriod = HistoryPeriod.YEAR },
                                modifier = Modifier.weight(1f)
                            )
                            HistoryPeriodButton(
                                text = "Всё время",
                                isSelected = selectedPeriod == HistoryPeriod.ALL_TIME,
                                onClick = { selectedPeriod = HistoryPeriod.ALL_TIME },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // 2. ИТОГИ ЗА ПЕРИОД
            if (filteredByPeriod.isNotEmpty()) {
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
                            val periodText = when (selectedPeriod) {
                                HistoryPeriod.TODAY -> "СЕГОДНЯ"
                                HistoryPeriod.WEEK -> "ЗА НЕДЕЛЮ"
                                HistoryPeriod.MONTH -> "ЗА МЕСЯЦ"
                                HistoryPeriod.YEAR -> "ЗА ГОД"
                                HistoryPeriod.ALL_TIME -> "ЗА ВСЁ ВРЕМЯ"
                            }
                            
                            Text(
                                text = periodText,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Баланс за период
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Баланс:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = currencyFormat.format(periodBalance),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (periodBalance >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Доходы
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Доходы",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
                                
                                Divider(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                )
                                
                                // Расходы
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Расходы",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
            
            // 3. ФИЛЬТР ПО ТИПУ (ВТОРЫМ)
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
                            text = "📊 ТИП ОПЕРАЦИЙ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Все операции
                            FilterChip(
                                selected = selectedType == null,
                                onClick = { selectedType = null },
                                label = { 
                                    Text(
                                        text = "Все",
                                        fontSize = 13.sp
                                    ) 
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            
                            // Только доходы
                            FilterChip(
                                selected = selectedType == TransactionType.INCOME,
                                onClick = { selectedType = TransactionType.INCOME },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedType == TransactionType.INCOME) 
                                                Color.White else IncomeGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Доходы",
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen,
                                    selectedLabelColor = Color.White,
                                    labelColor = IncomeGreen
                                )
                            )
                            
                            // Только расходы
                            FilterChip(
                                selected = selectedType == TransactionType.EXPENSE,
                                onClick = { selectedType = TransactionType.EXPENSE },
                                label = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (selectedType == TransactionType.EXPENSE) 
                                                Color.White else ExpenseRed
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Расходы",
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ExpenseRed,
                                    selectedLabelColor = Color.White,
                                    labelColor = ExpenseRed
                                )
                            )
                        }
                    }
                }
            }
            
            // 4. ЗАГОЛОВОК СПИСКА
            if (filteredTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ОПЕРАЦИИ",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${filteredTransactions.size} ${getPluralForm(filteredTransactions.size)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // 5. СПИСОК ТРАНЗАКЦИЙ
            if (filteredTransactions.isNotEmpty()) {
                items(filteredTransactions) { transaction ->
                    HistoryTransactionItem(
                        transaction = transaction,
                        onTransactionClick = {
                            navController.navigate("add_transaction/${transaction.id}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // ПУСТОЙ ЭКРАН
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                filteredByPeriod.isEmpty() -> "Нет операций за выбранный период"
                                selectedType != null -> "Нет ${if (selectedType == TransactionType.INCOME) "доходов" else "расходов"} за этот период"
                                else -> "Нет операций"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Измените параметры фильтрации или добавьте новую операцию",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("add_transaction/0") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить операцию")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryPeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun HistoryTransactionItem(
    transaction: Transaction,
    onTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("ru")) }
    
    Card(
        modifier = modifier
            .clickable { onTransactionClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка категории
            Icon(
                imageVector = getCategoryIcon(transaction.category, transaction.type),
                contentDescription = null,
                tint = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Информация
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                if (transaction.description.isNotBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                // Дата и время
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${dateFormat.format(Date(transaction.date))} ${timeFormat.format(Date(transaction.date))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // Сумма
            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
            )
        }
    }
}

// Вспомогательные функции
private fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun getPluralForm(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "операция"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "операции"
        else -> "операций"
    }
}