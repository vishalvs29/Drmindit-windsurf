package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.shared.domain.audience.AudienceType

/**
 * Audience Selection Screen
 * First step in onboarding - user selects their audience type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudienceSelectionScreen(
    onAudienceSelected: (AudienceType) -> Unit,
    onSkipSelection: () -> Unit
) {
    var selectedAudience by remember { mutableStateOf<AudienceType?>(null) }
    var showSkipDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1), // Indigo
                        Color(0xFF8B5CF6), // Purple
                        Color(0xFFEC4899)  // Pink
                    ),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Audience Selection",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome to DrMindit",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select your audience type to personalize your experience",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
            
            // Audience Options
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                AudienceOptionCard(
                    audience = AudienceType.STUDENT,
                    isSelected = selectedAudience == AudienceType.STUDENT,
                    onSelect = { selectedAudience = AudienceType.STUDENT },
                    icon = Icons.Default.School,
                    title = "Student",
                    description = "Academic success, exam anxiety, focus improvement",
                    color = Color(0xFF3B82F6) // Blue
                )
                
                AudienceOptionCard(
                    audience = AudienceType.CORPORATE,
                    isSelected = selectedAudience == AudienceType.CORPORATE,
                    onSelect = { selectedAudience = AudienceType.CORPORATE },
                    icon = Icons.Default.Business,
                    title = "Corporate",
                    description = "Workplace wellness, burnout prevention, stress management",
                    color = Color(0xFF10B981) // Green
                )
                
                AudienceOptionCard(
                    audience = AudienceType.POLICE_MILITARY,
                    isSelected = selectedAudience == AudienceType.POLICE_MILITARY,
                    onSelect = { selectedAudience = AudienceType.POLICE_MILITARY },
                    icon = Icons.Default.Security,
                    title = "Police/Military",
                    description = "Trauma-safe support, resilience training, stress recovery",
                    color = Color(0xFF6366F1) // Indigo
                )
            }
            
            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Button(
                    onClick = {
                        selectedAudience?.let { audience ->
                            onAudienceSelected(audience)
                        }
                    },
                    enabled = selectedAudience != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = { showSkipDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip for now",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
    
    // Skip Confirmation Dialog
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = {
                Text(
                    text = "Skip Audience Selection?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Personalizing your experience helps us provide the most relevant content. Are you sure you want to skip?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSkipDialog = false
                        onSkipSelection()
                    }
                ) {
                    Text("Skip")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSkipDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual audience option card
 */
@Composable
private fun AudienceOptionCard(
    audience: AudienceType,
    isSelected: Boolean,
    onSelect: () -> Unit,
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                color.copy(alpha = 0.2f)
            } else {
                Color.White.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                color
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Audience confirmation screen
 */
@Composable
fun AudienceConfirmationScreen(
    audience: AudienceType,
    onConfirm: () -> Unit,
    onChangeAudience: () -> Unit
) {
    val audienceInfo = getAudienceInfo(audience)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        audienceInfo.primaryColor,
                        audienceInfo.secondaryColor
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Great choice!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "You've selected ${audience.displayName}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Benefits
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "What you'll get:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    audienceInfo.benefits.forEach { benefit ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Benefit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = benefit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = audienceInfo.primaryColor
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = onChangeAudience,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Change audience",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Audience information data
 */
private data class AudienceInfo(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val benefits: List<String>
)

private fun getAudienceInfo(audience: AudienceType): AudienceInfo {
    return when (audience) {
        AudienceType.STUDENT -> AudienceInfo(
            displayName = "Student",
            primaryColor = Color(0xFF3B82F6),
            secondaryColor = Color(0xFF1E40AF),
            benefits = listOf(
                "Exam anxiety relief programs",
                "Focus improvement techniques",
                "Study habit optimization",
                "Confidence building exercises",
                "Academic stress management"
            )
        )
        
        AudienceType.CORPORATE -> AudienceInfo(
            displayName = "Corporate",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF047857),
            benefits = listOf(
                "Burnout prevention programs",
                "Workplace stress management",
                "Work-life balance techniques",
                "Productivity optimization",
                "Professional resilience building"
            )
        )
        
        AudienceType.POLICE_MILITARY -> AudienceInfo(
            displayName = "Police/Military",
            primaryColor = Color(0xFF6366F1),
            secondaryColor = Color(0xFF4338CA),
            benefits = listOf(
                "Trauma-safe support programs",
                "Stress recovery techniques",
                "Resilience training exercises",
                "Sleep improvement programs",
                "Emotional regulation tools"
            )
        )
    }
}
