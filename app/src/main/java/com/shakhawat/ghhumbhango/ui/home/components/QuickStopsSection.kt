package com.shakhawat.ghhumbhango.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.ui.theme.AccentBlue
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary

@Composable
fun QuickStopsSection(
    quickStops: List<Destination>,
    selectedDestination: Destination?,
    onSelectStop: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = R.string.quick_stops_title),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PureWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            quickStops.forEach { stop ->
                val isSelected = selectedDestination?.name == stop.name
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) RoyalBluePrimary.copy(alpha = 0.35f) else NavyCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) RoyalBlueLight else BorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectStop(stop) }
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = stop.name,
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        color = if (isSelected) PureWhite else TextSecondary
                    )
                }
            }
        }
    }
}

