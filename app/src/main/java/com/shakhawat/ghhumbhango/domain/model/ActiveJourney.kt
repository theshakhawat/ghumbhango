package com.shakhawat.ghhumbhango.domain.model

/**
 * Domain representation of an active journey being monitored by the application.
 */
data class ActiveJourney(
    val destination: Destination,
    val alarmDistance: AlarmDistance,
    val state: JourneyState = JourneyState.TRACKING,
    val currentDistanceMeters: Float? = null,
    val formattedDistanceRemaining: String? = null,
    val userLocation: UserLocation? = null,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val lastUpdateMillis: Long = System.currentTimeMillis(),
    val isApproaching: Boolean = false,
    val consecutiveProximityHits: Int = 0
) {
    val isActive: Boolean
        get() = state == JourneyState.TRACKING || state == JourneyState.PAUSED || state == JourneyState.APPROACHING

    val isPaused: Boolean
        get() = state == JourneyState.PAUSED

    val isTracking: Boolean
        get() = state == JourneyState.TRACKING
}

