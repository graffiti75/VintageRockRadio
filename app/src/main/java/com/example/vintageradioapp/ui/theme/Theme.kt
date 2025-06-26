package com.example.vintageradioapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
// import androidx.compose.foundation.isSystemInDarkTheme // Not used if forcing one theme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Using a "dark" vintage theme as primary
private val VintageDarkColorScheme = darkColorScheme(
    primary = VintageAccent,
    onPrimary = VintageBrown, // Text/icons on primary accent (e.g., on buttons)
    secondary = VintageDarkRed, // A secondary accent
    onSecondary = VintageCream,
    background = VintageBrown,
    onBackground = VintageCream,
    surface = VintageControlPanel, // Background for control area, cards
    onSurface = VintageCream,
    error = Color(0xFFCF6679), // Standard Material error red
    onError = Color(0xFF000000)
)

@Composable
fun VintageRadioAppTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(), // Can be used if you want to support light/dark system choice
    content: @Composable () -> Unit
) {
    val colorScheme = VintageDarkColorScheme // Forcing our vintage dark theme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Set status bar to match app background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Text on status bar is light
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
