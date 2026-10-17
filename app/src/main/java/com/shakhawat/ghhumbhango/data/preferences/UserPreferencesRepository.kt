package com.shakhawat.ghhumbhango.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ghumbhango_user_prefs")

/**
 * DataStore-backed repository managing application preferences and configurations.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DEFAULT_ALARM_DISTANCE = intPreferencesKey("default_alarm_distance_meters")
        val IS_CUSTOM_DISTANCE_SET = booleanPreferencesKey("is_custom_distance_set")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val VOLUME_RAMPING_ENABLED = booleanPreferencesKey("volume_ramping_enabled")
        val ADAPTIVE_ALARM_ENABLED = booleanPreferencesKey("adaptive_alarm_intensity_enabled")
        val SNOOZE_DURATION_MINUTES = intPreferencesKey("snooze_duration_minutes")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on_tracking")
        val USE_BENGALI_NUMERALS = booleanPreferencesKey("use_bengali_numerals")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_PERMISSION_COMPLETED = booleanPreferencesKey("is_permission_completed")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound_id")
        val CUSTOM_ALARM_URI = stringPreferencesKey("custom_alarm_uri")
        val CUSTOM_ALARM_NAME = stringPreferencesKey("custom_alarm_name")
        val APP_LANGUAGE = stringPreferencesKey("app_language_code")
        val CUSTOM_CATEGORIES = stringSetPreferencesKey("custom_user_categories")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val isCustomSet = preferences[PreferencesKeys.IS_CUSTOM_DISTANCE_SET] ?: false
            val alarmMeters = if (!isCustomSet) {
                AlarmDistance.DEFAULT.meters
            } else {
                preferences[PreferencesKeys.DEFAULT_ALARM_DISTANCE] ?: AlarmDistance.DEFAULT.meters
            }
            val isVibration = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true
            val isVolumeRamping = preferences[PreferencesKeys.VOLUME_RAMPING_ENABLED] ?: true
            val isAdaptiveAlarm = preferences[PreferencesKeys.ADAPTIVE_ALARM_ENABLED] ?: false
            val snoozeMinutes = preferences[PreferencesKeys.SNOOZE_DURATION_MINUTES] ?: 2
            val isKeepScreenOn = preferences[PreferencesKeys.KEEP_SCREEN_ON] ?: false
            val useBengaliNumerals = preferences[PreferencesKeys.USE_BENGALI_NUMERALS] ?: true
            val themeModeId = preferences[PreferencesKeys.THEME_MODE]
            val themeMode = com.shakhawat.ghhumbhango.domain.model.AppThemeMode.fromId(themeModeId)
            val soundId = preferences[PreferencesKeys.ALARM_SOUND]
            val sound = com.shakhawat.ghhumbhango.domain.model.AlarmSound.fromId(soundId)
            val customAlarmUri = preferences[PreferencesKeys.CUSTOM_ALARM_URI]
            val customAlarmName = preferences[PreferencesKeys.CUSTOM_ALARM_NAME]
            val langCode = preferences[PreferencesKeys.APP_LANGUAGE]
            val language = com.shakhawat.ghhumbhango.domain.model.AppLanguage.fromCode(langCode)

            UserSettings(
                defaultAlarmDistance = AlarmDistance.fromMeters(alarmMeters),
                isVibrationEnabled = isVibration,
                isVolumeRampingEnabled = isVolumeRamping,
                isAdaptiveAlarmEnabled = isAdaptiveAlarm,
                snoozeDurationMinutes = snoozeMinutes,
                isKeepScreenOn = isKeepScreenOn,
                useBengaliNumerals = useBengaliNumerals,
                themeMode = themeMode,
                alarmSound = sound,
                customAlarmUri = customAlarmUri,
                customAlarmName = customAlarmName,
                appLanguage = language
            )
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
        }

    val isPermissionCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_PERMISSION_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setPermissionCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PERMISSION_COMPLETED] = completed
        }
    }

    suspend fun setDefaultAlarmDistance(distance: AlarmDistance) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ALARM_DISTANCE] = distance.meters
            preferences[PreferencesKeys.IS_CUSTOM_DISTANCE_SET] = true
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setVolumeRampingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOLUME_RAMPING_ENABLED] = enabled
        }
    }

    suspend fun setAdaptiveAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ADAPTIVE_ALARM_ENABLED] = enabled
        }
    }

    suspend fun setSnoozeDurationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SNOOZE_DURATION_MINUTES] = minutes
        }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_SCREEN_ON] = enabled
        }
    }

    suspend fun setUseBengaliNumerals(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_BENGALI_NUMERALS] = enabled
        }
    }

    suspend fun setThemeMode(mode: com.shakhawat.ghhumbhango.domain.model.AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.id
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setAlarmSound(sound: com.shakhawat.ghhumbhango.domain.model.AlarmSound) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARM_SOUND] = sound.id
        }
    }

    suspend fun setCustomAlarmTone(uri: String?, name: String?) {
        context.dataStore.edit { preferences ->
            if (uri != null) {
                preferences[PreferencesKeys.CUSTOM_ALARM_URI] = uri
            } else {
                preferences.remove(PreferencesKeys.CUSTOM_ALARM_URI)
            }
            if (name != null) {
                preferences[PreferencesKeys.CUSTOM_ALARM_NAME] = name
            } else {
                preferences.remove(PreferencesKeys.CUSTOM_ALARM_NAME)
            }
            preferences[PreferencesKeys.ALARM_SOUND] = com.shakhawat.ghhumbhango.domain.model.AlarmSound.CUSTOM.id
        }
    }

    suspend fun setAppLanguage(language: com.shakhawat.ghhumbhango.domain.model.AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = language.code
        }
    }

    val customCategories: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            prefs[PreferencesKeys.CUSTOM_CATEGORIES] ?: setOf("বাসা", "কাজ")
        }

    suspend fun addCustomCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_CATEGORIES] ?: setOf("বাসা", "কাজ")
            preferences[PreferencesKeys.CUSTOM_CATEGORIES] = current + trimmed
        }
    }

    suspend fun removeCustomCategory(category: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_CATEGORIES] ?: setOf("বাসা", "কাজ")
            preferences[PreferencesKeys.CUSTOM_CATEGORIES] = current - category
        }
    }

    val recentSearches: Flow<List<Destination>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val raw = prefs[PreferencesKeys.RECENT_SEARCHES] ?: ""
            if (raw.isBlank()) emptyList()
            else {
                try {
                    raw.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
                        val parts = line.split(";;")
                        if (parts.size >= 4) {
                            Destination(
                                id = parts[0],
                                name = parts[1],
                                latitude = parts[2].toDoubleOrNull() ?: 0.0,
                                longitude = parts[3].toDoubleOrNull() ?: 0.0,
                                address = parts.getOrNull(4),
                                isSaved = false
                            )
                        } else null
                    }
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun addRecentSearch(dest: Destination) {
        if (dest.name.isBlank()) return
        context.dataStore.edit { preferences ->
            val raw = preferences[PreferencesKeys.RECENT_SEARCHES] ?: ""
            val current = if (raw.isBlank()) mutableListOf() else raw.split("\n").filter { it.isNotBlank() }.toMutableList()
            val entry = "${dest.id};;${dest.name};;${dest.latitude};;${dest.longitude};;${dest.address ?: ""}"
            current.removeAll { it.contains(";;${dest.name};;") }
            current.add(0, entry)
            val limited = current.take(8).joinToString("\n")
            preferences[PreferencesKeys.RECENT_SEARCHES] = limited
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.RECENT_SEARCHES)
        }
    }

    companion object {
        @Volatile
        private var instance: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}

