package com.shakhawat.ghhumbhango.domain.model

/**
 * Supported display theme modes for GhhumBhango:
 * SYSTEM: Matches the device OS dark/light mode
 * LIGHT: Forces clean bright theme
 * DARK: Forces deep night/navy theme
 */
enum class AppThemeMode(val id: String) {
    SYSTEM("SYSTEM"),
    LIGHT("LIGHT"),
    DARK("DARK");

    fun getLocalizedName(language: AppLanguage): String {
        return when (this) {
            SYSTEM -> if (language == AppLanguage.BANGLA) "সিস্টেম ডিফল্ট" else "System Default"
            LIGHT -> if (language == AppLanguage.BANGLA) "লাইট মোড" else "Light Mode"
            DARK -> if (language == AppLanguage.BANGLA) "ডার্ক মোড" else "Dark Mode"
        }
    }

    companion object {
        fun fromId(id: String?): AppThemeMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LIGHT
        }
    }
}

