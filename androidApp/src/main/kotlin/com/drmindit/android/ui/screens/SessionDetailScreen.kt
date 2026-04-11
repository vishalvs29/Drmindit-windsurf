package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun SessionDetailScreen(
    sessionId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scrollOffset = scrollState.value.toFloat()
    
    // Background gradient with parallax effect
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1C2C), // Deep navy
            Color(0xFF1E3A5F), // Mid blue
            Color(0xFF2D5A7B), // Lighter blue
        ),
        startY = 0f,
        endY = 1000f + scrollOffset * 0.5f
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
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with back button
            SessionDetailHeader(onNavigateBack = onNavigateBack)
            
            // Hero section with background image
            SessionHeroSection(onNavigateToPlayer = onNavigateToPlayer)
            
            // Session information
            SessionInformationSection()
            
            // Benefits
            SessionBenefits()
            
            // Instructor info
            InstructorSection()
            
            // Related sessions
            RelatedSessionsSection(onNavigateToPlayer = onNavigateToPlayer)
            
            // CTA section
            CTASection(onNavigateToPlayer = onNavigateToPlayer)
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SessionDetailHeader(
    onNavigateBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0x1A4FD1C5), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack, 
                contentDescription = "Back",
                tint = Color(0xFF4FD1C5)
            )
        }
        
        Row {
            IconButton(
                onClick = { /* Handle favorite */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x1A4FD1C5), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder, 
                    contentDescription = "Favorite",
                    tint = Color(0xFF4FD1C5)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /* Handle share */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0x1A4FD1C5), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share, 
                    contentDescription = "Share",
                    tint = Color(0xFF4FD1C5)
                )
            }
        }
    }
}

@Composable
fun SessionHeroSection(onNavigateToPlayer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 20.dp)
    ) {
        // Background with gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x4A4FD1C5), // Teal with opacity
                            Color(0x1A667EEA), // Purple with opacity
                            Color(0x0DFFFFFF)  // White with opacity
                        )
                    )
                )
        ) {
            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Evening Meditation",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Deep relaxation for better sleep",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoChip("Sleep", Color(0xFF667EEA))
                        InfoChip("15 min", Color(0xFF4FD1C5))
                        InfoChip("Beginner", Color(0xFF48BB78))
                    }
                }
                
                Button(
                    onClick = onNavigateToPlayer,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD1C5))
                ) {
                    Text("Start Session", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SessionInformationSection() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "About This Session",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
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
                    text = "This gentle evening meditation is designed to help you unwind from the day and prepare for restful sleep. Through guided breathing and body awareness techniques, you'll release tension and find deep relaxation.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.9f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "15 minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4FD1C5),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Difficulty",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Beginner",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF48BB78),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Sleep",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF667EEA),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionBenefits() {
    val benefits = listOf(
        "Reduce stress and anxiety",
        "Improve sleep quality",
        "Enhance mind-body connection",
        "Promote deep relaxation",
        "Calm racing thoughts"
    )
    
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Benefits",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
        )
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4FD1C5),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorSection() {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Instructor avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4FD1C5),
                                Color(0xFF667EEA)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SJ",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Sarah Johnson",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Certified Meditation Instructor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                )
                Text(
                    text = "10+ years experience",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4FD1C5),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RelatedSessionsSection(onNavigateToPlayer: () -> Unit) {
    val relatedSessions = listOf(
        RelatedSessionDetail("Morning Meditation", "10 min", Color(0xFF4FD1C5)),
        RelatedSessionDetail("Deep Sleep", "20 min", Color(0xFF667EEA)),
        RelatedSessionDetail("Stress Relief", "15 min", Color(0xFFED8936))
    )
    
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Related Sessions",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
        )
        
        relatedSessions.forEach { session ->
            RelatedSessionDetailCard(session = session, onNavigateToPlayer = onNavigateToPlayer)
        }
    }
}

@Composable
fun RelatedSessionDetailCard(session: RelatedSessionDetail, onNavigateToPlayer: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPlayer() },
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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = session.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = session.color,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = onNavigateToPlayer,
                modifier = Modifier
                    .size(40.dp)
                    .background(session.color.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, 
                    contentDescription = "Play",
                    tint = session.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CTASection(onNavigateToPlayer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ready to begin your journey?",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.SemiBold
        )
        
        Button(
            onClick = onNavigateToPlayer,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD1C5))
        ) {
            Text("Start Session", color = Color.White)
        }
        
        OutlinedButton(
            onClick = { /* Handle favorite */ },
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4FD1C5))
        ) {
            Text("Add to Favorites", color = Color(0xFF4FD1C5))
        }
    }
}

data class RelatedSessionDetail(
    val title: String,
    val duration: String,
    val color: Color
)
