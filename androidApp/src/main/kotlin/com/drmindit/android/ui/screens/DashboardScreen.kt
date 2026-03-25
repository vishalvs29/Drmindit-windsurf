package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.DashboardViewModel
import com.drmindit.android.ui.viewmodel.CrisisViewModel
import com.drmindit.shared.domain.model.Mood
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    crisisViewModel: CrisisViewModel = viewModel(),
    onSessionClick: (String) -> Unit = {},
    onProgramClick: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {}
) {
    val greeting = getGreeting()
    val selectedMood = remember { mutableStateOf<Mood?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
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
    }
}

@Composable
private fun HeaderSection(
    greeting: String,
    userName: String
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(greeting, style = MaterialTheme.typography.headlineMedium)
            Text(userName)
        }

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
private fun MentalHealthScoreCard(score: Float) {
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
    selectedMood: Mood?,
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
                        isSelected = selectedMood == mood,
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedSessionsSection(
    sessions: List<com.drmindit.shared.domain.model.Session>,
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
            }
        }
    }
}

@Composable
private fun WeeklyInsightsSection() {
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
    Text("Focus Card")
}

@Composable
private fun ActiveProgramsSection(onProgramClick: (String) -> Unit) {
    Text("Programs")
}

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

private fun getGreeting(): String {
    val hour = LocalDateTime.now().hour
    return if (hour < 12) "Good morning" else "Good evening"
}

private fun getScoreMessage(score: Float): String {
    return when {
        score > 0.8f -> "Excellent! Your mental health is thriving."
        score > 0.6f -> "Good! You're maintaining healthy mental wellness."
        score > 0.4f -> "Fair. There's room for improvement."
        else -> "Needs attention. Consider seeking support."
    }
}
