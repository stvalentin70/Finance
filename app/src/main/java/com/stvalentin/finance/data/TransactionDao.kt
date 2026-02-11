package com.stvalentin.finance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    fun getTransactionsByCategory(category: String): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    @Insert
    suspend fun insert(transaction: Transaction): Long
    
    @Update
    suspend fun update(transaction: Transaction)
    
    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 0") // 0 = INCOME
    fun getTotalIncome(): Flow<Double>
    
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 1") // 1 = EXPENSE
    fun getTotalExpenses(): Flow<Double>
    
    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 0 THEN amount ELSE 0 END), 0) -
            COALESCE(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0)
        FROM transactions
    """)
    fun getBalance(): Flow<Double>
    
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE type = :type 
        GROUP BY category 
        ORDER BY total DESC
    """)
    fun getCategoryStats(type: TransactionType): Flow<List<CategoryStat>>
}

data class CategoryStat(
    val category: String,
    val total: Double
)