package com.orienteering.hunt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orienteering.hunt.data.models.GeoLocation
import com.orienteering.hunt.data.models.Hunt
import com.orienteering.hunt.data.models.LeaderboardEntry
import com.orienteering.hunt.data.models.Player
import com.orienteering.hunt.data.models.PlayerProgress
import com.orienteering.hunt.data.repository.HuntRepository
import com.orienteering.hunt.services.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPlayer: Player? = null,
    val isOnboarding: Boolean = true
)

data class HuntSelectionUiState(
    val hunts: List<Hunt> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedDifficulty: String? = null
)

data class ActiveHuntUiState(
    val hunt: Hunt? = null,
    val progress: PlayerProgress? = null,
    val currentLocation: GeoLocation? = null,
    val currentClueIndex: Int = 0,
    val distanceToTarget: Float? = null,
    val canCheckIn: Boolean = false,
    val showCheckInSuccess: Boolean = false,
    val checkInPoints: Int = 0,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GameViewModel(
    private val repository: HuntRepository,
    private val locationService: LocationService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    
    private val _huntSelectionState = MutableStateFlow(HuntSelectionUiState())
    val huntSelectionState: StateFlow<HuntSelectionUiState> = _huntSelectionState.asStateFlow()
    
    private val _activeHuntState = MutableStateFlow(ActiveHuntUiState())
    val activeHuntState: StateFlow<ActiveHuntUiState> = _activeHuntState.asStateFlow()
    
    private val _leaderboardState = MutableStateFlow(LeaderboardUiState())
    val leaderboardState: StateFlow<LeaderboardUiState> = _leaderboardState.asStateFlow()
    
    private val _currentLocation = MutableStateFlow<GeoLocation?>(null)
    val currentLocation: StateFlow<GeoLocation?> = _currentLocation.asStateFlow()
    
    private var locationTrackingJob: Job? = null
    
    val currentPlayer: StateFlow<Player?> = repository.currentPlayer
    val activeProgress: StateFlow<PlayerProgress?> = repository.activeProgress
    
    fun setPlayer(player: Player) {
        repository.setCurrentPlayer(player)
        _uiState.update { 
            it.copy(
                currentPlayer = player,
                isOnboarding = false
            )
        }
    }
    
    fun createPlayer(username: String, displayName: String) {
        val player = Player(
            id = username,
            username = username,
            displayName = displayName,
            email = "",
            isAdmin = false
        )
        setPlayer(player)
    }
    
    fun loadHunts(search: String? = null, difficulty: String? = null) {
        viewModelScope.launch {
            _huntSelectionState.update { it.copy(isLoading = true, error = null) }
            
            repository.refreshHunts(search, difficulty).fold(
                onSuccess = { hunts ->
                    _huntSelectionState.update { 
                        it.copy(
                            hunts = hunts.filter { hunt -> hunt.isActive },
                            isLoading = false,
                            searchQuery = search ?: "",
                            selectedDifficulty = difficulty
                        )
                    }
                },
                onFailure = { error ->
                    _huntSelectionState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load hunts"
                        )
                    }
                }
            )
        }
    }
    
    fun loadLeaderboard(huntId: String? = null) {
        viewModelScope.launch {
            _leaderboardState.update { it.copy(isLoading = true, error = null) }
            
            repository.getLeaderboard(huntId).fold(
                onSuccess = { entries ->
                    _leaderboardState.update {
                        it.copy(
                            entries = entries,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _leaderboardState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load leaderboard"
                        )
                    }
                }
            )
        }
    }
    
    fun startHunt(huntId: String) {
        viewModelScope.launch {
            val player = _uiState.value.currentPlayer ?: return@launch
            
            _activeHuntState.update { it.copy(isLoading = true, error = null) }
            
            repository.startHunt(huntId, player.id).fold(
                onSuccess = { progress ->
                    val hunt = repository.getCachedHuntById(huntId)
                    _activeHuntState.update {
                        it.copy(
                            hunt = hunt,
                            progress = progress,
                            currentClueIndex = 0,
                            isCompleted = false,
                            isLoading = false
                        )
                    }
                    startLocationTracking()
                },
                onFailure = { error ->
                    _activeHuntState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to start hunt"
                        )
                    }
                }
            )
        }
    }
    
    private fun startLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = viewModelScope.launch {
            locationService.getLocationUpdates().collect { location ->
                _currentLocation.value = location
                updateDistanceToTarget(location)
            }
        }
    }
    
    private fun updateDistanceToTarget(location: GeoLocation) {
        val state = _activeHuntState.value
        val hunt = state.hunt ?: return
        val progress = state.progress ?: return
        
        val currentLocationIndex = progress.currentLocationIndex
        if (currentLocationIndex >= hunt.locations.size) return
        
        val targetLocation = hunt.locations[currentLocationIndex]
        val distance = locationService.calculateDistance(location, targetLocation.location)
        val canCheckIn = locationService.isWithinCheckInRadius(location, targetLocation.location)
        
        _activeHuntState.update {
            it.copy(
                currentLocation = location,
                distanceToTarget = distance,
                canCheckIn = canCheckIn
            )
        }
    }
    
    fun checkIn() {
        viewModelScope.launch {
            val state = _activeHuntState.value
            val hunt = state.hunt ?: return@launch
            val progress = state.progress ?: return@launch
            val location = state.currentLocation ?: return@launch
            
            if (!state.canCheckIn) return@launch
            
            _activeHuntState.update { it.copy(isLoading = true) }
            
            repository.checkIn(hunt.id, location.latitude, location.longitude).fold(
                onSuccess = { (updatedProgress, pointsEarned, isCompleted) ->
                    _activeHuntState.update {
                        it.copy(
                            progress = updatedProgress,
                            showCheckInSuccess = true,
                            checkInPoints = pointsEarned,
                            isCompleted = isCompleted,
                            canCheckIn = false,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _activeHuntState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Check-in failed"
                        )
                    }
                }
            )
        }
    }
    
    fun dismissCheckInSuccess() {
        _activeHuntState.update { it.copy(showCheckInSuccess = false, checkInPoints = 0) }
    }
    
    fun showHint(): String? {
        val state = _activeHuntState.value
        val hunt = state.hunt ?: return null
        val progress = state.progress ?: return null
        
        val currentLocationIndex = progress.currentLocationIndex
        if (currentLocationIndex >= hunt.locations.size) return null
        
        val location = hunt.locations[currentLocationIndex]
        return location.hint
    }
    
    fun getCurrentClue(): String? {
        val state = _activeHuntState.value
        val hunt = state.hunt ?: return null
        val progress = state.progress ?: return null
        
        val currentLocationIndex = progress.currentLocationIndex
        if (currentLocationIndex >= hunt.locations.size) return null
        
        val clue = hunt.clues.find { it.huntLocationId == hunt.locations[currentLocationIndex].id }
        return clue?.text
    }
    
    fun endHunt() {
        locationTrackingJob?.cancel()
        repository.clearActiveProgress()
        _activeHuntState.value = ActiveHuntUiState()
    }
    
    fun abandonHunt(huntId: String, onAbandoned: () -> Unit) {
        viewModelScope.launch {
            _activeHuntState.update { it.copy(isLoading = true, error = null) }
            
            repository.abandonHunt(huntId).fold(
                onSuccess = {
                    locationTrackingJob?.cancel()
                    repository.clearActiveProgress()
                    _activeHuntState.value = ActiveHuntUiState()
                    onAbandoned()
                },
                onFailure = { error ->
                    _activeHuntState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to abandon hunt"
                        )
                    }
                }
            )
        }
    }
    
    fun hasLocationPermissions(): Boolean = locationService.hasLocationPermissions()
    
    fun getHuntById(id: String): Hunt? = repository.getCachedHuntById(id)
    
    fun getPlayerCompletedHunts(): Int {
        return _uiState.value.currentPlayer?.completedHunts ?: 0
    }
    
    fun getPlayerTotalPoints(): Int {
        return _uiState.value.currentPlayer?.totalPoints ?: 0
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
        _huntSelectionState.update { it.copy(error = null) }
        _activeHuntState.update { it.copy(error = null) }
        _leaderboardState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        locationTrackingJob?.cancel()
    }
}
