package com.stvalentin.finance.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class KeepAliveWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "KeepAliveWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Keep-alive worker started")
        
        // Просто ждем 5 секунд (минимальная работа)
        delay(5000)
        
        Log.d(TAG, "Keep-alive worker finished")
        return Result.success()
    }
}