package com.example.moviebooking.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Gradient colors cho các thành phần UI hiện đại
object ModernGradients {
    val primaryGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF121212),
            Color(0xFF1F1F1F)
        )
    )

    val accentGradient = Brush.horizontalGradient(
        colors = listOf(
            AccentColor,
            Color(0xFF00BCD4)
        )
    )

    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF252525),
            Color(0xFF1A1A1A)
        )
    )
}

// Các kích thước và spacing đồng nhất
object ModernDimensions {
    val cardRadius = 12.dp
    val buttonRadius = 24.dp
    val contentPadding = 16.dp
    val smallSpacing = 8.dp
    val mediumSpacing = 16.dp
    val largeSpacing = 24.dp
}

// Hiệu ứng và thuộc tính khác
object ModernEffects {
    const val defaultAnimationDuration = 300 // ms
    const val cardElevation = 0f
    const val buttonElevation = 0f
}