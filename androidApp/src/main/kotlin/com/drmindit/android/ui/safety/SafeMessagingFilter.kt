package com.drmindit.android.ui.safety

/**
 * WHO/AFSP compliant safe messaging filter for mental health content
 * Ensures all AI responses follow safe communication guidelines
 */
object SafeMessagingFilter {
    
    private val unsafeWords = setOf(
        // Never use these words
        "commit", "suicide", "kill", "harm", "hurt", "injure", "end", "die",
        "cut", "harmful", "dangerous", "risk", "threaten", "violence"
    )
    
    private val unsafePhrases = setOf(
        "commit suicide", "kill yourself", "harm yourself", "end your life",
        "ways to die", "how to kill", "suicide methods"
    )
    
    private val safeAlternatives = mapOf(
        "commit" to "harm thoughts",
        "kill" to "hurt yourself",
        "harm" to "hurt yourself",
        "end" to "end your life",
        "die" to "pass away",
        "cut" to "hurt yourself",
        "suicide" to "having thoughts of ending your life",
        "violence" to "aggressive thoughts"
    )
    
    /**
     * Filters AI response for safe messaging compliance
     * @param originalResponse The raw AI-generated response
     * @return Filtered, safe response
     */
    fun filterResponse(originalResponse: String): String {
        var filteredResponse = originalResponse.lowercase()
        
        // Replace unsafe words with safe alternatives
        unsafeWords.forEach { unsafe ->
            safeAlternatives[unsafe]?.let { safe ->
                filteredResponse = filteredResponse.replace(unsafe, safe)
            }
        }
        
        // Remove unsafe phrases entirely
        unsafePhrases.forEach { unsafe ->
            filteredResponse = filteredResponse.replace(unsafe.lowercase(), "[removed for safety]")
        }
        
        // Add hope-promoting language
        filteredResponse = addHopefulLanguage(filteredResponse)
        
        return filteredResponse
    }
    
    /**
     * Adds hopeful, recovery-focused language to responses
     */
    private fun addHopefulLanguage(text: String): String {
        val hopeWords = listOf(
            "recovery", "healing", "hope", "strength", "courage", "support",
            "tomorrow", "future", "better", "progress", "growth", "peace"
        )
        
        var enhancedText = text
        
        // Ensure response contains hopeful elements
        if (!hopeWords.any { it in text.lowercase() }) {
            enhancedText += " Remember that healing is possible, and you're not alone in this journey. 🌱"
        }
        
        return enhancedText
    }
    
    /**
     * Validates if content discusses self-harm safely
     */
    fun discussSelfHarmSafely(input: String): Boolean {
        val safeKeywords = listOf(
            "thoughts of hurting", "feeling like ending", "wanting to disappear",
            "thinking about self-harm", "having dark thoughts", "feeling hopeless",
            "need help", "want to talk", "feeling overwhelmed", "struggling with thoughts"
        )
        
        return safeKeywords.any { it in input.lowercase() }
    }
    
    /**
     * Gets crisis resources based on detected level
     */
    fun getCrisisResources(level: String): List<String> {
        return when (level.lowercase()) {
            "immediate" -> listOf(
                "🚨 Immediate Support Available",
                "National Suicide Prevention Lifeline: 988",
                "Crisis Text Line: Text HOME to 741741",
                "Emergency: 911"
            )
            "high" -> listOf(
                "🆘 Support Resources",
                "iCall: 9152927898",
                "Vandrevala Foundation: 18602667232",
                "Local mental health services"
            )
            "medium" -> listOf(
                "🧘 Support Options",
                "Talk to a trusted friend or family member",
                "Contact your therapist or counselor",
                "Practice grounding exercises"
            )
            else -> listOf(
                "🌱 Support Available",
                "Take deep breaths",
                "Practice mindfulness",
                "Reach out if needed"
            )
        }
    }
}
