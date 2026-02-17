package com.stvalentin.finance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stvalentin.finance.data.RegularPayment
import com.stvalentin.finance.data.TransactionCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRegularPaymentScreen(
    navController: NavController,
    paymentId: Long? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var dayOfMonth by remember { mutableStateOf(1) }
    var reminderDays by remember { mutableStateOf(1) }
    var description by remember { mutableStateOf("") }
    
    var showDayPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    val isEditing = paymentId != null && paymentId != 0L
    val payment by viewModel.getRegularPaymentById(paymentId ?: 0).collectAsState(initial = null)
    
    val categories = TransactionCategories.expenseCategories
    
    LaunchedEffect(payment) {
        if (isEditing && payment != null) {
            name = payment!!.name
            amountText = payment!!.amount.toString()
            selectedCategory = payment!!.category
            dayOfMonth = payment!!.dayOfMonth
            reminderDays = payment!!.reminderDays
            description = payment!!.description
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Редактировать платеж" else "Новый платеж",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить"
                            )
                        }
                    } else {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Название платежа
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название платежа") },
                placeholder = { Text("Квартплата, Интернет, Кредит...") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Сумма
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amountText = it
                    }
                },
                label = { Text("Сумма") },
                placeholder = { Text("0.00") },
                trailingIcon = {
                    Text(
                        text = "₽",
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Категория
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // День месяца
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDayPicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "День месяца",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "$dayOfMonth число",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Изменить",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Напоминание
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Напоминание",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("За сколько дней напоминать?")
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (reminderDays > 0) reminderDays-- },
                                enabled = reminderDays > 0
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Уменьшить"
                                )
                            }
                            
                            Text(
                                text = "$reminderDays",
                                modifier = Modifier.width(40.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            
                            IconButton(
                                onClick = { reminderDays++ },
                                enabled = reminderDays < 30
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Увеличить"
                                )
                            }
                        }
                    }
                }
            }
            
            // Описание
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сохранения
            Button(
                onClick = {
                    if (name.isNotEmpty() && amountText.isNotEmpty() && selectedCategory.isNotEmpty()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (isEditing && payment != null) {
                                val updated = payment!!.copy(
                                    name = name,
                                    category = selectedCategory,
                                    amount = amount,
                                    dayOfMonth = dayOfMonth,
                                    reminderDays = reminderDays,
                                    description = description
                                )
                                viewModel.updateRegularPayment(updated)
                            } else {
                                viewModel.addRegularPayment(
                                    name = name,
                                    category = selectedCategory,
                                    amount = amount,
                                    dayOfMonth = dayOfMonth,
                                    reminderDays = reminderDays,
                                    description = description
                                )
                            }
                            navController.navigateUp()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = if (isEditing) "Обновить" else "Сохранить",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Диалог выбора дня
    if (showDayPicker) {
        DayPickerDialog(
            currentDay = dayOfMonth,
            onDaySelected = { day ->
                dayOfMonth = day
                showDayPicker = false
            },
            onDismiss = { showDayPicker = false }
        )
    }
    
    // Диалог удаления
    if (showDeleteDialog && payment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить платеж?") },
            text = { Text("Вы уверены, что хотите удалить этот регулярный платеж?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRegularPayment(payment!!)
                        showDeleteDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun DayPickerDialog(
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDay by remember { mutableStateOf(currentDay) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите день месяца") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Обычно вы платите этого числа")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedDay > 1) selectedDay-- }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Уменьшить")
                    }
                    
                    Text(
                        text = "$selectedDay",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(80.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = { if (selectedDay < 31) selectedDay++ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Увеличить")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDaySelected(selectedDay) }) {
                Text("Выбрать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}