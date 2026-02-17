// file: RegularPaymentDao.kt
package com.stvalentin.finance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RegularPaymentDao {
    
    @Query("SELECT * FROM regular_payments WHERE isActive = 1 ORDER BY dayOfMonth ASC")
    fun getAllActivePayments(): Flow<List<RegularPayment>>
    
    @Query("SELECT * FROM regular_payments ORDER BY dayOfMonth ASC")
    fun getAllPayments(): Flow<List<RegularPayment>>
    
    @Query("SELECT * FROM regular_payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): RegularPayment?
    
    @Insert
    suspend fun insert(payment: RegularPayment): Long
    
    @Update
    suspend fun update(payment: RegularPayment)
    
    @Delete
    suspend fun delete(payment: RegularPayment)
    
    @Query("SELECT * FROM regular_payments WHERE isActive = 1 AND dayOfMonth = :day")
    suspend fun getPaymentsByDay(day: Int): List<RegularPayment>
    
    @Query("""
        SELECT * FROM regular_payments 
        WHERE isActive = 1 
        AND (
            (nextDueDate IS NOT NULL AND nextDueDate BETWEEN :startDate AND :endDate)
            OR
            (nextDueDate IS NULL AND 
             dayOfMonth BETWEEN :startDay AND :endDay)
        )
    """)
    suspend fun getDuePayments(startDate: Long, endDate: Long, startDay: Int, endDay: Int): List<RegularPayment>
    
    @Query("UPDATE regular_payments SET lastPaidDate = :paidDate, nextDueDate = :nextDue WHERE id = :id")
    suspend fun markAsPaid(id: Long, paidDate: Long, nextDue: Long)
}