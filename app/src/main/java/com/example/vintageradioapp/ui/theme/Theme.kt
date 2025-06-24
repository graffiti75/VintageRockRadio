package com.example.vintageradioapp.ui.theme // Updated package name

import android.app.Activity
// import android.os.Build // Not strictly needed for this theme setup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Using only a "dark" vintage theme for this app, as a radio player might typically have a darker aesthetic.
private val VintageColorScheme = darkColorScheme(
    primary = VintageOrange, // Accent color
    onPrimary = Color.Black, // Text/icons on primary
    secondary = VintageButtonColor, // Buttons or secondary accents
    onSecondary = VintageCream, // Text/icons on secondary
    background = VintageBrown, // Main background of the app
    onBackground = VintageCream, // Text on main background
    surface = VintageControlPanel, // Background for cards, sheets, menus
    onSurface = VintageCream, // Text on surfaces
    error = Color(0xFFCF6679), // Standard error color
    onError = Color.Black
    // Define other colors like primaryContainer, secondaryContainer if needed
)

@Composable
fun VintageRadioAppTheme( // Updated theme name
    darkTheme: Boolean = true, // Forcing dark theme as per vintage radio feel, can be isSystemInDarkTheme()
    // Dynamic coloring is available on Android 12+
    // dynamicColor: Boolean = true, // Set to false if you want to strictly use your custom theme
    content: @Composable () -> Unit
) {
    val colorScheme = VintageColorScheme // Always use our custom vintage scheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
