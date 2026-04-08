package com.drmindit.android.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Color Palette - Premium Dark Theme
val DrMinditColors = DrMinditColorPalette(
    // Background Colors
    background = Color(0xFF0B1C2C), // Deep navy blue
    surface = Color(0xFF0F2940), // Slightly lighter navy
    card = Color(0xFF1A3A52), // Card background with transparency
    
    // Primary Colors
    primary = Color(0xFF4FD1C5), // Teal accent
    primaryVariant = Color(0xFF38B2AC), // Darker teal
    secondary = Color(0xFF667EEA), // Soft purple
    
    // Gradient Colors
    gradientStart = Color(0xFF0B1C2C), // Deep navy
    gradientMid = Color(0xFF1E3A5F), // Mid blue
    gradientEnd = Color(0xFF4FD1C5), // Teal
    
    // Text Colors
    onBackground = Color(0xFFE2E8F0), // Light gray text
    onSurface = Color(0xFFE2E8F0), // Light gray text
    onPrimary = Color(0xFF0B1C2C), // Dark text on primary
    onSecondary = Color(0xFF0B1C2C), // Dark text on secondary
    
    // Status Colors
    success = Color(0xFF48BB78), // Green
    warning = Color(0xFFED8936), // Orange
    error = Color(0xFFF56565), // Red
    info = Color(0xFF4299E1), // Blue
    
    // Mood Colors
    moodCalm = Color(0xFF4FD1C5), // Teal
    moodHappy = Color(0xFFF6E05E), // Yellow
    moodFocused = Color(0xFF667EEA), // Purple
    moodAnxious = Color(0xFFED8936), // Orange
    moodSad = Color(0xFF718096), // Gray
    moodEnergetic = Color(0xFF48BB78), // Green
    
    // Glass Effects
    glassBackground = Color(0x0DFFFFFF), // 5% white
    glassBorder = Color(0x1AFFFFFF), // 10% white
    glassShadow = Color(0x33000000), // 20% black
    
    // Interactive States
    hover = Color(0x1A4FD1C5), // Teal with opacity
    pressed = Color(0x334FD1C5), // Teal with higher opacity
    disabled = Color(0x4A718096), // Gray with opacity
)

data class DrMinditColorPalette(
    val background: Color,
    val surface: Color,
    val card: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val moodCalm: Color,
    val moodHappy: Color,
    val moodFocused: Color,
    val moodAnxious: Color,
    val moodSad: Color,
    val moodEnergetic: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val glassShadow: Color,
    val hover: Color,
    val pressed: Color,
    val disabled: Color,
)

// Extension functions for easy access
val ColorPalette.background: Color get() = DrMinditColors.background
val ColorPalette.surface: Color get() = DrMinditColors.surface
val ColorPalette.card: Color get() = DrMinditColors.card
val ColorPalette.primary: Color get() = DrMinditColors.primary
val ColorPalette.primaryVariant: Color get() = DrMinditColors.primaryVariant
val ColorPalette.secondary: Color get() = DrMinditColors.secondary
val ColorPalette.onBackground: Color get() = DrMinditColors.onBackground
val ColorPalette.onSurface: Color get() = DrMinditColors.onSurface
val ColorPalette.onPrimary: Color get() = DrMinditColors.onPrimary
val ColorPalette.onSecondary: Color get() = DrMinditColors.onSecondary

object ColorPalette {
    val background = DrMinditColors.background
    val surface = DrMinditColors.surface
    val card = DrMinditColors.card
    val primary = DrMinditColors.primary
    val primaryVariant = DrMinditColors.primaryVariant
    val secondary = DrMinditColors.secondary
    val onBackground = DrMinditColors.onBackground
    val onSurface = DrMinditColors.onSurface
    val onPrimary = DrMinditColors.onPrimary
    val onSecondary = DrMinditColors.onSecondary
    val success = DrMinditColors.success
    val warning = DrMinditColors.warning
    val error = DrMinditColors.error
    val info = DrMinditColors.info
}
