package com.shakhawat.ghhumbhango.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shakhawat.ghhumbhango.data.local.entity.RoutePresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePresetDao {

    @Query("SELECT * FROM route_presets ORDER BY createdAt ASC")
    fun getAll(): Flow<List<RoutePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: RoutePresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(presets: List<RoutePresetEntity>): List<Long>

    @Update
    suspend fun update(preset: RoutePresetEntity): Int

    @Query("DELETE FROM route_presets WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT COUNT(*) FROM route_presets")
    suspend fun getCount(): Int
}

