package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val playerId: String,
    val playerName: String,
    val playerAvatarUrl: String?,
    val huntId: String,
    val huntName: String,
    val score: Int,
    val completionTime: Long,
    val completedAt: Long
) {
    val completionTimeFormatted: String
        get() {
            val seconds = completionTime / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }
}
