package com.drmindit.shared.domain.conversation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Response Generator
 * Generates AI responses and manages hybrid response system
 */
@Singleton
class ResponseGenerator @Inject constructor(
    private val aiService: AIService,
    private val contentLibrary: ContentLibrary
) {
    
    /**
     * Generate AI response for general conversation
     */
    suspend fun generateGeneralChatResponse(
        message: String,
        userProfile: UserProfile
    ): String {
        val prompt = buildGeneralChatPrompt(message, userProfile)
        return aiService.generateResponse(prompt)
    }
    
    /**
     * Generate AI response for specific flow step
     */
    suspend fun generateAIResponse(
        message: String,
        flowStep: FlowStep,
        userProfile: UserProfile
    ): String {
        val prompt = buildFlowStepPrompt(message, flowStep, userProfile)
        return aiService.generateResponse(prompt)
    }
    
    /**
     * Generate crisis response
     */
    fun generateCrisisResponse(message: String, userProfile: UserProfile): CrisisResponse {
        val crisisDetector = CrisisDetector()
        return crisisDetector.generateCrisisResponse(message, userProfile)
    }
    
    /**
     * Generate flow completion response
     */
    suspend fun generateFlowCompletionResponse(
        flow: ConversationFlow,
        userProfile: UserProfile
    ): String {
        val prompt = buildFlowCompletionPrompt(flow, userProfile)
        return aiService.generateResponse(prompt)
    }
    
    /**
     * Get emergency resources
     */
    fun getEmergencyResources(userProfile: UserProfile): List<Resource> {
        return contentLibrary.getEmergencyResources()
    }
    
    /**
     * Generate exercise recommendations
     */
    fun generateExerciseRecommendations(
        flow: ConversationFlow,
        userProfile: UserProfile,
        currentStep: FlowStep
    ): List<Exercise> {
        val userPreferences = userProfile.preferences
        val availableExercises = currentStep.exercises
        
        // Filter exercises based on user preferences
        return availableExercises.filter { exercise ->
            userPreferences.exerciseTypes.contains(exercise.type) ||
            exercise.difficulty == Difficulty.EASY
        }
    }
    
    /**
     * Generate resource recommendations
     */
    fun generateResourceRecommendations(
        flow: ConversationFlow,
        userProfile: UserProfile,
        currentStep: FlowStep
    ): List<Resource> {
        val userPreferences = userProfile.preferences
        val availableResources = currentStep.resources
        
        // Filter resources based on user preferences and access level
        return availableResources.filter { resource ->
            (!resource.isPremium || userPreferences.privacyLevel != PrivacyLevel.MINIMAL) &&
            resource.category != ResourceCategory.CRISIS
        }
    }
    
    /**
     * Generate follow-up questions
     */
    suspend fun generateFollowUpQuestions(
        conversation: List<ConversationMessage>,
        userProfile: UserProfile
    ): List<String> {
        val recentMessages = conversation.takeLast(5)
        val context = buildFollowUpContext(recentMessages, userProfile)
        val prompt = buildFollowUpPrompt(context)
        
        return aiService.generateFollowUpQuestions(prompt)
    }
    
    /**
     * Generate personalized content suggestions
     */
    suspend fun generatePersonalizedSuggestions(
        userProfile: UserProfile,
        conversationHistory: ConversationHistory
    ): List<PersonalizedSuggestion> {
        val prompt = buildPersonalizationPrompt(userProfile, conversationHistory)
        val aiResponse = aiService.generateResponse(prompt)
        
        return parsePersonalizedSuggestions(aiResponse)
    }
    
    /**
     * Build prompt for general chat
     */
    private fun buildGeneralChatPrompt(message: String, userProfile: UserProfile): String {
        return """
            You are DrMindit, an AI mental health companion. Your role is to provide supportive, empathetic, and safe mental health guidance.
            
            User Profile:
            - Name: ${userProfile.name}
            - Preferences: ${userProfile.preferences.communicationStyle.name} communication style
            - Previous sessions: ${userProfile.totalSessions}
            - Current streak: ${userProfile.progress.currentStreak} days
            - Risk factors: ${userProfile.riskFactors.joinToString(", ")}
            - Strengths: ${userProfile.strengths.joinToString(", ")}
            
            User's message: "$message"
            
            Guidelines:
            1. Be warm, supportive, and empathetic
            2. Use ${userProfile.preferences.communicationStyle.name.lowercase()} communication style
            3. Reference their strengths and progress when appropriate
            4. Ask thoughtful follow-up questions to deepen understanding
            5. If they mention concerning content, gently suggest professional help
            6. Keep responses concise but thorough (2-3 paragraphs max)
            7. Always prioritize safety and wellbeing
            
            Respond to the user's message with empathy and support.
        """.trimIndent()
    }
    
    /**
     * Build prompt for flow step
     */
    private fun buildFlowStepPrompt(
        message: String,
        flowStep: FlowStep,
        userProfile: UserProfile
    ): String {
        return """
            You are DrMindit, an AI mental health companion guiding a user through a structured therapeutic flow.
            
            Current Flow: ${flowStep.title}
            Flow Step: ${flowStep.step + 1}/${flowStep.maxSteps}
            Step Purpose: ${flowStep.prompt}
            
            User Profile:
            - Name: ${userProfile.name}
            - Communication Style: ${userProfile.preferences.communicationStyle.name}
            - Experience Level: ${userProfile.totalSessions} previous sessions
            
            User's message: "$message"
            
            Guidelines:
            1. Acknowledge and validate their response
            2. Guide them toward the step's objectives
            3. Use ${userProfile.preferences.communicationStyle.name.lowercase()} style
            4. Reference the flow's purpose and progress
            5. Encourage engagement with exercises if available
            6. Maintain therapeutic boundaries and safety
            7. Keep responses focused on the current step's goals
            
            Respond to support their progress through this therapeutic step.
        """.trimIndent()
    }
    
    /**
     * Build prompt for flow completion
     */
    private fun buildFlowCompletionPrompt(
        flow: ConversationFlow,
        userProfile: UserProfile
    ): String {
        return """
            You are DrMindit, an AI mental health companion completing a structured therapeutic flow with a user.
            
            Completed Flow: ${flow.name}
            Flow Description: ${flow.description}
            Total Steps: ${flow.steps.size}
            Flow Outcomes: ${flow.outcomes.joinToString(", ")}
            
            User Profile:
            - Name: ${userProfile.name}
            - Communication Style: ${userProfile.preferences.communicationStyle.name}
            - Progress: ${userProfile.progress.currentStreak} day streak
            
            Guidelines:
            1. Celebrate their completion and progress
            2. Summarize key learnings from the flow
            3. Provide encouragement for continued practice
            4. Suggest next steps or maintenance strategies
            5. Offer ongoing support
            6. Use ${userProfile.preferences.communicationStyle.name.lowercase()} style
            7. Keep responses positive and forward-looking
            
            Generate a completion response that acknowledges their achievement and encourages continued growth.
        """.trimIndent()
    }
    
    /**
     * Build follow-up context
     */
    private fun buildFollowUpContext(
        messages: List<ConversationMessage>,
        userProfile: UserProfile
    ): String {
        val conversationSummary = messages.joinToString("\n") { message ->
            "${message.sender.name}: ${message.content}"
        }
        
        return """
            Recent Conversation:
            $conversationSummary
            
            User Profile:
            - Name: ${userProfile.name}
            - Communication Style: ${userProfile.preferences.communicationStyle.name}
            - Current Streak: ${userProfile.progress.currentStreak} days
            
            Generate thoughtful follow-up questions to deepen the conversation and support the user's mental health journey.
        """.trimIndent()
    }
    
    /**
     * Build follow-up prompt
     */
    private fun buildFollowUpPrompt(context: String): String {
        return """
            $context
            
            Generate 3-5 thoughtful follow-up questions that:
            1. Are open-ended and encourage reflection
            2. Are relevant to the conversation context
            3. Support the user's mental health journey
            4. Are empathetic and non-judgmental
            5. Help deepen understanding of their experience
            
            Format as a numbered list of questions.
        """.trimIndent()
    }
    
    /**
     * Build personalization prompt
     */
    private fun buildPersonalizationPrompt(
        userProfile: UserProfile,
        conversationHistory: ConversationHistory
    ): String {
        return """
            Generate personalized suggestions for a user based on their profile and conversation history.
            
            User Profile:
            - Name: ${userProfile.name}
            - Preferences: ${userProfile.preferences}
            - Progress: ${userProfile.progress}
            - History: ${conversationHistory}
            
            Generate 3-5 personalized suggestions that include:
            1. Exercise recommendations based on their preferences
            2. Resource suggestions relevant to their needs
            3. Goal suggestions for their mental health journey
            4. Practice recommendations based on their history
            5. Support strategies aligned with their strengths
            
            Format as a JSON array of suggestion objects with id, title, description, and type fields.
        """.trimIndent()
    }
    
    /**
     * Parse personalized suggestions from AI response
     */
    private fun parsePersonalizedSuggestions(aiResponse: String): List<PersonalizedSuggestion> {
        // This would parse the AI response into structured suggestions
        // For now, return placeholder suggestions
        return listOf(
            PersonalizedSuggestion(
                id = "daily_mindfulness",
                title = "Daily Mindfulness Practice",
                description = "Start your day with 5 minutes of mindful breathing",
                type = SuggestionType.EXERCISE
            ),
            PersonalizedSuggestion(
                id = "gratitude_journal",
                title = "Gratitude Journal",
                description = "Write down 3 things you're grateful for each evening",
                type = SuggestionType.PRACTICE
            ),
            PersonalizedSuggestion(
                id = "stress_management",
                title = "Stress Management Techniques",
                description = "Learn quick stress relief exercises for busy moments",
                type = SuggestionType.RESOURCE
            )
        )
    }
}

/**
 * AI Service interface
 */
interface AIService {
    suspend fun generateResponse(prompt: String): String
    suspend fun generateFollowUpQuestions(prompt: String): List<String>
}

/**
 * Content Library interface
 */
interface ContentLibrary {
    fun getEmergencyResources(): List<Resource>
    fun getExercisesByType(type: ExerciseType): List<Exercise>
    fun getResourcesByCategory(category: ResourceCategory): List<Resource>
}

/**
 * Personalized suggestion data
 */
data class PersonalizedSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val type: SuggestionType,
    val isPremium: Boolean = false,
    val estimatedTime: Int? = null
)

/**
 * Suggestion types
 */
enum class SuggestionType {
    EXERCISE,
    PRACTICE,
    RESOURCE,
    GOAL,
    STRATEGY
}
