package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.domain.model.*
import com.drmindit.shared.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _preferences = MutableStateFlow<NotificationPreference?>(null)
    val preferences: StateFlow<NotificationPreference?> = _preferences.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = getCurrentUserId()
                val result = notificationRepository.getNotificationPreferences(userId)
                if (result.isSuccess) {
                    _preferences.value = result.getOrNull() ?: createDefaultPreferences(userId)
                } else {
                    _error.value = "Failed to load preferences"
                    _preferences.value = createDefaultPreferences(userId)
                }
            } catch (e: Exception) {
                _error.value = e.message
                _preferences.value = createDefaultPreferences(getCurrentUserId())
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleChannel(channel: NotificationChannel) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedChannels = currentPrefs.channels.toMutableMap()
        val currentChannelPref = updatedChannels[channel] ?: ChannelPreference()
        updatedChannels[channel] = currentChannelPref.copy(enabled = !currentChannelPref.enabled)
        
        val updatedPrefs = currentPrefs.copy(
            channels = updatedChannels,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun updateChannelPreference(channel: NotificationChannel, preference: ChannelPreference) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedChannels = currentPrefs.channels.toMutableMap()
        updatedChannels[channel] = preference
        
        val updatedPrefs = currentPrefs.copy(
            channels = updatedChannels,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun toggleTopic(topic: NotificationTopic) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedTopics = currentPrefs.topics.toMutableMap()
        val currentTopicPref = updatedTopics[topic] ?: TopicPreference()
        updatedTopics[topic] = currentTopicPref.copy(enabled = !currentTopicPref.enabled)
        
        val updatedPrefs = currentPrefs.copy(
            topics = updatedTopics,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun updateTopicPreference(topic: NotificationTopic, preference: TopicPreference) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedTopics = currentPrefs.topics.toMutableMap()
        updatedTopics[topic] = preference
        
        val updatedPrefs = currentPrefs.copy(
            topics = updatedTopics,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun toggleQuietHours(enabled: Boolean) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedQuietHours = currentPrefs.quietHours?.copy(enabled = enabled) 
            ?: QuietHours(enabled = enabled)
        
        val updatedPrefs = currentPrefs.copy(
            quietHours = updatedQuietHours,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun updateQuietHours(quietHours: QuietHours) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedPrefs = currentPrefs.copy(
            quietHours = quietHours,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun updateFrequency(frequency: NotificationFrequency) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedPrefs = currentPrefs.copy(
            frequency = frequency,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun savePreferences() {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = notificationRepository.updateNotificationPreferences(currentPrefs)
                if (result.isSuccess) {
                    _saveSuccess.value = true
                    // Clear success after a delay
                    kotlinx.coroutines.delay(3000)
                    _saveSuccess.value = false
                } else {
                    _error.value = "Failed to save preferences"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetToDefaults() {
        val userId = getCurrentUserId()
        val defaultPrefs = createDefaultPreferences(userId)
        _preferences.value = defaultPrefs
    }
    
    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
    
    fun clearError() {
        _error.value = null
    }
    
    // Channel management
    fun registerChannel(channel: NotificationChannel, identifier: String) {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val userChannel = UserChannel(
                    userId = userId,
                    channel = channel,
                    identifier = identifier,
                    isActive = true,
                    verified = false
                )
                
                val result = notificationRepository.registerUserChannel(userChannel)
                if (result.isSuccess) {
                    // Show success message or update UI
                } else {
                    _error.value = "Failed to register ${channel.name}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun verifyChannel(channel: NotificationChannel) {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val result = notificationRepository.verifyChannel(userId, channel)
                if (result.isSuccess) {
                    // Show verification status
                } else {
                    _error.value = "Failed to verify ${channel.name}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deleteChannel(channel: NotificationChannel) {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val result = notificationRepository.deleteUserChannel(userId, channel)
                if (result.isSuccess) {
                    // Update preferences to reflect channel removal
                    val currentPrefs = _preferences.value ?: return
                    val updatedChannels = currentPrefs.channels.toMutableMap()
                    updatedChannels.remove(channel)
                    
                    val updatedPrefs = currentPrefs.copy(
                        channels = updatedChannels,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    _preferences.value = updatedPrefs
                } else {
                    _error.value = "Failed to delete ${channel.name}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // Test notifications
    fun sendTestNotification(channel: NotificationChannel) {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val testNotification = Notification(
                    id = "test_${System.currentTimeMillis()}",
                    userId = userId,
                    type = NotificationType.DAILY_REMINDER,
                    title = "Test Notification",
                    body = "This is a test notification from DrMindit!",
                    channels = setOf(channel),
                    priority = NotificationPriority.NORMAL
                )
                
                val result = notificationRepository.sendNotification(testNotification)
                if (result.isSuccess) {
                    // Show success message
                } else {
                    _error.value = "Failed to send test notification"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // Analytics and insights
    fun getNotificationHistory() {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val result = notificationRepository.getNotificationHistory(userId, 20)
                if (result.isSuccess) {
                    // Handle notification history
                } else {
                    _error.value = "Failed to load notification history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun getNotificationAnalytics(startDate: Long, endDate: Long) {
        val userId = getCurrentUserId()
        
        viewModelScope.launch {
            try {
                val result = notificationRepository.getNotificationAnalytics(userId, startDate, endDate)
                if (result.isSuccess) {
                    // Handle analytics data
                } else {
                    _error.value = "Failed to load analytics"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    // Advanced settings
    fun updateLanguage(language: String) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedPrefs = currentPrefs.copy(
            language = language,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    fun updateTimezone(timezone: String) {
        val currentPrefs = _preferences.value ?: return
        val userId = getCurrentUserId()
        
        val updatedPrefs = currentPrefs.copy(
            timezone = timezone,
            updatedAt = System.currentTimeMillis()
        )
        
        _preferences.value = updatedPrefs
    }
    
    // Helper functions
    private fun getCurrentUserId(): String {
        // In a real app, this would come from authentication service
        return "current_user_id"
    }
    
    private fun createDefaultPreferences(userId: String): NotificationPreference {
        return NotificationPreference(
            userId = userId,
            channels = mapOf(
                NotificationChannel.IN_APP to ChannelPreference(enabled = true, priority = NotificationPriority.NORMAL),
                NotificationChannel.PUSH_NOTIFICATION to ChannelPreference(enabled = true, priority = NotificationPriority.NORMAL),
                NotificationChannel.WHATSAPP to ChannelPreference(enabled = false, priority = NotificationPriority.NORMAL),
                NotificationChannel.TELEGRAM to ChannelPreference(enabled = false, priority = NotificationPriority.NORMAL),
                NotificationChannel.EMAIL to ChannelPreference(enabled = true, priority = NotificationPriority.LOW)
            ),
            topics = mapOf(
                NotificationTopic.DAILY_WELLNESS to TopicPreference(enabled = true, frequency = NotificationFrequency.DAILY),
                NotificationTopic.SLEEP to TopicPreference(enabled = true, frequency = NotificationFrequency.DAILY),
                NotificationTopic.ANXIETY to TopicPreference(enabled = true, frequency = NotificationFrequency.WEEKLY),
                NotificationTopic.STRESS to TopicPreference(enabled = true, frequency = NotificationFrequency.WEEKLY),
                NotificationTopic.MINDFULNESS to TopicPreference(enabled = true, frequency = NotificationFrequency.WEEKLY),
                NotificationTopic.FOCUS to TopicPreference(enabled = false, frequency = NotificationFrequency.NEVER)
            ),
            quietHours = QuietHours(
                enabled = false,
                startTime = "22:00",
                endTime = "08:00",
                timezone = "UTC",
                allowEmergency = true
            ),
            frequency = NotificationFrequency.DAILY,
            timezone = "UTC",
            language = "en"
        )
    }
    
    // Validation helpers
    fun validateQuietHours(startTime: String, endTime: String): Boolean {
        return try {
            val start = java.time.LocalTime.parse(startTime)
            val end = java.time.LocalTime.parse(endTime)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun validateChannelSettings(channel: NotificationChannel, preference: ChannelPreference): Boolean {
        return when {
            preference.maxPerDay <= 0 -> false
            preference.minTimeBetween < 0 -> false
            else -> true
        }
    }
    
    // Export/Import settings
    fun exportSettings(): String? {
        val prefs = _preferences.value ?: return null
        
        return try {
            kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            }.encodeToString(prefs)
        } catch (e: Exception) {
            _error.value = "Failed to export settings"
            null
        }
    }
    
    fun importSettings(settingsJson: String) {
        viewModelScope.launch {
            try {
                val importedPrefs = kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<NotificationPreference>(settingsJson)
                
                val userId = getCurrentUserId()
                val updatedPrefs = importedPrefs.copy(userId = userId)
                
                _preferences.value = updatedPrefs
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to import settings: ${e.message}"
            }
        }
    }
}

// Extension functions for better UI state management
fun NotificationSettingsViewModel.isChannelEnabled(channel: NotificationChannel): Boolean {
    return _preferences.value?.isChannelEnabled(channel) ?: false
}

fun NotificationSettingsViewModel.isTopicEnabled(topic: NotificationTopic): Boolean {
    return _preferences.value?.isTopicEnabled(topic) ?: false
}

fun NotificationSettingsViewModel.getChannelPreference(channel: NotificationChannel): ChannelPreference? {
    return _preferences.value?.channels[channel]
}

fun NotificationSettingsViewModel.getTopicPreference(topic: NotificationTopic): TopicPreference? {
    return _preferences.value?.topics[topic]
}

fun NotificationSettingsViewModel.getQuietHours(): QuietHours? {
    return _preferences.value?.quietHours
}

fun NotificationSettingsViewModel.getFrequency(): NotificationFrequency {
    return _preferences.value?.frequency ?: NotificationFrequency.DAILY
}

fun NotificationSettingsViewModel.getLanguage(): String {
    return _preferences.value?.language ?: "en"
}

fun NotificationSettingsViewModel.getTimezone(): String {
    return _preferences.value?.timezone ?: "UTC"
}

// State flow for reactive UI updates
fun NotificationSettingsViewModel.observeChannelEnabled(channel: NotificationChannel): Flow<Boolean> {
    return preferences.map { prefs ->
        prefs?.isChannelEnabled(channel) ?: false
    }
}

fun NotificationSettingsViewModel.observeTopicEnabled(topic: NotificationTopic): Flow<Boolean> {
    return preferences.map { prefs ->
        prefs?.isTopicEnabled(topic) ?: false
    }
}

fun NotificationSettingsViewModel.observeQuietHours(): Flow<QuietHours?> {
    return preferences.map { prefs ->
        prefs?.quietHours
    }
}

fun NotificationSettingsViewModel.observeFrequency(): Flow<NotificationFrequency> {
    return preferences.map { prefs ->
        prefs?.frequency ?: NotificationFrequency.DAILY
    }
}
