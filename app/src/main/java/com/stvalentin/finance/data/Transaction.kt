package com.stvalentin.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
) {
    fun formattedDate(): String {
        val dateObj = Date(date)
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return format.format(dateObj)
    }
    
    fun formattedTime(): String {
        val dateObj = Date(date)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(dateObj)
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}

object TransactionCategories {
    val incomeCategories = listOf(
        "Зарплата",
        "Фриланс", 
        "Инвестиции",
        "Подарок",
        "Возврат долга",
        "Другое"
    )
    
    val expenseCategories = listOf(
        "Продукты",
        "Транспорт",
        "Жилье",
        "Развлечения",
        "Здоровье",
        "Одежда",
        "Образование",
        "Рестораны",
        "Другое"
    )
}