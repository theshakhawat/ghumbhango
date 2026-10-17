package com.shakhawat.ghhumbhango.domain.model

/**
 * Represents the intensity tier for alarm audio volume and vibration pattern.
 * Supports adaptive scaling based on distance to the destination.
 */
enum class AlarmIntensity(
    val volumeRatio: Float,
    val vibrationTimings: LongArray,
    val vibrationAmplitudes: IntArray?
) {
    /**
     * Gentle initial warning when just entering the alarm radius (e.g., around 400m for a 500m radius).
     */
    LOW(
        volumeRatio = 0.35f,
        vibrationTimings = longArrayOf(0, 350, 900),
        vibrationAmplitudes = intArrayOf(0, 100, 0)
    ),

    /**
     * Moderate intensity as passenger gets closer (e.g., 200m - 300m for a 500m radius).
     */
    MEDIUM(
        volumeRatio = 0.70f,
        vibrationTimings = longArrayOf(0, 700, 400),
        vibrationAmplitudes = intArrayOf(0, 180, 0)
    ),

    /**
     * Maximum intensity when arriving within 100m or when adaptive alarm is disabled.
     */
    FULL(
        volumeRatio = 1.0f,
        vibrationTimings = longArrayOf(0, 1000, 300, 1000, 300),
        vibrationAmplitudes = intArrayOf(0, 255, 0, 255, 0)
    );

    companion object {
        /**
         * Calculates appropriate alarm intensity based on remaining distance and chosen radius.
         *
         * For 500m radius (user requested):
         * - around 400m (>300m): LOW
         * - 300m - 200m (100m..300m): MEDIUM
         * - within 100m: FULL
         */
        fun calculate(distanceMeters: Number, radiusMeters: Int): AlarmIntensity {
            val d = distanceMeters.toDouble()
            return when {
                d <= 100.0 -> FULL
                radiusMeters == 500 -> {
                    if (d <= 300.0) MEDIUM else LOW
                }
                d <= (radiusMeters * 0.30).coerceAtLeast(100.0) -> FULL
                d <= (radiusMeters * 0.65) -> MEDIUM
                else -> LOW
            }
        }
    }
}
