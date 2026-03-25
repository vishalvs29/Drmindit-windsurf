package com.drmindit.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drmindit.android.crisis.CrisisDetector
import com.drmindit.android.crisis.CrisisEscalationManager
import com.drmindit.shared.domain.model.*
import com.drmindit.shared.domain.repository.ChatRepository
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
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var crisisDetector: CrisisDetector
    private lateinit var crisisEscalationManager: CrisisEscalationManager
    private lateinit var chatViewModel: ChatViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        chatRepository = mockk()
        crisisDetector = mockk()
        crisisEscalationManager = mockk()
        
        // Setup default mock behaviors
        every { chatRepository.getActiveChatSession(any()) } returns Result.success(null)
        every { chatRepository.createChatSession(any()) } returns Result.success(
            ChatSession(
                id = "test-session",
                userId = "test-user",
                title = "Test Session",
                messages = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true
            )
        )
        every { chatRepository.getMessages(any(), any()) } returns flowOf(emptyList())
        every { crisisDetector.analyzeMessage(any(), any()) } returns CrisisAssessment(
            riskLevel = RiskLevel.LOW,
            hasCrisisKeywords = false,
            isLowMood = false,
            detectedKeywords = emptyList(),
            emergencyHelplines = emptyList()
        )
        every { crisisDetector.crisisState } returns flowOf(com.drmindit.android.crisis.CrisisState.Normal)
        every { crisisDetector.getCurrentCrisisState() } returns com.drmindit.android.crisis.CrisisState.Normal
        every { crisisDetector.getEmergencyHelplines() } returns emptyList()
        every { crisisEscalationManager.handleCrisisEvent(any(), any(), any(), any()) } just Runs
        
        chatViewModel = ChatViewModel(chatRepository, crisisDetector, crisisEscalationManager)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `sendMessage with normal message processes successfully`() {
        // Given
        val message = "Hello, how are you?"
        val aiResponse = ChatMessage(
            id = "ai-response",
            text = "I'm doing well, thank you!",
            sender = MessageSender.AI,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.TEXT
        )
        
        every { chatRepository.sendMessage(any(), any(), any()) } returns Result.success(aiResponse)
        
        // When
        chatViewModel.sendMessage(message)
        
        // Then
        verify { chatRepository.sendMessage(any(), message, any()) }
        verify { crisisDetector.analyzeMessage(message, null) }
        assertFalse(chatViewModel.showCrisisModal.value)
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
