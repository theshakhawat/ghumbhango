package com.shakhawat.ghhumbhango.ui.journey

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.data.repository.RouteRepository
import com.shakhawat.ghhumbhango.domain.model.ActiveJourney
import com.shakhawat.ghhumbhango.domain.model.AppLanguage
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.domain.util.LocalizationHelper
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlin.math.roundToInt
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun JourneyLiveTrackingScreen(
    journey: ActiveJourney,
    onStopJourney: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val appLanguage = LocalAppLanguage.current
    val isBangla = appLanguage == AppLanguage.BANGLA
    val appColors = LocalAppColors.current

    var showStopConfirmDialog by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var routePoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    var routeLineRef by remember { mutableStateOf<Polyline?>(null) }
    var routeCasingRef by remember { mutableStateOf<Polyline?>(null) }
    var userMarkerRef by remember { mutableStateOf<Marker?>(null) }
    var hasCenteredRoute by remember { mutableStateOf(false) }

    // Fetch real road route from OSRM
    LaunchedEffect(journey.destination.latitude, journey.destination.longitude, journey.userLocation != null) {
        if (journey.userLocation != null || routePoints.isEmpty()) {
            val startLat = journey.userLocation?.latitude ?: (journey.destination.latitude - 0.03)
            val startLon = journey.userLocation?.longitude ?: (journey.destination.longitude - 0.02)
            val result = RouteRepository.getRoadRoute(
                startLat = startLat,
                startLon = startLon,
                destLat = journey.destination.latitude,
                destLon = journey.destination.longitude
            )
            if (result.points.size >= 2) {
                routePoints = result.points
                routeCasingRef?.setPoints(result.points)
                routeLineRef?.setPoints(result.points)
                mapViewRef?.invalidate()
                if (!hasCenteredRoute) {
                    hasCenteredRoute = true
                    try {
                        val bBox = BoundingBox.fromGeoPoints(result.points)
                        mapViewRef?.zoomToBoundingBox(bBox, true, 130)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Dynamic real-time remaining distance in meters
    val remainingMeters = remember(journey.userLocation, journey.destination, journey.currentDistanceMeters) {
        if (journey.userLocation != null) {
            DistanceCalculator.calculateDistance(journey.userLocation, journey.destination)
        } else {
            journey.currentDistanceMeters
        }
    }

    // Formatted real-time remaining distance
    val formattedRemainingDist = remember(remainingMeters, isBangla) {
        if (remainingMeters != null) {
            val formatted = LocalizationHelper.formatDistance(remainingMeters, isBangla)
            if (isBangla) "$formatted বাকি" else "$formatted remaining"
        } else {
            if (isBangla) "দূরত্ব গণনা হচ্ছে..." else "Calculating distance..."
        }
    }

    // Dynamic real-time speed in km/h
    val rawSpeedMps = journey.userLocation?.speedMps ?: 0f
    val liveSpeedKmh = remember(rawSpeedMps) {
        if (rawSpeedMps >= 0.8f) { // ~3 km/h or more
            (rawSpeedMps * 3.6f).roundToInt()
        } else {
            0
        }
    }
    val speedText = remember(liveSpeedKmh, isBangla) {
        if (isBangla) {
            "${LocalizationHelper.toLocalizedDigits(liveSpeedKmh, true)} কিমি/ঘণ্টা"
        } else {
            "$liveSpeedKmh km/h"
        }
    }

    // Dynamic real-time estimated arrival time (ETA)
    val estimatedTimeText = remember(remainingMeters, liveSpeedKmh, isBangla) {
        if (remainingMeters == null) {
            if (isBangla) "গণনা হচ্ছে..." else "Calculating..."
        } else if (remainingMeters <= 50f) {
            if (isBangla) "গন্তব্যে পৌঁছে গেছেন" else "Arrived at destination"
        } else {
            // If vehicle is moving at >= 10 km/h, use real speed; otherwise assume average city transit pace (25 km/h)
            val effectiveSpeedKmh = if (liveSpeedKmh >= 10) liveSpeedKmh.toFloat() else 25f
            val speedMps = (effectiveSpeedKmh * 1000f) / 60f
            val totalMinutes = (remainingMeters / speedMps).roundToInt().coerceAtLeast(1)

            if (totalMinutes < 60) {
                if (isBangla) {
                    "${LocalizationHelper.toLocalizedDigits(totalMinutes, true)} মিনিট"
                } else {
                    "$totalMinutes min${if (totalMinutes > 1) "s" else ""}"
                }
            } else {
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                if (isBangla) {
                    if (mins == 0) {
                        "${LocalizationHelper.toLocalizedDigits(hours, true)} ঘণ্টা"
                    } else {
                        "${LocalizationHelper.toLocalizedDigits(hours, true)} ঘণ্টা ${LocalizationHelper.toLocalizedDigits(mins, true)} মিনিট"
                    }
                } else {
                    if (mins == 0) {
                        "$hours hr${if (hours > 1) "s" else ""}"
                    } else {
                        "$hours hr $mins min${if (mins > 1) "s" else ""}"
                    }
                }
            }
        }
    }

    // Initialize osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("ghumbhango_osm", Context.MODE_PRIVATE)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.HOME,
                onHomeClick = onNavigateToHome,
                onSavedClick = { /* Active Journey */ },
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
            // 1. Fullscreen OSMDroid Map
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                        val destPoint = GeoPoint(journey.destination.latitude, journey.destination.longitude)
                        val userPoint = if (journey.userLocation != null) {
                            GeoPoint(journey.userLocation.latitude, journey.userLocation.longitude)
                        } else {
                            GeoPoint(journey.destination.latitude - 0.03, journey.destination.longitude - 0.02)
                        }

                        // Outer halo/casing line for route
                        val casing = Polyline(this).apply {
                            outlinePaint.color = android.graphics.Color.parseColor("#400072FF")
                            outlinePaint.strokeWidth = 16f
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                            if (routePoints.size >= 2) {
                                setPoints(routePoints)
                            } else {
                                addPoint(userPoint)
                                addPoint(destPoint)
                            }
                        }
                        routeCasingRef = casing
                        overlays.add(casing)

                        // Inner vibrant electric blue road route
                        val line = Polyline(this).apply {
                            outlinePaint.color = android.graphics.Color.parseColor("#0072FF")
                            outlinePaint.strokeWidth = 10f
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                            if (routePoints.size >= 2) {
                                setPoints(routePoints)
                            } else {
                                addPoint(userPoint)
                                addPoint(destPoint)
                            }
                        }
                        routeLineRef = line
                        overlays.add(line)

                        // Destination Marker
                        val destMarker = Marker(this).apply {
                            position = destPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = journey.destination.name
                            icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_destination)
                        }
                        overlays.add(destMarker)

                        // User Marker
                        val userMarker = Marker(this).apply {
                            position = userPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = strings.yourLocation
                            icon = androidx.appcompat.content.res.AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_user)
                        }
                        userMarkerRef = userMarker
                        overlays.add(userMarker)

                        post {
                            if (width > 0 && height > 0) {
                                try {
                                    if (routePoints.size >= 2) {
                                        val bBox = BoundingBox.fromGeoPoints(routePoints)
                                        zoomToBoundingBox(bBox, false, 130)
                                    } else {
                                        val minLat = minOf(userPoint.latitude, destPoint.latitude) - 0.008
                                        val maxLat = maxOf(userPoint.latitude, destPoint.latitude) + 0.008
                                        val minLon = minOf(userPoint.longitude, destPoint.longitude) - 0.008
                                        val maxLon = maxOf(userPoint.longitude, destPoint.longitude) + 0.008
                                        zoomToBoundingBox(BoundingBox(maxLat, maxLon, minLat, minLon), false, 100)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                },
                update = { map ->
                    val userPoint = if (journey.userLocation != null) {
                        GeoPoint(journey.userLocation.latitude, journey.userLocation.longitude)
                    } else null
                    if (userPoint != null) {
                        userMarkerRef?.position = userPoint
                        map.invalidate()
                    }
                }
            )

            // 2. Top Header Card: Journey active + Destination + End Journey button
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column {
                    // Row 1: Bus Icon + Journey Active + End Journey Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE8F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = Color(0xFF0072FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = strings.journeyActive,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = appColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Red Pill Button: End Journey (Compact, single-line, non-wrapping)
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(14.dp),
                                    spotColor = Color(0xFFEF4444)
                                )
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEF4444))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showStopConfirmDialog = true
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.endJourney,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Green Target Icon + Destination Name + Remaining Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = journey.destination.name.ifBlank { strings.destinationLabel },
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = formattedRemainingDist,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0072FF),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 3. Floating Recenter / Location Button (Right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 120.dp)
                    .size(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(CircleShape)
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        journey.userLocation?.let { loc ->
                            mapViewRef?.controller?.animateTo(GeoPoint(loc.latitude, loc.longitude))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = Color(0xFF0072FF),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Bottom Metrics Floating Status Card ("আনুমানিক সময়" & "গতি")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: আনুমানিক সময়
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (appColors.isDark) Color(0x2210B981) else Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = strings.estimatedTime,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = appColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = estimatedTimeText,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = appColors.textPrimary
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(appColors.divider)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right Column: Speed
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE8F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = strings.speed,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = appColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = speedText,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = appColors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Stop Journey Confirmation Dialog
        if (showStopConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showStopConfirmDialog = false },
                title = {
                    Text(
                        text = strings.endJourneyConfirmTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.endJourneyConfirmText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showStopConfirmDialog = false
                            onStopJourney()
                        }
                    ) {
                        Text(
                            text = strings.confirm,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStopConfirmDialog = false }) {
                        Text(
                            text = strings.cancel,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textMuted
                        )
                    }
                },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
