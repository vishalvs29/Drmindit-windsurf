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
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {}
) {
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
                AnalyticsHeader()
            }
            
            item {
                FocusBreakdown()
            }
            
            item {
                SessionStats()
            }
            
            item {
                MoodTrends()
            }
            
            item {
                ProgressCharts()
            }
            
            item {
                Insights()
            }
        }
    }
}

@Composable
fun AnalyticsHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Deep insights into your mental wellness journey",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun FocusBreakdown() {
    val focusAreas = listOf(
        FocusArea("Mindfulness", 75f, Color(0xFF4FD1C5)),
        FocusArea("Sleep", 60f, Color(0xFF667EEA)),
        FocusArea("Anxiety", 45f, Color(0xFFED8936)),
        FocusArea("Focus", 80f, Color(0xFF48BB78)),
        FocusArea("Stress", 55f, Color(0xFFF56565))
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
                text = "Focus Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            focusAreas.forEach { area ->
                FocusAreaCard(area = area)
            }
        }
    }
}

@Composable
fun FocusAreaCard(area: FocusArea) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = area.name,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${area.progress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = area.color,
                fontWeight = FontWeight.SemiBold
            )
        }
        
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
                    .fillMaxWidth(area.progress / 100f)
                    .fillMaxHeight()
                    .background(
                        area.color,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                    )
            )
        }
    }
}

@Composable
fun SessionStats() {
    val stats = listOf(
        SessionStat("Total Sessions", "42", Icons.Default.MusicNote, Color(0xFF4FD1C5)),
        SessionStat("Total Minutes", "623", Icons.Default.Timer, Color(0xFF667EEA)),
        SessionStat("Avg. Session", "14.8", Icons.Default.Schedule, Color(0xFF48BB78)),
        SessionStat("Completion Rate", "87%", Icons.Default.TrendingUp, Color(0xFFED8936))
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
                text = "Session Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stats) { stat ->
                    SessionStatCard(stat = stat)
                }
            }
        }
    }
}

@Composable
fun SessionStatCard(stat: SessionStat) {
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
fun MoodTrends() {
    val moodTrends = listOf(
        MoodTrend("Calm", 65f, Color(0xFF4FD1C5), "+5%"),
        MoodTrend("Focused", 70f, Color(0xFF48BB78), "+8%"),
        MoodTrend("Anxious", 30f, Color(0xFFED8936), "-12%"),
        MoodTrend("Happy", 75f, Color(0xFFF6E05E), "+3%"),
        MoodTrend("Energetic", 60f, Color(0xFF9F7AEA), "+7%")
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
                text = "Mood Trends",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                moodTrends.forEach { trend ->
                    MoodTrendCard(trend = trend)
                }
            }
        }
    }
}

@Composable
fun MoodTrendCard(trend: MoodTrend) {
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
                    .background(trend.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = trend.name.first().toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = trend.color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column {
                Text(
                    text = trend.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${trend.value.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                )
            }
        }
        
        Text(
            text = trend.change,
            style = MaterialTheme.typography.bodySmall,
            color = if (trend.change.startsWith("+")) Color(0xFF48BB78) else Color(0xFFF56565),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProgressCharts() {
    val weeklyData = listOf(
        "Mon" to 45f,
        "Tue" to 60f,
        "Wed" to 30f,
        "Thu" to 75f,
        "Fri" to 50f,
        "Sat" to 80f,
        "Sun" to 65f
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
                text = "Weekly Progress",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            WeeklyChart(data = weeklyData)
        }
    }
}

@Composable
fun WeeklyChart(data: List<Pair<String, Float>>) {
    val maxValue = data.maxOfOrNull { it.second } ?: 100f
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEach { (day, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                    modifier = Modifier.width(40.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Color(0x1AFFFFFF),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(value / maxValue)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4FD1C5),
                                        Color(0xFF667EEA)
                                    )
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                            )
                    )
                }
                
                Text(
                    text = "${value.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
}

@Composable
fun Insights() {
    val insights = listOf(
        Insight(
            "Best Performance",
            "Your most productive sessions are in the morning",
            Icons.Default.WbSunny,
            Color(0xFFF6E05E)
        ),
        Insight(
            "Sleep Improvement",
            "Your sleep quality has improved by 23% this month",
            Icons.Default.Bedtime,
            Color(0xFF667EEA)
        ),
        Insight(
            "Consistency Goal",
            "You're 3 sessions away from your weekly goal",
            Icons.Default.TrendingUp,
            Color(0xFF48BB78)
        )
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
                text = "Insights",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                insights.forEach { insight ->
                    InsightCard(insight = insight)
                }
            }
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(insight.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = null,
                tint = insight.color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
            )
        }
    }
}

data class FocusArea(
    val name: String,
    val progress: Float,
    val color: Color
)

data class SessionStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class MoodTrend(
    val name: String,
    val value: Float,
    val color: Color,
    val change: String
)

data class Insight(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)
