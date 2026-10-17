package com.shakhawat.ghhumbhango.domain.model

import androidx.annotation.RawRes
import com.shakhawat.ghhumbhango.R

/**
 * Available alarm tone presets with display labels and offline raw resources.
 */
enum class AlarmSound(
    val id: String,
    val displayLabel: String,
    @field:RawRes val rawResId: Int
) {
    DEFAULT(
        id = "DEFAULT",
        displayLabel = "ডিফল্ট অ্যালার্ম",
        rawResId = R.raw.alarm_default
    ),
    BUS_HORN(
        id = "BUS_HORN",
        displayLabel = "বাস হর্ন ও মেলোডি",
        rawResId = R.raw.alarm_bus_horn
    ),
    GENTLE_BELL(
        id = "GENTLE_BELL",
        displayLabel = "মৃদু ঘণ্টার ধ্বনি",
        rawResId = R.raw.alarm_gentle_bell
    ),
    TRAIN_WHISTLE(
        id = "TRAIN_WHISTLE",
        displayLabel = "ট্রেন হুইসেল",
        rawResId = R.raw.alarm_train_whistle
    ),
    CUSTOM(
        id = "CUSTOM",
        displayLabel = "কাস্টম অডিও / রিংটোন",
        rawResId = 0
    );

    fun getLocalizedName(language: AppLanguage): String {
        return when (this) {
            DEFAULT -> if (language == AppLanguage.ENGLISH) "Default Alarm" else "ডিফল্ট অ্যালার্ম"
            BUS_HORN -> if (language == AppLanguage.ENGLISH) "Bus Horn & Melody" else "বাস হর্ন ও মেলোডি"
            GENTLE_BELL -> if (language == AppLanguage.ENGLISH) "Gentle Bell Tone" else "মৃদু ঘণ্টার ধ্বনি"
            TRAIN_WHISTLE -> if (language == AppLanguage.ENGLISH) "Train Whistle" else "ট্রেন হুইসেল"
            CUSTOM -> if (language == AppLanguage.ENGLISH) "Custom Audio / Ringtone" else "কাস্টম অডিও / রিংটোন"
        }
    }

    companion object {
        val DEFAULT_SOUND = DEFAULT

        fun fromId(id: String?): AlarmSound {
            if (id.isNullOrBlank()) return DEFAULT_SOUND
            return entries.find { it.id.equals(id, ignoreCase = true) }
                ?: entries.find { it.displayLabel == id }
                ?: DEFAULT_SOUND
        }

        fun fromDisplayLabel(label: String?): AlarmSound {
            if (label.isNullOrBlank()) return DEFAULT_SOUND
            return entries.find { it.displayLabel == label }
                ?: fromId(label)
        }
    }
}

