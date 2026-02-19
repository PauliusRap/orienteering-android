package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Hunt(
    val id: String,
    val name: String,
    val description: String,
    val locations: List<HuntLocation>,
    val clues: List<Clue>,
    val totalPoints: Int,
    val estimatedDurationMinutes: Int,
    val difficulty: HuntDifficulty = HuntDifficulty.INTERMEDIATE,
    val imageUrl: String? = null,
    val isActive: Boolean = true
) {
    val locationCount: Int get() = locations.size
}

enum class HuntDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}
