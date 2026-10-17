package com.shakhawat.ghhumbhango.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity
import com.shakhawat.ghhumbhango.data.local.entity.RoutePresetEntity
import com.shakhawat.ghhumbhango.data.repository.DestinationRepository
import com.shakhawat.ghhumbhango.data.repository.JourneyHistoryRepository
import com.shakhawat.ghhumbhango.domain.model.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val selectedTab: Int = 0, // 0: ইতিহাস, 1: রুট প্রিসেট, 2: সংরক্ষিত স্টপ
    val historyList: List<JourneyHistoryEntity> = emptyList(),
    val presetList: List<RoutePresetEntity> = emptyList(),
    val savedStops: List<Destination> = emptyList(),
    val isClearHistoryDialogVisible: Boolean = false,
    val isAddPresetDialogVisible: Boolean = false,
    val isAddSavedStopDialogVisible: Boolean = false,
    val presetInitialDestination: Destination? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepo = JourneyHistoryRepository.getInstance(application)
    private val destinationRepo = DestinationRepository(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepo.ensureInitialized()
            destinationRepo.ensureInitialized()
        }

        // Collect Journey History
        viewModelScope.launch {
            historyRepo.allHistory.collectLatest { list ->
                _uiState.update { it.copy(historyList = list) }
            }
        }

        // Collect Route Presets
        viewModelScope.launch {
            historyRepo.allPresets.collectLatest { list ->
                _uiState.update { it.copy(presetList = list) }
            }
        }

        // Collect Saved Destinations
        viewModelScope.launch {
            destinationRepo.savedDestinations.collectLatest { list ->
                _uiState.update { it.copy(savedStops = list) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            historyRepo.deleteHistory(id)
        }
    }

    fun showClearHistoryDialog() {
        _uiState.update { it.copy(isClearHistoryDialogVisible = true) }
    }

    fun dismissClearHistoryDialog() {
        _uiState.update { it.copy(isClearHistoryDialogVisible = false) }
    }

    fun confirmClearHistory() {
        viewModelScope.launch {
            historyRepo.clearHistory()
            _uiState.update { it.copy(isClearHistoryDialogVisible = false) }
        }
    }

    fun openAddPresetDialog(initialDestination: Destination? = null) {
        _uiState.update {
            it.copy(
                isAddPresetDialogVisible = true,
                presetInitialDestination = initialDestination
            )
        }
    }

    fun closeAddPresetDialog() {
        _uiState.update {
            it.copy(
                isAddPresetDialogVisible = false,
                presetInitialDestination = null
            )
        }
    }

    fun saveNewPreset(preset: RoutePresetEntity) {
        viewModelScope.launch {
            historyRepo.savePreset(preset)
            closeAddPresetDialog()
        }
    }

    fun savePreset(
        title: String,
        destinationName: String,
        destinationAddress: String?,
        latitude: Double,
        longitude: Double,
        alarmDistanceMeters: Int,
        category: String
    ) {
        viewModelScope.launch {
            historyRepo.savePreset(
                RoutePresetEntity(
                    title = title,
                    destinationName = destinationName,
                    destinationAddress = destinationAddress,
                    latitude = latitude,
                    longitude = longitude,
                    alarmDistanceMeters = alarmDistanceMeters,
                    category = category
                )
            )
            closeAddPresetDialog()
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            historyRepo.deletePreset(id)
        }
    }

    fun deleteSavedStop(id: Long) {
        viewModelScope.launch {
            destinationRepo.deleteDestination(id)
        }
    }

    fun openAddSavedStopDialog() {
        _uiState.update { it.copy(isAddSavedStopDialogVisible = true) }
    }

    fun closeAddSavedStopDialog() {
        _uiState.update { it.copy(isAddSavedStopDialogVisible = false) }
    }

    fun saveCustomStop(name: String, address: String?, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            destinationRepo.saveDestination(
                Destination(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    isSaved = true
                )
            )
            closeAddSavedStopDialog()
        }
    }
}

