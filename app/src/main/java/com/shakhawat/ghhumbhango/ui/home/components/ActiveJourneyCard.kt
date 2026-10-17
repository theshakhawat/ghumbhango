package com.shakhawat.ghhumbhango.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.domain.model.ActiveJourney
import com.shakhawat.ghhumbhango.domain.model.JourneyState
import com.shakhawat.ghhumbhango.domain.util.DistanceCalculator
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
import com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextMuted
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary

/**
 * High-visibility tracking dashboard card displayed when a journey is active.
 */
@Composable
fun ActiveJourneyCard(
    journey: ActiveJourney,
    onPauseJourney: () -> Unit,
    onResumeJourney: () -> Unit,
    onStopJourneyClick: () -> Unit,
    onDismissAlarm: () -> Unit = {},
    onSnoozeAlarm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val appLanguage = LocalAppLanguage.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val statusColor = when (journey.state) {
        JourneyState.TRACKING -> Color(0xFF10B981) // Emerald Green
        JourneyState.PAUSED -> AmberAccent
        JourneyState.APPROACHING -> Color(0xFFEF4444) // Urgent Red
        else -> TextMuted
    }

    val statusText = when (journey.state) {
        JourneyState.TRACKING -> if (strings is BengaliStrings) "রিয়েলটাইম ট্র্যাকিং চলছে" else "Real-time Tracking Active"
        JourneyState.PAUSED -> if (strings is BengaliStrings) "ট্র্যাকিং সাময়িক বিরতিতে" else "Tracking Paused"
        JourneyState.APPROACHING -> if (strings is BengaliStrings) "🚨 গন্তব্য সমাগত! প্রস্তুত হোন" else "🚨 Approaching Destination! Get Ready"
        else -> if (strings is BengaliStrings) "যাত্রার প্রস্তুতি" else "Preparing Journey"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(statusColor.copy(alpha = 0.6f), BorderSubtle)
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 1. Status Pill & Live Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                            .alpha(if (journey.state == JourneyState.TRACKING) pulseAlpha else 1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = statusColor
                    )
                }

                // Configured Alarm Distance Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurface)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${strings.alarmDistanceHeader}: ${journey.alarmDistance.getLocalizedLabel(appLanguage)}",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = AmberAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Destination Details
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(RoyalBluePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = journey.destination.name,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = PureWhite
                    )
                    if (!journey.destination.address.isNullOrBlank()) {
                        Text(
                            text = journey.destination.address,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Prominent Distance Remaining Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavySurface)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.remainingDistance,
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = journey.formattedDistanceRemaining ?: if (strings is BengaliStrings) "অবস্থান গণনা হচ্ছে…" else "Calculating location…",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = if (journey.isApproaching) Color(0xFFEF4444) else AmberAccent,
                        modifier = Modifier.semantics {
                            liveRegion = if (journey.isApproaching) LiveRegionMode.Assertive else LiveRegionMode.Polite
                            contentDescription = journey.formattedDistanceRemaining?.let {
                                if (journey.isApproaching) "Alert! Destination near: $it remaining" else "Distance remaining: $it"
                            } ?: "GPS locating"
                        }
                    )

                    // Accuracy & telemetry footer
                    val accuracyText = journey.userLocation?.accuracyMeters?.let {
                        if (strings is BengaliStrings) "জিপিএস নির্ভুলতা: ±${DistanceCalculator.toBengaliDigits(it.toInt())} মি." else "GPS Accuracy: ±${it.toInt()}m"
                    } ?: if (strings is BengaliStrings) "জিপিএস সংকেত সক্রিয়" else "GPS signal active"

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = accuracyText,
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Action Buttons
            if (journey.state == JourneyState.APPROACHING) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismissAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Dismiss Alarm"
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (strings is BengaliStrings) "জেগে উঠেছি • অ্যালার্ম বন্ধ করুন" else "I'm Awake • Dismiss Alarm",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                    }

                    OutlinedButton(
                        onClick = onSnoozeAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "Snooze Alarm"
                            },
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.snoozeAlarm,
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 13.5.sp,
                            color = PureWhite
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pause / Resume Button
                    if (journey.state == JourneyState.TRACKING) {
                        OutlinedButton(
                            onClick = onPauseJourney,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "চলমান যাত্রা সাময়িক বিরতি দিন"
                                },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PureWhite.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (strings is BengaliStrings) "বিরতি দিন" else "Pause",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PureWhite
                            )
                        }
                    } else {
                        Button(
                            onClick = onResumeJourney,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Resume Journey"
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalBluePrimary,
                                contentColor = PureWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (strings is BengaliStrings) "চালু করুন" else "Resume",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PureWhite
                            )
                        }
                    }

                    // Stop Journey Button
                    Button(
                        onClick = onStopJourneyClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = "End Journey"
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.endJourney,
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
