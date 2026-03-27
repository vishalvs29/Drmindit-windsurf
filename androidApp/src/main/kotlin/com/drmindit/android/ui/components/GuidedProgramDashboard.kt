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
import com.drmindit.android.ui.viewmodel.GuidedProgramViewModel
import com.drmindit.shared.domain.program.ProgramState
import com.drmindit.shared.domain.program.UserProgramProgress
import timber.log.Timber

/**
 * Guided Program Dashboard - Main Screen Replacing Chat
 * Shows structured programs with day-by-day progression
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedProgramDashboard(
    onStartProgram: (String) -> Unit,
    onContinueProgram: () -> Unit,
    onViewProgress: () -> Unit,
    onSettings: () -> Unit,
    viewModel: GuidedProgramViewModel = hiltViewModel()
) {
    // Collect state
    val activeProgram by viewModel.activeProgram.collectAsStateWithLifecycle()
    val currentDay by viewModel.currentDay.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val programState by viewModel.programState.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val availablePrograms by viewModel.availablePrograms.collectAsStateWithLifecycle()
    
    // Load available programs
    LaunchedEffect(Unit) {
        Timber.d("🎯 Loading guided programs dashboard")
        viewModel.loadAvailablePrograms()
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
                DashboardHeader(onSettings = onSettings)
            }
            
            // Active program section
            if (activeProgram != null && programState != ProgramState.COMPLETED) {
                item {
                    ActiveProgramSection(
                        program = activeProgram!!,
                        currentDay = currentDay,
                        currentStep = currentStep,
                        userProgress = userProgress,
                        onContinue = onContinueProgram,
                        onViewProgress = onViewProgress
                    )
                }
            }
            
            // Welcome section (no active program)
            if (activeProgram == null) {
                item {
                    WelcomeSection(
                        onStartProgram = onStartProgram
                    )
                }
            }
            
            // Quick actions
            item {
                QuickActionsSection(
                    hasActiveProgram = activeProgram != null,
                    onContinue = onContinueProgram,
                    onViewProgress = onViewProgress,
                    onStartNew = { /* Show program selection */ }
                )
            }
            
            // Available programs
            if (availablePrograms.isNotEmpty()) {
                item {
                    AvailableProgramsSection(
                        programs = availablePrograms,
                        onStartProgram = onStartProgram,
                        hasActiveProgram = activeProgram != null
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
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Your Mental Health Journey",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Structured programs for lasting change",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
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
    program: com.drmindit.shared.domain.program.GuidedProgram,
    currentDay: com.drmindit.shared.domain.program.ProgramDay?,
    currentStep: com.drmindit.shared.domain.program.ProgramStep?,
    userProgress: UserProgramProgress?,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit
) {
    val progress = userProgress?.let { progress ->
        (progress.completedDays.size.toFloat() / program.days.size * 100).toInt()
    } ?: 0
    
    val currentDayNumber = userProgress?.currentDay ?: 1
    val streakDays = userProgress?.streakDays ?: 0
    
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
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day $currentDayNumber of ${program.days.size}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        if (streakDays > 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "$streakDays day streak",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Active",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current day info
            currentDay?.let { day ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Today: ${day.title}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = day.description,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 16.sp
                        )
                    }
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
                        text = "Overall Progress",
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
                    text = "Continue Today's Session",
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
private fun WelcomeSection(
    onStartProgram: (String) -> Unit
) {
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
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Welcome to Your Journey",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start a structured program to build lasting mental wellness",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { /* Show program selection */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E3A8A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Choose Your Program",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    hasActiveProgram: Boolean,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit,
    onStartNew: () -> Unit
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
                description = "View journey",
                onClick = onViewProgress,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickActionCard(
                icon = Icons.Default.Explore,
                title = "Explore",
                description = "All programs",
                onClick = onStartNew,
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )
            
            QuickActionCard(
                icon = Icons.Default.Today,
                title = "Check-in",
                description = "Daily mood",
                onClick = { /* Daily check-in */ },
                color = Color(0xFF8B5CF6),
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
private fun AvailableProgramsSection(
    programs: List<com.drmindit.shared.domain.program.GuidedProgram>,
    onStartProgram: (String) -> Unit,
    hasActiveProgram: Boolean
) {
    Column {
        Text(
            text = if (hasActiveProgram) "Other Programs" else "Available Programs",
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
    program: com.drmindit.shared.domain.program.GuidedProgram,
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
        Row(
            modifier = Modifier.padding(16.dp),
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
                    text = "${program.days.size} days • ${program.difficulty.name}",
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
private fun DailyCheckInSection() {
    var moodRating by remember { mutableStateOf(3) }
    
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..5).forEach { rating ->
                    IconButton(
                        onClick = { moodRating = rating },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (rating <= moodRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (rating <= moodRating) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { /* Save check-in */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Check-in")
            }
        }
    }
}

// Helper functions
private fun getCategoryColor(category: com.drmindit.shared.domain.program.ProgramCategory): Color {
    return when (category) {
        com.drmindit.shared.domain.program.ProgramCategory.ANXIETY -> Color(0xFFEF4444)
        com.drmindit.shared.domain.program.ProgramCategory.STRESS -> Color(0xFFF59E0B)
        com.drmindit.shared.domain.program.ProgramCategory.CONFIDENCE -> Color(0xFF10B981)
        com.drmindit.shared.domain.program.ProgramCategory.SLEEP -> Color(0xFF6366F1)
        com.drmindit.shared.domain.program.ProgramCategory.FOCUS -> Color(0xFF3B82F6)
    }
}

private fun getCategoryIcon(category: com.drmindit.shared.domain.program.ProgramCategory): ImageVector {
    return when (category) {
        com.drmindit.shared.domain.program.ProgramCategory.ANXIETY -> Icons.Default.Psychology
        com.drmindit.shared.domain.program.ProgramCategory.STRESS -> Icons.Default.HeartBroken
        com.drmindit.shared.domain.program.ProgramCategory.CONFIDENCE -> Icons.Default.TrendingUp
        com.drmindit.shared.domain.program.ProgramCategory.SLEEP -> Icons.Default.Bedtime
        com.drmindit.shared.domain.program.ProgramCategory.FOCUS -> Icons.Default.CenterFocusStrong
    }
}
