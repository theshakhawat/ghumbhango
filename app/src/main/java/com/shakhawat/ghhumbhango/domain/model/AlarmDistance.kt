package com.shakhawat.ghhumbhango.domain.model

/**
 * Configurable alarm proximity distances for journey tracking.
 * Supported options: 100m, 200m (default), 500m, and 1km.
 */
enum class AlarmDistance(
    val meters: Int,
    val displayLabel: String,
    val shortLabel: String,
    val description: String
) {
    M_100(
        meters = 100,
        displayLabel = "১০০ মিটার",
        shortLabel = "১০০ মি.",
        description = "গন্তব্যের খুব কাছে (১০০ মিটার) পৌঁছালে অ্যালার্ম বাজবে। লোকাল বাসে বা দ্রুত নামার জন্য উপযুক্ত।"
    ),
    M_200(
        meters = 200,
        displayLabel = "২০০ মিটার",
        shortLabel = "২০০ মি.",
        description = "গন্তব্যের ২০০ মিটার আগে সতর্ক করা হবে। মাঝারি ভিড়ে নামার প্রস্তুতি নিতে সহায়ক।"
    ),
    M_500(
        meters = 500,
        displayLabel = "৫০০ মিটার",
        shortLabel = "৫০০ মি.",
        description = "গন্তব্যের ৫০০ মিটার আগেই অ্যালার্ম বাজবে। ঘুম থেকে উঠে ব্যাগ গুছিয়ে নামার জন্য সবচেয়ে আদর্শ।"
    ),
    KM_1(
        meters = 1000,
        displayLabel = "১ কিলোমিটার",
        shortLabel = "১ কি.মি.",
        description = "গন্তব্যের ১ কিমি আগে সতর্ক করা হবে। হাইওয়ে বাস, এক্সপ্রেস ও দূরপাল্লার ট্রেনের জন্য সেরা।"
    );

    fun getLocalizedLabel(language: AppLanguage): String {
        return when (this) {
            M_100 -> if (language == AppLanguage.ENGLISH) "100 Meters" else "১০০ মিটার"
            M_200 -> if (language == AppLanguage.ENGLISH) "200 Meters" else "২০০ মিটার"
            M_500 -> if (language == AppLanguage.ENGLISH) "500 Meters" else "৫০০ মিটার"
            KM_1 -> if (language == AppLanguage.ENGLISH) "1 Kilometer" else "১ কিলোমিটার"
        }
    }

    companion object {
        val DEFAULT = M_200
        val M_250 = M_200

        fun fromMeters(meters: Int): AlarmDistance {
            if (meters == 250) return M_200
            return entries.find { it.meters == meters } ?: DEFAULT
        }
    }
}

