package com.stvalentin.finance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.stvalentin.finance.R

class PlaceholderWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Создаем Intent для открытия активности
        val intent = Intent(context, PlaceholderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Получаем макет виджета
        val views = RemoteViews(context.packageName, R.layout.widget_placeholder)

        // Устанавливаем обработчик нажатия на весь виджет
        views.setOnClickPendingIntent(R.id.widget_placeholder, pendingIntent)

        // Обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}