package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // Chat session management
    suspend fun createChatSession(userId: String): Result<ChatSession>
    suspend fun getChatSession(sessionId: String): Result<ChatSession?>
    suspend fun getActiveChatSession(userId: String): Result<ChatSession?>
    suspend fun updateChatSession(session: ChatSession): Result<ChatSession>
    suspend fun deleteChatSession(sessionId: String): Result<Unit>
    suspend fun getAllChatSessions(userId: String): Result<List<ChatSession>>
    
    // Message management
    suspend fun addMessage(sessionId: String, message: ChatMessage): Result<ChatMessage>
    suspend fun updateMessage(message: ChatMessage): Result<ChatMessage>
    suspend fun deleteMessage(sessionId: String, messageId: String): Result<Unit>
    suspend fun getMessages(sessionId: String, limit: Int = 50): Result<List<ChatMessage>>
    suspend fun searchMessages(userId: String, query: String): Result<List<ChatMessage>>
    
    // AI integration
    suspend fun sendMessage(
        sessionId: String, 
        userMessage: String,
        context: List<ChatMessage> = emptyList()
    ): Result<AIResponse>
    
    // Session recommendations
    suspend fun getRecommendedSessions(
        moodCategory: MoodCategory,
        limit: Int = 3
    ): Result<List<RecommendedSession>>
    
    // Safety and escalation
    suspend fun detectRiskLevel(message: String): Result<RiskLevel>
    suspend fun getEmergencyHelplines(country: String = "US"): Result<List<EmergencyHelpline>>
    suspend fun reportCrisisEvent(event: CrisisEvent): Result<Unit>
    
    // Analytics and insights
    suspend fun getChatAnalytics(sessionId: String): Result<ChatAnalytics>
    suspend fun getUserChatInsights(userId: String): Result<List<ChatAnalytics>>
    suspend fun updateChatPreferences(preferences: ChatPreferences): Result<ChatPreferences>
    suspend fun getChatPreferences(userId: String): Result<ChatPreferences?>
    
    // Real-time updates
    fun observeChatSession(sessionId: String): Flow<ChatSession?>
    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>
    fun observeTypingIndicator(sessionId: String): Flow<Boolean>
}

