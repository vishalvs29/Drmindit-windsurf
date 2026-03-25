package com.drmindit.android.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.drmindit.android.player.ImprovedAudioPlayerController
import com.drmindit.android.player.ImprovedAudioPlayerState

/**
 * Improved Audio Player Screen with proper lifecycle management
 */
@Composable
fun ImprovedAudioPlayerScreen(
    audioPlayerController: ImprovedAudioPlayerController
) {
    val playerState by audioPlayerController.playerState.collectAsStateWithLifecycle()
    val currentSession by audioPlayerController.currentSession.collectAsStateWithLifecycle()
    val isLoading by audioPlayerController.isLoading.collectAsStateWithLifecycle()
    val error by audioPlayerController.error.collectAsStateWithLifecycle()
    val isFavorite by audioPlayerController.isFavorite.collectAsStateWithLifecycle()
    val isDownloaded by audioPlayerController.isDownloaded.collectAsStateWithLifecycle()
    val downloadProgress by audioPlayerController.downloadProgress.collectAsStateWithLifecycle()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Effect to initialize player when screen is first composed
    LaunchedEffect(Unit) {
        audioPlayerController.initialize(lifecycleOwner)
    }
    
    // Effect to cleanup when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            audioPlayerController.stop()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Session Info Header
            if (currentSession != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title and Artist
                        Text(
                            text = playerState.title ?: "No Title",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (playerState.artist != null) {
                            Text(
                                text = playerState.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Session ID and Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session: ${currentSession.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            
                            // Status badges
                            if (isFavorite.value) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            if (isDownloaded.value) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "Downloaded",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Player Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Loading and Error States
                    when {
                        isLoading.value -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading audio...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        error.value != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = error.value ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        else -> {
                            // Seek Bar
                            if (playerState.duration > 0) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = formatTime(playerState.currentPosition),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Text(
                                        text = " / ${formatTime(playerState.duration)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Progress Bar
                                    LinearProgressIndicator(
                                        progress = playerState.progressPercentage / 100f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surface,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Playback Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Skip Backward
                                IconButton(
                                    onClick = { audioPlayerController.skipBackward(10) },
                                    enabled = playerState.duration > 0,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Skip Backward 10 seconds",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Play/Pause Button
                                IconButton(
                                    onClick = { 
                                        if (playerState.isPlaying) {
                                            audioPlayerController.pause()
                                        } else {
                                            audioPlayerController.play()
                                        }
                                    },
                                    enabled = playerState.duration > 0,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Skip Forward
                                IconButton(
                                    onClick = { audioPlayerController.skipForward(10) },
                                    enabled = playerState.duration > 0,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Skip Forward 10 seconds",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Stop Button
                                IconButton(
                                    onClick = { audioPlayerController.stop() },
                                    enabled = playerState.duration > 0,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                        ),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Favorite Button
                                IconButton(
                                    onClick = { audioPlayerController.toggleFavorite() },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = if (isFavorite.value) {
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            }
                                        ),
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite.value) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite.value) "Remove from Favorites" else "Add to Favorites",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                // Speed Control
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.CenterVertically),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { 
                                            val newSpeed = if (playerState.playbackSpeed >= 2.0f) 0.5f else playerState.playbackSpeed + 0.5f
                                            audioPlayerController.setPlaybackSpeed(newSpeed)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease Speed",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    Text(
                                        text = "${playerState.playbackSpeed}x",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    
                                    IconButton(
                                        onClick = { 
                                            val newSpeed = if (playerState.playbackSpeed <= 1.5f) 2.0f else playerState.playbackSpeed + 0.5f
                                            audioPlayerController.setPlaybackSpeed(newSpeed)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase Speed",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}

/**
 * Format time in MM:SS format
 */
private fun formatTime(timeMs: Int): String {
    if (timeMs < 0) return "0:00"
    
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
