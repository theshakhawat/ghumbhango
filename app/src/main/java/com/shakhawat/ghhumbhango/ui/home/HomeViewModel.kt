package com.shakhawat.ghhumbhango.ui.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakhawat.ghhumbhango.data.location.DefaultLocationClient
import com.shakhawat.ghhumbhango.data.repository.DestinationRepository
import com.shakhawat.ghhumbhango.data.repository.GeocodeResult
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.GpsStatus
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.service.JourneyTrackingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DestinationRepository(application)
    private val locationClient = DefaultLocationClient(application)
    private val preferencesRepo = com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository.getInstance(application)
    private val historyRepo = com.shakhawat.ghhumbhango.data.repository.JourneyHistoryRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var locationJob: Job? = null

    init {
        // Ensure Room database is initialized
        viewModelScope.launch {
            repository.ensureInitialized()
        }

        // Observe user settings from DataStore
        viewModelScope.launch {
            preferencesRepo.userSettings.collectLatest { prefs ->
                _uiState.update { current ->
                    if (!current.isJourneyActive && current.selectedDestination == null) {
                        current.copy(selectedDistance = prefs.defaultAlarmDistance)
                    } else {
                        current
                    }
                }
            }
        }

        // Observe saved destinations reactively from Room
        viewModelScope.launch {
            repository.savedDestinations.collectLatest { list ->
                _uiState.update { current ->
                    current.copy(
                        savedDestinations = list,
                        searchResults = if (current.searchQuery.isBlank()) list else current.searchResults
                    )
                }
            }
        }

        // Observe journey history reactively from Room for recent journeys on homepage
        viewModelScope.launch {
            historyRepo.allHistory.collectLatest { history ->
                _uiState.update { current ->
                    current.copy(recentJourneys = history)
                }
            }
        }

        // Observe background tracking service state reactively
        viewModelScope.launch {
            JourneyTrackingService.activeJourney.collectLatest { active ->
                _uiState.update { current ->
                    if (active != null) {
                        current.copy(
                            activeJourney = active,
                            selectedDestination = active.destination,
                            selectedDistance = active.alarmDistance,
                            distanceToDestinationMeters = active.currentDistanceMeters,
                            formattedDistanceRemaining = active.formattedDistanceRemaining,
                            isWithinAlarmRadius = active.isApproaching
                        )
                    } else {
                        val cleared = current.copy(activeJourney = null)
                        recomputeDistance(cleared)
                    }
                }
            }
        }

        refreshLocationState()
    }

    fun refreshLocationState() {
        val fine = locationClient.hasFineLocationPermission()
        val coarse = locationClient.hasLocationPermission()
        val gpsOn = locationClient.isGpsEnabled()
        val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        _uiState.update { current ->
            val updated = current.copy(
                hasFineLocationPermission = fine,
                hasCoarseLocationPermission = coarse,
                hasNotificationPermission = hasNotification,
                isGpsEnabled = gpsOn,
                gpsStatus = when {
                    !coarse -> GpsStatus.PermissionDenied()
                    !gpsOn -> GpsStatus.GpsDisabled()
                    !fine -> GpsStatus.ApproximateOnly()
                    current.currentLocation != null -> GpsStatus.Ready(current.currentLocation)
                    else -> GpsStatus.Searching()
                }
            )
            recomputeDistance(updated)
        }

        if (coarse && gpsOn) {
            startLocationUpdates()
        }
    }

    fun onPermissionResult(fineGranted: Boolean, coarseGranted: Boolean) {
        val gpsOn = locationClient.isGpsEnabled()
        _uiState.update { current ->
            val updated = current.copy(
                hasFineLocationPermission = fineGranted,
                hasCoarseLocationPermission = coarseGranted,
                isGpsEnabled = gpsOn,
                gpsStatus = when {
                    !fineGranted && !coarseGranted -> GpsStatus.PermissionDenied()
                    !gpsOn -> GpsStatus.GpsDisabled()
                    !fineGranted -> GpsStatus.ApproximateOnly()
                    current.currentLocation != null -> GpsStatus.Ready(current.currentLocation)
                    else -> GpsStatus.Searching()
                }
            )
            recomputeDistance(updated)
        }

        if (coarseGranted && gpsOn) {
            startLocationUpdates()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasNotificationPermission = granted) }
    }

    private fun startLocationUpdates() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            // First fetch last known location quickly
            locationClient.getLastKnownLocation()?.let { last ->
                _uiState.update { current ->
                    val updated = current.copy(
                        currentLocation = last,
                        gpsStatus = GpsStatus.Ready(last)
                    )
                    recomputeDistance(updated)
                }
            }

            // Stream continuous location fixes
            locationClient.getLocationUpdates(intervalMs = 4000L)
                .catch { error ->
                    _uiState.update {
                        it.copy(gpsStatus = GpsStatus.Searching("সিগন্যাল দুর্বল বা বিচ্ছিন্ন: ${error.localizedMessage}"))
                    }
                }
                .collectLatest { loc ->
                    _uiState.update { current ->
                        val updated = current.copy(
                            currentLocation = loc,
                            gpsStatus = GpsStatus.Ready(loc)
                        )
                        recomputeDistance(updated)
                    }
                }
        }
    }

    private fun recomputeDistance(current: HomeUiState): HomeUiState {
        val loc = current.currentLocation
        val dest = current.selectedDestination
        return if (loc != null && dest != null) {
            val eval = DistanceCalculator.evaluateProximity(
                userLocation = loc,
                destination = dest,
                alarmRadiusMeters = current.selectedDistance.meters
            )
            current.copy(
                distanceToDestinationMeters = eval.distanceMeters,
                formattedDistanceRemaining = eval.formattedDistance,
                isWithinAlarmRadius = eval.isWithinRadius,
                isDistanceReliable = eval.isReliable
            )
        } else {
            current.copy(
                distanceToDestinationMeters = null,
                formattedDistanceRemaining = null,
                isWithinAlarmRadius = false,
                isDistanceReliable = true
            )
        }
    }

    fun selectAlarmDistance(distance: AlarmDistance) {
        if (_uiState.value.isJourneyActive) return
        _uiState.update { current ->
            val updated = current.copy(selectedDistance = distance)
            recomputeDistance(updated)
        }
        viewModelScope.launch {
            preferencesRepo.setDefaultAlarmDistance(distance)
        }
    }

    fun selectDestination(destination: Destination?) {
        if (_uiState.value.isJourneyActive) return
        _uiState.update { current ->
            val updated = current.copy(selectedDestination = destination, isSearchSheetOpen = false)
            recomputeDistance(updated)
        }
        destination?.id?.toLongOrNull()?.let { id ->
            viewModelScope.launch {
                repository.markLastUsed(id)
            }
        }
    }

    fun clearDestination() {
        if (_uiState.value.isJourneyActive) return
        _uiState.update { current ->
            val updated = current.copy(selectedDestination = null)
            recomputeDistance(updated)
        }
    }

    fun startJourney() {
        val dest = _uiState.value.selectedDestination ?: return
        val distance = _uiState.value.selectedDistance
        JourneyTrackingService.start(getApplication(), dest, distance)
    }

    fun pauseJourney() {
        JourneyTrackingService.pause(getApplication())
    }

    fun resumeJourney() {
        JourneyTrackingService.resume(getApplication())
    }

    fun requestStopJourney() {
        _uiState.update { it.copy(showStopConfirmationDialog = true) }
    }

    fun dismissStopConfirmation() {
        _uiState.update { it.copy(showStopConfirmationDialog = false) }
    }

    fun confirmStopJourney() {
        JourneyTrackingService.stop(getApplication())
        _uiState.update {
            it.copy(
                showStopConfirmationDialog = false,
                activeJourney = null
            )
        }
    }

    fun dismissAlarm() {
        JourneyTrackingService.dismissAlarm(getApplication())
    }

    fun snoozeAlarm() {
        JourneyTrackingService.snoozeAlarm(getApplication())
    }

    fun openSearchSheet() {
        _uiState.update {
            it.copy(
                isSearchSheetOpen = true,
                searchQuery = "",
                searchResults = it.savedDestinations,
                onlineSearchMessage = null
            )
        }
    }

    fun closeSearchSheet() {
        _uiState.update { it.copy(isSearchSheetOpen = false) }
    }

    fun openSaveCustomDialog() {
        _uiState.update { it.copy(isSaveCustomDialogOpen = true) }
    }

    fun closeSaveCustomDialog() {
        _uiState.update { it.copy(isSaveCustomDialogOpen = false) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, onlineSearchMessage = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { it.copy(searchResults = it.savedDestinations) }
            } else {
                repository.searchLocal(query).collectLatest { localResults ->
                    _uiState.update { current ->
                        if (current.searchQuery == query) {
                            current.copy(searchResults = localResults)
                        } else {
                            current
                        }
                    }
                }
            }
        }
    }

    fun searchOnline() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingOnline = true, onlineSearchMessage = null) }
            when (val result = repository.searchOnline(query)) {
                is GeocodeResult.Success -> {
                    _uiState.update {
                        val combined = (result.destinations + it.searchResults).distinctBy { d -> d.name }
                        it.copy(
                            isSearchingOnline = false,
                            searchResults = combined,
                            onlineSearchMessage = if (result.destinations.isEmpty()) "কোনো অনলাইন ফলাফল পাওয়া যায়নি।" else null
                        )
                    }
                }
                is GeocodeResult.Offline -> {
                    _uiState.update {
                        it.copy(
                            isSearchingOnline = false,
                            onlineSearchMessage = result.message
                        )
                    }
                }
                is GeocodeResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSearchingOnline = false,
                            onlineSearchMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun saveCustomDestination(
        name: String,
        latitude: Double,
        longitude: Double,
        address: String?
    ) {
        viewModelScope.launch {
            val destination = Destination(
                name = name.trim(),
                latitude = latitude,
                longitude = longitude,
                address = address?.trim(),
                isSaved = true
            )
            val insertedId = repository.saveDestination(destination)
            val savedWithId = destination.copy(id = insertedId.toString())
            _uiState.update { current ->
                val updated = current.copy(
                    selectedDestination = savedWithId,
                    isSaveCustomDialogOpen = false,
                    isSearchSheetOpen = false
                )
                recomputeDistance(updated)
            }
        }
    }

    fun deleteSavedDestination(destination: Destination) {
        val id = destination.id.toLongOrNull() ?: return
        viewModelScope.launch {
            repository.deleteDestination(id)
            if (_uiState.value.selectedDestination?.id == destination.id) {
                clearDestination()
            }
        }
    }
}
