package com.drmindit.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.drmindit.shared.domain.model.Mood
import com.drmindit.shared.domain.model.SessionCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
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
private fun HeaderSection(greeting: String, userName: String) {
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
