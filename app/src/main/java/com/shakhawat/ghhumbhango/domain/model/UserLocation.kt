package com.shakhawat.ghhumbhango.domain.model

/**
 * Domain representation of user's real GPS fix.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isHighAccuracy: Boolean
        get() = accuracyMeters in 0.1f..30.0f

    val isMediumAccuracy: Boolean
        get() = accuracyMeters in 30.1f..70.0f

    val isPoorAccuracy: Boolean
        get() = accuracyMeters > 70.0f

    val formattedAccuracy: String
        get() = when {
            isHighAccuracy -> "উচ্চ সঠিকতা (±${accuracyMeters.toInt()} মি.)"
            isMediumAccuracy -> "মাঝারি সঠিকতা (±${accuracyMeters.toInt()} মি.)"
            else -> "দুর্বল সিগন্যাল (±${accuracyMeters.toInt()} মি.)"
        }
}

