package com.example.moviebooking.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.moviebooking.ui.theme.PoppinsFamily

object BrandingUtil {
    // Brand colors
    val PrimaryBrand = Color(0xFF2196F3)     // Blue
    val SecondaryBrand = Color(0xFFFF9800)   // Orange
    val AccentBrand = Color(0xFF4CAF50)      // Green

    // Logo styling
    val LogoTextStyle = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 1.sp,
        color = PrimaryBrand
    )

    // App name styling
    val AppNameStyle = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        color = PrimaryBrand
    )

    // Slogan styling
    val SloganStyle = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp,
        color = Color.Gray
    )
}