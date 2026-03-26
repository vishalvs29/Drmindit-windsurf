package com.drmindit.android.ui.components

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drmindit.android.player.MeditationAudioService
import com.drmindit.android.ui.viewmodel.MeditationPlayerViewModel
import com.drmindit.shared.domain.model.MeditationSession
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Meditation Player Screen with proper lifecycle handling
 * Fixes the background playback bug by stopping audio when user exits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationPlayerScreen(
    session: MeditationSession,
    onNavigateBack: () -> Unit,
    viewModel: MeditationPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // Collect state from ViewModel
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    
    // Screen lifecycle management
    val isScreenVisible = remember { mutableStateOf(true) }
    
    // Load session when screen appears
    LaunchedEffect(session) {
        Timber.d("MeditationPlayerScreen: Loading session ${session.id}")
        viewModel.loadSession(session)
    }
    
    // Handle screen visibility changes
    DisposableEffect(Unit) {
        onDispose {
            Timber.d("MeditationPlayerScreen: Screen being disposed - stopping playback")
            isScreenVisible.value = false
            
            // CRITICAL: Stop playback when screen is disposed
            viewModel.onUserLeavingScreen()
        }
    }
    
    // Handle back navigation
    BackHandler {
        Timber.d("MeditationPlayerScreen: Back pressed - stopping playback")
        viewModel.onUserLeavingScreen()
        onNavigateBack()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E), // Deep blue
                        Color(0xFF283593), // Medium blue
                        Color(0xFF3949AB)  // Light blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        Timber.d("MeditationPlayerScreen: Back button clicked")
                        viewModel.onUserLeavingScreen()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Session title
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                
                // More options
                IconButton(
                    onClick = { /* TODO: Show options */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album art / session image
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Color.Black.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                session.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = session.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    // Default meditation icon
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Meditation",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Session info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = session.instructor ?: "DrMindit",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${session.duration} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress section
            if (playerState.isReady || playerState.isBuffering) {
                ProgressSection(
                    playerState = playerState,
                    onSeek = { position -> viewModel.seekTo(position) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Playback controls
            PlaybackControlsSection(
                playerState = playerState,
                uiState = uiState,
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                onStop = { viewModel.stop() },
                onSkipForward = { viewModel.skipForward() },
                onSkipBackward = { viewModel.skipBackward() },
                onSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error display
            if (uiState.hasError) {
                ErrorSection(
                    errorMessage = uiState.errorMessage ?: "Unknown error occurred",
                    onRetry = { 
                        viewModel.clearError()
                        viewModel.loadSession(session)
                    },
                    onDismiss = { viewModel.clearError() }
                )
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

/**
 * Progress section with seek bar
 */
@Composable
private fun ProgressSection(
    playerState: com.drmindit.android.player.MeditationAudioPlayerState,
    onSeek: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = playerState.formattedCurrentPosition,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Text(
                text = playerState.formattedDuration,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        Slider(
            value = if (playerState.duration > 0) {
                playerState.currentPosition.toFloat() / playerState.duration
            } else 0f,
            onValueChange = { progress ->
                val position = (progress * playerState.duration).toLong()
                onSeek(position)
            },
            enabled = !playerState.isBuffering && playerState.isReady,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Buffering indicator
        if (playerState.isBuffering) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Buffering...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Playback controls section
 */
@Composable
private fun PlaybackControlsSection(
    playerState: com.drmindit.android.player.MeditationAudioPlayerState,
    uiState: MeditationPlayerUiState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSpeedChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main controls row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Skip backward
            ControlButton(
                icon = Icons.Default.Replay10,
                onClick = onSkipBackward,
                enabled = playerState.isReady && !playerState.isBuffering
            )
            
            // Play/Pause button
            ControlButton(
                icon = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                onClick = if (playerState.isPlaying) onPause else onPlay,
                enabled = playerState.isReady && !playerState.isBuffering,
                size = 64.dp,
                iconSize = 32.dp
            )
            
            // Skip forward
            ControlButton(
                icon = Icons.Default.Forward30,
                onClick = onSkipForward,
                enabled = playerState.isReady && !playerState.isBuffering
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Secondary controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stop button
            ControlButton(
                icon = Icons.Default.Stop,
                onClick = onStop,
                enabled = playerState.isReady,
                size = 48.dp,
                iconSize = 24.dp
            )
            
            // Speed control
            SpeedControlButton(
                currentSpeed = playerState.playbackSpeed,
                onSpeedChange = onSpeedChange,
                enabled = playerState.isReady && !playerState.isBuffering
            )
        }
    }
}

/**
 * Individual control button
 */
@Composable
private fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                if (enabled) {
                    Color.White.copy(alpha = 0.2f)
                } else {
                    Color.White.copy(alpha = 0.1f)
                },
                CircleShape
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Speed control button
 */
@Composable
private fun SpeedControlButton(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        ControlButton(
            icon = Icons.Default.Speed,
            onClick = { expanded = true },
            enabled = enabled
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            speedOptions.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = {
                        onSpeedChange(speed)
                        expanded = false
                    },
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * Error section display
 */
@Composable
private fun ErrorSection(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f),
            contentColor = Color.Red
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Retry")
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}
