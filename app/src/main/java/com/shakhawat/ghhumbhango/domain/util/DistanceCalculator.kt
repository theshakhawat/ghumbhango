package com.shakhawat.ghhumbhango.domain.util

import android.location.Location
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import java.util.Locale

/**
 * Result of GPS proximity evaluation.
 */
data class ProximityEvaluation(
    val distanceMeters: Float,
    val isWithinRadius: Boolean,
    val isReliable: Boolean,
    val uncertaintyMeters: Float,
    val formattedDistance: String
)

/**
 * 100% offline geodesic distance calculator using WGS-84 ellipsoid and Bengali numeral formatting.
 */
object DistanceCalculator {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    /**
     * Calculates WGS-84 geodesic distance between two GPS coordinate pairs in meters.
     * Requires NO network connectivity.
     */
    fun calculateDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(startLat, startLng, endLat, endLng, results)
        return results[0]
    }

    fun calculateDistance(userLocation: UserLocation, destination: Destination): Float {
        return calculateDistanceMeters(
            startLat = userLocation.latitude,
            startLng = userLocation.longitude,
            endLat = destination.latitude,
            endLng = destination.longitude
        )
    }

    /**
     * Evaluates proximity with GPS accuracy verification to prevent false alarms.
     */
    fun evaluateProximity(
        userLocation: UserLocation,
        destination: Destination,
        alarmRadiusMeters: Int
    ): ProximityEvaluation {
        val rawDistance = calculateDistance(userLocation, destination)
        val accuracy = userLocation.accuracyMeters

        // A reading is considered reliable if GPS accuracy is within reasonable threshold (< 75m)
        val isReliable = accuracy in 0.1f..75.0f
        val isWithin = rawDistance <= alarmRadiusMeters

        return ProximityEvaluation(
            distanceMeters = rawDistance,
            isWithinRadius = isWithin,
            isReliable = isReliable,
            uncertaintyMeters = accuracy,
            formattedDistance = formatDistance(rawDistance)
        )
    }

    /**
     * Formats distance into localized representation (Bengali by default).
     * E.g. 450m -> "৪৫০ মিটার", 1250m -> "১.৩ কি.মি."
     */
    fun formatDistance(meters: Float, useBengali: Boolean = true): String {
        return LocalizationHelper.formatDistance(meters, useBengali)
    }

    fun toBengaliDigits(number: Int): String = LocalizationHelper.toLocalizedDigits(number, true)

    fun toBengaliDigits(number: Long): String = LocalizationHelper.toLocalizedDigits(number, true)

    fun toBengaliDigits(input: String): String = LocalizationHelper.toLocalizedDigits(input, true)
}

