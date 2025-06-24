package com.example.vintageradioapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp), // Slightly more rounded for bigger elements
    large = RoundedCornerShape(12.dp)  // For dialogs or large panels
)
