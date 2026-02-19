package com.orienteering.hunt.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Auth DTOs
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("total_points")
    val totalPoints: Int = 0,
    @SerialName("completed_hunts")
    val completedHunts: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null
)

// Profile Update DTOs
@Serializable
data class UpdateProfileRequest(
    val username: String? = null,
    val email: String? = null
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("oldPassword")
    val oldPassword: String,
    @SerialName("newPassword")
    val newPassword: String
)

// Hunt DTOs
@Serializable
data class HuntDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("total_points")
    val totalPoints: Int,
    @SerialName("estimated_duration_minutes")
    val estimatedDurationMinutes: Int,
    val difficulty: String = "INTERMEDIATE",
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    val locations: List<HuntLocationDto> = emptyList(),
    val clues: List<ClueDto> = emptyList()
)

@Serializable
data class HuntLocationDto(
    val id: String,
    val name: String,
    val description: String,
    val location: GeoLocationDto,
    val hint: String? = null,
    val points: Int,
    val order: Int
)

@Serializable
data class GeoLocationDto(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class ClueDto(
    val id: String,
    @SerialName("hunt_location_id")
    val huntLocationId: String,
    val text: String,
    val hint: String? = null,
    val difficulty: String = "MEDIUM"
)

// Progress DTOs
@Serializable
data class ProgressDto(
    val id: String,
    @SerialName("player_id")
    val playerId: String,
    @SerialName("hunt_id")
    val huntId: String,
    @SerialName("visited_locations")
    val visitedLocations: List<String> = emptyList(),
    @SerialName("current_location_index")
    val currentLocationIndex: Int = 0,
    @SerialName("earned_points")
    val earnedPoints: Int = 0,
    @SerialName("started_at")
    val startedAt: String,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("is_completed")
    val isCompleted: Boolean = false,
    val hunt: HuntDto? = null
)

@Serializable
data class CheckInRequest(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class CheckInResponse(
    val success: Boolean,
    val message: String,
    val progress: ProgressDto? = null,
    @SerialName("points_earned")
    val pointsEarned: Int = 0
)

// Leaderboard DTOs
@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    @SerialName("player_id")
    val playerId: String,
    @SerialName("player_name")
    val playerName: String,
    @SerialName("player_avatar_url")
    val playerAvatarUrl: String? = null,
    @SerialName("hunt_id")
    val huntId: String? = null,
    @SerialName("hunt_name")
    val huntName: String? = null,
    val score: Int,
    @SerialName("completion_time")
    val completionTime: Long? = null,
    @SerialName("completed_at")
    val completedAt: String? = null
)

// Error response
@Serializable
data class ApiError(
    val error: String,
    val message: String? = null
)
