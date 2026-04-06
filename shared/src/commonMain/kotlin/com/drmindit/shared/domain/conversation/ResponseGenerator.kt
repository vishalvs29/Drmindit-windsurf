package com.drmindit.shared.domain.conversation

import com.drmindit.shared.domain.model.*
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Response Generator
 * Generates contextual responses based on user intent and conversation state
 */
class ResponseGenerator {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val random = Random.Default
    
    /**
     * Generate response for user message
     */
    fun generateResponse(
        intent: ConversationIntent,
        context: ConversationContext,
        sentiment: Sentiment,
        riskLevel: RiskLevel
    ): GeneratedResponse {
        
        val baseResponse = getBaseResponse(intent, sentiment)
        val suggestions = getSuggestions(intent)
        val followUpQuestions = getFollowUpQuestions(intent)
        val sessionRecommendations = getSessionRecommendations(intent)
        
        return GeneratedResponse(
            message = baseResponse,
            confidence = calculateConfidence(intent, context),
            suggestions = suggestions,
            followUpQuestions = followUpQuestions,
            sessionRecommendations = sessionRecommendations,
            riskLevel = riskLevel,
            requiresEscalation = riskLevel == RiskLevel.CRITICAL,
            metadata = mapOf(
                "intent" to intent.name,
                "sentiment" to sentiment.name,
                "risk_level" to riskLevel.name,
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }
    
    /**
     * Generate crisis response
     */
    fun generateCrisisResponse(riskLevel: RiskLevel): GeneratedResponse {
        val message = when (riskLevel) {
            RiskLevel.CRITICAL -> "I'm very concerned about your safety. Please reach out to a crisis helpline immediately. Your life matters."
            RiskLevel.HIGH -> "I understand you're going through a difficult time. Let me connect you with support resources."
            RiskLevel.MEDIUM -> "It sounds like you're dealing with a lot. I'm here to help you work through this."
            RiskLevel.LOW -> "I'm here to support you. Let's talk about what's on your mind."
        }
        
        val helplines = listOf(
            EmergencyHelpline(
                id = "988",
                name = "988 Suicide & Crisis Lifeline",
                phoneNumber = "988",
                country = "US"
            ),
            EmergencyHelpline(
                id = "crisis-text",
                name = "Crisis Text Line",
                phoneNumber = "741741",
                country = "US"
            )
        )
        
        return GeneratedResponse(
            message = message,
            confidence = 0.95f,
            suggestions = listOf("Call 988", "Text HOME to 741741", "Talk to a therapist"),
            followUpQuestions = emptyList(),
            sessionRecommendations = emptyList(),
            riskLevel = riskLevel,
            requiresEscalation = riskLevel == RiskLevel.CRITICAL,
            metadata = mapOf(
                "crisis_response" to "true",
                "risk_level" to riskLevel.name
            )
        )
    }
    
    private fun getBaseResponse(intent: ConversationIntent, sentiment: Sentiment): String {
        val responses = mapOf(
            ConversationIntent.STRESS_RELIEF to mapOf(
                Sentiment.NEGATIVE to listOf(
                    "I understand you're feeling stressed. Let's work together to find some relief.",
                    "Stress can be overwhelming. I'm here to help you through this.",
                    "It sounds like you're under a lot of pressure. Let's take a moment to breathe."
                ),
                Sentiment.NEUTRAL to listOf(
                    "Stress management is an important skill. What's been on your mind?",
                    "I can help you develop stress coping strategies. Where would you like to start?"
                ),
                Sentiment.POSITIVE to listOf(
                    "It's great that you're being proactive about stress management!",
                    "Taking care of stress is important. How can I support you?"
                )
            ),
            ConversationIntent.SLEEP_SUPPORT to mapOf(
                Sentiment.NEGATIVE to listOf(
                    "Sleep issues can be really challenging. I'm here to help you find solutions.",
                    "I understand how frustrating sleep problems can be. Let's work on this together."
                ),
                Sentiment.NEUTRAL to listOf(
                    "Good sleep is essential for wellness. What sleep challenges are you facing?",
                    "I can help you improve your sleep quality. What would you like to focus on?"
                ),
                Sentiment.POSITIVE to listOf(
                    "Working on sleep health is wonderful! How can I support your goals?"
                )
            ),
            ConversationIntent.EMOTIONAL_SUPPORT to mapOf(
                Sentiment.NEGATIVE to listOf(
                    "It's brave to share your feelings. I'm here to listen and support you.",
                    "Thank you for trusting me with your emotions. You're not alone in this.",
                    "Your feelings are valid. Let's work through this together."
                ),
                Sentiment.NEUTRAL to listOf(
                    "I'm here to help you explore your emotions. What's on your heart?",
                    "Emotional wellness is a journey. How can I support you today?"
                ),
                Sentiment.POSITIVE to listOf(
                    "It's wonderful that you're in touch with your emotions!",
                    "Your emotional awareness is impressive. How can I help you grow?"
                )
            ),
            ConversationIntent.MEDITATION_GUIDE to mapOf(
                Sentiment.NEUTRAL to listOf(
                    "Meditation is a powerful practice. Let me guide you through it.",
                    "I'd love to help you with meditation. Are you new to the practice?"
                ),
                Sentiment.POSITIVE to listOf(
                    "Your interest in meditation is wonderful! Let's find the right approach for you."
                )
            ),
            ConversationIntent.WELLNESS_CHECK to mapOf(
                Sentiment.NEUTRAL to listOf(
                    "Let's check in on your overall wellness. How have you been feeling?",
                    "I'm here to support your wellness journey. What would you like to explore?"
                ),
                Sentiment.POSITIVE to listOf(
                    "It's great that you're focusing on wellness! How can I help you thrive?"
                )
            )
        )
        
        val intentResponses = responses[intent] ?: mapOf(
            Sentiment.NEUTRAL to listOf("I'm here to help. What would you like to talk about?")
        )
        
        val sentimentResponses = intentResponses[sentiment] ?: intentResponses[Sentiment.NEUTRAL] ?: listOf("I'm here to support you.")
        
        return sentimentResponses.random()
    }
    
    private fun getSuggestions(intent: ConversationIntent): List<String> {
        return when (intent) {
            ConversationIntent.STRESS_RELIEF -> listOf(
                "Try deep breathing exercises",
                "Take a short walk",
                "Practice progressive muscle relaxation",
                "Listen to calming music"
            )
            ConversationIntent.SLEEP_SUPPORT -> listOf(
                "Establish a bedtime routine",
                "Try sleep meditation",
                "Practice good sleep hygiene",
                "Consider a warm bath before bed"
            )
            ConversationIntent.EMOTIONAL_SUPPORT -> listOf(
                "Practice self-compassion",
                "Write in a journal",
                "Talk to a trusted friend",
                "Engage in a hobby you enjoy"
            )
            ConversationIntent.MEDITATION_GUIDE -> listOf(
                "Start with 5-minute meditation",
                "Focus on your breath",
                "Try body scan meditation",
                "Use a guided meditation app"
            )
            ConversationIntent.WELLNESS_CHECK -> listOf(
                "Check your mood daily",
                "Practice gratitude",
                "Stay hydrated",
                "Get regular exercise"
            )
            ConversationIntent.CRISES_INTERVENTION -> listOf(
                "Call 988 Suicide & Crisis Lifeline",
                "Text HOME to 741741",
                "Contact emergency services",
                "Reach out to a trusted person"
            )
            else -> listOf(
                "Take a moment to breathe",
                "Practice mindfulness",
                "Connect with nature",
                "Listen to calming music"
            )
        }
    }
    
    private fun getFollowUpQuestions(intent: ConversationIntent): List<String> {
        return when (intent) {
            ConversationIntent.STRESS_RELIEF -> listOf(
                "What's been causing you the most stress lately?",
                "Have you tried any stress management techniques before?",
                "How does stress typically affect your daily life?"
            )
            ConversationIntent.SLEEP_SUPPORT -> listOf(
                "How long have you been having sleep issues?",
                "What does your current bedtime routine look like?",
                "How do you feel when you wake up in the morning?"
            )
            ConversationIntent.EMOTIONAL_SUPPORT -> listOf(
                "How long have you been feeling this way?",
                "What triggered these feelings?",
                "What would make you feel even a little better right now?"
            )
            ConversationIntent.MEDITATION_GUIDE -> listOf(
                "Have you meditated before?",
                "What are you hoping to achieve with meditation?",
                "How much time can you dedicate to practice?"
            )
            ConversationIntent.WELLNESS_CHECK -> listOf(
                "How has your energy been lately?",
                "Are there any areas of wellness you'd like to improve?",
                "What does self-care look like for you?"
            )
            else -> emptyList()
        }
    }
    
    private fun getSessionRecommendations(intent: ConversationIntent): List<String> {
        return when (intent) {
            ConversationIntent.STRESS_RELIEF -> listOf(
                "Stress Relief Meditation",
                "Deep Breathing Exercise",
                "Progressive Muscle Relaxation"
            )
            ConversationIntent.SLEEP_SUPPORT -> listOf(
                "Bedtime Meditation",
                "Sleep Stories",
                "Body Scan for Sleep"
            )
            ConversationIntent.EMOTIONAL_SUPPORT -> listOf(
                "Self-Compassion Meditation",
                "Emotional Regulation",
                "Loving-Kindness Meditation"
            )
            ConversationIntent.MEDITATION_GUIDE -> listOf(
                "Beginner's Meditation",
                "Mindfulness Basics",
                "Breath Awareness"
            )
            ConversationIntent.WELLNESS_CHECK -> listOf(
                "Daily Wellness Check",
                "Gratitude Practice",
                "Mindful Movement"
            )
            else -> emptyList()
        }
    }
    
    private fun calculateConfidence(intent: ConversationIntent, context: ConversationContext): Float {
        val baseConfidence = 0.8f
        val contextBonus = if (context.step > 0) 0.1f else 0.0f
        val intentBonus = when (intent) {
            ConversationIntent.CRISES_INTERVENTION -> 0.15f
            ConversationIntent.STRESS_RELIEF -> 0.1f
            ConversationIntent.EMOTIONAL_SUPPORT -> 0.1f
            else -> 0.05f
        }
        
        return (baseConfidence + contextBonus + intentBonus).coerceAtMost(1.0f)
    }
}

// Supporting data classes
data class GeneratedResponse(
    val message: String,
    val confidence: Float,
    val suggestions: List<String>,
    val followUpQuestions: List<String>,
    val sessionRecommendations: List<String>,
    val riskLevel: RiskLevel,
    val requiresEscalation: Boolean,
    val metadata: Map<String, String>
)
