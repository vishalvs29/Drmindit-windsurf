package com.drmindit.shared.domain.audience

import kotlinx.serialization.Serializable

/**
 * Enhanced Audience-Specific AI System
 * Replaces generic AI chat with contextual, audience-tailored responses
 */
object EnhancedAudienceAI {
    
    /**
     * Get system prompt for specific audience and context
     */
    fun getSystemPrompt(
        audience: AudienceType,
        programCategory: ProgramCategory,
        currentStep: StepType,
        userContext: UserContext
    ): String {
        val basePrompt = getBasePrompt(audience)
        val contextPrompt = getContextualPrompt(programCategory, currentStep, userContext)
        val safetyPrompt = getSafetyPrompt(audience)
        
        return """
            $basePrompt
            
            Current Context:
            - Program: ${programCategory.name}
            - Step Type: ${currentStep.name}
            - User Mood: ${userContext.currentMood}/10
            - User Stress: ${userContext.currentStress}/10
            - Progress: Day ${userContext.currentDay} of program
            
            $contextPrompt
            
            $safetyPrompt
            
            Guidelines:
            - Stay within your role and tone
            - Focus on the current step's objective
            - Provide actionable, practical guidance
            - Avoid generic AI responses
            - Keep responses concise and relevant
            - Always prioritize user safety and wellbeing
        """.trimIndent()
    }
    
    /**
     * Get response style for audience
     */
    fun getResponseStyle(audience: AudienceType): AIResponseStyle {
        return when (audience) {
            AudienceType.STUDENT -> AIResponseStyle(
                persona = "Supportive Academic Mentor",
                tone = "Friendly, encouraging, simple language",
                languageLevel = LanguageLevel.SIMPLE,
                traumaSafe = false,
                focusAreas = listOf("academic performance", "exam anxiety", "study habits", "confidence"),
                responseLength = ResponseLength.MEDIUM,
                useEmojis = true,
                useRelatableExamples = true
            )
            
            AudienceType.CORPORATE -> AIResponseStyle(
                persona = "Workplace Wellness Coach",
                tone = "Professional, concise, productivity-focused",
                languageLevel = LanguageLevel.PROFESSIONAL,
                traumaSafe = false,
                focusAreas = listOf("work stress", "burnout prevention", "work-life balance", "productivity"),
                responseLength = ResponseLength.CONCISE,
                useEmojis = false,
                useRelatableExamples = true
            )
            
            AudienceType.POLICE_MILITARY -> AIResponseStyle(
                persona = "Trauma-Informed Support Specialist",
                tone = "Calm, direct, non-triggering, grounding",
                languageLevel = LanguageLevel.DIRECT,
                traumaSafe = true,
                focusAreas = listOf("emotional regulation", "grounding", "sleep", "resilience", "safety"),
                responseLength = ResponseLength.CONCISE,
                useEmojis = false,
                useRelatableExamples = false
            )
        }
    }
    
    /**
     * Generate contextual response based on step type
     */
    fun generateContextualResponse(
        audience: AudienceType,
        stepType: StepType,
        userInput: String,
        userContext: UserContext
    ): String {
        val style = getResponseStyle(audience)
        
        return when (stepType) {
            StepType.INSTRUCTION -> generateInstructionalResponse(style, userInput)
            StepType.EXERCISE -> generateExerciseResponse(style, userInput, userContext)
            StepType.REFLECTION -> generateReflectionResponse(style, userInput, userContext)
            StepType.AUDIO_SESSION -> generateAudioSessionResponse(style, userInput)
            StepType.ASSESSMENT -> generateAssessmentResponse(style, userInput, userContext)
            StepType.PRACTICE -> generatePracticeResponse(style, userInput, userContext)
        }
    }
    
    /**
     * Get base prompt for audience
     */
    private fun getBasePrompt(audience: AudienceType): String {
        return when (audience) {
            AudienceType.STUDENT -> """
                You are a supportive academic mental health mentor helping students succeed.
                Your role is to provide practical guidance for academic challenges and wellbeing.
                Use simple, encouraging language that students can easily understand.
                Focus on building confidence and practical skills for academic success.
            """.trimIndent()
            
            AudienceType.CORPORATE -> """
                You are a workplace wellness coach helping professionals manage stress and prevent burnout.
                Your role is to provide concise, actionable guidance for workplace challenges.
                Use professional language and focus on productivity and work-life balance.
                Emphasize practical solutions that can be implemented in busy work schedules.
            """.trimIndent()
            
            AudienceType.POLICE_MILITARY -> """
                You are a trauma-informed support specialist for high-stress service roles.
                Your role is to provide grounding, stabilization, and resilience support.
                Use calm, direct language that is safe and non-triggering.
                Focus on practical techniques for emotional regulation and stress management.
                Always prioritize safety and avoid potentially triggering content.
            """.trimIndent()
        }
    }
    
