package com.stvalentin.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "regular_payments")
data class RegularPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val amount: Double,
    val dayOfMonth: Int,
    val reminderDays: Int = 1,
    val isActive: Boolean = true,
    val description: String = "",
    val lastPaidDate: Long? = null,
    val nextDueDate: Long? = null
) {
    fun isPaidThisMonth(): Boolean {
        if (lastPaidDate == null) return false
        
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        
        calendar.timeInMillis = lastPaidDate
        val lastPaidMonth = calendar.get(Calendar.MONTH)
        val lastPaidYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = today
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return lastPaidMonth == currentMonth && lastPaidYear == currentYear
    }
}