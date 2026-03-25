package com.drmindit.android.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.drmindit.android.player.AudioPlayerController
import com.drmindit.android.player.rememberAudioPlayerManager
import com.drmindit.android.ui.components.*
import com.drmindit.shared.domain.model.AudioSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    audioPlayerController: AudioPlayerController = hiltViewModel(),
    audioPlayerManager: AudioPlayerManager = rememberAudioPlayerManager(LocalContext.current)
) {
    // Connect controller to player manager
    LaunchedEffect(Unit) {
        audioPlayerController.connectToAudioPlayerManager(audioPlayerManager)
        audioPlayerController.loadSession(sessionId)
    }
    
    val currentSession by audioPlayerController.currentSession.collectAsStateWithLifecycle()
    val playerState by audioPlayerController.playerState.collectAsStateWithLifecycle()
    val isLoading by audioPlayerController.isLoading.collectAsStateWithLifecycle()
    val error by audioPlayerController.error.collectAsStateWithLifecycle()
    val isFavorite by audioPlayerController.isFavorite.collectAsStateWithLifecycle()
    val isDownloaded by audioPlayerController.isDownloaded.collectAsStateWithLifecycle()
    val downloadProgress by audioPlayerController.downloadProgress.collectAsStateWithLifecycle()
    
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = screenHeight.value
                )
            )
    ) {
        // Background image
        currentSession?.let { session ->
            AsyncImage(
                model = session.thumbnailUrl,
                contentDescription = session.title,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.3f),
                contentScale = ContentScale.Crop
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Row {
                    IconButton(
                        onClick = { audioPlayerController.toggleFavorite() },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            if (isDownloaded) {
                                audioPlayerController.deleteDownloadedSession()
                            } else {
                                audioPlayerController.downloadSession()
                            }
                        },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        if (downloadProgress > 0f && downloadProgress < 1f) {
                            CircularProgressIndicator(
                                progress = downloadProgress,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Session info
            currentSession?.let { session ->
                SessionInfoSection(
                    session = session,
                    playerState = playerState,
                    isLoading = isLoading,
                    error = error,
                    onPlayPause = { 
                        if (playerState.isPlaying) {
                            audioPlayerController.pause()
                        } else {
                            audioPlayerController.play()
                        }
                    },
                    onSeek = audioPlayerController::seekTo,
                    onSkipForward = audioPlayerController::skipForward,
                    onSkipBackward = audioPlayerController::skipBackward,
                    onSetSpeed = audioPlayerController::setPlaybackSpeed,
                    onRetry = audioPlayerController::retryLoad,
                    onClearError = audioPlayerController::clearError
                )
            }
        }
    }
}

@Composable
private fun SessionInfoSection(
    session: AudioSession,
    playerState: AudioPlayerState,
    isLoading: Boolean,
    error: String?,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Thumbnail
        AsyncImage(
            model = session.thumbnailUrl,
            contentDescription = session.title,
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title and instructor
        Text(
            text = session.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "by ${session.instructorName}",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Category and duration
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Text(
                    text = session.category.name,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
            
            Surface(
                modifier = Modifier.padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Text(
                    text = formatDuration(session.duration),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Loading state
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading session...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        // Error state
        error?.let { errorMessage ->
            Column(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onClearError
                            ) {
                                Text("Dismiss")
                            }
                            Button(
                                onClick = onRetry
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
        
        // Player controls
        if (!isLoading && error == null) {
            PlayerControlsSection(
                playerState = playerState,
                session = session,
                onPlayPause = onPlayPause,
                onSeek = onSeek,
                onSkipForward = onSkipForward,
                onSkipBackward = onSkipBackward,
                onSetSpeed = onSetSpeed
            )
        }
    }
}

@Composable
private fun PlayerControlsSection(
    playerState: AudioPlayerState,
    session: AudioSession,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSetSpeed: (Float) -> Unit
) {
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    var speedExpanded by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        if (playerState.duration > 0) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = playerState.currentPosition.toFloat(),
                    onValueChange = { value -> onSeek(value.toLong()) },
                    valueRange = 0f..playerState.duration.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onPrimary,
                        activeTrackColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerState.formattedCurrentPosition,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    Text(
                        text = playerState.formattedDuration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        } else if (playerState.isBuffering) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Buffering...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main controls
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Skip backward
            IconButton(
                onClick = onSkipBackward,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Skip backward 10s",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Play/Pause
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .size(72.dp)
            ) {
                if (playerState.isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            // Skip forward
            IconButton(
                onClick = onSkipForward,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Skip forward 10s",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Speed control
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Speed:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box {
                OutlinedButton(
                    onClick = { speedExpanded = true },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "${playerState.playbackSpeed}x",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                DropdownMenu(
                    expanded = speedExpanded,
                    onDismissRequest = { speedExpanded = false }
                ) {
                    speedOptions.forEach { speed ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${speed}x",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            onClick = {
                                onSetSpeed(speed)
                                speedExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
