package com.stvalentin.finance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    initialDateTime: Long
) {
    val context = LocalContext.current
    
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = initialDateTime } }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale("ru")) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale("ru")) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateTimeSelected(selectedDate.time)
                    onDismiss()
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = {
            Text(
                text = "Выберите дату и время",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ТЕКУЩИЙ ВЫБОР",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = dateFormatter.format(selectedDate),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontSize = 16.sp
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = timeFormatter.format(selectedDate),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Выбрать дату",
                            fontSize = 13.sp  // ← УМЕНЬШЕНО С 14sp НА 13sp
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Выбрать время",
                            fontSize = 13.sp  // ← УМЕНЬШЕНО С 14sp НА 13sp
                        )
                    }
                }
                
                Text(
                    text = "Нажмите на кнопки выше, чтобы изменить дату или время",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    )
    
    if (showDatePicker) {
        AndroidDatePickerDialog(
            context = context,
            initialDate = selectedDate.time,
            onDateSelected = { timestamp ->
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                cal.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                selectedDate = cal.time
                calendar.timeInMillis = cal.timeInMillis
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    if (showTimePicker) {
        AndroidTimePickerDialog(
            context = context,
            initialTime = selectedDate.time,
            onTimeSelected = { timestamp ->
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                cal.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                selectedDate = cal.time
                calendar.timeInMillis = cal.timeInMillis
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun AndroidDatePickerDialog(
    context: android.content.Context,
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialDate }
    
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(cal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Отмена") { _, _ ->
                onDismiss()
            }
            setOnDismissListener { onDismiss() }
        }
    }
    
    LaunchedEffect(Unit) {
        datePickerDialog.show()
    }
}

@Composable
fun AndroidTimePickerDialog(
    context: android.content.Context,
    initialTime: Long,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTime }
    
    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                onTimeSelected(cal.timeInMillis)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).apply {
            setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Отмена") { _, _ ->
                onDismiss()
            }
            setOnDismissListener { onDismiss() }
        }
    }
    
    LaunchedEffect(Unit) {
        timePickerDialog.show()
    }
}