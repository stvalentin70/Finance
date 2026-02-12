package com.stvalentin.finance.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.stvalentin.finance.R
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Long
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(it)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = {
            Text("Выберите дату")
        },
        text = {
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false
            )
        }
    )
}