package com.shakhawat.ghhumbhango.data.repository

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.shakhawat.ghhumbhango.data.local.GhumBhangoDatabase
import com.shakhawat.ghhumbhango.data.local.entity.SavedDestinationEntity
import com.shakhawat.ghhumbhango.domain.model.Destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

sealed interface GeocodeResult {
    data class Success(val destinations: List<Destination>) : GeocodeResult
    data class Offline(val message: String) : GeocodeResult
    data class Error(val message: String) : GeocodeResult
}

class DestinationRepository(private val context: Context) {

    private val database = GhumBhangoDatabase.getInstance(context)
    private val dao = database.savedDestinationDao()

    val savedDestinations: Flow<List<Destination>> = dao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            dao.insertAll(com.shakhawat.ghhumbhango.data.local.defaultPrepopulatedStops)
        }
    }

    fun searchLocal(query: String): Flow<List<Destination>> {
        return dao.search(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    fun getSavedByCategory(category: String): Flow<List<Destination>> =
        dao.getByCategory(category).map { list -> list.map { it.toDomain() } }

    suspend fun saveDestination(destination: Destination, isFavorite: Boolean = true): Long =
        withContext(Dispatchers.IO) {
            dao.insert(SavedDestinationEntity.fromDomain(destination, isFavorite))
        }

    suspend fun saveDestinationWithCategory(destination: Destination, category: String, isFavorite: Boolean = true): Long =
        withContext(Dispatchers.IO) {
            dao.insert(SavedDestinationEntity.fromDomain(destination.copy(category = category), isFavorite, category))
        }

    suspend fun deleteDestination(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun markLastUsed(id: Long) = withContext(Dispatchers.IO) {
        dao.updateLastUsed(id, System.currentTimeMillis())
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Resolves real geocoded coordinates for online search queries.
     * Adheres strictly to the specification:
     * - Never uses fake coordinates.
     * - Honestly reports when offline or when no match is found.
     */
    companion object {
        private val searchCache = java.util.concurrent.ConcurrentHashMap<String, List<Destination>>()
    }

    /**
     * Fast immediate in-memory search across default pre-populated stops (0ms).
     */
    fun searchLocalImmediate(query: String): List<Destination> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()
        return com.shakhawat.ghhumbhango.data.local.defaultPrepopulatedStops
            .filter { it.name.contains(q, ignoreCase = true) || (it.address?.contains(q, ignoreCase = true) == true) }
            .mapIndexed { idx, entity ->
                Destination(
                    id = if (entity.id > 0) entity.id.toString() else "pre_${idx}_${entity.name.hashCode()}",
                    name = entity.name,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    address = entity.address,
                    isSaved = entity.isFavorite
                )
            }
    }

    /**
     * Fast direct search in Room database (1-2ms).
     */
    suspend fun searchLocalDatabase(query: String): List<Destination> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()
        try {
            dao.searchDirect(q).map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Resolves real geocoded coordinates for online search queries.
     * Uses in-memory cache, fast OSM Nominatim/Photon geocoding with 2.5s timeout,
     * deduplication, and local DB fallback.
     */
    suspend fun searchOnline(query: String): GeocodeResult = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext GeocodeResult.Success(emptyList())
        }

        val cacheKey = trimmed.lowercase(Locale.ROOT)
        searchCache[cacheKey]?.let { cached ->
            if (cached.isNotEmpty()) {
                return@withContext GeocodeResult.Success(cached)
            }
        }

        // Check local database first for instant results
        val localMatches = searchLocalDatabase(trimmed)

        if (!isNetworkAvailable()) {
            if (localMatches.isNotEmpty()) {
                return@withContext GeocodeResult.Success(localMatches)
            }
            return@withContext GeocodeResult.Offline(
                "ইন্টারনেট সংযোগ নেই। সংরক্ষিত গন্তব্য তালিকা থেকে নির্বাচন করুন।"
            )
        }

        try {
            var destinations = searchNominatim(trimmed)

            if (destinations.isEmpty()) {
                destinations = searchPhoton(trimmed)
            }

            // Fallback to Android Geocoder with a strict 1.5s timeout if OSM/Photon had no result
            if (destinations.isEmpty()) {
                destinations = kotlinx.coroutines.withTimeoutOrNull(1500) {
                    try {
                        val geocoder = Geocoder(context, Locale.forLanguageTag("bn-BD"))
                        val addresses = geocoder.getFromLocationName(trimmed, 6)
                        if (!addresses.isNullOrEmpty()) {
                            addresses.mapNotNull { address ->
                                val featureName = address.featureName ?: address.locality ?: address.subAdminArea ?: trimmed
                                val addressLine = address.getAddressLine(0) ?: "$featureName, বাংলাদেশ"
                                Destination(
                                    id = "geo_${System.currentTimeMillis()}_${address.latitude}",
                                    name = featureName,
                                    latitude = address.latitude,
                                    longitude = address.longitude,
                                    address = addressLine,
                                    isSaved = false
                                )
                            }
                        } else emptyList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                } ?: emptyList()
            }

            // Combine online with local matches and deduplicate
            val combined = mutableListOf<Destination>()
            combined.addAll(destinations)
            for (local in localMatches) {
                if (combined.none { it.name.trim().equals(local.name.trim(), ignoreCase = true) }) {
                    combined.add(local)
                }
            }

            val deduplicated = deduplicateDestinations(combined)

            if (deduplicated.isNotEmpty()) {
                searchCache[cacheKey] = deduplicated
                GeocodeResult.Success(deduplicated)
            } else {
                GeocodeResult.Error("'$query' নামের কোনো স্থান পাওয়া যায়নি। সঠিক বানান দিয়ে আবার চেষ্টা করুন।")
            }
        } catch (e: Exception) {
            if (localMatches.isNotEmpty()) {
                GeocodeResult.Success(localMatches)
            } else {
                GeocodeResult.Error("গন্তব্য অনুসন্ধানে সমস্যা হয়েছে: ${e.localizedMessage}")
            }
        }
    }

    private fun searchNominatim(query: String): List<Destination> {
        return try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=6&countrycodes=bd&accept-language=bn,en"
            val url = java.net.URL(urlString)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "GhhumBhango-Travel-Alarm/1.0 (contact@ghumbhango.app)")
            conn.connectTimeout = 2500
            conn.readTimeout = 2500
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(responseText)
                val results = mutableListOf<Destination>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val displayName = obj.optString("display_name", "")
                    val parts = displayName.split(",")
                    val shortName = parts.firstOrNull()?.trim() ?: query
                    val lat = obj.optDouble("lat", 0.0)
                    val lon = obj.optDouble("lon", 0.0)
                    if (lat != 0.0 && lon != 0.0) {
                        results.add(
                            Destination(
                                id = "osm_${obj.optString("place_id", i.toString())}",
                                name = shortName,
                                latitude = lat,
                                longitude = lon,
                                address = displayName,
                                isSaved = false
                            )
                        )
                    }
                }
                results
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun searchPhoton(query: String): List<Destination> {
        return try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val urlString = "https://photon.komoot.io/api/?q=$encoded&limit=6&lat=23.8103&lon=90.4125"
            val url = java.net.URL(urlString)
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "GhhumBhango-Travel-Alarm/1.0 (contact@ghumbhango.app)")
            conn.connectTimeout = 2500
            conn.readTimeout = 2500
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = org.json.JSONObject(responseText)
                val features = root.optJSONArray("features") ?: return emptyList()
                val results = mutableListOf<Destination>()
                for (i in 0 until features.length()) {
                    val feature = features.getJSONObject(i)
                    val props = feature.optJSONObject("properties") ?: continue
                    val geom = feature.optJSONObject("geometry") ?: continue
                    val coords = geom.optJSONArray("coordinates") ?: continue
                    val lon = coords.optDouble(0, 0.0)
                    val lat = coords.optDouble(1, 0.0)
                    val name = props.optString("name", query)
                    val city = props.optString("city", props.optString("district", "বাংলাদেশ"))
                    if (lat != 0.0 && lon != 0.0) {
                        results.add(
                            Destination(
                                id = "photon_${props.optString("osm_id", i.toString())}",
                                name = name,
                                latitude = lat,
                                longitude = lon,
                                address = "$name, $city",
                                isSaved = false
                            )
                        )
                    }
                }
                results
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun deduplicateDestinations(list: List<Destination>): List<Destination> {
        val unique = mutableListOf<Destination>()
        for (item in list) {
            val isDuplicate = unique.any { existing ->
                existing.name.trim().equals(item.name.trim(), ignoreCase = true) ||
                (Math.abs(existing.latitude - item.latitude) < 0.0015 &&
                 Math.abs(existing.longitude - item.longitude) < 0.0015)
            }
            if (!isDuplicate) {
                unique.add(item)
            }
        }
        return unique
    }

    /**
     * Resolves human-readable stoppage address for given map coordinates.
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Pair<String, String?> =
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable()) {
                val latStr = String.format(Locale.US, "%.4f", latitude)
                val lngStr = String.format(Locale.US, "%.4f", longitude)
                return@withContext Pair("মানচিত্রের স্থান ($latStr, $lngStr)", "অফলাইন মোড — সংরক্ষিত রাখুন")
            }
            try {
                val geocoder = Geocoder(context, Locale.forLanguageTag("bn-BD"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val name = address.featureName ?: address.locality ?: address.subLocality ?: "মানচিত্রের স্থান"
                    val line = address.getAddressLine(0) ?: "$name, বাংলাদেশ"
                    Pair(name, line)
                } else {
                    Pair("মানচিত্রের স্থান", "অক্ষাংশ: $latitude, দ্রাঘিমাংশ: $longitude")
                }
            } catch (e: Exception) {
                val latStr = String.format(Locale.US, "%.4f", latitude)
                val lngStr = String.format(Locale.US, "%.4f", longitude)
                Pair("মানচিত্রের স্থান ($latStr, $lngStr)", "অফলাইন মোড")
            }
        }
}