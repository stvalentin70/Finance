package com.stvalentin.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
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
            val viewModel: FinanceViewModel = viewModel(
                factory = FinanceViewModelFactory(
                    AppDatabase.getDatabase(context).transactionDao()
                )
            )
            
            MainScreen(
                onAddTransactionClick = {
                    // TODO: Добавить экран добавления транзакции
                },
                onTransactionClick = { transaction ->
                    // TODO: Добавить экран редактирования транзакции
                },
                viewModel = viewModel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinanceAppPreview() {
    FinanceTheme {
        FinanceApp()
    }
}