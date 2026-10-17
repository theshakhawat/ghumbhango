package com.shakhawat.ghhumbhango.ui.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary

@Composable
fun SaveCustomDestinationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, lat: Double, lng: Double, address: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(id = R.string.save_dialog_title),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = appColors.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text(stringResource(id = R.string.save_dialog_name_hint), fontFamily = HindSiliguriFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Latitude and Longitude in a row
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it; errorMessage = null },
                        label = { Text("Latitude", fontSize = 11.sp) },
                        placeholder = { Text("23.7570", fontSize = 11.sp, color = appColors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appColors.textPrimary,
                            unfocusedTextColor = appColors.textPrimary,
                            focusedBorderColor = Color(0xFF0072FF),
                            unfocusedBorderColor = appColors.cardBorder,
                            focusedLabelColor = Color(0xFF0072FF),
                            unfocusedLabelColor = appColors.textMuted
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it; errorMessage = null },
                        label = { Text("Longitude", fontSize = 11.sp) },
                        placeholder = { Text("90.3894", fontSize = 11.sp, color = appColors.textMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appColors.textPrimary,
                            unfocusedTextColor = appColors.textPrimary,
                            focusedBorderColor = Color(0xFF0072FF),
                            unfocusedBorderColor = appColors.cardBorder,
                            focusedLabelColor = Color(0xFF0072FF),
                            unfocusedLabelColor = appColors.textMuted
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address (optional)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(id = R.string.save_dialog_address_hint), fontFamily = HindSiliguriFontFamily, fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.textPrimary,
                        unfocusedTextColor = appColors.textPrimary,
                        focusedBorderColor = Color(0xFF0072FF),
                        unfocusedBorderColor = appColors.cardBorder,
                        focusedLabelColor = Color(0xFF0072FF),
                        unfocusedLabelColor = appColors.textMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "অনুগ্রহ করে স্থানের নাম লিখুন"
                    } else if (lat == null || lng == null) {
                        errorMessage = "সঠিক অক্ষাংশ ও দ্রাঘিমাংশ দিন (যেমন: 23.7570, 90.3894)"
                    } else if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
                        errorMessage = "স্থানাঙ্কের মান সঠিক নয় (Lat: -90 থেকে 90, Lng: -180 থেকে 180)"
                    } else {
                        onSave(name, lat, lng, address.ifBlank { null })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.save_btn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel_btn),
                    fontFamily = HindSiliguriFontFamily,
                    color = appColors.textMuted
                )
            }
        }
    )
}

