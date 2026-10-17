package com.shakhawat.ghhumbhango.ui.home.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.ui.theme.AccentBlue
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.AmberLight
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.TextTertiary

@Composable
fun DestinationCard(
    selectedDestination: Destination?,
    onSearchClick: () -> Unit,
    onMapPickClick: () -> Unit,
    onClearDestination: () -> Unit,
    modifier: Modifier = Modifier,
    distanceRemaining: String? = null,
    isWithinRadius: Boolean = false,
    alarmRadiusLabel: String = "৫০০ মিটার"
) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(BorderSubtle, BorderSubtle.copy(alpha = 0.4f))
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Card Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoyalBluePrimary.copy(alpha = 0.2f))
                        .border(1.dp, RoyalBlueLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = RoyalBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(id = R.string.destination_card_title),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedDestination == null) {
                // Empty state: Search trigger & Map selection button
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search bar trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(NavyCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSearchClick
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.destination_search_placeholder),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    // Map selection trigger
                    OutlinedButton(
                        onClick = onMapPickClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.horizontalGradient(
                                listOf(BorderSubtle, RoyalBluePrimary.copy(alpha = 0.6f))
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.destination_pick_map),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Selected Destination Active Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RoyalBluePrimary.copy(alpha = 0.12f))
                        .border(1.5.dp, RoyalBlueLight.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(RoyalBluePrimary.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.destination_selected_label),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = AccentBlue
                            )
                            Text(
                                text = selectedDestination.name,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = PureWhite
                            )
                            if (!selectedDestination.address.isNullOrEmpty()) {
                                Text(
                                    text = selectedDestination.address,
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onClearDestination,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NavySurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.destination_clear_btn),
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Live Calculated Geodesic Distance Remaining Pill
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavySurface)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = if (isWithinRadius) AmberAccent else AccentBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (distanceRemaining != null) {
                                    if (strings is BengaliStrings) "দূরত্ব বাকি: $distanceRemaining" else "Remaining: $distanceRemaining"
                                } else {
                                    if (strings is BengaliStrings) "জিপিএস দূরত্বের হিসাব চলছে…" else "Calculating GPS distance…"
                                },
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isWithinRadius) AmberLight else PureWhite
                            )
                        }

                        // Proximity badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isWithinRadius) AmberAccent.copy(alpha = 0.2f)
                                    else RoyalBluePrimary.copy(alpha = 0.25f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isWithinRadius) {
                                    if (strings is BengaliStrings) "সীমার ভেতরে ($alarmRadiusLabel)" else "Within range ($alarmRadiusLabel)"
                                } else {
                                    if (strings is BengaliStrings) "সীমা: $alarmRadiusLabel" else "Range: $alarmRadiusLabel"
                                },
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.5.sp,
                                color = if (isWithinRadius) AmberAccent else AccentBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
