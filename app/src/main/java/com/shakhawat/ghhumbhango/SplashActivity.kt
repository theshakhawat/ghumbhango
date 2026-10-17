package com.shakhawat.ghhumbhango

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
import com.shakhawat.ghhumbhango.ui.splash.SplashScreen
import com.shakhawat.ghhumbhango.ui.theme.GhhumBhangoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private var targetDestination: String = "home"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPreferencesRepository.getInstance(applicationContext)
        lifecycleScope.launch {
            try {
                val isOnboardingCompleted = userPrefs.isOnboardingCompleted.first()
                val isPermissionCompleted = userPrefs.isPermissionCompleted.first()
                targetDestination = when {
                    !isOnboardingCompleted -> "onboarding"
                    !isPermissionCompleted -> "permission"
                    else -> "home"
                }
            } catch (e: Exception) {
                targetDestination = "home"
            }
        }

        setContent {
            GhhumBhangoTheme {
                SplashScreen(
                    onSplashFinished = {
                        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
                            putExtra("START_DESTINATION", targetDestination)
                        }
                        startActivity(intent)
                        @Suppress("DEPRECATION")
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                )
            }
        }
    }
}
