package com.shakhawat.ghhumbhango

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shakhawat.ghhumbhango.domain.model.JourneyState
import com.shakhawat.ghhumbhango.service.JourneyTrackingService
import com.shakhawat.ghhumbhango.ui.alarm.AlarmScreen
import com.shakhawat.ghhumbhango.ui.theme.GhhumBhangoTheme

/**
 * High-priority, lockscreen-bypassing Activity launched when the user enters the alarm radius.
 */
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureLockscreenWakeup()

        // Prevent accidental back gesture cancellation while half-asleep
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ignore back gesture; passenger must explicitly tap Dismiss or Snooze
            }
        })

        setContent {
            GhhumBhangoTheme {
                val activeJourney by JourneyTrackingService.activeJourney.collectAsStateWithLifecycle()

                // If the journey was dismissed or stopped outside this activity, dismiss screen
                if (activeJourney == null || activeJourney?.state != JourneyState.APPROACHING) {
                    finish()
                }

                val journey = activeJourney
                val destName = journey?.destination?.name ?: "গন্তব্য"
                val destAddress = journey?.destination?.address
                val distanceText = journey?.formattedDistanceRemaining
                val radiusText = journey?.alarmDistance?.displayLabel

                AlarmScreen(
                    destinationName = destName,
                    destinationAddress = destAddress,
                    distanceRemainingText = distanceText,
                    alarmRadiusText = radiusText,
                    onDismissAlarm = {
                        JourneyTrackingService.dismissAlarm(this@AlarmActivity)
                        finish()
                    },
                    onSnoozeAlarm = {
                        JourneyTrackingService.snoozeAlarm(this@AlarmActivity)
                        finish()
                    }
                )
            }
        }
    }

    private fun configureLockscreenWakeup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

