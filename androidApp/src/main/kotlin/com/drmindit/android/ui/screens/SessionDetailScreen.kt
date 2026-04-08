package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.theme.*

@Composable
fun SessionDetailScreen(sessionId: String) {
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
            SessionDetailHeader()
            
            // Hero section with background image
            SessionHeroSection()
            
            // Session information
            SessionInformation()
            
            // Benefits
            SessionBenefits()
            
            // Instructor info
            InstructorSection()
            
            // Related sessions
            RelatedSessions()
            
            // CTA section
            CTASection()
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SessionDetailHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { /* Handle back */ },
            modifier = Modifier.size(48.dp),
            backgroundColor = Color(0x1A4FD1C5),
            contentColor = Color(0xFF4FD1C5)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        
        Row {
            IconButton(
                onClick = { /* Handle favorite */ },
                modifier = Modifier.size(48.dp),
                backgroundColor = Color(0x1A4FD1C5),
                contentColor = Color(0xFF4FD1C5)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { /* Handle share */ },
                modifier = Modifier.size(48.dp),
                backgroundColor = Color(0x1A4FD1C5),
                contentColor = Color(0xFF4FD1C5)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }
    }
}

@Composable
fun SessionHeroSection() {
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
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
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
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GradientButton(
                        text = "Start Session",
                        onClick = { /* Handle session start */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun SessionInformation() {
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
                    color = Color(0xFFE2E8F0).copy(alpha = 0.9f),
                    lineHeight = 24.sp
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
                    .clip(androidx.compose.foundation.shape.CircleShape)
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
fun RelatedSessions() {
    val relatedSessions = listOf(
        RelatedSession("Morning Meditation", "10 min", Color(0xFF4FD1C5)),
        RelatedSession("Deep Sleep", "20 min", Color(0xFF667EEA)),
        RelatedSession("Stress Relief", "15 min", Color(0xFFED8936))
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
            RelatedSessionCard(session = session)
        }
    }
}

@Composable
fun RelatedSessionCard(session: RelatedSession) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle session click */ },
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
                onClick = { /* Handle play */ },
                modifier = Modifier.size(40.dp),
                backgroundColor = session.color.copy(alpha = 0.2f),
                contentColor = session.color
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CTASection() {
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
        
        GradientButton(
            text = "Start Session",
            onClick = { /* Handle session start */ },
            modifier = Modifier.fillMaxWidth()
        )
        
        SecondaryButton(
            text = "Add to Favorites",
            onClick = { /* Handle favorite */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

data class RelatedSession(
    val title: String,
    val duration: String,
    val color: Color
)
