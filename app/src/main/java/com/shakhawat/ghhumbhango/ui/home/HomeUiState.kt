package com.shakhawat.ghhumbhango.ui.home

import com.shakhawat.ghhumbhango.domain.model.ActiveJourney
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.GpsStatus
import com.shakhawat.ghhumbhango.domain.model.UserLocation

/**
 * UI State for the GhumBhango Home Screen, Destination Selection, Active Tracking, and Journey Controls.
 */
data class HomeUiState(
    val selectedDestination: Destination? = null,
    val selectedDistance: AlarmDistance = AlarmDistance.DEFAULT,
    val currentLocation: UserLocation? = null,
    val gpsStatus: GpsStatus = GpsStatus.Searching(),
    val isGpsEnabled: Boolean = true,
    val hasFineLocationPermission: Boolean = false,
    val hasCoarseLocationPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val activeJourney: ActiveJourney? = null,
    val showStopConfirmationDialog: Boolean = false,
    val isSearchSheetOpen: Boolean = false,
    val isSaveCustomDialogOpen: Boolean = false,
    val searchQuery: String = "",
    val isSearchingOnline: Boolean = false,
    val onlineSearchMessage: String? = null,
    val savedDestinations: List<Destination> = emptyList(),
    val searchResults: List<Destination> = emptyList(),
    val quickStops: List<Destination> = emptyList(),
    val recentJourneys: List<com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity> = emptyList(),
    val distanceToDestinationMeters: Float? = null,
    val formattedDistanceRemaining: String? = null,
    val isWithinAlarmRadius: Boolean = false,
    val isDistanceReliable: Boolean = true
) {
    val hasLocationPermission: Boolean
        get() = hasFineLocationPermission || hasCoarseLocationPermission

    val isJourneyActive: Boolean
        get() = activeJourney?.isActive == true

    val canStartJourney: Boolean
        get() = selectedDestination != null && !isJourneyActive && hasLocationPermission && isGpsEnabled
}
