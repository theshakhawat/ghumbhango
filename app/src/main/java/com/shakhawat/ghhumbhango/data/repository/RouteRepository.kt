package com.shakhawat.ghhumbhango.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class RouteResult(
    val points: List<GeoPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

object RouteRepository {

    private val routeCache = ConcurrentHashMap<String, RouteResult>()

    /**
     * Fetches the real road route connecting start and destination points using OpenStreetMap routing (OSRM).
     * If network fails or offline, provides a clean fallback route.
     */
    suspend fun getRoadRoute(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): RouteResult = withContext(Dispatchers.IO) {
        val key = String.format(Locale.US, "%.4f,%.4f->%.4f,%.4f", startLat, startLon, destLat, destLon)
        routeCache[key]?.let { return@withContext it }

        // 1. Fetch real road geometry from OpenStreetMap OSRM routing service
        val osrmResult = fetchFromOsrm(startLat, startLon, destLat, destLon)
        if (osrmResult != null && osrmResult.points.size >= 2) {
            routeCache[key] = osrmResult
            return@withContext osrmResult
        }

        // 2. Fallback: Straight line with intermediate road point if offline
        val fallbackPoints = listOf(
            GeoPoint(startLat, startLon),
            GeoPoint((startLat + destLat) / 2, (startLon + destLon) / 2),
            GeoPoint(destLat, destLon)
        )
        val fallbackDist = calculateDistanceMeters(startLat, startLon, destLat, destLon)
        val fallbackDuration = fallbackDist / 11.0 // approx 40 km/h
        RouteResult(fallbackPoints, fallbackDist, fallbackDuration)
    }

    private fun fetchFromOsrm(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double
    ): RouteResult? {
        return try {
            val urlString = String.format(
                Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                startLon, startLat, destLon, destLat
            )
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "GhhumBhango-Travel-Alarm/1.0 (contact@ghumbhango.app)")
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                if (root.optString("code") == "Ok") {
                    val routes = root.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val routeObj = routes.getJSONObject(0)
                        val distMeters = routeObj.optDouble("distance", 0.0)
                        val durSecs = routeObj.optDouble("duration", 0.0)
                        val geometry = routeObj.optJSONObject("geometry")
                        val coords = geometry?.optJSONArray("coordinates")
                        if (coords != null && coords.length() > 0) {
                            val points = ArrayList<GeoPoint>(coords.length())
                            for (i in 0 until coords.length()) {
                                val pt = coords.getJSONArray(i)
                                val lon = pt.getDouble(0)
                                val lat = pt.getDouble(1)
                                points.add(GeoPoint(lat, lon))
                            }
                            return RouteResult(points, distMeters, durSecs)
                        }
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

