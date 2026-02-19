package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val id: String,
    val username: String,
    val displayName: String,
    val email: String = "",
    val isAdmin: Boolean = false,
    val avatarUrl: String? = null,
    val totalPoints: Int = 0,
    val completedHunts: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
