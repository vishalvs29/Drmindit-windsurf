package com.drmindit.android.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.notifications.NotificationManager
import com.drmindit.android.notifications.NotificationPreferences

/**
 * Notification Preferences Screen
 * Allows users to control all notification settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    notificationManager: NotificationManager,
    onNavigateBack: () -> Unit
) {
    val preferences by notificationManager.notificationPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showTimePicker by remember { mutableStateOf(false) }
    var showQuietHoursPicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Load current preferences
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.foundation.background.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8), // Soft blue
                        Color(0xFF2E7D32)  // Gentle purple
                    ),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            NotificationPreferencesHeader(onNavigateBack)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Preferences Content
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Notification Channels
                    item {
                        NotificationChannelSection(
                            title = "Notification Channels",
                            preferences = preferences,
                            onPreferenceChanged = { newPreferences ->
                                notificationManager.updateNotificationPreferences(newPreferences)
                            }
                        )
                    }
                    
                    // Reminder Settings
                    item {
                        ReminderSettingsSection(
                            title = "Reminder Settings",
                            preferences = preferences,
                            onPreferenceChanged = { newPreferences ->
                                notificationManager.updateNotificationPreferences(newPreferences)
                            },
                            onTimePickerClick = { showTimePicker = true },
                            onQuietHoursClick = { showQuietHoursPicker = true }
                        )
                    }
                    
                    // Third-Party Integrations
                    item {
                        ThirdPartyIntegrationsSection(
                            title = "Third-Party Integrations",
                            preferences = preferences,
                            onPreferenceChanged = { newPreferences ->
                                notificationManager.updateNotificationPreferences(newPreferences)
                            }
                        )
                    }
                    
                    // Advanced Settings
                    item {
                        AdvancedSettingsSection(
                            title = "Advanced Settings",
                            preferences = preferences,
                            onPreferenceChanged = { newPreferences ->
                                notificationManager.updateNotificationPreferences(newPreferences)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = preferences.reminderTime,
            onTimeSelected = { time ->
                notificationManager.updateNotificationPreferences(
                    preferences.copy(reminderTime = time)
                )
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Quiet Hours Picker Dialog
    if (showQuietHoursPicker) {
        QuietHoursPickerDialog(
            initialStart = preferences.quietHoursStart,
            initialEnd = preferences.quietHoursEnd,
            enabled = preferences.quietHours,
            onHoursSelected = { enabled, start, end ->
                notificationManager.updateNotificationPreferences(
                    preferences.copy(
                        quietHours = enabled,
                        quietHoursStart = start,
                        quietHoursEnd = end
                    )
                )
                showQuietHoursPicker = false
            },
            onDismiss = { showQuietHoursPicker = false }
        )
    }
}

/**
 * Notification channel preferences section
 */
@Composable
private fun NotificationChannelSection(
    title: String,
    preferences: NotificationPreferences,
    onPreferenceChanged: (NotificationPreferences) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // In-App Notifications
        PreferenceSwitch(
            title = "In-App Notifications",
            subtitle = "Show notifications within the app",
            icon = Icons.Default.Notifications,
            checked = preferences.inAppNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(inAppNotifications = checked))
            }
        )
        
        // Push Notifications
        PreferenceSwitch(
            title = "Push Notifications",
            subtitle = "Receive notifications when app is closed",
            icon = Icons.Default.Cloud,
            checked = preferences.pushNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(pushNotifications = checked))
            }
        )
        
        // Email Notifications
        PreferenceSwitch(
            title = "Email Notifications",
            subtitle = "Receive daily summaries via email",
            icon = Icons.Default.Email,
            checked = preferences.emailNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(emailNotifications = checked))
            }
        )
    }
}

/**
 * Reminder settings section
 */
