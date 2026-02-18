package com.stvalentin.finance.ui

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeAnalysisScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val averageMonthlyIncome by viewModel.averageMonthlyIncome.collectAsState()
    val mainIncomeSource by viewModel.mainIncomeSource.collectAsState()
    val typicalIncomeDay by viewModel.typicalIncomeDay.collectAsState()
    val incomeStability by viewModel.incomeStability.collectAsState()
    val daysToNextIncome by viewModel.daysToNextIncome.collectAsState()
    val nextIncomeDate by viewModel.nextIncomeDate.collectAsState()
    val incomeDays by viewModel.incomeDays.collectAsState()
    val periodIncome by viewModel.periodIncome.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("ru", "RU")) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Анализ доходов",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
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
            // ОСНОВНОЙ ДОХОД
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
                            text = "СРЕДНИЙ ДОХОД В МЕСЯЦ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currencyFormat.format(averageMonthlyIncome),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IncomeGreen
                        )
                        
                        if (periodIncome > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "В этом месяце: ${currencyFormat.format(periodIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            // ИСТОЧНИК ДОХОДА
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
                            text = "📊 ОСНОВНОЙ ИСТОЧНИК",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mainIncomeSource,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = IncomeGreen
                            )
                            
                            when (mainIncomeSource) {
                                "Зарплата" -> Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = IncomeGreen
                                )
                                "Пенсия" -> Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = IncomeGreen
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = IncomeGreen
                                )
                            }
                        }
                    }
                }
            }
            
            // ДЕНЬ ДОХОДА
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
                            text = "📅 ДЕНЬ ДОХОДА",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$typicalIncomeDay число",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = IncomeGreen
                            )
                            
                            if (daysToNextIncome in 0..30) {
                                Text(
                                    text = "через $daysToNextIncome дн.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (daysToNextIncome <= 5) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (nextIncomeDate != null) {
                            Text(
                                text = "Следующий доход: ${dateFormat.format(Date(nextIncomeDate!!))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // СТАБИЛЬНОСТЬ ДОХОДА
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
                            text = "📈 СТАБИЛЬНОСТЬ ДОХОДА",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val stabilityPercent = (incomeStability * 100).toInt()
                        val stabilityColor = when {
                            stabilityPercent >= 80 -> IncomeGreen
                            stabilityPercent >= 50 -> IncomeGreen.copy(alpha = 0.7f)
                            else -> ExpenseRed
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Стабильность:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$stabilityPercent%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = stabilityColor
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = { incomeStability.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(top = 4.dp),
                            color = stabilityColor,
                            trackColor = stabilityColor.copy(alpha = 0.2f)
                        )
                        
                        Text(
                            text = when {
                                stabilityPercent >= 80 -> "Доход стабильный, можно планировать бюджет"
                                stabilityPercent >= 50 -> "Доход умеренно стабильный, имейте резерв"
                                else -> "Доход нестабильный, рекомендуем увеличить подушку"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // ИСТОРИЯ ДОХОДОВ (без FlowRow, просто список)
            if (incomeDays.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "📋 ДНИ ДОХОДОВ",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Divider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Просто список в виде строки
                            Text(
                                text = incomeDays.distinct().sorted().joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "Наиболее частый день: $typicalIncomeDay число",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IncomeGreen,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // РЕКОМЕНДАЦИИ
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
                            text = generateIncomeAdvice(
                                mainIncomeSource, 
                                typicalIncomeDay, 
                                daysToNextIncome, 
                                averageMonthlyIncome, 
                                incomeStability
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

fun generateIncomeAdvice(
    source: String,
    day: Int,
    daysToNext: Int,
    avgIncome: Double,
    stability: Double
): String {
    return when {
        daysToNext <= 3 -> {
            "⏰ До $source осталось $daysToNext дн. Не забудьте спланировать бюджет!"
        }
        stability < 0.5 -> {
            "📊 Доход нестабильный. Рекомендуем увеличить подушку безопасности до 6 месяцев расходов"
        }
        avgIncome > 0 -> {
            val recommendedSave = (avgIncome * 0.1).toInt()
            "💰 Рекомендуем откладывать $recommendedSave ₽ с каждого дохода (10%)"
        }
        else -> {
            "💡 Добавьте доходы в историю, чтобы получить персональные рекомендации"
        }
    }
}