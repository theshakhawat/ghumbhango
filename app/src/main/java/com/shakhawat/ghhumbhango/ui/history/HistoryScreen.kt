package com.shakhawat.ghhumbhango.ui.history

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shakhawat.ghhumbhango.data.local.entity.JourneyHistoryEntity
import com.shakhawat.ghhumbhango.data.local.entity.RoutePresetEntity
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.domain.model.UserLocation
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onRestartJourney: (Destination, AlarmDistance) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    currentLocation: UserLocation? = null,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.HISTORY,
                onHomeClick = onNavigateToHome,
                onSavedClick = onNavigateToSearch,
                onHistoryClick = { /* Already on history */ },
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
            // 1. Top Bar: Back Button, Title & Clear History
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
                        text = strings.historyScreenTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = appColors.textPrimary
                    )

                    // Clear History Button
                    if (uiState.selectedTab == 0 && uiState.historyList.isNotEmpty()) {
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
                                    viewModel.showClearHistoryDialog()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.delete,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (uiState.selectedTab == 1 || uiState.selectedTab == 2) {
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
                                    if (uiState.selectedTab == 1) {
                                        viewModel.openAddPresetDialog()
                                    } else {
                                        viewModel.openAddSavedStopDialog()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.add,
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }
                }
            }

            // 2. Tab Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HistoryTabChip(
                    label = strings.tabHistory,
                    icon = Icons.Default.History,
                    isSelected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) }
                )
                HistoryTabChip(
                    label = strings.tabPresets,
                    icon = Icons.Default.Route,
                    isSelected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) }
                )
                HistoryTabChip(
                    label = strings.tabSavedStops,
                    icon = Icons.Default.Bookmark,
                    isSelected = uiState.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Tab Contents
            when (uiState.selectedTab) {
                0 -> {
                    // Journey History Tab
                    if (uiState.historyList.isEmpty()) {
                        EmptyHistoryPlaceholder(
                            icon = Icons.Default.History,
                            title = strings.noHistoryFound,
                            subtitle = ""
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.historyList, key = { it.id }) { item ->
                                HistoryCardItem(
                                    item = item,
                                    onRestart = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRestartJourney(item.toDestination(), item.toAlarmDistance())
                                    },
                                    onDelete = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.deleteHistoryItem(item.id)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }

                1 -> {
                    // Route Presets Tab
                    if (uiState.presetList.isEmpty()) {
                        EmptyHistoryPlaceholder(
                            icon = Icons.Default.Route,
                            title = strings.noPresetsFound,
                            subtitle = ""
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.presetList, key = { it.id }) { preset ->
                                PresetCardItem(
                                    preset = preset,
                                    onStart = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRestartJourney(preset.toDestination(), preset.toAlarmDistance())
                                    },
                                    onDelete = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.deletePreset(preset.id)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }

                2 -> {
                    // Saved Destinations Tab
                    if (uiState.savedStops.isEmpty()) {
                        EmptyHistoryPlaceholder(
                            icon = Icons.Default.Bookmark,
                            title = strings.noSavedStopsFound,
                            subtitle = ""
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = uiState.savedStops,
                                key = { index, stop -> "${stop.id}_${stop.name.hashCode()}_$index" }
                            ) { _, stop ->
                                SavedStopCardItem(
                                    destination = stop,
                                    onStart = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onRestartJourney(stop, AlarmDistance.DEFAULT)
                                    },
                                    onDelete = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        stop.id.toLongOrNull()?.let { id ->
                                            viewModel.deleteSavedStop(id)
                                        }
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }

        // Clear History Dialog
        if (uiState.isClearHistoryDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissClearHistoryDialog() },
                title = {
                    Text(
                        text = strings.clearAllHistory,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.clearAllHistory,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmClearHistory() }
                    ) {
                        Text(
                            text = strings.delete,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissClearHistoryDialog() }) {
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

        // Add Preset Dialog
        if (uiState.isAddPresetDialogVisible) {
            AddPresetCleanDialog(
                initialDestination = uiState.presetInitialDestination,
                currentLocation = currentLocation,
                onDismiss = { viewModel.closeAddPresetDialog() },
                onConfirm = { title, destName, address, lat, lng, dist, category ->
                    viewModel.saveNewPreset(
                        RoutePresetEntity(
                            title = title,
                            destinationName = destName,
                            destinationAddress = address,
                            latitude = lat,
                            longitude = lng,
                            alarmDistanceMeters = dist.meters,
                            category = category
                        )
                    )
                }
            )
        }

        // Add Saved Stop Dialog
        if (uiState.isAddSavedStopDialogVisible) {
            AddSavedStopCleanDialog(
                currentLocation = currentLocation,
                onDismiss = { viewModel.closeAddSavedStopDialog() },
                onConfirm = { name, address, lat, lng ->
                    viewModel.saveCustomStop(name, address, lat, lng)
                }
            )
        }
    }
}

@Composable
private fun HistoryTabChip(
    label: String,
    icon: ImageVector,
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else appColors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
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
private fun HistoryCardItem(
    item: JourneyHistoryEntity,
    onRestart: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current
    val dateFormat = remember { SimpleDateFormat("d MMMM, yyyy • h:mm a", Locale("bn", "BD")) }
    val formattedDate = remember(item.endTimeMillis) {
        DistanceCalculator.toBengaliDigits(dateFormat.format(Date(item.endTimeMillis)))
    }
    val isCompleted = item.status == "COMPLETED"

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
            .padding(16.dp)
    ) {
        Column {
            // Header Row: Bus Icon + Destination + Status Badge
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCompleted) (if (appColors.isDark) Color(0x2216A34A) else Color(0xFFDCFCE7))
                                else (if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF16A34A) else Color(0xFF0072FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.destinationName,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        if (!item.destinationAddress.isNullOrBlank()) {
                            Text(
                                text = item.destinationAddress,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.5.sp,
                                color = appColors.textMuted
                            )
                        }
                    }
                }

                // Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isCompleted) (if (appColors.isDark) Color(0x2216A34A) else Color(0xFFDCFCE7))
                            else (if (appColors.isDark) Color(0x22D97706) else Color(0xFFFEF3C7))
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
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

            // Metadata Row: Time & Alarm Distance Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = appColors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }

                Text(
                    text = "পরিধি: ${DistanceCalculator.toBengaliDigits(item.alarmDistanceMeters)} মি.",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color(0xFF0072FF)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row: Restart Journey Button + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vibrant Blue Restart Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0072FF))
                        .clickable { onRestart() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.restartJourney,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (appColors.isDark) Color(0x33EF4444) else Color(0xFFFEE2E2))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCardItem(
    preset: RoutePresetEntity,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
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
            .padding(16.dp)
    ) {
        Column {
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
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = Color(0xFF0072FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = preset.title,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "${strings.destinationLabel}: ${preset.destinationName}",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color(0xFF0072FF)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (appColors.isDark) Color(0x33EF4444) else Color(0xFFFEE2E2))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0072FF))
                    .clickable { onStart() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.startJourney,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedStopCardItem(
    destination: Destination,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = LocalAppStrings.current
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
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (appColors.isDark) Color(0x2216A34A) else Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination.name,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                if (!destination.address.isNullOrBlank()) {
                    Text(
                        text = destination.address,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        color = appColors.textMuted
                    )
                }
            }

            // Quick Alarm Set Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                    .clickable { onStart() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.startJourney,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Color(0xFF0072FF)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (appColors.isDark) Color(0x33EF4444) else Color(0xFFFEE2E2))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(appColors.cardBackground)
                .border(1.dp, appColors.cardBorder, CircleShape)
                .shadow(4.dp, CircleShape, ambientColor = appColors.shadowColor, spotColor = appColors.shadowColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0072FF),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontFamily = HindSiliguriFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = appColors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontFamily = HindSiliguriFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = appColors.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AddPresetCleanDialog(
    initialDestination: Destination?,
    currentLocation: UserLocation? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, destName: String, address: String?, lat: Double, lng: Double, dist: AlarmDistance, cat: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    var title by remember { mutableStateOf("") }
    var destName by remember { mutableStateOf(initialDestination?.name ?: "") }
    var address by remember { mutableStateOf(initialDestination?.address ?: "") }
    var lat by remember { mutableStateOf(initialDestination?.latitude ?: (currentLocation?.latitude ?: 23.7508)) }
    var lng by remember { mutableStateOf(initialDestination?.longitude ?: (currentLocation?.longitude ?: 90.3938)) }
    var isLocationPinned by remember { mutableStateOf(initialDestination != null) }
    var selectedDistance by remember { mutableStateOf(AlarmDistance.DEFAULT) }
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        com.shakhawat.ghhumbhango.ui.map.MapLocationPinPickerDialog(
            initialLat = lat,
            initialLng = lng,
            initialName = destName,
            initialAddress = address,
            currentLocation = currentLocation,
            onDismiss = { showMapPicker = false },
            onLocationPinned = { name, addr, pLat, pLng ->
                lat = pLat
                lng = pLng
                if (destName.isBlank() || destName == "মানচিত্রের স্থান" || destName == "নির্বাচিত স্থান") {
                    destName = name
                }
                address = addr
                isLocationPinned = true
                showMapPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "নতুন রুট প্রিসেট যোগ করুন",
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.textPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("প্রিসেটের নাম (যেমন: অফিস রুট)", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedContainerColor = appColors.inputBg,
                        unfocusedContainerColor = appColors.inputBg,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = destName,
                    onValueChange = { destName = it },
                    label = { Text("গন্তব্য নাম", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedContainerColor = appColors.inputBg,
                        unfocusedContainerColor = appColors.inputBg,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা বা বিবরণ", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showMapPicker = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "ম্যাপে পিন করুন",
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedContainerColor = appColors.inputBg,
                        unfocusedContainerColor = appColors.inputBg,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Location Pin Action Button / Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF0072FF))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isLocationPinned) (if (appColors.isDark) Color(0x2210B981) else Color(0xFFF0FDF4))
                            else (if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showMapPicker = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLocationPinned) Icons.Default.CheckCircle else Icons.Default.Place,
                            contentDescription = null,
                            tint = if (isLocationPinned) Color(0xFF10B981) else Color(0xFF0072FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLocationPinned) {
                                "পিন নিশ্চিত: ${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)} (পরিবর্তন করুন)"
                            } else {
                                "📍 ম্যাপ থেকে লোকেশন পিন করুন"
                            },
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isLocationPinned) Color(0xFF10B981) else Color(0xFF0072FF)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && destName.isNotBlank()) {
                        onConfirm(title, destName, address, lat, lng, selectedDistance, "WORK")
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = "সংরক্ষণ করুন",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0072FF)
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
        },
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddSavedStopCleanDialog(
    currentLocation: UserLocation? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String?, lat: Double, lng: Double) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(currentLocation?.latitude ?: 23.7508) }
    var lng by remember { mutableStateOf(currentLocation?.longitude ?: 90.3938) }
    var isLocationPinned by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    if (showMapPicker) {
        com.shakhawat.ghhumbhango.ui.map.MapLocationPinPickerDialog(
            initialLat = lat,
            initialLng = lng,
            initialName = name,
            initialAddress = address,
            currentLocation = currentLocation,
            onDismiss = { showMapPicker = false },
            onLocationPinned = { pName, addr, pLat, pLng ->
                lat = pLat
                lng = pLng
                if (name.isBlank() || name == "মানচিত্রের স্থান" || name == "নির্বাচিত স্থান") {
                    name = pName
                }
                address = addr
                isLocationPinned = true
                showMapPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "নতুন স্টপ সংরক্ষণ করুন",
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.textPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("স্টপের নাম (যেমন: খিলক্ষেত)", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedContainerColor = appColors.inputBg,
                        unfocusedContainerColor = appColors.inputBg,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা বা বিবরণ", fontFamily = HindSiliguriFontFamily) },
                    singleLine = true,
                    trailingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    showMapPicker = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "ম্যাপে পিন করুন",
                                tint = Color(0xFF0072FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedContainerColor = appColors.inputBg,
                        unfocusedContainerColor = appColors.inputBg,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), spotColor = Color(0xFF0072FF))
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isLocationPinned) (if (appColors.isDark) Color(0x2210B981) else Color(0xFFF0FDF4))
                            else (if (appColors.isDark) Color(0x220072FF) else Color(0xFFE0F2FE))
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showMapPicker = true
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLocationPinned) Icons.Default.CheckCircle else Icons.Default.Place,
                            contentDescription = null,
                            tint = if (isLocationPinned) Color(0xFF10B981) else Color(0xFF0072FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLocationPinned) {
                                "পিন নিশ্চিত: ${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)} (পরিবর্তন করুন)"
                            } else {
                                "📍 ম্যাপ থেকে লোকেশন পিন করুন"
                            },
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isLocationPinned) Color(0xFF10B981) else Color(0xFF0072FF)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, address, lat, lng)
                        onDismiss()
                    }
                }
            ) {
                Text(
                    text = "সংরক্ষণ করুন",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0072FF)
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
        },
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
