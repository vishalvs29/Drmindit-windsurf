package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.viewmodel.CrisisViewModel
import com.drmindit.shared.domain.model.Mood

@Composable
fun CrisisDetectionTrigger(
    crisisViewModel: CrisisViewModel,
    modifier: Modifier = Modifier
) {
    var moodNotes by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var moodScore by remember { mutableStateOf<Int?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Auto-detect crisis based on mood selection
    LaunchedEffect(selectedMood) {
        selectedMood?.let { mood ->
            val score = when (mood) {
                Mood.VERY_HAPPY -> 10
                Mood.HAPPY -> 8
                Mood.NEUTRAL -> 6
                Mood.CALM -> 7
                Mood.ENERGETIC -> 9
                Mood.ANXIOUS -> 4
                Mood.SAD -> 3
                Mood.VERY_SAD -> 2
                Mood.TIRED -> 5
            }
            moodScore = score
            
            // Trigger crisis detection
            crisisViewModel.detectCrisis(
                moodScore = score,
                moodType = mood
            )
        }
    }
    
    // Auto-detect crisis based on mood notes
    LaunchedEffect(moodNotes) {
        if (moodNotes.length > 10) { // Only check after some text is entered
            crisisViewModel.detectCrisis(
                notes = moodNotes,
                moodScore = moodScore,
                moodType = selectedMood
            )
        }
    }
    
    // Auto-detect crisis based on mood score
    LaunchedEffect(moodScore) {
        moodScore?.let { score ->
            if (score <= 2) {
                crisisViewModel.detectCrisis(
                    moodScore = score,
                    moodType = selectedMood
                )
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Crisis button
                IconButton(
                    onClick = { crisisViewModel.manualCrisisTrigger() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF5252),
                                    Color(0xFFE53935)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = "Crisis Support",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mood selection
            Text(
                text = "Select your mood:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Mood.values()) { mood ->
                    MoodSelectionCard(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = { 
                            selectedMood = if (selectedMood == mood) null else mood
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mood notes
            Text(
                text = "Add notes (optional):",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = moodNotes,
                onValueChange = { moodNotes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Share how you're feeling...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        // Trigger final crisis detection
                        crisisViewModel.detectCrisis(
                            moodScore = moodScore,
                            notes = moodNotes,
                            moodType = selectedMood
                        )
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                minLines = 3,
                maxLines = 5
            )
            
            // Warning for crisis keywords
            if (containsCrisisKeywords(moodNotes)) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "We noticed some concerning words. Help is available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodSelectionCard(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> {
            when (mood) {
                Mood.VERY_SAD, Mood.SAD, Mood.ANXIOUS -> Color(0xFFFF5252)
                Mood.TIRED -> Color(0xFFFF9800)
                Mood.NEUTRAL -> Color(0xFFFFC107)
                Mood.CALM, Mood.HAPPY, Mood.VERY_HAPPY -> Color(0xFF4CAF50)
                Mood.ENERGETIC -> Color(0xFF2196F3)
            }
        }
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getMoodEmoji(mood),
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = mood.name.split("_").joinToString(" "),
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CrisisBanner(
    isVisible: Boolean,
    message: String,
    severity: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    if (!isVisible) return
    
    val backgroundColor = when (severity.lowercase()) {
        "critical" -> Color(0xFFD32F2F)
        "high" -> Color(0xFFFF5722)
        "medium" -> Color(0xFFFF9800)
        "low" -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = "Crisis Alert",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Get Help")
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
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

private fun getMoodEmoji(mood: Mood): String {
    return when (mood) {
        Mood.VERY_HAPPY -> "😄"
        Mood.HAPPY -> "😊"
        Mood.NEUTRAL -> "😐"
        Mood.CALM -> "😌"
        Mood.ENERGETIC -> "⚡"
        Mood.ANXIOUS -> "😰"
        Mood.SAD -> "😢"
        Mood.VERY_SAD -> "😭"
        Mood.TIRED -> "😴"
    }
}

private fun containsCrisisKeywords(text: String): Boolean {
    val crisisKeywords = setOf(
        "suicidal", "want to die", "kill myself", "end my life",
        "suicide", "no reason to live", "better off dead",
        "hopeless", "worthless", "burden", "give up"
    )
    
    val lowerText = text.lowercase()
    return crisisKeywords.any { keyword ->
        lowerText.contains(keyword)
    }
}
