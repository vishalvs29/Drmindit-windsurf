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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.drmindit.android.ui.viewmodel.OnboardingViewModel
import com.drmindit.shared.domain.audience.AudienceType
import timber.log.Timber

/**
 * Enhanced Onboarding Screen with Audience Selection
 * Critical first step for personalized mental health experience
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedOnboardingScreen(
    onAudienceSelected: (AudienceType) -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var selectedAudience by remember { mutableStateOf<AudienceType?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep blue
                        Color(0xFF3B82F6), // Medium blue
                        Color(0xFF60A5FA)  // Light blue
                    )
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
            // Header section
            HeaderSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome message
            WelcomeSection()
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Audience selection
            AudienceSelectionSection(
                selectedAudience = selectedAudience,
                onAudienceSelected = { audience ->
                    selectedAudience = audience
                    showConfirmation = true
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action buttons
            ActionButtonsSection(
                selectedAudience = selectedAudience,
                onContinue = {
                    selectedAudience?.let { audience ->
                        onAudienceSelected(audience)
                    }
                },
                onSkip = onSkip
            )
        }
        
        // Confirmation dialog
        if (showConfirmation && selectedAudience != null) {
            AudienceConfirmationDialog(
                audience = selectedAudience!!,
                onConfirm = {
                    onAudienceSelected(selectedAudience!!)
                    showConfirmation = false
                },
                onDismiss = {
                    showConfirmation = false
                    selectedAudience = null
                }
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo/icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "DrMindit",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to DrMindit",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your Personal Mental Health Companion",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Choose Your Journey",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Select your audience type to get a personalized mental health experience tailored to your specific needs and challenges.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AudienceSelectionSection(
    selectedAudience: AudienceType?,
    onAudienceSelected: (AudienceType) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "I am a...",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Student option
        AudienceOptionCard(
            audience = AudienceType.STUDENT,
            isSelected = selectedAudience == AudienceType.STUDENT,
            onClick = { onAudienceSelected(AudienceType.STUDENT) },
            icon = Icons.Default.School,
            title = "Student",
            description = "Academic support, exam anxiety, focus improvement",
            color = Color(0xFF10B981) // Green
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Corporate option
        AudienceOptionCard(
            audience = AudienceType.CORPORATE,
            isSelected = selectedAudience == AudienceType.CORPORATE,
            onClick = { onAudienceSelected(AudienceType.CORPORATE) },
            icon = Icons.Default.Business,
            title = "Professional",
            description = "Work stress, burnout prevention, work-life balance",
            color = Color(0xFF3B82F6) // Blue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Police/Military option
        AudienceOptionCard(
            audience = AudienceType.POLICE_MILITARY,
            isSelected = selectedAudience == AudienceType.POLICE_MILITARY,
            onClick = { onAudienceSelected(AudienceType.POLICE_MILITARY) },
            icon = Icons.Default.Security,
            title = "Service Member",
            description = "Trauma-safe support, resilience, stress management",
            color = Color(0xFFEF4444) // Red
        )
    }
}

@Composable
private fun AudienceOptionCard(
    audience: AudienceType,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                color.copy(alpha = 0.3f)
            } else {
                Color.White.copy(alpha = 0.1f)
            }
        ),
        elevation = if (isSelected) CardDefaults.cardElevation(8.dp) else CardDefaults.cardElevation(2.dp)
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
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
                    lineHeight = 18.sp
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

@Composable
private fun ActionButtonsSection(
    selectedAudience: AudienceType?,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Continue button
        Button(
            onClick = onContinue,
            enabled = selectedAudience != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (selectedAudience != null) {
                Text(
                    text = "Continue as ${selectedAudience.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Select Your Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Skip button
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip for now",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You can change this later in settings",
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AudienceConfirmationDialog(
    audience: AudienceType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val audienceInfo = when (audience) {
        AudienceType.STUDENT -> Triple(
            "Student Journey",
            "Academic support, exam anxiety management, focus improvement, and study-life balance.",
            Color(0xFF10B981)
        )
        AudienceType.CORPORATE -> Triple(
            "Professional Journey",
            "Work stress management, burnout prevention, productivity enhancement, and work-life balance.",
            Color(0xFF3B82F6)
        )
        AudienceType.POLICE_MILITARY -> Triple(
            "Service Member Journey",
            "Trauma-safe support, resilience building, stress management, and emotional regulation.",
            Color(0xFFEF4444)
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Your Journey",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "You've selected the ${audienceInfo.first}.",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = audienceInfo.second,
                    color = Color.Black.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This will personalize your entire experience with tailored programs and content.",
                    color = Color.Black.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = audienceInfo.third
                )
            ) {
                Text("Start Journey", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Choose Different", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}
