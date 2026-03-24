package com.drmindit.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.drmindit.android.ui.components.*
import com.drmindit.shared.domain.model.AudioSession
import com.drmindit.shared.domain.model.SessionCategories
import com.drmindit.shared.domain.model.RealAudioContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLibraryScreen(
    onSessionClick: (String) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    audioSessionRepository: com.drmindit.shared.data.repository.AudioSessionRepository = hiltViewModel()
) {
    val allSessions by remember { mutableStateOf(RealAudioContent.getSampleSessions()) }
    val featuredSessions by remember { mutableStateOf(RealAudioContent.getFeaturedSessions()) }
    val sessionOfDay by remember { mutableStateOf(RealAudioContent.getSessionOfDay()) }
    val recentlyPlayed by remember { mutableStateOf(RealAudioContent.getRecentlyPlayed()) }
    val continueListening by remember { mutableStateOf(RealAudioContent.getContinueListening()) }
    
    val searchQuery by remember { mutableStateOf("") }
    val selectedCategory by remember { mutableStateOf<com.drmindit.shared.domain.model.SessionCategory?>(null) }
    
    val filteredSessions = remember(searchQuery, selectedCategory) {
        val filtered = allSessions.filter { session ->
            val matchesSearch = searchQuery.isBlank() || 
                session.title.contains(searchQuery, ignoreCase = true) ||
                session.description.contains(searchQuery, ignoreCase = true) ||
                session.instructorName.contains(searchQuery, ignoreCase = true) ||
                session.tags.any { it.contains(searchQuery, ignoreCase = true) }
            
            val matchesCategory = selectedCategory == null || session.category.id == selectedCategory.id
            
            matchesSearch && matchesCategory
        }
        filtered
    }
    
    val isLoading by remember { mutableStateOf(false) }
    val error by remember { mutableStateOf<String?>(null) }
    
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                LibraryHeader(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
            
            // Session of the Day
            item {
                SessionOfDaySection(
                    session = sessionOfDay,
                    onSessionClick = onNavigateToPlayer
                )
            }
            
            // Continue Listening
            if (continueListening.isNotEmpty()) {
                item {
                    ContinueListeningSection(
                        sessions = continueListening,
                        onSessionClick = onNavigateToPlayer
                    )
                }
            }
            
            // Recently Played
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    RecentlyPlayedSection(
                        sessions = recentlyPlayed,
                        onSessionClick = onNavigateToPlayer
                    )
                }
            }
            
            // Featured Sessions
            item {
                FeaturedSessionsSection(
                    sessions = featuredSessions,
                    onSessionClick = onNavigateToPlayer
                )
            }
            
            // All Sessions
            item {
                AllSessionsSection(
                    sessions = filteredSessions,
                    isLoading = isLoading,
                    error = error,
                    onSessionClick = onNavigateToPlayer
                )
            }
        }
    }
}

@Composable
private fun LibraryHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: com.drmindit.shared.domain.model.SessionCategory?,
    onCategorySelected: (com.drmindit.shared.domain.model.SessionCategory?) -> Unit
) {
    Column {
        // Title
        Text(
            text = "Meditation Library",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search sessions, instructors, or topics...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category Filter
        CategoryFilterRow(
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: com.drmindit.shared.domain.model.SessionCategory?,
    onCategorySelected: (com.drmindit.shared.domain.model.SessionCategory?) -> Unit
) {
    val categories = SessionCategories.getAllCategories()
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All option
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") }
            )
        }
        
        // Categories
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory?.id == category.id,
                onClick = { onCategorySelected(category) },
                label = { Text(category.name) }
            )
        }
    }
}

@Composable
private fun SessionOfDaySection(
    session: AudioSession,
    onSessionClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session of the Day",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "TODAY",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        SessionOfDayCard(
            session = session,
            onSessionClick = onSessionClick
        )
    }
}

@Composable
private fun SessionOfDayCard(
    session: AudioSession,
    onSessionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick(session.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = session.thumbnailUrl,
                contentDescription = session.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "by ${session.instructorName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = session.category.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Text(
                        text = formatDuration(session.duration),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    RatingStars(
                        rating = session.rating,
                        size = 12.dp
                    )
                }
            }
            
            IconButton(
                onClick = { onSessionClick(session.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play"
                )
            }
        }
    }
}

@Composable
private fun ContinueListeningSection(
    sessions: List<AudioSession>,
    onSessionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Continue Listening",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { session ->
                SessionCard(
                    session = session,
                    onSessionClick = onSessionClick,
                    showProgress = true,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentlyPlayedSection(
    sessions: List<AudioSession>,
    onSessionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Recently Played",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { session ->
                SessionCard(
                    session = session,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
private fun FeaturedSessionsSection(
    sessions: List<AudioSession>,
    onSessionClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Sessions",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            TextButton(
                onClick = { /* Navigate to all featured */ }
            ) {
                Text("See All")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(sessions.take(4)) { session ->
                SessionCard(
                    session = session,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun AllSessionsSection(
    sessions: List<AudioSession>,
    isLoading: Boolean,
    error: String?,
    onSessionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "All Sessions",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            sessions.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No sessions found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your search or filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(sessions) { session ->
                        SessionCard(
                            session = session,
                            onSessionClick = onSessionClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: AudioSession,
    onSessionClick: (String) -> Unit,
    showProgress: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onSessionClick(session.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = session.thumbnailUrl,
                contentDescription = session.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "by ${session.instructorName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = session.category.name,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        
                        Text(
                            text = formatDuration(session.duration),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    
                    if (session.isPremium) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (showProgress) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = 0.3f, // Placeholder progress
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
