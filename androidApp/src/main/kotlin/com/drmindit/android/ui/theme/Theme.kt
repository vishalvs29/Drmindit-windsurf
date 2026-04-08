package com.drmindit.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark Color Scheme for DrMindit
private val DarkColorScheme = darkColorScheme(
    primary = DrMinditColors.primary,
    onPrimary = DrMinditColors.onPrimary,
    primaryContainer = DrMinditColors.primaryVariant,
    onPrimaryContainer = DrMinditColors.onBackground,
    
    secondary = DrMinditColors.secondary,
    onSecondary = DrMinditColors.onSecondary,
    secondaryContainer = DrMinditColors.primaryVariant,
    onSecondaryContainer = DrMinditColors.onBackground,
    
    background = DrMinditColors.background,
    onBackground = DrMinditColors.onBackground,
    
    surface = DrMinditColors.surface,
    onSurface = DrMinditColors.onSurface,
    
    surfaceVariant = DrMinditColors.card,
    onSurfaceVariant = DrMinditColors.onSurface,
    
    error = DrMinditColors.error,
    onError = Color.White,
    
    outline = DrMinditColors.glassBorder,
    outlineVariant = DrMinditColors.glassBorder,
    
    scrim = DrMinditColors.glassShadow,
)

@Composable
fun DrMinditTheme(
    darkTheme: Boolean = true, // Always use dark theme for mental wellness
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        DarkColorScheme // Force dark theme even in light mode
    }

    MaterialTheme(
        colorScheme = colors,
        typography = DrMinditTypography,
        shapes = DrMinditShapes,
        content = content
    )
}

// Extension functions for accessing theme colors
val MaterialTheme.drMinditColors: DrMinditColorPalette
    @Composable get() = DrMinditColors

val MaterialTheme.customTypography: CustomTypography
    @Composable get() = CustomTypography

val MaterialTheme.customShapes: CustomShapes
    @Composable get() = CustomShapes
