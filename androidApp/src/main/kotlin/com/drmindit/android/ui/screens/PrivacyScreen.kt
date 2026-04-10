package com.drmindit.android.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Privacy and data management screen showing all collected data
 * Builds trust through transparency
 */
@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(factory = viewModelFactory { UserViewModel(MockUserRepository()) })
) {
    val user by userViewModel.user.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // Delete user account
    suspend fun deleteAllData() {
        isDeleting.value = true
        try {
            val result = userViewModel.deleteAccount()
            if (result.isSuccess) {
                // Navigate to login or home screen
                onNavigateBack()
            }
        } catch (e: Exception) {
            // Show error message
            isDeleting.value = false
        }
    }
    val scrollState = rememberScrollState()
    
    // Mock data - in real app, this would come from SharedPreferences/DataStore
    val dataCategories = listOf(
        DataCategory(
            icon = Icons.Default.Person,
            title = "Personal Information",
            description = "Name, email, profile details",
            dataTypes = listOf("Name", "Email", "Profile Picture", "Date of Birth"),
            lastCollected = "2024-01-15",
            retention = "Until account deletion"
        ),
        DataCategory(
            icon = Icons.Default.Mood,
            title = "Mood & Wellness Data",
            description = "Daily mood ratings, session history, wellbeing score",
            dataTypes = listOf("Mood Ratings", "Session History", "Wellbeing Score", "Mood Trends"),
            lastCollected = "2024-01-15",
            retention = "2 years"
        ),
        DataCategory(
            icon = Icons.Default.Chat,
            title = "Chat History",
            description = "AI chat conversations, crisis detection events",
            dataTypes = listOf("Chat Messages", "Crisis Alerts", "Support Interactions"),
            lastCollected = "2024-01-15",
            retention = "1 year"
        ),
        DataCategory(
            icon = Icons.Default.Settings,
            title = "App Usage",
            description = "Session completion, time spent, feature usage",
            dataTypes = listOf("Session Duration", "Play Count", "Favorite Sessions", "Time of Day Usage"),
            lastCollected = "2024-01-15",
            retention = "6 months"
        ),
        DataCategory(
            icon = Icons.Default.Devices,
            title = "Device & Technical",
            description = "Device ID, app version, crash reports, performance data",
            dataTypes = listOf("Device ID", "App Version", "Crash Reports", "Performance Metrics"),
            lastCollected = "2024-01-15",
            retention = "1 year"
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B1C2C), // Deep navy
                        Color(0xFF1E3A5F), // Mid blue
                        Color(0xFF2D5A7B)  // Lighter blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp),
                    backgroundColor = Color(0x1A4FD1C5),
                    contentColor = Color(0xFF4FD1C5)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                }
                
                Text(
                    text = "My Data",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Trust message
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔒 Your Privacy Matters",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "We believe in complete transparency about your data. Below is everything we collect, why we collect it, and how long we keep it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                        lineHeight = 1.5.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Data protection promise
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Data protection",
                            tint = Color(0xFF4FD1C5),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Your data is encrypted and never shared with third parties without your consent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Data categories
            dataCategories.forEach { category ->
                DataCategoryCard(category = category)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete all data button
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Delete All My Data",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This will permanently delete all your personal data, mood history, chat conversations, and app usage information. This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE74C3C).copy(alpha = 0.8f),
                        lineHeight = 1.4.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SecondaryButton(
                            text = "Cancel",
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f)
                        )
                        
                        GradientButton(
                            text = if (isDeleting) "Deleting..." else "Delete All Data",
                            onClick = { 
                                showDeleteDialog = false
                                // In a real app, this would be called in a coroutine
                                // deleteAllData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isDeleting
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DataCategoryCard(
    category: DataCategory
) {
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = Color(0xFF4FD1C5),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Last updated: ${category.lastCollected}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                    }
                }
                
                Text(
                    text = "Retain for ${category.retention}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4FD1C5),
                    modifier = Modifier
                        .background(
                            Color(0xFF4FD1C5).copy(alpha = 0.2f),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                lineHeight = 1.4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Data types
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                category.dataTypes.forEach { dataType ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dataType,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE2E8F0).copy(alpha = 0.7f)
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Data type info",
                            tint = Color(0xFF4FD1C5).copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

data class DataCategory(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val dataTypes: List<String>,
    val lastCollected: String,
    val retention: String
)