    /**
     * Get contextual prompt for program and step
     */
    private fun getContextualPrompt(
        category: ProgramCategory,
        stepType: StepType,
        userContext: UserContext
    ): String {
        val categoryContext = when (category) {
            ProgramCategory.ANXIETY -> "Focus on anxiety management techniques and confidence building"
            ProgramCategory.STRESS -> "Focus on stress reduction and coping mechanisms"
            ProgramCategory.FOCUS -> "Focus on concentration improvement and distraction management"
            ProgramCategory.SLEEP -> "Focus on sleep hygiene and relaxation techniques"
            ProgramCategory.TRAUMA -> "Focus on grounding techniques and emotional safety"
            ProgramCategory.RESILIENCE -> "Focus on building mental strength and stress tolerance"
            ProgramCategory.BURNOUT -> "Focus on energy management and recovery strategies"
        }
        
        val stepContext = when (stepType) {
            StepType.INSTRUCTION -> "Provide clear educational content about the topic"
            StepType.EXERCISE -> "Guide the user through the specific exercise with clear instructions"
            StepType.REFLECTION -> "Ask thoughtful questions that encourage self-reflection"
            StepType.AUDIO_SESSION -> "Prepare the user for the guided audio session"
            StepType.ASSESSMENT -> "Help the user evaluate their progress and insights"
            StepType.PRACTICE -> "Guide practical application of learned skills"
        }
        
        return """
            Program Focus: $categoryContext
            Step Objective: $stepContext
        """.trimIndent()
    }
    
    /**
     * Get safety prompt for audience
     */
    private fun getSafetyPrompt(audience: AudienceType): String {
        val baseSafety = """
            Safety Guidelines:
            - Always prioritize user safety and wellbeing
            - Never provide harmful or dangerous advice
            - Encourage professional help when needed
            - Maintain supportive, non-judgmental tone
            - If user expresses crisis, immediately provide resources
            - Respect privacy and boundaries
        """.trimIndent()
        
        val audienceSpecific = when (audience) {
            AudienceType.POLICE_MILITARY -> """
                Additional Safety for Service Roles:
                - Avoid potentially triggering content related to trauma
                - Use grounding techniques when user appears distressed
                - Focus on present-moment awareness and safety
                - Provide immediate stabilization techniques
                - Never ask about specific traumatic experiences
            """.trimIndent()
            
            else -> ""
        }
        
        return baseSafety + (if (audienceSpecific.isNotEmpty()) "\n\n$audienceSpecific" else "")
    }
    
