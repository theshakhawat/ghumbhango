package com.shakhawat.ghhumbhango.ui.destination

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.alarm.AlarmPlayer
import com.shakhawat.ghhumbhango.alarm.CustomAlarmAudioHelper
import com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
import com.shakhawat.ghhumbhango.data.repository.RouteRepository
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.AlarmSound
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import com.shakhawat.ghhumbhango.domain.model.UserSettings
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun SetAlarmScreen(
    destination: Destination,
    currentLocation: UserLocation?,
    initialAlarmDistance: AlarmDistance = AlarmDistance.M_500,
    onStartJourney: (Destination, AlarmDistance) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current

    val repository = remember { UserPreferencesRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val userSettings by repository.userSettings.collectAsStateWithLifecycle(initialValue = UserSettings())

    var selectedDistance by remember { mutableStateOf(initialAlarmDistance) }
    var showSoundDialog by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val pickedPair = CustomAlarmAudioHelper.handleRingtonePickerResult(context, result.data)
            if (pickedPair != null) {
                val (uri, title) = pickedPair
                coroutineScope.launch {
                    repository.setCustomAlarmTone(uri, title)
                }
                AlarmPlayer.playPreview(context, AlarmSound.CUSTOM, Uri.parse(uri))
            }
        }
    }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val pickedPair = CustomAlarmAudioHelper.handleAudioFilePickerResult(context, uri)
            if (pickedPair != null) {
                val (savedUri, title) = pickedPair
                coroutineScope.launch {
                    repository.setCustomAlarmTone(savedUri, title)
                }
                AlarmPlayer.playPreview(context, AlarmSound.CUSTOM, Uri.parse(savedUri))
            }
        }
    }

    var previewRoutePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var previewCasingRef by remember { mutableStateOf<Polyline?>(null) }
    var previewLineRef by remember { mutableStateOf<Polyline?>(null) }
    var previewMapRef by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(destination.latitude, destination.longitude, currentLocation?.latitude, currentLocation?.longitude) {
        val startLat = currentLocation?.latitude ?: (destination.latitude - 0.04)
        val startLon = currentLocation?.longitude ?: (destination.longitude - 0.03)
        val res = RouteRepository.getRoadRoute(
            startLat = startLat,
            startLon = startLon,
            destLat = destination.latitude,
            destLon = destination.longitude
        )
        if (res.points.size >= 2) {
            previewRoutePoints = res.points
            previewCasingRef?.setPoints(res.points)
            previewLineRef?.setPoints(res.points)
            previewMapRef?.invalidate()
            try {
                val bBox = BoundingBox.fromGeoPoints(res.points)
                previewMapRef?.zoomToBoundingBox(bBox, true, 50)
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AlarmPlayer.stopPreview()
        }
    }

    // Initialize osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("ghumbhango_osm", Context.MODE_PRIVATE)
        )
    }

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.HOME,
                onHomeClick = onNavigateToHome,
                onSavedClick = onBackClick,
                onHistoryClick = onNavigateToHistory,
                onSettingsClick = onNavigateToSettings
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header: Back Button & Title "অ্যালার্ম সেট করুন"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                onBackClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = appColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = strings.setAlarmTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = appColors.textPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(44.dp)) // balance spacing
                }
            }

            // 2. Interactive Route Map Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(230.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
            ) {
                // OSMDroid Map View showing User location, Destination & Polyline
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            previewMapRef = this
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            isTilesScaledToDpi = true
                            zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                            val destPoint = GeoPoint(destination.latitude, destination.longitude)
                            val userPoint = if (currentLocation != null) {
                                GeoPoint(currentLocation.latitude, currentLocation.longitude)
                            } else {
                                // Default nearby reference (Uttara / Airport)
                                GeoPoint(destination.latitude - 0.04, destination.longitude - 0.03)
                            }

                            // Outer halo casing for route
                            val casing = Polyline(this).apply {
                                outlinePaint.color = android.graphics.Color.parseColor("#400072FF")
                                outlinePaint.strokeWidth = 14f
                                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                                if (previewRoutePoints.size >= 2) {
                                    setPoints(previewRoutePoints)
                                } else {
                                    addPoint(userPoint)
                                    addPoint(destPoint)
                                }
                            }
                            previewCasingRef = casing
                            overlays.add(casing)

                            // Inner vibrant road route line
                            val line = Polyline(this).apply {
                                outlinePaint.color = android.graphics.Color.parseColor("#0072FF")
                                outlinePaint.strokeWidth = 9f
                                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                                if (previewRoutePoints.size >= 2) {
                                    setPoints(previewRoutePoints)
                                } else {
                                    addPoint(userPoint)
                                    addPoint(destPoint)
                                }
                            }
                            previewLineRef = line
                            overlays.add(line)

                            // Add Destination Marker
                            val destMarker = Marker(this).apply {
                                position = destPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = destination.name
                                icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_destination)
                            }
                            overlays.add(destMarker)

                            // Add User Marker
                            val userMarker = Marker(this).apply {
                                position = userPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                title = "আপনার অবস্থান"
                                icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_user)
                            }
                            overlays.add(userMarker)

                            post {
                                if (width > 0 && height > 0) {
                                    try {
                                        if (previewRoutePoints.size >= 2) {
                                            val bBox = BoundingBox.fromGeoPoints(previewRoutePoints)
                                            zoomToBoundingBox(bBox, false, 40)
                                        } else {
                                            val minLat = minOf(userPoint.latitude, destPoint.latitude) - 0.008
                                            val maxLat = maxOf(userPoint.latitude, destPoint.latitude) + 0.008
                                            val minLon = minOf(userPoint.longitude, destPoint.longitude) - 0.008
                                            val maxLon = maxOf(userPoint.longitude, destPoint.longitude) + 0.008
                                            zoomToBoundingBox(BoundingBox(maxLat, maxLon, minLat, minLon), false, 40)
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    },
                    update = { map ->
                        if (previewRoutePoints.size >= 2) {
                            previewCasingRef?.setPoints(previewRoutePoints)
                            previewLineRef?.setPoints(previewRoutePoints)
                            map.invalidate()
                        }
                    }
                )

                // Tooltip Callout: "আপনার অবস্থান" (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 14.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), ambientColor = appColors.shadowColor, spotColor = appColors.shadowColor)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = strings.yourLocation,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0072FF)
                        )
                        Text(
                            text = if (currentLocation != null) strings.currentLocation else strings.defaultLocationUttara,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = appColors.textMuted
                        )
                    }
                }

                // Tooltip Callout: "গন্তব্য" (Top Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 14.dp)
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp), ambientColor = appColors.shadowColor, spotColor = appColors.shadowColor)
                        .clip(RoundedCornerShape(12.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.destinationLabel,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = destination.name,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = appColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Section: "আগে থেকে অ্যালার্ম বাজবে"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = strings.whenToAlarmHeader,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = appColors.textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        AlarmDistance.entries.forEachIndexed { index, dist ->
                            AlarmDistanceRadioOption(
                                title = dist.getLocalizedLabel(userSettings.appLanguage),
                                isSelected = selectedDistance == dist,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedDistance = dist
                                }
                            )
                            if (index < AlarmDistance.entries.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Section: "অ্যালার্ম সাউন্ড"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = strings.alarmSoundHeader,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = appColors.textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(20.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showSoundDialog = true
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blue Bell Container
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE8F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userSettings.alarmSound.getLocalizedName(userSettings.appLanguage),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = appColors.textPrimary
                            )
                            if (userSettings.alarmSound == AlarmSound.CUSTOM && !userSettings.customAlarmName.isNullOrBlank()) {
                                Text(
                                    text = "🎵 ${userSettings.customAlarmName}",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0072FF),
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = appColors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 5. Vibrant Blue Action Button: "যাত্রা শুরু করুন"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF0072FF),
                        ambientColor = Color(0xFF0072FF)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0072FF),
                                Color(0xFF0088FF)
                            )
                        )
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartJourney(destination, selectedDistance)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Start Journey",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.startJourneyTrackingBtn,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Sound selection dialog
        if (showSoundDialog) {
            DisposableEffect(Unit) {
                onDispose {
                    AlarmPlayer.stopPreview()
                }
            }

            var previewingSound by remember { mutableStateOf<AlarmSound?>(null) }

            AlertDialog(
                onDismissRequest = {
                    AlarmPlayer.stopPreview()
                    showSoundDialog = false
                },
                title = {
                    Text(
                        text = strings.alarmSoundDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Column {
                        AlarmSound.entries.forEach { sound ->
                            val isSelected = (userSettings.alarmSound == sound)
                            val isCustom = (sound == AlarmSound.CUSTOM)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0072FF).copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            coroutineScope.launch {
                                                repository.setAlarmSound(sound)
                                            }
                                            if (isCustom) {
                                                if (!userSettings.customAlarmUri.isNullOrBlank()) {
                                                    previewingSound = sound
                                                    val customUri = try { Uri.parse(userSettings.customAlarmUri) } catch (_: Exception) { null }
                                                    AlarmPlayer.playPreview(context, sound, customUri)
                                                }
                                            } else {
                                                previewingSound = sound
                                                AlarmPlayer.playPreview(context, sound)
                                            }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            coroutineScope.launch {
                                                repository.setAlarmSound(sound)
                                            }
                                            if (isCustom) {
                                                if (!userSettings.customAlarmUri.isNullOrBlank()) {
                                                    previewingSound = sound
                                                    val customUri = try { Uri.parse(userSettings.customAlarmUri) } catch (_: Exception) { null }
                                                    AlarmPlayer.playPreview(context, sound, customUri)
                                                }
                                            } else {
                                                previewingSound = sound
                                                AlarmPlayer.playPreview(context, sound)
                                            }
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = Color(0xFF0072FF),
                                            unselectedColor = appColors.textSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = sound.getLocalizedName(userSettings.appLanguage),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color(0xFF0072FF) else appColors.textPrimary
                                        )
                                        if (isCustom) {
                                            Text(
                                                text = if (!userSettings.customAlarmName.isNullOrBlank())
                                                    "🎵 ${userSettings.customAlarmName}"
                                                else
                                                    strings.customAlarmNotSet,
                                                fontFamily = HindSiliguriFontFamily,
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color(0xFF0072FF) else appColors.textMuted
                                            )
                                        }
                                    }
                                    if (previewingSound == sound) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = strings.soundPlaying,
                                            tint = Color(0xFF0072FF),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (isCustom && isSelected) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 44.dp, end = 8.dp, bottom = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (appColors.isDark) Color(0xFF1E3560) else Color(0xFFEFF6FF))
                                                .border(1.dp, Color(0xFF0072FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    ringtonePickerLauncher.launch(
                                                        CustomAlarmAudioHelper.createRingtonePickerIntent(userSettings.customAlarmUri)
                                                    )
                                                }
                                                .padding(vertical = 6.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "🔔 " + strings.chooseRingtone,
                                                fontFamily = HindSiliguriFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0072FF)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (appColors.isDark) Color(0xFF1E3560) else Color(0xFFEFF6FF))
                                                .border(1.dp, Color(0xFF0072FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .clickable {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    audioFilePickerLauncher.launch("audio/*")
                                                }
                                                .padding(vertical = 6.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "📁 " + strings.chooseAudioFile,
                                                fontFamily = HindSiliguriFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = Color(0xFF0072FF)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        AlarmPlayer.stopPreview()
                        showSoundDialog = false
                    }) {
                        Text(
                            text = strings.ok,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0072FF)
                        )
                    }
                },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun AlarmDistanceRadioOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) (if (appColors.isDark) Color(0x220072FF) else Color(0xFFEBF5FF)) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF0072FF) else appColors.textMuted,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 16.sp,
                color = if (isSelected) Color(0xFF0072FF) else appColors.textPrimary
            )
        }
    }
}
