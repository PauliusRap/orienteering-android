package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProgress(
    val id: String,
    val playerId: String,
    val huntId: String,
    val visitedLocations: Set<String> = emptySet(),
    val currentLocationIndex: Int = 0,
    val earnedPoints: Int = 0,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isCompleted: Boolean = false
) {
    val elapsedTime: Long
        get() = (completedAt ?: System.currentTimeMillis()) - startedAt

    val elapsedTimeFormatted: String
        get() {
            val seconds = elapsedTime / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }

    fun isLocationVisited(locationId: String): Boolean = locationId in visitedLocations

    fun markLocationVisited(locationId: String, points: Int): PlayerProgress {
        return copy(
            visitedLocations = visitedLocations + locationId,
            earnedPoints = earnedPoints + points
        )
    }

    fun advanceToNextLocation(totalLocations: Int): PlayerProgress {
        val nextIndex = (currentLocationIndex + 1).coerceAtMost(totalLocations - 1)
        val completed = nextIndex >= totalLocations - 1 && visitedLocations.size >= totalLocations
        return copy(
            currentLocationIndex = nextIndex,
            isCompleted = completed,
            completedAt = if (completed) System.currentTimeMillis() else null
        )
    }
}
