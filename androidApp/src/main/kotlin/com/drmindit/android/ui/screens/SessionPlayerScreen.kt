package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.theme.*

@Composable
fun SessionPlayerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val isPlaying = remember { mutableStateOf(false) }
    val currentTime = remember { mutableStateOf(0f) }
    val totalTime = 900f // 15 minutes in seconds
    
    // Breathing animation
    val breathingAnimation = rememberInfiniteTransition()
    val breathingScale by breathingAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Background gradient
    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1E3A5F), // Mid blue
            Color(0xFF0B1C2C), // Deep navy
            Color(0xFF0B1C2C), // Deep navy
        ),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            SessionPlayerTopBar(onNavigateBack = onNavigateBack)
            
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Breathing Orb
                BreathingOrb(
                    scale = breathingScale,
                    isPlaying = isPlaying.value
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Timer
                TimerDisplay(
                    currentTime = currentTime.value,
                    totalTime = totalTime,
                    isPlaying = isPlaying.value
                )
                
                Spacer(modifier = Modifier.height(60.dp))
                
                // Session Info
                SessionInfo()
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Progress Bar
                ProgressBar(
                    currentTime = currentTime.value,
                    totalTime = totalTime
                )
                
                Spacer(modifier = Modifier.height(60.dp))
                
                // Controls
                PlayerControls(
                    isPlaying = isPlaying.value,
                    onPlayPause = { isPlaying.value = !isPlaying.value },
                    onRewind = { currentTime.value = (currentTime.value - 15f).coerceAtLeast(0f) },
                    onForward = { currentTime.value = (currentTime.value + 15f).coerceAtMost(totalTime) }
                )
            }
        }
    }
}

@Composable
fun SessionPlayerTopBar(
    onNavigateBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(48.dp),
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
            Text(
                text = "Evening Meditation",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
        }
        
        IconButton(
            onClick = { /* Handle favorite */ },
            modifier = Modifier.size(48.dp),
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        ) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
        }
    }
}

@Composable
fun BreathingOrb(
    scale: Float,
    isPlaying: Boolean
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0x4A4FD1C5), // Teal with opacity
                        Color(0x1A667EEA), // Purple with opacity
                        Color(0x0DFFFFFF)  // White with opacity
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inner glow
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x664FD1C5), // Teal with higher opacity
                            Color(0x33667EEA), // Purple with opacity
                            Color(0x1AFFFFFF)  // White with opacity
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Center orb
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4FD1C5), // Teal
                                Color(0xFF667EEA), // Purple
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun TimerDisplay(
    currentTime: Float,
    totalTime: Float,
    isPlaying: Boolean
) {
    val minutes = (currentTime / 60).toInt()
    val seconds = (currentTime % 60).toInt()
    val totalMinutes = (totalTime / 60).toInt()
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
        
        Text(
            text = "of $totalMinutes minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SessionInfo() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Evening Meditation",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "Deep relaxation for better sleep",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoChip("Sleep", Color(0xFF667EEA))
            InfoChip("15 min", Color(0xFF4FD1C5))
            InfoChip("Beginner", Color(0xFF48BB78))
        }
    }
}

@Composable
fun InfoChip(
    text: String,
    color: Color
) {
    GlassCard(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        cornerRadius = 50.dp,
        backgroundColor = Color(0x0DFFFFFF),
        borderColor = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProgressBar(
    currentTime: Float,
    totalTime: Float
) {
    val progress = currentTime / totalTime
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Color(0x1AFFFFFF),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4FD1C5),
                                Color(0xFF667EEA)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                    )
            )
        }
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentTime),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
            Text(
                text = formatTime(totalTime),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rewind button
        ControlButton(
            icon = Icons.Default.Replay15,
            onClick = onRewind,
            size = 56.dp,
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        )
        
        // Play/Pause button
        ControlButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPlayPause,
            size = 80.dp,
            backgroundColor = Color(0xFF4FD1C5),
            contentColor = Color(0xFF0B1C2C)
        )
        
        // Forward button
        ControlButton(
            icon = Icons.Default.Forward15,
            onClick = onForward,
            size = 56.dp,
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        )
    }
}

@Composable
fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    contentColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        cornerRadius = 50.dp
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
    }
}

private fun formatTime(seconds: Float): String {
    val minutes = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return String.format("%d:%02d", minutes, secs)
}
