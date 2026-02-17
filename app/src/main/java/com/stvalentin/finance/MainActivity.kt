package com.stvalentin.finance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.ui.*
import com.stvalentin.finance.widget.KeepAliveWorker
import com.stvalentin.finance.workers.PaymentReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "✓ Разрешение на уведомления ПОЛУЧЕНО")
        } else {
            Log.d("MainActivity", "✗ Разрешение на уведомления ОТКЛОНЕНО")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "onCreate: запуск приложения")
        
        // Запрашиваем разрешение на уведомления для Android 13+
        requestNotificationPermission()
        
        // Запускаем фиктивный Worker при старте приложения
        startKeepAliveWorker()
        
        // Принудительно запускаем Worker для уведомлений
        startPaymentReminderWorker()
        
        setContent {
            FinanceApp()
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("MainActivity", "Android 13+, проверяем разрешение на уведомления")
            
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MainActivity", "Разрешение НЕ предоставлено, запрашиваем...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("MainActivity", "Разрешение УЖЕ предоставлено")
            }
        } else {
            Log.d("MainActivity", "Android < 13, разрешение на уведомления не требуется")
        }
    }
    
    private fun startKeepAliveWorker() {
        val workRequest = PeriodicWorkRequestBuilder<KeepAliveWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .setRequiresCharging(false)
                    .build()
            )
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "keep_alive_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun startPaymentReminderWorker() {
        val workManager = WorkManager.getInstance(this)
        
        // Отменяем старые Worker'ы
        workManager.cancelUniqueWork("payment_reminders")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        // Запускаем каждые 15 минут для теста
        val request = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
         .setInitialDelay(1, TimeUnit.MINUTES)
         .build()
        
        workManager.enqueueUniquePeriodicWork(
            "payment_reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        
        Log.d("MainActivity", "PaymentReminderWorker запущен (интервал 15 минут)")
    }
}

@Composable
fun FinanceApp() {
    FinanceTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            
            // Создаем ViewModel с тремя DAO и context
            val viewModel: FinanceViewModel = viewModel(
                factory = FinanceViewModelFactory(
                    database.transactionDao(),
                    database.regularPaymentDao(),
                    database.savingDao(),
                    context
                )
            )
            
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    if (currentRoute != "add_transaction/{transactionId}" &&
                        currentRoute != "add_regular_payment/{paymentId}" &&
                        currentRoute != "add_saving/{savingId}") {
                        BottomNavigationBar(navController = navController)
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                onAddTransactionClick = {
                                    navController.navigate("add_transaction/0")
                                },
                                onTransactionClick = { transaction ->
                                    navController.navigate("add_transaction/${transaction.id}")
                                },
                                viewModel = viewModel,
                                navController = navController
                            )
                        }
                        
                        composable("add_transaction/{transactionId}") { backStackEntry ->
                            val transactionId = backStackEntry.arguments?.getString("transactionId")?.toLongOrNull()
                            AddEditTransactionScreen(
                                navController = navController,
                                transactionId = transactionId,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("statistics") {
                            StatisticsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("history") {
                            HistoryScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("payment_calendar") {
                            PaymentCalendarScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("add_regular_payment/{paymentId}") { backStackEntry ->
                            val paymentId = backStackEntry.arguments?.getString("paymentId")?.toLongOrNull()
                            AddEditRegularPaymentScreen(
                                navController = navController,
                                paymentId = paymentId,
                                viewModel = viewModel
                            )
                        }
                        
                        // НОВЫЕ МАРШРУТЫ ДЛЯ КОПИЛКИ
                        composable("savings") {
                            SavingsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable("add_saving/{savingId}") { backStackEntry ->
                            val savingId = backStackEntry.arguments?.getString("savingId")?.toLongOrNull()
                            AddEditSavingScreen(
                                navController = navController,
                                savingId = savingId,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}