package com.stvalentin.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember  // ДОБАВЛЕНО!
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

@Composable
fun BalanceChart(
    data: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Недостаточно данных для графика",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(vertical = 8.dp)
        ) {
            // Показываем первую и последнюю точку
            val firstPoint = data.firstOrNull()
            val lastPoint = data.lastOrNull()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Начало периода",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    firstPoint?.second?.let {
                        Text(
                            text = currencyFormat.format(it),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (it >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Конец периода",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    lastPoint?.second?.let {
                        Text(
                            text = currencyFormat.format(it),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (it >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Тренд
            if (firstPoint != null && lastPoint != null) {
                val change = lastPoint.second - firstPoint.second
                val changePercent = if (firstPoint.second != 0.0) {
                    (change / firstPoint.second * 100).toInt()
                } else {
                    0
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (change >= 0) 
                            IncomeGreen.copy(alpha = 0.1f) 
                        else 
                            ExpenseRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Изменение за период:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${if (change >= 0) "+" else ""}${currencyFormat.format(change)} ($changePercent%)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (change >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }
    }
}