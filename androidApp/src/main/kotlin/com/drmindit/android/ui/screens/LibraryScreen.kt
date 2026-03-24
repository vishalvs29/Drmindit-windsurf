package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.drmindit.android.ui.components.*
import com.drmindit.shared.domain.model.SessionCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSessionClick: (String) -> Unit = {},
    onCategoryClick: (SessionCategory) -> Unit = {},
    onSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SessionCategory?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Sample data
    val sessionOfTheDay = remember {
        SessionData(
            id = "1",
            title = "Evening Wind Down",
            instructor = "Dr. Sarah Chen",
            duration = 15,
            rating = 4.9f,
            imageUrl = null,
            isFavorite = false,
            category = SessionCategory.SLEEP
        )
    }

    val categories = remember {
        listOf(
            CategoryData(SessionCategory.SLEEP, "Sleep", Icons.Default.Bedtime, Color(0xFF6B73FF)),
            CategoryData(SessionCategory.ANXIETY, "Anxiety", Icons.Default.Psychology, Color(0xFF7E57C2)),
            CategoryData(SessionCategory.STRESS, "Stress", Icons.Default.Spa, Color(0xFF26A69A)),
            CategoryData(SessionCategory.FOCUS, "Focus", Icons.Default.CenterFocusStrong, Color(0xFF42A5F5)),
            CategoryData(SessionCategory.DEPRESSION, "Depression", Icons.Default.HeartBroken, Color(0xFFEC407A)),
            CategoryData(SessionCategory.MINDFULNESS, "Mindfulness", Icons.Default.SelfImprovement, Color(0xFF66BB6A))
        )
    }

    val sessionsByCategory = remember {
        mapOf(
            SessionCategory.SLEEP to listOf(
                SessionData("2", "Deep Sleep Journey", "Prof. James Miller", 20, 4.8f, null, false, SessionCategory.SLEEP),
                SessionData("3", "Bedtime Stories", "Dr. Emily Brown", 25, 4.7f, null, false, SessionCategory.SLEEP),
                SessionData("4", "Sleep Meditation", "Dr. Michael Lee", 30, 4.9f, null, false, SessionCategory.SLEEP)
            ),
            SessionCategory.ANXIETY to listOf(
                SessionData("5", "Anxiety Relief", "Dr. Sarah Chen", 15, 4.9f, null, false, SessionCategory.ANXIETY),
                SessionData("6", "Calm Your Mind", "Prof. James Miller", 10, 4.8f, null, false, SessionCategory.ANXIETY),
                SessionData("7", "Breathing for Anxiety", "Dr. Emily Brown", 12, 4.7f, null, false, SessionCategory.ANXIETY)
            ),
            SessionCategory.STRESS to listOf(
                SessionData("8", "Stress Reduction", "Dr. Michael Lee", 18, 4.8f, null, false, SessionCategory.STRESS),
                SessionData("9", "Workplace Stress", "Prof. James Miller", 20, 4.9f, null, false, SessionCategory.STRESS),
                SessionData("10", "Quick Stress Relief", "Dr. Sarah Chen", 8, 4.7f, null, false, SessionCategory.STRESS)
            ),
            SessionCategory.FOCUS to listOf(
                SessionData("11", "Deep Focus", "Dr. Emily Brown", 25, 4.8f, null, false, SessionCategory.FOCUS),
                SessionData("12", "Study Session", "Prof. James Miller", 30, 4.9f, null, false, SessionCategory.FOCUS),
                SessionData("13", "Concentration Boost", "Dr. Michael Lee", 15, 4.7f, null, false, SessionCategory.FOCUS)
            )
        )
    }

    Column(
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
        // Header
        HeaderSection()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Search Bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { 
                        keyboardController?.hide()
                        onSearch(it)
                    }
                )
            }

            // Session of the Day
            item {
                SessionOfTheDayCard(
                    session = sessionOfTheDay,
                    onPlayClick = { onSessionClick(sessionOfTheDay.title) },
                    onFavoriteClick = { /* Handle favorite */ }
                )
            }

            // Categories
            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = selectedCategory == category.sessionCategory,
                            onClick = {
                                selectedCategory = category.sessionCategory
                                onCategoryClick(category.sessionCategory)
                            }
                        )
                    }
                }
            }

            // Sessions by Category
            sessionsByCategory.forEach { (category, sessions) ->
                if (selectedCategory == null || selectedCategory == category) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = category.name.replace("_", " "),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(sessions) { session ->
                        SessionCard(
                            title = session.title,
                            instructor = session.instructor,
                            duration = session.duration,
                            rating = session.rating,
                            imageUrl = session.imageUrl,
                            isFavorite = session.isFavorite,
                            onFavoriteClick = { /* Handle favorite */ },
                            onPlayClick = { onSessionClick(session.title) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Guided Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Find your perfect session",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = { /* Handle filters */ },
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filters",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search sessions, instructors...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) }
        ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

@Composable
private fun SessionOfTheDayCard(
    session: SessionData,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
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
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Session of the Day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${session.instructor} • ${session.duration} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (session.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (session.isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingStars(rating = session.rating)
                
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Play Now")
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "category_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .size(100.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color.copy(alpha = 0.2f) 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = category.color
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) category.color else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

private data class SessionData(
    val id: String,
    val title: String,
    val instructor: String,
    val duration: Int,
    val rating: Float,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val category: SessionCategory
)

private data class CategoryData(
    val sessionCategory: SessionCategory,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
