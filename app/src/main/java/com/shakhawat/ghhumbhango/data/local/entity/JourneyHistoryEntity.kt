package com.shakhawat.ghhumbhango.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination

/**
 * Room entity representing a completed or stopped transit journey.
 */
@Entity(tableName = "journey_history")
data class JourneyHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val destinationName: String,
    val destinationAddress: String? = null,
    val latitude: Double,
    val longitude: Double,
    val alarmDistanceMeters: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long = System.currentTimeMillis(),
    val status: String, // "COMPLETED" (সফল আগমন) or "STOPPED" (স্থগিত)
    val sourceName: String? = null
) {
    val durationMinutes: Long
        get() = maxOf(1L, (endTimeMillis - startTimeMillis) / (60 * 1000L))

    fun toDestination(): Destination = Destination(
        id = id.toString(),
        name = destinationName,
        latitude = latitude,
        longitude = longitude,
        address = destinationAddress,
        isSaved = true
    )

    fun toAlarmDistance(): AlarmDistance = AlarmDistance.fromMeters(alarmDistanceMeters)
}

