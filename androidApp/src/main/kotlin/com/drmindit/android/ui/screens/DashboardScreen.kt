package com.drmindit.android.ui.screens

<<<<<<< HEAD
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
=======
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
>>>>>>> master
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
import com.drmindit.android.ui.components.*
=======
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.DashboardViewModel
import com.drmindit.android.ui.viewmodel.CrisisViewModel
>>>>>>> master
import com.drmindit.shared.domain.model.Mood
import com.drmindit.shared.domain.model.SessionCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
<<<<<<< HEAD
    userName: String = "Sarah",
    mentalHealthScore: Float = 0.75f,
    currentStreak: Int = 7,
    totalMindfulMinutes: Int = 145,
    onSessionClick: (String) -> Unit = {},
    onMoodSelected: (Mood) -> Unit = {},
    onProgramClick: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {}
) {
    val greeting = getGreeting()
    val selectedMood = remember { mutableStateOf<Mood?>(null) }

    Column(
=======
    dashboardViewModel: DashboardViewModel = viewModel(),
    crisisViewModel: CrisisViewModel = viewModel(),
    onSessionClick: (String) -> Unit = {},
    onProgramClick: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val crisisUiState by crisisViewModel.uiState.collectAsState()
    val crisisDetectionResult by crisisViewModel.crisisDetectionResult.collectAsState()
    
    LaunchedEffect(Unit) {
        dashboardViewModel.refreshData()
    }
    
    // Show crisis modal if detected
    if (crisisUiState.showCrisisModal && crisisDetectionResult != null) {
        CrisisModal(
            isVisible = crisisUiState.showCrisisModal,
            crisisResult = crisisDetectionResult!!,
            helplines = crisisUiState.emergencyHelplines,
            onDismiss = { crisisViewModel.dismissCrisisModal() },
            onCallHelpline = { phone ->
                // Handle phone call
                crisisViewModel.dismissCrisisModal()
            },
            onStartGrounding = { crisisViewModel.resolveCrisis() },
            onResolve = { crisisViewModel.resolveCrisis() }
        )
    }
    
    // Show grounding exercise
    if (crisisUiState.showGroundingExercise) {
        GroundingExerciseScreen(
            onComplete = { crisisViewModel.completeGroundingExercise() },
            onSkip = { crisisViewModel.dismissGroundingExercise() }
        )
        return
    }
    
    Box(
>>>>>>> master
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
<<<<<<< HEAD
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        HeaderSection(greeting = greeting, userName = userName)

        // Mental Health Score Card
        MentalHealthScoreCard(score = mentalHealthScore)

        // Daily Check-in Section
        DailyCheckInSection(
            selectedMood = selectedMood.value,
            onMoodSelected = { mood ->
                selectedMood.value = mood
                onMoodSelected(mood)
            }
        )

        // Quick Stats Row
        QuickStatsRow(
            currentStreak = currentStreak,
            totalMindfulMinutes = totalMindfulMinutes
        )

        // Recommended Sessions
        RecommendedSessionsSection(
            onSessionClick = onSessionClick,
            onViewAllSessions = onViewAllSessions
        )

        // Weekly Insights
        WeeklyInsightsSection()

        // Focus Card
        FocusCard()

        // Active Programs
        ActiveProgramsSection(onProgramClick = onProgramClick)
=======
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header Section
                HeaderSection(
                    userName = uiState.user?.name ?: "User",
                    greeting = getGreeting()
                )
                
                // Crisis Detection Component
                CrisisDetectionTrigger(
                    crisisViewModel = crisisViewModel,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Mental Health Score Card
                uiState.user?.let { user ->
                    MentalHealthScoreCard(
                        score = calculateMentalHealthScore(user),
                        userName = user.name
                    )
                }
                
                // Daily Check-in Section
                DailyCheckInSection(
                    onMoodSelected = { mood ->
                        // Log mood and check for crisis
                        crisisViewModel.detectCrisis(
                            moodType = mood,
                            moodScore = getMoodScore(mood)
                        )
                    }
                )
                
                // Quick Stats Row
                QuickStatsRow(
                    currentStreak = uiState.analytics?.currentStreak ?: 0,
                    totalMindfulMinutes = uiState.analytics?.totalMindfulMinutes ?: 0
                )
                
                // Recommended Sessions
                RecommendedSessionsSection(
                    sessions = uiState.recommendedSessions,
                    onSessionClick = onSessionClick,
                    onViewAllSessions = onViewAllSessions,
                    isLoading = uiState.isLoading
                )
                
                // Weekly Insights
                uiState.analytics?.let { analytics ->
                    WeeklyInsightsSection(analytics = analytics)
                }
                
                // Focus Card
                FocusCard()
                
                // Active Programs
                ActiveProgramsSection(onProgramClick = onProgramClick)
                
                // Error display
                uiState.error?.let { error ->
                    ErrorCard(
                        error = error,
                        onDismiss = { dashboardViewModel.clearError() }
                    )
                }
                
                crisisUiState.error?.let { error ->
                    ErrorCard(
                        error = error,
                        onDismiss = { crisisViewModel.clearError() },
                        isCrisisError = true
                    )
                }
            }
        }
>>>>>>> master
    }
}

