package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.android.domain.crisis.CrisisDetector
import com.drmindit.android.domain.model.CrisisAlert
import com.drmindit.android.domain.model.CrisisLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing chat interactions and crisis detection
 */
class ChatViewModel(
    private val crisisDetector: CrisisDetector = CrisisDetector()
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
    
    private val _crisisAlert = MutableStateFlow<CrisisAlert?>(null)
    val crisisAlert: StateFlow<CrisisAlert?> = _crisisAlert.asStateFlow()
    
    private val _showCrisisDialog = MutableStateFlow(false)
    val showCrisisDialog: StateFlow<Boolean> = _showCrisisDialog.asStateFlow()
    
    private val _showCrisisBanner = MutableStateFlow(false)
    val showCrisisBanner: StateFlow<Boolean> = _showCrisisBanner.asStateFlow()
    
    private val _crisisMessage = MutableStateFlow("")
    val crisisMessage: StateFlow<String> = _crisisMessage.asStateFlow()
    
    fun updateInput(input: String) {
        _currentInput.value = input
        
        // Real-time crisis detection with immediate banner
        val alert = crisisDetector.analyzeText(input)
        if (alert.level != CrisisLevel.NONE) {
            _crisisMessage.value = "I'm here with you. You're not alone."
            _showCrisisBanner.value = true
            
            // Only show dialog for immediate crises
            if (alert.requiresImmediateAction) {
                _showCrisisDialog.value = true
            }
        } else {
            _crisisMessage.value = ""
            _showCrisisBanner.value = false
            _showCrisisDialog.value = false
        }
    }
    
    fun sendMessage() {
        val input = _currentInput.value.trim()
        if (input.isEmpty()) return
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                text = input,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + userMessage
            
            // Clear crisis states after sending
            _crisisMessage.value = ""
            _showCrisisBanner.value = false
            _showCrisisDialog.value = false
            
            // Clear input
            _currentInput.value = ""
            
            // Show typing indicator
            _isTyping.value = true
            
            // Simulate AI response (replace with actual AI integration)
            kotlinx.coroutines.delay(1000)
            
            val response = generateResponse(input)
            val aiMessage = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = response,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            
            _messages.value = _messages.value + aiMessage
            _isTyping.value = false
        }
    }
    
    private fun generateResponse(userInput: String): String {
        // Check for crisis first
        val crisisAlert = crisisDetector.analyzeText(userInput)
        if (crisisAlert.level != CrisisLevel.NONE) {
            return getCrisisResponse(crisisAlert)
        }
        
        // Generate appropriate response based on input
        return when {
            userInput.contains("anxious", ignoreCase = true) -> 
                "I understand you're feeling anxious. Let's try some breathing exercises together."
            userInput.contains("sad", ignoreCase = true) -> 
                "I'm here to support you. Would you like to talk about what's making you feel sad?"
            userInput.contains("stress", ignoreCase = true) -> 
                "Stress can be overwhelming. Let's explore some relaxation techniques."
            userInput.contains("sleep", ignoreCase = true) -> 
                "Good sleep is essential. I can help you with some sleep meditation techniques."
            else -> "I'm here to help you on your mental wellness journey. What would you like to discuss today?"
        }
    }
    
    private fun getCrisisResponse(alert: CrisisAlert): String {
        return when (alert.level) {
            CrisisLevel.IMMEDIATE -> 
                "I'm concerned about your safety. Please reach out to a crisis hotline immediately. " +
                "You can call 988 in the US and Canada, or text HOME to 741741."
            CrisisLevel.HIGH -> 
                "It sounds like you're going through a difficult time. " +
                "Please consider talking to a mental health professional or calling a support line."
            CrisisLevel.MEDIUM -> 
                "I notice you might be struggling. " +
                "Would you like to try some grounding exercises or talk about what's on your mind?"
            CrisisLevel.LOW -> 
                "I'm here to support you. " +
                "Let's work together to find some helpful strategies."
            CrisisLevel.NONE -> ""
        }
    }
    
    fun dismissCrisisDialog() {
        _showCrisisDialog.value = false
        _crisisAlert.value = null
    }
    
    fun clearMessages() {
        _messages.value = emptyList()
    }
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)
