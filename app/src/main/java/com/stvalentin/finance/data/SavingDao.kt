package com.stvalentin.finance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingDao {
    
    @Query("SELECT * FROM savings WHERE isActive = 1 ORDER BY dateUpdated DESC")
    fun getAllSavings(): Flow<List<Saving>>
    
    @Query("SELECT * FROM savings WHERE id = :id")
    suspend fun getSavingById(id: Long): Saving?
    
    @Insert
    suspend fun insert(saving: Saving): Long
    
    @Update
    suspend fun update(saving: Saving)
    
    @Delete
    suspend fun delete(saving: Saving)
    
    @Query("UPDATE savings SET isActive = 0 WHERE id = :id")
    suspend fun archiveSaving(id: Long)
    
    @Query("SELECT SUM(amount) FROM savings WHERE isActive = 1")
    fun getTotalSavings(): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM savings WHERE isActive = 1 AND currency = :currency")
    fun getTotalSavingsByCurrency(currency: String): Flow<Double?>
    
    @Query("SELECT * FROM savings WHERE isActive = 1 AND category = :category")
    fun getSavingsByCategory(category: String): Flow<List<Saving>>
}