package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.android.ai.MentalHealthChatManager
import com.drmindit.shared.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mental Health Chat Controller
 * Manages AI-powered chat interface with safety and context awareness
 */
@HiltViewModel
class MentalHealthChatController @Inject constructor(
    private val chatManager: MentalHealthChatManager
) : ViewModel() {
    
    val chatState: StateFlow<com.drmindit.android.ai.ChatState> = chatManager.chatState
    val messages: StateFlow<List<ChatMessage>> = chatManager.messages
    val isTyping: StateFlow<Boolean> = chatManager.isTyping
    
    /**
     * Initialize chat when ViewModel is created
     */
    fun initializeChat() {
        viewModelScope.launch {
            chatManager.initializeChat()
        }
    }
    
    /**
     * Send user message
     */
    fun sendUserMessage(content: String, emotionTags: List<String> = emptyList()) {
        viewModelScope.launch {
            chatManager.sendUserMessage(content, emotionTags)
        }
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? = chatManager.chatState.value.currentSessionId
    
    /**
     * Clear chat history
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            // Implementation would clear local chat history
            // This is a placeholder for actual implementation
        }
    }
    
    /**
     * Export chat data for backup
     */
    fun exportChatData(): String {
        return "Chat export functionality would be implemented here"
    }
}
