package com.shakhawat.ghhumbhango.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JourneyHistoryDao {

    @Query("SELECT * FROM journey_history ORDER BY endTimeMillis DESC")
    fun getAll(): Flow<List<JourneyHistoryEntity>>

    @Query("SELECT * FROM journey_history ORDER BY endTimeMillis DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<JourneyHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: JourneyHistoryEntity): Long

    @Query("DELETE FROM journey_history WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM journey_history")
    suspend fun clearAll(): Int

    @Query("SELECT COUNT(*) FROM journey_history")
    suspend fun getCount(): Int
}

