package com.shakhawat.ghhumbhango.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.shakhawat.ghhumbhango.alarm.CustomAlarmAudioHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.alarm.AlarmPlayer
import com.shakhawat.ghhumbhango.data.preferences.UserPreferencesRepository
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.domain.model.AlarmSound
import com.shakhawat.ghhumbhango.domain.model.AppLanguage
import com.shakhawat.ghhumbhango.domain.model.AppThemeMode
import com.shakhawat.ghhumbhango.domain.model.UserSettings
import com.shakhawat.ghhumbhango.ui.common.AppBottomNavigationBar
import com.shakhawat.ghhumbhango.ui.common.NavTab
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { UserPreferencesRepository.getInstance(context) }
    val userSettings by repository.userSettings.collectAsStateWithLifecycle(initialValue = UserSettings())
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current

    var showDistanceDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showOfflineMapDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showAdaptiveInfoDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

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

    DisposableEffect(Unit) {
        onDispose {
            AlarmPlayer.stopPreview()
        }
    }

    Scaffold(
        containerColor = appColors.background,
        bottomBar = {
            AppBottomNavigationBar(
                currentTab = NavTab.SETTINGS,
                onHomeClick = onNavigateToHome,
                onSavedClick = onNavigateToSearch,
                onHistoryClick = onNavigateToHistory,
                onSettingsClick = { /* Already on settings */ }
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
            // 1. Top Bar: Back Button & Title
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
                        text = strings.settingsTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = appColors.textPrimary
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Card Group 1: General Settings (5 items)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
            ) {
                Column {
                    SettingsRowItem(
                        title = strings.offlineMapTitle,
                        subtitle = strings.offlineMapSubtitle,
                        icon = Icons.Default.Map,
                        iconBgColor = if (appColors.isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                        iconTintColor = if (appColors.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        onClick = { showOfflineMapDialog = true }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.alarmSoundTitle,
                        subtitle = if (userSettings.alarmSound == AlarmSound.CUSTOM && !userSettings.customAlarmName.isNullOrBlank())
                            "${userSettings.alarmSound.getLocalizedName(userSettings.appLanguage)} (${userSettings.customAlarmName})"
                        else
                            userSettings.alarmSound.getLocalizedName(userSettings.appLanguage),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        iconBgColor = if (appColors.isDark) Color(0xFF7F1D1D) else Color(0xFFFEE2E2),
                        iconTintColor = if (appColors.isDark) Color(0xFFF87171) else Color(0xFFEF4444),
                        onClick = { showSoundDialog = true }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.defaultDistanceTitle,
                        subtitle = strings.defaultDistanceSubtitle(userSettings.defaultAlarmDistance.meters),
                        icon = Icons.Default.LocationOn,
                        iconBgColor = if (appColors.isDark) Color(0xFF14532D) else Color(0xFFDCFCE7),
                        iconTintColor = if (appColors.isDark) Color(0xFF4ADE80) else Color(0xFF16A34A),
                        onClick = { showDistanceDialog = true }
                    )
                    SettingsDivider()

                    SettingsSwitchRowItem(
                        title = strings.adaptiveAlarmTitle,
                        subtitle = strings.adaptiveAlarmSubtitle,
                        icon = Icons.Default.Tune,
                        iconBgColor = if (appColors.isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF),
                        iconTintColor = if (appColors.isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        checked = userSettings.isAdaptiveAlarmEnabled,
                        onCheckedChange = { isChecked ->
                            coroutineScope.launch {
                                repository.setAdaptiveAlarmEnabled(isChecked)
                            }
                        },
                        onInfoClick = {
                            showAdaptiveInfoDialog = true
                        }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.locationSettingsTitle,
                        subtitle = strings.locationSettingsSubtitle,
                        icon = Icons.Default.GpsFixed,
                        iconBgColor = if (appColors.isDark) Color(0xFF134E4A) else Color(0xFFCCFBF1),
                        iconTintColor = if (appColors.isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488),
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (_: Exception) {}
                        }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.batteryOptimizationTitle,
                        subtitle = strings.batteryOptimizationSubtitle,
                        icon = Icons.Default.BatteryAlert,
                        iconBgColor = if (appColors.isDark) Color(0xFF581C87) else Color(0xFFF3E8FF),
                        iconTintColor = if (appColors.isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
                        onClick = { showBatteryDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Card Group 2: Display, Language & About (3 items)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
            ) {
                Column {
                    SettingsRowItem(
                        title = strings.themeModeTitle,
                        subtitle = strings.themeModeSubtitle(userSettings.themeMode),
                        icon = Icons.Default.DarkMode,
                        iconBgColor = if (appColors.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                        iconTintColor = if (appColors.isDark) Color(0xFF60A5FA) else Color(0xFF334155),
                        onClick = { showThemeDialog = true }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.languageTitle,
                        subtitle = userSettings.appLanguage.nativeName,
                        icon = Icons.Default.Language,
                        iconBgColor = if (appColors.isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE),
                        iconTintColor = if (appColors.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        onClick = { showLanguageDialog = true }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.aboutAppTitle,
                        subtitle = strings.aboutAppSubtitle,
                        icon = Icons.Default.Info,
                        iconBgColor = if (appColors.isDark) Color(0xFF581C87) else Color(0xFFF3E8FF),
                        iconTintColor = if (appColors.isDark) Color(0xFFC084FC) else Color(0xFF9333EA),
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Card Group 3: Privacy & Support (2 items)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = appColors.shadowColor,
                        spotColor = appColors.shadowColor
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(appColors.cardBackground)
                    .border(1.dp, appColors.cardBorder, RoundedCornerShape(22.dp))
            ) {
                Column {
                    SettingsRowItem(
                        title = strings.privacyPolicyTitle,
                        subtitle = strings.privacyPolicySubtitle,
                        icon = Icons.Default.Security,
                        iconBgColor = if (appColors.isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                        iconTintColor = if (appColors.isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                        onClick = { showPrivacyDialog = true }
                    )
                    SettingsDivider()

                    SettingsRowItem(
                        title = strings.helpFeedbackTitle,
                        subtitle = strings.helpFeedbackSubtitle,
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconBgColor = if (appColors.isDark) Color(0xFF7C2D12) else Color(0xFFFFEDD5),
                        iconTintColor = if (appColors.isDark) Color(0xFFFB923C) else Color(0xFFEA580C),
                        onClick = { showFeedbackDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // --- Dialogs ---

        // Theme Mode Selection Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = {
                    Text(
                        text = strings.themeModeDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Column {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = (userSettings.themeMode == mode)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            repository.setThemeMode(mode)
                                        }
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            repository.setThemeMode(mode)
                                        }
                                        showThemeDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF0072FF),
                                        unselectedColor = appColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = mode.getLocalizedName(userSettings.appLanguage),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = if (isSelected) Color(0xFF0072FF) else appColors.textPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(
                            text = strings.close,
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

        // Distance Selection Dialog
        if (showDistanceDialog) {
            AlertDialog(
                onDismissRequest = { showDistanceDialog = false },
                title = {
                    Text(
                        text = strings.selectDistanceDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Column {
                        AlarmDistance.entries.forEach { dist ->
                            val isSelected = (userSettings.defaultAlarmDistance == dist)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            repository.setDefaultAlarmDistance(dist)
                                        }
                                        showDistanceDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.setDefaultAlarmDistance(dist)
                                        }
                                        showDistanceDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF0072FF),
                                        unselectedColor = appColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = dist.getLocalizedLabel(userSettings.appLanguage),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (isSelected) Color(0xFF0072FF) else appColors.textPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDistanceDialog = false }) {
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

        // Sound Selection Dialog
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
                                    .background(if (isSelected && isCustom) appColors.topBarButtonBg else Color.Transparent)
                                    .padding(vertical = 2.dp)
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
                                        Text(
                                            text = "▶",
                                            color = Color(0xFF0072FF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
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

        // Offline Map Dialog
        if (showOfflineMapDialog) {
            AlertDialog(
                onDismissRequest = { showOfflineMapDialog = false },
                title = {
                    Text(
                        text = strings.offlineMapDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.offlineMapDialogText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showOfflineMapDialog = false }) {
                        Text(
                            text = strings.gotIt,
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

        // Adaptive Alarm Info Dialog
        if (showAdaptiveInfoDialog) {
            AlertDialog(
                onDismissRequest = { showAdaptiveInfoDialog = false },
                title = {
                    Text(
                        text = strings.adaptiveAlarmDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.adaptiveAlarmDialogText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showAdaptiveInfoDialog = false }) {
                        Text(
                            text = strings.gotIt,
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

        // Battery Optimization Dialog
        if (showBatteryDialog) {
            AlertDialog(
                onDismissRequest = { showBatteryDialog = false },
                title = {
                    Text(
                        text = strings.batteryOptimizationDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.batteryOptimizationDialogText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showBatteryDialog = false
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        } catch (_: Exception) {}
                    }) {
                        Text(
                            text = strings.goToSettingsBtn,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0072FF)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBatteryDialog = false }) {
                        Text(
                            text = strings.cancel,
                            fontFamily = HindSiliguriFontFamily,
                            color = appColors.textSecondary
                        )
                    }
                },
                containerColor = appColors.cardBackground,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Language Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Text(
                        text = strings.selectLanguageDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Column {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = (userSettings.appLanguage == lang)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            repository.setAppLanguage(lang)
                                        }
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        coroutineScope.launch {
                                            repository.setAppLanguage(lang)
                                        }
                                        showLanguageDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF0072FF),
                                        unselectedColor = appColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lang.nativeName,
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = if (isSelected) Color(0xFF0072FF) else appColors.textPrimary
                                    )
                                    if (lang == AppLanguage.BANGLA) {
                                        Text(
                                            text = "Bengali",
                                            fontFamily = HindSiliguriFontFamily,
                                            fontSize = 12.sp,
                                            color = appColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(
                            text = strings.close,
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

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.aboutAppDialogTitle,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = appColors.textPrimary
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.aboutAppDialogText,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.5.sp,
                            color = appColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shakhawatdev.com"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Developer Website: ",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.5.sp,
                                color = appColors.textMuted
                            )
                            Text(
                                text = "shakhawatdev.com",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0072FF),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
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

        // Privacy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Text(
                        text = strings.privacyPolicyDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.privacyPolicyDialogText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
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

        // Feedback Dialog
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                title = {
                    Text(
                        text = strings.helpFeedbackDialogTitle,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.textPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.helpFeedbackDialogText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        color = appColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showFeedbackDialog = false }) {
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
private fun SettingsSwitchRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onInfoClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0072FF).copy(alpha = 0.12f))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onInfoClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "i",
                        color = Color(0xFF0072FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = appColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = { isChecked ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(isChecked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0072FF),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = if (appColors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Square Icon Container
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = appColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = appColors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = appColors.textMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    val appColors = LocalAppColors.current
    HorizontalDivider(
        modifier = Modifier.padding(start = 78.dp, end = 16.dp),
        thickness = 0.8.dp,
        color = appColors.divider
    )
}
