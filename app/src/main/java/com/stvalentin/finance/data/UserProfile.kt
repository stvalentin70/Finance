package com.stvalentin.finance.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,  // Всегда одна запись с id=1
    
    // ТЕГИ СТАТУСОВ (все могут быть true одновременно)
    val isStudent: Boolean = false,        // Студент
    val isWorker: Boolean = false,         // Работник по найму
    val isEntrepreneur: Boolean = false,   // Предприниматель
    val isRetiree: Boolean = false,        // Пенсионер
    val isInvestor: Boolean = false,       // Инвестор
    val isHousewife: Boolean = false,      // Домохозяйка/ин
    val isUnemployed: Boolean = false,     // Безработный
    
    // Основная информация
    val age: Int? = null,
    val city: String = "",
    
    // Семья
    val hasChildren: Boolean = false,
    val dependents: Int = 0,
    
    // Жилье
    val hasMortgage: Boolean = false,
    val hasRent: Boolean = false,
    val housingPayment: Double = 0.0,
    
    // Транспорт
    val hasCar: Boolean = false,
    val hasCarLoan: Boolean = false,
    val carPayment: Double = 0.0,
    
    // Доходы
    val mainIncomeDay: Int = 5,
    val hasPension: Boolean = false,
    val hasSalary: Boolean = false,
    val hasBusinessIncome: Boolean = false,
    
    // Кредиты
    val hasConsumerLoans: Boolean = false,
    val totalLoanPayment: Double = 0.0,
    
    // Настройки советов
    val enableCriticalAlerts: Boolean = true,
    val enableRecommendations: Boolean = true,
    val enableMotivation: Boolean = true,
    
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getActiveStatuses(): List<String> {
        val statuses = mutableListOf<String>()
        if (isStudent) statuses.add("Студент")
        if (isWorker) statuses.add("Работник")
        if (isEntrepreneur) statuses.add("Предприниматель")
        if (isRetiree) statuses.add("Пенсионер")
        if (isInvestor) statuses.add("Инвестор")
        if (isHousewife) statuses.add("Домохозяйка/ин")
        if (isUnemployed) statuses.add("Безработный")
        return statuses
    }
    
    fun getActiveStatusEmojis(): String {
        val emojis = mutableListOf<String>()
        if (isStudent) emojis.add("🎓")
        if (isWorker) emojis.add("💼")
        if (isEntrepreneur) emojis.add("🏭")
        if (isRetiree) emojis.add("👴")
        if (isInvestor) emojis.add("📈")
        if (isHousewife) emojis.add("🏠")
        if (isUnemployed) emojis.add("🕊️")
        return emojis.joinToString(" ")
    }
}