package com.shakhawat.ghhumbhango.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class DefaultLocationClient(
    private val context: Context
) : LocationClient {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    override fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isGpsEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): UserLocation? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val spd = if (location.hasSpeed() && location.speed >= 0.7f) location.speed else 0f
                        continuation.resume(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracyMeters = location.accuracy,
                                speedMps = spd,
                                timestamp = location.time
                            )
                        )
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalMs: Long): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            throw LocationClient.LocationException("লোকেশন পারমিশন দেওয়া হয়নি")
        }

        if (!isGpsEnabled()) {
            throw LocationClient.LocationException("জিপিএস নিষ্ক্রিয় রয়েছে")
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMinUpdateDistanceMeters(0f)
            .build()

        val locationCallback = object : LocationCallback() {
            private var lastLoc: android.location.Location? = null

            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { location ->
                    val ageMs = System.currentTimeMillis() - location.time
                    if (ageMs < 120_000) {
                        var speedMps: Float? = if (location.hasSpeed()) location.speed else null
                        val prev = lastLoc
                        if (speedMps == null) {
                            if (prev != null) {
                                val timeDiffSec = (location.time - prev.time) / 1000f
                                if (timeDiffSec in 0.5f..15.0f) {
                                    val dist = location.distanceTo(prev)
                                    speedMps = if (dist < 1.2f) 0f else (dist / timeDiffSec)
                                }
                            } else {
                                speedMps = 0f
                            }
                        }

                        // Filter out small GPS drift when standing still (< 0.7 m/s is ~2.5 km/h)
                        val cleanSpeed = if (speedMps != null && speedMps < 0.7f) 0f else (speedMps ?: 0f)
                        lastLoc = location

                        trySend(
                            UserLocation(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracyMeters = location.accuracy,
                                speedMps = cleanSpeed,
                                timestamp = location.time
                            )
                        )
                    }
                }
            }
        }

        fusedClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedClient.removeLocationUpdates(locationCallback)
        }
    }
}

