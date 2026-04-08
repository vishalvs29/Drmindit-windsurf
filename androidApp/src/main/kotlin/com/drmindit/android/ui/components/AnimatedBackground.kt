package com.drmindit.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.cos

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    particles: Int = 20
) {
    val density = LocalDensity.current
    
    // Create infinite animation for background movement
    val infiniteTransition = rememberInfiniteTransition()
    
    // Animate particles
    val particles = remember(particles) {
        List(particles) { index ->
            Particle(
                initialX = (index * 100) % 1000,
                initialY = (index * 150) % 1000,
                speed = 0.5f + (index % 5) * 0.2f,
                amplitude = 20f + (index % 3) * 10f,
                phase = (index * 0.5f)
            )
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particle ->
            val animatedX by infiniteTransition.animateFloat(
                initialValue = particle.initialX,
                targetValue = particle.initialX + 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (10000 / particle.speed).toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            
            val animatedY by infiniteTransition.animateFloat(
                initialValue = particle.initialY,
                targetValue = particle.initialY + particle.amplitude * 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .offset(
                        x = (animatedX % 1000f).dp,
                        y = (animatedY % 1000f).dp
                    )
                    .size(
                        (4 + particle.amplitude / 10f).dp
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x4A4FD1C5), // Teal with opacity
                                Color(0x1A667EEA), // Purple with opacity
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun BreathingOrbBackground(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val breathingTransition = rememberInfiniteTransition()
    
    val scale by breathingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val rotation by breathingTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A4FD1C5),
                            Color(0x0D667EEA),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Middle orb
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x334FD1C5),
                            Color(0x1A667EEA),
                            Color(0x0DFFFFFF)
                        )
                    )
                )
        )
        
        // Inner animated orb
        Box(
            modifier = Modifier
                .size(200.dp * scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x664FD1C5),
                            Color(0x33667EEA),
                            Color(0x1AFFFFFF)
                        )
                    )
                )
        )
    }
}

@Composable
fun GradientWaveBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Wave layers
        repeat(3) { index ->
            val alpha = 0.1f - (index * 0.03f)
            val height = 200f + (index * 50f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp)
                    .offset(y = (waveOffset + index * 100) % 1000.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x4A4FD1C5).copy(alpha = alpha),
                                Color(0x4A667EEA).copy(alpha = alpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val amplitude: Float,
    val phase: Float
)
