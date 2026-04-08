package com.drmindit.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = Color(0x0DFFFFFF), // 5% white
    borderColor: Color = Color(0x1AFFFFFF), // 10% white
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x33000000),
                spotColor = Color(0x33000000)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = borderWidth,
            color = borderColor
        )
    ) {
        content()
    }
}

@Composable
fun GlassCardWithGradient(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    gradient: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0x1A4FD1C5), // Teal with opacity
            Color(0x1A667EEA), // Purple with opacity
        )
    ),
    backgroundColor: Color = Color(0x0DFFFFFF), // 5% white
    shadowElevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x33000000),
                spotColor = Color(0x33000000)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = gradient
        )
    ) {
        content()
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = Color(0xFF1A3A52),
    shadowElevation: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x1A4FD1C5),
                spotColor = Color(0x1A4FD1C5)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        content()
    }
}
