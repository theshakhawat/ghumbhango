package com.shakhawat.ghhumbhango.ui.readiness

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shakhawat.ghhumbhango.data.readiness.SystemReadinessChecker
import com.shakhawat.ghhumbhango.domain.model.ReadinessActionType
import com.shakhawat.ghhumbhango.domain.model.ReadinessItem
import com.shakhawat.ghhumbhango.domain.model.SystemReadiness
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors

/**
 * System Readiness Audit screen ensuring permissions, hardware GPS,
 * notification channels, and battery optimization exemptions are ready before travel.
 * Designed to match the app's clean, modern light theme.
 */
@Composable
fun ReadinessScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val checker = remember { SystemReadinessChecker(context) }
    var readiness by remember { mutableStateOf(checker.checkReadiness()) }
    var isOemGuideExpanded by remember { mutableStateOf(false) }

    // Re-evaluate on return to app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                readiness = checker.checkReadiness()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission launchers
    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        readiness = checker.checkReadiness()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        readiness = checker.checkReadiness()
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        readiness = checker.checkReadiness()
    }

    fun handleAction(item: ReadinessItem) {
        when (item.actionType) {
            ReadinessActionType.REQUEST_LOCATION_PERMISSION -> {
                locationLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            ReadinessActionType.REQUEST_NOTIFICATION_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            ReadinessActionType.OPEN_LOCATION_SETTINGS -> {
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(context, "লোকেশন সেটিংস খুলুন", Toast.LENGTH_SHORT).show()
                }
            }
            ReadinessActionType.REQUEST_BATTERY_OPTIMIZATION -> {
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
                        Toast.makeText(context, "ব্যাটারি সেটিংস খুলুন", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            ReadinessActionType.OPEN_APPLICATION_SETTINGS -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
            ReadinessActionType.NONE -> {}
        }
    }

    val appColors = LocalAppColors.current

    Scaffold(
        containerColor = appColors.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
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
                            contentDescription = "ফিরে যান",
                            tint = appColors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "সিস্টেম প্রস্তুতি ও প্রি-জার্নি চেক",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = appColors.textPrimary
                        )
                        Text(
                            text = "অ্যালার্ম মিস না হওয়ার নিশ্চয়তা",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.5.sp,
                            color = Color(0xFF0072FF)
                        )
                    }
                }
            }

            // 2. Scrollable Body
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                // Overall Status Score Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (readiness.isFullyReady) {
                                        "আপনার ফোন পুরোপুরি প্রস্তুত!"
                                    } else if (readiness.hasCriticalIssues) {
                                        "কিছু জরুরি পারমিশন প্রয়োজন"
                                    } else {
                                        "প্রস্তুতি প্রায় সম্পূর্ণ"
                                    },
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (readiness.isFullyReady) Color(0xFF059669)
                                           else if (readiness.hasCriticalIssues) Color(0xFFDC2626)
                                           else Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${DistanceCalculator.toBengaliDigits(readiness.readyCount)}টি বিষয়ের মধ্যে ${DistanceCalculator.toBengaliDigits(readiness.totalCount)}টি সঠিক আছে",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 13.sp,
                                    color = appColors.textMuted
                                )
                            }

                            // Readiness percentage badge
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (readiness.isFullyReady) (if (appColors.isDark) Color(0x2210B981) else Color(0xFFECFDF5))
                                        else if (readiness.hasCriticalIssues) (if (appColors.isDark) Color(0x22EF4444) else Color(0xFFFEF2F2))
                                        else (if (appColors.isDark) Color(0x22F59E0B) else Color(0xFFFFFBEB))
                                    )
                                    .border(
                                        2.dp,
                                        if (readiness.isFullyReady) Color(0xFF10B981)
                                        else if (readiness.hasCriticalIssues) Color(0xFFEF4444)
                                        else Color(0xFFF59E0B),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${DistanceCalculator.toBengaliDigits(readiness.readinessPercentage)}%",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (readiness.isFullyReady) Color(0xFF10B981)
                                           else if (readiness.hasCriticalIssues) Color(0xFFEF4444)
                                           else Color(0xFFF59E0B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { readiness.readyCount.toFloat() / readiness.totalCount.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (readiness.isFullyReady) Color(0xFF10B981)
                                   else if (readiness.hasCriticalIssues) Color(0xFFEF4444)
                                   else Color(0xFF0072FF),
                            trackColor = appColors.divider
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Checklist Section Title
                Text(
                    text = "প্রয়োজনীয় সক্ষমতার তালিকা",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = appColors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                readiness.items.forEach { item ->
                    ReadinessItemRow(
                        item = item,
                        onActionClick = { handleAction(item) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. OEM Battery Guidance Card (Samsung / Xiaomi / OnePlus)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isOemGuideExpanded = !isOemGuideExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (appColors.isDark) Color(0x22F59E0B) else Color(0xFFFFFBEB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Xiaomi / Samsung ব্যাকগ্রাউন্ড নির্দেশিকা",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = appColors.textPrimary
                                )
                            }

                            Icon(
                                imageVector = if (isOemGuideExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = appColors.textMuted
                            )
                        }

                        AnimatedVisibility(visible = isOemGuideExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(appColors.inputBg)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "কিছু ফোনে (যেমন Xiaomi, Samsung, OnePlus, Vivo) ব্যাটারি বাঁচাতে স্ক্রিন বন্ধ হলে অ্যাপের ব্যাকগ্রাউন্ড জিপিএস বন্ধ করে দেয়। অ্যালার্ম শতভাগ নিশ্চিত করতে:",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 13.sp,
                                    color = appColors.textSecondary,
                                    lineHeight = 19.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "• Xiaomi (MIUI / HyperOS): Settings > Apps > GhumBhango > Battery Saver > 'No Restrictions' দিন এবং Autostart চালু করুন।\n\n" +
                                            "• Samsung (One UI): Settings > Apps > GhumBhango > Battery > 'Unrestricted' নির্বাচন করুন।\n\n" +
                                            "• OnePlus / Oppo / Vivo: Battery > Background activity > 'Allow' করুন।",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.5.sp,
                                    color = appColors.textPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Return to Dashboard Button
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0x200072FF),
                            spotColor = Color(0x350072FF)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0072FF),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "ড্যাশবোর্ডে ফিরে যান",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReadinessItemRow(
    item: ReadinessItem,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.cardBackground),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = appColors.shadowColor,
                spotColor = appColors.shadowColor
            )
            .border(
                1.dp,
                if (item.isReady) appColors.cardBorder
                else if (item.isCritical) if (appColors.isDark) Color(0xFF7F1D1D) else Color(0xFFFECACA)
                else if (appColors.isDark) Color(0xFF78350F) else Color(0xFFFDE68A),
                RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isReady) (if (appColors.isDark) Color(0x2210B981) else Color(0xFFECFDF5))
                        else if (item.isCritical) (if (appColors.isDark) Color(0x22EF4444) else Color(0xFFFEF2F2))
                        else (if (appColors.isDark) Color(0x22F59E0B) else Color(0xFFFFFBEB))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isReady) Icons.Default.Check
                                  else if (item.isCritical) Icons.Default.Close
                                  else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (item.isReady) Color(0xFF10B981)
                           else if (item.isCritical) Color(0xFFEF4444)
                           else Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = appColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 12.5.sp,
                    color = appColors.textMuted,
                    lineHeight = 17.5.sp
                )
            }

            // Action Button if not ready
            if (!item.isReady && item.actionType != ReadinessActionType.NONE) {
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isCritical) Color(0xFFEF4444) else Color(0xFF0072FF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "ঠিক করুন",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}
