package com.shakhawat.ghhumbhango.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.style.TextAlign
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.shakhawat.ghhumbhango.domain.model.AppLanguage
import com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.destination.DestinationSearchSheet
import com.shakhawat.ghhumbhango.ui.destination.SaveCustomDestinationDialog
import com.shakhawat.ghhumbhango.ui.home.components.ActiveJourneyCard
import com.shakhawat.ghhumbhango.ui.home.components.StopJourneyConfirmationDialog
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import kotlinx.coroutines.launch

private data class MockupStoppage(
    val id: Long,
    val name: String,
    val locationDescription: String,
    val latitude: Double,
    val longitude: Double,
    val iconBgColor: Color,
    val pinColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSetAlarm: (Destination) -> Unit = {},
    onNavigateToJourneyTracking: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToReadiness: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val appLanguage = com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var isDrawerOpen by remember { mutableStateOf(false) }
    var showExitAppConfirmDialog by remember { mutableStateOf(false) }
    var showLocationOffDialog by remember { mutableStateOf(false) }
    var selectedStoppageForConfirmation by remember { mutableStateOf<Destination?>(null) }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onPermissionResult(fineGranted, coarseGranted)
        viewModel.refreshLocationState()
        if ((fineGranted || coarseGranted) && !viewModel.uiState.value.isGpsEnabled) {
            showLocationOffDialog = true
        }
    }

    fun promptEnableLocation() {
        if (!uiState.hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!uiState.isGpsEnabled) {
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    // Auto-prompt on initial launch if location is off (Pathao style)
    LaunchedEffect(Unit) {
        viewModel.refreshLocationState()
        if (!uiState.hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!uiState.isGpsEnabled) {
            showLocationOffDialog = true
        }
    }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onNotificationPermissionResult(isGranted)
        viewModel.startJourney()
    }

    // Refresh location on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLocationState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val appColors = LocalAppColors.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.HOME,
                onHomeClick = { /* Already on Home */ },
                onSavedClick = onNavigateToSearch,
                onHistoryClick = onNavigateToHistory,
                onSettingsClick = onNavigateToSettings
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Top Section Container: Sky Backdrop + Top App Bar + Scenic Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = appColors.gradientSky
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        // Top App Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Hamburger Menu Button (Left)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        ambientColor = appColors.shadowColor,
                                        spotColor = appColors.shadowColor
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(appColors.topBarButtonBg)
                                    .border(1.dp, appColors.topBarButtonBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isDrawerOpen = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = strings.menu,
                                    tint = appColors.iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Brand Logo (Center): GhhumBhango Logo + "ঘুমভাঙ্গো"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = appColors.textPrimary
                                )
                            }

                            // Settings Button (Right)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        ambientColor = appColors.shadowColor,
                                        spotColor = appColors.shadowColor
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(appColors.topBarButtonBg)
                                    .border(1.dp, appColors.topBarButtonBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNavigateToSettings()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = strings.settingsTitle,
                                    tint = appColors.iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Scenic Panoramic Landscape Banner (Blue bus, Road, Red pin, Cityscape)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .height(110.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.home_scenic_banner),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Pathao-style "আপনার লোকেশন অফ" Alert Card
                AnimatedVisibility(
                    visible = !uiState.hasLocationPermission || !uiState.isGpsEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0xFFEF4444).copy(alpha = 0.2f),
                                spotColor = Color(0xFFEF4444).copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (appColors.isDark) Color(0xFF261414) else Color(0xFFFEF2F2))
                            .border(
                                1.5.dp,
                                if (appColors.isDark) Color(0xFF7F1D1D) else Color(0xFFFCA5A5),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (appColors.isDark) Color(0xFF451A1A) else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsOff,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (!uiState.hasLocationPermission)
                                                (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "লোকেশন অনুমতি প্রয়োজন" else "Location Permission Needed")
                                            else
                                                (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "আপনার লোকেশন অফ" else "Your Location is Off"),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (appColors.isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = if (!uiState.hasLocationPermission)
                                            (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                                "দূরত্ব মাপতে এবং গন্তব্যে পৌঁছালে সঠিক সময়ে অ্যালার্ম বাজাতে লোকেশন পারমিশন দিন।"
                                            else
                                                "Grant location permission to track distance and alarm on arrival.")
                                        else
                                            (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                                "সঠিক সময়ে ঘুম ভাঙাতে এবং গন্তব্য ট্র্যাক করতে ফোনের জিপিএস / লোকেশন চালু করুন।"
                                            else
                                                "Please turn on device location to track your journey and ring alarm on time."),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = if (appColors.isDark) Color(0xFFE2E8F0) else Color(0xFF475569)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    promptEnableLocation()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (!uiState.hasLocationPermission)
                                        (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "অনুমতি দিন" else "Grant Permission")
                                    else
                                        (if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "লোকেশন চালু করুন" else "Turn On Location"),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }


                // 2. Active Journey Dashboard (When Journey is running)
                if (uiState.isJourneyActive && uiState.activeJourney != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp, bottom = 12.dp)
                            .clickable {
                                onNavigateToJourneyTracking()
                            }
                    ) {
                        ActiveJourneyCard(
                            journey = uiState.activeJourney!!,
                            onPauseJourney = { viewModel.pauseJourney() },
                            onResumeJourney = { viewModel.resumeJourney() },
                            onStopJourneyClick = { viewModel.requestStopJourney() },
                            onDismissAlarm = { viewModel.dismissAlarm() },
                            onSnoozeAlarm = {
                                viewModel.snoozeAlarm()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                            "অ্যালার্ম ২ মিনিটের জন্য স্নুজ করা হয়েছে"
                                        else
                                            "Alarm snoozed for 2 minutes"
                                    )
                                }
                            }
                        )
                    }
                } else {
                    // 3. Hero Card: "নতুন যাত্রা শুরু করুন"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 6.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0x0F000000),
                                spotColor = Color(0x15000000)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(appColors.cardBackground)
                            .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Row: Text on Left + 3D Map Pin Graphic on Right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.home_hero_title),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 21.sp,
                                        lineHeight = 27.sp,
                                        color = appColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(id = R.string.home_hero_subtitle),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        color = appColors.textSecondary
                                    )
                                }

                                Image(
                                    painter = painterResource(id = R.drawable.home_hero_map_pin),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(width = 115.dp, height = 75.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Emerald Green Button: "গন্তব্য খুঁজুন" with Magnifying Glass
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = Color(0xFF00C06A),
                                        ambientColor = Color(0xFF00C06A)
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF00C86E),
                                                Color(0xFF00B55E)
                                            )
                                        )
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (!uiState.hasLocationPermission || !uiState.isGpsEnabled) {
                                            promptEnableLocation()
                                        } else {
                                            onNavigateToSearch()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp)
                                            .background(Color.White.copy(alpha = 0.4f))
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = stringResource(id = R.string.home_search_btn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Section: "সাম্প্রতিক যাত্রা" + "সব দেখুন >"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.recentJourneysTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )

                    if (uiState.recentJourneys.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigateToHistory()
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.home_see_all),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0072FF)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Recent Journeys List or Empty State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    if (uiState.recentJourneys.isEmpty()) {
                        EmptyRecentJourneysCard(
                            onStartNewJourney = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (!uiState.hasLocationPermission || !uiState.isGpsEnabled) {
                                    promptEnableLocation()
                                } else {
                                    onNavigateToSearch()
                                }
                            }
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.recentJourneys.take(4).forEach { journey ->
                                RecentJourneyHomeCard(
                                    journey = journey,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        if (!uiState.hasLocationPermission || !uiState.isGpsEnabled) {
                                            promptEnableLocation()
                                        } else {
                                            val destination = journey.toDestination()
                                            viewModel.selectDestination(destination)
                                            viewModel.selectAlarmDistance(journey.toAlarmDistance())
                                            onNavigateToSetAlarm(destination)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Quick Stoppage Confirmation & Start Journey Bottom Sheet
        if (selectedStoppageForConfirmation != null && !uiState.isJourneyActive) {
            ModalBottomSheet(
                onDismissRequest = { selectedStoppageForConfirmation = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "গন্তব্য নির্ধারিত হয়েছে",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${selectedStoppageForConfirmation!!.name} (${selectedStoppageForConfirmation!!.address ?: ""})",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = Color(0xFF0072FF)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "অ্যালার্মের দূরত্ব নির্বাচন করুন:",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Distance chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlarmDistance.values().forEach { distance ->
                            val isSelected = uiState.selectedDistance == distance
                            val isDefault = distance == AlarmDistance.DEFAULT
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0072FF)
                                        else if (isDefault) Color(0xFFEFF6FF)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else if (isDefault) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF0072FF)
                                                else if (isDefault) Color(0xFF0072FF)
                                                else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.selectAlarmDistance(distance)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                             ) {
                                Text(
                                    text = distance.getLocalizedLabel(appLanguage),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = if (isSelected || isDefault) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White 
                                            else if (isDefault) Color(0xFF0072FF) 
                                            else Color(0xFF1E293B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Start Journey Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(8.dp, RoundedCornerShape(27.dp), spotColor = Color(0xFF0072FF))
                            .clip(RoundedCornerShape(27.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0072FF), Color(0xFF0052D4))
                                )
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (!uiState.hasLocationPermission || !uiState.isGpsEnabled) {
                                    promptEnableLocation()
                                    return@clickable
                                }
                                selectedStoppageForConfirmation = null
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !uiState.hasNotificationPermission) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.startJourney()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                                "যাত্রা শুরু হয়েছে!"
                                            else
                                                "Journey started!"
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.startJourney,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Side Drawer Modal for Hamburger Menu
        if (isDrawerOpen) {
            ModalBottomSheet(
                onDismissRequest = { isDrawerOpen = false },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = appColors.textPrimary
                            )
                            Text(
                                text = stringResource(id = R.string.app_tagline),
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 12.sp,
                                color = appColors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    DrawerItem(
                        icon = Icons.Default.Map,
                        title = strings.pickFromMap,
                        onClick = {
                            isDrawerOpen = false
                            if (!uiState.hasLocationPermission || !uiState.isGpsEnabled) {
                                promptEnableLocation()
                            } else {
                                onNavigateToMap()
                            }
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Security,
                        title = stringResource(id = R.string.cd_readiness_status),
                        onClick = {
                            isDrawerOpen = false
                            onNavigateToReadiness()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.History,
                        title = strings.historyScreenTitle,
                        onClick = {
                            isDrawerOpen = false
                            onNavigateToHistory()
                        }
                    )

                    DrawerItem(
                        icon = Icons.Default.Settings,
                        title = strings.settingsTitle,
                        onClick = {
                            isDrawerOpen = false
                            onNavigateToSettings()
                        }
                    )

                    HorizontalDivider(
                        color = appColors.cardBorder.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    DrawerItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "অ্যাপ থেকে প্রস্থান" else "Exit App",
                        iconTint = Color(0xFFEF4444),
                        textColor = Color(0xFFEF4444),
                        onClick = {
                            isDrawerOpen = false
                            showExitAppConfirmDialog = true
                        }
                    )
                }
            }
        }

        // Exit App Confirmation Modal
        if (showExitAppConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitAppConfirmDialog = false },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "অ্যাপ থেকে প্রস্থান" else "Exit App",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                            "আপনি কি নিশ্চিত যে ঘুমালে জাগুন অ্যাপটি থেকে বের হতে চান?"
                        else
                            "Are you sure you want to exit Ghum Bhango?",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitAppConfirmDialog = false
                            (context as? android.app.Activity)?.finishAffinity()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "প্রস্থান করুন" else "Exit",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitAppConfirmDialog = false }) {
                        Text(
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "বাতিল" else "Cancel",
                            fontFamily = HindSiliguriFontFamily,
                            color = appColors.textMuted
                        )
                    }
                }
            )
        }

        // Location Off Dialog (Pathao-style prompt)
        if (showLocationOffDialog) {
            AlertDialog(
                onDismissRequest = { showLocationOffDialog = false },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(22.dp),
                icon = {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsOff,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "আপনার লোকেশন অফ আছে" else "Your Location is Off",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = appColors.textPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                            "ঘুমভাঙ্গো অ্যাপের মাধ্যমে সঠিক সময়ে অ্যালার্ম ও গন্তব্য ট্র্যাক করতে আপনার ফোনের জিপিএস / লোকেশন চালু করা আবশ্যক। অনুগ্রহ করে লোকেশন চালু করুন।"
                        else
                            "To track your destination and ring alarm at the right time, GPS / Location must be enabled. Please turn on location.",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp,
                        color = appColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLocationOffDialog = false
                            try {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C06A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "সেটিংস থেকে চালু করুন" else "Turn On from Settings",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLocationOffDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings) "পরে করব" else "Later",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textMuted
                        )
                    }
                }
            )
        }

        // Stop Journey Confirmation Modal
        if (uiState.showStopConfirmationDialog) {
            StopJourneyConfirmationDialog(
                destinationName = uiState.selectedDestination?.name ?: strings.destinationLabel,
                onConfirmStop = {
                    viewModel.confirmStopJourney()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                "যাত্রা সমাপ্ত করা হয়েছে"
                            else
                                "Journey ended"
                        )
                    }
                },
                onDismiss = { viewModel.dismissStopConfirmation() }
            )
        }

        // Destination Search & Saved Sheet
        if (uiState.isSearchSheetOpen) {
            DestinationSearchSheet(
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearchingOnline = uiState.isSearchingOnline,
                onlineSearchMessage = uiState.onlineSearchMessage,
                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onSearchOnline = { viewModel.searchOnline() },
                onSelectDestination = { destination ->
                    viewModel.selectDestination(destination)
                    selectedStoppageForConfirmation = destination
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("গন্তব্য নির্বাচিত: ${destination.name}")
                    }
                },
                onDeleteDestination = { destination ->
                    viewModel.deleteSavedDestination(destination)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("${destination.name} মুছে ফেলা হয়েছে")
                    }
                },
                onOpenAddCustomDialog = { viewModel.openSaveCustomDialog() },
                onDismiss = { viewModel.closeSearchSheet() }
            )
        }

        // Custom Destination Dialog
        if (uiState.isSaveCustomDialogOpen) {
            SaveCustomDestinationDialog(
                onDismiss = { viewModel.closeSaveCustomDialog() },
                onSave = { name, lat, lng, address ->
                    viewModel.saveCustomDestination(name, lat, lng, address)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("নতুন গন্তব্য সংরক্ষিত: $name")
                    }
                }
            )
        }
    }
}

