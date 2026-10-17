package com.shakhawat.ghhumbhango.ui.destination

import android.Manifest
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shakhawat.ghhumbhango.data.location.DefaultLocationClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
import com.shakhawat.ghhumbhango.data.repository.DestinationRepository
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchStoppageItem(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val iconBgColor: Color = Color(0xFFE0F2FE),
    val pinColor: Color = Color(0xFF0072FF),
    val categoryKey: String = "general",
    val isSaved: Boolean = false
)

@Composable
fun DestinationSearchScreen(
    onSelectDestination: (Destination) -> Unit,
    onPickOnMap: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current
    val repository = remember { DestinationRepository(context) }
    val userPrefsRepo = remember { UserPreferencesRepository.getInstance(context) }
    val locationClient = remember { DefaultLocationClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var hasLocationPermission by remember { mutableStateOf(locationClient.hasLocationPermission()) }
    var isGpsEnabled by remember { mutableStateOf(locationClient.isGpsEnabled()) }
    var showLocationOffDialog by remember { mutableStateOf(false) }
    var pendingActionOnLocationEnabled by remember { mutableStateOf<(() -> Unit)?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fine || coarse
        isGpsEnabled = locationClient.isGpsEnabled()
        if (hasLocationPermission && !isGpsEnabled) {
            showLocationOffDialog = true
        } else if (hasLocationPermission && isGpsEnabled) {
            showLocationOffDialog = false
            pendingActionOnLocationEnabled?.invoke()
            pendingActionOnLocationEnabled = null
        }
    }

    fun promptEnableLocation(onSuccess: (() -> Unit)? = null) {
        hasLocationPermission = locationClient.hasLocationPermission()
        isGpsEnabled = locationClient.isGpsEnabled()
        pendingActionOnLocationEnabled = onSuccess
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!isGpsEnabled) {
            showLocationOffDialog = true
        } else {
            onSuccess?.invoke()
            pendingActionOnLocationEnabled = null
        }
    }

    // Refresh location on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasLocationPermission = locationClient.hasLocationPermission()
                isGpsEnabled = locationClient.isGpsEnabled()
                if (hasLocationPermission && isGpsEnabled) {
                    showLocationOffDialog = false
                    pendingActionOnLocationEnabled?.invoke()
                    pendingActionOnLocationEnabled = null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val savedDestinationsList by repository.savedDestinations.collectAsStateWithLifecycle(initialValue = emptyList())
    val customCategoriesSet by userPrefsRepo.customCategories.collectAsStateWithLifecycle(initialValue = setOf("বাসা", "কাজ"))
    val recentSearchesList by userPrefsRepo.recentSearches.collectAsStateWithLifecycle(initialValue = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterKey by remember { mutableStateOf("all") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var onlineResults by remember { mutableStateOf<List<Destination>>(emptyList()) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var destinationToSave by remember { mutableStateOf<Destination?>(null) }

    // Categories list: "all", "recent", "বাসা", "কাজ", plus any custom categories
    val baseCategories = listOf("বাসা", "কাজ")
    val allCategoryNames = remember(customCategoriesSet) {
        val list = mutableListOf<String>()
        list.addAll(baseCategories)
        for (c in customCategoriesSet) {
            if (!list.contains(c)) list.add(c)
        }
        list
    }

    // Filter items based on selected category or search query
    val displayedItems = remember(
        searchQuery,
        selectedFilterKey,
        onlineResults,
        savedDestinationsList,
        recentSearchesList
    ) {
        if (searchQuery.isNotBlank()) {
            val trimmed = searchQuery.trim()
            val localImmediate = repository.searchLocalImmediate(trimmed).mapIndexed { idx, dest ->
                SearchStoppageItem(
                    id = "local_${idx}_${dest.name.hashCode()}_${dest.id}",
                    name = dest.name,
                    address = dest.address ?: "বাংলাদেশ",
                    latitude = dest.latitude,
                    longitude = dest.longitude,
                    iconBgColor = Color(0xFFE0F2FE),
                    pinColor = Color(0xFF0072FF),
                    categoryKey = dest.category,
                    isSaved = savedDestinationsList.any { it.name.trim().equals(dest.name.trim(), ignoreCase = true) }
                )
            }
            val savedMatches = savedDestinationsList.filter { item ->
                item.name.contains(trimmed, ignoreCase = true) ||
                (item.address?.contains(trimmed, ignoreCase = true) == true)
            }.mapIndexed { idx, item ->
                SearchStoppageItem(
                    id = "saved_${idx}_${item.id}",
                    name = item.name,
                    address = item.address ?: "সংরক্ষিত স্থান",
                    latitude = item.latitude,
                    longitude = item.longitude,
                    iconBgColor = Color(0xFFDCFCE7),
                    pinColor = Color(0xFF16A34A),
                    categoryKey = item.category,
                    isSaved = true
                )
            }
            val onlineMatches = onlineResults.mapIndexed { idx, dest ->
                SearchStoppageItem(
                    id = "online_${idx}_${dest.name.hashCode()}_${dest.id}",
                    name = dest.name,
                    address = dest.address ?: "বাংলাদেশ",
                    latitude = dest.latitude,
                    longitude = dest.longitude,
                    iconBgColor = Color(0xFFE0F2FE),
                    pinColor = Color(0xFF0072FF),
                    categoryKey = "general",
                    isSaved = savedDestinationsList.any { it.name.trim().equals(dest.name.trim(), ignoreCase = true) }
                )
            }
            val combined = mutableListOf<SearchStoppageItem>()
            combined.addAll(savedMatches)
            for (local in localImmediate) {
                if (combined.none { it.name.trim().equals(local.name.trim(), ignoreCase = true) }) {
                    combined.add(local)
                }
            }
            for (online in onlineMatches) {
                if (combined.none { it.name.trim().equals(online.name.trim(), ignoreCase = true) }) {
                    combined.add(online)
                }
            }
            combined
        } else {
            // When not searching, show user's saved items or recent searches according to selectedFilterKey
            when (selectedFilterKey) {
                "all" -> {
                    savedDestinationsList.mapIndexed { idx, dest ->
                        val pinColor = when (dest.category) {
                            "বাসা" -> Color(0xFFEA580C)
                            "কাজ" -> Color(0xFF0284C7)
                            else -> Color(0xFF16A34A)
                        }
                        val bgColor = when (dest.category) {
                            "বাসা" -> Color(0xFFFFEDD5)
                            "কাজ" -> Color(0xFFE0F2FE)
                            else -> Color(0xFFDCFCE7)
                        }
                        SearchStoppageItem(
                            id = "saved_${idx}_${dest.id}",
                            name = dest.name,
                            address = dest.address ?: dest.category,
                            latitude = dest.latitude,
                            longitude = dest.longitude,
                            iconBgColor = bgColor,
                            pinColor = pinColor,
                            categoryKey = dest.category,
                            isSaved = true
                        )
                    }
                }
                "recent" -> {
                    recentSearchesList.mapIndexed { idx, dest ->
                        SearchStoppageItem(
                            id = "recent_${idx}_${dest.id}",
                            name = dest.name,
                            address = dest.address ?: "সাম্প্রতিক অনুসন্ধান",
                            latitude = dest.latitude,
                            longitude = dest.longitude,
                            iconBgColor = Color(0xFFFCE7F3),
                            pinColor = Color(0xFFDB2777),
                            categoryKey = "recent",
                            isSaved = savedDestinationsList.any { it.name.trim().equals(dest.name.trim(), ignoreCase = true) }
                        )
                    }
                }
                else -> {
                    // Filter saved places by category name (e.g. "বাসা", "কাজ", etc.)
                    savedDestinationsList.filter {
                        it.category.trim().equals(selectedFilterKey.trim(), ignoreCase = true)
                    }.mapIndexed { idx, dest ->
                        val pinColor = when (dest.category) {
                            "বাসা" -> Color(0xFFEA580C)
                            "কাজ" -> Color(0xFF0284C7)
                            else -> Color(0xFF9333EA)
                        }
                        val bgColor = when (dest.category) {
                            "বাসা" -> Color(0xFFFFEDD5)
                            "কাজ" -> Color(0xFFE0F2FE)
                            else -> Color(0xFFF3E8FF)
                        }
                        SearchStoppageItem(
                            id = "saved_cat_${idx}_${dest.id}",
                            name = dest.name,
                            address = dest.address ?: dest.category,
                            latitude = dest.latitude,
                            longitude = dest.longitude,
                            iconBgColor = bgColor,
                            pinColor = pinColor,
                            categoryKey = dest.category,
                            isSaved = true
                        )
                    }
                }
            }
        }
    }

    // Debounced online search
    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.length >= 2) {
            delay(250)
            isSearchingOnline = true
            try {
                val res = repository.searchOnline(trimmed)
                if (res is com.shakhawat.ghhumbhango.data.repository.GeocodeResult.Success && res.destinations.isNotEmpty()) {
                    onlineResults = res.destinations
                }
            } catch (_: Exception) {
            } finally {
                isSearchingOnline = false
            }
        } else {
            onlineResults = emptyList()
        }
    }

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.SAVED,
                onHomeClick = onNavigateToHome,
                onSavedClick = { /* Already in search/saved */ },
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
        ) {
            // 1. Top Section: Back button, Title & 3D Map Header Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button
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

                    // Title
                    Text(
                        text = strings.searchDestinationTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = appColors.textPrimary
                    )

                    // 3D Folded Map Graphic
                    Box(
                        modifier = Modifier.size(52.dp, 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.search_header_map),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = appColors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = strings.searchInputHint,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = appColors.textMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = appColors.textPrimary
                            ),
                            cursorBrush = SolidColor(Color(0xFF0072FF)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (appColors.isDark) Color(0xFF334155) else Color(0xFFCBD5E1))
                                .clickable { searchQuery = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (isSearchingOnline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color(0xFF0072FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. "ম্যাপ থেকে নির্বাচন করুন" Action Card (Directly below search bar!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        promptEnableLocation(onSuccess = onPickOnMap)
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (appColors.isDark) Color(0x2216A34A) else Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_map_pick_folded),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.pickOnMapTitle,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.pickOnMapSubtitle,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = appColors.textMuted
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = appColors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Categories Filter Chips Row with Add Category Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All
                FilterChipItem(
                    label = strings.filterAll,
                    icon = null,
                    isSelected = selectedFilterKey == "all",
                    onClick = { selectedFilterKey = "all" }
                )
                // Recent
                FilterChipItem(
                    label = strings.filterRecent,
                    icon = Icons.Default.Schedule,
                    isSelected = selectedFilterKey == "recent",
                    onClick = { selectedFilterKey = "recent" }
                )
                // Home (বাসা)
                FilterChipItem(
                    label = "বাসা",
                    icon = Icons.Default.Home,
                    isSelected = selectedFilterKey == "বাসা",
                    onClick = { selectedFilterKey = "বাসা" }
                )
                // Work (কাজ)
                FilterChipItem(
                    label = "কাজ",
                    icon = Icons.Default.Work,
                    isSelected = selectedFilterKey == "কাজ",
                    onClick = { selectedFilterKey = "কাজ" }
                )
                // Custom user categories
                allCategoryNames.filter { it != "বাসা" && it != "কাজ" }.forEach { customCat ->
                    FilterChipItem(
                        label = customCat,
                        icon = Icons.Default.Star,
                        isSelected = selectedFilterKey == customCat,
                        onClick = { selectedFilterKey = customCat }
                    )
                }
                // Add new category chip (+)
                Box(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, Color(0xFF0072FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showAddCategoryDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add category",
                            tint = Color(0xFF0072FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (strings is BengaliStrings) "ক্যাটাগরি" else "Category",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0072FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Stoppage Destination List
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = appColors.textMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                if (strings is BengaliStrings) "কোনো ফলাফল পাওয়া যায়নি" else "No results found"
                            } else if (selectedFilterKey == "recent") {
                                if (strings is BengaliStrings) "কোনো সাম্প্রতিক অনুসন্ধান নেই" else "No recent searches"
                            } else {
                                if (strings is BengaliStrings) "এই ক্যাটাগরিতে কোনো সংরক্ষিত স্থান নেই" else "No saved destinations in this category"
                            },
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (strings is BengaliStrings)
                                "উপরে অনুসন্ধান করে স্থান খুঁজুন অথবা ম্যাপ থেকে নির্বাচন করে সংরক্ষণ করুন।"
                            else
                                "Search for places above or pick on map to save.",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 13.sp,
                            color = appColors.textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = displayedItems,
                        key = { index, item -> "${item.id}_${item.name.hashCode()}_$index" }
                    ) { _, item ->
                        StoppageCardItem(
                            item = item,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                promptEnableLocation(onSuccess = {
                                    val destination = Destination(
                                        id = item.id,
                                        name = item.name,
                                        latitude = item.latitude,
                                        longitude = item.longitude,
                                        address = item.address,
                                        isSaved = item.isSaved,
                                        category = item.categoryKey
                                    )
                                    coroutineScope.launch {
                                        userPrefsRepo.addRecentSearch(destination)
                                    }
                                    onSelectDestination(destination)
                                })
                            },
                            onSaveClick = {
                                val destination = Destination(
                                    id = item.id,
                                    name = item.name,
                                    latitude = item.latitude,
                                    longitude = item.longitude,
                                    address = item.address,
                                    isSaved = true,
                                    category = item.categoryKey
                                )
                                destinationToSave = destination
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Dialog: Add New Custom Category
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { newCategoryName ->
                showAddCategoryDialog = false
                coroutineScope.launch {
                    userPrefsRepo.addCustomCategory(newCategoryName)
                    selectedFilterKey = newCategoryName
                    Toast.makeText(context, "নতুন ক্যাটাগরি '$newCategoryName' যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Dialog: Save Location to Category
    if (destinationToSave != null) {
        val dest = destinationToSave!!
        SaveLocationCategoryDialog(
            destination = dest,
            availableCategories = allCategoryNames,
            onDismiss = { destinationToSave = null },
            onSave = { category ->
                destinationToSave = null
                coroutineScope.launch {
                    repository.saveDestinationWithCategory(dest, category)
                    userPrefsRepo.addRecentSearch(dest)
                    Toast.makeText(context, "'${dest.name}' স্থানটি '$category' ক্যাটাগরিতে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            },
            onCreateAndSave = { newCategory ->
                destinationToSave = null
                coroutineScope.launch {
                    userPrefsRepo.addCustomCategory(newCategory)
                    repository.saveDestinationWithCategory(dest, newCategory)
                    userPrefsRepo.addRecentSearch(dest)
                    Toast.makeText(context, "'${dest.name}' স্থানটি '$newCategory' ক্যাটাগরিতে সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Location Off Dialog (Pathao style prompt)
    if (showLocationOffDialog) {
        AlertDialog(
            onDismissRequest = {
                showLocationOffDialog = false
                pendingActionOnLocationEnabled = null
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
                    text = if (strings is BengaliStrings) "আপনার লোকেশন অফ আছে" else "Your Location is Off",
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
                    text = if (strings is BengaliStrings)
                        "ম্যাপে অবস্থান দেখতে এবং গন্তব্যে পৌঁছালে সঠিক সময়ে অ্যালার্ম পেতে ডিভাইসের লোকেশন ও জিপিএস চালু করা আবশ্যক। অনুগ্রহ করে লোকেশন চালু করুন।"
                    else
                        "Device location and GPS must be enabled to view map and trigger alarms on arrival. Please turn on location.",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
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
                        text = if (strings is BengaliStrings) "সেটিংস থেকে চালু করুন" else "Turn On from Settings",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLocationOffDialog = false
                        pendingActionOnLocationEnabled = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (strings is BengaliStrings) "বাতিল" else "Cancel",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = appColors.textMuted
                    )
                }
            }
        )
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 3.dp else 1.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF0072FF) else appColors.cardBackground)
            .border(
                1.dp,
                if (isSelected) Color(0xFF0072FF) else appColors.cardBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else appColors.textMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.5.sp,
                color = if (isSelected) Color.White else appColors.textPrimary
            )
        }
    }
}

@Composable
private fun StoppageCardItem(
    item: SearchStoppageItem,
    onClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .clip(RoundedCornerShape(18.dp))
            .background(appColors.cardBackground)
            .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
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
            // Rounded Squarish Icon Container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (appColors.isDark) item.pinColor.copy(alpha = 0.2f) else item.iconBgColor
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = item.pinColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.address,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = appColors.textMuted
                )
            }

            // Save / Bookmark button
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save to category",
                    tint = if (item.isSaved) Color(0xFF0072FF) else appColors.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = appColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (categoryName: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "নতুন ক্যাটাগরি তৈরি করুন",
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.textPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "যেমন: বাসা, কাজ, বিশ্ববিদ্যালয়, টিউশন, জিম ইত্যাদি",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 13.sp,
                    color = appColors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("ক্যাটাগরির নাম", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onAdd(categoryName.trim())
                    }
                },
                enabled = categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "যোগ করুন",
                    fontFamily = HindSiliguriFontFamily,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "বাতিল",
                    fontFamily = HindSiliguriFontFamily,
                    color = appColors.textMuted
                )
            }
        }
    )
}

@Composable
fun SaveLocationCategoryDialog(
    destination: Destination,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (category: String) -> Unit,
    onCreateAndSave: (newCategory: String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(if (availableCategories.isNotEmpty()) availableCategories.first() else "বাসা") }
    var isCreatingNew by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "স্থান সংরক্ষণ করুন",
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.textPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = destination.name,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0072FF)
                )
                destination.address?.let {
                    Text(
                        text = it,
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "ক্যাটাগরি নির্বাচন করুন:",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Selectable category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val isSelected = (!isCreatingNew && selectedCategory == cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFF0072FF) else appColors.cardBorder.copy(alpha = 0.5f))
                                .clickable {
                                    isCreatingNew = false
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else appColors.textPrimary
                            )
                        }
                    }

                    // + নতুন ক্যাটাগরি chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCreatingNew) Color(0xFF0072FF) else appColors.cardBorder.copy(alpha = 0.5f))
                            .clickable { isCreatingNew = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ নতুন",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 13.sp,
                            fontWeight = if (isCreatingNew) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCreatingNew) Color.White else Color(0xFF0072FF)
                        )
                    }
                }

                if (isCreatingNew) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCategoryText,
                        onValueChange = { newCategoryText = it },
                        label = { Text("নতুন ক্যাটাগরির নাম", fontFamily = HindSiliguriFontFamily) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appColors.textPrimary,
                            unfocusedTextColor = appColors.textPrimary,
                            focusedBorderColor = Color(0xFF0072FF),
                            unfocusedBorderColor = appColors.cardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCreatingNew) {
                        if (newCategoryText.isNotBlank()) {
                            onCreateAndSave(newCategoryText.trim())
                        }
                    } else {
                        onSave(selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "সংরক্ষণ করুন",
                    fontFamily = HindSiliguriFontFamily,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "বাতিল",
                    fontFamily = HindSiliguriFontFamily,
                    color = appColors.textMuted
                )
            }
        }
    )
}
