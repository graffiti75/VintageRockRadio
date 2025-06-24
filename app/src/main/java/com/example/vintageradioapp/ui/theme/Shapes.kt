package com.example.vintageradioapp.ui.theme // Updated package name

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(2.dp), // Vintage might have sharper edges or subtle curves
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp) // Or slightly rounded like 8.dp for main panels
)
