package com.shakhawat.ghhumbhango.ui.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.alarm.AlarmPlayer
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PermissionScreen(
    onPermissionsCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val appColors = LocalAppColors.current

    // Real-time permission tracking states
    var fineLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var batteryIgnored by remember {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    var isSoundTesting by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        batteryIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Automatically update when returning from system settings or permission prompts
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isSoundTesting) {
                AlarmPlayer.stop()
            }
        }
    }

    // Permission launchers
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshPermissions()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshPermissions()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshPermissions()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(appColors.gradientSky))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Section (Scrollable if screen is small)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Top Header with Bengali typography on Left & Boy illustration on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.perm_screen_title_1),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = stringResource(id = R.string.perm_screen_title_2),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = stringResource(id = R.string.perm_screen_title_highlight),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            color = Color(0xFF0072FF)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = R.string.perm_screen_subtitle),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = appColors.textMuted
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.permission_header_boy),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(135.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Permission Items (4 Cards matching mockup)

                // Card 1: লোকেশন (GPS)
                PermissionCard(
                    iconRes = R.drawable.ic_perm_location,
                    iconBgColor = Color(0xFFEBF3FF),
                    title = stringResource(id = R.string.perm_item_location_title),
                    subtitle = stringResource(id = R.string.perm_item_location_desc),
                    statusText = if (fineLocationGranted) stringResource(id = R.string.perm_status_granted) else stringResource(id = R.string.perm_status_required),
                    statusColor = if (fineLocationGranted) Color(0xFF10B981) else Color(0xFF0072FF),
                    onClick = {
                        locationLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 2: নোটিফিকেশন
                PermissionCard(
                    iconRes = R.drawable.ic_perm_bell,
                    iconBgColor = Color(0xFFFFF7ED),
                    title = stringResource(id = R.string.perm_item_notification_title),
                    subtitle = stringResource(id = R.string.perm_item_notification_desc),
                    statusText = if (notificationGranted) stringResource(id = R.string.perm_status_granted) else stringResource(id = R.string.perm_status_required),
                    statusColor = if (notificationGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            try {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 3: ব্যাকগ্রাউন্ড ও ব্যাটারি
                PermissionCard(
                    iconRes = R.drawable.ic_perm_battery,
                    iconBgColor = Color(0xFFECFDF5),
                    title = stringResource(id = R.string.perm_item_battery_title),
                    subtitle = stringResource(id = R.string.perm_item_battery_desc),
                    statusText = if (batteryIgnored) stringResource(id = R.string.perm_status_granted) else stringResource(id = R.string.perm_status_battery_check),
                    statusColor = Color(0xFF10B981),
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            batteryLauncher.launch(intent)
                        } catch (e: Exception) {
                            try {
                                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(fallback)
                            } catch (ex: Exception) {
                                // Fallback
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card 4: অ্যালার্ম সাউন্ড
                PermissionCard(
                    iconRes = R.drawable.ic_perm_sound,
                    iconBgColor = Color(0xFFF5F3FF),
                    title = stringResource(id = R.string.perm_item_sound_title),
                    subtitle = stringResource(id = R.string.perm_item_sound_desc),
                    statusText = if (isSoundTesting) stringResource(id = R.string.perm_status_sound_playing) else stringResource(id = R.string.perm_status_test_sound),
                    statusColor = if (isSoundTesting) Color(0xFFEF4444) else Color(0xFF8B5CF6),
                    onClick = {
                        if (!isSoundTesting) {
                            isSoundTesting = true
                            AlarmPlayer.start(context, isVibrationEnabled = true)
                            coroutineScope.launch {
                                delay(2200)
                                AlarmPlayer.stop()
                                isSoundTesting = false
                            }
                        } else {
                            AlarmPlayer.stop()
                            isSoundTesting = false
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Bottom Action Button: "সব সেটিংস যাচাই করুন"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, top = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0xFF0072FF),
                            ambientColor = Color(0xFF0072FF)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0072FF),
                                    Color(0xFF0052D4)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!fineLocationGranted) {
                                locationLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else if (!notificationGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onPermissionsCompleted()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.perm_btn_verify_all),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right_thick),
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

@Composable
private fun PermissionCard(
    iconRes: Int,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    statusText: String,
    statusColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = modifier
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon Container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = appColors.textPrimary
                )
                Text(
                    text = subtitle,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = appColors.textMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusText,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = statusColor
                )
            }

            // Trailing Chevron
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right_thick),
                contentDescription = null,
                tint = appColors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

