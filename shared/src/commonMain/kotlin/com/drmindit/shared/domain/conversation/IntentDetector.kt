package com.drmindit.shared.domain.conversation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intent Detection System
 * Analyzes user messages to determine conversation intent and emotional tone
 */
@Singleton
class IntentDetector @Inject constructor() {
    
    /**
     * Detect user intent from message
     */
    fun detectIntent(message: String, userProfile: UserProfile): UserIntent {
        val normalizedMessage = message.lowercase().trim()
        
        // Check for crisis indicators first (highest priority)
        if (containsCrisisKeywords(normalizedMessage)) {
            return UserIntent.CRISIS_HELP
        }
        
        // Check for specific intents in order of priority
        return when {
            containsAnxietyKeywords(normalizedMessage) -> UserIntent.ANXIETY_HELP
            containsOverthinkingKeywords(normalizedMessage) -> UserIntent.OVERTHINKING_HELP
            containsLowMoodKeywords(normalizedMessage) -> UserIntent.LOW_MOOD_SUPPORT
            containsSleepKeywords(normalizedMessage) -> UserIntent.SLEEP_HELP
            containsStressKeywords(normalizedMessage) -> UserIntent.STRESS_RELIEF
            containsCheckinKeywords(normalizedMessage) -> UserIntent.CHECKIN
            containsResourceKeywords(normalizedMessage) -> UserIntent.RESOURCE_REQUEST
            containsProgressKeywords(normalizedMessage) -> UserIntent.PROGRESS_UPDATE
            containsFeedbackKeywords(normalizedMessage) -> UserIntent.FEEDBACK
            else -> UserIntent.GENERAL_CHAT
        }
    }
    
    /**
     * Detect emotional tone from message
     */
    fun detectEmotionalTone(message: String): EmotionalTone {
        val normalizedMessage = message.lowercase().trim()
        
        return when {
            containsPositiveKeywords(normalizedMessage) -> EmotionalTone.POSITIVE
            containsAnxiousKeywords(normalizedMessage) -> EmotionalTone.ANXIOUS
            containsSadKeywords(normalizedMessage) -> EmotionalTone.SAD
            containsAngryKeywords(normalizedMessage) -> EmotionalTone.ANGRY
            containsConfusedKeywords(normalizedMessage) -> EmotionalTone.CONFUSED
            containsStressedKeywords(normalizedMessage) -> EmotionalTone.STRESSED
            containsExhaustedKeywords(normalizedMessage) -> EmotionalTone.EXHAUSTED
            containsPanickedKeywords(normalizedMessage) -> EmotionalTone.PANICKED
            containsCalmKeywords(normalizedMessage) -> EmotionalTone.CALM
            containsMotivatedKeywords(normalizedMessage) -> EmotionalTone.MOTIVATED
            containsHopefulKeywords(normalizedMessage) -> EmotionalTone.HOPEFUL
            else -> EmotionalTone.NEUTRAL
        }
    }
    
    /**
     * Extract keywords from message
     */
    fun extractKeywords(message: String): List<String> {
        val normalizedMessage = message.lowercase().trim()
        val keywords = mutableListOf<String>()
        
        // Extract emotion keywords
        allEmotionKeywords.forEach { (emotion, words) ->
            words.forEach { word ->
                if (normalizedMessage.contains(word)) {
                    keywords.add(word)
                }
            }
        }
        
        // Extract intent keywords
        allIntentKeywords.forEach { (intent, words) ->
            words.forEach { word ->
                if (normalizedMessage.contains(word)) {
                    keywords.add(word)
                }
            }
        }
        
        return keywords.distinct()
    }
    
    /**
     * Extract entities from message
     */
    fun extractEntities(message: String): Map<String, String> {
        val entities = mutableMapOf<String, String>()
        val normalizedMessage = message.lowercase().trim()
        
        // Extract time references
        timePatterns.forEach { (pattern, entityType) ->
            val regex = Regex(pattern)
            regex.findAll(normalizedMessage).forEach { match ->
                entities[entityType] = match.value
            }
        }
        
        // Extract numbers
        val numberRegex = Regex("\\d+")
        numberRegex.findAll(normalizedMessage).forEach { match ->
            entities["number"] = match.value
        }
        
        // Extract durations
        if (normalizedMessage.contains("minute") || normalizedMessage.contains("hour")) {
            val durationRegex = Regex("(\\d+)\\s*(minute|hour|day|week)")
            durationRegex.findAll(normalizedMessage).forEach { match ->
                entities["duration"] = match.value
            }
        }
        
        return entities
    }
    
