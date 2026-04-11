package com.drmindit.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Composite wellbeing score based on multiple factors
 */
data class WellbeingScore(
    val score: Float,
    val trendDirection: Float, // -1 to 1, negative to positive
    val consistencyScore: Float, // 0 to 1
    val goalProgressScore: Float, // 0 to 1
    val components: Map<String, Float>
) {
    val level: String
        get() = when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Fair"
            score >= 20 -> "Needs Attention"
            else -> "Critical"
        }
    
    val levelColor: Color
        get() = when {
            score >= 80 -> Color(0xFF4FD1C5) // Green
            score >= 60 -> Color(0xFF48BB78) // Blue-green
            score >= 40 -> Color(0xFFED8936) // Yellow
            score >= 20 -> Color(0xFFE74C3C) // Orange
            else -> Color(0xFF718096) // Red
        }
}

/**
 * Wellbeing score display with animated progress
 */
@Composable
fun WellbeingScoreCard(
    wellbeingScore: WellbeingScore
) {
    val animatedScore by animateFloatAsState(
        targetValue = wellbeingScore.score,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.EaseInOutCubic),
        label = "scoreAnimation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E3A5F).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score level and trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wellbeing Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                    )
                    
                    Text(
                        text = "${wellbeingScore.score.toInt()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = wellbeingScore.levelColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = wellbeingScore.level,
                        style = MaterialTheme.typography.bodyMedium,
                        color = wellbeingScore.levelColor
                    )
                }
                
                // Trend indicator
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Trend",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (wellbeingScore.trendDirection > 0) {
                                Icons.Default.TrendingUp
                            } else if (wellbeingScore.trendDirection < 0) {
                                Icons.Default.TrendingDown
                            } else {
                                Icons.Default.TrendingFlat
                            },
                            contentDescription = "Trend direction",
                            tint = if (wellbeingScore.trendDirection > 0) {
                                Color(0xFF4FD1C5)
                            } else if (wellbeingScore.trendDirection < 0) {
                                Color(0xFFE74C3C)
                            } else {
                                Color(0xFFE2E8F0).copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = "${(wellbeingScore.trendDirection * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (wellbeingScore.trendDirection > 0) {
                                Color(0xFF4FD1C5)
                            } else if (wellbeingScore.trendDirection < 0) {
                                Color(0xFFE74C3C)
                            } else {
                                Color(0xFFE2E8F0).copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Component breakdown
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Components",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                wellbeingScore.components.forEach { (component, score) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = component,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Progress bar for component
                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0).copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(score / 100f)
                                    .clip(CircleShape)
                                    .background(wellbeingScore.levelColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "${score.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
