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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val transactions by viewModel.allTransactions.collectAsState()
    
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("ru")) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    // Фильтруем транзакции по выбранной дате
    val transactionsForSelectedDate = remember(transactions, selectedDate) {
        val startOfDay = getStartOfDay(selectedDate)
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
        
        transactions.filter { transaction ->
            transaction.date in startOfDay..endOfDay
        }.sortedByDescending { it.date }
    }
    
    // Подсчет доходов и расходов за выбранный день
    val dailyIncome = remember(transactionsForSelectedDate) {
        transactionsForSelectedDate
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }
    
    val dailyExpenses = remember(transactionsForSelectedDate) {
        transactionsForSelectedDate
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }
    
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
            // КАРТОЧКА ВЫБОРА ДАТЫ
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
                            text = monthYearFormat.format(Date(selectedDate)).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Дата",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Выберите дату",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = dateFormat.format(Date(selectedDate)),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                            
                            Button(
                                onClick = { showDatePicker = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text("Изменить", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
            
            // ИТОГИ ЗА ДЕНЬ
            if (transactionsForSelectedDate.isNotEmpty()) {
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
                                text = "ИТОГИ ЗА ${dateFormat.format(Date(selectedDate)).uppercase()}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
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
                                        text = currencyFormat.format(dailyIncome),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = IncomeGreen
                                    )
                                }
                                
                                // Вертикальный разделитель
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
                                        text = currencyFormat.format(dailyExpenses),
                                        style = MaterialTheme.typography.titleLarge.copy(
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
            
            // ЗАГОЛОВОК СПИСКА
            if (transactionsForSelectedDate.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ОПЕРАЦИИ ЗА ${dateFormat.format(Date(selectedDate)).uppercase()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${transactionsForSelectedDate.size} операций",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            
            // СПИСОК ТРАНЗАКЦИЙ
            if (transactionsForSelectedDate.isNotEmpty()) {
                items(transactionsForSelectedDate) { transaction ->
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
                            text = "Нет операций за эту дату",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Выберите другую дату или добавьте новую операцию",
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
    
    // ДИАЛОГ ВЫБОРА ДАТЫ
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { timestamp ->
                selectedDate = timestamp
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            initialDate = selectedDate
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
                
                // Время операции
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.formattedTime(),
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

private fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}