package com.example.vintageradioapp.ui.theme // Updated package name

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif, // A more vintage feel
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = VintageTextColor // Ensure text color is set if not inheriting
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = VintageTextColor
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = VintageTextColor
    ),
    // Define other styles like button, caption if needed
    // Example for Button text
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, // Buttons might be more readable with SansSerif
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = VintageCream // Or a contrasting color for buttons
    ),
    displayMedium = TextStyle( // For song title, band
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = VintageTextColor
    ),
    bodyMedium = TextStyle( // For year, decade
        fontFamily = FontFamily.Serif,
        fontSize = 14.sp,
        color = VintageTextColor.copy(alpha = 0.8f)
    ),
    bodySmall = TextStyle( // For youtube ID
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = VintageTextColor.copy(alpha = 0.6f)
    )

)
