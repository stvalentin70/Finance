package com.stvalentin.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stvalentin.finance.data.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    viewModel: FinanceViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val income by viewModel.totalIncome.collectAsState()
    val expenses by viewModel.totalExpenses.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Финансы",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить транзакцию")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Карточка с балансом
            BalanceCard(
                balance = balance,
                income = income,
                expenses = expenses,
                modifier = Modifier.padding(16.dp)
            )
            
            // Заголовок списка транзакций
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Последние транзакции",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                if (transactions.isNotEmpty()) {
                    Text(
                        text = "${transactions.size} транзакций",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            // Список транзакций
            if (transactions.isEmpty()) {
                // Пустой экран
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Нет транзакций",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Нажмите + чтобы добавить первую транзакцию",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onTransactionClick = onTransactionClick,
                            onDeleteClick = {
                                transactionToDelete = it
                                showDeleteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Диалог подтверждения удаления
        if (showDeleteDialog && transactionToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    transactionToDelete = null
                },
                title = {
                    Text(text = "Удалить транзакцию?")
                },
                text = {
                    Text(text = "Вы уверены, что хотите удалить эту транзакцию? Это действие нельзя отменить.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            transactionToDelete?.let { viewModel.deleteTransaction(it) }
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            transactionToDelete = null
                        }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}