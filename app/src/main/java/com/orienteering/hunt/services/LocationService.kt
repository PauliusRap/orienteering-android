package com.orienteering.hunt.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.orienteering.hunt.data.models.GeoLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return fineLocation || coarseLocation
    }

    suspend fun getCurrentLocation(): GeoLocation = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermissions()) {
            continuation.resumeWithException(SecurityException("Location permission not granted"))
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            GeoLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                altitude = location.altitude,
                                accuracy = location.accuracy
                            )
                        )
                    } else {
                        continuation.resume(GeoLocation.DEFAULT)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } catch (e: SecurityException) {
            continuation.resumeWithException(e)
        }
    }

    fun getLocationUpdates(intervalMs: Long = 5000): Flow<GeoLocation> = callbackFlow {
        if (!hasLocationPermissions()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMs
        ).apply {
            setMinUpdateDistanceMeters(5f)
            setGranularity(com.google.android.gms.location.Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(true)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(
                        GeoLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracy = location.accuracy
                        )
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                context.mainLooper
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }.distinctUntilChanged()

    fun calculateDistance(
        location1: GeoLocation,
        location2: GeoLocation
    ): Float = location1.distanceTo(location2)

    fun isWithinCheckInRadius(
        currentLocation: GeoLocation,
        targetLocation: GeoLocation,
        radiusMeters: Float = DEFAULT_CHECK_IN_RADIUS
    ): Boolean = currentLocation.isWithinRadius(targetLocation, radiusMeters)

    companion object {
        const val DEFAULT_CHECK_IN_RADIUS = 30f
    }
}
