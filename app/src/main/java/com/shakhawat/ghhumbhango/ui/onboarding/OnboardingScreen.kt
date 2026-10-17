package com.shakhawat.ghhumbhango.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val titleRes: Int,
    val descriptionRes: Int,
    val imageRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                titleRes = R.string.onboarding_title_1,
                descriptionRes = R.string.onboarding_desc_1,
                imageRes = R.drawable.onboarding_step_1
            ),
            OnboardingPageData(
                titleRes = R.string.onboarding_title_2,
                descriptionRes = R.string.onboarding_desc_2,
                imageRes = R.drawable.onboarding_step_2
            ),
            OnboardingPageData(
                titleRes = R.string.onboarding_title_3,
                descriptionRes = R.string.onboarding_desc_3,
                imageRes = R.drawable.onboarding_step_3
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val appColors = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(appColors.gradientSky))
    ) {
        // Top "Skip" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage < pages.size - 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(
                    onClick = onFinishOnboarding
                ) {
                    Text(
                        text = "এড়িয়ে যান",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = appColors.textMuted
                    )
                }
            }
        }

        // Horizontal Pager with the 3 onboarding slides
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageData = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 44.dp, bottom = 96.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header Bengali Title
                Text(
                    text = stringResource(id = pageData.titleRes),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    lineHeight = 34.sp,
                    color = appColors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Center 3D Illustration Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = appColors.shadowColor,
                            spotColor = appColors.shadowColor
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(appColors.cardBackground)
                        .border(1.dp, appColors.cardBorder, RoundedCornerShape(26.dp))
                ) {
                    Image(
                        painter = painterResource(id = pageData.imageRes),
                        contentDescription = stringResource(id = pageData.titleRes),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(598f / 476f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 3. Dot Indicator (Active & Inactive dots)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { dotIndex ->
                        val isSelected = pagerState.currentPage == dotIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 9.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF0072FF) else appColors.topBarButtonBorder
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 4. Feature description filling the empty space requested by the user
                Text(
                    text = stringResource(id = pageData.descriptionRes),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = appColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // 5. Glossy Blue Circular Next / Finish Button on Bottom Right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 28.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFF0072FF),
                        ambientColor = Color(0xFF0072FF)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF38A5FF),
                                Color(0xFF0072FF),
                                Color(0xFF0054D1)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishOnboarding()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right_thick),
                    contentDescription = "পরবর্তী",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

