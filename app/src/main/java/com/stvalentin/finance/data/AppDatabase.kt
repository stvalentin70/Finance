package com.stvalentin.finance.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Transaction::class, RegularPayment::class, Saving::class],  // ← Добавлен Saving
    version = 3,  // ← Увеличена версия (было 2, стало 3)
    exportSchema = false
)
@TypeConverters(TransactionTypeConverter::class, DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun regularPaymentDao(): RegularPaymentDao
    abstract fun savingDao(): SavingDao  // ← Добавлен новый DAO
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance.db"
                )
                .fallbackToDestructiveMigration()  // Оставляем для простоты при изменении схемы
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}