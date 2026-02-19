package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Clue(
    val id: String,
    val huntLocationId: String,
    val text: String,
    val hint: String? = null,
    val difficulty: ClueDifficulty = ClueDifficulty.MEDIUM
)

enum class ClueDifficulty {
    EASY,
    MEDIUM,
    HARD
}
