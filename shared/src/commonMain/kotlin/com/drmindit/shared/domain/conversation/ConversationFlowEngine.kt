package com.drmindit.shared.domain.conversation

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import com.drmindit.shared.domain.model.*

/**
 * Conversation Flow Engine
 * Manages conversation states, flows, and transitions
 */
class ConversationFlowEngine {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    
    private val _currentContext = MutableStateFlow<ConversationContext>(
        ConversationContext(
            state = ConversationState.GENERAL_CHAT,
            step = 0,
            maxSteps = 1,
            data = emptyMap(),
            metadata = emptyMap()
        )
    )
    val currentContext = _currentContext.asStateFlow()
    
    /**
     * Process user message and determine next conversation state
     */
    suspend fun processMessage(
        message: String,
        userId: String,
        sessionId: String
    ): Result<ConversationResponse> {
        return try {
            mutex.withLock {
                val context = _currentContext.value
                
                // Detect intent
                val intent = detectIntent(message)
                
                // Check for crisis
                val riskLevel = detectRisk(message)
                
                // Generate response based on intent and context
                val response = generateResponse(intent, context, riskLevel)
                
                // Update conversation context
                val newContext = updateContext(context, intent, response)
                _currentContext.value = newContext
                
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset conversation context
     */
    suspend fun resetContext() {
        mutex.withLock {
            _currentContext.value = ConversationContext(
                state = ConversationState.GENERAL_CHAT,
                step = 0,
                maxSteps = 1,
                data = emptyMap(),
                metadata = emptyMap()
            )
        }
    }
    
    /**
     * Set specific conversation state
     */
    suspend fun setState(state: ConversationState, data: Map<String, String> = emptyMap()) {
        mutex.withLock {
            _currentContext.value = _currentContext.value.copy(
                state = state,
                step = 0,
                data = data
            )
        }
    }
    
    private fun detectIntent(message: String): ConversationIntent {
        val lowerMessage = message.lowercase()
        
        return when {
            lowerMessage.contains("stress") || lowerMessage.contains("anxious") -> ConversationIntent.STRESS_RELIEF
            lowerMessage.contains("sleep") || lowerMessage.contains("tired") -> ConversationIntent.SLEEP_SUPPORT
            lowerMessage.contains("depress") || lowerMessage.contains("sad") -> ConversationIntent.EMOTIONAL_SUPPORT
            lowerMessage.contains("meditation") || lowerMessage.contains("mindful") -> ConversationIntent.MEDITATION_GUIDE
            lowerMessage.contains("help") || lowerMessage.contains("crisis") -> ConversationIntent.CRISES_INTERVENTION
            else -> ConversationIntent.GENERAL_CHAT
        }
    }
    
    private fun detectRisk(message: String): RiskLevel {
        val lowerMessage = message.lowercase()
        
        return when {
            lowerMessage.contains("suicide") || lowerMessage.contains("kill myself") -> RiskLevel.CRITICAL
            lowerMessage.contains("panic") || lowerMessage.contains("emergency") -> RiskLevel.HIGH
            lowerMessage.contains("struggle") || lowerMessage.contains("overwhelm") -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    private fun generateResponse(
        intent: ConversationIntent,
        context: ConversationContext,
        riskLevel: RiskLevel
    ): ConversationResponse {
        
        val baseResponse = when (intent) {
            ConversationIntent.STRESS_RELIEF -> "I understand you're feeling stressed. Let's work together to find some relief."
            ConversationIntent.SLEEP_SUPPORT -> "Sleep issues can be challenging. I can help you with relaxation techniques."
            ConversationIntent.EMOTIONAL_SUPPORT -> "It's brave to reach out. I'm here to support you through this difficult time."
            ConversationIntent.MEDITATION_GUIDE -> "Meditation is a great practice. Let me guide you through a simple exercise."
            ConversationIntent.CRISES_INTERVENTION -> "I'm concerned about your safety. Let me connect you with immediate support."
            ConversationIntent.GENERAL_CHAT -> "I'm here to help. What would you like to talk about today?"
            ConversationIntent.ANXIETY_MANAGEMENT -> "I understand you're feeling anxious. Let's work on some calming techniques together."
            ConversationIntent.WELLNESS_CHECK -> "It's good to check in on your wellness. How have you been feeling lately?"
            ConversationIntent.SESSION_RECOMMENDATION -> "I can help you find the right session for your needs. Let me suggest some options."
        }
        
        val suggestions = when (intent) {
            ConversationIntent.STRESS_RELIEF -> listOf("Deep breathing", "Progressive muscle relaxation", "Mindful walking")
            ConversationIntent.SLEEP_SUPPORT -> listOf("Sleep hygiene tips", "Bedtime meditation", "Body scan exercise")
            ConversationIntent.EMOTIONAL_SUPPORT -> listOf("Practice self-compassion", "Write in a journal", "Engage in a hobby")
            ConversationIntent.MEDITATION_GUIDE -> listOf("Start with 5-minute meditation", "Focus on your breath", "Use a guided meditation app")
            ConversationIntent.CRISES_INTERVENTION -> listOf("Call 988 Suicide & Crisis Lifeline", "Text HOME to 741741", "Contact emergency services")
            ConversationIntent.GENERAL_CHAT -> listOf("Take a moment to breathe", "Practice mindfulness", "Connect with nature")
            ConversationIntent.ANXIETY_MANAGEMENT -> listOf("Try grounding exercises", "Practice progressive muscle relaxation", "Use calming breathing techniques")
            ConversationIntent.WELLNESS_CHECK -> listOf("Check your mood daily", "Practice gratitude", "Stay hydrated and exercise")
            ConversationIntent.SESSION_RECOMMENDATION -> listOf("Browse available sessions", "Try a beginner meditation", "Schedule regular practice")
        }
        
        return ConversationResponse(
            message = baseResponse,
            intent = intent,
            confidence = 0.85f,
            suggestions = suggestions,
            riskLevel = riskLevel,
            requiresEscalation = riskLevel == RiskLevel.CRITICAL,
            metadata = mapOf(
                "timestamp" to System.currentTimeMillis().toString(),
                "intent" to intent.name,
                "risk" to riskLevel.name
            )
        )
    }
    
    private fun updateContext(
        context: ConversationContext,
        intent: ConversationIntent,
        response: ConversationResponse
    ): ConversationContext {
        return context.copy(
            step = context.step + 1,
            data = context.data + ("lastIntent" to intent.name),
            metadata = context.metadata + ("lastResponse" to response.message)
        )
    }
}

// Supporting data classes
data class ConversationContext(
    val state: ConversationState,
    val step: Int,
    val maxSteps: Int,
    val data: Map<String, String>,
    val metadata: Map<String, String>
)

data class ConversationResponse(
    val message: String,
    val intent: ConversationIntent,
    val confidence: Float,
    val suggestions: List<String>,
    val riskLevel: RiskLevel,
    val requiresEscalation: Boolean,
    val metadata: Map<String, String>
)

enum class ConversationState {
    GENERAL_CHAT,
    STRESS_ASSESSMENT,
    ANXIETY_MANAGEMENT,
    SLEEP_IMPROVEMENT,
    MEDITATION_GUIDE,
    CRISES_INTERVENTION,
    EMOTIONAL_SUPPORT,
    WELLNESS_PLANNING
}

enum class ConversationIntent {
    GENERAL_CHAT,
    STRESS_RELIEF,
    ANXIETY_MANAGEMENT,
    SLEEP_SUPPORT,
    EMOTIONAL_SUPPORT,
    MEDITATION_GUIDE,
    CRISES_INTERVENTION,
    WELLNESS_CHECK,
    SESSION_RECOMMENDATION
}
