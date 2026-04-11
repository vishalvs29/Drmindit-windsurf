package com.drmindit.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*

@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSession: (String) -> Unit = {},
    onNavigateToPlayer: () -> Unit = {}
) {
    val searchQuery = remember { mutableStateOf("") }
    val selectedCategory = remember { mutableStateOf("All") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
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
                ExploreHeader()
            }
            
            item {
                SearchBar(
                    query = searchQuery.value,
                    onQueryChange = { searchQuery.value = it },
                    onSearch = { keyboardController?.hide() }
                )
            }
            
            item {
                CategoryFilter(
                    selectedCategory = selectedCategory.value,
                    onCategorySelected = { selectedCategory.value = it }
                )
            }
            
            item {
                FeaturedSessions(onNavigateToPlayer = onNavigateToPlayer)
            }
            
            item {
                PopularSessionsSection(onNavigateToPlayer = onNavigateToPlayer)
            }
            
            item {
                RecommendedSessionsSection(onNavigateToPlayer = onNavigateToPlayer)
            }
        }
    }
}

@Composable
fun ExploreHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Explore",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Discover new meditation and wellness sessions",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 50.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search sessions...",
                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF4FD1C5)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFFE2E8F0),
                focusedTextColor = Color(0xFFE2E8F0),
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF4FD1C5)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            singleLine = true
        )
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "All", "Mindfulness", "Sleep", "Anxiety", "Focus", "Stress", "Breathing"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                label = category,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
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
    
    val textColor = if (isSelected) {
        Color(0xFF4FD1C5) // Teal
    } else {
        Color(0xFFE2E8F0).copy(alpha = 0.7f) // Light gray
    }

    GlassCard(
        modifier = Modifier
            .clickable(onClick = onClick),
        cornerRadius = 50.dp,
        backgroundColor = backgroundColor,
        borderColor = borderColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FeaturedSessions(onNavigateToPlayer: () -> Unit) {
    val featuredSessions = listOf(
        ExploreSession(
            title = "Deep Sleep Journey",
            description = "Fall asleep peacefully with this guided meditation",
            duration = "30 min",
            category = "Sleep",
            color = Color(0xFF667EEA),
            imageRes = "sleep_journey"
        ),
        ExploreSession(
            title = "Morning Mindfulness",
            description = "Start your day with clarity and focus",
            duration = "10 min",
            category = "Mindfulness",
            color = Color(0xFF4FD1C5),
            imageRes = "morning_mindfulness"
        ),
        ExploreSession(
            title = "Anxiety Relief",
            description = "Calm your mind and reduce stress",
            duration = "15 min",
            category = "Anxiety",
            color = Color(0xFFED8936),
            imageRes = "anxiety_relief"
        )
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Featured",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(featuredSessions) { session ->
                FeaturedExploreSessionCard(session = session, onNavigateToPlayer = onNavigateToPlayer)
            }
        }
    }
}

@Composable
fun FeaturedExploreSessionCard(session: ExploreSession, onNavigateToPlayer: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .width(200.dp)
            .height(250.dp),
        cornerRadius = 20.dp,
        backgroundColor = session.color.copy(alpha = 0.1f),
        borderColor = session.color.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                
                Text(
                    text = session.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                    maxLines = 3
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExploreInfoChip(session.category, session.color)
                    ExploreInfoChip(session.duration, Color(0xFF4FD1C5))
                }
            }
            
            Button(
                onClick = onNavigateToPlayer,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = session.color)
            ) {
                Text("Start", color = Color.White)
            }
        }
    }
}

@Composable
fun ExploreSessionCard(session: ExploreSession, onNavigateToPlayer: () -> Unit) {
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Text(
                    text = session.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE2E8F0).copy(alpha = 0.7f),
                    maxLines = 2
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExploreInfoChip(session.category, session.color)
                    ExploreInfoChip(session.duration, Color(0xFF4FD1C5))
                }
            }
            
            IconButton(
                onClick = { onNavigateToPlayer() },
                modifier = Modifier
                    .size(48.dp)
                    .background(session.color.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = session.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PopularSessionsSection(onNavigateToPlayer: () -> Unit) {
    val popularSessions = listOf(
        ExploreSession(
            title = "Stress Management",
            description = "Learn to handle daily stress effectively",
            duration = "20 min",
            category = "Stress",
            color = Color(0xFFF56565),
            imageRes = "stress_management"
        ),
        ExploreSession(
            title = "Focused Breathing",
            description = "Improve concentration with breath work",
            duration = "12 min",
            category = "Focus",
            color = Color(0xFF48BB78),
            imageRes = "focused_breathing"
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Popular Right Now",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        popularSessions.forEach { session ->
            ExploreSessionCard(session = session, onNavigateToPlayer = onNavigateToPlayer)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RecommendedSessionsSection(onNavigateToPlayer: () -> Unit) {
    val recommendedSessions = listOf(
        ExploreSession(
            title = "Body Scan Meditation",
            description = "Progressive relaxation for your entire body",
            duration = "25 min",
            category = "Relaxation",
            color = Color(0xFF4FD1C5),
            imageRes = "body_scan"
        ),
        ExploreSession(
            title = "Mindful Walking",
            description = "Practice mindfulness while walking",
            duration = "15 min",
            category = "Mindfulness",
            color = Color(0xFF667EEA),
            imageRes = "mindful_walking"
        ),
        ExploreSession(
            title = "Quick Stress Break",
            description = "5-minute stress relief for busy moments",
            duration = "5 min",
            category = "Stress",
            color = Color(0xFFED8936),
            imageRes = "quick_stress_break"
        )
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Recommended for You",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
        
        recommendedSessions.forEach { session ->
            ExploreSessionCard(session = session, onNavigateToPlayer = onNavigateToPlayer)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ExploreInfoChip(
    text: String,
    color: Color
) {
    GlassCard(
        cornerRadius = 50.dp,
        backgroundColor = Color(0x0DFFFFFF),
        borderColor = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

data class ExploreSession(
    val title: String,
    val description: String,
    val duration: String,
    val category: String,
    val color: Color,
    val imageRes: String
)
