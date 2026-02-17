package com.stvalentin.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings")
data class Saving(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                  // Название накопления (например, "Подушка безопасности")
    val category: String,               // Категория из savingCategories
    val amount: Double,                 // Сумма
    val currency: String = "RUB",       // Валюта (RUB, USD, EUR)
    val note: String = "",              // Заметка
    val dateCreated: Long = System.currentTimeMillis(),  // Дата создания
    val dateUpdated: Long = System.currentTimeMillis(),  // Дата последнего обновления
    val targetAmount: Double? = null,    // Целевая сумма (если есть цель)
    val isActive: Boolean = true         // Активно ли накопление
)