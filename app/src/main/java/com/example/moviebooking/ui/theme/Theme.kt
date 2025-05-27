package com.example.moviebooking.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.Black,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.Black,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = AccentColor,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    error = ErrorColor,
    onError = Color.Black
)

@Composable
fun MovieBookingTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    fontScale: FontScale = FontScale(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalFontScale provides fontScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}