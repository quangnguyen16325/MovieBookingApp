package com.example.moviebooking.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class FontScale(
    val scale: Float = 1.0f,
    val baseHeadingSize: TextUnit = 28.sp,
    val baseSubheadingSize: TextUnit = 16.sp,
    val baseBodySize: TextUnit = 14.sp,
    val baseCaptionSize: TextUnit = 12.sp
) {
    val headingSize: TextUnit get() = (baseHeadingSize.value * scale).sp
    val subheadingSize: TextUnit get() = (baseSubheadingSize.value * scale).sp
    val bodySize: TextUnit get() = (baseBodySize.value * scale).sp
    val captionSize: TextUnit get() = (baseCaptionSize.value * scale).sp
}

val LocalFontScale = staticCompositionLocalOf { FontScale() }