package com.drmindit.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.ui.viewmodel.NotificationSettingsViewModel
import com.drmindit.shared.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.clearSaveSuccess()
        }
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
            NotificationSettingsHeader(onNavigateBack = onNavigateBack)
            
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item {
                        ChannelSettingsSection(
                            preferences = preferences,
                            onChannelToggle = viewModel::toggleChannel,
                            onChannelPreferenceChange = viewModel::updateChannelPreference
                        )
                    }
                    
                    item {
                        TopicSettingsSection(
                            preferences = preferences,
                            onTopicToggle = viewModel::toggleTopic,
                            onTopicPreferenceChange = viewModel::updateTopicPreference
                        )
                    }
                    
                    item {
                        QuietHoursSection(
                            quietHours = preferences?.quietHours,
                            onQuietHoursToggle = viewModel::toggleQuietHours,
                            onQuietHoursChange = viewModel::updateQuietHours
                        )
                    }
                    
                    item {
                        FrequencySettingsSection(
                            frequency = preferences?.frequency ?: NotificationFrequency.DAILY,
                            onFrequencyChange = viewModel::updateFrequency
                        )
                    }
                    
                    item {
                        SaveButton(
                            onClick = viewModel::savePreferences,
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
        
        // Success message
        AnimatedVisibility(
            visible = saveSuccess,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Settings saved successfully!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationSettingsHeader(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Notification Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        IconButton(
            onClick = { /* Reset to defaults */ }
        ) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Reset"
            )
        }
    }
}

