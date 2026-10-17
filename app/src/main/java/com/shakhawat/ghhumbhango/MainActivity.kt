package com.shakhawat.ghhumbhango

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
import com.shakhawat.ghhumbhango.domain.model.AppLanguage
import com.shakhawat.ghhumbhango.domain.model.AppThemeMode
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserSettings
import com.shakhawat.ghhumbhango.ui.destination.DestinationSearchScreen
import com.shakhawat.ghhumbhango.ui.destination.SetAlarmScreen
import com.shakhawat.ghhumbhango.ui.home.HomeScreen
import com.shakhawat.ghhumbhango.ui.home.HomeViewModel
import com.shakhawat.ghhumbhango.ui.journey.JourneyLiveTrackingScreen
import com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.localization.getAppStrings
import com.shakhawat.ghhumbhango.ui.map.MapScreen
import com.shakhawat.ghhumbhango.ui.onboarding.OnboardingScreen
import com.shakhawat.ghhumbhango.ui.permission.PermissionScreen
import com.shakhawat.ghhumbhango.ui.theme.GhhumBhangoTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDestination = intent.getStringExtra("START_DESTINATION") ?: "home"

        setContent {
            val userPrefsRepo = remember { UserPreferencesRepository.getInstance(applicationContext) }
            val userSettings by userPrefsRepo.userSettings.collectAsStateWithLifecycle(initialValue = UserSettings())
            val currentLanguage = userSettings.appLanguage
            val strings = remember(currentLanguage) { getAppStrings(currentLanguage) }

            LaunchedEffect(currentLanguage) {
                try {
                    val locale = Locale(currentLanguage.code)
                    Locale.setDefault(locale)
                    val config = resources.configuration
                    config.setLocale(locale)
                    @Suppress("DEPRECATION")
                    resources.updateConfiguration(config, resources.displayMetrics)
                } catch (_: Exception) {}
            }

            val isDark = when (userSettings.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalAppLanguage provides currentLanguage,
                LocalAppStrings provides strings
            ) {
                GhhumBhangoTheme(darkTheme = isDark) {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = viewModel()
                    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                    fun getRouteOrder(route: String?): Int {
                        return when (route) {
                            "home" -> 0
                            "destination_search" -> 1
                            "history" -> 2
                            "settings" -> 3
                            "map" -> 4
                            "set_alarm" -> 5
                            "journey_tracking" -> 6
                            "readiness" -> 7
                            else -> 0
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = initialDestination,
                        enterTransition = {
                            val initialOrder = getRouteOrder(initialState.destination.route)
                            val targetOrder = getRouteOrder(targetState.destination.route)
                            if (targetOrder >= initialOrder) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            } else {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            }
                        },
                        exitTransition = {
                            val initialOrder = getRouteOrder(initialState.destination.route)
                            val targetOrder = getRouteOrder(targetState.destination.route)
                            if (targetOrder >= initialOrder) {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            } else {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            }
                        },
                        popEnterTransition = {
                            val initialOrder = getRouteOrder(initialState.destination.route)
                            val targetOrder = getRouteOrder(targetState.destination.route)
                            if (targetOrder <= initialOrder) {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            } else {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            }
                        },
                        popExitTransition = {
                            val initialOrder = getRouteOrder(initialState.destination.route)
                            val targetOrder = getRouteOrder(targetState.destination.route)
                            if (targetOrder <= initialOrder) {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            } else {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                    ) {
                    composable("onboarding") {
                        val coroutineScope = rememberCoroutineScope()
                        val userPrefs = remember { UserPreferencesRepository.getInstance(applicationContext) }
                        OnboardingScreen(
                            onFinishOnboarding = {
                                coroutineScope.launch {
                                    userPrefs.setOnboardingCompleted(true)
                                    navController.navigate("permission") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("permission") {
                        val coroutineScope = rememberCoroutineScope()
                        val userPrefs = remember { UserPreferencesRepository.getInstance(applicationContext) }
                        PermissionScreen(
                            onPermissionsCompleted = {
                                coroutineScope.launch {
                                    userPrefs.setPermissionCompleted(true)
                                    navController.navigate("home") {
                                        popUpTo("permission") { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToSearch = {
                                navController.navigate("destination_search") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSetAlarm = { dest ->
                                homeViewModel.selectDestination(dest)
                                navController.navigate("set_alarm")
                            },
                            onNavigateToJourneyTracking = {
                                navController.navigate("journey_tracking")
                            },
                            onNavigateToMap = {
                                navController.navigate("map")
                            },
                            onNavigateToReadiness = {
                                navController.navigate("readiness")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("destination_search") {
                        DestinationSearchScreen(
                            onSelectDestination = { dest ->
                                homeViewModel.selectDestination(dest)
                                navController.navigate("set_alarm")
                            },
                            onPickOnMap = {
                                navController.navigate("map")
                            },
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("set_alarm") {
                        val dest = uiState.selectedDestination ?: Destination(
                            id = "khilkhet_default",
                            name = "খিলক্ষেত",
                            latitude = 23.8293,
                            longitude = 90.4203,
                            address = "ঢাকা, বাংলাদেশ"
                        )
                        SetAlarmScreen(
                            destination = dest,
                            currentLocation = uiState.currentLocation,
                            initialAlarmDistance = uiState.selectedDistance,
                            onStartJourney = { destination, alarmDistance ->
                                homeViewModel.selectDestination(destination)
                                homeViewModel.selectAlarmDistance(alarmDistance)
                                homeViewModel.startJourney()
                                navController.navigate("journey_tracking") {
                                    popUpTo("home") { inclusive = false }
                                }
                            },
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("journey_tracking") {
                        val activeJourney = uiState.activeJourney
                        if (activeJourney != null) {
                            JourneyLiveTrackingScreen(
                                journey = activeJourney,
                                onStopJourney = {
                                    homeViewModel.confirmStopJourney()
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = false }
                                    }
                                },
                                onNavigateToHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToHistory = {
                                    navController.navigate("history") {
                                        launchSingleTop = true
                                    }
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        }
                    }

                    composable("map") {
                        MapScreen(
                            currentLocation = uiState.currentLocation,
                            initialDestination = uiState.selectedDestination,
                            initialAlarmDistance = uiState.selectedDistance,
                            isPickingMode = !uiState.isJourneyActive,
                            onConfirmDestination = { destination, alarmDistance ->
                                homeViewModel.selectDestination(destination)
                                homeViewModel.selectAlarmDistance(alarmDistance)
                                navController.navigate("set_alarm") {
                                    popUpTo("map") { inclusive = true }
                                }
                            },
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("readiness") {
                        com.shakhawat.ghhumbhango.ui.readiness.ReadinessScreen(
                            onBackClick = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("settings") {
                        com.shakhawat.ghhumbhango.ui.settings.SettingsScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSearch = {
                                navController.navigate("destination_search") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToHistory = {
                                navController.navigate("history") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable("history") {
                        com.shakhawat.ghhumbhango.ui.history.HistoryScreen(
                            onRestartJourney = { destination, alarmDistance ->
                                homeViewModel.selectDestination(destination)
                                homeViewModel.selectAlarmDistance(alarmDistance)
                                navController.navigate("set_alarm")
                            },
                            currentLocation = uiState.currentLocation,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSearch = {
                                navController.navigate("destination_search") {
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
}
