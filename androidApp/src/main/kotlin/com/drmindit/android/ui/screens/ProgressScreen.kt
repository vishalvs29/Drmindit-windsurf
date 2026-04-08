package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.theme.*

@Composable
fun ProgressScreen() {
    // Background gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1C2C), // Deep navy
            Color(0xFF1E3A5F), // Mid blue
            Color(0xFF2D5A7B), // Lighter blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                ProgressHeader()
            }
            
            item {
                StatsOverview()
            }
            
            item {
                WeeklyMoodChart()
            }
            
            item {
                AchievementsSection()
            }
            
            item {
                RecentActivity()
            }
        }
    }
}

@Composable
fun ProgressHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Your Progress",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track your mental wellness journey",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatsOverview() {
    val stats = listOf(
        StatItem("Total Minutes", "245", Icons.Default.Timer, Color(0xFF4FD1C5)),
        StatItem("Current Streak", "7", Icons.Default.LocalFireDepartment, Color(0xFFED8936)),
        StatItem("Sessions", "18", Icons.Default.MusicNote, Color(0xFF667EEA)),
        StatItem("Avg. Duration", "14", Icons.Default.Schedule, Color(0xFF48BB78))
    )
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stats) { stat ->
                    StatCard(stat = stat)
                }
            }
        }
    }
}

@Composable
fun StatCard(stat: StatItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(stat.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                )
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeeklyMoodChart() {
    val moodData = listOf(
        DayMood("Mon", 0.7f, Color(0xFF4FD1C5)),
        DayMood("Tue", 0.8f, Color(0xFF48BB78)),
        DayMood("Wed", 0.6f, Color(0xFFED8936)),
        DayMood("Thu", 0.9f, Color(0xFF4FD1C5)),
        DayMood("Fri", 0.7f, Color(0xFF667EEA)),
        DayMood("Sat", 0.8f, Color(0xFF48BB78)),
        DayMood("Sun", 0.6f, Color(0xFF4FD1C5))
    )
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Weekly Mood",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                moodData.forEach { day ->
                    MoodBar(day = day)
                }
            }
        }
    }
}

@Composable
fun MoodBar(day: DayMood) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.day,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                modifier = Modifier.width(40.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        Color(0x1AFFFFFF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(day.value)
                        .fillMaxHeight()
                        .background(
                            day.color,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun AchievementsSection() {
    val achievements = listOf(
        Achievement("First Session", Icons.Default.MilitaryTech, true),
        Achievement("Week Streak", Icons.Default.LocalFireDepartment, true),
        Achievement("Mindful Master", Icons.Default.Psychology, false),
        Achievement("Sleep Champion", Icons.Default.Bedtime, false)
    )
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val iconColor = if (achievement.unlocked) Color(0xFF4FD1C5) else Color(0xFF718096)
    val textColor = if (achievement.unlocked) Color(0xFFE2E8F0) else Color(0xFF718096)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = achievement.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (achievement.unlocked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Unlocked",
                tint = Color(0xFF4FD1C5),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color(0xFF718096),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RecentActivity() {
    val activities = listOf(
        RecentActivityItem("Evening Meditation", "2 hours ago", "15 min"),
        RecentActivityItem("Morning Breathing", "Yesterday", "5 min"),
        RecentActivityItem("Focus Session", "2 days ago", "10 min")
    )
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities) { activity ->
                    ActivityCard(activity = activity)
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: RecentActivityItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = activity.duration,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4FD1C5),
            fontWeight = FontWeight.Medium
        )
    }
}

data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class DayMood(
    val day: String,
    val value: Float,
    val color: Color
)

data class Achievement(
    val title: String,
    val icon: ImageVector,
    val unlocked: Boolean
)

data class RecentActivityItem(
    val title: String,
    val time: String,
    val duration: String
)
