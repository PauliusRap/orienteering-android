package com.orienteering.hunt.data.models

import kotlinx.serialization.Serializable
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f
) {
    fun distanceTo(other: GeoLocation): Float {
        val earthRadius = 6371000.0

        val lat1Rad = Math.toRadians(latitude)
        val lat2Rad = Math.toRadians(other.latitude)
        val deltaLat = Math.toRadians(other.latitude - latitude)
        val deltaLon = Math.toRadians(other.longitude - longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    fun isWithinRadius(other: GeoLocation, radiusMeters: Float): Boolean {
        return distanceTo(other) <= radiusMeters
    }

    companion object {
        val DEFAULT = GeoLocation(
            latitude = 37.7749,
            longitude = -122.4194
        )
    }
}
