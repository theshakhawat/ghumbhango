package com.shakhawat.ghhumbhango.ui.home.components

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.ui.localization.BengaliStrings
import com.shakhawat.ghhumbhango.ui.localization.LocalAppStrings
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors

/**
 * Dialog preventing accidental cancellation of active journey alarms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopJourneyConfirmationDialog(
    destinationName: String,
    onConfirmStop: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = strings.endJourneyConfirmTitle,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = appColors.textPrimary
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (strings is BengaliStrings)
                        "গন্তব্য \"$destinationName\"-এর ট্র্যাকিং বন্ধ করলে পৌঁছানোর সতর্কবার্তা (অ্যালার্ম) আর বাজবে না।"
                    else
                        "Stopping tracking for \"$destinationName\" will disable the destination alarm.",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 15.sp,
                    color = appColors.textPrimary,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.endJourneyConfirmText,
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 14.sp,
                    color = appColors.textMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (strings is BengaliStrings) "হ্যাঁ, বন্ধ করুন" else "Yes, End",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, appColors.cardBorder)
            ) {
                Text(
                    text = if (strings is BengaliStrings) "চালিয়ে যান" else "Keep Going",
                    fontFamily = HindSiliguriFontFamily,
                    color = appColors.textPrimary,
                    fontSize = 14.sp
                )
            }
        }
    )
}

