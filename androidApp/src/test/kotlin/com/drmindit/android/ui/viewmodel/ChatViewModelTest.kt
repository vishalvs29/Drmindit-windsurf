package com.drmindit.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drmindit.android.domain.crisis.CrisisDetector
import com.drmindit.android.ui.viewmodel.ChatViewModel
import com.drmindit.android.domain.model.CrisisLevel
import com.drmindit.android.domain.model.CrisisAlert
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
class ChatViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var crisisDetector: CrisisDetector
    private lateinit var chatViewModel: ChatViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        crisisDetector = mockk()
        
        // Create ChatViewModel with only CrisisDetector (actual constructor)
        chatViewModel = ChatViewModel(crisisDetector = crisisDetector)
        
        // Setup default mock behaviors
        every { crisisDetector.analyzeText(any()) } returns CrisisAlert(
            level = CrisisLevel.NONE,
            requiresImmediateAction = false,
            detectedKeywords = emptyList(),
            riskFactors = null
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `sendMessage with normal message processes successfully`() {
        // Given
        val testInput = "I'm feeling anxious today"
        
        // When
        chatViewModel.updateInput(testInput)
        
        // Then
        chatViewModel.sendMessage()
        
        // Assert
        val messages = chatViewModel.messages.value
        assertEquals(2, messages.size) // Initial greeting + response
        assertEquals("I'm feeling anxious today", messages[0].text)
        assertTrue(messages[0].isFromUser)
        assertTrue(messages[1].isFromUser == false)
        assertTrue(messages[1].text.contains("overwhelming"))
    }
    
    @Test
    fun `crisis detection triggers banner for high level`() {
        // Given
        val crisisInput = "I want to hurt myself"
        
        // When
        chatViewModel.updateInput(crisisInput)
        
        // Then
        val showBanner = chatViewModel.showCrisisBanner.value
        val showDialog = chatViewModel.showCrisisDialog.value
        
        // Assert
        assertTrue(showBanner, "Crisis banner should show for high level")
        assertFalse(showDialog, "Dialog should not show for high level")
    }
    
    @Test
    fun `crisis detection triggers dialog for immediate level`() {
        // Given
        val immediateInput = "I want to kill myself"
        
        // Mock crisis detector to return immediate level
        every { crisisDetector.analyzeText(immediateInput) } returns CrisisAlert(
            level = CrisisLevel.IMMEDIATE,
            requiresImmediateAction = true,
            detectedKeywords = listOf("kill", "myself"),
            riskFactors = null
        )
        
        // When
        chatViewModel.updateInput(immediateInput)
        
        // Then
        val showBanner = chatViewModel.showCrisisBanner.value
        val showDialog = chatViewModel.showCrisisDialog.value
        
        // Assert
        assertTrue(showBanner, "Crisis banner should show for immediate level")
        assertTrue(showDialog, "Dialog should show for immediate level")
    }
    
    @Test
    fun `sendMessage with crisis keywords triggers crisis modal`() {
        // Given
        val message = "I feel suicidal"
        val crisisAssessment = CrisisAssessment(
            riskLevel = RiskLevel.CRITICAL,
            hasCrisisKeywords = true,
            isLowMood = false,
            detectedKeywords = listOf("suicidal"),
            emergencyHelplines = listOf(
                EmergencyHelpline(
                    name = "Test Helpline",
                    phone = "1234567890",
                    description = "Test",
                    availableHours = "24/7"
                )
            )
        )
        
        every { crisisDetector.analyzeMessage(message, null) } returns crisisAssessment
        every { crisisDetector.getCurrentCrisisState() } returns com.drmindit.android.crisis.CrisisState.Critical(
            message = "Help available",
            emergencyHelplines = crisisAssessment.emergencyHelplines,
            requiresImmediateAction = true
        )
        
        // When
        chatViewModel.sendMessage(message)
        
        // Then
        verify { crisisEscalationManager.handleCrisisEvent(any(), any(), message, RiskLevel.CRITICAL) }
        assertTrue(chatViewModel.showCrisisModal.value)
    }
    
    @Test
    fun `updateCurrentMood with low mood triggers crisis detection`() {
        // Given
        val mood = MoodCategory.VERY_LOW
        val crisisAssessment = CrisisAssessment(
            riskLevel = RiskLevel.HIGH,
            hasCrisisKeywords = false,
            isLowMood = true,
            detectedKeywords = emptyList(),
            emergencyHelplines = emptyList()
        )
        
        every { crisisDetector.analyzeMessage("", mood) } returns crisisAssessment
        every { crisisDetector.getCurrentCrisisState() } returns com.drmindit.android.crisis.CrisisState.HighRisk(
            message = "Support available",
            emergencyHelplines = emptyList(),
            requiresImmediateAction = false
        )
        
        // When
        chatViewModel.updateCurrentMood(mood)
        
        // Then
        verify { crisisDetector.analyzeMessage("", mood) }
        assertEquals(mood, chatViewModel.currentMood.value)
    }
    
    @Test
    fun `dismissCrisisModal hides modal and resets crisis state`() {
        // Given
        every { crisisDetector.getCurrentCrisisState() } returns com.drmindit.android.crisis.CrisisState.HighRisk(
            message = "Test",
            emergencyHelplines = emptyList(),
            requiresImmediateAction = false
        )
        
        // When
        chatViewModel.dismissCrisisModal()
        
        // Then
        verify { crisisDetector.resetCrisisState() }
        assertFalse(chatViewModel.showCrisisModal.value)
    }
    
    @Test
    fun `showGroundingSession shows grounding and hides crisis modal`() {
        // When
        chatViewModel.showGroundingSession()
        
        // Then
        assertTrue(chatViewModel.showGroundingSession.value)
        assertFalse(chatViewModel.showCrisisModal.value)
    }
    
    @Test
    fun `dismissGroundingSession hides grounding session`() {
        // Given
        chatViewModel.showGroundingSession()
        
        // When
        chatViewModel.dismissGroundingSession()
        
        // Then
        assertFalse(chatViewModel.showGroundingSession.value)
    }
    
    @Test
    fun `callHelpline logs the phone number`() {
        // Given
        val phoneNumber = "1234567890"
        
        // When
        chatViewModel.callHelpline(phoneNumber)
        
        // Then
        // In a real implementation, this would verify phone dialer integration
        // For now, we just ensure no exceptions are thrown
    }
    
    @Test
    fun `clearError removes error state`() {
        // Given - setup error state through failed message
        every { chatRepository.sendMessage(any(), any(), any()) } returns Result.failure(
            RuntimeException("Test error")
        )
        chatViewModel.sendMessage("test message")
        
        // When
        chatViewModel.clearError()
        
        // Then
        assertEquals(null, chatViewModel.chatState.value.error)
    }
    
    @Test
    fun `updateMessage updates current message state`() {
        // Given
        val message = "Test message"
        
        // When
        chatViewModel.updateMessage(message)
        
        // Then
        assertEquals(message, chatViewModel.currentMessage.value)
    }
}
