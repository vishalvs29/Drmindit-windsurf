package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.shared.domain.analytics.*
import com.drmindit.shared.data.repository.MoodAnalyticsRepository
import kotlinx.coroutines.launch

/**
 * Mood Analytics Dashboard
 * Comprehensive mood tracking and analytics interface
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAnalyticsDashboard(
    repository: MoodAnalyticsRepository,
    userId: String,
    onNavigateToMoodEntry: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // State management
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.WEEK) }
    var showInsights by remember { mutableStateOf(true) }
    var showRiskAlerts by remember { mutableStateOf(true) }
    
    // Data collection
    val moodAnalytics by repository.getMoodAnalytics(
        MoodAnalyticsRequest(
            userId = userId,
            period = selectedPeriod,
            includeInsights = showInsights,
            includeRiskAlerts = showRiskAlerts,
            includeTriggers = true
        )
    ).collectAsStateWithLifecycle(initial = Result.failure(Exception("Loading...")))
    
    val moodChartData by repository.getMoodChartData(userId, selectedPeriod)
        .collectAsStateWithLifecycle(initial = emptyList())
    
    val moodDistribution by repository.getMoodDistribution(userId)
        .collectAsStateWithLifecycle(initial = emptyMap())
    
    val streakData by repository.getStreakData(userId)
        .collectAsStateWithLifecycle(initial = StreakData(0, 0, emptyList(), 0f, 0))
    
    val riskAlerts by repository.getRiskAlerts(userId)
        .collectAsStateWithLifecycle(initial = emptyList())
    
    val insights by repository.generateMoodInsights(userId)
        .collectAsStateWithLifecycle(initial = emptyList())
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E8), // Soft green
                        Color(0xFFF3E5F5), // Soft purple
                        Color(0xFFE3F2FD)  // Soft blue
                    ),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            DashboardHeader(
                selectedPeriod = selectedPeriod,
                onPeriodChanged = { selectedPeriod = it },
                onNavigateToMoodEntry = onNavigateToMoodEntry
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main analytics content
            moodAnalytics.getOrNull()?.let { analytics ->
                // Mood Trend Chart
                MoodTrendChart(
                    chartData = moodChartData,
                    period = selectedPeriod,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Quick Stats Cards
                QuickStatsCards(
                    analytics = analytics,
                    streakData = streakData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Mood Distribution
                MoodDistributionChart(
                    distribution = moodDistribution,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Risk Alerts
                if (showRiskAlerts && riskAlerts.isNotEmpty()) {
                    RiskAlertsSection(
                        alerts = riskAlerts,
                        onDismissAlert = { alertId ->
                            scope.launch {
                                repository.dismissRiskAlert(alertId)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // AI Insights
                if (showInsights && insights.isNotEmpty()) {
                    AIInsightsSection(
                        insights = insights,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } ?: run {
                // Loading or error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (moodAnalytics.isFailure) {
                        Text(
                            text = "Unable to load mood analytics. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dashboard Header
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardHeader(
    selectedPeriod: AnalyticsPeriod,
    onPeriodChanged: (AnalyticsPeriod) -> Unit,
    onNavigateToMoodEntry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Mood Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Track your emotional wellbeing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period selector
            FilterChip(
                onClick = { },
                label = selectedPeriod.displayName,
                selected = true,
                colors = FilterChipDefaults.filterChipColors(
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(32.dp)
            )
            
            // Add mood entry button
            FloatingActionButton(
                onClick = onNavigateToMoodEntry,
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Mood Entry",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Mood Trend Chart
 */