    /**
     * Calculate confidence score for intent detection
     */
    fun calculateConfidence(message: String, detectedIntent: UserIntent): Float {
        val normalizedMessage = message.lowercase().trim()
        val intentKeywords = allIntentKeywords[detectedIntent] ?: emptyList()
        
        val matchedKeywords = intentKeywords.count { keyword ->
            normalizedMessage.contains(keyword)
        }
        
        val totalKeywords = intentKeywords.size
        val messageLength = normalizedMessage.split(" ").size
        
        // Calculate confidence based on keyword matches and message context
        val keywordConfidence = if (totalKeywords > 0) {
            matchedKeywords.toFloat() / totalKeywords.toFloat()
        } else 0f
        
        val contextConfidence = when {
            messageLength >= 10 -> 0.8f
            messageLength >= 5 -> 0.6f
            else -> 0.4f
        }
        
        return (keywordConfidence + contextConfidence) / 2f
    }
    
    // Crisis detection keywords (highest priority)
    private fun containsCrisisKeywords(message: String): Boolean {
        val crisisKeywords = listOf(
            "suicide", "kill myself", "end my life", "want to die",
            "hurt myself", "self harm", "cut myself", "overdose",
            "can't go on", "no way out", "giving up", "ending it all",
            "emergency", "crisis", "danger", "harm", "die", "death",
            "don't want to live", "better off dead", "no reason to live"
        )
        
        return crisisKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Anxiety keywords
    private fun containsAnxietyKeywords(message: String): Boolean {
        val anxietyKeywords = listOf(
            "anxious", "anxiety", "worried", "worry", "nervous", "panic",
            "panic attack", "racing heart", "can't breathe", "chest tight",
            "overwhelmed", "fearful", "scared", "afraid", "terrified",
            "restless", "on edge", "tense", "uneasy", "apprehensive"
        )
        
        return anxietyKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Overthinking keywords
    private fun containsOverthinkingKeywords(message: String): Boolean {
        val overthinkingKeywords = listOf(
            "overthinking", "rumination", "obsessing", "stuck in my head",
            "can't stop thinking", "racing thoughts", "thoughts won't stop",
            "looping thoughts", "mental loop", "stuck thinking", "over analyzing",
            "what if", "should have", "could have", "if only", "replaying"
        )
        
        return overthinkingKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Low mood keywords
    private fun containsLowMoodKeywords(message: String): Boolean {
        val lowMoodKeywords = listOf(
            "depressed", "depression", "sad", "down", "blue", "low mood",
            "hopeless", "helpless", "worthless", "empty", "numb",
            "no energy", "tired", "exhausted", "can't get out of bed",
            "no motivation", "nothing matters", "what's the point"
        )
        
        return lowMoodKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Sleep keywords
    private fun containsSleepKeywords(message: String): Boolean {
        val sleepKeywords = listOf(
            "sleep", "insomnia", "can't sleep", "trouble sleeping",
            "awake all night", "sleepless", "restless", "nightmare",
            "tired", "exhausted", "fatigue", "drowsy", "sleepy",
            "bedtime", "wake up", "sleep quality", "sleep disorder"
        )
        
        return sleepKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Stress keywords
    private fun containsStressKeywords(message: String): Boolean {
        val stressKeywords = listOf(
            "stress", "stressed", "overwhelmed", "pressure", "burnout",
            "too much", "can't handle", "swamped", "burdened",
            "work stress", "life stress", "stressful", "tension"
        )
        
        return stressKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Check-in keywords
    private fun containsCheckinKeywords(message: String): Boolean {
        val checkinKeywords = listOf(
            "check in", "how am i", "mood", "feeling today",
            "daily check", "status update", "how are you", "current mood",
            "today's mood", "feeling now", "current state"
        )
        
        return checkinKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Resource keywords
    private fun containsResourceKeywords(message: String): Boolean {
        val resourceKeywords = listOf(
            "resource", "tool", "help", "guide", "information",
            "recommendation", "suggestion", "advice", "tips",
            "techniques", "strategies", "methods", "approach"
        )
        
        return resourceKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Progress keywords
    private fun containsProgressKeywords(message: String): Boolean {
        val progressKeywords = listOf(
            "progress", "improvement", "better", "worse", "change",
            "journey", "growth", "development", "advancement",
            "moving forward", "stuck", "regression", "setback"
        )
        
        return progressKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Feedback keywords
    private fun containsFeedbackKeywords(message: String): Boolean {
        val feedbackKeywords = listOf(
            "feedback", "opinion", "thoughts", "review", "rating",
            "suggestion", "improvement", "better", "worse", "experience"
        )
        
        return feedbackKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Positive emotion keywords
    private fun containsPositiveKeywords(message: String): Boolean {
        val positiveKeywords = listOf(
            "happy", "glad", "excited", "joyful", "cheerful", "delighted",
            "pleased", "satisfied", "content", "grateful", "thankful",
            "optimistic", "hopeful", "confident", "proud", "energetic",
            "amazing", "wonderful", "great", "fantastic", "excellent"
        )
        
        return positiveKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Sad emotion keywords
    private fun containsSadKeywords(message: String): Boolean {
        val sadKeywords = listOf(
            "sad", "depressed", "down", "blue", "unhappy", "miserable",
            "heartbroken", "devastated", "grief", "sorrow", "disappointed",
            "let down", "discouraged", "hopeless", "despair", "crying"
        )
        
        return sadKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Angry emotion keywords
    private fun containsAngryKeywords(message: String): Boolean {
        val angryKeywords = listOf(
            "angry", "mad", "furious", "enraged", "irritated", "frustrated",
            "annoyed", "upset", "outraged", "resentful", "bitter",
            "hostile", "aggressive", "violent", "rage", "fuming"
        )
        
        return angryKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Confused emotion keywords
    private fun containsConfusedKeywords(message: String): Boolean {
        val confusedKeywords = listOf(
            "confused", "uncertain", "unsure", "unclear", "puzzled",
            "bewildered", "disoriented", "lost", "clueless", "perplexed"
        )
        
        return confusedKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Stressed emotion keywords
    private fun containsStressedKeywords(message: String): Boolean {
        val stressedKeywords = listOf(
            "stressed", "overwhelmed", "pressured", "burdened", "strained",
            "tense", "anxious", "worried", "nervous", "agitated"
        )
        
        return stressedKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Exhausted emotion keywords
    private fun containsExhaustedKeywords(message: String): Boolean {
        val exhaustedKeywords = listOf(
            "exhausted", "tired", "fatigued", "drained", "worn out",
            "burned out", "weary", "spent", "depleted", "run down"
        )
        
        return exhaustedKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Panicked emotion keywords
    private fun containsPanickedKeywords(message: String): Boolean {
        val panickedKeywords = listOf(
            "panicked", "panic", "terrified", "horrified", "scared to death",
            "frightened", "alarmed", "shocked", "stunned", "dismayed"
        )
        
        return panickedKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Calm emotion keywords
    private fun containsCalmKeywords(message: String): Boolean {
        val calmKeywords = listOf(
            "calm", "peaceful", "relaxed", "tranquil", "serene",
            "composed", "collected", "untroubled", "at ease", "restful"
        )
        
        return calmKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Motivated emotion keywords
    private fun containsMotivatedKeywords(message: String): Boolean {
        val motivatedKeywords = listOf(
            "motivated", "determined", "driven", "ambitious", "focused",
            "energized", "inspired", "enthusiastic", "passionate", "committed"
        )
        
        return motivatedKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Hopeful emotion keywords
    private fun containsHopefulKeywords(message: String): Boolean {
        val hopefulKeywords = listOf(
            "hopeful", "optimistic", "positive", "encouraged", "confident",
            "buoyant", "upbeat", "cheerful", "bright", "promising"
        )
        
        return hopefulKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    // Comprehensive keyword mappings
    companion object {
        val allIntentKeywords = mapOf(
            UserIntent.ANXIETY_HELP to listOf(
                "anxious", "anxiety", "worried", "worry", "nervous", "panic",
                "panic attack", "overwhelmed", "fearful", "scared", "afraid"
            ),
            UserIntent.OVERTHINKING_HELP to listOf(
                "overthinking", "rumination", "obsessing", "stuck in my head",
                "can't stop thinking", "racing thoughts", "mental loop"
            ),
            UserIntent.LOW_MOOD_SUPPORT to listOf(
                "depressed", "depression", "sad", "down", "blue", "low mood",
                "hopeless", "helpless", "worthless", "empty", "numb"
            ),
            UserIntent.SLEEP_HELP to listOf(
                "sleep", "insomnia", "can't sleep", "trouble sleeping",
                "awake all night", "sleepless", "restless", "nightmare"
            ),
            UserIntent.STRESS_RELIEF to listOf(
                "stress", "stressed", "overwhelmed", "pressure", "burnout",
                "too much", "can't handle", "swamped", "burdened"
            ),
            UserIntent.CHECKIN to listOf(
                "check in", "how am i", "mood", "feeling today",
                "daily check", "status update", "how are you"
            ),
            UserIntent.RESOURCE_REQUEST to listOf(
                "resource", "tool", "help", "guide", "information",
                "recommendation", "suggestion", "advice", "tips"
            ),
            UserIntent.PROGRESS_UPDATE to listOf(
                "progress", "improvement", "better", "worse", "change",
                "journey", "growth", "development", "advancement"
            ),
            UserIntent.FEEDBACK to listOf(
                "feedback", "opinion", "thoughts", "review", "rating",
                "suggestion", "improvement", "experience"
            ),
            UserIntent.CRISIS_HELP to listOf(
                "suicide", "kill myself", "end my life", "want to die",
                "hurt myself", "self harm", "can't go on", "emergency"
            )
        )
        
        val allEmotionKeywords = mapOf(
            EmotionalTone.POSITIVE to listOf(
                "happy", "glad", "excited", "joyful", "cheerful", "delighted",
                "pleased", "satisfied", "content", "grateful", "thankful"
            ),
            EmotionalTone.ANXIOUS to listOf(
                "anxious", "anxiety", "worried", "worry", "nervous", "panic",
                "fearful", "scared", "afraid", "terrified", "restless"
            ),
            EmotionalTone.SAD to listOf(
                "sad", "depressed", "down", "blue", "unhappy", "miserable",
                "heartbroken", "devastated", "grief", "sorrow", "disappointed"
            ),
            EmotionalTone.ANGRY to listOf(
                "angry", "mad", "furious", "enraged", "irritated", "frustrated",
                "annoyed", "upset", "outraged", "resentful", "bitter"
            ),
            EmotionalTone.CONFUSED to listOf(
                "confused", "uncertain", "unsure", "unclear", "puzzled",
                "bewildered", "disoriented", "lost", "clueless", "perplexed"
            ),
            EmotionalTone.STRESSED to listOf(
                "stressed", "overwhelmed", "pressured", "burdened", "strained",
                "tense", "anxious", "worried", "nervous", "agitated"
            ),
            EmotionalTone.EXHAUSTED to listOf(
                "exhausted", "tired", "fatigued", "drained", "worn out",
                "burned out", "weary", "spent", "depleted", "run down"
            ),
            EmotionalTone.PANICKED to listOf(
                "panicked", "panic", "terrified", "horrified", "scared to death",
                "frightened", "alarmed", "shocked", "stunned", "dismayed"
            ),
            EmotionalTone.CALM to listOf(
                "calm", "peaceful", "relaxed", "tranquil", "serene",
                "composed", "collected", "untroubled", "at ease", "restful"
            ),
            EmotionalTone.MOTIVATED to listOf(
                "motivated", "determined", "driven", "ambitious", "focused",
                "energized", "inspired", "enthusiastic", "passionate", "committed"
            ),
            EmotionalTone.HOPEFUL to listOf(
                "hopeful", "optimistic", "positive", "encouraged", "confident",
                "buoyant", "upbeat", "cheerful", "bright", "promising"
            )
        )
        
        val timePatterns = mapOf(
            "morning" to "time",
            "afternoon" to "time",
            "evening" to "time",
            "night" to "time",
            "today" to "time",
            "yesterday" to "time",
            "tomorrow" to "time",
            "last week" to "time",
            "next week" to "time",
            "\\d+\\s*(minute|hour|day|week)" to "duration"
        )
    }
}
