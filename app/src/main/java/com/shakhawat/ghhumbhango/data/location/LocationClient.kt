package com.shakhawat.ghhumbhango.data.location

import com.shakhawat.ghhumbhango.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(intervalMs: Long = 4000L): Flow<UserLocation>
    suspend fun getLastKnownLocation(): UserLocation?
    fun isGpsEnabled(): Boolean
    fun hasLocationPermission(): Boolean
    fun hasFineLocationPermission(): Boolean

    class LocationException(message: String) : Exception(message)
}

