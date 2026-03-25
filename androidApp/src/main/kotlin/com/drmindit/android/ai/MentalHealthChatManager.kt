package com.drmindit.android.ai

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.drmindit.shared.domain.model.ChatMessage
import com.drmindit.shared.domain.repository.ChatRepository
import com.drmindit.android.config.SecureConfigManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI-powered Mental Health Chat Manager
 * Handles LLM integration, context-aware conversations, and safety measures
 */
@Singleton
class MentalHealthChatManager @Inject constructor(
    private val context: Context,
    private val chatRepository: ChatRepository,
    private val secureConfigManager: SecureConfigManager
) {
    
    private val dataStore: DataStore<Preferences> by lazy {
        context.dataStore
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()
    
    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    /**
     * Initialize chat with mental health context
     */
    suspend fun initializeChat() {
        // Load conversation history
        loadChatHistory()
        
        // Send welcome message from AI
        sendAIMessage(
            content = generateWelcomeMessage(),
            messageType = ChatMessageType.AI_WELCOME
        )
    }
    
    /**
     * Send user message to AI
     */
    suspend fun sendUserMessage(content: String, emotionTags: List<String> = emptyList()) {
        _isTyping.value = true
        
        try {
            val userMessage = ChatMessage(
                id = generateMessageId(),
                content = content,
                sender = ChatSender.USER,
                timestamp = System.currentTimeMillis(),
                messageType = ChatMessageType.USER,
                emotionTags = emotionTags,
                isRead = true
            )
            
            // Add to local state immediately
            _messages.value = _messages.value + userMessage
            
            // Save to repository
            chatRepository.sendMessage(userMessage)
            
            // Get AI response
            getAIResponse(userMessage)
            
        } catch (e: Exception) {
            // Handle error gracefully
            val errorMessage = ChatMessage(
                id = generateMessageId(),
                content = "I'm having trouble processing your message right now. Please try again.",
                sender = ChatSender.SYSTEM,
                timestamp = System.currentTimeMillis(),
                messageType = ChatMessageType.ERROR,
                isRead = true
            )
            
            _messages.value = _messages.value + errorMessage
        } finally {
            _isTyping.value = false
        }
    }
    
    /**
     * Get AI response with mental health context
     */
    private suspend fun getAIResponse(userMessage: ChatMessage) {
        _isTyping.value = true
        
        try {
            // Build conversation context
            val context = buildConversationContext(userMessage)
            
            // Generate mental health focused prompt
            val prompt = generateMentalHealthPrompt(userMessage.content, context)
            
            // Get API key securely
            val apiKey = secureConfigManager.getOpenAIApiKey()
            
            // Call LLM API
            val response = callLLMApi(prompt, apiKey)
            
            // Process response
            val aiMessage = ChatMessage(
                id = generateMessageId(),
                content = response.content.trim(),
                sender = ChatSender.AI,
                timestamp = System.currentTimeMillis(),
                messageType = ChatMessageType.AI_RESPONSE,
                emotionAnalysis = response.emotionAnalysis,
                isRead = true,
                safetyScore = response.safetyScore
            )
            
            // Add to messages
            _messages.value = _messages.value + aiMessage
            
            // Save to repository
            chatRepository.sendMessage(aiMessage)
            
            // Analyze for crisis detection
            analyzeForCrisis(response.content)
            
        } catch (e: Exception) {
            val errorMessage = ChatMessage(
                id = generateMessageId(),
                content = "I'm experiencing technical difficulties. Please try again in a moment.",
                sender = ChatSender.SYSTEM,
                timestamp = System.currentTimeMillis(),
                messageType = ChatMessageType.ERROR,
                isRead = true
            )
            
            _messages.value = _messages.value + errorMessage
        } finally {
            _isTyping.value = false
        }
    }
    
    /**
     * Build conversation context for better AI responses
     */
    private fun buildConversationContext(currentMessage: ChatMessage): ConversationContext {
        val recentMessages = _messages.value.takeLast(10)
        val currentEmotion = extractEmotionalTone(recentMessages)
        val timeOfDay = getTimeOfDayContext()
        val sessionCount = getSessionCount(recentMessages)
        
        return ConversationContext(
            recentMessages = recentMessages,
            currentEmotionalTone = currentEmotion,
            timeOfDay = timeOfDay,
            sessionCount = sessionCount,
            userIntent = extractUserIntent(currentMessage.content)
        )
    }
    
    /**
     * Generate mental health focused prompt
     */
    private fun generateMentalHealthPrompt(userContent: String, context: ConversationContext): String {
        val basePrompt = """
            You are DrMindit, an AI mental health companion. Your role is to provide supportive, empathetic, and safe mental health guidance.
            
            Current Context:
            - Time of day: ${context.timeOfDay}
            - User's emotional tone: ${context.currentEmotionalTone}
            - Session count: ${context.sessionCount}
            - Recent conversation themes: ${extractThemes(context.recentMessages)}
            
            User's message: "$userContent"
            - User's intent: ${context.userIntent}
        """.trimIndent()
        
        val safetyGuidelines = """
            Safety Guidelines:
            1. Always prioritize user safety and wellbeing
            2. Never provide harmful or dangerous advice
            3. Encourage professional help when needed
            4. Maintain supportive, non-judgmental tone
            5. If user expresses crisis, immediately provide resources
            6. Respect privacy and boundaries
        """.trimIndent()
        
        return """
            $basePrompt
            
            $safetyGuidelines
            
            Please respond to the user's message with:
            - Empathy and understanding
            - Relevant mental health insights
            - Practical coping strategies
            - Encouragement and support
            - Questions to deepen understanding when appropriate
            - If crisis is detected, prioritize safety and immediate help
            
            Keep responses concise (2-3 paragraphs max) but thorough.
            Always maintain a hopeful, supportive tone.
        """.trimIndent()
    }
    
    /**
     * Generate welcome message
     */
    private fun generateWelcomeMessage(): String {
        val timeOfDay = getTimeOfDayContext()
        return when (timeOfDay) {
            "morning" -> "Good morning! I'm here to support your mental wellness journey today. How are you feeling?"
            "afternoon" -> "Hello! I'm here to help you navigate whatever you're experiencing. What's on your mind this afternoon?"
            "evening" -> "Good evening! Taking a moment to check in can support your wellbeing tonight. How has your day been?"
            else -> "Hello! I'm DrMindit, your mental health companion. I'm here to listen and support you. What would you like to talk about?"
        }
    }
    
    /**
     * Call LLM API with mental health context
     */
    private suspend fun callLLMApi(prompt: String, apiKey: String): LLMResponse {
        // This would integrate with actual LLM API
        // For now, simulating with contextual responses
        
        val emotionKeywords = extractEmotionalKeywords(prompt)
        val isCrisis = detectCrisisKeywords(prompt)
        
        return if (isCrisis) {
            LLMResponse(
                content = generateCrisisResponse(),
                emotionAnalysis = "crisis",
                safetyScore = 0.1f,
                requiresImmediateAction = true
            )
        } else {
            LLMResponse(
                content = generateContextualResponse(prompt, emotionKeywords),
                emotionAnalysis = analyzeEmotion(emotionKeywords),
                safetyScore = calculateSafetyScore(emotionKeywords),
                requiresImmediateAction = false
            )
        }
    }
    
    /**
     * Generate crisis response
     */
    private fun generateCrisisResponse(): String {
        return """
            I hear that you're going through a really difficult time right now. Your safety is the most important thing.
            
            Immediate Support Options:
            🚨 **Crisis Hotline**: 988 (Available 24/7)
            📱 **Crisis Text Line**: Text HOME to 741741
            🏥 **Emergency Services**: Call 911 or go to nearest emergency room
            
            While you wait for help:
            🧘 **Try deep breathing**: Inhale for 4 seconds, hold for 4, exhale for 4
            📍 **Ground yourself**: Name 5 things you can see, hear, smell, touch, taste
            🤝 **Reach out**: Contact a trusted friend, family member, or mental health professional
            
            You are not alone in this. Help is available, and you deserve support. Please reach out to one of the resources above.
            
            I'll stay here with you. You matter, and your wellbeing matters deeply.
        """.trimIndent()
    }
    
    /**
     * Generate contextual response
     */
    private fun generateContextualResponse(prompt: String, emotionKeywords: List<String>): String {
        val primaryEmotion = emotionKeywords.firstOrNull()
        
        return when (primaryEmotion) {
            "anxious", "worried", "nervous" -> """
                I hear your anxiety, and I want you to know that what you're feeling is completely valid. Anxiety is our body's natural response to stress.
                
                Here are some techniques that might help right now:
                🧘 **Box Breathing**: Try 4-7-8 breathing (inhale 4, hold 7, exhale 8)
                🌊 **Progressive Muscle Relaxation**: Tense and release muscle groups
                📝 **Grounding**: Focus on your senses - what can you see, hear, smell, touch?
                🎯 **5-4-3-2-1**: Name 5 things you observe, 4 you can't, 3 you can smell, 2 you can taste, 1 thing you can feel
                
                Remember: These feelings are temporary, like clouds passing. You have the strength to navigate through them. Would you like to try any of these techniques together?
            """.trimIndent()
            
            "sad", "depressed", "down", "hopeless" -> """
                I hear the weight of what you're carrying, and I want you to know it's okay to feel this way. Your feelings are valid and important.
                
                Sometimes the bravest thing we can do is acknowledge when we're struggling. You don't have to carry this alone.
                
                Gentle support options:
                💙 **Self-compassion**: Treat yourself with the same kindness you'd offer a friend
                🌱 **Small steps**: Even tiny actions count - getting up, opening curtains, taking a shower
                📞 **Professional support**: Consider reaching out to a mental health professional
                🎵 **Comforting activities**: Music, nature sounds, or gentle movement
                🤝 **Connection**: You're not alone in this experience
                
                You deserve support, care, and understanding. Would you like to explore any gentle coping strategies together?
            """.trimIndent()
            
            "angry", "frustrated", "irritated" -> """
                I hear your frustration, and it makes sense that you're feeling this way. Your anger is a valid emotion that deserves understanding.
                
                Healthy ways to process anger:
                🥊 **Physical release**: Safe exercise, punching pillow, intense movement
                🧊 **Cooling down**: Cold water on face, deep breathing, counting to 10
                📝 **Journaling**: Write down what triggered this feeling
                🚶 **Time out**: Step away from the situation temporarily
                💬 **Express constructively**: Use "I" statements instead of blaming
                
                Your feelings are signals, not character flaws. Would you like to explore what's underneath this anger?
            """.trimIndent()
            
            "happy", "excited", "joyful", "grateful" -> """
                It's wonderful to hear positivity in your voice! These moments of joy are precious and worth celebrating.
                
                Ways to nurture this feeling:
                🌟 **Gratitude practice**: Name 3 things you're thankful for right now
                🎯 **Savor the moment**: Fully experience this positive feeling without rushing
                🤝 **Share your joy**: Consider telling someone who supports you
                🌱 **Build on it**: What activities or accomplishments led to this feeling?
                💪 **Pay it forward**: Use this positive energy for something meaningful
                
                Positive emotions are strengths that help us build resilience. What's contributing to this good feeling?
            """.trimIndent()
            
            else -> """
                Thank you for sharing that with me. I'm here to listen with an open heart and mind.
                
                Whatever you're experiencing - whether it's clarity, confusion, excitement, or anything in between - I'm here to explore it with you.
                
                Mental wellness is a journey, and every conversation helps us understand ourselves better. What aspect would you like to focus on today?
            """.trimIndent()
        }
    }
    
    /**
     * Analyze for crisis indicators
     */
    private fun analyzeForCrisis(content: String) {
        val crisisKeywords = listOf(
            "suicide", "kill myself", "end my life", "hurt myself",
            "can't go on", "no way out", "giving up",
            "overwhelmed", "can't handle", "breaking point",
            "emergency", "crisis", "danger", "harm"
        )
        
        return crisisKeywords.any { keyword -> 
            content.lowercase().contains(keyword.lowercase())
        }
    }
    
    /**
     * Extract emotional keywords
     */
    private fun extractEmotionalKeywords(text: String): List<String> {
        val emotionMap = mapOf(
            "anxious" to listOf("anxious", "worried", "nervous", "panic", "fear"),
            "sad" to listOf("sad", "depressed", "down", "hopeless", "crying", "tears"),
            "angry" to listOf("angry", "frustrated", "irritated", "mad", "upset"),
            "happy" to listOf("happy", "excited", "joyful", "grateful", "glad", "cheerful"),
            "confused" to listOf("confused", "uncertain", "unsure", "questioning"),
            "calm" to listOf("calm", "peaceful", "relaxed", "serene", "content")
        )
        
        return emotionMap.entries.flatMap { (_, keywords) ->
            keywords.filter { keyword -> 
                text.lowercase().contains(keyword.lowercase())
            }
        }.distinct()
    }
    
    /**
     * Analyze emotion from keywords
     */
    private fun analyzeEmotion(keywords: List<String>): String {
        val emotionScores = mapOf(
            "anxious" to 0.8f, "worried" to 0.7f, "nervous" to 0.6f, "panic" to 0.9f,
            "sad" to 0.8f, "depressed" to 0.9f, "down" to 0.6f, "hopeless" to 1.0f,
            "angry" to 0.8f, "frustrated" to 0.7f, "irritated" to 0.6f, "mad" to 0.9f,
            "happy" to 0.2f, "excited" to 0.3f, "joyful" to 0.1f, "grateful" to 0.1f,
            "confused" to 0.5f, "calm" to 0.1f
        )
        
        return keywords.maxOfOrNullOrNull { emotionScores[it] } ?: "neutral"
    }
    
    /**
     * Calculate safety score
     */
    private fun calculateSafetyScore(keywords: List<String>): Float {
        val dangerousKeywords = listOf("harm", "kill", "hurt", "damage", "danger")
        val hasDangerousKeywords = dangerousKeywords.any { keyword ->
            keywords.any { it.contains(keyword, ignoreCase = true) }
        }
        
        return if (hasDangerousKeywords) 0.2f else 0.8f
    }
    
    /**
     * Extract emotional tone from messages
     */
    private fun extractEmotionalTone(messages: List<ChatMessage>): String {
        val recentMessages = messages.takeLast(5)
        val emotions = recentMessages.mapNotNull { message ->
            message.emotionTags?.firstOrNull()
        }.filterNotNull()
        
        return when {
            emotions.all { it == "anxious" } -> "anxious"
            emotions.all { it == "sad" } -> "sad"
            emotions.all { it == "angry" } -> "angry"
            emotions.all { it == "happy" } -> "positive"
            emotions.isEmpty() -> "neutral"
            else -> "mixed"
        }
    }
    
    /**
     * Extract user intent
     */
    private fun extractUserIntent(content: String): String {
        return when {
            content.contains("help") -> "seeking_help"
            content.contains("talk") || content.contains("chat") -> "seeking_conversation"
            content.contains("feel") || content.contains("emotion") -> "exploring_emotions"
            content.contains("worry") || content.contains("anxious") -> "managing_anxiety"
            content.contains("sad") || content.contains("depressed") -> "processing_sadness"
            content.contains("angry") || content.contains("frustrated") -> "venting_frustration"
            else -> "general_checkin"
        }
    }
    
    /**
     * Extract themes from conversation
     */
    private fun extractThemes(messages: List<ChatMessage>): List<String> {
        val themes = mutableSetOf<String>()
        
        messages.forEach { message ->
            when (message.sender) {
                ChatSender.USER -> {
                    if (message.content.contains("work")) themes.add("work_stress")
                    if (message.content.contains("family")) themes.add("family_concerns")
                    if (message.content.contains("relationship")) themes.add("relationship_issues")
                    if (message.content.contains("sleep")) themes.add("sleep_problems")
                    if (message.content.contains("anxiety")) themes.add("anxiety_management")
                }
                ChatSender.AI -> {
                    if (message.content.contains("breathing")) themes.add("coping_techniques")
                    if (message.content.contains("meditation")) themes.add("mindfulness_practice")
                    if (message.content.contains("therapy")) themes.add("professional_guidance")
                }
            }
        }
        
        return themes.toList()
    }
    
    /**
     * Get time of day context
     */
    private fun getTimeOfDayContext(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
    }
    
    /**
     * Get session count
     */
    private fun getSessionCount(messages: List<ChatMessage>): Int {
        return messages.count { it.sender == ChatSender.USER }
    }
    
    /**
     * Generate unique message ID
     */
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Load chat history
     */
    private suspend fun loadChatHistory() {
        try {
            val history = chatRepository.getRecentMessages(50)
            _messages.value = history
        } catch (e: Exception) {
            // Start fresh if history fails to load
        }
    }
    
    /**
     * Save chat state
     */
    private suspend fun saveChatState() {
        try {
            val stateJson = json.encodeToString(ChatState.serializer(), _chatState.value)
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("chat_state")] = stateJson
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }
    
    private fun stringPreferencesKey(key: String): Preferences.Key<String> {
        return stringPreferencesKey(key)
    }
}

/**
 * Chat state for persistence
 */
data class ChatState(
    val currentSessionId: String? = null,
    val lastActivity: Long = System.currentTimeMillis()
)

/**
 * LLM API response
 */
data class LLMResponse(
    val content: String,
    val emotionAnalysis: String,
    val safetyScore: Float,
    val requiresImmediateAction: Boolean
)

/**
 * Conversation context for better AI responses
 */
data class ConversationContext(
    val recentMessages: List<ChatMessage>,
    val currentEmotionalTone: String,
    val timeOfDay: String,
    val sessionCount: Int,
    val userIntent: String
)
