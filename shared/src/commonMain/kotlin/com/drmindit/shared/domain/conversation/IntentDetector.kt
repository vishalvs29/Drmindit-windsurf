package com.drmindit.shared.domain.conversation

import com.drmindit.shared.domain.model.*
import kotlinx.serialization.json.Json

/**
 * Intent Detector
 * Analyzes user messages to determine conversation intent
 */
class IntentDetector {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Detect primary intent from user message
     */
    fun detectIntent(message: String): IntentResult {
        val processedMessage = message.lowercase().trim()
        
        // Check for crisis indicators first
        val crisisIntent = detectCrisisIntent(processedMessage)
        if (crisisIntent != null) {
            return crisisIntent
        }
        
        // Check for specific intents
        val intents = listOf(
            detectStressIntent(processedMessage),
            detectAnxietyIntent(processedMessage),
            detectSleepIntent(processedMessage),
            detectMeditationIntent(processedMessage),
            detectEmotionalIntent(processedMessage),
            detectWellnessIntent(processedMessage)
        )
        
        // Return highest confidence intent
        return intents.maxByOrNull { it.confidence } ?: IntentResult(
            intent = ConversationIntent.GENERAL_CHAT,
            confidence = 0.5f,
            entities = emptyMap(),
            sentiment = detectSentiment(processedMessage)
        )
    }
    
    /**
     * Detect multiple possible intents
     */
    fun detectMultipleIntents(message: String): List<IntentResult> {
        val processedMessage = message.lowercase().trim()
        
        return listOf(
            detectCrisisIntent(processedMessage),
            detectStressIntent(processedMessage),
            detectAnxietyIntent(processedMessage),
            detectSleepIntent(processedMessage),
            detectMeditationIntent(processedMessage),
            detectEmotionalIntent(processedMessage),
            detectWellnessIntent(processedMessage)
        ).filterNotNull().sortedByDescending { it.confidence }
    }
    
    private fun detectCrisisIntent(message: String): IntentResult? {
        val crisisKeywords = listOf(
            "suicide", "kill myself", "end my life", "want to die",
            "panic attack", "emergency", "crisis", "can't breathe",
            "overwhelmed", "breaking point", "can't take it anymore"
        )
        
        val foundKeywords = crisisKeywords.filter { message.contains(it) }
        
        return if (foundKeywords.isNotEmpty()) {
            IntentResult(
                intent = ConversationIntent.CRISES_INTERVENTION,
                confidence = if (foundKeywords.size >= 2) 0.95f else 0.85f,
                entities = mapOf("crisis_keywords" to foundKeywords.joinToString(",")),
                sentiment = Sentiment.NEGATIVE
            )
        } else null
    }
    
    private fun detectStressIntent(message: String): IntentResult {
        val stressKeywords = listOf("stress", "stressed", "overwhelmed", "pressure", "work", "deadline")
        val foundKeywords = stressKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.STRESS_RELIEF,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("stress_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectAnxietyIntent(message: String): IntentResult {
        val anxietyKeywords = listOf("anxious", "anxiety", "worry", "worried", "panic", "nervous")
        val foundKeywords = anxietyKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.ANXIETY_MANAGEMENT,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("anxiety_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectSleepIntent(message: String): IntentResult {
        val sleepKeywords = listOf("sleep", "insomnia", "tired", "exhausted", "night", "bedtime")
        val foundKeywords = sleepKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.SLEEP_SUPPORT,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("sleep_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectMeditationIntent(message: String): IntentResult {
        val meditationKeywords = listOf("meditation", "meditate", "mindful", "mindfulness", "breathing")
        val foundKeywords = meditationKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.MEDITATION_GUIDE,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("meditation_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectEmotionalIntent(message: String): IntentResult {
        val emotionalKeywords = listOf("sad", "depressed", "happy", "angry", "frustrated", "lonely")
        val foundKeywords = emotionalKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.EMOTIONAL_SUPPORT,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("emotional_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectWellnessIntent(message: String): IntentResult {
        val wellnessKeywords = listOf("health", "wellness", "exercise", "diet", "routine", "habits")
        val foundKeywords = wellnessKeywords.filter { message.contains(it) }
        
        return IntentResult(
            intent = ConversationIntent.WELLNESS_CHECK,
            confidence = 0.3f + (foundKeywords.size * 0.2f),
            entities = mapOf("wellness_keywords" to foundKeywords.joinToString(",")),
            sentiment = detectSentiment(message)
        )
    }
    
    private fun detectSentiment(message: String): Sentiment {
        val positiveWords = listOf("good", "great", "happy", "excellent", "wonderful", "amazing")
        val negativeWords = listOf("bad", "terrible", "sad", "angry", "frustrated", "worried")
        
        val positiveCount = positiveWords.count { message.contains(it) }
        val negativeCount = negativeWords.count { message.contains(it) }
        
        return when {
            positiveCount > negativeCount -> Sentiment.POSITIVE
            negativeCount > positiveCount -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
}

// Supporting data classes
data class IntentResult(
    val intent: ConversationIntent,
    val confidence: Float,
    val entities: Map<String, String>,
    val sentiment: Sentiment
)

