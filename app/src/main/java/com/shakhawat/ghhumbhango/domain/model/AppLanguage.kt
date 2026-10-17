package com.shakhawat.ghhumbhango.domain.model

/**
 * Supported in-app languages for GhumBhango.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String
) {
    BANGLA(
        code = "bn",
        nativeName = "বাংলা",
        englishName = "Bengali"
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English"
    );

    companion object {
        val DEFAULT = BANGLA

        fun fromCode(code: String?): AppLanguage {
            if (code.isNullOrBlank()) return DEFAULT
            return entries.find { it.code.equals(code, ignoreCase = true) }
                ?: entries.find { it.name.equals(code, ignoreCase = true) }
                ?: DEFAULT
        }
    }
}

