package com.stvalentin.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.ui.*
import com.stvalentin.finance.widget.KeepAliveWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Запускаем фиктивный Worker при старте приложения
        startKeepAliveWorker()
        
        setContent {
            FinanceApp()
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
            .setInitialDelay(1, TimeUnit.HOURS) // Первый запуск через час
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "keep_alive_worker",
            ExistingPeriodicWorkPolicy.KEEP, // Не создавать новый, если уже есть
            workRequest
        )
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
            val viewModel: FinanceViewModel = viewModel(
                factory = FinanceViewModelFactory(database.transactionDao())
            )
            
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            Scaffold(
                bottomBar = {
                    if (currentRoute != "add_transaction/{transactionId}") {
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
                                viewModel = viewModel
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
                    }
                }
            }
        }
    }
}