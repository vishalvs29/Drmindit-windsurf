package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.android.domain.crisis.CrisisDetector
import com.drmindit.android.domain.model.CrisisLevel
import com.drmindit.android.domain.model.CrisisAlert
import com.drmindit.android.ui.safety.SafeMessagingFilter
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
            
            val rawResponse = generateResponse(input)
            val safeResponse = SafeMessagingFilter.filterResponse(rawResponse)
            
            val aiMessage = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                text = safeResponse,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            
            _messages.value = _messages.value + aiMessage
            _isTyping.value = false
        }
    }
    
    private fun generateResponse(input: String): String {
        val alert = crisisDetector.analyzeText(input)
        
        return when (alert.level) {
            CrisisLevel.IMMEDIATE -> {
                "It sounds like you're really struggling right now — I'm here with you. You're not alone, and there are people who want to help you through this. Let's take one breath together. 🌱\n\n" +
                "If you need to talk to someone immediately, here are some options:\n" +
                "• iCall: 9152927898 (24/7)\n" +
                "• Vandrevala Foundation: 18602667232 (24/7)\n" +
                "• Emergency: 112\n\n" +
                "Would you like me to guide you through a quick grounding exercise while you consider these options?"
            }
            CrisisLevel.HIGH -> {
                "I can hear how difficult this is for you right now. Thank you for trusting me enough to share this. You're showing real strength by reaching out. 🌟\n\n" +
                "Sometimes just having someone acknowledge the struggle can help. Would you like to talk about what's happening, or would you prefer a simple breathing exercise to help you feel more grounded?"
            }
            CrisisLevel.MEDIUM -> {
                "That sounds really challenging. I'm here to support you through this moment. 💙\n\n" +
                "Remember that difficult feelings are temporary, even when they feel overwhelming. What's one small thing we could focus on together right now?"
            }
            CrisisLevel.LOW -> {
                "I notice things might be feeling heavy for you right now. That's completely valid, and I'm grateful you're sharing it with me. 🌱\n\n" +
                "Sometimes just naming what we're feeling can help us understand it better. Would you like to explore what's contributing to this feeling?"
            }
            else -> {
                // Generate empathetic response based on input content
                when {
                    input.contains("anxious", ignoreCase = true) || 
                    input.contains("anxiety", ignoreCase = true) ->
                        "Anxiety can feel so overwhelming, like you're carrying a weight that's hard to put down. I'm here to help you find moments of peace, even small ones. What usually helps when anxiety feels this intense?"
                    
                    input.contains("sad", ignoreCase = true) || 
                    input.contains("depressed", ignoreCase = true) ->
                        "I'm sorry you're going through this sadness right now. It takes courage to even acknowledge these feelings, and I'm honored you're sharing them with me. 🌱 You don't have to carry this alone."
                    
                    input.contains("stress", ignoreCase = true) || 
                    input.contains("overwhelmed", ignoreCase = true) ->
                        "That sounds like so much to handle at once. It makes complete sense that you'd feel overwhelmed. Let's take this one moment at a time. What's one small thing that might feel manageable right now?"
                    
                    input.contains("lonely", ignoreCase = true) || 
                    input.contains("alone", ignoreCase = true) ->
                        "Feeling lonely can be one of the most painful human experiences. Thank you for letting me be here with you in this moment. You're not as alone as you might feel right now. 💙"
                    
                    else -> "Thank you for sharing that with me. I'm here to listen and support you however I can. What's on your mind today?"
                }
            }
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
