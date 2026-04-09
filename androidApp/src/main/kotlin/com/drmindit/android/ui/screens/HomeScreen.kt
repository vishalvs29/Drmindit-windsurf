package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToSession: (String) -> Unit = {},
    onNavigateToExplore: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    userViewModel: UserViewModel? = null
) {
    val userName by remember { mutableStateOf("Alex") } // TODO: Get from UserViewModel
    val scrollState = rememberScrollState()
    val scrollOffset = scrollState.value.toFloat()
    
    // Background gradient that responds to scroll
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1C2C), // Deep navy
            Color(0xFF1E3A5F), // Mid blue
            Color(0xFF2D5A7B), // Lighter blue
        ),
        startY = 0f,
        endY = 1000f + scrollOffset
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            HomeHeader()
            
            // Welcome Message
            WelcomeSection(userName = userName)
            
            // Mood Selector
            MoodSelector()
            
            // Featured Session
            FeaturedSession()
            
            // Categories
            CategoriesSection()
            
            // Recent Sessions
            RecentSessions()
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DrMindit",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF4FD1C5),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your Mental Wellness",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
        
        Row {
            IconButton(
                icon = {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                },
                onClick = { /* Handle notifications */ },
                modifier = Modifier.size(40.dp),
                backgroundColor = Color(0x1A4FD1C5),
                contentColor = Color(0xFF4FD1C5)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                },
                onClick = onNavigateToPlayer,
                modifier = Modifier.size(48.dp),
                backgroundColor = Color(0xFF4FD1C5).copy(alpha = 0.2f),
                contentColor = Color(0xFF4FD1C5)
            )
        }
    }
}

@Composable
fun WelcomeSection(
    userName: String = "Alex"
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome back, $userName",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Light
            )
            Text(
                text = "How are you feeling today?",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MoodSelector() {
    val moods = listOf(
        "Calm" to " calm",
        "Happy" to " happy",
        "Focused" to " focused",
        "Anxious" to " anxious",
        "Sad" to " sad",
        "Energetic" to " energetic"
    )
    
    var selectedMood by remember { mutableStateOf("") }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(moods) { (mood, emoji) ->
                MoodChip(
                    emoji = emoji,
                    label = mood,
                    isSelected = selectedMood == mood,
                    onClick = { selectedMood = mood }
                )
            }
        }
    }
}

@Composable
fun MoodChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        Color(0x1A4FD1C5) // Teal with opacity
    } else {
        Color(0x0DFFFFFF) // White with opacity
    }
    
    val borderColor = if (isSelected) {
        Color(0xFF4FD1C5) // Teal
    } else {
        Color(0x1AFFFFFF) // White with opacity
    }

    GlassCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        cornerRadius = 20.dp,
        backgroundColor = backgroundColor,
        borderColor = borderColor
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeaturedSession() {
    GlassCardWithGradient(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        cornerRadius = 24.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1A4FD1C5),
                            Color(0x1A667EEA),
                            Color(0x0DFFFFFF)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Evening Meditation",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Deep relaxation for better sleep",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "15 min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4FD1C5)
                        )
                        Text(
                            text = "Sleep",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF667EEA)
                        )
                    }
                }
                
                GradientButton(
                    text = "Start Session",
                    onClick = { onNavigateToPlayer() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoriesSection() {
    val categories = listOf(
        "Mindfulness" to Color(0xFF4FD1C5),
        "Sleep" to Color(0xFF667EEA),
        "Anxiety" to Color(0xFFED8936),
        "Focus" to Color(0xFF48BB78),
        "Stress" to Color(0xFFF56565),
        "Breathing" to Color(0xFF9F7AEA)
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { (category, color) ->
                CategoryChip(
                    label = category,
                    color = color,
                    onClick = { /* Handle category click */ }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        cornerRadius = 50.dp,
        backgroundColor = Color(0x0DFFFFFF),
        borderColor = color
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RecentSessions() {
    val recentSessions = listOf(
        "Morning Breathing" to "5 min",
        "Focus Meditation" to "10 min",
        "Sleep Stories" to "20 min"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Sessions",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        recentSessions.forEach { (title, duration) ->
            SessionCard(
                title = title,
                duration = duration,
                onClick = { /* Handle session click */ }
            )
        }
    }
}

@Composable
fun SessionCard(
    title: String,
    duration: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4FD1C5)
                )
            }
            
            IconButton(
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                },
                onClick = { onNavigateToPlayer() },
                modifier = Modifier.size(48.dp),
                backgroundColor = Color(0xFF4FD1C5).copy(alpha = 0.2f),
                contentColor = Color(0xFF4FD1C5)
            )
        }
    }
}
