package com.shakhawat.ghhumbhango.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakhawat.ghhumbhango.R
import com.shakhawat.ghhumbhango.ui.theme.AccentBlue
import com.shakhawat.ghhumbhango.ui.theme.AmberAccent
import com.shakhawat.ghhumbhango.ui.theme.AmberLight
import com.shakhawat.ghhumbhango.ui.theme.HindSiliguriFontFamily
import com.shakhawat.ghhumbhango.ui.theme.NavyBackground
import com.shakhawat.ghhumbhango.ui.theme.NavySurface
import com.shakhawat.ghhumbhango.ui.theme.PureWhite
import com.shakhawat.ghhumbhango.ui.theme.RoyalBlueLight
import com.shakhawat.ghhumbhango.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = remember { Animatable(0f) }
    val contentOffsetY = remember { Animatable(-24f) }

    // Subtle pulsing animation for the amber indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        // Animate content entrance
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        contentOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Keep splash displayed gracefully then transition
        delay(1800)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // 1. Text-free cinematic nighttime travel illustration
        Image(
            painter = painterResource(id = R.drawable.splashscreen),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top deep navy pedestal gradient for maximum contrast & crisp readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.58f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyBackground,
                            NavyBackground.copy(alpha = 0.95f),
                            NavyBackground.copy(alpha = 0.75f),
                            NavyBackground.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. Bottom subtle gradient for edge-to-edge navigation bar blending
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NavyBackground.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // 4. Native brand typography & controls repositioned to TopCenter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffsetY.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // GhhumBhango App Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.5.dp, RoyalBlueLight.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Smart Travel Alarm Badge Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NavySurface.copy(alpha = 0.85f))
                    .border(1.dp, RoyalBlueLight.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AmberAccent.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.app_subtitle_badge),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Bengali Brand Title
            Text(
                text = stringResource(id = R.string.app_name_bengali),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                lineHeight = 50.sp,
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            // English Subtitle
            Text(
                text = stringResource(id = R.string.app_name_english).uppercase(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                color = AccentBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Amber glowing accent divider
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AmberAccent,
                                AmberLight,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline: "ঘুম আসুক, স্টপ মিস নয়!"
            Text(
                text = stringResource(id = R.string.app_tagline),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                color = AmberLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Minimal elegant progress indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.splash_loading),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

