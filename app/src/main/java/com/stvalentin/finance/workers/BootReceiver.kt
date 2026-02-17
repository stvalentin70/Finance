package com.stvalentin.finance.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Перезапускаем Worker после перезагрузки
            val workRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(1, TimeUnit.DAYS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "payment_reminders",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}