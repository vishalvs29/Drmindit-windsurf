package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.domain.model.*
import com.drmindit.shared.domain.repository.ChatRepository
import com.drmindit.android.crisis.CrisisDetector
import com.drmindit.android.crisis.CrisisEscalationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val crisisDetector: CrisisDetector,
    private val crisisEscalationManager: CrisisEscalationManager
) : ViewModel() {
    
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
    
    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()
    
    private val _currentMood = MutableStateFlow<MoodCategory?>(null)
    val currentMood: StateFlow<MoodCategory?> = _currentMood.asStateFlow()
    
    private val _showCrisisModal = MutableStateFlow(false)
    val showCrisisModal: StateFlow<Boolean> = _showCrisisModal.asStateFlow()
    
    private val _showGroundingSession = MutableStateFlow(false)
    val showGroundingSession: StateFlow<Boolean> = _showGroundingSession.asStateFlow()
    
    private var currentSessionId: String? = null
    private val userId = "current_user" // In real app, get from auth service
    
    init {
        initializeChat()
        observeCrisisState()
    }
    
    private fun initializeChat() {
        viewModelScope.launch {
            // Try to get active session or create new one
            val activeSession = chatRepository.getActiveChatSession(userId)
            if (activeSession.isSuccess) {
                val session = activeSession.getOrNull()
                if (session != null) {
                    currentSessionId = session.id
                    loadChatSession(session.id)
                } else {
                    createNewChatSession()
                }
            } else {
                createNewChatSession()
            }
        }
    }
    
    private fun createNewChatSession() {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true)
            
            val sessionResult = chatRepository.createChatSession(userId)
            if (sessionResult.isSuccess) {
                val session = sessionResult.getOrNull()
                if (session != null) {
                    currentSessionId = session.id
                    _chatState.value = ChatState(
                        currentSession = session,
                        messages = emptyList(),
                        suggestedReplies = getDefaultQuickReplies()
                    )
                    
                    // Add welcome message
                    addWelcomeMessage()
                }
            } else {
                _chatState.value = _chatState.value.copy(
                    error = "Failed to create chat session",
                    isLoading = false
                )
            }
        }
    }
    
    private fun addWelcomeMessage() {
        viewModelScope.launch {
            val welcomeMessage = ChatMessage(
                id = "welcome_${System.currentTimeMillis()}",
                text = "Hi! I'm your AI wellness companion. I'm here to support you with stress, anxiety, sleep issues, and more. How are you feeling today?",
                sender = MessageSender.AI,
                timestamp = System.currentTimeMillis(),
                metadata = MessageMetadata(
                    quickReplies = getDefaultQuickReplies(),
                    moodTag = MoodCategory.NEUTRAL,
                    confidence = 1.0f
                )
            )
            
            currentSessionId?.let { sessionId ->
                chatRepository.addMessage(sessionId, welcomeMessage)
                loadChatSession(sessionId)
            }
        }
    }
    
    private fun loadChatSession(sessionId: String) {
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true)
            
            val messagesResult = chatRepository.getMessages(sessionId, 50)
            if (messagesResult.isSuccess) {
                val messages = messagesResult.getOrNull() ?: emptyList()
                val lastMessage = messages.lastOrNull { it.isAIMessage() }
                val suggestedReplies = lastMessage?.getQuickReplies() ?: getDefaultQuickReplies()
                
                _chatState.value = _chatState.value.copy(
                    messages = messages,
                    isLoading = false,
                    suggestedReplies = suggestedReplies
                )
            } else {
                _chatState.value = _chatState.value.copy(
                    error = "Failed to load messages",
                    isLoading = false
                )
            }
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            _chatState.value = _chatState.value.copy(isLoading = true, error = null)
            _isTyping.value = true
            _currentMessage.value = ""
            
            try {
                // Analyze message for crisis indicators
                val assessment = crisisDetector.analyzeMessage(message, _currentMood.value)
                crisisDetector.updateCrisisState(assessment)
                
                // Handle crisis escalation
                if (assessment.riskLevel >= RiskLevel.HIGH) {
                    _showCrisisModal.value = true
                    crisisEscalationManager.handleCrisisEvent(
                        userId = userId,
                        sessionId = sessionId,
                        message = message,
                        riskLevel = assessment.riskLevel
                    )
                }
                
                val context = _chatState.value.messages.takeLast(10)
                val response = chatRepository.sendMessage(sessionId, message, context)
                
                if (response.isSuccess) {
                    val aiResponse = response.getOrNull()
                    if (aiResponse != null) {
                        // Update suggested replies for next message
                        _chatState.value = _chatState.value.copy(
                            suggestedReplies = aiResponse.quickReplies.ifEmpty { getDefaultQuickReplies() },
                            riskDetected = aiResponse.riskLevel > RiskLevel.LOW,
                            safetyAlert = if (aiResponse.riskLevel > RiskLevel.LOW) {
                                SafetyAlert(
                                    level = aiResponse.riskLevel,
                                    message = "Support is available. Please reach out if you need help.",
                                    suggestedHelplines = crisisDetector.getEmergencyHelplines(),
                                    requiresImmediateAction = aiResponse.riskLevel == RiskLevel.CRITICAL,
                                    autoEscalation = aiResponse.requiresEscalation
                                )
                            } else null
                        )
                        
                        // Reload messages to include AI response
                        loadChatSession(sessionId)
                        
                        // Report crisis if detected
                        if (aiResponse.riskLevel >= RiskLevel.HIGH) {
                            reportCrisisEvent(message, aiResponse.riskLevel)
                        }
                    }
                } else {
                    _chatState.value = _chatState.value.copy(
                        error = response.exceptionOrNull()?.message ?: "Failed to send message"
                    )
                }
            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    error = "Something went wrong. Please try again."
                )
            } finally {
                _chatState.value = _chatState.value.copy(isLoading = false)
                _isTyping.value = false
            }
        }
    }
    
    fun updateMessage(message: String) {
        _currentMessage.value = message
    }
    
    fun clearChat() {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                chatRepository.deleteChatSession(sessionId)
                createNewChatSession()
            }
        }
    }
    
    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
    }
    
    fun retryLastMessage() {
        // Find the last user message and resend it
        val lastUserMessage = _chatState.value.messages
            .lastOrNull { it.isUserMessage() }
        
        if (lastUserMessage != null) {
            // Remove the last failed AI message if exists
            val messages = _chatState.value.messages.toMutableList()
            if (messages.lastOrNull()?.isAIMessage() == true) {
                messages.removeLastOrNull()
                _chatState.value = _chatState.value.copy(messages = messages)
            }
            
            sendMessage(lastUserMessage.text)
        }
    }
    
    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                val messages = _chatState.value.messages
                val message = messages.find { it.id == messageId }
                if (message != null && !message.isRead) {
                    val updatedMessage = message.copy(isRead = true)
                    chatRepository.updateMessage(updatedMessage)
                    
                    val updatedMessages = messages.map { 
                        if (it.id == messageId) updatedMessage else it 
                    }
                    _chatState.value = _chatState.value.copy(messages = updatedMessages)
                }
            }
        }
    }
    
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                chatRepository.deleteMessage(sessionId, messageId)
                loadChatSession(sessionId)
            }
        }
    }
    
    fun reportMessage(messageId: String, reason: String) {
        viewModelScope.launch {
            // In a real implementation, this would send a report to moderation
            println("Message reported: $messageId, Reason: $reason")
        }
    }
    
    fun getEmergencyHelplines() {
        viewModelScope.launch {
            try {
                val helplines = chatRepository.getEmergencyHelplines()
                if (helplines.isSuccess) {
                    val helplineList = helplines.getOrNull() ?: emptyList()
                    // Update safety alert with helplines
                    val currentAlert = _chatState.value.safetyAlert
                    if (currentAlert != null) {
                        _chatState.value = _chatState.value.copy(
                            safetyAlert = currentAlert.copy(
                                suggestedHelplines = helplineList
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _chatState.value = _chatState.value.copy(
                    error = "Failed to load helplines"
                )
            }
        }
    }
    
    fun updateChatPreferences(preferences: ChatPreferences) {
        viewModelScope.launch {
            chatRepository.updateChatPreferences(preferences)
        }
    }
    
    fun getSessionAnalytics(): ChatAnalytics? {
        return currentSessionId?.let { sessionId ->
            try {
                // This would be called from a analytics screen
                null // Placeholder
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun getDefaultQuickReplies(): List<String> {
        return listOf(
            "I feel anxious",
            "I can't sleep",
            "I feel stressed",
            "I feel low",
            "I need motivation",
            "Tell me more"
        )
    }
    
    private fun reportCrisisEvent(message: String, riskLevel: RiskLevel) {
        viewModelScope.launch {
            val crisisEvent = CrisisEvent(
                id = "crisis_${System.currentTimeMillis()}",
                userId = userId,
                sessionId = currentSessionId ?: "",
                message = message,
                riskLevel = riskLevel,
                timestamp = System.currentTimeMillis(),
                resolved = false,
                escalated = riskLevel == RiskLevel.CRITICAL,
                notes = "Detected by AI safety system"
            )
            
            chatRepository.reportCrisisEvent(crisisEvent)
        }
    }
    
    // Voice input support (structure for future implementation)
    fun startVoiceInput() {
        // TODO: Implement voice input
    }
    
    fun stopVoiceInput() {
        // TODO: Implement voice input
    }
    
    // Accessibility support
    fun announceMessage(message: String) {
        // TODO: Implement accessibility announcements
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up any resources
    }
    
    // Crisis management methods
    private fun observeCrisisState() {
        viewModelScope.launch {
            crisisDetector.crisisState.collect { crisisState ->
                _showCrisisModal.value = crisisState != com.drmindit.android.crisis.CrisisState.Normal
            }
        }
    }
    
    fun dismissCrisisModal() {
        _showCrisisModal.value = false
        crisisDetector.resetCrisisState()
    }
    
    fun showGroundingSession() {
        _showGroundingSession.value = true
        _showCrisisModal.value = false
    }
    
    fun dismissGroundingSession() {
        _showGroundingSession.value = false
    }
    
    fun callHelpline(phoneNumber: String) {
        // This would integrate with phone dialer
        // For now, just log the action
        println("Calling helpline: $phoneNumber")
    }
    
    fun updateCurrentMood(mood: MoodCategory) {
        _currentMood.value = mood
        
        // Check if mood indicates crisis
        val assessment = crisisDetector.analyzeMessage("", mood)
        if (assessment.riskLevel >= RiskLevel.HIGH) {
            crisisDetector.updateCrisisState(assessment)
            _showCrisisModal.value = true
        }
    }
}

// Crisis event data class
data class CrisisEvent(
    val id: String,
    val userId: String,
    val sessionId: String,
    val message: String,
    val riskLevel: RiskLevel,
    val timestamp: Long,
    val resolved: Boolean = false,
    val escalated: Boolean = false,
    val notes: String? = null,
    val followUpRequired: Boolean = riskLevel >= RiskLevel.HIGH,
    val followUpTimestamp: Long? = if (followUpRequired) timestamp + (24 * 60 * 60 * 1000) else null
)

// Chat preferences extension
fun ChatViewModel.getChatPreferences(): Flow<ChatPreferences?> {
    return flow {
        try {
            val preferences = chatRepository.getChatPreferences(userId)
            emit(preferences.getOrNull())
        } catch (e: Exception) {
            emit(null)
        }
    }
}

// Chat analytics helper
fun ChatViewModel.getChatInsights(): Flow<List<ChatAnalytics>> {
    return flow {
        try {
            val insights = chatRepository.getUserChatInsights(userId)
            emit(insights.getOrNull() ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

// Message search functionality
fun ChatViewModel.searchMessages(query: String): Flow<List<ChatMessage>> {
    return flow {
        try {
            val results = chatRepository.searchMessages(userId, query)
            emit(results.getOrNull() ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

// Chat session management
fun ChatViewModel.getAllChatSessions(): Flow<List<ChatSession>> {
    return flow {
        try {
            val sessions = chatRepository.getAllChatSessions(userId)
            emit(sessions.getOrNull() ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

fun ChatViewModel.switchToSession(sessionId: String) {
    viewModelScope.launch {
        currentSessionId = sessionId
        loadChatSession(sessionId)
    }
}

// Mood tracking
fun ChatViewModel.trackMood(mood: MoodCategory) {
    viewModelScope.launch {
        val moodMessage = ChatMessage(
            id = "mood_${System.currentTimeMillis()}",
            text = "I'm feeling ${mood.getDisplayName().lowercase()}",
            sender = MessageSender.USER,
            timestamp = System.currentTimeMillis(),
            messageType = MessageType.MOOD_CHECK,
            metadata = MessageMetadata(moodTag = mood)
        )
        
        currentSessionId?.let { sessionId ->
            chatRepository.addMessage(sessionId, moodMessage)
            loadChatSession(sessionId)
        }
    }
}

// Session recommendations
fun ChatViewModel.getSessionRecommendations(mood: MoodCategory): Flow<List<RecommendedSession>> {
    return flow {
        try {
            val recommendations = chatRepository.getRecommendedSessions(mood, 3)
            emit(recommendations.getOrNull() ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
