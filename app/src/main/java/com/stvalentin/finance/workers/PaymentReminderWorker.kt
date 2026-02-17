package com.stvalentin.finance.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stvalentin.finance.MainActivity
import com.stvalentin.finance.R
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.data.RegularPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.*

class PaymentReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "PaymentReminderWorker"
        private const val CHANNEL_ID = "payment_reminders"
        private const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "========================")
        Log.d(TAG, "doWork: Worker ЗАПУЩЕН!")
        Log.d(TAG, "Текущее время: ${Date()}")
        Log.d(TAG, "========================")
        
        return withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                
                Log.d(TAG, "Получаем платежи из БД")
                val payments = database.regularPaymentDao().getAllActivePayments()
                    .first()
                
                Log.d(TAG, "Найдено платежей: ${payments.size}")
                
                if (payments.isEmpty()) {
                    Log.d(TAG, "Платежей нет, завершаем работу")
                    return@withContext Result.success()
                }
                
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_MONTH)
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                Log.d(TAG, "Сегодня: $today.${currentMonth + 1}.$currentYear")
                
                val duePayments = mutableListOf<RegularPayment>()
                val overduePayments = mutableListOf<RegularPayment>()
                
                for (payment in payments) {
                    Log.d(TAG, "Проверяем платеж: ${payment.name}, день: ${payment.dayOfMonth}")
                    
                    // Проверяем просроченные платежи
                    if (payment.dayOfMonth < today) {
                        if (!payment.isPaidThisMonth()) {
                            overduePayments.add(payment)
                            Log.d(TAG, "→ Просрочен: ${payment.name}")
                        }
                    }
                    
                    // Проверяем платежи сегодня
                    if (payment.dayOfMonth == today) {
                        if (!payment.isPaidThisMonth()) {
                            duePayments.add(payment)
                            Log.d(TAG, "→ Нужно оплатить сегодня: ${payment.name}")
                        }
                    }
                    
                    // Проверяем платежи на завтра (с учетом напоминания)
                    if (payment.dayOfMonth == today + 1) {
                        if (!payment.isPaidThisMonth()) {
                            duePayments.add(payment)
                            Log.d(TAG, "→ Нужно оплатить завтра: ${payment.name}")
                        }
                    }
                }
                
                // Отправляем уведомления
                if (overduePayments.isNotEmpty()) {
                    val message = buildString {
                        append("Просроченные платежи:\n")
                        overduePayments.take(3).forEach {
                            append("• ${it.name} - ${it.amount}₽\n")
                        }
                        if (overduePayments.size > 3) {
                            append("и еще ${overduePayments.size - 3}...")
                        }
                    }
                    sendNotification(
                        "⚠️ Просроченные платежи",
                        message
                    )
                    Log.d(TAG, "Отправлено уведомление о просроченных: ${overduePayments.size}")
                } else if (duePayments.isNotEmpty()) {
                    val message = buildString {
                        append("Скоро нужно оплатить:\n")
                        duePayments.take(3).forEach {
                            val dayText = when (it.dayOfMonth) {
                                today -> "сегодня"
                                today + 1 -> "завтра"
                                else -> "${it.dayOfMonth} числа"
                            }
                            append("• ${it.name} - ${it.amount}₽ ($dayText)\n")
                        }
                        if (duePayments.size > 3) {
                            append("и еще ${duePayments.size - 3}...")
                        }
                    }
                    sendNotification(
                        "📅 Напоминание о платежах",
                        message
                    )
                    Log.d(TAG, "Отправлено уведомление о предстоящих: ${duePayments.size}")
                } else {
                    Log.d(TAG, "Нет платежей для уведомлений")
                }
                
                Log.d(TAG, "Worker завершил работу УСПЕШНО")
                Result.success()
                
            } catch (e: Exception) {
                Log.e(TAG, "ОШИБКА в Worker:", e)
                Result.retry()
            }
        }
    }
    
    private fun sendNotification(title: String, message: String) {
        try {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Создаем канал для уведомлений (для Android 8+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о платежах",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Уведомления о предстоящих и просроченных платежах"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            // Intent для открытия приложения
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_payment_calendar", true)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Создаем уведомление
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(com.stvalentin.finance.R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Уведомление отправлено: $title")
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при отправке уведомления:", e)
        }
    }
}