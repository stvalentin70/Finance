package com.stvalentin.finance.ui

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
import com.stvalentin.finance.data.Saving
import com.stvalentin.finance.data.TransactionCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSavingScreen(
    navController: NavController,
    savingId: Long? = null,
    viewModel: FinanceViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("RUB") }
    var note by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var hasTarget by remember { mutableStateOf(false) }
    
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedCurrency by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val isEditing = savingId != null && savingId != 0L
    val saving by viewModel.getSavingById(savingId ?: 0).collectAsState(initial = null)
    
    val categories = TransactionCategories.savingCategories
    val currencies = listOf("RUB", "USD", "EUR", "KZT", "CNY")
    
    LaunchedEffect(saving) {
        if (isEditing && saving != null) {
            name = saving!!.name
            amountText = saving!!.amount.toString()
            selectedCategory = saving!!.category
            selectedCurrency = saving!!.currency
            note = saving!!.note
            if (saving!!.targetAmount != null) {
                hasTarget = true
                targetAmountText = saving!!.targetAmount.toString()
            }
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Редактировать" else "Новое накопление",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onTertiaryContainer
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
            // Название накопления
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                placeholder = { Text("Подушка безопасности, Отпуск, Машина...") },
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
                        text = selectedCurrency,
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
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
            
            // Валюта
            ExposedDropdownMenuBox(
                expanded = expandedCurrency,
                onExpandedChange = { expandedCurrency = it }
            ) {
                OutlinedTextField(
                    value = selectedCurrency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Валюта") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedCurrency,
                    onDismissRequest = { expandedCurrency = false }
                ) {
                    currencies.forEach { currency ->
                        DropdownMenuItem(
                            text = { Text(currency) },
                            onClick = {
                                selectedCurrency = currency
                                expandedCurrency = false
                            }
                        )
                    }
                }
            }
            
            // Цель (опционально)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Установить цель",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = hasTarget,
                            onCheckedChange = { hasTarget = it }
                        )
                    }
                    
                    if (hasTarget) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = targetAmountText,
                            onValueChange = {
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    targetAmountText = it
                                }
                            },
                            label = { Text("Целевая сумма") },
                            placeholder = { Text("0.00") },
                            trailingIcon = {
                                Text(
                                    text = selectedCurrency,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Заметка
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка сохранения
            Button(
                onClick = {
                    if (name.isNotEmpty() && amountText.isNotEmpty() && selectedCategory.isNotEmpty()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val targetAmount = if (hasTarget && targetAmountText.isNotEmpty()) {
                            targetAmountText.toDoubleOrNull()
                        } else {
                            null
                        }
                        
                        if (amount > 0) {
                            if (isEditing && saving != null) {
                                val updated = saving!!.copy(
                                    name = name,
                                    category = selectedCategory,
                                    amount = amount,
                                    currency = selectedCurrency,
                                    note = note,
                                    targetAmount = targetAmount,
                                    dateUpdated = System.currentTimeMillis()
                                )
                                viewModel.updateSaving(updated)
                            } else {
                                viewModel.addSaving(
                                    name = name,
                                    category = selectedCategory,
                                    amount = amount,
                                    currency = selectedCurrency,
                                    note = note,
                                    targetAmount = targetAmount
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
                    containerColor = MaterialTheme.colorScheme.tertiary
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
    
    // Диалог подтверждения удаления
    if (showDeleteDialog && saving != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить накопление?") },
            text = { Text("Вы уверены, что хотите удалить \"${saving!!.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSaving(saving!!)
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