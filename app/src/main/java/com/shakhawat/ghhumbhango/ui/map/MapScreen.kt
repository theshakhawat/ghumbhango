package com.shakhawat.ghhumbhango.ui.map

import android.content.Context
import android.graphics.Paint
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shakhawat.ghhumbhango.data.location.DefaultLocationClient
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.data.repository.DestinationRepository
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

private data class SearchSuggestionItem(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isRecent: Boolean
)

@Composable
fun MapScreen(
    currentLocation: UserLocation?,
    initialDestination: Destination?,
    initialAlarmDistance: AlarmDistance,
    isPickingMode: Boolean,
    onConfirmDestination: (Destination, AlarmDistance) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = onBackClick,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    val appLanguage = LocalAppLanguage.current
    val appColors = LocalAppColors.current
    val repository = remember { DestinationRepository(context) }
    val locationClient = remember { DefaultLocationClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedDestination by remember { mutableStateOf(initialDestination) }
    var selectedDistance by remember { mutableStateOf(initialAlarmDistance) }
    var pickedPoint by remember {
        mutableStateOf(
            if (initialDestination != null) {
                GeoPoint(initialDestination.latitude, initialDestination.longitude)
            } else if (currentLocation != null) {
                GeoPoint(currentLocation.latitude, currentLocation.longitude)
            } else {
                GeoPoint(23.7508, 90.3938) // Dhaka Farmgate center
            }
        )
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isSatelliteLayer by remember { mutableStateOf(false) }

    var hasLocationPermission by remember { mutableStateOf(locationClient.hasLocationPermission()) }
    var isGpsEnabled by remember { mutableStateOf(locationClient.isGpsEnabled()) }
    var showLocationRequiredDialog by remember { mutableStateOf(!hasLocationPermission || !isGpsEnabled) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fine || coarse
        isGpsEnabled = locationClient.isGpsEnabled()
        if (hasLocationPermission && isGpsEnabled) {
            showLocationRequiredDialog = false
            coroutineScope.launch {
                locationClient.getLastKnownLocation()?.let { loc ->
                    val pt = GeoPoint(loc.latitude, loc.longitude)
                    pickedPoint = pt
                    mapViewRef?.controller?.animateTo(pt)
                    mapViewRef?.controller?.setZoom(16.5)
                }
            }
        } else if (hasLocationPermission && !isGpsEnabled) {
            showLocationRequiredDialog = true
        }
    }

    fun promptEnableLocation() {
        hasLocationPermission = locationClient.hasLocationPermission()
        isGpsEnabled = locationClient.isGpsEnabled()
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!isGpsEnabled) {
            try {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        } else {
            showLocationRequiredDialog = false
        }
    }

    // Auto-check on entering MapScreen
    LaunchedEffect(Unit) {
        hasLocationPermission = locationClient.hasLocationPermission()
        isGpsEnabled = locationClient.isGpsEnabled()
        if (!hasLocationPermission) {
            showLocationRequiredDialog = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!isGpsEnabled) {
            showLocationRequiredDialog = true
        } else {
            showLocationRequiredDialog = false
            coroutineScope.launch {
                locationClient.getLastKnownLocation()?.let { loc ->
                    val pt = GeoPoint(loc.latitude, loc.longitude)
                    pickedPoint = pt
                    mapViewRef?.controller?.animateTo(pt)
                }
            }
        }
    }

    // Refresh location on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = locationClient.hasLocationPermission()
                isGpsEnabled = locationClient.isGpsEnabled()
                if (hasLocationPermission && isGpsEnabled) {
                    showLocationRequiredDialog = false
                    coroutineScope.launch {
                        locationClient.getLastKnownLocation()?.let { loc ->
                            val pt = GeoPoint(loc.latitude, loc.longitude)
                            pickedPoint = pt
                            mapViewRef?.controller?.animateTo(pt)
                            mapViewRef?.controller?.setZoom(16.5)
                        }
                    }
                } else {
                    showLocationRequiredDialog = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSuggestionsCardVisible by remember { mutableStateOf(true) }
    var isSearchingOnline by remember { mutableStateOf(false) }

    val userPrefsRepo = remember { com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository.getInstance(context) }
    val savedRecentSearches by userPrefsRepo.recentSearches.collectAsStateWithLifecycle(initialValue = emptyList())
    val recentSearches = remember(savedRecentSearches) {
        savedRecentSearches.map { d ->
            SearchSuggestionItem(
                id = d.id,
                name = d.name,
                latitude = d.latitude,
                longitude = d.longitude,
                address = d.address ?: "বাংলাদেশ",
                isRecent = true
            )
        }.take(4)
    }

    var onlineSearchResults by remember { mutableStateOf<List<SearchSuggestionItem>>(emptyList()) }

    // Instant local results (0ms) from comprehensive built-in/saved stops
    val instantLocalResults = remember(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.length >= 2) {
            repository.searchLocalImmediate(trimmed).map { d ->
                SearchSuggestionItem(
                    id = d.id,
                    name = d.name,
                    latitude = d.latitude,
                    longitude = d.longitude,
                    address = d.address ?: "বাংলাদেশ",
                    isRecent = false
                )
            }
        } else {
            emptyList()
        }
    }

    // Fast debounced search as user types
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.length >= 2) {
            kotlinx.coroutines.delay(250) // Debounce rapid keystrokes
            isSearchingOnline = true
            val result = repository.searchOnline(trimmed)
            isSearchingOnline = false
            if (result is com.shakhawat.ghhumbhango.data.repository.GeocodeResult.Success && result.destinations.isNotEmpty()) {
                onlineSearchResults = result.destinations.map { d ->
                    SearchSuggestionItem(
                        id = d.id,
                        name = d.name,
                        latitude = d.latitude,
                        longitude = d.longitude,
                        address = d.address ?: "বাংলাদেশ",
                        isRecent = false
                    )
                }
            }
        } else {
            onlineSearchResults = emptyList()
        }
    }


    // Initialize osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("ghumbhango_osm", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = "GhumBhango-Travel-Alarm/1.0"
    }

    // Function to select and focus on a destination
    fun selectAndFocusDestination(dest: Destination) {
        if (!hasLocationPermission || !isGpsEnabled) {
            showLocationRequiredDialog = true
            promptEnableLocation()
            return
        }
        selectedDestination = dest
        pickedPoint = GeoPoint(dest.latitude, dest.longitude)
        isSuggestionsCardVisible = false
        keyboardController?.hide()
        coroutineScope.launch {
            userPrefsRepo.addRecentSearch(dest)
        }
        mapViewRef?.let { map ->
            map.controller.animateTo(pickedPoint)
            map.controller.setZoom(16.0)
        }
    }

    // Perform search when user submits
    fun performSearch() {
        if (!hasLocationPermission || !isGpsEnabled) {
            showLocationRequiredDialog = true
            promptEnableLocation()
            return
        }
        val trimmed = searchQuery.trim()
        if (trimmed.isBlank()) {
            isSuggestionsCardVisible = true
            return
        }
        // If we already have instant local matches, select immediately (0ms)
        if (instantLocalResults.isNotEmpty()) {
            val first = instantLocalResults.first()
            selectAndFocusDestination(
                Destination(
                    id = first.id,
                    name = first.name,
                    latitude = first.latitude,
                    longitude = first.longitude,
                    address = first.address
                )
            )
            return
        }
        if (onlineSearchResults.isNotEmpty()) {
            val first = onlineSearchResults.first()
            selectAndFocusDestination(
                Destination(
                    id = first.id,
                    name = first.name,
                    latitude = first.latitude,
                    longitude = first.longitude,
                    address = first.address
                )
            )
            return
        }
        isSearchingOnline = true
        coroutineScope.launch {
            val result = repository.searchOnline(trimmed)
            isSearchingOnline = false
            if (result is com.shakhawat.ghhumbhango.data.repository.GeocodeResult.Success && result.destinations.isNotEmpty()) {
                val first = result.destinations.first()
                selectAndFocusDestination(first)
            }
        }
    }

    // Clean up MapView on exit
    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onDetach()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        // 1. Fullscreen Interactive Map View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                    val startPoint = if (selectedDestination != null) {
                        GeoPoint(selectedDestination!!.latitude, selectedDestination!!.longitude)
                    } else if (currentLocation != null) {
                        GeoPoint(currentLocation.latitude, currentLocation.longitude)
                    } else {
                        GeoPoint(23.7508, 90.3938)
                    }

                    controller.setZoom(15.2)
                    controller.setCenter(startPoint)

                    // Map tap receiver
                    if (isPickingMode) {
                        val eventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                if (!hasLocationPermission || !isGpsEnabled) {
                                    showLocationRequiredDialog = true
                                    promptEnableLocation()
                                    return true
                                }
                                p?.let { pt ->
                                    pickedPoint = pt
                                    isSuggestionsCardVisible = false
                                    coroutineScope.launch {
                                        val (name, addr) = repository.reverseGeocode(pt.latitude, pt.longitude)
                                        val dest = Destination(
                                            id = "tap_${System.currentTimeMillis()}",
                                            name = name,
                                            latitude = pt.latitude,
                                            longitude = pt.longitude,
                                            address = addr
                                        )
                                        selectedDestination = dest
                                    }
                                }
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        }
                        overlays.add(0, MapEventsOverlay(eventsReceiver))
                    }

                    mapViewRef = this
                }
            },
            update = { mapView ->
                val eventOverlays = mapView.overlays.filterIsInstance<MapEventsOverlay>()
                mapView.overlays.clear()
                mapView.overlays.addAll(eventOverlays)

                // Draw User Location Pin & Circle
                currentLocation?.let { userLoc ->
                    val userPoint = GeoPoint(userLoc.latitude, userLoc.longitude)

                    val userAccuracyCircle = Polygon(mapView).apply {
                        points = Polygon.pointsAsCircle(userPoint, userLoc.accuracyMeters.coerceAtLeast(30f).toDouble())
                        fillPaint.color = android.graphics.Color.argb(35, 0, 114, 255)
                        fillPaint.style = Paint.Style.FILL
                        outlinePaint.color = android.graphics.Color.argb(120, 0, 114, 255)
                        outlinePaint.strokeWidth = 2f
                    }
                    mapView.overlays.add(userAccuracyCircle)

                    val userMarker = Marker(mapView).apply {
                        position = userPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = strings.yourLocation
                        val userIcon = AppCompatResources.getDrawable(context, R.drawable.ic_map_pin_user)
                        icon = userIcon
                    }
                    mapView.overlays.add(userMarker)
                }

                // Draw Destination Pin & Proximity Circle
                selectedDestination?.let { dest ->
                    val destPoint = GeoPoint(dest.latitude, dest.longitude)

                    val destCircle = Polygon(mapView).apply {
                        points = Polygon.pointsAsCircle(destPoint, selectedDistance.meters.toDouble())
                        fillPaint.color = android.graphics.Color.argb(45, 0, 114, 255)
                        fillPaint.style = Paint.Style.FILL
                        outlinePaint.color = android.graphics.Color.argb(200, 0, 114, 255)
                        outlinePaint.strokeWidth = 4f
                    }
                    mapView.overlays.add(destCircle)

                    val destMarker = Marker(mapView).apply {
                        position = destPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = dest.name
                        snippet = "${strings.alarmDistanceHeader}: ${selectedDistance.getLocalizedLabel(appLanguage)}"
                        val destIcon = AppCompatResources.getDrawable(context, R.drawable.ic_map_pin_destination)
                        icon = destIcon
                    }
                    mapView.overlays.add(destMarker)
                }

                mapView.invalidate()
            }
        )

        // 2. Top Search Bar Floating Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (rounded square card on left)
                Box(
                    modifier = Modifier
                        .size(46.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Search Input Box (rounded bar)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            isSuggestionsCardVisible = true
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = appColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = strings.searchPlaceholder,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = appColors.textMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    isSuggestionsCardVisible = true
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = appColors.textPrimary
                                ),
                                cursorBrush = SolidColor(Color(0xFF0072FF)),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = appColors.textMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        searchQuery = ""
                                    }
                            )
                        }
                    }
                }
            }

            val filteredSuggestions = remember(searchQuery, onlineSearchResults, instantLocalResults, recentSearches) {
                if (searchQuery.isBlank()) {
                    recentSearches
                } else {
                    val combined = mutableListOf<SearchSuggestionItem>()
                    // 1. Add instant local matches immediately (0ms)
                    combined.addAll(instantLocalResults)
                    // 2. Add online search results without duplicating names
                    for (item in onlineSearchResults) {
                        if (combined.none { it.name.trim().equals(item.name.trim(), ignoreCase = true) }) {
                            combined.add(item)
                        }
                    }
                    // 3. Fallback to matching recent searches if still empty
                    if (combined.isEmpty()) {
                        combined.addAll(
                            recentSearches.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.address.contains(searchQuery, ignoreCase = true)
                            }
                        )
                    }
                    combined.take(6)
                }
            }

            val shouldShowSuggestions = isSuggestionsCardVisible && (
                if (searchQuery.isBlank()) recentSearches.isNotEmpty() else filteredSuggestions.isNotEmpty()
            )

            // 3. Suggestions Floating Card anchored beneath the search bar
            AnimatedVisibility(
                visible = shouldShowSuggestions,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 56.dp) // Aligns with search field and button
                        .padding(top = 8.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isBlank()) Icons.Default.Schedule else Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (searchQuery.isBlank()) strings.recentSearchesHeader else strings.mapResultsHeader,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = appColors.textMuted
                            )
                            if (isSearchingOnline) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0072FF),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        filteredSuggestions.forEachIndexed { index, item ->
                            SuggestionRow(
                                item = item,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val destination = Destination(
                                        id = item.id,
                                        name = item.name,
                                        latitude = item.latitude,
                                        longitude = item.longitude,
                                        address = item.address
                                    )
                                    selectAndFocusDestination(destination)
                                }
                            )

                            // Show subtle horizontal divider
                            if (index < filteredSuggestions.size - 1) {
                                HorizontalDivider(
                                    color = appColors.divider,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Floating Map Controls (Bottom-Right: My Location & Layers)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // My Location Target Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (!hasLocationPermission || !isGpsEnabled) {
                            showLocationRequiredDialog = true
                            promptEnableLocation()
                        } else {
                            currentLocation?.let { loc ->
                                val pt = GeoPoint(loc.latitude, loc.longitude)
                                mapViewRef?.controller?.animateTo(pt)
                                mapViewRef?.controller?.setZoom(16.5)
                            } ?: run {
                                coroutineScope.launch {
                                    locationClient.getLastKnownLocation()?.let { loc ->
                                        val pt = GeoPoint(loc.latitude, loc.longitude)
                                        mapViewRef?.controller?.animateTo(pt)
                                        mapViewRef?.controller?.setZoom(16.5)
                                    }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_my_location),
                    contentDescription = strings.yourLocation,
                    tint = Color(0xFF0072FF),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Map Layers Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isSatelliteLayer = !isSatelliteLayer
                        mapViewRef?.setTileSource(
                            if (isSatelliteLayer) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_layers),
                    contentDescription = strings.mapLayers,
                    tint = appColors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 5. Selected Destination Confirmation Sheet (Appears when location is chosen)
        if (selectedDestination != null && !isSuggestionsCardVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 86.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedDestination!!.name,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = appColors.textPrimary
                                )
                                if (!selectedDestination!!.address.isNullOrBlank()) {
                                    Text(
                                        text = selectedDestination!!.address!!,
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = appColors.textMuted
                                    )
                                }
                            }

                            // Close selection button
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = appColors.textMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        selectedDestination = null
                                        isSuggestionsCardVisible = true
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Proximity Distance Selector Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AlarmDistance.values().forEach { distance ->
                                val isSelected = selectedDistance == distance
                                val isDefault = distance == AlarmDistance.DEFAULT
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) Color(0xFF0072FF)
                                            else if (isDefault) appColors.topBarButtonBg
                                            else appColors.inputBg
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else if (isDefault) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF0072FF)
                                                    else if (isDefault) Color(0xFF0072FF)
                                                    else appColors.cardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedDistance = distance
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = distance.getLocalizedLabel(appLanguage),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = if (isSelected || isDefault) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White
                                                else if (isDefault) Color(0xFF0072FF)
                                                else appColors.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Destination Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(6.dp, RoundedCornerShape(25.dp), spotColor = Color(0xFF0072FF))
                                .clip(RoundedCornerShape(25.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0072FF), Color(0xFF0052D4))
                                    )
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!hasLocationPermission || !isGpsEnabled) {
                                        showLocationRequiredDialog = true
                                        promptEnableLocation()
                                    } else {
                                        onConfirmDestination(selectedDestination!!, selectedDistance)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = strings.confirmDestination,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Floating Bottom Navigation Bar (Unified Component)
        AppBottomNavigationBar(
            currentTab = NavTab.HOME,
            onHomeClick = onNavigateToHome,
            onSavedClick = { isSuggestionsCardVisible = true },
            onHistoryClick = onNavigateToHistory,
            onSettingsClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Location Required Dialog (Pathao style - cannot stay without location on)
        if (showLocationRequiredDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLocationRequiredDialog = false
                    onBackClick()
                },
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
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                            "লোকেশন অন করা আবশ্যক"
                        else
                            "Location Required",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = appColors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                            "ম্যাপে আপনার সঠিক অবস্থান দেখতে এবং গন্তব্য নির্বাচন করতে ডিভাইসের লোকেশন ও জিপিএস চালু করা আবশ্যক। লোকেশন অন ছাড়া এই পেজে প্রবেশ করা যাবে না।"
                        else
                            "Device location and GPS must be enabled to view your position and pick destinations on the map. You cannot enter this page without enabling location.",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = appColors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            promptEnableLocation()
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
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                "লোকেশন চালু করুন"
                            else
                                "Turn On Location",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLocationRequiredDialog = false
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (strings is com.shakhawat.ghhumbhango.ui.localization.BengaliStrings)
                                "ফিরে যান"
                            else
                                "Go Back",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = appColors.textMuted
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    item: SearchSuggestionItem,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon (Clock for recent, Pin for locations)
        Icon(
            imageVector = if (item.isRecent) Icons.Default.Schedule else Icons.Default.Place,
            contentDescription = null,
            tint = appColors.textMuted,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Title
        Text(
            text = item.name,
            fontFamily = HindSiliguriFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = appColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        // Trailing ↗ Arrow
        Icon(
            painter = painterResource(id = R.drawable.ic_north_east),
            contentDescription = null,
            tint = appColors.textMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
