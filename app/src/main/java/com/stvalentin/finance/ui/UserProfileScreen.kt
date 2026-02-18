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
import com.stvalentin.finance.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Локальные состояния
    var isStudent by remember { mutableStateOf(userProfile?.isStudent ?: false) }
    var isWorker by remember { mutableStateOf(userProfile?.isWorker ?: false) }
    var isEntrepreneur by remember { mutableStateOf(userProfile?.isEntrepreneur ?: false) }
    var isRetiree by remember { mutableStateOf(userProfile?.isRetiree ?: false) }
    var isInvestor by remember { mutableStateOf(userProfile?.isInvestor ?: false) }
    var isHousewife by remember { mutableStateOf(userProfile?.isHousewife ?: false) }
    var isUnemployed by remember { mutableStateOf(userProfile?.isUnemployed ?: false) }
    
    var age by remember { mutableStateOf(userProfile?.age?.toString() ?: "") }
    var hasChildren by remember { mutableStateOf(userProfile?.hasChildren ?: false) }
    var dependents by remember { mutableStateOf(userProfile?.dependents ?: 0) }
    var hasMortgage by remember { mutableStateOf(userProfile?.hasMortgage ?: false) }
    var hasRent by remember { mutableStateOf(userProfile?.hasRent ?: false) }
    var housingPayment by remember { mutableStateOf(userProfile?.housingPayment?.toString() ?: "") }
    var hasCar by remember { mutableStateOf(userProfile?.hasCar ?: false) }
    var hasCarLoan by remember { mutableStateOf(userProfile?.hasCarLoan ?: false) }
    var carPayment by remember { mutableStateOf(userProfile?.carPayment?.toString() ?: "") }
    var mainIncomeDay by remember { mutableStateOf(userProfile?.mainIncomeDay?.toString() ?: "5") }
    
    // Обновляем локальные состояния при изменении профиля
    LaunchedEffect(userProfile) {
        userProfile?.let {
            isStudent = it.isStudent
            isWorker = it.isWorker
            isEntrepreneur = it.isEntrepreneur
            isRetiree = it.isRetiree
            isInvestor = it.isInvestor
            isHousewife = it.isHousewife
            isUnemployed = it.isUnemployed
            age = it.age?.toString() ?: ""
            hasChildren = it.hasChildren
            dependents = it.dependents
            hasMortgage = it.hasMortgage
            hasRent = it.hasRent
            housingPayment = it.housingPayment.takeIf { it > 0 }?.toString() ?: ""
            hasCar = it.hasCar
            hasCarLoan = it.hasCarLoan
            carPayment = it.carPayment.takeIf { it > 0 }?.toString() ?: ""
            mainIncomeDay = it.mainIncomeDay.toString()
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Мой профиль",
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
            // СТАТУСЫ
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
                            text = "🎭 ВАШИ СТАТУСЫ (можно выбрать несколько)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        StatusCheckbox(
                            emoji = "🎓",
                            text = "Студент",
                            checked = isStudent,
                            onCheckedChange = { isStudent = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "💼",
                            text = "Работник",
                            checked = isWorker,
                            onCheckedChange = { isWorker = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "🏭",
                            text = "Предприниматель",
                            checked = isEntrepreneur,
                            onCheckedChange = { isEntrepreneur = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "👴",
                            text = "Пенсионер",
                            checked = isRetiree,
                            onCheckedChange = { isRetiree = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "📈",
                            text = "Инвестор",
                            checked = isInvestor,
                            onCheckedChange = { isInvestor = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "🏠",
                            text = "Домохозяйка/ин",
                            checked = isHousewife,
                            onCheckedChange = { isHousewife = it }
                        )
                        
                        StatusCheckbox(
                            emoji = "🕊️",
                            text = "Безработный",
                            checked = isUnemployed,
                            onCheckedChange = { isUnemployed = it }
                        )
                    }
                }
            }
            
            // ОСНОВНАЯ ИНФОРМАЦИЯ
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
                            text = "👤 ОСНОВНАЯ ИНФОРМАЦИЯ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = age,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                                    age = newValue
                                }
                            },
                            label = { Text("Возраст") },
                            placeholder = { Text("25") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = mainIncomeDay,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                                    mainIncomeDay = newValue
                                }
                            },
                            label = { Text("День основного дохода") },
                            placeholder = { Text("5") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
            
            // СЕМЬЯ
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
                            text = "👨‍👩‍👧 СЕМЬЯ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Есть дети",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = hasChildren,
                                onCheckedChange = { hasChildren = it }
                            )
                        }
                        
                        if (hasChildren) {
                            NumberPicker(
                                value = dependents,
                                onValueChange = { dependents = it },
                                label = "Количество детей"
                            )
                        }
                    }
                }
            }
            
            // ЖИЛЬЕ
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
                            text = "🏠 ЖИЛЬЕ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ипотека",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = hasMortgage,
                                onCheckedChange = { hasMortgage = it }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Аренда",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = hasRent,
                                onCheckedChange = { hasRent = it }
                            )
                        }
                        
                        if (hasMortgage || hasRent) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = housingPayment,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                        housingPayment = newValue
                                    }
                                },
                                label = { Text("Ежемесячный платеж") },
                                placeholder = { Text("25000") },
                                trailingIcon = { Text("₽") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
            
            // ТРАНСПОРТ
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
                            text = "🚗 ТРАНСПОРТ",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Есть автомобиль",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Checkbox(
                                checked = hasCar,
                                onCheckedChange = { hasCar = it }
                            )
                        }
                        
                        if (hasCar) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Кредит на авто",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Checkbox(
                                    checked = hasCarLoan,
                                    onCheckedChange = { hasCarLoan = it }
                                )
                            }
                            
                            if (hasCarLoan) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = carPayment,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                            carPayment = newValue
                                        }
                                    },
                                    label = { Text("Ежемесячный платеж") },
                                    placeholder = { Text("15000") },
                                    trailingIcon = { Text("₽") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
            
            // КНОПКА СОХРАНЕНИЯ
            item {
                Button(
                    onClick = {
                        val updatedProfile = (userProfile ?: UserProfile()).copy(
                            isStudent = isStudent,
                            isWorker = isWorker,
                            isEntrepreneur = isEntrepreneur,
                            isRetiree = isRetiree,
                            isInvestor = isInvestor,
                            isHousewife = isHousewife,
                            isUnemployed = isUnemployed,
                            age = if (age.isNotEmpty()) age.toInt() else null,
                            hasChildren = hasChildren,
                            dependents = dependents,
                            hasMortgage = hasMortgage,
                            hasRent = hasRent,
                            housingPayment = if (housingPayment.isNotEmpty()) housingPayment.toDouble() else 0.0,
                            hasCar = hasCar,
                            hasCarLoan = hasCarLoan,
                            carPayment = if (carPayment.isNotEmpty()) carPayment.toDouble() else 0.0,
                            mainIncomeDay = if (mainIncomeDay.isNotEmpty()) mainIncomeDay.toInt() else 5,
                            lastUpdated = System.currentTimeMillis()
                        )
                        viewModel.updateUserProfile(updatedProfile)
                        navController.navigateUp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Сохранить",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusCheckbox(
    emoji: String,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                enabled = value > 0
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Уменьшить")
            }
            
            Text(
                text = "$value",
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            IconButton(
                onClick = { onValueChange(value + 1) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Увеличить")
            }
        }
    }
}