@Composable
private fun ReminderSettingsSection(
    title: String,
    preferences: NotificationPreferences,
    onPreferenceChanged: (NotificationPreferences) -> Unit,
    onTimePickerClick: () -> Unit,
    onQuietHoursClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Daily Reminders
        PreferenceSwitch(
            title = "Daily Reminders",
            subtitle = "Get daily meditation reminders",
            icon = Icons.Default.Schedule,
            checked = preferences.dailyReminders,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(dailyReminders = checked))
            }
        )
        
        // Reminder Time
        PreferenceItem(
            title = "Reminder Time",
            subtitle = preferences.reminderTime,
            icon = Icons.Default.AccessTime,
            onClick = onTimePickerClick
        )
        
        // Session Reminders
        PreferenceSwitch(
            title = "Session Reminders",
            subtitle = "Get notified before scheduled sessions",
            icon = Icons.Default.Event,
            checked = preferences.sessionReminders,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(sessionReminders = checked))
            }
        )
        
        // Streak Notifications
        PreferenceSwitch(
            title = "Streak Notifications",
            subtitle = "Celebrate your meditation milestones",
            icon = Icons.Default.EmojiEvents,
            checked = preferences.streakNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(streakNotifications = checked))
            }
        )
        
        // Progress Notifications
        PreferenceSwitch(
            title = "Progress Notifications",
            subtitle = "Get notified about your daily progress",
            icon = Icons.Default.TrendingUp,
            checked = preferences.progressNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(progressNotifications = checked))
            }
        )
        
        // Personalized Nudges
        PreferenceSwitch(
            title = "Personalized Nudges",
            subtitle = "Receive smart, context-aware reminders",
            icon = Icons.Default.Psychology,
            checked = preferences.personalizedNudges,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(personalizedNudges = checked))
            }
        )
        
        // Quiet Hours
        PreferenceSwitch(
            title = "Quiet Hours",
            subtitle = "Limit notifications during specific hours",
            icon = Icons.Default.Nightlight,
            checked = preferences.quietHours,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(quietHours = checked))
            }
        )
        
        if (preferences.quietHours) {
            PreferenceItem(
                title = "Quiet Hours Schedule",
                subtitle = "${preferences.quietHoursStart} - ${preferences.quietHoursEnd}",
                icon = Icons.Default.AccessTime,
                onClick = onQuietHoursClick
            )
        }
    }
}

/**
 * Third-party integrations section
 */
@Composable
private fun ThirdPartyIntegrationsSection(
    title: String,
    preferences: NotificationPreferences,
    onPreferenceChanged: (NotificationPreferences) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // WhatsApp Notifications
        PreferenceSwitch(
            title = "WhatsApp Notifications",
            subtitle = "Receive reminders via WhatsApp",
            icon = Icons.Default.Chat,
            checked = preferences.whatsappNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(whatsappNotifications = checked))
            }
        )
        
        // Telegram Notifications
        PreferenceSwitch(
            title = "Telegram Notifications",
            subtitle = "Receive reminders via Telegram bot",
            icon = Icons.Default.Send,
            checked = preferences.telegramNotifications,
            onCheckedChange = { checked ->
                onPreferenceChanged(preferences.copy(telegramNotifications = checked))
            }
        )
    }
}

/**
 * Advanced settings section
 */
@Composable
private fun AdvancedSettingsSection(
    title: String,
    preferences: NotificationPreferences,
    onPreferenceChanged: (NotificationPreferences) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Test Notifications
        PreferenceItem(
            title = "Test Notifications",
            subtitle = "Send a test notification",
            icon = Icons.Default.NotificationsActive,
            onClick = {
                // This would trigger a test notification
            }
        )
        
        // Clear All Notifications
        PreferenceItem(
            title = "Clear All Notifications",
            subtitle = "Clear all notification history",
            icon = Icons.Default.Clear,
            onClick = {
                // This would clear notification history
            }
        )
        
        // Reset Preferences
        PreferenceItem(
            title = "Reset to Defaults",
            subtitle = "Reset all notification preferences",
            icon = Icons.Default.Restore,
            onClick = {
                onPreferenceChanged(NotificationPreferences())
            }
        )
    }
}

/**
 * Preference switch component
 */
@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
    }
}

/**
 * Preference item component
 */
@Composable
private fun PreferenceItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Header component
 */
@Composable
private fun NotificationPreferencesHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                ),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Text(
            text = "Notification Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(48.dp))
    }
}

/**
 * Time picker dialog
 */
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val (hour, minute) = initialTime.split(":").map { it.toInt() }
    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Reminder Time",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select time for daily reminders",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Time picker UI would go here
                // For now, using simple text representation
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Quiet hours picker dialog
 */
@Composable
private fun QuietHoursPickerDialog(
    initialStart: String,
    initialEnd: String,
    enabled: Boolean,
    onHoursSelected: (Boolean, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var quietHoursEnabled by remember { mutableStateOf(enabled) }
    var startTime by remember { mutableStateOf(initialStart) }
    var endTime by remember { mutableStateOf(initialEnd) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Quiet Hours",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Switch(
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it },
                    label = { Text("Enable Quiet Hours") }
                )
                
                if (quietHoursEnabled) {
                    Text(
                        text = "Notifications will be silenced during these hours",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    // Time pickers would go here
                    Text(
                        text = "Start: $startTime - End: $endTime",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onHoursSelected(quietHoursEnabled, startTime, endTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
