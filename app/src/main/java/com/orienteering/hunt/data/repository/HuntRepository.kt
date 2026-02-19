package com.orienteering.hunt.data.repository

import com.orienteering.hunt.data.api.ApiService
import com.orienteering.hunt.data.api.toHunt
import com.orienteering.hunt.data.api.toLeaderboardEntry
import com.orienteering.hunt.data.api.toPlayerProgress
import com.orienteering.hunt.data.models.Hunt
import com.orienteering.hunt.data.models.LeaderboardEntry
import com.orienteering.hunt.data.models.Player
import com.orienteering.hunt.data.models.PlayerProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HuntRepository(
    private val apiService: ApiService
) {
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    private val _activeProgress = MutableStateFlow<PlayerProgress?>(null)
    val activeProgress: StateFlow<PlayerProgress?> = _activeProgress.asStateFlow()

    private val _allProgress = MutableStateFlow<List<PlayerProgress>>(emptyList())
    val allProgress: StateFlow<List<PlayerProgress>> = _allProgress.asStateFlow()
    
    private val _cachedHunts = MutableStateFlow<List<Hunt>>(emptyList())
    val cachedHunts: StateFlow<List<Hunt>> = _cachedHunts.asStateFlow()

    suspend fun refreshHunts(search: String? = null, difficulty: String? = null): Result<List<Hunt>> {
        return apiService.getHunts(search, difficulty).map { huntDtos ->
            val hunts = huntDtos.map { it.toHunt() }
            _cachedHunts.value = hunts
            hunts
        }
    }
    
    suspend fun getHuntById(id: String): Result<Hunt> {
        val cached = _cachedHunts.value.find { it.id == id }
        if (cached != null) return Result.success(cached)
        
        return apiService.getHunt(id).map { it.toHunt() }
    }

    fun getCachedHuntById(id: String): Hunt? {
        return _cachedHunts.value.find { it.id == id }
    }

    suspend fun getActiveHunts(): Result<List<Hunt>> {
        val hunts = _cachedHunts.value.ifEmpty {
            refreshHunts().getOrDefault(emptyList())
        }
        return Result.success(hunts.filter { it.isActive })
    }

    fun setCurrentPlayer(player: Player) {
        _currentPlayer.value = player
    }

    suspend fun startHunt(huntId: String, playerId: String): Result<PlayerProgress> {
        return apiService.startHunt(huntId).map { progressDto ->
            val progress = progressDto.toPlayerProgress()
            _activeProgress.value = progress
            progress
        }
    }
    
    suspend fun abandonHunt(huntId: String): Result<Unit> {
        return apiService.abandonHunt(huntId).also { result ->
            if (result.isSuccess) {
                _activeProgress.value = null
            }
        }
    }

    suspend fun checkIn(huntId: String, latitude: Double, longitude: Double): Result<Triple<PlayerProgress, Int, Boolean>> {
        return apiService.checkIn(huntId, latitude, longitude).map { response ->
            val progress = response.progress?.toPlayerProgress() ?: _activeProgress.value!!
            _activeProgress.value = progress
            
            val currentList = _allProgress.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == progress.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = progress
            } else {
                currentList.add(progress)
            }
            _allProgress.value = currentList
            
            Triple(progress, response.pointsEarned, progress.isCompleted)
        }
    }

    fun updateProgress(progress: PlayerProgress) {
        _activeProgress.value = progress
        val currentList = _allProgress.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.id == progress.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = progress
        } else {
            currentList.add(progress)
        }
        _allProgress.value = currentList
    }

    fun clearActiveProgress() {
        _activeProgress.value?.let { progress ->
            val currentList = _allProgress.value.toMutableList()
            currentList.add(progress)
            _allProgress.value = currentList
        }
        _activeProgress.value = null
    }

    suspend fun refreshProgress(): Result<List<PlayerProgress>> {
        return apiService.getProgress().map { progressDtos ->
            val progresses = progressDtos.map { it.toPlayerProgress() }
            _allProgress.value = progresses
            
            val active = progresses.firstOrNull { !it.isCompleted }
            _activeProgress.value = active
            
            progresses
        }
    }
    
    suspend fun getProgressForHunt(huntId: String): Result<PlayerProgress> {
        return apiService.getProgress(huntId).map { it.toPlayerProgress() }
    }

    suspend fun getLeaderboard(huntId: String? = null): Result<List<LeaderboardEntry>> {
        return if (huntId != null) {
            apiService.getLeaderboard(huntId).map { entries ->
                entries.map { it.toLeaderboardEntry() }
            }
        } else {
            apiService.getGlobalLeaderboard().map { entries ->
                entries.map { it.toLeaderboardEntry() }
            }
        }
    }

    fun getPlayerProgress(playerId: String): List<PlayerProgress> {
        return _allProgress.value.filter { it.playerId == playerId }
    }
}
