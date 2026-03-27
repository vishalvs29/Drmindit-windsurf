package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.ui.viewmodel.StructuredProgramViewModel
import com.drmindit.shared.domain.audience.AudienceType
import com.drmindit.shared.domain.program.ProgramFlowState
import timber.log.Timber

/**
 * Structured Program Dashboard - Main screen replacing generic chat
 * Shows active programs, progress, and "Continue Today's Session" functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredProgramDashboard(
    audienceType: AudienceType,
    onStartProgram: (String) -> Unit,
    onContinueProgram: () -> Unit,
    onViewProgress: () -> Unit,
    onSettings: () -> Unit,
    viewModel: StructuredProgramViewModel = hiltViewModel()
) {
    // Collect state
    val currentProgram by viewModel.currentProgram.collectAsStateWithLifecycle()
    val programState by viewModel.programState.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val availablePrograms by viewModel.availablePrograms.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    
    // Load programs for audience
    LaunchedEffect(audienceType) {
        Timber.d("🎯 Loading programs for audience: $audienceType")
        viewModel.loadProgramsForAudience(audienceType)
    }
    
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                DashboardHeader(
                    audienceType = audienceType,
                    onSettings = onSettings
                )
            }
            
            // Active program section
            if (currentProgram != null) {
                item {
                    ActiveProgramSection(
                        program = currentProgram!!,
                        programState = programState,
                        userProgress = userProgress,
                        onContinue = onContinueProgram,
                        onViewProgress = onViewProgress
                    )
                }
            }
            
            // Quick actions
            item {
                QuickActionsSection(
                    hasActiveProgram = currentProgram != null,
                    onStartNew = { /* Show program selection */ },
                    onContinue = onContinueProgram,
                    onViewProgress = onViewProgress
                )
            }
            
            // Recommendations
            if (recommendations.isNotEmpty()) {
                item {
                    RecommendationsSection(
                        recommendations = recommendations,
                        onStartProgram = onStartProgram
                    )
                }
            }
            
            // Available programs
            if (availablePrograms.isNotEmpty()) {
                item {
                    AvailableProgramsSection(
                        programs = availablePrograms,
                        onStartProgram = onStartProgram
                    )
                }
            }
            
            // Daily check-in
            item {
                DailyCheckInSection()
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    audienceType: AudienceType,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${audienceType.displayName} Journey",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        
        IconButton(
            onClick = onSettings,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ActiveProgramSection(
    program: com.drmindit.shared.domain.program.StructuredProgram,
    programState: ProgramFlowState,
    userProgress: com.drmindit.shared.domain.program.UserProgramProgress?,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit
) {
    val progress = userProgress?.let { progress ->
        (progress.completedSteps.size.toFloat() / program.totalSteps.toFloat() * 100).toInt()
    } ?: 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Program title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Day ${userProgress?.completedSteps?.size ?: 0} of ${program.totalSteps}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                when (programState) {
                    ProgramFlowState.ACTIVE -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Active",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    ProgramFlowState.PAUSED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Paused",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    ProgramFlowState.COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6366F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Completed",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$progress%",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White.copy(alpha = 0.3f),
                    trackColor = Color.Transparent,
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Continue button
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E3A8A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = when (programState) {
                        ProgramFlowState.ACTIVE -> "Continue Today's Session"
                        ProgramFlowState.PAUSED -> "Resume Program"
                        ProgramFlowState.COMPLETED -> "Review Program"
                        else -> "Start Program"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // View progress button
            TextButton(
                onClick = onViewProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View Detailed Progress",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    hasActiveProgram: Boolean,
    onStartNew: () -> Unit,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (hasActiveProgram) {
            QuickActionCard(
                icon = Icons.Default.PlayArrow,
                title = "Continue",
                description = "Today's session",
                onClick = onContinue,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                icon = Icons.Default.Assessment,
                title = "Progress",
                description = "View stats",
                onClick = onViewProgress,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickActionCard(
                icon = Icons.Default.Add,
                title = "Start Program",
                description = "Begin journey",
                onClick = onStartNew,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                icon = Icons.Default.Explore,
                title = "Explore",
                description = "All programs",
                onClick = onStartNew,
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecommendationsSection(
    recommendations: List<com.drmindit.shared.domain.program.ProgramRecommendation>,
    onStartProgram: (String) -> Unit
) {
    Column {
        Text(
            text = "Recommended for You",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        recommendations.take(2).forEach { recommendation ->
            ProgramRecommendationCard(
                recommendation = recommendation,
                onStartProgram = onStartProgram
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProgramRecommendationCard(
    recommendation: com.drmindit.shared.domain.program.ProgramRecommendation,
    onStartProgram: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartProgram(recommendation.programId) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recommendation indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (recommendation.priority) {
                            com.drmindit.shared.domain.program.RecommendationPriority.HIGH -> Color(0xFFEF4444)
                            com.drmindit.shared.domain.program.RecommendationPriority.MEDIUM -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.reason,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Score: ${(recommendation.score * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AvailableProgramsSection(
    programs: List<com.drmindit.shared.domain.program.StructuredProgram>,
    onStartProgram: (String) -> Unit
) {
    Column {
        Text(
            text = "All Programs",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        programs.take(3).forEach { program ->
            ProgramCard(
                program = program,
                onStartProgram = onStartProgram
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        if (programs.size > 3) {
            TextButton(
                onClick = { /* View all programs */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View All Programs (${programs.size})",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    program: com.drmindit.shared.domain.program.StructuredProgram,
    onStartProgram: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartProgram(program.id) },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Program category icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(getCategoryColor(program.category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(program.category),
                        contentDescription = null,
                        tint = getCategoryColor(program.category),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${program.totalSteps} days • ${program.difficulty.name}",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = program.description,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DailyCheckInSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Daily Check-in",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "How are you feeling today?",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { rating ->
                    Button(
                        onClick = { /* Handle rating */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("$rating")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun getCategoryColor(category: com.drmindit.shared.domain.program.ProgramCategory): Color {
    return when (category) {
        com.drmindit.shared.domain.program.ProgramCategory.ANXIETY -> Color(0xFFEF4444)
        com.drmindit.shared.domain.program.ProgramCategory.STRESS -> Color(0xFFF59E0B)
        com.drmindit.shared.domain.program.ProgramCategory.FOCUS -> Color(0xFF3B82F6)
        com.drmindit.shared.domain.program.ProgramCategory.SLEEP -> Color(0xFF6366F1)
        com.drmindit.shared.domain.program.ProgramCategory.TRAUMA -> Color(0xFF8B5CF6)
        com.drmindit.shared.domain.program.ProgramCategory.RESILIENCE -> Color(0xFF10B981)
        com.drmindit.shared.domain.program.ProgramCategory.BURNOUT -> Color(0xFFEC4899)
    }
}

private fun getCategoryIcon(category: com.drmindit.shared.domain.program.ProgramCategory): ImageVector {
    return when (category) {
        com.drmindit.shared.domain.program.ProgramCategory.ANXIETY -> Icons.Default.Psychology
        com.drmindit.shared.domain.program.ProgramCategory.STRESS -> Icons.Default.HeartBroken
        com.drmindit.shared.domain.program.ProgramCategory.FOCUS -> Icons.Default.CenterFocusStrong
        com.drmindit.shared.domain.program.ProgramCategory.SLEEP -> Icons.Default.Bedtime
        com.drmindit.shared.domain.program.ProgramCategory.TRAUMA -> Icons.Default.Healing
        com.drmindit.shared.domain.program.ProgramCategory.RESILIENCE -> Icons.Default.FitnessCenter
        com.drmindit.shared.domain.program.ProgramCategory.BURNOUT -> Icons.Default.LocalFireDepartment
    }
}
