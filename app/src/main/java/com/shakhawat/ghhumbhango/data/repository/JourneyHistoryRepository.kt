package com.shakhawat.ghhumbhango.data.repository

import android.content.Context
import com.shakhawat.ghhumbhango.data.local.GhumBhangoDatabase
import com.shakhawat.ghhumbhango.data.local.defaultPrepopulatedPresets
import com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity
import com.shakhawat.ghhumbhango.data.local.entity.RoutePresetEntity
import com.shakhawat.ghhumbhango.domain.model.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for managing past journey records, frequent route presets, and travel statistics.
 */
class JourneyHistoryRepository(context: Context) {

    private val database = GhumBhangoDatabase.getInstance(context)
    private val historyDao = database.journeyHistoryDao()
    private val presetDao = database.routePresetDao()

    val allHistory: Flow<List<JourneyHistoryEntity>> = historyDao.getAll()

    val allPresets: Flow<List<RoutePresetEntity>> = presetDao.getAll()

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        if (presetDao.getCount() == 0) {
            presetDao.insertAll(defaultPrepopulatedPresets)
        }
    }

    suspend fun recordJourney(
        destination: Destination,
        alarmDistanceMeters: Int,
        startTimeMillis: Long,
        status: String,
        endTimeMillis: Long = System.currentTimeMillis(),
        sourceName: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val entity = JourneyHistoryEntity(
            destinationName = destination.name,
            destinationAddress = destination.address,
            latitude = destination.latitude,
            longitude = destination.longitude,
            alarmDistanceMeters = alarmDistanceMeters,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            status = status,
            sourceName = sourceName
        )
        historyDao.insert(entity)
    }

    suspend fun deleteHistory(id: Long): Int = withContext(Dispatchers.IO) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory(): Int = withContext(Dispatchers.IO) {
        historyDao.clearAll()
    }

    suspend fun savePreset(preset: RoutePresetEntity): Long = withContext(Dispatchers.IO) {
        presetDao.insert(preset)
    }

    suspend fun deletePreset(id: Long): Int = withContext(Dispatchers.IO) {
        presetDao.deleteById(id)
    }

    companion object {
        @Volatile
        private var INSTANCE: JourneyHistoryRepository? = null

        fun getInstance(context: Context): JourneyHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = JourneyHistoryRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