@Composable
private fun MoodTrendChart(
    chartData: List<MoodChartPoint>,
    period: AnalyticsPeriod,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Mood Trend - ${period.displayName}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple line chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (chartData.isNotEmpty()) {
                    MoodLineChart(
                        data = chartData,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No mood data available for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple Mood Line Chart
 */
@Composable
private fun MoodLineChart(
    data: List<MoodChartPoint>,
    modifier: Modifier = Modifier
) {
    // This is a simplified chart implementation
    // In a real app, you'd use a proper charting library like MPAndroidChart
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 16.dp.toPx()
        
        if (data.isEmpty()) return@Canvas
        
        val xStep = (canvasWidth - 2 * padding) / (data.size - 1)
        val yMax = 5f // Maximum mood score
        val yMin = 1f // Minimum mood score
        val yRange = yMax - yMin
        
        // Draw line
        val linePath = Path().apply {
            data.forEachIndexed { index, point ->
                val x = padding + index * xStep
                val y = canvasHeight - padding - ((point.moodScore - yMin) / yRange) * (canvasHeight - 2 * padding)
                
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        drawPath(
            path = linePath,
            color = MaterialTheme.colorScheme.primary,
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw points
        data.forEachIndexed { index, point ->
            val x = padding + index * xStep
            val y = canvasHeight - padding - ((point.moodScore - yMin) / yRange) * (canvasHeight - 2 * padding)
            
            drawCircle(
                color = point.moodType.color.let { Color(android.graphics.Color.parseColor(it)) },
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/**
 * Quick Stats Cards
 */
@Composable
private fun QuickStatsCards(
    analytics: MoodAnalytics,
    streakData: StreakData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Average Mood Card
        StatCard(
            title = "Average Mood",
            value = String.format("%.1f", analytics.averageMoodScore),
            subtitle = "Out of 5",
            icon = Icons.Default.Mood,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        // Current Streak Card
        StatCard(
            title = "Current Streak",
            value = streakData.currentStreak.toString(),
            subtitle = "Days",
            icon = Icons.Default.LocalFireDepartment,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )
        
        // Total Entries Card
        StatCard(
            title = "Total Entries",
            value = analytics.totalEntries.toString(),
            subtitle = "This ${analytics.period.displayName.lowercase()}",
            icon = Icons.Default.BarChart,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual Stat Card
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Mood Distribution Chart
 */
@Composable
private fun MoodDistributionChart(
    distribution: Map<MoodType, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Mood Distribution",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (distribution.isNotEmpty()) {
                val total = distribution.values.sum()
                distribution.forEach { (mood, count) ->
                    val percentage = (count.toFloat() / total * 100)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mood color indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(android.graphics.Color.parseColor(mood.color)))
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Mood name
                        Text(
                            text = mood.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Count and percentage
                        Text(
                            text = "$count (${String.format("%.0f", percentage)}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Text(
                    text = "No mood data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Risk Alerts Section
 */
@Composable
private fun RiskAlertsSection(
    alerts: List<RiskAlert>,
    onDismissAlert: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Risk Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                if (alerts.isNotEmpty()) {
                    Badge {
                        Text(
                            text = alerts.size.toString(),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts.take(3)) { alert ->
                    RiskAlertCard(
                        alert = alert,
                        onDismiss = { onDismissAlert(alert.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Risk Alert Card
 */
@Composable
private fun RiskAlertCard(
    alert: RiskAlert,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(alert.severity.color)).copy(alpha = 0.1f),
            contentColor = Color(android.graphics.Color.parseColor(alert.severity.color))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(android.graphics.Color.parseColor(alert.severity.color)),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = alert.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * AI Insights Section
 */
@Composable
private fun AIInsightsSection(
    insights: List<MoodInsight>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "AI Insights",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(insights.take(3)) { insight ->
                    InsightCard(insight = insight)
                }
            }
        }
    }
}

/**
 * Individual Insight Card
 */
@Composable
private fun InsightCard(
    insight: MoodInsight
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(android.graphics.Color.parseColor(insight.priority.color)).copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                if (insight.actionable) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Actionable Insight",
                        tint = Color(android.graphics.Color.parseColor(insight.priority.color)),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = insight.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