@Composable
private fun MockupStoppageCard(
    stoppage: MockupStoppage,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(appColors.cardBackground)
            .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Location Pin Container with distinctive color
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(stoppage.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_perm_location),
                    contentDescription = null,
                    tint = stoppage.pinColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: Name and Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stoppage.name,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = stoppage.locationDescription,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    color = appColors.textSecondary
                )
            }

            // Right: Chevron >
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color = Color(0xFF0072FF),
    textColor: Color? = null,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontFamily = HindSiliguriFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = textColor ?: appColors.textPrimary
        )
    }
}

@Composable
private fun RecentJourneyHomeCard(
    journey: com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity,
    onClick: () -> Unit
) {
    val appLanguage = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    val isBengali = appLanguage == AppLanguage.BANGLA

    val dateFormat = remember(isBengali) {
        if (isBengali) {
            SimpleDateFormat("d MMMM, yyyy • hh:mm a", Locale("bn", "BD"))
        } else {
            SimpleDateFormat("d MMM, yyyy • hh:mm a", Locale.ENGLISH)
        }
    }

    val formattedTime = remember(journey.startTimeMillis, journey.endTimeMillis, isBengali) {
        val timeToFormat = if (journey.startTimeMillis > 0) journey.startTimeMillis else journey.endTimeMillis
        val formatted = dateFormat.format(Date(timeToFormat))
        if (isBengali) DistanceCalculator.toBengaliDigits(formatted) else formatted
    }

    val isCompleted = journey.status == "COMPLETED"
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(appColors.cardBackground)
            .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Date & Time + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedTime,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = appColors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCompleted) strings.statusSuccess else strings.statusCancelled,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (isCompleted) Color(0xFF16A34A) else Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route visual: Source to Destination
            Column(modifier = Modifier.fillMaxWidth()) {
                // Source Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${strings.sourceLabel}: ",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )

                    Text(
                        text = journey.sourceName ?: strings.currentLocation,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vertical Route connector line
                Box(
                    modifier = Modifier
                        .padding(start = 5.5.dp)
                        .width(1.5.dp)
                        .height(14.dp)
                        .background(appColors.divider)
                )

                // Destination Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Text(
                        text = "${strings.destinationLabel}: ",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = appColors.textSecondary
                    )

                    Text(
                        text = journey.destinationName,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = appColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!journey.destinationAddress.isNullOrBlank()) {
                    Text(
                        text = journey.destinationAddress,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 23.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row: Alarm distance pill + "পুনরায় শুরু করুন" action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.alarmDistanceHeader}: ${DistanceCalculator.toBengaliDigits(journey.alarmDistanceMeters)} মি.",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF0072FF)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0072FF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.restartJourney,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentJourneysCard(
    onStartNewJourney: () -> Unit
) {
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(appColors.cardBackground)
            .border(1.dp, appColors.cardBorder, RoundedCornerShape(20.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(if (appColors.isDark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = Color(0xFF0072FF),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.noRecentJourneysTitle,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = appColors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.noRecentJourneysSubtitle,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = appColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Start New Journey Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = Color(0xFF0072FF),
                        ambientColor = Color(0xFF0072FF)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0072FF), Color(0xFF0052D4))
                        )
                    )
                    .clickable {
                        onStartNewJourney()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.startNewJourneyBtn,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
