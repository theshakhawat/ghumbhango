package com.shakhawat.ghhumbhango.domain.model

/**
 * Domain representation of persistent user preferences stored in Jetpack DataStore.
 */
data class UserSettings(
    val defaultAlarmDistance: AlarmDistance = AlarmDistance.DEFAULT,
    val isVibrationEnabled: Boolean = true,
    val isVolumeRampingEnabled: Boolean = true,
    val isAdaptiveAlarmEnabled: Boolean = false,
    val snoozeDurationMinutes: Int = 2,
    val isKeepScreenOn: Boolean = false,
    val useBengaliNumerals: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val alarmSound: AlarmSound = AlarmSound.DEFAULT,
    val customAlarmUri: String? = null,
    val customAlarmName: String? = null,
    val appLanguage: AppLanguage = AppLanguage.BANGLA
)

