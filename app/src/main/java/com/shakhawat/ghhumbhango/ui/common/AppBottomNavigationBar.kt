package com.shakhawat.ghhumbhango.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily

enum class NavTab {
    HOME, SAVED, HISTORY, SETTINGS
}

@Composable
fun AppBottomNavigationBar(
    currentTab: NavTab,
    onHomeClick: () -> Unit,
    onSavedClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = appColors.shadowColor,
                    spotColor = appColors.shadowColor
                )
                .clip(RoundedCornerShape(32.dp))
                .background(appColors.bottomNavBg)
                .border(1.dp, appColors.bottomNavBorder, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val strings = com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings.current

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = strings.navHome,
                    icon = Icons.Default.Home,
                    isSelected = currentTab == NavTab.HOME,
                    onClick = onHomeClick
                )
                NavBarItem(
                    label = strings.navSaved,
                    icon = Icons.Default.Star,
                    isSelected = currentTab == NavTab.SAVED,
                    onClick = onSavedClick
                )
                NavBarItem(
                    label = strings.navHistory,
                    icon = Icons.Default.History,
                    isSelected = currentTab == NavTab.HISTORY,
                    onClick = onHistoryClick
                )
                NavBarItem(
                    label = strings.navSettings,
                    icon = Icons.Default.Settings,
                    isSelected = currentTab == NavTab.SETTINGS,
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val appColors = LocalAppColors.current

            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) appColors.primaryLight else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) appColors.primary else appColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = HindSiliguriFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp,
                color = if (isSelected) appColors.primary else appColors.textSecondary
            )
        }
    }
}

