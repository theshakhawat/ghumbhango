package com.shakhawat.ghhumbhango.ui.map

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.data.repository.DestinationRepository
import com.shakhawat.ghhumbhango.data.repository.GeocodeResult
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@Composable
fun MapLocationPinPickerDialog(
    initialLat: Double = 23.7508,
    initialLng: Double = 90.3938,
    initialName: String = "",
    initialAddress: String = "",
    currentLocation: UserLocation? = null,
    onDismiss: () -> Unit,
    onLocationPinned: (name: String, address: String, lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DestinationRepository(context) }

    var pickedLat by remember { mutableStateOf(initialLat) }
    var pickedLng by remember { mutableStateOf(initialLng) }
    var pickedName by remember { mutableStateOf(initialName.ifBlank { "নির্বাচিত স্থান" }) }
    var pickedAddress by remember { mutableStateOf(initialAddress) }
    var isGeocoding by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Destination>>(emptyList()) }
    var isSearchDropdownVisible by remember { mutableStateOf(false) }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var markerRef by remember { mutableStateOf<Marker?>(null) }

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

    // Function to update coordinates and reverse-geocode address
    fun updatePinnedLocation(lat: Double, lng: Double, customName: String? = null, customAddr: String? = null) {
        pickedLat = lat
        pickedLng = lng
        markerRef?.position = GeoPoint(lat, lng)
        mapViewRef?.invalidate()

        if (customName != null && customAddr != null) {
            pickedName = customName
            pickedAddress = customAddr
            return
        }

        coroutineScope.launch {
            isGeocoding = true
            try {
                val (name, addr) = repository.reverseGeocode(lat, lng)
                pickedName = name
                pickedAddress = addr ?: "$name, বাংলাদেশ"
            } catch (_: Exception) {
                pickedName = "মানচিত্রের স্থান"
                pickedAddress = String.format(Locale.US, "%.4f° N, %.4f° E", lat, lng)
            } finally {
                isGeocoding = false
            }
        }
    }

    // Search query listener
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            val localResults = repository.searchLocalImmediate(q)
            if (localResults.isNotEmpty()) {
                searchResults = localResults.take(5)
                isSearchDropdownVisible = true
            }

            searchJob?.cancel()
            searchJob = coroutineScope.launch {
                delay(300)
                isSearchingOnline = true
                val onlineRes = repository.searchOnline(q)
                isSearchingOnline = false
                if (onlineRes is GeocodeResult.Success && onlineRes.destinations.isNotEmpty()) {
                    val combined = (localResults + onlineRes.destinations).distinctBy { it.name }.take(6)
                    searchResults = combined
                    isSearchDropdownVisible = true
                }
            }
        } else {
            searchResults = emptyList()
            isSearchDropdownVisible = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Fullscreen Interactive Map
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                        val startPt = GeoPoint(pickedLat, pickedLng)
                        controller.setZoom(16.0)
                        controller.setCenter(startPt)

                        // Destination Pin Marker
                        val pin = Marker(this).apply {
                            position = startPt
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = pickedName
                            icon = AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_destination)
                        }
                        markerRef = pin
                        overlays.add(pin)

                        // User GPS marker if available
                        currentLocation?.let { uLoc ->
                            val uMarker = Marker(this).apply {
                                position = GeoPoint(uLoc.latitude, uLoc.longitude)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                title = strings.yourLocation
                                icon = AppCompatResources.getDrawable(ctx, R.drawable.ic_map_pin_user)
                            }
                            overlays.add(uMarker)
                        }

                        // Tap event to place/move pin
                        val eventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { pt ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isSearchDropdownVisible = false
                                    keyboardController?.hide()
                                    updatePinnedLocation(pt.latitude, pt.longitude)
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        }
                        overlays.add(0, MapEventsOverlay(eventsReceiver))
                    }
                },
                update = { map ->
                    markerRef?.position = GeoPoint(pickedLat, pickedLng)
                    map.invalidate()
                }
            )

            // 2. Top Header Container: Back button + Search bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back/Close Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                                onDismiss()
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

                    Spacer(modifier = Modifier.width(10.dp))

                    // Search Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = appColors.shadowColor,
                                spotColor = appColors.shadowColor
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(appColors.cardBackground)
                            .border(1.dp, appColors.cardBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 15.sp,
                                    color = appColors.textPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    keyboardController?.hide()
                                }),
                                cursorBrush = SolidColor(Color(0xFF0072FF)),
                                modifier = Modifier.weight(1f),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "গন্তব্য বা এলাকা খুঁজুন...",
                                            fontFamily = HindSiliguriFontFamily,
                                            fontSize = 14.sp,
                                            color = appColors.textMuted
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (isSearchingOnline) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0072FF)
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = appColors.textMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            isSearchDropdownVisible = false
                                        }
                                )
                            }
                        }
                    }
                }

                // Search Results Dropdown Card
                AnimatedVisibility(
                    visible = isSearchDropdownVisible && searchResults.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = appColors.shadowColor,
                                spotColor = appColors.shadowColor
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(appColors.cardBackground)
                            .border(1.dp, appColors.cardBorder, RoundedCornerShape(16.dp))
                            .heightIn(max = 240.dp)
                    ) {
                        LazyColumn {
                            items(searchResults) { dest ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            searchQuery = dest.name
                                            isSearchDropdownVisible = false
                                            keyboardController?.hide()
                                            updatePinnedLocation(dest.latitude, dest.longitude, dest.name, dest.address)
                                            mapViewRef?.controller?.animateTo(GeoPoint(dest.latitude, dest.longitude))
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF0072FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dest.name,
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = appColors.textPrimary
                                        )
                                        if (!dest.address.isNullOrBlank()) {
                                            Text(
                                                text = dest.address,
                                                fontFamily = HindSiliguriFontFamily,
                                                fontSize = 12.sp,
                                                color = appColors.textMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = appColors.divider, thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // 3. Floating GPS button (Bottom Right above confirmation card)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 185.dp)
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
                        currentLocation?.let { uLoc ->
                            updatePinnedLocation(uLoc.latitude, uLoc.longitude)
                            mapViewRef?.controller?.animateTo(GeoPoint(uLoc.latitude, uLoc.longitude))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    tint = Color(0xFF0072FF),
                    modifier = Modifier.size(22.dp)
                )
            }

            // 4. Floating Bottom Confirmation Card
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column {
                    // Pinned location title & address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0072FF).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পিনকৃত লোকেশন",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = appColors.textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pickedName,
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textPrimary
                            )
                            if (isGeocoding) {
                                Text(
                                    text = "ঠিকানা শনাক্ত করা হচ্ছে...",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0072FF)
                                )
                            } else if (pickedAddress.isNotBlank()) {
                                Text(
                                    text = pickedAddress,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.5.sp,
                                    color = appColors.textMuted,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Confirmation Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0xFF0072FF))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0072FF))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onLocationPinned(pickedName, pickedAddress, pickedLat, pickedLng)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "এই লোকেশন পিন নিশ্চিত করুন",
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
    }
}

