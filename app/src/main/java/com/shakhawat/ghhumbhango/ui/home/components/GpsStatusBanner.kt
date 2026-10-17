package com.shakhawat.ghhumbhango.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.WrongLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.GpsStatus
import com.shakhawat.ghhumbhango.ui.theme.AccentBlue
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.AmberLight
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import com.shakhawat.ghhumbhango.ui.theme.TextTertiary

@Composable
fun GpsStatusBanner(
    gpsStatus: GpsStatus,
    onRequestPermission: () -> Unit,
    onEnableGps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    when (gpsStatus) {
        is GpsStatus.PermissionDenied -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AmberAccent.copy(alpha = 0.12f))
                    .border(1.dp, AmberAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WrongLocation,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (strings is BengaliStrings) "লোকেশন অনুমতি প্রয়োজন" else "Location Permission Needed",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = PureWhite
                        )
                        Text(
                            text = if (strings is BengaliStrings) "দূরত্ব মাপতে ও অ্যালার্ম বাজাতে অনুমতি দিন" else "Allow permission to track distance & alarm",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (strings is BengaliStrings) "অনুমতি দিন" else "Grant",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = NavySurface
                    )
                }
            }
        }

        is GpsStatus.GpsDisabled -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AmberAccent.copy(alpha = 0.12f))
                    .border(1.dp, AmberAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsOff,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (strings is BengaliStrings) "ডিভাইসের জিপিএস বন্ধ" else "Device GPS Disabled",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = PureWhite
                        )
                        Text(
                            text = if (strings is BengaliStrings) "সঠিক সময়ে জাগাতে জিপিএস চালু করুন" else "Turn on GPS to ring alarm on time",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Button(
                    onClick = onEnableGps,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (strings is BengaliStrings) "চালু করুন" else "Enable",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PureWhite
                    )
                }
            }
        }

        is GpsStatus.ApproximateOnly -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsNotFixed,
                        contentDescription = null,
                        tint = AmberLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (strings is BengaliStrings) "নিখুঁত অ্যালার্মের জন্য প্রিসাইজ পারমিশন দিন" else "Grant precise GPS for accurate alarm",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (strings is BengaliStrings) "নিখুঁত করুন" else "Upgrade",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = PureWhite
                    )
                }
            }
        }

        is GpsStatus.Ready -> {
            val loc = gpsStatus.location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (strings is BengaliStrings) "জিপিএস সক্রিয় • ${loc.formattedAccuracy}" else "GPS Active • ${loc.formattedAccuracy}",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = PureWhite
                    )
                }

                Text(
                    text = String.format("%.4f° N, %.4f° E", loc.latitude, loc.longitude),
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }

        is GpsStatus.Searching -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationSearching,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = gpsStatus.message,
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

