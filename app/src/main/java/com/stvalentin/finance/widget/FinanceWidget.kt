package com.stvalentin.finance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import com.stvalentin.finance.R
import com.stvalentin.finance.data.AppDatabase
import com.stvalentin.finance.data.TransactionType
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FinanceWidget : AppWidgetProvider() {
    
    companion object {
        private const val TAG = "FinanceWidget"
        private const val PREFS_NAME = "WidgetPrefs"
        private const val LAST_UPDATE_KEY = "last_update_time"
        private const val MIN_UPDATE_INTERVAL = 30000 // 30 секунд
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate started for widgets: ${appWidgetIds.joinToString()}")
        
        // Запускаем обновление без проверки времени
        performUpdate(context, appWidgetManager, appWidgetIds)
    }
    
    // Отдельный метод для принудительного обновления
    fun forceUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "Force update for widgets: ${appWidgetIds.joinToString()}")
        performUpdate(context, appWidgetManager, appWidgetIds)
    }
    
    private fun performUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val stats = getTodayStats(context)
                
                withContext(Dispatchers.Main) {
                    for (widgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, widgetId, stats)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget", e)
            }
        }
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        stats: TodayStats
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        try {
            // Дата и время
            val currentDateTime = Date()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
            views.setTextViewText(R.id.tv_datetime, dateFormat.format(currentDateTime))
            
            // Форматируем числа
            val format = NumberFormat.getNumberInstance(Locale("ru", "RU")).apply {
                maximumFractionDigits = 0
            }
            
            // Устанавливаем суммы
            views.setTextViewText(R.id.tv_expense_amount, "${format.format(stats.expenses)} ₽")
            views.setTextViewText(R.id.tv_income_amount, "${format.format(stats.income)} ₽")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting views", e)
            views.setTextViewText(R.id.tv_expense_amount, "-- ₽")
            views.setTextViewText(R.id.tv_income_amount, "-- ₽")
        }
        
        // Обработчик нажатия
        val intent = Intent(context, QuickInputActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
        Log.d(TAG, "Widget $appWidgetId updated")
    }
    
    private suspend fun getTodayStats(context: Context): TodayStats {
        val database = AppDatabase.getDatabase(context)
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val todayEnd = todayStart + 24 * 60 * 60 * 1000 - 1
        
        val todayTransactions = database.transactionDao()
            .getTransactionsBetweenDates(todayStart, todayEnd)
        
        var income = 0.0
        var expenses = 0.0
        
        for (transaction in todayTransactions) {
            if (transaction.type == TransactionType.INCOME) {
                income += transaction.amount
            } else {
                expenses += transaction.amount
            }
        }
        
        return TodayStats(income, expenses)
    }
    
    data class TodayStats(
        val income: Double,
        val expenses: Double
    )
}