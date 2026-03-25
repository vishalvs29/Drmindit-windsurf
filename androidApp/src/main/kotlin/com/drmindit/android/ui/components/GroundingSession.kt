package com.drmindit.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Grounding session component for crisis management
 * Provides breathing exercises and calming techniques
 */
@Composable
fun GroundingSession(
    onComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(GroundingPhase.INHALE) }
    var cyclesCompleted by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    
    val totalCycles = 5
    val phaseDuration = when (currentPhase) {
        GroundingPhase.INHALE -> 4000L
        GroundingPhase.HOLD -> 4000L
        GroundingPhase.EXHALE -> 4000L
        GroundingPhase.PAUSE -> 2000L
    }
    
    // Animation for breathing circle
    val animatedSize by animateFloatAsState(
        targetValue = if (currentPhase == GroundingPhase.INHALE) 1.2f else 0.8f,
        animationSpec = tween(
            durationMillis = phaseDuration.toInt(),
            easing = EaseInOutCubic
        ),
        label = "breathing"
    )
    
    LaunchedEffect(isPlaying, currentPhase) {
        if (isPlaying) {
            delay(phaseDuration)
            when (currentPhase) {
                GroundingPhase.INHALE -> currentPhase = GroundingPhase.HOLD
                GroundingPhase.HOLD -> currentPhase = GroundingPhase.EXHALE
                GroundingPhase.EXHALE -> {
                    currentPhase = GroundingPhase.PAUSE
                    cyclesCompleted++
                    if (cyclesCompleted >= totalCycles) {
                        onComplete()
                    }
                }
                GroundingPhase.PAUSE -> currentPhase = GroundingPhase.INHALE
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5), // Blue
                        Color(0xFF1565C0)  // Dark Blue
                    )
                )
            )
    ) {
        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Grounding Exercise",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Follow the breathing pattern to calm your mind",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Breathing Circle
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    val strokeWidth = 8.dp.toPx()
                    val radius = size.minDimension / 2 - strokeWidth / 2
                    
                    // Background circle
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    // Progress circle
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * (cyclesCompleted.toFloat() / totalCycles),
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
                
                // Breathing indicator
                Box(
                    modifier = Modifier
                        .size(120.dp * animatedSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPhase.displayText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Phase instructions
            Text(
                text = currentPhase.instruction,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress
            Text(
                text = "Cycle ${cyclesCompleted + 1} of $totalCycles",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Play/Pause button
            FloatingActionButton(
                onClick = { isPlaying = !isPlaying },
                containerColor = Color.White,
                contentColor = Color(0xFF1E88E5)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Grounding exercise phases
 */
enum class GroundingPhase(
    val displayText: String,
    val instruction: String
) {
    INHALE("Breathe In", "Slowly inhale through your nose for 4 seconds"),
    HOLD("Hold", "Hold your breath for 4 seconds"),
    EXHALE("Breathe Out", "Slowly exhale through your mouth for 4 seconds"),
    PAUSE("Rest", "Rest for 2 seconds before the next cycle")
}

/**
 * Quick grounding techniques
 */
@Composable
fun QuickGroundingTechniques(
    onTechniqueSelected: (String) -> Unit
) {
    val techniques = listOf(
        "5-4-3-2-1 Grounding",
        "Box Breathing",
        "Progressive Muscle Relaxation",
        "Mindful Observation"
    )
    
    Column {
        Text(
            text = "Quick Grounding Techniques",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        techniques.forEach { technique ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { onTechniqueSelected(technique) }
            ) {
                Text(
                    text = technique,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
