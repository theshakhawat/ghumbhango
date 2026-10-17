# Proguard / R8 Configuration for GhumBhango (ঘুম ভাঙ্গো)

# 1. GhumBhango Models and Room Entities
-keep class com.shakhawat.ghhumbhango.domain.model.** { *; }
-keep class com.shakhawat.ghhumbhango.data.local.entity.** { *; }
-keep class com.shakhawat.ghhumbhango.data.local.dao.** { *; }
-keep class com.shakhawat.ghhumbhango.data.local.GhumBhangoDatabase { *; }

# 2. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 3. OSMDroid (OpenStreetMap Offline Mapping)
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# 4. Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# 5. Jetpack DataStore Preferences
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# 6. Kotlinx Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

