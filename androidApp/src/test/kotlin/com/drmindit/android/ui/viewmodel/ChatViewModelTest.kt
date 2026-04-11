package com.drmindit.android.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drmindit.android.domain.crisis.CrisisDetector
import com.drmindit.android.ui.viewmodel.ChatViewModel
import com.drmindit.android.domain.model.CrisisLevel
import com.drmindit.android.domain.model.CrisisAlert
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var crisisDetector: CrisisDetector
    private lateinit var chatViewModel: ChatViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        crisisDetector = mockk()
        
        // Setup default mock behaviors
        every { crisisDetector.analyzeText(any()) } returns CrisisAlert(
            level = CrisisLevel.NONE,
            detectedText = "",
            timestamp = System.currentTimeMillis(),
            requiresImmediateAction = false,
            detectedKeywords = emptyList(),
            riskFactors = null
        )

        // Create ChatViewModel with only CrisisDetector (actual constructor)
        chatViewModel = ChatViewModel(crisisDetector = crisisDetector)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `sendMessage with normal message processes successfully`() = runTest {
        // Given
        val testInput = "I'm feeling anxious today"
        
        // When
        chatViewModel.updateInput(testInput)
        chatViewModel.sendMessage()
        
        // Advance time to skip delay
        advanceTimeBy(1100)
        runCurrent()
        
        // Assert
        val messages = chatViewModel.messages.value
        assertEquals(2, messages.size) // User message + AI response
        assertEquals("I'm feeling anxious today", messages[0].text)
        assertTrue(messages[0].isFromUser)
        assertFalse(messages[1].isFromUser)
        assertTrue(messages[1].text.isNotEmpty())
    }
    
    @Test
    fun `crisis detection triggers banner for high level`() = runTest {
        // Given
        val crisisInput = "I want to hurt myself"
        
        every { crisisDetector.analyzeText(crisisInput) } returns CrisisAlert(
            level = CrisisLevel.HIGH,
            detectedText = crisisInput,
            timestamp = System.currentTimeMillis(),
            requiresImmediateAction = false,
            detectedKeywords = listOf("hurt", "myself"),
            riskFactors = null
        )
        
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
    fun `crisis detection triggers dialog for immediate level`() = runTest {
        // Given
        val immediateInput = "I want to kill myself"
        
        // Mock crisis detector to return immediate level
        every { crisisDetector.analyzeText(immediateInput) } returns CrisisAlert(
            level = CrisisLevel.IMMEDIATE,
            requiresImmediateAction = true,
            detectedKeywords = listOf("kill", "myself"),
            riskFactors = null,
            detectedText = immediateInput,
            timestamp = System.currentTimeMillis()
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
}
