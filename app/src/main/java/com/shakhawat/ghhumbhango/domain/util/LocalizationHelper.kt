package com.shakhawat.ghhumbhango.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Centralized localization utility ensuring consistent Bengali/English numeral formatting,
 * date/time localization, and transit distance displays according to user settings.
 */
object LocalizationHelper {

    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    /**
     * Converts ASCII digits 0-9 into Bengali Unicode numerals ০-৯ if [useBengali] is true.
     */
    fun toLocalizedDigits(input: String, useBengali: Boolean = true): String {
        if (!useBengali) return input
        val builder = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                builder.append(bengaliDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }

    fun toLocalizedDigits(number: Number, useBengali: Boolean = true): String {
        return toLocalizedDigits(number.toString(), useBengali)
    }

    /**
     * Formats distance in meters into localized human-readable string.
     * E.g. 450m -> "৪৫০ মিটার" / "450 m"
     * 1450m -> "১.৫ কি.মি." / "1.5 km"
     */
    fun formatDistance(meters: Float, useBengali: Boolean = true): String {
        return if (meters < 1000f) {
            val rounded = meters.toInt()
            if (useBengali) {
                "${toLocalizedDigits(rounded, true)} মিটার"
            } else {
                "$rounded meters"
            }
        } else {
            val km = meters / 1000f
            val formattedKm = String.format(Locale.US, "%.1f", km)
            if (useBengali) {
                "${toLocalizedDigits(formattedKm, true)} কি.মি."
            } else {
                "$formattedKm km"
            }
        }
    }

    /**
     * Formats duration in minutes into localized representation.
     * E.g. 25 -> "২৫ মিনিট" / "25 mins"
     */
    fun formatDurationMinutes(minutes: Long, useBengali: Boolean = true): String {
        val safeMinutes = maxOf(1L, minutes)
        return if (useBengali) {
            "${toLocalizedDigits(safeMinutes, true)} মিনিট"
        } else {
            "$safeMinutes min${if (safeMinutes > 1L) "s" else ""}"
        }
    }

    /**
     * Formats timestamp into localized date-time representation.
     * E.g. "১৭ অক্টো, ১১:৪৫ AM"
     */
    fun formatTimestamp(millis: Long, useBengali: Boolean = true): String {
        val date = Date(millis)
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.US)
        val formatted = sdf.format(date)
        return toLocalizedDigits(formatted, useBengali)
    }

    /**
     * Formats geographic coordinates into high-precision localized text.
     */
    fun formatCoordinates(latitude: Double, longitude: Double, useBengali: Boolean = true): String {
        val formattedLat = String.format(Locale.US, "%.4f", latitude)
        val formattedLng = String.format(Locale.US, "%.4f", longitude)
        val text = "$formattedLat° N, $formattedLng° E"
        return toLocalizedDigits(text, useBengali)
    }
}