@Composable
private fun ChannelSettingsSection(
    preferences: NotificationPreference?,
    onChannelToggle: (NotificationChannel) -> Unit,
    onChannelPreferenceChange: (NotificationChannel, ChannelPreference) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Notification Channels",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NotificationChannel.values().forEach { channel ->
                ChannelToggleItem(
                    channel = channel,
                    isEnabled = preferences?.isChannelEnabled(channel) ?: false,
                    channelPreference = preferences?.channels[channel],
                    onToggle = { onChannelToggle(channel) },
                    onPreferenceChange = { pref -> onChannelPreferenceChange(channel, pref) }
                )
                
                if (channel != NotificationChannel.values().last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ChannelToggleItem(
    channel: NotificationChannel,
    isEnabled: Boolean,
    channelPreference: ChannelPreference?,
    onToggle: () -> Unit,
    onPreferenceChange: (ChannelPreference) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        imageVector = getChannelIcon(channel),
                        contentDescription = channel.name,
                        tint = if (isEnabled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = getChannelDisplayName(channel),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (isEnabled) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = getChannelDescription(channel),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEnabled) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
            
            if (isEnabled && channel != NotificationChannel.IN_APP) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getPriorityDisplayName(channelPreference?.priority ?: NotificationPriority.NORMAL),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Max per day",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${channelPreference?.maxPerDay ?: 10}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Row {
                                IconButton(
                                    onClick = { 
                                        val current = channelPreference?.maxPerDay ?: 10
                                        onPreferenceChange((channelPreference ?: ChannelPreference()).copy(maxPerDay = maxOf(1, current - 1)))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { 
                                        val current = channelPreference?.maxPerDay ?: 10
                                        onPreferenceChange((channelPreference ?: ChannelPreference()).copy(maxPerDay = current + 1))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicSettingsSection(
    preferences: NotificationPreference?,
    onTopicToggle: (NotificationTopic) -> Unit,
    onTopicPreferenceChange: (NotificationTopic, TopicPreference) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Topics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val topics = listOf(
                NotificationTopic.SLEEP,
                NotificationTopic.ANXIETY,
                NotificationTopic.STRESS,
                NotificationTopic.MINDFULNESS,
                NotificationTopic.FOCUS,
                NotificationTopic.DAILY_WELLNESS
            )
            
            topics.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { topic ->
                        TopicToggleItem(
                            topic = topic,
                            isEnabled = preferences?.isTopicEnabled(topic) ?: false,
                            topicPreference = preferences?.topics[topic],
                            onToggle = { onTopicToggle(topic) },
                            onPreferenceChange = { pref -> onTopicPreferenceChange(topic, pref) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                if (row != topics.chunked(2).last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun TopicToggleItem(
    topic: NotificationTopic,
    isEnabled: Boolean,
    topicPreference: TopicPreference?,
    onToggle: () -> Unit,
    onPreferenceChange: (TopicPreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getTopicDisplayName(topic),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isEnabled) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
            
            if (isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getFrequencyDisplayName(topicPreference?.frequency ?: NotificationFrequency.DAILY),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun QuietHoursSection(
    quietHours: QuietHours?,
    onQuietHoursToggle: (Boolean) -> Unit,
    onQuietHoursChange: (QuietHours) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isEnabled = quietHours?.enabled ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (isEnabled) 
                            "Enabled: ${quietHours?.startTime} - ${quietHours?.endTime}"
                        else 
                            "Disabled",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onQuietHoursToggle(it) }
                )
            }
            
            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Time",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        OutlinedTextField(
                            value = quietHours?.startTime ?: "",
                            onValueChange = { 
                                onQuietHoursChange((quietHours ?: QuietHours()).copy(startTime = it))
                            },
                            label = { Text("HH:MM") },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "End Time",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        OutlinedTextField(
                            value = quietHours?.endTime ?: "",
                            onValueChange = { 
                                onQuietHoursChange((quietHours ?: QuietHours()).copy(endTime = it))
                            },
                            label = { Text("HH:MM") },
                            modifier = Modifier.width(120.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allow Emergency",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Switch(
                            checked = quietHours?.allowEmergency ?: false,
                            onCheckedChange = { 
                                onQuietHoursChange((quietHours ?: QuietHours()).copy(allowEmergency = it))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencySettingsSection(
    frequency: NotificationFrequency,
    onFrequencyChange: (NotificationFrequency) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Global Frequency",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NotificationFrequency.values().forEach { freq ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFrequencyChange(freq) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = frequency == freq,
                        onClick = { onFrequencyChange(freq) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = getFrequencyDisplayName(freq),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (freq != NotificationFrequency.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = "Save Settings",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// Helper functions
private fun getChannelIcon(channel: NotificationChannel): ImageVector {
    return when (channel) {
        NotificationChannel.IN_APP -> Icons.Default.Notifications
        NotificationChannel.PUSH_NOTIFICATION -> Icons.Default.NotificationsActive
        NotificationChannel.WHATSAPP -> Icons.Default.Chat
        NotificationChannel.TELEGRAM -> Icons.Default.Send
        NotificationChannel.EMAIL -> Icons.Default.Email
    }
}

private fun getChannelDisplayName(channel: NotificationChannel): String {
    return when (channel) {
        NotificationChannel.IN_APP -> "In-App Notifications"
        NotificationChannel.PUSH_NOTIFICATION -> "Push Notifications"
        NotificationChannel.WHATSAPP -> "WhatsApp"
        NotificationChannel.TELEGRAM -> "Telegram"
        NotificationChannel.EMAIL -> "Email"
    }
}

private fun getChannelDescription(channel: NotificationChannel): String {
    return when (channel) {
        NotificationChannel.IN_APP -> "Notifications shown within the app"
        NotificationChannel.PUSH_NOTIFICATION -> "Push notifications on your device"
        NotificationChannel.WHATSAPP -> "Messages sent via WhatsApp"
        NotificationChannel.TELEGRAM -> "Messages sent via Telegram bot"
        NotificationChannel.EMAIL -> "Email notifications"
    }
}

private fun getPriorityDisplayName(priority: NotificationPriority): String {
    return when (priority) {
        NotificationPriority.LOW -> "Low"
        NotificationPriority.NORMAL -> "Normal"
        NotificationPriority.HIGH -> "High"
        NotificationPriority.URGENT -> "Urgent"
    }
}

private fun getTopicDisplayName(topic: NotificationTopic): String {
    return when (topic) {
        NotificationTopic.SLEEP -> "😴 Sleep"
        NotificationTopic.ANXIETY -> "😰 Anxiety"
        NotificationTopic.STRESS -> "😤 Stress"
        NotificationTopic.MINDFULNESS -> "🧘 Mindfulness"
        NotificationTopic.FOCUS -> "🎯 Focus"
        NotificationTopic.DAILY_WELLNESS -> "🌿 Daily Wellness"
        else -> topic.name
    }
}

private fun getFrequencyDisplayName(frequency: NotificationFrequency): String {
    return when (frequency) {
        NotificationFrequency.IMMEDIATELY -> "Immediately"
        NotificationFrequency.HOURLY -> "Hourly"
        NotificationFrequency.DAILY -> "Daily"
        NotificationFrequency.WEEKLY -> "Weekly"
        NotificationFrequency.MONTHLY -> "Monthly"
        NotificationFrequency.NEVER -> "Never"
    }
}
