package com.drmindit.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BreathingLoadingAnimation(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 60.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4FD1C5).copy(alpha = alpha),
                            Color(0xFF667EEA).copy(alpha = alpha * 0.7f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun PulsingDotsLoadingAnimation(
    modifier: Modifier = Modifier,
    dotSize: androidx.compose.ui.unit.Dp = 8.dp,
    dotSpacing: androidx.compose.ui.unit.Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val delay = index * 200
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(delay)
                )
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(
                        Color(0xFF4FD1C5).copy(alpha = alpha)
                    )
            )
        }
    }
}

@Composable
fun CircularLoadingAnimation(
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 3.dp,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(size)
        ) {
            val canvasWidth = size.toPx()
            val canvasHeight = size.toPx()
            val strokeWidthPx = strokeWidth.toPx()
            
            // Background circle
            drawCircle(
                color = Color(0x1AFFFFFF),
                radius = (canvasWidth / 2) - strokeWidthPx / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidthPx
                )
            )
            
            // Progress arc
            drawArc(
                color = Color(0xFF4FD1C5),
                startAngle = -90f,
                sweepAngle = (rotation / 360f) * 270f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidthPx,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = canvasWidth - strokeWidthPx,
                    height = canvasHeight - strokeWidthPx
                ),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = strokeWidthPx / 2,
                    y = strokeWidthPx / 2
                )
            )
        }
    }
}

@Composable
fun MeditationLoadingAnimation(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val middleScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(500)
        )
    )
    
    val innerScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1000)
        )
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Box(
            modifier = Modifier
                .size(size * outerScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x1A4FD1C5)
                        )
                    )
                )
        )
        
        // Middle ring
        Box(
            modifier = Modifier
                .size(size * middleScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x1A667EEA)
                        )
                    )
                )
        )
        
        // Inner ring
        Box(
            modifier = Modifier
                .size(size * innerScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x1AFFFFFF)
                        )
                    )
                )
        )
    }
}
