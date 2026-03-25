package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.PrimaryButton
import com.drmindit.android.ui.components.SecondaryButton
import com.drmindit.shared.data.repository.CrisisDetectionResult
import com.drmindit.shared.data.repository.CrisisSeverity
import com.drmindit.shared.data.repository.EmergencyHelpline
import com.drmindit.shared.domain.model.Mood
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrisisModal(
    isVisible: Boolean,
    crisisResult: CrisisDetectionResult,
    helplines: List<EmergencyHelpline>,
    onDismiss: () -> Unit = {},
    onCallHelpline: (String) -> Unit = {},
    onStartGrounding: () -> Unit = {},
    onResolve: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clip(RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Crisis Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF5252),
                                        Color(0xFFE53935)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = "Crisis Alert",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Crisis Message
                    Text(
                        text = "You are not alone. Help is available right now.",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = getCrisisMessage(crisisResult.severity),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Emergency Actions
                    Text(
                        text = "Immediate Help",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Helplines
                    helplines.take(3).forEach { helpline ->
                        HelplineCard(
                            helpline = helpline,
                            onCall = { onCallHelpline(helpline.phone) },
                            onWebsite = helpline.website?.let { 
                                { uriHandler.openUri(it) } 
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Alternative Actions
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Or try these calming options:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Grounding\nExercise",
                            onClick = onStartGrounding,
                            modifier = Modifier.weight(1f)
                        )
                        
                        SecondaryButton(
                            text = "Breathing\nExercise",
                            onClick = { /* Start breathing */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Dismiss with confirmation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "I'm Safe Now",
                            onClick = onResolve,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Note: We don't provide a dismiss button for crisis mode
                        // User must take action or confirm they're safe
                    }
                }
            }
        }
    }
}

@Composable
private fun HelplineCard(
    helpline: EmergencyHelpline,
    onCall: () -> Unit,
    onWebsite: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = helpline.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = helpline.description ?: "24/7 Mental Health Support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (helpline.is24x7) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Available 24/7",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            PrimaryButton(
                text = "Call",
                onClick = onCall,
                modifier = Modifier.width(100.dp)
            )
            
            if (onWebsite != null) {
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onWebsite,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Website",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GroundingExerciseScreen(
    onComplete: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf(
        "5-4-3-2-1 Grounding: Name 5 things you see, 4 you can touch, 3 you hear, 2 you smell, 1 you taste.",
        "Deep Breathing: Inhale for 4 seconds, hold for 4, exhale for 6. Repeat 5 times.",
        "Body Scan: Focus on each part of your body from toes to head, noticing sensations without judgment.",
        "Safe Place Visualization: Imagine a place where you feel completely safe and peaceful. Explore it in detail."
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8),
                        Color(0xFFF5F5F5)
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Grounding Exercise",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Step ${currentStep + 1} of ${steps.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = steps[currentStep],
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 0) {
                        SecondaryButton(
                            text = "Previous",
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (currentStep < steps.size - 1) {
                        PrimaryButton(
                            text = "Next",
                            onClick = { currentStep++ },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        PrimaryButton(
                            text = "Complete",
                            onClick = onComplete,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TextButton(onClick = onSkip) {
            Text(
                text = "Skip Exercise",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCrisisMessage(severity: CrisisSeverity): String {
    return when (severity) {
        CrisisSeverity.CRITICAL -> "We're concerned about your safety. Please reach out for immediate support."
        CrisisSeverity.HIGH -> "It sounds like you're going through a difficult time. Support is available."
        CrisisSeverity.MEDIUM -> "Everyone needs help sometimes. You don't have to go through this alone."
        CrisisSeverity.LOW -> "Taking care of your mental health is important. Here are some resources."
    }
}
