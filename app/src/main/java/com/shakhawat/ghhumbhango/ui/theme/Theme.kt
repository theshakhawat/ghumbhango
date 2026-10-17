package com.shakhawat.ghhumbhango.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val iconTint: Color,
    val divider: Color,
    val primary: Color,
    val primaryLight: Color,
    val topBarButtonBg: Color,
    val topBarButtonBorder: Color,
    val bottomNavBg: Color,
    val bottomNavBorder: Color,
    val shadowColor: Color,
    val inputBg: Color,
    val gradientSky: List<Color>
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF0F7FD),
    backgroundSecondary = Color(0xFFF4F8FD),
    surface = Color.White,
    cardBackground = Color.White,
    cardBorder = Color(0xFFE2E8F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    iconTint = Color(0xFF1E293B),
    divider = Color(0xFFF1F5F9),
    primary = Color(0xFF0072FF),
    primaryLight = Color(0xFFE3F2FD),
    topBarButtonBg = Color.White,
    topBarButtonBorder = Color(0xFFE2E8F0),
    bottomNavBg = Color.White,
    bottomNavBorder = Color(0xFFE2E8F0),
    shadowColor = Color(0x10000000),
    inputBg = Color(0xFFF8FAFC),
    gradientSky = listOf(Color(0xFFE3F2FD), Color(0xFFEDF6FC), Color(0xFFF4F8FD))
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF081226),
    backgroundSecondary = Color(0xFF0D1B36),
    surface = Color(0xFF122244),
    cardBackground = Color(0xFF122244),
    cardBorder = Color(0xFF1E3560),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    iconTint = Color(0xFFE2E8F0),
    divider = Color(0xFF1E3560),
    primary = Color(0xFF3B82F6),
    primaryLight = Color(0xFF1E3A8A),
    topBarButtonBg = Color(0xFF122244),
    topBarButtonBorder = Color(0xFF1E3560),
    bottomNavBg = Color(0xFF122244),
    bottomNavBorder = Color(0xFF1E3560),
    shadowColor = Color(0x40000000),
    inputBg = Color(0xFF0D1B36),
    gradientSky = listOf(Color(0xFF0D1B36), Color(0xFF0F1E3C), Color(0xFF081226))
)

val LocalAppColors = compositionLocalOf { LightAppColors }

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0072FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF0072FF),
    secondary = AmberAccent,
    onSecondary = Color.White,
    secondaryContainer = AmberLight,
    onSecondaryContainer = Color(0xFF0F172A),
    background = Color(0xFFF0F7FD),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlueLight,
    onPrimary = PureWhite,
    primaryContainer = RoyalBluePrimary,
    onPrimaryContainer = PureWhite,
    secondary = AmberAccent,
    onSecondary = NavyBackground,
    secondaryContainer = AmberLight,
    onSecondaryContainer = NavyBackground,
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    background = NavyBackground,
    onBackground = PureWhite,
    surface = NavySurface,
    onSurface = PureWhite,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun GhhumBhangoTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GhhumBhangoTypography,
            content = content
        )
    }
}
