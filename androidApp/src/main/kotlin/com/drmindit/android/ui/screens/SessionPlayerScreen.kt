package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drmindit.android.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionPlayerScreen(
    sessionTitle: String = "Morning Meditation",
    instructor: String = "Dr. Sarah Chen",
    duration: Int = 600, // seconds
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0f) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    
    // Animation for waveform visualization
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_animation")
    val animatedBars by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6B73FF).copy(alpha = 0.3f),
                        Color(0xFFB39DDB).copy(alpha = 0.2f),
                        Color(0xFF121212)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BackButton(onClick = onBack)
                
                IconButton(
                    onClick = { /* Handle favorite */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Session Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sessionTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = instructor,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Audio Visualization
            AudioVisualization(
                isPlaying = isPlaying,
                animatedValue = animatedBars,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Progress Section
            ProgressSection(
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { position -> currentPosition = position }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Playback Controls
            PlaybackControls(
                isPlaying = isPlaying,
                onPlayPause = { isPlaying = !isPlaying },
                onSkipBackward = { /* Skip 10 seconds back */ },
                onSkipForward = { /* Skip 10 seconds forward */ },
                playbackSpeed = playbackSpeed,
                onSpeedChange = { speed -> playbackSpeed = speed }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Actions
            BottomActions(onComplete = onComplete)
        }
    }
}

@Composable
private fun AudioVisualization(
    isPlaying: Boolean,
    animatedValue: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 40
    val barHeights = remember(barCount) { 
        List(barCount) { (0.2f..1f).random() } 
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val targetHeight = if (isPlaying) {
                barHeights[index] * animatedValue
            } else {
                0.2f
            }
            
            val height by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * 10,
                    easing = EaseOutCubic
                ),
                label = "bar_height_$index"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height * 160.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ProgressSection(
    currentPosition: Float,
    duration: Int,
    onSeek: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Bar
        Slider(
            value = currentPosition,
            onValueChange = onSeek,
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition.toInt()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    playbackSpeed: Float,
    onSpeedChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Speed Control
        Card(
            onClick = {
                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                val currentIndex = speeds.indexOf(playbackSpeed)
                val nextIndex = (currentIndex + 1) % speeds.size
                onSpeedChange(speeds[nextIndex])
            },
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${playbackSpeed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Skip Backward
        IconButton(
            onClick = onSkipBackward,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Skip Backward",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        // Play/Pause Button
        PlayButton(
            isPlaying = isPlaying,
            onClick = onPlayPause,
            size = 80f
        )

        // Skip Forward
        IconButton(
            onClick = onSkipForward,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Skip Forward",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        // Timer
        IconButton(
            onClick = { /* Handle timer */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BottomActions(onComplete: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Complete Session Button
        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Complete Session",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Additional Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButtonWithLabel(
                icon = Icons.Default.Download,
                label = "Download",
                onClick = { /* Handle download */ }
            )
            
            IconButtonWithLabel(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = { /* Handle share */ }
            )
            
            IconButtonWithLabel(
                icon = Icons.Default.Bookmark,
                label = "Save",
                onClick = { /* Handle save */ }
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
