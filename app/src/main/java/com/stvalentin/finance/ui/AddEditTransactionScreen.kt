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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    navController: NavController,
    transactionId: Long? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    // Состояния формы
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Загружаем транзакцию для редактирования
    val isEditing = transactionId != null && transactionId != 0L
    val transaction by viewModel.getTransactionById(transactionId ?: 0).collectAsState(initial = null)
    
    // Заполняем форму при редактировании
    LaunchedEffect(transaction) {
        if (isEditing && transaction != null) {
            selectedType = transaction!!.type
            amountText = transaction!!.amount.toString()
            selectedCategory = transaction!!.category
            description = transaction!!.description
        }
    }

    val categories = if (selectedType == TransactionType.INCOME) {
        TransactionCategories.incomeCategories
    } else {
        TransactionCategories.expenseCategories
    }

    // Устанавливаем категорию по умолчанию для нового
    LaunchedEffect(selectedType) {
        if (!isEditing) {
            selectedCategory = categories.firstOrNull() ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen.copy(alpha = 0.1f) 
                    else 
                        ExpenseRed.copy(alpha = 0.1f),
                    titleContentColor = if (selectedType == TransactionType.INCOME) 
                        IncomeGreen 
                    else 
                        ExpenseRed
                ),
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = ExpenseRed
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
                    // Кнопка "Расход"
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
                        enabled = !isEditing // Нельзя менять тип при редактировании
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
                    
                    // Кнопка "Доход"
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
                        enabled = !isEditing // Нельзя менять тип при редактировании
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
            
            // Поле ввода суммы
            OutlinedTextField(
                value = amountText,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amountText = it
                    }
                },
                label = { Text("Сумма") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null
                    )
                },
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
            
            // Выпадающий список категорий
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
            
            // Поле описания (необязательное)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                placeholder = { Text("Например: Продукты в Пятерочке") },
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
                                // Обновляем существующую транзакцию
                                val updatedTransaction = transaction!!.copy(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amount,
                                    description = description
                                )
                                viewModel.updateTransaction(updatedTransaction)
                            } else {
                                // Добавляем новую транзакцию
                                viewModel.addTransaction(
                                    type = selectedType,
                                    category = selectedCategory,
                                    amount = amount,
                                    description = description
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