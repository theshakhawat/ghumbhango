package com.shakhawat.ghhumbhango.ui.alarm

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors

@Composable
fun AlarmScreen(
    destinationName: String,
    destinationAddress: String?,
    distanceRemainingText: String?,
    alarmRadiusText: String?,
    onDismissAlarm: () -> Unit,
    onSnoozeAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "alarmAnimation")

    // Gentle pulsing animation for the ringing bell
    val bellScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bellScale"
    )

    // Wave ring pulse alpha
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    val remainingLabel = if (!distanceRemainingText.isNullOrBlank()) {
        strings.remainingText(distanceRemainingText)
    } else if (!alarmRadiusText.isNullOrBlank()) {
        strings.remainingText(alarmRadiusText)
    } else {
        strings.remainingText("500m")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(appColors.gradientSky))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Section: 3D Bus + Notification Headline
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // 3D Bus Image
                Image(
                    painter = painterResource(id = R.drawable.alarm_bus_header),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(170.dp, 88.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.destinationArrivedTitle,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Destination Badge Card
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = destinationName,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = appColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = remainingLabel,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.5.sp,
                                color = appColors.textMuted
                            )
                        }
                    }
                }
            }

            // 2. Middle Section: Pulsing Golden Bell Hero Graphic
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    // Pulsing Ring 1
                    val ringColor1 = if (appColors.isDark) Color(0xFFF59E0B).copy(alpha = ringAlpha * 0.25f) else Color(0xFFFFF3CD).copy(alpha = ringAlpha * 0.4f)
                    val ringColor2 = if (appColors.isDark) Color(0xFFD97706).copy(alpha = ringAlpha * 0.35f) else Color(0xFFFFECB3).copy(alpha = ringAlpha * 0.6f)
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .clip(CircleShape)
                            .background(ringColor1)
                    )
                    // Pulsing Ring 2
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .background(ringColor2)
                    )

                    // Hero Bell Graphic
                    Image(
                        painter = painterResource(id = R.drawable.alarm_ringing_bell),
                        contentDescription = "Ringing Bell",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(180.dp)
                            .scale(bellScale)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = strings.alarmAndVibrationRinging,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = appColors.textMuted
                )
            }

            // 3. Bottom Section: Prominent Red "অ্যালার্ম বন্ধ করুন" Button + Optional Snooze
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Red Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFFEF4444),
                            ambientColor = Color(0xFFEF4444)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFDC2626)
                                )
                            )
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismissAlarm()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = strings.dismissAlarm,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.dismissAlarm,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Snooze option (2 minutes)
                Text(
                    text = strings.snoozeTwoMinutes,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = appColors.textMuted,
                    modifier = Modifier
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSnoozeAlarm()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
