package com.stvalentin.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceApp()
        }
    }
}

@Composable
fun FinanceApp() {
    FinanceTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val viewModel: FinanceViewModel = viewModel(
                factory = FinanceViewModelFactory(database.transactionDao())
            )
            
            val navController = rememberNavController()
            
            NavHost(
                navController = navController,
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen(
                        onAddTransactionClick = {
                            navController.navigate("add_transaction")
                        },
                        onTransactionClick = { transaction ->
                            // TODO: Редактирование транзакции
                        },
                        viewModel = viewModel
                    )
                }
                
                composable("add_transaction") {
                    AddTransactionScreen(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}