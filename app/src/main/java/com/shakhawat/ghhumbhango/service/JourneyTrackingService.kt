package com.shakhawat.ghhumbhango.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.shakhawat.ghhumbhango.AlarmActivity
import com.shakhawat.ghhumbhango.MainActivity
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.alarm.AlarmPlayer
import com.shakhawat.ghhumbhango.data.location.DefaultLocationClient
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.AlarmIntensity
import com.shakhawat.ghhumbhango.domain.model.ActiveJourney
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.JourneyState
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Android Foreground Service responsible for continuous GPS location tracking,
 * distance calculation, wake lock maintenance, proximity alarm dispatch, and ongoing notifications.
 */
class JourneyTrackingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var trackingJob: Job? = null
    private lateinit var locationClient: DefaultLocationClient
    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var preferencesRepo: com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
    private lateinit var historyRepo: com.shakhawat.ghhumbhango.data.repository.JourneyHistoryRepository

    private var isAlarmTriggered = false
    private var isOneKmAlertTriggered = false
    private var initialDistanceMeters: Float? = null
    private var snoozeUntilMillis: Long = 0L
    private var isBatteryLow = false

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_LOW -> {
                    isBatteryLow = true
                    _activeJourney.value?.let { journey ->
                        notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(journey))
                    }
                }
                Intent.ACTION_BATTERY_OKAY -> {
                    isBatteryLow = false
                    _activeJourney.value?.let { journey ->
                        notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(journey))
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(applicationContext)
        preferencesRepo = com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository.getInstance(applicationContext)
        historyRepo = com.shakhawat.ghhumbhango.data.repository.JourneyHistoryRepository.getInstance(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "GhumBhango::JourneyTrackingWakeLock"
        ).apply {
            setReferenceCounted(false)
        }

        val batteryFilter = android.content.IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        registerReceiver(batteryReceiver, batteryFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val destName = intent.getStringExtra(EXTRA_DEST_NAME) ?: return START_NOT_STICKY
                val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
                val destLng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
                val destAddress = intent.getStringExtra(EXTRA_DEST_ADDRESS)
                val alarmMeters = intent.getIntExtra(EXTRA_ALARM_METERS, 500)

                val alarmDistance = AlarmDistance.fromMeters(alarmMeters)
                val destination = Destination(
                    name = destName,
                    latitude = destLat,
                    longitude = destLng,
                    address = destAddress
                )

                isAlarmTriggered = false
                isOneKmAlertTriggered = false
                initialDistanceMeters = null
                snoozeUntilMillis = 0L

                val journey = ActiveJourney(
                    destination = destination,
                    alarmDistance = alarmDistance,
                    state = JourneyState.TRACKING
                )
                _activeJourney.value = journey

                startForegroundServiceSafely(buildTrackingNotification(journey))
                acquireWakeLock()
                startTrackingLoop(destination, alarmDistance)
            }

            ACTION_PAUSE -> {
                val current = _activeJourney.value
                if (current != null && current.state == JourneyState.TRACKING) {
                    val pausedJourney = current.copy(state = JourneyState.PAUSED)
                    _activeJourney.value = pausedJourney
                    trackingJob?.cancel()
                    releaseWakeLock()
                    notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(pausedJourney))
                }
            }

            ACTION_RESUME -> {
                val current = _activeJourney.value
                if (current != null && current.state == JourneyState.PAUSED) {
                    val resumedJourney = current.copy(state = JourneyState.TRACKING)
                    _activeJourney.value = resumedJourney
                    acquireWakeLock()
                    startTrackingLoop(resumedJourney.destination, resumedJourney.alarmDistance)
                    notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(resumedJourney))
                }
            }

            ACTION_DISMISS_ALARM -> {
                dismissAlarmTrigger()
            }

            ACTION_SNOOZE_ALARM -> {
                snoozeAlarmTrigger()
            }

            ACTION_STOP -> {
                stopTrackingService()
            }
        }
        return START_STICKY
    }

    private fun startTrackingLoop(destination: Destination, alarmDistance: AlarmDistance) {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            locationClient.getLocationUpdates(intervalMs = 3000L)
                .catch {
                    // Keep tracking alive even on temporary GPS glitch
                }
                .collectLatest { loc ->
                    val eval = DistanceCalculator.evaluateProximity(
                        userLocation = loc,
                        destination = destination,
                        alarmRadiusMeters = alarmDistance.meters
                    )

                    val current = _activeJourney.value ?: return@collectLatest
                    if (current.state == JourneyState.PAUSED || current.state == JourneyState.STOPPED || current.state == JourneyState.COMPLETED) {
                        return@collectLatest
                    }

                    val isWithinRadius = eval.isWithinRadius && eval.isReliable
                    val consecutiveHits = if (isWithinRadius) {
                        current.consecutiveProximityHits + 1
                    } else {
                        0
                    }

                    // Require at least 2 consecutive reliable hits or high accuracy fix (<=25m) inside radius
                    val qualifiesForAlarm = consecutiveHits >= 2 || (isWithinRadius && loc.accuracyMeters <= 25f)
                    val isSnoozed = System.currentTimeMillis() < snoozeUntilMillis

                    val newState = if (qualifiesForAlarm) {
                        JourneyState.APPROACHING
                    } else {
                        JourneyState.TRACKING
                    }

                    val updatedJourney = current.copy(
                        currentDistanceMeters = eval.distanceMeters,
                        formattedDistanceRemaining = eval.formattedDistance,
                        userLocation = loc,
                        lastUpdateMillis = System.currentTimeMillis(),
                        isApproaching = qualifiesForAlarm,
                        consecutiveProximityHits = consecutiveHits,
                        state = newState
                    )

                    _activeJourney.value = updatedJourney
                    notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(updatedJourney))

                    // 1 KM early warning alert check
                    if (initialDistanceMeters == null && eval.distanceMeters > 0f) {
                        initialDistanceMeters = eval.distanceMeters
                    }

                    val qualifiesForOneKmAlert = !isOneKmAlertTriggered &&
                        !isAlarmTriggered &&
                        eval.distanceMeters <= 1000f &&
                        eval.distanceMeters > alarmDistance.meters &&
                        (initialDistanceMeters == null || initialDistanceMeters!! >= 900f)

                    if (qualifiesForOneKmAlert) {
                        isOneKmAlertTriggered = true
                        sendOneKmProximityAlert(destination)
                    }

                    // Trigger alarm and full-screen alert when entering proximity
                    if (qualifiesForAlarm && !isAlarmTriggered && !isSnoozed) {
                        triggerAlarm(updatedJourney, alarmDistance)
                    } else if (isAlarmTriggered && !isSnoozed) {
                        // Dynamically update intensity as distance closes in if adaptive alarm is enabled
                        serviceScope.launch {
                            val settings = preferencesRepo.userSettings.first()
                            if (settings.isAdaptiveAlarmEnabled) {
                                val targetIntensity = AlarmIntensity.calculate(
                                    distanceMeters = eval.distanceMeters,
                                    radiusMeters = alarmDistance.meters
                                )
                                AlarmPlayer.updateIntensity(
                                    context = applicationContext,
                                    intensity = targetIntensity,
                                    isVibrationEnabled = settings.isVibrationEnabled
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun triggerAlarm(journey: ActiveJourney, alarmDistance: AlarmDistance) {
        isAlarmTriggered = true

        // 1. Play looping audio and vibrating pattern (respecting user preferences and adaptive intensity)
        serviceScope.launch {
            val settings = preferencesRepo.userSettings.first()
            val customUri = if (settings.alarmSound == com.shakhawat.ghhumbhango.domain.model.AlarmSound.CUSTOM && !settings.customAlarmUri.isNullOrBlank()) {
                try { Uri.parse(settings.customAlarmUri) } catch (_: Exception) { null }
            } else null

            val initialIntensity = if (settings.isAdaptiveAlarmEnabled) {
                AlarmIntensity.calculate(
                    distanceMeters = journey.currentDistanceMeters ?: alarmDistance.meters.toFloat(),
                    radiusMeters = alarmDistance.meters
                )
            } else {
                AlarmIntensity.FULL
            }

            AlarmPlayer.start(
                applicationContext,
                isVibrationEnabled = settings.isVibrationEnabled,
                alarmSound = settings.alarmSound,
                customToneUri = customUri,
                intensity = initialIntensity
            )
        }

        // 2. Build full-screen lockscreen intent
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            10,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 3. Notification actions: Dismiss & Snooze
        val dismissIntent = Intent(this, JourneyTrackingService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            11,
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val snoozeIntent = Intent(this, JourneyTrackingService::class.java).apply {
            action = ACTION_SNOOZE_ALARM
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            12,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val distanceText = journey.formattedDistanceRemaining?.let {
            "অবশিষ্ট দূরত্ব: $it • প্রস্তুত হোন!"
        } ?: "আপনার স্টপ এসে গেছে!"

        val alarmNotification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bus)
            .setContentTitle("🚨 গন্তব্য সমাগত: ${journey.destination.name}")
            .setContentText(distanceText)
            .setSubText("জরুরি অ্যালার্ম")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, "বন্ধ করুন", dismissPendingIntent)
            .addAction(0, "২ মিনিট স্নুজ", snoozePendingIntent)
            .build()

        notificationManager.notify(ALARM_NOTIFICATION_ID, alarmNotification)

        // 4. Launch AlarmActivity directly as well
        try {
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            // Handled via fullScreenIntent in notification
        }
    }

    private fun dismissAlarmTrigger() {
        AlarmPlayer.stop()
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        val current = _activeJourney.value
        if (current != null) {
            serviceScope.launch {
                historyRepo.recordJourney(
                    destination = current.destination,
                    alarmDistanceMeters = current.alarmDistance.meters,
                    startTimeMillis = current.startTimeMillis,
                    status = "COMPLETED"
                )
            }
        }
        _activeJourney.value = _activeJourney.value?.copy(state = JourneyState.COMPLETED)
        stopTrackingService(isCompleted = true)
    }

    private fun snoozeAlarmTrigger() {
        AlarmPlayer.stop()
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        isAlarmTriggered = false
        serviceScope.launch {
            val settings = preferencesRepo.userSettings.first()
            val snoozeMinutes = settings.snoozeDurationMinutes.coerceAtLeast(1)
            snoozeUntilMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

            val current = _activeJourney.value
            if (current != null) {
                val trackingJourney = current.copy(state = JourneyState.TRACKING, isApproaching = false)
                _activeJourney.value = trackingJourney
                notificationManager.notify(NOTIFICATION_ID, buildTrackingNotification(trackingJourney))
            }
        }
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire(8 * 60 * 60 * 1000L) // 8-hour maximum timeout for long cross-country commutes
            }
        } catch (e: Exception) {
            // Safe wakeLock acquisition
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun startForegroundServiceSafely(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildTrackingNotification(journey: ActiveJourney): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when (journey.state) {
            JourneyState.APPROACHING -> "🚨 গন্তব্য সমাগত: ${journey.destination.name}"
            JourneyState.PAUSED -> "যাত্রা বিরতিতে রয়েছে: ${journey.destination.name}"
            else -> "যাত্রা চলছে: ${journey.destination.name}"
        }

        val isStale = (System.currentTimeMillis() - journey.lastUpdateMillis) > 35_000L
        val distanceText = if (isStale && journey.state == JourneyState.TRACKING) {
            journey.formattedDistanceRemaining?.let {
                "বাকি দূরত্ব: $it • ⚠️ সিগন্যাল অপেক্ষমাণ…"
            } ?: "জিপিএস সংকেত অপেক্ষমাণ…"
        } else {
            journey.formattedDistanceRemaining?.let {
                "বাকি দূরত্ব: $it (অ্যালার্ম: ${journey.alarmDistance.displayLabel})"
            } ?: "জিপিএস অবস্থান নেওয়া হচ্ছে…"
        }

        val subText = if (isBatteryLow) {
            "⚠️ চার্জ কম • ট্র্যাকিং সক্রিয়"
        } else {
            "ঘুম ভাঙ্গো"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bus)
            .setContentTitle(title)
            .setContentText(distanceText)
            .setSubText(subText)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(journey.state != JourneyState.STOPPED && journey.state != JourneyState.COMPLETED)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Actions: Pause / Resume
        if (journey.state == JourneyState.TRACKING) {
            val pauseIntent = Intent(this, JourneyTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                1,
                pauseIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "বিরতি দিন", pausePendingIntent)
        } else if (journey.state == JourneyState.PAUSED) {
            val resumeIntent = Intent(this, JourneyTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
                2,
                resumeIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(0, "চালু করুন", resumePendingIntent)
        }

        // Action: Stop
        val stopIntent = Intent(this, JourneyTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            3,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.addAction(0, "যাত্রা বন্ধ", stopPendingIntent)

        return builder.build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Tracking Status Channel (Low importance, silent)
            val trackingChannel = NotificationChannel(
                CHANNEL_ID,
                "ঘুম ভাঙ্গো যাত্রা ট্র্যাকিং",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "চলমান যাত্রার রিয়েলটাইম জিপিএস ট্র্যাকিং ও দূরত্ব নোটিফিকেশন"
                setShowBadge(false)
            }

            // 2. Urgent Alarm Channel (High importance, sound, vibration, lockscreen alert)
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "ঘুম ভাঙ্গো গন্তব্য অ্যালার্ম",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "গন্তব্যে পৌঁছানোর জরুরি অ্যালার্ম ও সতর্কবার্তা"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 400, 1000, 400)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            // 3. Early Warning Channel (High importance, heads-up banner, sound & vibration)
            val earlyWarningChannel = NotificationChannel(
                EARLY_WARNING_CHANNEL_ID,
                "ঘুম ভাঙ্গো আগাম সতর্কতা (১ কিমি)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "গন্তব্যের ১ কিমি পূর্বে আগাম নোটিফিকেশন ও সতর্কবার্তা"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(earlyWarningChannel)
        }
    }

    private fun sendOneKmProximityAlert(destination: Destination) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            20,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        serviceScope.launch {
            val settings = preferencesRepo.userSettings.first()
            val strings = if (settings.appLanguage == com.shakhawat.ghhumbhango.domain.model.AppLanguage.BANGLA) {
                com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
            } else {
                com.shakhawat.ghhumbhango.ui.localization.EnglishStrings
            }

            val title = strings.oneKmAlertTitle
            val content = strings.oneKmAlertContent(destination.name)

            val notification = NotificationCompat.Builder(this@JourneyTrackingService, EARLY_WARNING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bus)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build()

            notificationManager.notify(EARLY_WARNING_NOTIFICATION_ID, notification)
        }
    }

    private fun stopTrackingService(isCompleted: Boolean = false) {
        val current = _activeJourney.value
        if (!isCompleted && current != null && current.state != JourneyState.COMPLETED) {
            serviceScope.launch {
                historyRepo.recordJourney(
                    destination = current.destination,
                    alarmDistanceMeters = current.alarmDistance.meters,
                    startTimeMillis = current.startTimeMillis,
                    status = "STOPPED"
                )
            }
        }
        trackingJob?.cancel()
        AlarmPlayer.stop()
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        notificationManager.cancel(EARLY_WARNING_NOTIFICATION_ID)
        isOneKmAlertTriggered = false
        initialDistanceMeters = null
        releaseWakeLock()
        _activeJourney.value = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
        serviceScope.cancel()
        AlarmPlayer.stop()
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
        notificationManager.cancel(EARLY_WARNING_NOTIFICATION_ID)
        isOneKmAlertTriggered = false
        initialDistanceMeters = null
        releaseWakeLock()
        _activeJourney.value = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "ghumbhango_journey_channel"
        const val ALARM_CHANNEL_ID = "ghumbhango_alarm_channel"
        const val EARLY_WARNING_CHANNEL_ID = "ghumbhango_early_warning_channel"
        const val NOTIFICATION_ID = 1001
        const val ALARM_NOTIFICATION_ID = 2002
        const val EARLY_WARNING_NOTIFICATION_ID = 3003

        const val ACTION_START = "com.shakhawat.ghhumbhango.action.START"
        const val ACTION_PAUSE = "com.shakhawat.ghhumbhango.action.PAUSE"
        const val ACTION_RESUME = "com.shakhawat.ghhumbhango.action.RESUME"
        const val ACTION_STOP = "com.shakhawat.ghhumbhango.action.STOP"
        const val ACTION_DISMISS_ALARM = "com.shakhawat.ghhumbhango.action.DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.shakhawat.ghhumbhango.action.SNOOZE_ALARM"

        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_DEST_LAT = "extra_dest_lat"
        const val EXTRA_DEST_LNG = "extra_dest_lng"
        const val EXTRA_DEST_ADDRESS = "extra_dest_address"
        const val EXTRA_ALARM_METERS = "extra_alarm_meters"

        private val _activeJourney = MutableStateFlow<ActiveJourney?>(null)
        val activeJourney: StateFlow<ActiveJourney?> = _activeJourney.asStateFlow()

        fun start(context: Context, destination: Destination, alarmDistance: AlarmDistance) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DEST_NAME, destination.name)
                putExtra(EXTRA_DEST_LAT, destination.latitude)
                putExtra(EXTRA_DEST_LNG, destination.longitude)
                putExtra(EXTRA_DEST_ADDRESS, destination.address)
                putExtra(EXTRA_ALARM_METERS, alarmDistance.meters)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun dismissAlarm(context: Context) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_DISMISS_ALARM
            }
            context.startService(intent)
        }

        fun snoozeAlarm(context: Context) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_SNOOZE_ALARM
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, JourneyTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
