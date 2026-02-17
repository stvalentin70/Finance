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
import com.stvalentin.finance.data.RegularPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCalendarScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val payments by viewModel.regularPayments.collectAsState()
    
    // Состояние для диалога удаления
    var showDeleteDialog by remember { mutableStateOf(false) }
    var paymentToDelete by remember { mutableStateOf<RegularPayment?>(null) }
    
    // Группируем платежи по дням месяца
    val paymentsByDay = remember(payments) {
        payments
            .filter { it.isActive }
            .groupBy { it.dayOfMonth }
            .toSortedMap()
    }
    
    // Текущий месяц и год
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = remember { calendar.get(Calendar.MONTH) }
    val currentYear = remember { calendar.get(Calendar.YEAR) }
    val monthNames = arrayOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Платежный календарь",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("add_regular_payment/0") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить платеж"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Заголовок с месяцем и годом
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Text(
                        text = "Всего: ${payments.size} платежей",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            if (payments.isEmpty()) {
                // Пустой экран
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет регулярных платежей",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Добавьте регулярные платежи, чтобы не забывать об оплате",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    paymentsByDay.forEach { (day, dayPayments) ->
                        item {
                            PaymentDaySection(
                                day = day,
                                payments = dayPayments,
                                onPaymentClick = { payment ->
                                    navController.navigate("add_regular_payment/${payment.id}")
                                },
                                onPayNow = { payment ->
                                    viewModel.markPaymentAsPaid(payment)
                                },
                                onDeleteClick = { payment ->
                                    paymentToDelete = payment
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Диалог подтверждения удаления
    if (showDeleteDialog && paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                paymentToDelete = null
            },
            title = {
                Text(text = "Удалить платеж?")
            },
            text = {
                Text(text = "Вы уверены, что хотите удалить регулярный платеж \"${paymentToDelete?.name}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        paymentToDelete?.let { viewModel.deleteRegularPayment(it) }
                        showDeleteDialog = false
                        paymentToDelete = null
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        paymentToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun PaymentDaySection(
    day: Int,
    payments: List<RegularPayment>,
    onPaymentClick: (RegularPayment) -> Unit,
    onPayNow: (RegularPayment) -> Unit,
    onDeleteClick: (RegularPayment) -> Unit
) {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val isToday = day == today
    val isPast = day < today
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                isPast -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Заголовок дня
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else if (isPast) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isToday -> "• Сегодня"
                            isPast -> "• Просрочено"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                        else if (isPast) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${payments.size} ${getPaymentPlural(payments.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Список платежей за день
            payments.forEach { payment ->
                PaymentItem(
                    payment = payment,
                    onClick = { onPaymentClick(payment) },
                    onPayNow = { onPayNow(payment) },
                    onDeleteClick = { onDeleteClick(payment) }
                )
            }
        }
    }
}

@Composable
fun PaymentItem(
    payment: RegularPayment,
    onClick: () -> Unit,
    onPayNow: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currencyFormat = remember { 
        NumberFormat.getCurrencyInstance(Locale("ru", "RU")) 
    }
    val isPaid = payment.isPaidThisMonth()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = if (isPaid) Color.Gray else ExpenseRed,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = payment.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isPaid) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = payment.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaid) Color.Gray.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                )
            }
            
            // Сумма и кнопки
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormat.format(payment.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isPaid) Color.Gray else ExpenseRed
                )
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Кнопка оплаты
                    if (isPaid) {
                        // Уже оплачено - серая неактивная кнопка с галочкой
                        Button(
                            onClick = { /* Ничего не делаем */ },
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(90.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White
                            ),
                            enabled = false
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Оплачено", fontSize = 10.sp)
                        }
                    } else {
                        // Не оплачено - зеленая активная кнопка с рублем
                        Button(
                            onClick = onPayNow,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .width(80.dp)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IncomeGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Оплатить", fontSize = 10.sp)
                        }
                    }
                    
                    // Кнопка удаления
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getPaymentPlural(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "платеж"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "платежа"
        else -> "платежей"
    }
}