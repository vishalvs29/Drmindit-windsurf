package com.drmindit.android.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6B73FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF5558D9),
    onPrimaryContainer = Color(0xFFE1E0FF),
    
    secondary = Color(0xFFB39DDB),
    onSecondary = Color(0xFF1A1A2E),
    secondaryContainer = Color(0xFF2D2D44),
    onSecondaryContainer = Color(0xFFE8DEFF),
    
    tertiary = Color(0xFF81D4FA),
    onTertiary = Color(0xFF003547),
    tertiaryContainer = Color(0xFF004D63),
    onTertiaryContainer = Color(0xFFB8EFFF),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFFC5C4D6),
    
    outline = Color(0xFF8F8FA3),
    outlineVariant = Color(0xFF2A2A3E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6B73FF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1E0FF),
    onPrimaryContainer = Color(0xFF1F1F4E),
    
    secondary = Color(0xFF7E57C2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEFF),
    onSecondaryContainer = Color(0xFF2A1A4E),
    
    tertiary = Color(0xFF29B6F6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB8EFFF),
    onTertiaryContainer = Color(0xFF004D63),
    
    background = Color(0xFFF8F9FE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    
    surfaceVariant = Color(0xFFE6E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun DrMinditTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
