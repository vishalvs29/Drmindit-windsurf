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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.drmindit.android.player.EnhancedAudioService
import com.drmindit.android.ui.viewmodel.EnhancedMeditationPlayerViewModel
import com.drmindit.shared.domain.model.MeditationSession
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Enhanced Meditation Player Screen with comprehensive lifecycle management
 * Fixes all background playback and memory leak issues
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedMeditationPlayerScreen(
    session: MeditationSession,
    onNavigateBack: () -> Unit,
    viewModel: EnhancedMeditationPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // Collect state from ViewModel
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    
    // Screen lifecycle management
    var isScreenVisible by remember { mutableStateOf(true) }
    var lastScreenChange by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Load session when screen appears
    LaunchedEffect(session) {
        Timber.d("🎵 Screen: Loading session ${session.id}")
        viewModel.loadSession(session)
        isScreenVisible = true
        viewModel.onScreenVisible()
    }
    
    // CRITICAL: Handle screen disposal - FIXES BACKGROUND PLAYBACK BUG
    DisposableEffect(Unit) {
        onDispose {
            Timber.d("🎵 Screen: DISPOSING - CRITICAL CLEANUP")
            isScreenVisible = false
            
            // Check for rapid screen switching
            val now = System.currentTimeMillis()
            if (now - lastScreenChange < 500) {
                Timber.d("🎵 Rapid screen switch detected")
                viewModel.onRapidScreenSwitch()
            }
            lastScreenChange = now
            
            // IMMEDIATELY stop playback
            viewModel.onUserLeavingScreen()
        }
    }
    
    // Handle back navigation with cleanup
    BackHandler {
        Timber.d("🎵 Screen: Back pressed - CRITICAL CLEANUP")
        viewModel.onUserLeavingScreen()
        onNavigateBack()
    }
    
    // Handle app lifecycle changes
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.lifecycle.compose.collectLifecycleState(lifecycleOwner) { event ->
        when (event) {
            androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                Timber.d("🎵 Screen: Lifecycle PAUSE")
                viewModel.onAppBackgrounded()
            }
            androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                Timber.d("🎵 Screen: Lifecycle RESUME")
                viewModel.onAppForegrounded()
            }
            else -> {}
        }
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
            TopBarSection(
                onBackClick = {
                    Timber.d("🎵 Screen: Back button clicked")
                    viewModel.onUserLeavingScreen()
                    onNavigateBack()
                },
                sessionTitle = session.title
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Album art
            AlbumArtSection(
                imageUrl = session.imageUrl,
                title = session.title
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Session info
            SessionInfoSection(session = session)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress section
            if (playerState.isReady || playerState.isBuffering) {
                ProgressSection(
                    playerState = playerState,
                    onSeek = { position -> 
                        if (isScreenVisible) {
                            viewModel.seekTo(position)
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Error display
            if (uiState.hasError) {
                ErrorSection(
                    error = uiState.errorMessage,
                    onRetry = { viewModel.loadSession(session) },
                    onDismiss = { viewModel.clearError() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Playback controls
            PlaybackControlsSection(
                playerState = playerState,
                uiState = uiState,
                isScreenVisible = isScreenVisible,
                onPlay = { if (isScreenVisible) viewModel.play() },
                onPause = { if (isScreenVisible) viewModel.pause() },
                onStop = { 
                    viewModel.stopPlayback()
                    onNavigateBack()
                },
                onSkipForward = { if (isScreenVisible) viewModel.skipForward() },
                onSkipBackward = { if (isScreenVisible) viewModel.skipBackward() },
                onSpeedChange = { if (isScreenVisible) viewModel.setPlaybackSpeed(it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading indicator
            if (uiState.isLoading) {
                LoadingSection()
            }
        }
    }
}

@Composable
private fun TopBarSection(
    onBackClick: () -> Unit,
    sessionTitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                Timber.d("🎵 TopBar: Back clicked")
                onBackClick()
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
        
        Text(
            text = sessionTitle,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        
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
}

@Composable
private fun AlbumArtSection(
    imageUrl: String?,
    title: String
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Default album art
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
private fun SessionInfoSection(session: MeditationSession) {
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
            textAlign = CenterHorizontally
        )
    }
}

@Composable
private fun ProgressSection(
    playerState: EnhancedAudioPlayerState,
    onSeek: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar
        Slider(
            value = playerState.currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..playerState.duration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = playerState.formattedCurrentPosition,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                text = playerState.formattedDuration,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ErrorSection(
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error ?: "An error occurred",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun PlaybackControlsSection(
    playerState: EnhancedAudioPlayerState,
    uiState: EnhancedMeditationPlayerUiState,
    isScreenVisible: Boolean,
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
        // Main controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip backward
            ControlButton(
                icon = Icons.Default.Replay10,
                onClick = onSkipBackward,
                enabled = isScreenVisible && playerState.isReady && !playerState.isBuffering
            )
            
            // Play/Pause
            ControlButton(
                icon = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                onClick = if (playerState.isPlaying) onPause else onPlay,
                enabled = isScreenVisible && playerState.isReady && !playerState.isBuffering,
                size = 64.dp,
                backgroundColor = Color.White
            )
            
            // Skip forward
            ControlButton(
                icon = Icons.Default.Forward10,
                onClick = onSkipForward,
                enabled = isScreenVisible && playerState.isReady && !playerState.isBuffering
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Secondary controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed control
            SpeedControlButton(
                currentSpeed = uiState.playbackSpeed,
                onSpeedChange = onSpeedChange,
                enabled = isScreenVisible && playerState.isReady
            )
            
            // Stop button
            ControlButton(
                icon = Icons.Default.Stop,
                onClick = onStop,
                enabled = isScreenVisible && playerState.isReady,
                backgroundColor = Color.Red.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f)
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .background(
                backgroundColor,
                CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

@Composable
private fun SpeedControlButton(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    CircleShape
                )
        ) {
            Text(
                text = "${currentSpeed}x",
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = {
                        onSpeedChange(speed)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading meditation...",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
