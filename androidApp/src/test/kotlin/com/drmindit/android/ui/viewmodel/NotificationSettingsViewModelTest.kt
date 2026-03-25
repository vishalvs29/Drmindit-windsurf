package com.drmindit.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drmindit.shared.domain.model.*
import com.drmindit.shared.domain.repository.NotificationRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class NotificationSettingsViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var viewModel: NotificationSettingsViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        notificationRepository = mockk()
        
        // Setup default mock behaviors
        every { notificationRepository.getNotificationPreferences(any()) } returns Result.success(
            createDefaultPreferences()
        )
        every { notificationRepository.updateNotificationPreferences(any()) } returns Result.success(Unit)
        
        viewModel = NotificationSettingsViewModel(notificationRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initialization loads preferences successfully`() {
        // Then
        assertFalse(viewModel.isLoading.value)
        assertFalse(viewModel.saveSuccess.value)
        assertEquals(null, viewModel.error.value)
        assertTrue(viewModel.preferences.value != null)
    }
    
    @Test
    fun `toggleChannel enables and disables channel`() {
        // Given
        val channel = NotificationChannel.PUSH_NOTIFICATION
        val initialEnabled = viewModel.isChannelEnabled(channel)
        
        // When
        viewModel.toggleChannel(channel)
        
        // Then
        assertEquals(!initialEnabled, viewModel.isChannelEnabled(channel))
    }
    
    @Test
    fun `toggleTopic enables and disables topic`() {
        // Given
        val topic = NotificationTopic.DAILY_WELLNESS
        val initialEnabled = viewModel.isTopicEnabled(topic)
        
        // When
        viewModel.toggleTopic(topic)
        
        // Then
        assertEquals(!initialEnabled, viewModel.isTopicEnabled(topic))
    }
    
    @Test
    fun `toggleQuietHours enables and disables quiet hours`() {
        // Given
        val initialEnabled = viewModel.getQuietHours()?.enabled ?: false
        
        // When
        viewModel.toggleQuietHours(!initialEnabled)
        
        // Then
        assertEquals(!initialEnabled, viewModel.getQuietHours()?.enabled)
    }
    
    @Test
    fun `updateFrequency changes notification frequency`() {
        // Given
        val newFrequency = NotificationFrequency.WEEKLY
        
        // When
        viewModel.updateFrequency(newFrequency)
        
        // Then
        assertEquals(newFrequency, viewModel.getFrequency())
    }
    
    @Test
    fun `savePreferences saves successfully`() {
        // Given
        viewModel.toggleChannel(NotificationChannel.PUSH_NOTIFICATION)
        
        // When
        viewModel.savePreferences()
        
        // Then
        verify { notificationRepository.updateNotificationPreferences(any()) }
        assertTrue(viewModel.saveSuccess.value)
    }
    
    @Test
    fun `savePreferences handles error`() {
        // Given
        every { notificationRepository.updateNotificationPreferences(any()) } returns Result.failure(
            RuntimeException("Save failed")
        )
        
        // When
        viewModel.savePreferences()
        
        // Then
        assertFalse(viewModel.saveSuccess.value)
        assertEquals("Failed to save preferences", viewModel.error.value)
    }
    
    @Test
    fun `resetToDefaults resets to default preferences`() {
        // Given - modify some preferences
        viewModel.toggleChannel(NotificationChannel.PUSH_NOTIFICATION)
        viewModel.updateFrequency(NotificationFrequency.NEVER)
        
        // When
        viewModel.resetToDefaults()
        
        // Then
        val prefs = viewModel.preferences.value
        assertTrue(prefs?.channels?.get(NotificationChannel.PUSH_NOTIFICATION)?.enabled == true)
        assertEquals(NotificationFrequency.DAILY, viewModel.getFrequency())
    }
    
    @Test
    fun `validateQuietHours returns correct validation`() {
        // Given
        val validStartTime = "22:00"
        val validEndTime = "08:00"
        val invalidTime = "25:00"
        
        // Then
        assertTrue(viewModel.validateQuietHours(validStartTime, validEndTime))
        assertFalse(viewModel.validateQuietHours(invalidTime, validEndTime))
        assertFalse(viewModel.validateQuietHours(validStartTime, invalidTime))
    }
    
    @Test
    fun `validateChannelSettings returns correct validation`() {
        // Given
        val channel = NotificationChannel.PUSH_NOTIFICATION
        val validPreference = ChannelPreference(enabled = true, maxPerDay = 5, minTimeBetween = 60)
        val invalidPreference = ChannelPreference(enabled = true, maxPerDay = 0, minTimeBetween = -1)
        
        // Then
        assertTrue(viewModel.validateChannelSettings(channel, validPreference))
        assertFalse(viewModel.validateChannelSettings(channel, invalidPreference))
    }
    
    @Test
    fun `sendTestNotification sends notification`() {
        // Given
        val channel = NotificationChannel.PUSH_NOTIFICATION
        every { notificationRepository.sendNotification(any()) } returns Result.success(Unit)
        
        // When
        viewModel.sendTestNotification(channel)
        
        // Then
        verify { 
            notificationRepository.sendNotification(
                match<Notification> { notification ->
                    notification.channels.contains(channel) && 
                    notification.title == "Test Notification"
                }
            ) 
        }
    }
    
    @Test
    fun `exportSettings exports valid JSON`() {
        // When
        val exportedJson = viewModel.exportSettings()
        
        // Then
        assertTrue(exportedJson != null)
        assertTrue(exportedJson!!.contains("\"userId\""))
        assertTrue(exportedJson.contains("\"channels\""))
    }
    
    @Test
    fun `importSettings imports valid JSON`() {
        // Given
        val validJson = """
            {
                "userId": "test-user",
                "channels": {
                    "IN_APP": {"enabled": true}
                },
                "topics": {
                    "DAILY_WELLNESS": {"enabled": true}
                }
            }
        """.trimIndent()
        
        // When
        viewModel.importSettings(validJson)
        
        // Then
        assertTrue(viewModel.saveSuccess.value)
        assertEquals("test-user", viewModel.preferences.value?.userId)
    }
    
    @Test
    fun `importSettings handles invalid JSON`() {
        // Given
        val invalidJson = "{ invalid json }"
        
        // When
        viewModel.importSettings(invalidJson)
        
        // Then
        assertEquals("Failed to import settings", viewModel.error.value)
    }
    
    private fun createDefaultPreferences(): NotificationPreference {
        return NotificationPreference(
            userId = "test-user",
            channels = mapOf(
                NotificationChannel.IN_APP to ChannelPreference(enabled = true),
                NotificationChannel.PUSH_NOTIFICATION to ChannelPreference(enabled = true),
                NotificationChannel.EMAIL to ChannelPreference(enabled = true)
            ),
            topics = mapOf(
                NotificationTopic.DAILY_WELLNESS to TopicPreference(enabled = true),
                NotificationTopic.SLEEP to TopicPreference(enabled = true)
            ),
            quietHours = QuietHours(enabled = false),
            frequency = NotificationFrequency.DAILY
        )
    }
}
