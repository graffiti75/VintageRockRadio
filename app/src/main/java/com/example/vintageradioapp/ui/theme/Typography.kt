package com.example.vintageradioapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Consider adding actual vintage font files to res/font and using them here
// For example: val vintageFontFamily = FontFamily(Font(R.font.my_vintage_font))

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif, // Example, choose a suitable vintage-style font
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = VintageTextColor
    ),
    displayMedium = TextStyle( // For Band Name
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = VintageTextColor
    ),
    titleLarge = TextStyle( // For Song Title
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = VintageTextColor
    ),
    bodyLarge = TextStyle( // General text
        fontFamily = FontFamily.SansSerif, // More readable for general text
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = VintageTextColor
    ),
    bodyMedium = TextStyle( // For Year, Decade
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = VintageSecondaryTextColor
    ),
    labelLarge = TextStyle( // For Button text
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = VintageBrown // Text color on buttons (assuming button bg is VintageAccent)
    ),
    labelSmall = TextStyle( // For slider time
        fontFamily = FontFamily.Monospace, // Good for time display
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = VintageSecondaryTextColor
    ),
    bodySmall = TextStyle( // For YouTube ID - less prominent
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = VintageSecondaryTextColor.copy(alpha = 0.6f)
    )
)
