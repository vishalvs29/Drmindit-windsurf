package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.SessionPlayerViewModel

@Composable
fun SessionPlayerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    sessionPlayerViewModel: SessionPlayerViewModel = viewModel()
) {
    val uiState by sessionPlayerViewModel.uiState.collectAsState()
    
    // Load a sample session on screen enter
    LaunchedEffect(Unit) {
        sessionPlayerViewModel.loadSession(
            title = "Evening Meditation",
            audioUrl = "https://www.soundhelix.com/files/mp3s/SoundHelix-Song-1.mp3", // Sample audio URL
            duration = 900f // 15 minutes
        )
    }
    
    // Mood rating state
    var showMoodRatingBefore by remember { mutableStateOf(false) }
    var showMoodRatingAfter by remember { mutableStateOf(false) }
    var currentMood by remember { mutableFloatStateOf(5.0f) }
    
    // Show mood rating after session completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted && !showMoodRatingAfter) {
            showMoodRatingAfter = true
        }
    }
    
    // Breathing animation synced with playback
    val breathingAnimation = rememberInfiniteTransition(label = "breathing")
    val breathingScale by breathingAnimation.animateFloat(
        initialValue = 1f,
        targetValue = if (uiState.isPlaying) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Mood rating before session
                if (showMoodRatingBefore) {
                    MoodRatingDialog(
                        currentMood = currentMood,
                        onMoodSelected = { mood ->
                            currentMood = mood
                            sessionPlayerViewModel.setMoodBefore(mood)
                            showMoodRatingBefore = false
                        },
                        onDismiss = { showMoodRatingBefore = false }
                    )
                }
                
                // Mood rating after session
                else if (showMoodRatingAfter) {
                    MoodRatingDialog(
                        currentMood = currentMood,
                        onMoodSelected = { mood ->
                            currentMood = mood
                            sessionPlayerViewModel.setMoodAfter(mood)
                            showMoodRatingAfter = false
                        },
                        onDismiss = { showMoodRatingAfter = false }
                    )
                }
                
                // Session content
                else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Breathing Orb with Haptic Guide
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BreathingOrb(
                                scale = breathingScale,
                                isPlaying = uiState.isPlaying
                            )
                            
                            // Haptic breathing guide overlay
                            if (uiState.isPlaying) {
                                val progress = if (uiState.duration > 0) uiState.currentPosition / uiState.duration else 0f
                                val breathingPhase = when {
                                    progress < 0.25f -> "Inhale"
                                    progress < 0.5f -> "Hold"
                                    progress < 0.75f -> "Exhale"
                                    else -> "Pause"
                                }
                                HapticBreathingGuide(
                                    isPlaying = uiState.isPlaying,
                                    breathingPhase = breathingPhase
                                )
                            }
                        }
                
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Timer
                        TimerDisplay(
                            currentTime = uiState.currentPosition,
                            totalTime = uiState.duration.toFloat(),
                            isPlaying = uiState.isPlaying
                        )
                        
                        Spacer(modifier = Modifier.height(60.dp))
                        
                        // Session Info
                        SessionInfoSection()
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        // Progress Bar
                        PlayerProgressBar(
                            currentTime = uiState.currentPosition,
                            totalTime = uiState.duration.toFloat()
                        )
                        
                        Spacer(modifier = Modifier.height(60.dp))
                        
                        // Controls
                        PlayerControlsSection(
                            isPlaying = uiState.isPlaying,
                            onPlayPause = { sessionPlayerViewModel.playPause() },
                            onRewind = { sessionPlayerViewModel.skipBackward(10) },
                            onForward = { sessionPlayerViewModel.skipForward(10) }
                        )
                    }
                }
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
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x1A4FD1C5), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = "Navigate back",
                tint = Color(0xFF4FD1C5)
            )
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
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x1A4FD1C5), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder, 
                contentDescription = "Favorite",
                tint = Color(0xFF4FD1C5)
            )
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
fun SessionInfoSection() {
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
fun PlayerProgressBar(
    currentTime: Float,
    totalTime: Float
) {
    val progress = if (totalTime > 0) currentTime / totalTime else 0f
    
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
                    shape = RoundedCornerShape(50.dp)
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
                        shape = RoundedCornerShape(50.dp)
                    )
            )
        }
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlayerTime(currentTime),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
            Text(
                text = formatPlayerTime(totalTime),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlayerControlsSection(
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
        PlayerControlButton(
            icon = Icons.Default.Replay,
            onClick = onRewind,
            size = 56.dp,
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        )
        
        // Play/Pause button
        PlayerControlButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            onClick = onPlayPause,
            size = 80.dp,
            backgroundColor = Color(0xFF4FD1C5),
            contentColor = Color(0xFF0B1C2C)
        )
        
        // Forward button
        PlayerControlButton(
            icon = Icons.AutoMirrored.Filled.Forward,
            onClick = onForward,
            size = 56.dp,
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        )
    }
}

@Composable
fun PlayerControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    contentColor: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp)
        )
    }
}

private fun formatPlayerTime(seconds: Float): String {
    val minutes = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return String.format("%d:%02d", minutes, secs)
}
