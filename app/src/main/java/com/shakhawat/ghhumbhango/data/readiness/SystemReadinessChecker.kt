package com.shakhawat.ghhumbhango.data.readiness

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.shakhawat.ghhumbhango.domain.model.ReadinessActionType
import com.shakhawat.ghhumbhango.domain.model.ReadinessItem
import com.shakhawat.ghhumbhango.domain.model.SystemReadiness

/**
 * Evaluates system permissions, hardware switches, battery optimization exemptions,
 * and offline readiness to guarantee safe, unhindered journey alarm operation.
 */
class SystemReadinessChecker(private val context: Context) {

    fun checkReadiness(): SystemReadiness {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val networkCapabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        val isOnline = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val items = listOf(
            ReadinessItem(
                id = "gps_permission",
                title = "নিখুঁত জিপিএস অনুমতি",
                description = if (fineLocationGranted) {
                    "সঠিক পারমিশন মঞ্জুর করা আছে। মিটার নির্ভুলতায় ট্র্যাকিং সম্ভব।"
                } else if (coarseLocationGranted) {
                    "কেবল আনুমানিক পারমিশন দেওয়া আছে। সঠিক সময়ে অ্যালার্ম পেতে 'Precise' পারমিশন দিন।"
                } else {
                    "লোকেশন পারমিশন বন্ধ। যাত্রা শুরু করতে অবশ্যই জিপিএস পারমিশন প্রয়োজন।"
                },
                isReady = fineLocationGranted,
                isCritical = true,
                actionType = ReadinessActionType.REQUEST_LOCATION_PERMISSION
            ),
            ReadinessItem(
                id = "gps_switch",
                title = "ডিভাইসের জিপিএস সার্ভিস",
                description = if (gpsEnabled) {
                    "ফোনের লোকেশন সার্ভিস অন রয়েছে।"
                } else {
                    "ফোনের জিপিএস সুইচ বন্ধ। সেটিংস থেকে লোকেশন অন করুন।"
                },
                isReady = gpsEnabled,
                isCritical = true,
                actionType = ReadinessActionType.OPEN_LOCATION_SETTINGS
            ),
            ReadinessItem(
                id = "notification_permission",
                title = "নোটিফিকেশন ও অ্যালার্ম অনুমতি",
                description = if (notificationGranted) {
                    "স্ট্যাটাস বার নোটিফিকেশন ও জরুরি অ্যালার্ম অ্যালার্ট সক্রিয়।"
                } else {
                    "নোটিফিকেশন পারমিশন বন্ধ থাকলে ব্যাকগ্রাউন্ডে অ্যালার্ম নাও বাজতে পারে।"
                },
                isReady = notificationGranted,
                isCritical = true,
                actionType = ReadinessActionType.REQUEST_NOTIFICATION_PERMISSION
            ),
            ReadinessItem(
                id = "battery_optimization",
                title = "ব্যাটারি অপ্টিমাইজেশন ছাড় (Doze Mode)",
                description = if (batteryOptimizationIgnored) {
                    "ব্যাকগ্রাউন্ড ছাড় সক্রিয়। স্ক্রিন অফ থাকলেও অ্যালার্ম মিস হবে না।"
                } else {
                    "MIUI, Samsung বা অন্যান্য ফোনে স্ক্রিন অফ থাকলে অ্যাপ স্লিপে চলে যেতে পারে। 'Unrestricted' বা ছাড় দিন।"
                },
                isReady = batteryOptimizationIgnored,
                isCritical = false,
                actionType = ReadinessActionType.REQUEST_BATTERY_OPTIMIZATION
            ),
            ReadinessItem(
                id = "offline_status",
                title = "ইন্টারনেট ও অফলাইন প্রস্তুতি",
                description = if (isOnline) {
                    "ইন্টারনেট সক্রিয়। নতুন অনলাইন স্টপ অনুসন্ধান ও ম্যাপ ডাউনলোড সক্রিয়।"
                } else {
                    "অফলাইন মোড। সংরক্ষিত স্টপ ও পূর্বের ক্যাশ করা অফলাইন ম্যাপ কাজ করবে।"
                },
                isReady = true, // Offline capability is fully supported by design
                isCritical = false,
                actionType = ReadinessActionType.NONE
            )
        )

        return SystemReadiness(items)
    }
}

