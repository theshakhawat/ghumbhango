package com.shakhawat.ghhumbhango.domain.model

sealed interface GpsStatus {
    data class Ready(val location: UserLocation) : GpsStatus
    data class Searching(val message: String = "জিপিএস সিগন্যাল অনুসন্ধান চলছে…") : GpsStatus
    data class GpsDisabled(val message: String = "ডিভাইসের লোকেশন/জিপিএস বন্ধ রয়েছে।") : GpsStatus
    data class PermissionDenied(val message: String = "সঠিক লোকেশন অনুমতি প্রয়োজন।") : GpsStatus
    data class ApproximateOnly(val message: String = "শুধুমাত্র আনুমানিক অনুমতি রয়েছে। নিখুঁত অ্যালার্মের জন্য প্রিসাইজ পারমিশন দিন।") : GpsStatus
}

