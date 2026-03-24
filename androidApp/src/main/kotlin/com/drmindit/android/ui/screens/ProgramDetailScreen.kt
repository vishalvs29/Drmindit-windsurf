package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
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
import androidx.compose.ui.unit.dp
import com.drmindit.android.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programTitle: String = "21-Day Anxiety Reset",
    programDescription: String = "A comprehensive program to help you manage anxiety through daily mindfulness practices",
    currentDay: Int = 8,
    totalDays: Int = 21,
    onBack: () -> Unit = {},
    onDayClick: (Int) -> Unit = {},
    onStartProgram: () -> Unit = {}
) {
    val progress = currentDay.toFloat() / totalDays.toFloat()
    
    // Sample program days data
    val programDays = remember {
        listOf(
            ProgramDayData(1, "Introduction to Anxiety", "Understanding your anxiety", true, true),
            ProgramDayData(2, "Breathing Basics", "Learn fundamental breathing techniques", true, true),
            ProgramDayData(3, "Body Scan Meditation", "Connect with your body", true, true),
            ProgramDayData(4, "Mindful Walking", "Anxiety relief through movement", true, true),
            ProgramDayData(5, "Thought Observation", "Watch your thoughts without judgment", true, true),
            ProgramDayData(6, "Progressive Muscle Relaxation", "Release physical tension", true, true),
            ProgramDayData(7, "Loving Kindness", "Cultivate self-compassion", true, true),
            ProgramDayData(8, "Anxiety Triggers", "Identify your triggers", true, false), // Current day
            ProgramDayData(9, "Coping Strategies", "Build your toolkit", false, false),
            ProgramDayData(10, "Mindful Eating", "Anxiety and nutrition", false, false),
            ProgramDayData(11, "Sleep Hygiene", "Better sleep for less anxiety", false, false),
            ProgramDayData(12, "Social Connection", "The power of community", false, false),
            ProgramDayData(13, "Nature Therapy", "Healing through nature", false, false),
            ProgramDayData(14, "Creative Expression", "Channel emotions creatively", false, false),
            ProgramDayData(15, "Gratitude Practice", "Shift your focus", false, false),
            ProgramDayData(16, "Future Self Visualization", "Imagine anxiety-free life", false, false),
            ProgramDayData(17, "Relapse Prevention", "Maintain your progress", false, false),
            ProgramDayData(18, "Advanced Breathing", "Deeper techniques", false, false),
            ProgramDayData(19, "Mindful Communication", "Express yourself clearly", false, false),
            ProgramDayData(20, "Integration", "Bring it all together", false, false),
            ProgramDayData(21, "Graduation", "Celebrate your journey", false, false)
        )
    }

    Box(
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top Bar
            TopBar(
                title = programTitle,
                onBack = onBack,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // Program Header
                    ProgramHeader(
                        title = programTitle,
                        description = programDescription,
                        progress = progress,
                        currentDay = currentDay,
                        totalDays = totalDays,
                        onStartProgram = onStartProgram
                    )
                }

                item {
                    // Progress Overview
                    ProgressOverview(
                        currentDay = currentDay,
                        totalDays = totalDays,
                        progress = progress
                    )
                }

                item {
                    // Program Days
                    Text(
                        text = "Program Journey",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(programDays.chunked(3)) { chunk ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunk.forEach { dayData ->
                            ProgramDayCard(
                                dayData = dayData,
                                isCurrentDay = dayData.day == currentDay,
                                onClick = { onDayClick(dayData.day) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onClick = onBack)
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProgramHeader(
    title: String,
    description: String,
    progress: Float,
    currentDay: Int,
    totalDays: Int,
    onStartProgram: () -> Unit
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
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Day $currentDay of $totalDays",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (currentDay == 0) {
                PrimaryButton(
                    text = "Start Program",
                    onClick = onStartProgram
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        icon = Icons.Default.CalendarToday,
                        value = "$currentDay",
                        label = "Days Completed",
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    StatCard(
                        icon = Icons.Default.TrendingUp,
                        value = "${(progress * 100).toInt()}%",
                        label = "Progress",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressOverview(
    currentDay: Int,
    totalDays: Int,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressWithText(
                progress = progress,
                text = "$currentDay",
                subtitle = "of $totalDays days",
                modifier = Modifier.size(120.dp),
                strokeWidth = 8f,
                progressColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    progress >= 1.0f -> "🎉 Program Complete!"
                    progress >= 0.75f -> "Almost there! Keep going!"
                    progress >= 0.5f -> "Halfway through! Great progress!"
                    progress >= 0.25f -> "Good start! Stay consistent!"
                    else -> "Beginning your journey!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProgramDayCard(
    dayData: ProgramDayData,
    isCurrentDay: Boolean,
    onClick: () -> Unit
) {
    val cardColor = when {
        dayData.isCompleted -> MaterialTheme.colorScheme.primaryContainer
        isCurrentDay -> MaterialTheme.colorScheme.secondaryContainer
        dayData.isUnlocked -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        isCurrentDay -> MaterialTheme.colorScheme.secondary
        dayData.isCompleted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Card(
        onClick = if (dayData.isUnlocked) onClick else { /* Show locked message */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentDay) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = if (isCurrentDay) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = borderColor
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day Number
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            dayData.isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrentDay -> MaterialTheme.colorScheme.secondary
                            dayData.isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    dayData.isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "${dayData.day}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (dayData.isUnlocked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Day Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dayData.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (dayData.isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dayData.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dayData.isUnlocked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
            }

            // Status Icon
            if (!dayData.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            } else if (isCurrentDay) {
                Text(
                    text = "CURRENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class ProgramDayData(
    val day: Int,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean
)
