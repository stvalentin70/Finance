package com.stvalentin.finance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.navigation.NavController
import com.stvalentin.finance.data.RegularPayment
import com.stvalentin.finance.data.Saving
import com.stvalentin.finance.data.Transaction
import java.text.NumberFormat
import java.util.*

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
    val advice by viewModel.adviceMessage.collectAsState()
    val payments by viewModel.regularPayments.collectAsState()
    val allSavings by viewModel.allSavings.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val savingsByCurrency by viewModel.savingsByCurrency.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    currencyFormat.maximumFractionDigits = 0
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Плюс слева от названия
                        IconButton(
                            onClick = onAddTransactionClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить транзакцию",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "Трекер финансов",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
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
            
            // 2. НАКОПЛЕНИЯ (УЛУЧШЕННЫЙ БЛОК)
            item {
                SavingsOverviewCard(
                    totalSavings = totalSavings,
                    savingsByCurrency = savingsByCurrency,
                    recentSavings = allSavings.take(2),
                    onCardClick = { navController.navigate("savings") },
                    onAddClick = { navController.navigate("add_saving/0") },
                    currencyFormat = currencyFormat
                )
            }
            
            // 3. БЛИЖАЙШИЕ ПЛАТЕЖИ (из календаря)
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
            
            // 4. СОВЕТ ДНЯ
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsOverviewCard(
    totalSavings: Double,
    savingsByCurrency: Map<String, Double>,
    recentSavings: List<Saving>,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок с кнопкой добавления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "НАКОПЛЕНИЯ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Добавить накопление",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Общая сумма
            Text(
                text = "Всего: ${currencyFormat.format(totalSavings)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Распределение по валютам (заменяем FlowRow на обычный Row с Wrap)
            if (savingsByCurrency.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savingsByCurrency.entries.take(3).forEach { (currency, amount) ->
                        AssistChip(
                            onClick = { /* Показать детали по валюте */ },
                            label = {
                                Text("$currency: ${currencyFormat.format(amount)}")
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
            
            // Последние накопления
            if (recentSavings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Последние:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                
                recentSavings.forEach { saving ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = saving.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = currencyFormat.format(saving.amount),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            // Кнопка перехода
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Подробнее →",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
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