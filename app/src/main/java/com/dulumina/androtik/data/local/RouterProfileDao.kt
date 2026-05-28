package com.dulumina.androtik.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterProfileDao {
    @Query("SELECT * FROM router_profiles ORDER BY last_connected_at DESC, created_at DESC")
    fun getAllProfiles(): Flow<List<RouterProfileEntity>>

    @Query("SELECT * FROM router_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): RouterProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: RouterProfileEntity): Long

    @Update
    suspend fun update(profile: RouterProfileEntity)

    @Delete
    suspend fun delete(profile: RouterProfileEntity)

    @Query("UPDATE router_profiles SET last_connected_at = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Long, timestamp: Long)
}
