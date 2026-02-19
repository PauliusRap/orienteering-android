package com.orienteering.hunt.data.api

import com.orienteering.hunt.data.models.Clue
import com.orienteering.hunt.data.models.ClueDifficulty
import com.orienteering.hunt.data.models.GeoLocation
import com.orienteering.hunt.data.models.Hunt
import com.orienteering.hunt.data.models.HuntDifficulty
import com.orienteering.hunt.data.models.HuntLocation
import com.orienteering.hunt.data.models.LeaderboardEntry
import com.orienteering.hunt.data.models.Player
import com.orienteering.hunt.data.models.PlayerProgress

fun UserDto.toPlayer(): Player {
    return Player(
        id = id,
        username = username,
        displayName = displayName ?: username,
        email = email,
        isAdmin = isAdmin,
        avatarUrl = avatarUrl,
        totalPoints = totalPoints,
        completedHunts = completedHunts,
        createdAt = createdAt?.let { parseIsoDate(it) } ?: System.currentTimeMillis()
    )
}

fun HuntDto.toHunt(): Hunt {
    return Hunt(
        id = id,
        name = name,
        description = description,
        locations = locations.map { it.toHuntLocation() },
        clues = clues.map { it.toClue() },
        totalPoints = totalPoints,
        estimatedDurationMinutes = estimatedDurationMinutes,
        difficulty = difficulty.toHuntDifficulty(),
        imageUrl = imageUrl,
        isActive = isActive
    )
}

fun HuntLocationDto.toHuntLocation(): HuntLocation {
    return HuntLocation(
        id = id,
        name = name,
        description = description,
        location = location.toGeoLocation(),
        hint = hint ?: "",
        points = points,
        order = order
    )
}

fun GeoLocationDto.toGeoLocation(): GeoLocation {
    return GeoLocation(latitude = latitude, longitude = longitude)
}

fun ClueDto.toClue(): Clue {
    return Clue(
        id = id,
        huntLocationId = huntLocationId,
        text = text,
        hint = hint,
        difficulty = difficulty.toClueDifficulty()
    )
}

fun ProgressDto.toPlayerProgress(): PlayerProgress {
    return PlayerProgress(
        id = id,
        playerId = playerId,
        huntId = huntId,
        visitedLocations = visitedLocations.toSet(),
        currentLocationIndex = currentLocationIndex,
        earnedPoints = earnedPoints,
        startedAt = parseIsoDate(startedAt),
        completedAt = completedAt?.let { parseIsoDate(it) },
        isCompleted = isCompleted
    )
}

fun LeaderboardEntryDto.toLeaderboardEntry(): LeaderboardEntry {
    return LeaderboardEntry(
        rank = rank,
        playerId = playerId,
        playerName = playerName,
        playerAvatarUrl = playerAvatarUrl,
        huntId = huntId ?: "",
        huntName = huntName ?: "",
        score = score,
        completionTime = completionTime ?: 0L,
        completedAt = completedAt?.let { parseIsoDate(it) } ?: System.currentTimeMillis()
    )
}

fun String.toHuntDifficulty(): HuntDifficulty {
    return when (this.uppercase()) {
        "BEGINNER" -> HuntDifficulty.BEGINNER
        "INTERMEDIATE" -> HuntDifficulty.INTERMEDIATE
        "ADVANCED" -> HuntDifficulty.ADVANCED
        "EXPERT" -> HuntDifficulty.EXPERT
        else -> HuntDifficulty.INTERMEDIATE
    }
}

fun String.toClueDifficulty(): ClueDifficulty {
    return when (this.uppercase()) {
        "EASY" -> ClueDifficulty.EASY
        "MEDIUM" -> ClueDifficulty.MEDIUM
        "HARD" -> ClueDifficulty.HARD
        else -> ClueDifficulty.MEDIUM
    }
}

fun parseIsoDate(isoString: String): Long {
    return try {
        val cleanString = isoString
            .replace("Z", "+00:00")
            .substringBefore(".")
            .substringBefore("+")
        val parts = cleanString.split("T", "-", ":")
        if (parts.size >= 6) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            val hour = parts[3].toInt()
            val minute = parts[4].toInt()
            val second = parts[5].toInt()
            java.util.Calendar.getInstance().apply {
                set(year, month - 1, day, hour, minute, second)
            }.timeInMillis
        } else {
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
