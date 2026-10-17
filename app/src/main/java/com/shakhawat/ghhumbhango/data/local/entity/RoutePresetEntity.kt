package com.shakhawat.ghhumbhango.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination

/**
 * Entity representing a recurring commuter route preset (e.g., Office, Home, University).
 */
@Entity(tableName = "route_presets")
data class RoutePresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // e.g. "দৈনিক অফিস যাত্রা", "বাসা ফেরা"
    val destinationName: String,
    val destinationAddress: String? = null,
    val latitude: Double,
    val longitude: Double,
    val alarmDistanceMeters: Int = 500,
    val category: String = "WORK", // WORK, HOME, STUDY, OTHER
    val createdAt: Long = System.currentTimeMillis()
) {
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

