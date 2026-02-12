package com.stvalentin.finance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    onTransactionClick: (Transaction) -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTransactionClick(transaction) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Левая часть: иконка категории и информация
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Иконка категории
                Icon(
                    imageVector = getCategoryIcon(transaction.category, transaction.type),
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.INCOME) 
                        IncomeGreen 
                    else 
                        ExpenseRed,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    // Категория и тип
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Бейдж типа транзакции
                        Surface(
                            color = if (transaction.type == TransactionType.INCOME) 
                                IncomeGreen.copy(alpha = 0.2f) 
                            else 
                                ExpenseRed.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (transaction.type == TransactionType.INCOME) "Доход" else "Расход",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (transaction.type == TransactionType.INCOME) 
                                    IncomeGreen 
                                else 
                                    ExpenseRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Описание (если есть)
                    if (transaction.description.isNotBlank()) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                    
                    // Дата и время
                    Text(
                        text = "${transaction.formattedDate()} • ${transaction.formattedTime()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp
                    )
                }
            }
            
            // Правая часть: сумма и кнопка удаления
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${String.format("%,.2f", transaction.amount)} ₽",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (transaction.type == TransactionType.INCOME) 
                            IncomeGreen 
                        else 
                            ExpenseRed
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { onDeleteClick(transaction) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Функция для получения иконки по категории
fun getCategoryIcon(category: String, type: TransactionType): ImageVector {
    return when (category) {
        // Доходы
        "Зарплата" -> Icons.Default.Work
        "Фриланс" -> Icons.Default.Computer
        "Инвестиции" -> Icons.Default.TrendingUp
        "Подарок" -> Icons.Default.CardGiftcard
        "Возврат долга" -> Icons.Default.SwapHoriz
        "Другое" -> if (type == TransactionType.INCOME) Icons.Default.AttachMoney else Icons.Default.MoreHoriz
        
        // Расходы
        "Продукты" -> Icons.Default.ShoppingCart
        "Транспорт" -> Icons.Default.DirectionsBus
        "Жилье" -> Icons.Default.Home
        "Развлечения" -> Icons.Default.Movie
        "Здоровье" -> Icons.Default.LocalHospital
        "Одежда" -> Icons.Default.ShoppingBag
        "Образование" -> Icons.Default.School
        "Рестораны" -> Icons.Default.Restaurant
        "Кафе" -> Icons.Default.FreeBreakfast
        else -> if (type == TransactionType.INCOME) Icons.Default.AttachMoney else Icons.Default.ShoppingCart
    }
}