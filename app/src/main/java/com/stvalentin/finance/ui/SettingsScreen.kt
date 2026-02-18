package com.stvalentin.finance.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.stvalentin.finance.data.Transaction
import com.stvalentin.finance.data.TransactionType
import kotlinx.coroutines.launch
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    var messageType by remember { mutableStateOf(MessageType.SUCCESS) }
    
    val transactions by viewModel.allTransactions.collectAsState()
    
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"
    
    // Launcher для создания файла (экспорт)
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = exportTransactionsToCsv(context, transactions, uri)
                messageText = if (success) {
                    "Данные успешно экспортированы"
                } else {
                    "Ошибка при экспорте данных"
                }
                messageType = if (success) MessageType.SUCCESS else MessageType.ERROR
                showMessage = true
            }
        }
    }
    
    // Launcher для выбора файла (импорт) - ИСПРАВЛЕНО с OpenDocument
    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                // Сохраняем права на чтение URI
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                // Импортируем
                val success = importTransactionsFromCsv(context, viewModel, uri)
                
                messageText = if (success) {
                    "Данные успешно импортированы"
                } else {
                    "Файл имеет неверный формат или не содержит данных"
                }
                messageType = if (success) MessageType.SUCCESS else MessageType.ERROR
                showMessage = true
            } catch (e: SecurityException) {
                e.printStackTrace()
                messageText = "Нет прав доступа к файлу"
                messageType = MessageType.ERROR
                showMessage = true
            } catch (e: Exception) {
                e.printStackTrace()
                messageText = "Ошибка импорта: ${e.message}"
                messageType = MessageType.ERROR
                showMessage = true
            }
        }
    }
    
    // Launcher для разрешений (Android 10 и ниже)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "*/*"))
        } else {
            messageText = "Необходимо разрешение на чтение файлов"
            messageType = MessageType.ERROR
            showMessage = true
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Настройки",
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
            // ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ
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
                            text = "👤 ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ",
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
                        
                        // Редактировать профиль
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    navController.navigate("user_profile")
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Мой профиль",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Статус, семья, жилье",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // АНАЛИЗ ДОХОДОВ
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
                            text = "📈 АНАЛИЗ ДОХОДОВ",
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
                        
                        // Анализ доходов
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    navController.navigate("income_analysis")
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Анализ доходов",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Статистика и прогнозы",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // УПРАВЛЕНИЕ ДАННЫМИ
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
                            text = "📊 УПРАВЛЕНИЕ ДАННЫМИ",
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
                        
                        // ЭКСПОРТ ДАННЫХ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (transactions.isNotEmpty()) {
                                        val defaultFileName = "Finance_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale("ru")).format(Date())}.csv"
                                        createFileLauncher.launch(defaultFileName)
                                    } else {
                                        messageText = "Нет данных для экспорта"
                                        messageType = MessageType.ERROR
                                        showMessage = true
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Экспорт данных",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Сохранить транзакции в CSV файл",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // ИМПОРТ ДАННЫХ
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                        // Android 10 и ниже - запрашиваем разрешение
                                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                    } else {
                                        // Android 11+ - открываем документ-пикер
                                        selectFileLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "*/*"))
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Импорт данных",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Загрузить транзакции из CSV",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Удалить все транзакции
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (transactions.isNotEmpty()) {
                                        showDeleteAllDialog = true 
                                    } else {
                                        messageText = "Нет транзакций для удаления"
                                        messageType = MessageType.ERROR
                                        showMessage = true
                                    }
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Удалить все транзакции",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Очистить историю операций",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // О ПРИЛОЖЕНИИ
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
                            text = "ℹ️ О ПРИЛОЖЕНИИ",
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Версия",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = versionName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Разработчик",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Валентин Тихонов",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Год",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "2026",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Трекер финансов — простое приложение для учёта личных доходов и расходов.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            // Количество транзакций
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Всего транзакций:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${transactions.size}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
    
    // Диалог подтверждения удаления всех транзакций
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Удалить всё?") },
            text = { 
                Column {
                    Text("Вы уверены, что хотите удалить ВСЕ транзакции?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Всего транзакций: ${transactions.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                    Text("Это действие нельзя отменить.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllTransactions()
                        showDeleteAllDialog = false
                        messageText = "Все транзакции удалены"
                        messageType = MessageType.SUCCESS
                        showMessage = true
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Сообщение
    if (showMessage) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { showMessage = false }) {
                    Text("OK")
                }
            }
        ) {
            Text(
                text = messageText,
                color = if (messageType == MessageType.SUCCESS) 
                    IncomeGreen 
                else 
                    ExpenseRed
            )
        }
    }
}

enum class MessageType {
    SUCCESS, ERROR
}

// Функции экспорта/импорта с URI
fun exportTransactionsToCsv(context: Context, transactions: List<Transaction>, uri: Uri): Boolean {
    return try {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Заголовки CSV
                writer.append("ID,Тип,Категория,Сумма,Описание,Дата\n")
                
                // Данные
                transactions.forEach { t ->
                    writer.append("${t.id},")
                    writer.append("${if (t.type == TransactionType.INCOME) "Доход" else "Расход"},")
                    writer.append("${t.category},")
                    writer.append("${t.amount},")
                    writer.append("\"${t.description.replace("\"", "\"\"")}\",")
                    writer.append("${dateFormat.format(Date(t.date))}\n")
                }
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun importTransactionsFromCsv(context: Context, viewModel: FinanceViewModel, uri: Uri): Boolean {
    return try {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
        var importedCount = 0
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                var isFirstLine = true
                
                while (reader.readLine().also { line = it } != null) {
                    if (isFirstLine) {
                        isFirstLine = false
                        continue
                    }
                    
                    val currentLine = line
                    if (currentLine.isNullOrBlank()) continue
                    
                    val parts = parseCsvLine(currentLine)
                    if (parts.size >= 6) {
                        val type = if (parts[1] == "Доход") TransactionType.INCOME else TransactionType.EXPENSE
                        val category = parts[2]
                        val amount = parts[3].toDoubleOrNull() ?: continue
                        val description = parts[4]
                        val dateString = parts[5]
                        
                        val date = try {
                            dateFormat.parse(dateString)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        
                        viewModel.addTransaction(
                            type = type,
                            category = category,
                            amount = amount,
                            description = description,
                            date = date
                        )
                        importedCount++
                    }
                }
            }
        }
        importedCount > 0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Вспомогательная функция для парсинга CSV строки с учетом кавычек
fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    
    for (char in line) {
        when {
            char == '"' -> {
                inQuotes = !inQuotes
            }
            char == ',' && !inQuotes -> {
                result.add(current.toString())
                current.clear()
            }
            else -> {
                current.append(char)
            }
        }
    }
    result.add(current.toString())
    return result
}