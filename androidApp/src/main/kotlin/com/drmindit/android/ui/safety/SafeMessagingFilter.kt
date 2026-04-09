package com.drmindit.android.ui.safety

/**
 * WHO/AFSP compliant safe messaging filter for mental health content
 * Ensures all AI responses follow safe communication guidelines
 */
object SafeMessagingFilter {
    
    private val unsafePhrases = setOf(
        // Never use these phrases - actual harmful combinations
        "commit suicide", "kill yourself", "harm yourself", "end your life",
        "ways to die", "how to kill", "suicide methods", "cut yourself",
        "harm yourself", "end it all", "want to die", "kill myself"
    )
    
    private val safeAlternatives = mapOf(
        "commit suicide" to "having thoughts of ending your life",
        "kill yourself" to "hurt yourself",
        "harm yourself" to "hurt yourself",
        "end your life" to "end your suffering",
        "ways to die" to "thoughts of ending your life",
        "how to kill" to "thoughts of hurting yourself",
        "suicide methods" to "methods of self-harm",
        "cut yourself" to "hurt yourself",
        "harm yourself" to "hurt yourself",
        "end it all" to "feel overwhelmed and hopeless",
        "want to die" to "feel like you can't go on",
        "kill myself" to "hurt myself"
    )
    
    /**
     * Filters AI response for safe messaging compliance
     * @param originalResponse The raw AI-generated response
     * @return Filtered, safe response
     */
    fun filterResponse(originalResponse: String): String {
        var filteredResponse = originalResponse.lowercase()
        
        // Replace unsafe phrases with safe alternatives (phrase-level only)
        unsafePhrases.forEach { unsafe ->
            safeAlternatives[unsafe]?.let { safe ->
                filteredResponse = filteredResponse.replace(unsafe, safe)
            }
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
