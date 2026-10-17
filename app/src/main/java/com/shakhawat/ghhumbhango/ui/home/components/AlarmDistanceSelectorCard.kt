package com.shakhawat.ghhumbhango.ui.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.AlarmDistance
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.AmberLight
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.NavySurfaceVariant
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import com.shakhawat.ghhumbhango.ui.theme.TextTertiary

@Composable
fun AlarmDistanceSelectorCard(
    selectedDistance: AlarmDistance,
    onDistanceSelected: (AlarmDistance) -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Card Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberAccent.copy(alpha = 0.15f))
                        .border(1.dp, AmberAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = AmberLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(id = R.string.alarm_distance_title),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PureWhite
                    )
                    Text(
                        text = stringResource(id = R.string.alarm_distance_subtitle),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4 Distance Option Tiles in 2x2 Grid for optimal mobile touch targets
            val distances = AlarmDistance.entries
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First Row: 100m, 200m (Default)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DistanceOptionTile(
                        distance = distances[0],
                        isSelected = selectedDistance == distances[0],
                        onSelect = { onDistanceSelected(distances[0]) },
                        modifier = Modifier.weight(1f)
                    )
                    DistanceOptionTile(
                        distance = distances[1],
                        isSelected = selectedDistance == distances[1],
                        isDefault = true,
                        onSelect = { onDistanceSelected(distances[1]) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Second Row: 500m, 1km
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DistanceOptionTile(
                        distance = distances[2],
                        isSelected = selectedDistance == distances[2],
                        isDefault = false,
                        onSelect = { onDistanceSelected(distances[2]) },
                        modifier = Modifier.weight(1f)
                    )
                    DistanceOptionTile(
                        distance = distances[3],
                        isSelected = selectedDistance == distances[3],
                        onSelect = { onDistanceSelected(distances[3]) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Description Box for Selected Distance
            AnimatedContent(
                targetState = selectedDistance,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "distanceDescription"
            ) { target ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(NavyCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AmberAccent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = target.description,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceOptionTile(
    distance: AlarmDistance,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    isDefault: Boolean = false
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AmberAccent else BorderSubtle,
        animationSpec = tween(200),
        label = "tileBorder"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AmberAccent.copy(alpha = 0.12f) else NavyCard,
        animationSpec = tween(200),
        label = "tileBackground"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) PureWhite else TextSecondary,
        animationSpec = tween(200),
        label = "tileText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .semantics {
                role = Role.RadioButton
                stateDescription = if (isSelected) "নির্বাচিত" else "অনির্বাচিত"
                contentDescription = "${distance.displayLabel} সতর্কতা দূরত্ব"
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSelect
            )
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val appLanguage = com.shakhawat.ghhumbhango.ui.localization.LocalAppLanguage.current
                Text(
                    text = distance.getLocalizedLabel(appLanguage),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isDefault) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(RoyalBluePrimary.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.alarm_distance_default_badge),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = RoyalBlueLight
                    )
                }
            }
        }
    }
}