class ChatRepositoryImpl(
    private val aiService: AIService,
    private val sessionRepository: com.drmindit.shared.data.repository.AudioSessionRepository,
    private val localDataSource: ChatLocalDataSource,
    private val safetyService: SafetyService
) : ChatRepository {
    
    override suspend fun createChatSession(userId: String): Result<ChatSession> {
        return try {
            val session = ChatSession(
                id = generateSessionId(),
                userId = userId,
                title = "Wellness Chat - ${getCurrentDate()}",
                messages = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true
            )
            
            localDataSource.saveChatSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChatSession(sessionId: String): Result<ChatSession?> {
        return try {
            val session = localDataSource.getChatSession(sessionId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getActiveChatSession(userId: String): Result<ChatSession?> {
        return try {
            val session = localDataSource.getActiveChatSession(userId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateChatSession(session: ChatSession): Result<ChatSession> {
        return try {
            val updatedSession = session.copy(updatedAt = System.currentTimeMillis())
            localDataSource.updateChatSession(updatedSession)
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteChatSession(sessionId: String): Result<Unit> {
        return try {
            localDataSource.deleteChatSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllChatSessions(userId: String): Result<List<ChatSession>> {
        return try {
            val sessions = localDataSource.getAllChatSessions(userId)
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addMessage(sessionId: String, message: ChatMessage): Result<ChatMessage> {
        return try {
            localDataSource.addMessage(sessionId, message)
            
            // Update session timestamp
            getChatSession(sessionId).onSuccess { session ->
                if (session != null) {
                    updateChatSession(session.copy(updatedAt = System.currentTimeMillis()))
                }
            }
            
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMessage(message: ChatMessage): Result<ChatMessage> {
        return try {
            localDataSource.updateMessage(message)
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMessage(sessionId: String, messageId: String): Result<Unit> {
        return try {
            localDataSource.deleteMessage(sessionId, messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getMessages(sessionId: String, limit: Int): Result<List<ChatMessage>> {
        return try {
            val messages = localDataSource.getMessages(sessionId, limit)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchMessages(userId: String, query: String): Result<List<ChatMessage>> {
        return try {
            val messages = localDataSource.searchMessages(userId, query)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendMessage(
        sessionId: String,
        userMessage: String,
        context: List<ChatMessage>
    ): Result<AIResponse> {
        return try {
            // First, add user message
            val userChatMessage = ChatMessage(
                id = generateMessageId(),
                text = userMessage,
                sender = MessageSender.USER,
                timestamp = System.currentTimeMillis()
            )
            addMessage(sessionId, userChatMessage)
            
            // Detect risk level
            val riskLevel = detectRiskLevel(userMessage).getOrNull() ?: RiskLevel.LOW
            
            // Get AI response
            val aiResponse = aiService.getWellnessResponse(
                message = userMessage,
                context = context,
                riskLevel = riskLevel
            )
            
            if (aiResponse.isSuccess) {
                val response = aiResponse.getOrNull()!!
                
                // Get recommended sessions based on detected mood
                val recommendedSessions = if (response.moodTag != null) {
                    getRecommendedSessions(response.moodTag).getOrNull() ?: emptyList()
                } else {
                    emptyList()
                }
                
                // Create AI message with metadata
                val aiChatMessage = ChatMessage(
                    id = generateMessageId(),
                    text = response.text,
                    sender = MessageSender.AI,
                    timestamp = System.currentTimeMillis(),
                    messageType = if (riskLevel > RiskLevel.LOW) MessageType.SAFETY_ALERT else MessageType.TEXT,
                    metadata = MessageMetadata(
                        recommendedSessions = recommendedSessions,
                        quickReplies = response.quickReplies,
                        moodTag = response.moodTag,
                        riskLevel = riskLevel,
                        confidence = response.confidence,
                        processingTime = response.processingTime
                    )
                )
                
                addMessage(sessionId, aiChatMessage)
                
                // Return enhanced response
                Result.success(response.copy(
                    recommendedSessions = recommendedSessions,
                    riskLevel = riskLevel
                ))
            } else {
                Result.failure(aiResponse.exceptionOrNull() ?: Exception("AI service failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRecommendedSessions(
        moodCategory: MoodCategory,
        limit: Int
    ): Result<List<RecommendedSession>> {
        return try {
            val sessionCategory = moodCategory.getAssociatedSessionCategory()
            val sessions = sessionRepository.getSessionsByCategory(sessionCategory)
            
            if (sessions.isSuccess) {
                val audioSessions = sessions.getOrNull() ?: emptyList()
                val recommendedSessions = audioSessions.take(limit).map { session ->
                    RecommendedSession(
                        id = session.id,
                        title = session.title,
                        description = session.description,
                        duration = session.duration,
                        category = session.category.name,
                        instructorName = session.instructorName,
                        thumbnailUrl = session.thumbnailUrl,
                        audioUrl = session.audioUrl,
                        relevanceScore = calculateRelevanceScore(session, moodCategory),
                        reason = generateRecommendationReason(session, moodCategory)
                    )
                }
                Result.success(recommendedSessions)
            } else {
                Result.failure(sessions.exceptionOrNull() ?: Exception("Failed to get sessions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun detectRiskLevel(message: String): Result<RiskLevel> {
        return try {
            val riskLevel = safetyService.analyzeRisk(message)
            Result.success(riskLevel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEmergencyHelplines(country: String): Result<List<EmergencyHelpline>> {
        return try {
            val helplines = safetyService.getEmergencyHelplines(country)
            Result.success(helplines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reportCrisisEvent(event: CrisisEvent): Result<Unit> {
        return try {
            safetyService.reportCrisisEvent(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChatAnalytics(sessionId: String): Result<ChatAnalytics> {
        return try {
            val session = getChatSession(sessionId).getOrNull()
            if (session != null) {
                val analytics = ChatAnalytics(
                    sessionId = sessionId,
                    userId = session.userId,
                    messageCount = session.getMessageCount(),
                    averageResponseTime = calculateAverageResponseTime(session.messages),
                    sessionDuration = session.getDuration(),
                    moodChanges = detectMoodChanges(session.messages),
                    sessionRecommendations = countSessionRecommendations(session.messages),
                    escalations = countEscalations(session.messages),
                    completed = !session.isActive
                )
                Result.success(analytics)
            } else {
                Result.failure(Exception("Session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserChatInsights(userId: String): Result<List<ChatAnalytics>> {
        return try {
            val sessions = getAllChatSessions(userId).getOrNull() ?: emptyList()
            val insights = sessions.mapNotNull { session ->
                getChatAnalytics(session.id).getOrNull()
            }
            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateChatPreferences(preferences: ChatPreferences): Result<ChatPreferences> {
        return try {
            localDataSource.saveChatPreferences(preferences)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getChatPreferences(userId: String): Result<ChatPreferences?> {
        return try {
            val preferences = localDataSource.getChatPreferences(userId)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeChatSession(sessionId: String): Flow<ChatSession?> {
        return localDataSource.observeChatSession(sessionId)
    }
    
    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> {
        return localDataSource.observeMessages(sessionId)
    }
    
    override fun observeTypingIndicator(sessionId: String): Flow<Boolean> {
        return localDataSource.observeTypingIndicator(sessionId)
    }
    
    // Helper functions
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
    
    private fun calculateRelevanceScore(session: com.drmindit.shared.domain.model.AudioSession, mood: MoodCategory): Float {
        // Simple relevance scoring based on category match and rating
        val categoryScore = if (session.category.id == mood.getAssociatedSessionCategory()) 1.0f else 0.5f
        val ratingScore = session.rating / 5.0f
        return (categoryScore + ratingScore) / 2.0f
    }
    
    private fun generateRecommendationReason(session: com.drmindit.shared.domain.model.AudioSession, mood: MoodCategory): String {
        return when (mood) {
            MoodCategory.ANXIOUS -> "This session can help you find calm and reduce anxiety"
            MoodCategory.STRESSED -> "Designed to help you release tension and manage stress"
            MoodCategory.SLEEPLESS -> "Perfect for helping you relax and fall asleep naturally"
            MoodCategory.DEPRESSED -> "Gentle support for lifting your mood and finding hope"
            else -> "A guided session to support your wellness journey"
        }
    }
    
    private fun calculateAverageResponseTime(messages: List<ChatMessage>): Long {
        val aiMessages = messages.filter { it.isAIMessage() }
        if (aiMessages.size < 2) return 0L
        
        val responseTimes = mutableListOf<Long>()
        for (i in 1 until aiMessages.size) {
            val currentMessage = aiMessages[i]
            val previousUserMessage = messages
                .filter { it.timestamp < currentMessage.timestamp && it.isUserMessage() }
                .maxByOrNull { it.timestamp }
            
            if (previousUserMessage != null) {
                responseTimes.add(currentMessage.timestamp - previousUserMessage.timestamp)
            }
        }
        
        return if (responseTimes.isNotEmpty()) {
            responseTimes.average().toLong()
        } else 0L
    }
    
    private fun detectMoodChanges(messages: List<ChatMessage>): List<MoodChange> {
        val moodChanges = mutableListOf<MoodChange>()
        var lastMood: MoodCategory? = null
        
        messages.forEach { message ->
            val currentMood = message.metadata?.moodTag
            if (currentMood != null && currentMood != lastMood) {
                moodChanges.add(
                    MoodChange(
                        fromMood = lastMood,
                        toMood = currentMood,
                        timestamp = message.timestamp,
                        trigger = message.text.take(50)
                    )
                )
                lastMood = currentMood
            }
        }
        
        return moodChanges
    }
    
    private fun countSessionRecommendations(messages: List<ChatMessage>): Int {
        return messages.count { it.hasRecommendedSessions() }
    }
    
    private fun countEscalations(messages: List<ChatMessage>): Int {
        return messages.count { it.isSafetyAlert() }
    }
}

// Local data source interface
interface ChatLocalDataSource {
    suspend fun saveChatSession(session: ChatSession)
    suspend fun getChatSession(sessionId: String): ChatSession?
    suspend fun getActiveChatSession(userId: String): ChatSession?
    suspend fun updateChatSession(session: ChatSession)
    suspend fun deleteChatSession(sessionId: String)
    suspend fun getAllChatSessions(userId: String): List<ChatSession>
    
    suspend fun addMessage(sessionId: String, message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun deleteMessage(sessionId: String, messageId: String)
    suspend fun getMessages(sessionId: String, limit: Int): List<ChatMessage>
    suspend fun searchMessages(userId: String, query: String): List<ChatMessage>
    
    suspend fun saveChatPreferences(preferences: ChatPreferences)
    suspend fun getChatPreferences(userId: String): ChatPreferences?
    
    fun observeChatSession(sessionId: String): Flow<ChatSession?>
    fun observeMessages(sessionId: String): Flow<List<ChatMessage>>
    fun observeTypingIndicator(sessionId: String): Flow<Boolean>
}
