package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import com.drmindit.android.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationDashboardScreen(
    organizationName: String = "Tech Corp",
    onBack: () -> Unit = {},
    onDepartmentClick: (String) -> Unit = {},
    onExportReport: () -> Unit = {}
) {
    // Sample analytics data
    val analyticsData = remember {
        OrganizationAnalyticsData(
            totalActiveUsers = 1247,
            averageStressLevel = 3.2f,
            sleepRecoveryPercentage = 67f,
            engagementRate = 78f,
            weeklyTrends = listOf(
                WeekTrendData("Week 1", 1100, 4500, 3.8f),
                WeekTrendData("Week 2", 1180, 5200, 3.5f),
                WeekTrendData("Week 3", 1220, 5800, 3.3f),
                WeekTrendData("Week 4", 1247, 6200, 3.2f)
            ),
            departmentInsights = listOf(
                DepartmentData("Engineering", 450, 3.1f, 82f, listOf("Focus", "Stress")),
                DepartmentData("Sales", 280, 3.8f, 71f, listOf("Anxiety", "Stress")),
                DepartmentData("Marketing", 180, 3.4f, 75f, listOf("Focus", "Creativity")),
                DepartmentData("HR", 95, 2.9f, 88f, listOf("Mindfulness", "Wellness")),
                DepartmentData("Operations", 242, 3.3f, 76f, listOf("Stress", "Sleep"))
            ),
            categoryBreakdown = mapOf(
                "Stress" to 342,
                "Anxiety" to 278,
                "Focus" to 256,
                "Sleep" to 189,
                "Mindfulness" to 182
            ),
            completionStats = CompletionStatsData(
                totalProgramsStarted = 890,
                totalProgramsCompleted = 445,
                completionRate = 50f,
                averageCompletionTime = 14
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            HeaderSection(
                organizationName = organizationName,
                onBack = onBack,
                onExportReport = onExportReport
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Overview Cards
                item {
                    OverviewCards(analyticsData = analyticsData)
                }

                // Weekly Trends Chart
                item {
                    WeeklyTrendsChart(trends = analyticsData.weeklyTrends)
                }

                // Department Insights
                item {
                    DepartmentInsightsSection(
                        departments = analyticsData.departmentInsights,
                        onDepartmentClick = onDepartmentClick
                    )
                }

                // Category Breakdown
                item {
                    CategoryBreakdownSection(breakdown = analyticsData.categoryBreakdown)
                }

                // Completion Stats
                item {
                    CompletionStatsSection(stats = analyticsData.completionStats)
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    organizationName: String,
    onBack: () -> Unit,
    onExportReport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onClick = onBack)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = organizationName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Organization Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onExportReport,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Export Report",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun OverviewCards(analyticsData: OrganizationAnalyticsData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewCard(
                title = "Active Users",
                value = analyticsData.totalActiveUsers.toString(),
                subtitle = "+12% from last month",
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            OverviewCard(
                title = "Avg Stress Level",
                value = String.format("%.1f", analyticsData.averageStressLevel),
                subtitle = "Out of 10",
                icon = Icons.Default.Psychology,
                color = if (analyticsData.averageStressLevel <= 3f) Color(0xFF4CAF50) 
                else if (analyticsData.averageStressLevel <= 5f) Color(0xFFFF9800) 
                else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OverviewCard(
                title = "Sleep Recovery",
                value = "${analyticsData.sleepRecoveryPercentage.toInt()}%",
                subtitle = "Improved sleep quality",
                icon = Icons.Default.Bedtime,
                color = Color(0xFF6B73FF),
                modifier = Modifier.weight(1f)
            )
            
            OverviewCard(
                title = "Engagement",
                value = "${analyticsData.engagementRate.toInt()}%",
                subtitle = "Active participation",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF26A69A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyTrendsChart(trends: List<WeekTrendData>) {
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
                text = "Weekly Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Combined Chart
            Column {
                // Active Users Trend
                TrendChart(
                    title = "Active Users",
                    data = trends.map { it.activeUsers },
                    maxValue = trends.maxOf { it.activeUsers } + 100,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Stress Level Trend
                TrendChart(
                    title = "Average Stress Level",
                    data = trends.map { (it.stressLevel * 100).toInt() },
                    maxValue = 500,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun TrendChart(
    title: String,
    data: List<Int>,
    maxValue: Int,
    color: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val barHeight = (value.toFloat() / maxValue) * 100.dp
                    
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(color)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "W${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DepartmentInsightsSection(
    departments: List<DepartmentData>,
    onDepartmentClick: (String) -> Unit
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
                text = "Department Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                departments.forEach { department ->
                    DepartmentCard(
                        department = department,
                        onClick = { onDepartmentClick(department.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DepartmentCard(
    department: DepartmentData,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = department.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${department.activeUsers} active users",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(department.topCategories) { category ->
                        ChipButton(
                            text = category,
                            onClick = { /* Handle category click */ },
                            selected = false
                        )
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format("%.1f", department.averageStressLevel),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (department.averageStressLevel <= 3f) Color(0xFF4CAF50) 
                    else if (department.averageStressLevel <= 5f) Color(0xFFFF9800) 
                    else Color(0xFFF44336)
                )
                
                Text(
                    text = "Stress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${department.engagementRate.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Engaged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(breakdown: Map<String, Int>) {
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
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                breakdown.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                    CategoryBreakdownItem(
                        category = category,
                        count = count,
                        total = breakdown.values.sum()
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownItem(
    category: String,
    count: Int,
    total: Int
) {
    val percentage = (count.toFloat() / total) * 100
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "$count sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = getCategoryColor(category),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CompletionStatsSection(stats: CompletionStatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Program Completion Stats",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompletionStatItem(
                    value = stats.totalProgramsStarted.toString(),
                    label = "Programs Started",
                    color = MaterialTheme.colorScheme.primary
                )
                
                CompletionStatItem(
                    value = stats.totalProgramsCompleted.toString(),
                    label = "Programs Completed",
                    color = Color(0xFF4CAF50)
                )
                
                CompletionStatItem(
                    value = "${stats.completionRate.toInt()}%",
                    label = "Completion Rate",
                    color = Color(0xFFFF9800)
                )
                
                CompletionStatItem(
                    value = "${stats.averageCompletionTime} days",
                    label = "Avg. Time",
                    color = Color(0xFF6B73FF)
                )
            }
        }
    }
}

@Composable
private fun CompletionStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "stress" -> Color(0xFFFF9800)
        "anxiety" -> Color(0xFF7E57C2)
        "focus" -> Color(0xFF42A5F5)
        "sleep" -> Color(0xFF6B73FF)
        "mindfulness" -> Color(0xFF66BB6A)
        else -> MaterialTheme.colorScheme.primary
    }
}

// Data classes for organization analytics
private data class OrganizationAnalyticsData(
    val totalActiveUsers: Int,
    val averageStressLevel: Float,
    val sleepRecoveryPercentage: Float,
    val engagementRate: Float,
    val weeklyTrends: List<WeekTrendData>,
    val departmentInsights: List<DepartmentData>,
    val categoryBreakdown: Map<String, Int>,
    val completionStats: CompletionStatsData
)

private data class WeekTrendData(
    val week: String,
    val activeUsers: Int,
    val totalMinutes: Int,
    val stressLevel: Float
)

private data class DepartmentData(
    val name: String,
    val activeUsers: Int,
    val averageStressLevel: Float,
    val engagementRate: Float,
    val topCategories: List<String>
)

private data class CompletionStatsData(
    val totalProgramsStarted: Int,
    val totalProgramsCompleted: Int,
    val completionRate: Float,
    val averageCompletionTime: Int
)