@Composable
<<<<<<< HEAD
private fun HeaderSection(greeting: String, userName: String) {
=======
private fun HeaderSection(
    greeting: String,
    userName: String
) {
>>>>>>> master
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
<<<<<<< HEAD

=======
        
>>>>>>> master
        IconButton(
            onClick = { /* Handle notifications */ },
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
<<<<<<< HEAD
private fun MentalHealthScoreCard(score: Float) {
=======
private fun MentalHealthScoreCard(
    score: Float,
    userName: String
) {
>>>>>>> master
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mental Health Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            CircularProgressWithText(
                progress = score,
                text = "${(score * 100).toInt()}",
                subtitle = "Overall Wellness",
                modifier = Modifier.size(160.dp),
                strokeWidth = 10f,
                progressColor = if (score > 0.7f) Color(0xFF4CAF50) 
                else if (score > 0.4f) Color(0xFFFF9800) 
                else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getScoreMessage(score),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DailyCheckInSection(
<<<<<<< HEAD
    selectedMood: Mood?,
=======
>>>>>>> master
    onMoodSelected: (Mood) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Daily Check-in",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(Mood.values()) { mood ->
                    MoodCard(
                        mood = mood.name.replace("_", " "),
<<<<<<< HEAD
                        isSelected = selectedMood == mood,
=======
                        isSelected = false,
>>>>>>> master
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }
        }
    }
}

@Composable
<<<<<<< HEAD
private fun QuickStatsRow(currentStreak: Int, totalMindfulMinutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StreakIndicator(
            currentStreak = currentStreak,
            modifier = Modifier.weight(1f)
        )
        
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🧘",
                    fontSize = 32.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$totalMindfulMinutes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "Mindful Minutes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun RecommendedSessionsSection(
    onSessionClick: (String) -> Unit,
    onViewAllSessions: () -> Unit
) {
    val recommendedSessions = remember {
        listOf(
            Triple("Morning Meditation", "Dr. Sarah Chen", 10, 4.8f),
            Triple("Anxiety Relief", "Prof. James Miller", 15, 4.9f),
            Triple("Sleep Better", "Dr. Emily Brown", 20, 4.7f)
        )
    }

=======
private fun RecommendedSessionsSection(
    sessions: List<com.drmindit.shared.domain.model.Session>,
    onSessionClick: (String) -> Unit,
    onViewAllSessions: () -> Unit,
    isLoading: Boolean
) {
>>>>>>> master
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended for You",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onViewAllSessions) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
<<<<<<< HEAD
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recommendedSessions.forEach { (title, instructor, duration, rating) ->
                    SessionCard(
                        title = title,
                        instructor = instructor,
                        duration = duration,
                        rating = rating,
                        imageUrl = null,
                        isFavorite = false,
                        onFavoriteClick = { /* Handle favorite */ },
                        onPlayClick = { onSessionClick(title) }
                    )
                }
=======
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sessions.take(3).forEach { session ->
                        SessionCard(
                            title = session.title,
                            instructor = session.instructor,
                            duration = session.duration,
                            rating = session.rating,
                            imageUrl = session.imageUrl,
                            isFavorite = session.isFavorite,
                            onFavoriteClick = { /* Handle favorite */ },
                            onPlayClick = { onSessionClick(session.id) }
                        )
                    }
                }
>>>>>>> master
            }
        }
    }
}

@Composable
<<<<<<< HEAD
private fun WeeklyInsightsSection() {
=======
private fun WeeklyInsightsSection(
    analytics: com.drmindit.shared.domain.model.UserAnalytics
) {
>>>>>>> master
    val weeklyData = remember { listOf(65f, 80f, 45f, 90f, 70f, 85f, 75f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Weekly Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WeeklyProgressChart(weeklyData = weeklyData)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your stress levels improved by 23% this week! Keep up the great work.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = "Focus",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Today's Focus",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Deep breathing exercises",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
            
            IconButton(
                onClick = { /* Start focus session */ },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun ActiveProgramsSection(onProgramClick: (String) -> Unit) {
    val activePrograms = remember {
        listOf(
            Triple("21-Day Anxiety Reset", "Reduce anxiety in 3 weeks", 0.6f),
            Triple("Sleep Better Program", "Improve your sleep quality", 0.3f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Active Programs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activePrograms.forEach { (title, description, progress) ->
                    ProgramCard(
                        title = title,
                        description = description,
                        duration = 21,
                        progress = progress,
                        imageUrl = "",
                        onClick = { onProgramClick(title) }
                    )
                }
            }
        }
    }
}

<<<<<<< HEAD
=======
@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit,
    isCrisisError: Boolean = false
) {
    val backgroundColor = if (isCrisisError) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.errorContainer
    val contentColor = if (isCrisisError) Color(0xFFE65100) else MaterialTheme.colorScheme.onError
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCrisisError) Icons.Default.Warning else Icons.Default.Error,
                contentDescription = "Error",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor
                )
            }
        }
    }
}

>>>>>>> master
private fun getGreeting(): String {
    val hour = LocalDateTime.now().hour
    return when (hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun getScoreMessage(score: Float): String {
    return when {
        score > 0.8f -> "Excellent! Your mental health is thriving."
        score > 0.6f -> "Good! You're maintaining healthy mental wellness."
        score > 0.4f -> "Fair. There's room for improvement."
        else -> "Needs attention. Consider seeking support."
    }
}
<<<<<<< HEAD
=======

private fun calculateMentalHealthScore(user: com.drmindit.shared.domain.model.User): Float {
    // Calculate score based on various factors
    var score = 0.5f // Base score
    
    // Adjust based on stress level
    when (user.stressLevel) {
        com.drmindit.shared.domain.model.StressLevel.LOW -> score += 0.3f
        com.drmindit.shared.domain.model.StressLevel.MEDIUM -> score += 0.1f
        com.drmindit.shared.domain.model.StressLevel.HIGH -> score -= 0.1f
        com.drmindit.shared.domain.model.StressLevel.SEVERE -> score -= 0.3f
    }
    
    return score.coerceIn(0f, 1f)
}

private fun getMoodScore(mood: Mood): Int {
    return when (mood) {
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
}
>>>>>>> master
