package com.shakhawat.ghhumbhango.ui.destination

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.domain.model.Destination
import com.shakhawat.ghhumbhango.ui.theme.AccentBlue
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.AmberLight
import com.shakhawat.ghhumbhango.ui.theme.BorderSubtle
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyBackground
import com.shakhawat.ghhumbhango.ui.theme.NavyCard
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.RoyalBluePrimary
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import com.shakhawat.ghhumbhango.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationSearchSheet(
    searchQuery: String,
    searchResults: List<Destination>,
    isSearchingOnline: Boolean,
    onlineSearchMessage: String?,
    onQueryChanged: (String) -> Unit,
    onSearchOnline: () -> Unit,
    onSelectDestination: (Destination) -> Unit,
    onDeleteDestination: (Destination) -> Unit,
    onOpenAddCustomDialog: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderSubtle)
            )
        },
        modifier = modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.search_sheet_title),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = PureWhite
                )

                TextButton(onClick = onOpenAddCustomDialog) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.add_custom_destination_btn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = AmberLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_input_hint),
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = AccentBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = NavyCard,
                    unfocusedContainerColor = NavyCard,
                    focusedBorderColor = AmberAccent,
                    unfocusedBorderColor = BorderSubtle
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Online Search action bar
            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "তালিকায় না পেলে অনলাইনে অনুসন্ধান করুন:",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = onSearchOnline,
                        enabled = !isSearchingOnline,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBluePrimary,
                            disabledContainerColor = RoyalBluePrimary.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isSearchingOnline) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "খোঁজা হচ্ছে…",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 12.sp,
                                color = PureWhite
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.search_online_btn),
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 12.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            // Error or Offline Message Banner
            if (!onlineSearchMessage.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberAccent.copy(alpha = 0.12f))
                        .border(1.dp, AmberAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = AmberLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = onlineSearchMessage,
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = PureWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isBlank()) {
                    stringResource(id = R.string.saved_stops_header)
                } else {
                    stringResource(id = R.string.search_results_header)
                },
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AccentBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            // List of Destinations
            if (searchResults.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.no_results_found),
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = searchResults,
                        key = { index, destination -> "${destination.id}_${destination.name.hashCode()}_$index" }
                    ) { _, destination ->
                        DestinationResultItem(
                            destination = destination,
                            onSelect = { onSelectDestination(destination) },
                            onDelete = { onDeleteDestination(destination) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationResultItem(
    destination: Destination,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RoyalBluePrimary.copy(alpha = 0.25f))
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (destination.isSaved) AmberAccent else RoyalBlueLight,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = destination.name,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (destination.isSaved) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Saved",
                        tint = AmberAccent,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (!destination.address.isNullOrEmpty()) {
                Text(
                    text = destination.address,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Real Coordinates badge
            Text(
                text = String.format("%.4f° N, %.4f° E", destination.latitude, destination.longitude),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        // Allow deleting user-created destination if saved
        if (destination.isSaved && destination.id.toLongOrNull() != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

