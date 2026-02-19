package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable

@Serializable
data class HuntLocation(
    val id: String,
    val name: String,
    val description: String,
    val location: GeoLocation,
    val hint: String,
    val points: Int,
    val order: Int,
    val imageUrl: String? = null
)
