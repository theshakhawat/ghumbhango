package com.shakhawat.ghhumbhango.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shakhawat.ghhumbhango.data.local.entity.SavedDestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDestinationDao {

    @Query("SELECT * FROM saved_destinations ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAll(): Flow<List<SavedDestinationEntity>>

    @Query("SELECT * FROM saved_destinations WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<SavedDestinationEntity>>

    @Query("SELECT * FROM saved_destinations WHERE category = :category ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getByCategory(category: String): Flow<List<SavedDestinationEntity>>

    @Query("SELECT DISTINCT category FROM saved_destinations WHERE category IS NOT NULL AND category != ''")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM saved_destinations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<SavedDestinationEntity>>

    @Query("SELECT * FROM saved_destinations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' ORDER BY name ASC LIMIT 10")
    suspend fun searchDirect(query: String): List<SavedDestinationEntity>

    @Query("SELECT * FROM saved_destinations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedDestinationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(destination: SavedDestinationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(destinations: List<SavedDestinationEntity>): List<Long>

    @Update
    suspend fun update(destination: SavedDestinationEntity): Int

    @Delete
    suspend fun delete(destination: SavedDestinationEntity): Int

    @Query("DELETE FROM saved_destinations WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("UPDATE saved_destinations SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long): Int

    @Query("SELECT COUNT(*) FROM saved_destinations")
    suspend fun getCount(): Int
}