    /**
     * Generate instructional response
     */
    private fun generateInstructionalResponse(
        style: AIResponseStyle,
        userInput: String
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Great question! Let me explain this in a way that's easy to understand.
                
                Think of it like this: [simple analogy]
                
                Here are the key points to remember:
                • [Point 1 in simple terms]
                • [Point 2 with example]
                • [Point 3 practical application]
                
                Does this make sense? Feel free to ask if anything is unclear! 📚
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Here's the professional approach to this topic:
                
                Key Points:
                • [Point 1 - direct and actionable]
                • [Point 2 - business context]
                • [Point 3 - implementation strategy]
                
                This approach is evidence-based and proven effective in workplace settings.
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Let me explain this clearly and safely:
                
                Focus on these key elements:
                • [Point 1 - grounding and safety]
                • [Point 2 - practical technique]
                • [Point 3 - stabilization]
                
                This technique is designed to help you stay present and in control.
            """.trimIndent()
            
            else -> "Here's the information you need to understand this topic."
        }
    }
    
    /**
     * Generate exercise response
     */
    private fun generateExerciseResponse(
        style: AIResponseStyle,
        userInput: String,
        userContext: UserContext
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Ready to try this exercise? Here's how we'll do it:
                
                🎯 Step 1: [Clear instruction]
                🎯 Step 2: [Next step]
                🎯 Step 3: [Final step]
                
                Take your time and remember: it's okay if this feels new at first!
                You're doing great just by trying this. 💪
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Exercise Protocol:
                
                Step 1: [Instruction]
                Step 2: [Instruction]
                Duration: [Time frame]
                
                This technique is designed for quick implementation in professional settings.
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Let's do this exercise together, safely:
                
                Step 1: [Grounding instruction]
                Step 2: [Stabilization technique]
                Step 3: [Safety check]
                
                Go at your own pace. You're in control of this process.
            """.trimIndent()
            
            else -> "Here's how to complete this exercise:"
        }
    }
    
    /**
     * Generate reflection response
     */
    private fun generateReflectionResponse(
        style: AIResponseStyle,
        userInput: String,
        userContext: UserContext
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Thanks for sharing! Your reflections are really valuable. 🌟
                
                I notice you mentioned [key point from input]. That's really insightful!
                
                Here's something to consider: [gentle guidance]
                
                How does that resonate with you? There's no right or wrong answer here.
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Your reflection provides important insights for your professional development.
                
                Key observation: [analysis of input]
                
                Strategic consideration: [professional guidance]
                
                How will you apply this insight in your work context?
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Thank you for sharing. Your awareness is valuable.
                
                I hear that you're experiencing [safe reflection of input].
                
                Let's focus on what's helpful for you right now: [grounding guidance]
                
                You're doing important work by reflecting like this.
            """.trimIndent()
            
            else -> "Thank you for your reflection. Let's explore this further:"
        }
    }
    
    /**
     * Generate audio session response
     */
    private fun generateAudioSessionResponse(
        style: AIResponseStyle,
        userInput: String
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Perfect! Get ready for a relaxing audio session. 🎧
                
                Find a comfortable spot and take a deep breath.
                This session will help you [benefit].
                
                When you're ready, press play and let's begin this journey together!
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Audio session prepared for optimal focus and relaxation.
                
                Find a quiet space for 5-10 minutes.
                This session is designed to [professional benefit].
                
                Begin when ready for maximum effectiveness.
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Ready for a safe, grounding audio experience.
                
                Find a comfortable position where you feel secure.
                This session will help you stay present and calm.
                
                You're in control. Begin when you feel ready.
            """.trimIndent()
            
            else -> "Ready for the audio session. Get comfortable and press play when ready."
        }
    }
    
    /**
     * Generate assessment response
     */
    private fun generateAssessmentResponse(
        style: AIResponseStyle,
        userInput: String,
        userContext: UserContext
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Let's check in on your progress! 📊
                
                Based on what you've shared, I can see you've made [positive observation].
                That's really impressive!
                
                Here's what's working well: [strengths]
                Here's what we can build on: [growth areas]
                
                How are you feeling about your progress so far?
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Progress Assessment Summary:
                
                Strengths Demonstrated: [professional strengths]
                Areas for Development: [growth areas]
                ROI on Current Approach: [effectiveness analysis]
                
                Recommendations for continued improvement: [actionable steps]
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Let's gently check in on your progress.
                
                You've shown strength in: [acknowledged progress]
                You're developing skills in: [new abilities]
                
                Most importantly: you're staying safe and present.
                
                How does this assessment feel for you?
            """.trimIndent()
            
            else -> "Let's review your progress and plan next steps."
        }
    }
    
    /**
     * Generate practice response
     */
    private fun generatePracticeResponse(
        style: AIResponseStyle,
        userInput: String,
        userContext: UserContext
    ): String {
        return when (style.persona) {
            "Supportive Academic Mentor" -> """
                Time to put your new skills into practice! 🎯
                
                Here's your practice challenge: [clear task]
                
                Remember what we learned about [key concept]
                You've got this! Every step forward is progress.
                
                Let me know how it goes - I'm here to cheer you on! 📚✨
            """.trimIndent()
            
            "Workplace Wellness Coach" -> """
                Practice Implementation:
                
                Task: [specific action]
                Context: [workplace application]
                Timeline: [deadline]
                
                This practice will reinforce your professional development.
            """.trimIndent()
            
            "Trauma-Informed Support Specialist" -> """
                Let's practice this skill safely together.
                
                Practice task: [grounding activity]
                Focus on: [safety aspect]
                Duration: [time frame]
                
                Remember: you're in control. Practice at your own pace.
            """.trimIndent()
            
            else -> "Here's your practice task to reinforce what you've learned:"
        }
    }
}

/**
 * Enhanced AI response style configuration
 */
@Serializable
data class AIResponseStyle(
    val persona: String,
    val tone: String,
    val languageLevel: LanguageLevel,
    val traumaSafe: Boolean,
    val focusAreas: List<String>,
    val responseLength: ResponseLength,
    val useEmojis: Boolean,
    val useRelatableExamples: Boolean
)

@Serializable
enum class ResponseLength {
    CONCISE,    // 1-2 sentences
    MEDIUM,     // 1 paragraph
    DETAILED    // 2-3 paragraphs
}

/**
 * User context for AI responses
 */
@Serializable
data class UserContext(
    val currentMood: Int,        // 1-10 scale
    val currentStress: Int,      // 1-10 scale
    val currentDay: Int,         // Day of program
    val programId: String,
    val recentProgress: List<String> = emptyList(),
    val challenges: List<String> = emptyList(),
    val goals: List<String> = emptyList()
)
