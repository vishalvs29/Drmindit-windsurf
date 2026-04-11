package com.drmindit.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.drmindit.android.ui.components.GradientButton
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 5-second mood rating dialog for session tracking
 */
@Composable
fun MoodRatingDialog(
    currentMood: Float,
    onMoodSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMood by remember { mutableStateOf(currentMood) }
    val animatedMood by animateFloatAsState(
        targetValue = selectedMood.value,
        animationSpec = tween(durationMillis = 300, easing = androidx.compose.animation.core.EaseInOutCubic),
        label = "moodAnimation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mood emoji scale
            Text(
                text = "Rate your current mood",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mood slider with emojis
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mood emojis
                listOf(
                    "😔" to 1f,
                    "😐" to 3f,
                    "🙂" to 5f,
                    "😊" to 7f,
                    "🤗" to 9f,
                    "😄" to 10f
                ).forEach { (emoji, moodValue) ->
                    val isSelected = animatedMood.roundToInt() == moodValue.roundToInt()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF4FD1C5),
                                                Color(0xFF667EEA)
                                            )
                                        )
                                    } else {
                                        Color(0xFF1E3A5F).copy(alpha = 0.3f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                style = MaterialTheme.typography.headlineMedium,
                                fontSize = 24.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = when (moodValue) {
                                1f -> "Very Low"
                                3f -> "Low"
                                5f -> "Neutral"
                                7f -> "Good"
                                9f -> "Great"
                                10f -> "Excellent"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) Color(0xFF4FD1C5) else Color(0xFFE2E8F0).copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action button
            GradientButton(
                text = "Continue",
                onClick = { onMoodSelected(selectedMood.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
