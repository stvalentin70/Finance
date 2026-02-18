package com.stvalentin.finance.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileSync(): UserProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfile)
    
    @Update
    suspend fun update(userProfile: UserProfile)
    
    @Query("UPDATE user_profile SET lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateLastUpdated(timestamp: Long)
}