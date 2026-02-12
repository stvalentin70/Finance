package com.stvalentin.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionCategories
import com.stvalentin.finance.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    navController: NavController,
    transactionId: Long? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Дата
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    
    val isEditing = transactionId != null && transactionId != 0L
    val transaction by viewModel.getTransactionById(transactionId ?: 0).collectAsState(initial = null)
    
    LaunchedEffect(transaction) {
        if (isEditing && transaction != null) {
            selectedType = transaction!!.type
            amountText = transaction!!.amount.toString()
            selectedCategory = transaction!!.category
            description = transaction!!.description
            selectedDate = transaction!!.date
        }
    }

    val categories = if (selectedType == TransactionType.INCOME) {
        TransactionCategories.incomeCategories
    } else {
        TransactionCategories.expenseCategories
    }

    LaunchedEffect(selectedType) {
        if (!isEditing) {
            selectedCategory = categories.firstOrNull() ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) {
                            if (selectedType == TransactionType.INCOME) "Редактировать доход" else "Редактировать расход"
                        } else {
                            if (selectedType == TransactionType.INCOME) "Добавить доход" else "Добавить расход"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen.copy(alpha = 0.1f) 
                    else 
                        ExpenseRed.copy(alpha = 0.1f),
                    titleContentColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen 
                    else 
                        ExpenseRed
                )
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
            // Переключатель типа транзакции
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { selectedType = TransactionType.EXPENSE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.EXPENSE) 
                                ExpenseRed 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == TransactionType.EXPENSE) 
                                Color.White 
                            else 
                                ExpenseRed
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = !isEditing
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Расход")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { selectedType = TransactionType.INCOME },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.INCOME) 
                                IncomeGreen 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == TransactionType.INCOME) 
                                Color.White 
                            else 
                                IncomeGreen
                        ),
                        modifier = Modifier.weight(1f),
                        enabled = !isEditing
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Доход")
                    }
                }
            }
            
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = showError && amountText.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Дата
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            contentDescription = "Дата",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Дата",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(selectedDate)),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    
                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Выбрать", fontSize = 12.sp)
                    }
                }
            }
            
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
            
            // Описание
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                placeholder = { Text("Продукты, транспорт, развлечения...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сохранения
            Button(
                onClick = {
                    if (amountText.isNotEmpty() && selectedCategory.isNotEmpty()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            if (isEditing && transaction != null) {
                                val updatedTransaction = transaction!!.copy(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amount,
                                    description = description,
                                    date = selectedDate
                                )
                                viewModel.updateTransaction(updatedTransaction)
                            } else {
                                viewModel.addTransaction(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amount,
                                    description = description,
                                    date = selectedDate
                                )
                            }
                            navController.navigateUp()
                        } else {
                            showError = true
                        }
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen 
                    else 
                        ExpenseRed
                )
            ) {
                Text(
                    text = if (isEditing) "Обновить" else "Сохранить",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (showError) {
                Text(
                    text = "Пожалуйста, введите сумму и выберите категорию",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
    
    // Диалог выбора даты
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
    
    // Диалог подтверждения удаления
    if (showDeleteDialog && transaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить транзакцию?") },
            text = { Text("Вы уверены, что хотите удалить эту транзакцию?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(transaction!!)
                        showDeleteDialog = false
                        navController.navigateUp()
                    }
                ) {
                    Text("Удалить", color = ExpenseRed)
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