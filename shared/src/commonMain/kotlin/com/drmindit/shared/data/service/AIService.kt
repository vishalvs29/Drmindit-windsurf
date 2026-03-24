package com.drmindit.shared.data.service

import com.drmindit.shared.domain.model.*
import com.drmindit.shared.data.network.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AIService {
    suspend fun getWellnessResponse(
        message: String,
        context: List<ChatMessage>,
        riskLevel: RiskLevel
    ): Result<AIResponse>
    
    suspend fun analyzeIntent(message: String): Result<ChatIntent>
    suspend fun detectMood(message: String): Result<MoodCategory>
    suspend fun generateQuickReplies(context: List<ChatMessage>): Result<List<String>>
}

class OpenAIService(
    private val apiClient: ApiClient,
    private val promptTemplates: PromptTemplates
) : AIService {
    
    override suspend fun getWellnessResponse(
        message: String,
        context: List<ChatMessage>,
        riskLevel: RiskLevel
    ): Result<AIResponse> {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Build context-aware prompt
            val prompt = buildPrompt(message, context, riskLevel)
            
            // Call AI API
            val response = callAIAPI(prompt)
            
            if (response.isSuccess) {
                val aiText = response.getOrNull() ?: ""
                val processingTime = System.currentTimeMillis() - startTime
                
                // Parse response and extract metadata
                val parsedResponse = parseAIResponse(aiText)
                
                Result.success(AIResponse(
                    text = parsedResponse.text,
                    recommendedSessions = emptyList(), // Will be populated by repository
                    quickReplies = parsedResponse.quickReplies,
                    moodTag = parsedResponse.moodTag,
                    riskLevel = riskLevel,
                    confidence = parsedResponse.confidence,
                    processingTime = processingTime,
                    requiresEscalation = riskLevel >= RiskLevel.HIGH
                ))
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("AI API failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun analyzeIntent(message: String): Result<ChatIntent> {
        return try {
            val prompt = promptTemplates.getIntentAnalysisPrompt(message)
            val response = callAIAPI(prompt)
            
            if (response.isSuccess) {
                val intent = parseIntent(response.getOrNull() ?: "")
                Result.success(intent)
            } else {
                Result.failure(Exception("Intent analysis failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun detectMood(message: String): Result<MoodCategory> {
        return try {
            val prompt = promptTemplates.getMoodDetectionPrompt(message)
            val response = callAIAPI(prompt)
            
            if (response.isSuccess) {
                val mood = parseMood(response.getOrNull() ?: "")
                Result.success(mood)
            } else {
                Result.failure(Exception("Mood detection failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateQuickReplies(context: List<ChatMessage>): Result<List<String>> {
        return try {
            val lastMessage = context.lastOrNull { it.isAIMessage() }?.text ?: ""
            val prompt = promptTemplates.getQuickReplyPrompt(lastMessage)
            val response = callAIAPI(prompt)
            
            if (response.isSuccess) {
                val quickReplies = parseQuickReplies(response.getOrNull() ?: "")
                Result.success(quickReplies)
            } else {
                Result.failure(Exception("Quick reply generation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildPrompt(
        userMessage: String,
        context: List<ChatMessage>,
        riskLevel: RiskLevel
    ): String {
        val contextText = context.takeLast(5).joinToString("\n") { message ->
            "${message.sender.name.lowercase()}: ${message.text}"
        }
        
        val safetyInstructions = when (riskLevel) {
            RiskLevel.CRITICAL -> promptTemplates.CRITICAL_SAFETY_PROMPT
            RiskLevel.HIGH -> promptTemplates.HIGH_RISK_SAFETY_PROMPT
            RiskLevel.MEDIUM -> promptTemplates.MEDIUM_RISK_SAFETY_PROMPT
            RiskLevel.LOW -> ""
        }
        
        return """
            ${promptTemplates.WELLNESS_ASSISTANT_PROMPT}
            
            $safetyInstructions
            
            Recent conversation:
            $contextText
            
            User: $userMessage
            
            ${promptTemplates.RESPONSE_FORMAT_PROMPT}
        """.trimIndent()
    }
    
    private suspend fun callAIAPI(prompt: String): Result<String> {
        return try {
            // This would make an actual API call to OpenAI or compatible service
            // For now, return a mock response
            Result.success(generateMockResponse(prompt))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateMockResponse(prompt: String): String {
        // Mock AI responses for demonstration
        return when {
            prompt.contains("anxious", ignoreCase = true) -> """
                I understand you're feeling anxious right now. That's completely okay, and I'm here to support you.
                
                Let's try a simple breathing exercise together:
                1. Take a slow breath in for 4 counts
                2. Hold for 7 counts
                3. Exhale slowly for 8 counts
                4. Repeat 3-4 times
                
                This technique can help calm your nervous system. I also have some guided sessions specifically for anxiety relief that might be helpful.
                
                quick_replies: ["Tell me more about breathing exercises", "I want to try an anxiety session", "I'm still feeling anxious"]
                mood: anxious
                confidence: 0.85
            """.trimIndent()
            
            prompt.contains("sleep", ignoreCase = true) -> """
                I know how frustrating it can be when you can't sleep. Let's work together to help you find some rest.
                
                Here are a few things that might help right now:
                - Try the 4-7-8 breathing technique I mentioned
                - Avoid looking at your phone or screens
                - Focus on relaxing your muscles, starting from your toes and working up
                
                I have some wonderful sleep meditation sessions that could help you drift off naturally.
                
                quick_replies: "I want to try a sleep session", "Tell me more about the breathing technique", "I've tried everything"
                mood: sleepless
                confidence: 0.90
            """.trimIndent()
            
            prompt.contains("stress", ignoreCase = true) -> """
                Stress can feel overwhelming, but you've already taken a positive step by reaching out. Let's find some relief together.
                
                When you're feeling stressed, your body is in "fight or flight" mode. We need to signal to your body that you're safe.
                
                Try this quick reset:
                - Roll your shoulders back and down
                - Take three deep belly breaths
                - Notice one thing you can see, hear, and feel right now
                
                I have some excellent stress management sessions that can help you build long-term resilience.
                
                quick_replies: "I want to try a stress session", "Tell me more about stress", "I need immediate relief"
                mood: stressed
                confidence: 0.88
            """.trimIndent()
            
            prompt.contains("die", ignoreCase = true) || prompt.contains("hopeless", ignoreCase = true) -> """
                I hear how much pain you're in right now, and I want you to know that your life matters deeply. You are not alone in this.
                
                Please reach out to someone who can help you right now:
                - Call or text 988 in the US for the Suicide & Crisis Lifeline
                - Contact a trusted friend, family member, or healthcare provider
                - Go to the nearest emergency room if you're in immediate danger
                
                These feelings are temporary, even though they don't feel like it right now. Help is available, and you deserve support.
                
                Would you like me to share some emergency helpline numbers with you?
                
                quick_replies: "Yes, show me helplines", "I need to talk to someone now", "I'm scared"
                mood: depressed
                confidence: 0.95
            """.trimIndent()
            
            else -> """
                Thank you for sharing that with me. I'm here to support you on your wellness journey.
                
                It takes courage to reach out, and I'm glad you did. Everyone's path to mental wellness is different, and we'll find what works best for you.
                
                What would be most helpful for you right now? I can suggest guided meditations, breathing exercises, or just be here to listen.
                
                quick_replies: "I want to try a meditation", "I need help with anxiety", "I can't sleep", "I feel stressed"
                mood: neutral
                confidence: 0.75
            """.trimIndent()
        }
    }
    
    private fun parseAIResponse(response: String): ParsedAIResponse {
        val lines = response.split("\n")
        val text = lines.filter { !it.startsWith("quick_replies:") && !it.startsWith("mood:") && !it.startsWith("confidence:") }.joinToString("\n").trim()
        
        val quickRepliesLine = lines.find { it.startsWith("quick_replies:") }
        val quickReplies = quickRepliesLine?.substringAfter("quick_replies:")?.let { 
            it.trim().removeSurrounding("\"").split(",").map { it.trim() }
        } ?: emptyList()
        
        val moodLine = lines.find { it.startsWith("mood:") }
        val mood = moodLine?.substringAfter("mood:")?.trim()?.let { moodString ->
            try {
                MoodCategory.valueOf(moodString.uppercase())
            } catch (e: IllegalArgumentException) {
                MoodCategory.NEUTRAL
            }
        } ?: MoodCategory.NEUTRAL
        
        val confidenceLine = lines.find { it.startsWith("confidence:") }
        val confidence = confidenceLine?.substringAfter("confidence:")?.trim()?.toFloatOrNull() ?: 0.75f
        
        return ParsedAIResponse(
            text = text,
            quickReplies = quickReplies,
            moodTag = mood,
            confidence = confidence
        )
    }
    
    private fun parseIntent(response: String): ChatIntent {
        // Simplified intent parsing
        return ChatIntent(
            category = IntentCategory.CONVERSATION,
            confidence = 0.7f,
            entities = emptyMap(),
            sentiment = SentimentAnalysis(0.0f, 0.5f, SentimentLabel.NEUTRAL)
        )
    }
    
    private fun parseMood(response: String): MoodCategory {
        // Simplified mood parsing
        return MoodCategory.NEUTRAL
    }
    
    private fun parseQuickReplies(response: String): List<String> {
        // Simplified quick reply parsing
        return listOf("Tell me more", "I need help", "I'm feeling better")
    }
    
    private data class ParsedAIResponse(
        val text: String,
        val quickReplies: List<String>,
        val moodTag: MoodCategory,
        val confidence: Float
    )
}

class PromptTemplates {
    
    val WELLNESS_ASSISTANT_PROMPT = """
        You are a compassionate AI wellness assistant for DrMindit, a mental health app. Your role is to provide empathetic support and guidance for users dealing with stress, anxiety, sleep issues, and mild depression.
        
        IMPORTANT GUIDELINES:
        - Be warm, empathetic, and non-judgmental
        - Do NOT provide medical advice or diagnosis
        - Do NOT act as a therapist or doctor
        - Focus on wellness techniques and coping strategies
        - Always prioritize user safety
        - Keep responses concise but supportive
        - Suggest app features and sessions when relevant
        
        Your tone should be: calm, supportive, encouraging, and professional yet warm.
    """.trimIndent()
    
    val CRITICAL_SAFETY_PROMPT = """
        CRITICAL SAFETY ALERT: The user may be in immediate danger.
        
        You MUST:
        1. Express immediate concern and support
        2. Provide emergency helpline information
        3. Strongly encourage professional help
        4. Do NOT provide therapeutic techniques
        5. Prioritize safety above all else
        
        This is a crisis situation - respond with urgency and care.
    """.trimIndent()
    
    val HIGH_RISK_SAFETY_PROMPT = """
        HIGH RISK ALERT: The user may be experiencing serious mental health challenges.
        
        You MUST:
        1. Acknowledge their pain with empathy
        2. Gently suggest professional help
        3. Provide helpline information
        4. Be supportive but cautious
        5. Avoid deep therapeutic work
        
        Prioritize safety and professional help recommendations.
    """.trimIndent()
    
    val MEDIUM_RISK_SAFETY_PROMPT = """
        MEDIUM RISK ALERT: The user may be struggling with significant mental health issues.
        
        You MUST:
        1. Be extra supportive and empathetic
        2. Monitor for escalating risk
        3. Suggest professional help gently
        4. Provide basic wellness techniques
        5. Be cautious in your recommendations
        
        Balance support with appropriate safety measures.
    """.trimIndent()
    
    val RESPONSE_FORMAT_PROMPT = """
        Please respond in this format:
        
        [Your supportive response here]
        
        quick_replies: ["Option 1", "Option 2", "Option 3"]
        mood: [detected_mood]
        confidence: [0.0-1.0]
        
        Keep your main response under 200 words and make it genuinely helpful.
    """.trimIndent()
    
    fun getIntentAnalysisPrompt(message: String): String {
        return """
            Analyze the user's intent for this message: "$message"
            
            Categorize as one of: STRESS_RELIEF, ANXIETY_HELP, SLEEP_SUPPORT, DEPRESSION_SUPPORT, GENERAL_WELLNESS, MOOD_CHECK, SESSION_REQUEST, HELPLINE_REQUEST, CRISIS_SUPPORT, CONVERSATION, UNKNOWN
            
            Return format: category|confidence|entities|sentiment
        """.trimIndent()
    }
    
    fun getMoodDetectionPrompt(message: String): String {
        return """
            Detect the primary mood from this message: "$message"
            
            Options: ANXIOUS, STRESSED, SLEEPLESS, DEPRESSED, CALM, HAPPY, CONFUSED, OVERWHELMED, LONELY, FRUSTRATED, WORRIED, EXHAUSTED, MOTIVATED, GRATEFUL, NEUTRAL
            
            Return just the mood category name.
        """.trimIndent()
    }
    
    fun getQuickReplyPrompt(lastAIResponse: String): String {
        return """
            Based on this AI response, generate 3 relevant quick reply options:
            
            "$lastAIResponse"
            
            Options should be:
            - Short and natural
            - Relevant to the context
            - Different types (question, action, feeling)
            
            Return as: ["Option 1", "Option 2", "Option 3"]
        """.trimIndent()
    }
}

// Safety service for risk detection
interface SafetyService {
    suspend fun analyzeRisk(message: String): RiskLevel
    suspend fun getEmergencyHelplines(country: String): List<EmergencyHelpline>
    suspend fun reportCrisisEvent(event: CrisisEvent): Unit
}

class SafetyServiceImpl : SafetyService {
    
    private val riskKeywords = mapOf(
        RiskLevel.CRITICAL to listOf(
            "die", "suicide", "kill myself", "end my life", "want to die",
            "can't go on", "no point", "give up", "better off dead"
        ),
        RiskLevel.HIGH to listOf(
            "hopeless", "helpless", "worthless", "burden", "trap",
            "escape", "disappear", "unbearable", "overwhelmed"
        ),
        RiskLevel.MEDIUM to listOf(
            "depressed", "sad", "lonely", "empty", "numb",
            "lost", "confused", "stuck", "worried"
        )
    )
    
    override suspend fun analyzeRisk(message: String): RiskLevel {
        val lowercaseMessage = message.lowercase()
        
        // Check for critical risk first
        for ((level, keywords) in riskKeywords) {
            if (keywords.any { keyword -> lowercaseMessage.contains(keyword) }) {
                return level
            }
        }
        
        // Additional context analysis
        val riskIndicators = listOf(
            "never", "always", "everyone", "nobody", "forever"
        ).count { lowercaseMessage.contains(it) }
        
        return when {
            riskIndicators >= 3 -> RiskLevel.HIGH
            riskIndicators >= 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
    
    override suspend fun getEmergencyHelplines(country: String): List<EmergencyHelpline> {
        return when (country.uppercase()) {
            "US" -> listOf(
                EmergencyHelpline(
                    id = "us_988",
                    name = "National Suicide Prevention Lifeline",
                    phone = "988",
                    website = "https://988lifeline.org",
                    description = "24/7, free and confidential support",
                    country = "US",
                    isActive = true,
                    priority = 1
                ),
                EmergencyHelpline(
                    id = "us_crisis",
                    name = "Crisis Text Line",
                    phone = "Text HOME to 741741",
                    website = "https://www.crisistextline.org",
                    description = "Text with a trained crisis counselor",
                    country = "US",
                    isActive = true,
                    priority = 2
                )
            )
            "IN" -> listOf(
                EmergencyHelpline(
                    id = "in_icall",
                    name = "iCall Mental Health Helpline",
                    phone = "9152987821",
                    website = "https://icall.org.in",
                    description = "Psychological support by trained professionals",
                    country = "IN",
                    isActive = true,
                    priority = 1
                ),
                EmergencyHelpline(
                    id = "in_vandrevala",
                    name = "Vandrevala Foundation",
                    phone = "1860-266-2600",
                    website = "https://vandrevalafoundation.com",
                    description = "24/7 mental health helpline",
                    country = "IN",
                    isActive = true,
                    priority = 2
                )
            )
            else -> listOf(
                EmergencyHelpline(
                    id = "international",
                    name = "International Helpline",
                    phone = "Contact local emergency services",
                    website = "",
                    description = "Please contact your local emergency services",
                    country = "International",
                    isActive = true,
                    priority = 1
                )
            )
        }
    }
    
    override suspend fun reportCrisisEvent(event: CrisisEvent) {
        // In a real implementation, this would log to a secure system
        // for monitoring and follow-up
        println("Crisis event reported: ${event.id}")
    }
}
