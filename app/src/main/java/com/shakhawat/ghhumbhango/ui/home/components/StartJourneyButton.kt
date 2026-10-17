package com.shakhawat.ghhumbhango.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavySurfaceVariant
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import com.shakhawat.ghhumbhango.ui.theme.TextTertiary

@Composable
fun StartJourneyButton(
    isEnabled: Boolean,
    onStartJourney: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onStartJourney,
            enabled = isEnabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = NavySurfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (isEnabled) 6.dp else 0.dp
            ),
            contentPadding = ButtonDefaults.ContentPadding,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = if (isEnabled) {
                        "যাত্রা শুরু করুন"
                    } else {
                        "যাত্রা শুরু করতে প্রথমে একটি গন্তব্য নির্বাচন করুন"
                    }
                }
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isEnabled) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                colors = listOf(RoyalBluePrimary, RoyalBlueLight)
                            )
                        )
                    } else {
                        Modifier.background(NavySurfaceVariant.copy(alpha = 0.5f))
                    }
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = if (isEnabled) AmberAccent else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.start_journey_btn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isEnabled) PureWhite else TextTertiary
                )
            }
        }

        // Helper explanation when disabled
        AnimatedVisibility(
            visible = !isEnabled,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "⚠️ " + stringResource(id = R.string.start_journey_hint_disabled),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

