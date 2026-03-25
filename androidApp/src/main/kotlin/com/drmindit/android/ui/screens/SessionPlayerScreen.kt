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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.player.AudioPlayerService
import com.drmindit.android.player.AudioPlayerState
import com.drmindit.android.player.rememberAudioPlayerManager
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.SessionPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionPlayerScreen(
    sessionTitle: String,
    sessionInstructor: String,
    sessionDuration: Int,
    audioUrl: String,
    sessionId: String,
    onBack: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioPlayerManager = rememberAudioPlayerManager(context)
    val playerState by audioPlayerManager.playerState.collectAsStateWithLifecycle()
    
    // Load audio when screen is first displayed
    LaunchedEffect(sessionId, audioUrl) {
        audioPlayerManager.loadAudio(
            sessionId = sessionId,
            audioUrl = audioUrl,
            title = sessionTitle,
            artist = sessionInstructor
        )
    }
    
    // Handle completion
    LaunchedEffect(playerState.isCompleted) {
        if (playerState.isCompleted) {
            delay(1000) // Give user time to see completion
            onComplete()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6B73FF),
                        Color(0xFF000DFF)
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
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = { /* Open menu */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album Art / Visualization
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (playerState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    // Audio visualization placeholder
                    AudioVisualization(
                        isPlaying = playerState.isPlaying,
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Session Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = playerState.title ?: sessionTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = playerState.artist ?: sessionInstructor,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = if (playerState.duration > 0) {
                        playerState.currentPosition.toFloat() / playerState.duration
                    } else 0f,
                    onValueChange = { value ->
                        if (playerState.duration > 0) {
                            val newPosition = (value * playerState.duration).toLong()
                            audioPlayerManager.seekTo(newPosition)
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    thumb = {
                        SliderDefaults.Thumb(
                            modifier = Modifier.size(16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White
                            )
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerState.formattedCurrentPosition,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = playerState.formattedDuration,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Backward
                PlaybackButton(
                    icon = Icons.Default.Replay10,
                    onClick = { audioPlayerManager.skipBackward(10) },
                    size = 48.dp
                )
                
                // Play/Pause
                PlaybackButton(
                    icon = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    onClick = {
                        if (playerState.isPlaying) {
                            audioPlayerManager.pause()
                        } else {
                            audioPlayerManager.play()
                        }
                    },
                    size = 72.dp,
                    backgroundColor = Color.White,
                    iconColor = Color(0xFF6B73FF)
                )
                
                // Skip Forward
                PlaybackButton(
                    icon = Icons.Default.Forward10,
                    onClick = { audioPlayerManager.skipForward(10) },
                    size = 48.dp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Speed Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeedButton(
                    speed = 0.5f,
                    currentSpeed = playerState.playbackSpeed,
                    onClick = { audioPlayerManager.setPlaybackSpeed(0.5f) }
                )
                
                SpeedButton(
                    speed = 0.75f,
                    currentSpeed = playerState.playbackSpeed,
                    onClick = { audioPlayerManager.setPlaybackSpeed(0.75f) }
                )
                
                SpeedButton(
                    speed = 1.0f,
                    currentSpeed = playerState.playbackSpeed,
                    onClick = { audioPlayerManager.setPlaybackSpeed(1.0f) }
                )
                
                SpeedButton(
                    speed = 1.25f,
                    currentSpeed = playerState.playbackSpeed,
                    onClick = { audioPlayerManager.setPlaybackSpeed(1.25f) }
                )
                
                SpeedButton(
                    speed = 1.5f,
                    currentSpeed = playerState.playbackSpeed,
                    onClick = { audioPlayerManager.setPlaybackSpeed(1.5f) }
                )
            }
            
            // Error Display
            playerState.error?.let { error ->
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        
                        IconButton(
                            onClick = { audioPlayerManager.clearError() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    iconColor: Color = Color.White
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun SpeedButton(
    speed: Float,
    currentSpeed: Float,
    onClick: () -> Unit
) {
    val isSelected = currentSpeed == speed
    val backgroundColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)
    val textColor = if (isSelected) Color(0xFF6B73FF) else Color.White
    
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        Text(
            text = "${speed}x",
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AudioVisualization(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedBars = (1..5).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 800,
                    delayMillis = index * 100,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedBars.forEach { animatedValue ->
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(40.dp * animatedValue.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = if (isPlaying) animatedValue.value else 0.3f))
            )
        }
    }
}
