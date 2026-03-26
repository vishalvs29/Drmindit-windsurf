package com.drmindit.shared.domain.conversation

import kotlinx.serialization.Serializable

/**
 * Conversation state machine for DrMindit
 * Manages different conversation contexts and flows
 */
@Serializable
enum class ConversationState {
    ONBOARDING,          // Initial user onboarding
    CHECKIN,             // Daily check-in and mood assessment
    ANXIETY,             // Anxiety management flow
    OVERTHINKING,        // Overthinking and rumination flow
    LOW_MOOD,            // Low mood and depression support
    SLEEP,               // Sleep issues and insomnia
    GENERAL_CHAT,        // General conversation and support
    CRISIS,              // Crisis intervention (overrides all)
    REFLECTION,          // Post-session reflection
    RESOURCE_RECOMMENDATION, // Resource and tool recommendations
    PROGRESS_TRACKING    // Progress and goal tracking
}

/**
 * Conversation context with metadata
 */
@Serializable
data class ConversationContext(
    val state: ConversationState,
    val subState: String? = null,
    val step: Int = 0,
    val maxSteps: Int = 1,
    val userIntent: String? = null,
    val emotionalTone: String? = null,
    val severity: Severity = Severity.LOW,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Severity levels for conversation flows
 */
@Serializable
enum class Severity {
    LOW,      // Mild symptoms, general support
    MODERATE, // Moderate symptoms, structured intervention
    HIGH,     // Severe symptoms, intensive support
    CRITICAL  // Crisis level, immediate intervention
}

/**
 * User intent classification
 */
@Serializable
enum class UserIntent {
    CHECKIN,              // Daily mood check
    ANXIETY_HELP,         // Anxiety support requested
    STRESS_RELIEF,       // Stress management
    SLEEP_HELP,           // Sleep issues
    LOW_MOOD_SUPPORT,    // Depression support
    OVERTHINKING_HELP,   // Rumination management
    CRISIS_HELP,         // Crisis intervention
    GENERAL_CHAT,         // Casual conversation
    RESOURCE_REQUEST,     // Tools and resources
    PROGRESS_UPDATE,     // Progress sharing
    FEEDBACK,            // App feedback
    UNKNOWN              // Unclear intent
}

/**
 * Emotional tone detection
 */
@Serializable
enum class EmotionalTone {
    POSITIVE,      // Happy, grateful, excited
    NEUTRAL,       // Calm, balanced, focused
    ANXIOUS,       // Worried, nervous, fearful
    SAD,           // Depressed, hopeless, crying
    ANGRY,         // Frustrated, irritated, upset
    CONFUSED,      // Uncertain, questioning
    STRESSED,      // Overwhelmed, pressured
    EXHAUSTED,     // Tired, burned out
    PANICKED,      // Panic, emergency
    CALM,          // Peaceful, relaxed
    MOTIVATED,     // Energized, determined
    HOPEFUL        // Optimistic, encouraged
}

/**
 * Message types in conversation
 */
@Serializable
enum class MessageType {
    USER_INPUT,           // User message
    AI_RESPONSE,          // AI-generated response
    EXERCISE_PROMPT,     // Structured exercise prompt
    RESOURCE_LINK,        // Tool/resource recommendation
    CRISIS_INTERVENTION, // Crisis response
    SYSTEM_NOTIFICATION,  // System messages
    REFLECTION_QUESTION,  // Post-session reflection
    PROGRESS_UPDATE,     // User progress sharing
    FEEDBACK_REQUEST     // App feedback request
}

/**
 * Conversation message with rich metadata
 */
@Serializable
data class ConversationMessage(
    val id: String,
    val type: MessageType,
    val content: String,
    val sender: MessageSender,
    val context: ConversationContext,
    val metadata: MessageMetadata = MessageMetadata(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Message sender types
 */
@Serializable
enum class MessageSender {
    USER,      // User message
    AI,        // AI-generated response
    SYSTEM,    // System message
    CRISIS     // Crisis intervention
}

/**
 * Message metadata for tracking and analysis
 */
@Serializable
data class MessageMetadata(
    val emotionalTone: EmotionalTone? = null,
    val detectedIntent: UserIntent? = null,
    val severity: Severity = Severity.LOW,
    val keywords: List<String> = emptyList(),
    val entities: Map<String, String> = emptyMap(),
    val confidence: Float = 0.0f,
    val responseTime: Long? = null,
    val userSatisfaction: Int? = null, // 1-5 rating
    val isFollowUpRequired: Boolean = false,
    val nextStepHint: String? = null
)

/**
 * Flow step definition
 */
@Serializable
data class FlowStep(
    val id: String,
    val step: Int,
    val title: String,
    val prompt: String,
    val expectedInputType: InputType,
    val validationRules: List<ValidationRule> = emptyList(),
    val aiResponseEnabled: Boolean = true,
    val predefinedResponses: List<String> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val nextSteps: List<String> = emptyList(),
    val isOptional: Boolean = false,
    val timeoutSeconds: Int = 300
)

/**
 * Input types expected from user
 */
@Serializable
enum class InputType {
    TEXT,           // Free text input
    MULTIPLE_CHOICE, // Selection from options
    SCALE,          // Rating scale (1-10)
    YES_NO,         // Binary choice
    NUMBER,         // Numeric input
    DATE_TIME,      // Date/time selection
    EXERCISE_RESULT, // Exercise completion
    RATING,         // User rating
    FEEDBACK        // App feedback
}

/**
 * Validation rules for user input
 */
@Serializable
data class ValidationRule(
    val type: ValidationType,
    val parameters: Map<String, String> = emptyMap(),
    val errorMessage: String = "Invalid input"
)

/**
 * Validation rule types
 */
@Serializable
enum class ValidationType {
    REQUIRED,       // Input is required
    MIN_LENGTH,     // Minimum length
    MAX_LENGTH,     // Maximum length
    RANGE,          // Numeric range
    REGEX,          // Regular expression
    CUSTOM          // Custom validation logic
}

/**
 * Exercise definition
 */
@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val type: ExerciseType,
    val duration: Int, // in seconds
    val instructions: List<String>,
    val benefits: List<String>,
    val difficulty: Difficulty = Difficulty.EASY,
    val equipment: List<String> = emptyList(),
    val audioUrl: String? = null,
    val imageUrl: String? = null
)

/**
 * Exercise types
 */
@Serializable
enum class ExerciseType {
    BREATHING,      // Breathing exercises
    MINDFULNESS,    // Mindfulness practices
    BODY_SCAN,      // Body scan meditation
    GROUNDING,      // Grounding techniques
    PROGRESSIVE_RELAXATION, // Progressive muscle relaxation
    VISUALIZATION, // Guided imagery
    JOURNALING,     // Writing exercises
    COGNITIVE,      // Cognitive restructuring
    BEHAVIORAL,    // Behavioral activation
    SOCIAL,         // Social connection exercises
    PHYSICAL       // Physical activities
}

/**
 * Exercise difficulty levels
 */
@Serializable
enum class Difficulty {
    EASY,           // Simple, quick exercises
    MODERATE,       // Moderate complexity
    CHALLENGING     // Complex or longer exercises
}

/**
 * Resource definition
 */
@Serializable
data class Resource(
    val id: String,
    val name: String,
    val description: String,
    val type: ResourceType,
    val url: String? = null,
    val content: String? = null,
    val category: ResourceCategory,
    val tags: List<String> = emptyList(),
    val isPremium: Boolean = false,
    val estimatedTime: Int? = null, // in minutes
    val rating: Float = 0.0f,
    val reviewCount: Int = 0
)

/**
 * Resource types
 */
@Serializable
enum class ResourceType {
    ARTICLE,        // Educational articles
    VIDEO,          // Video content
    AUDIO,          // Audio recordings
    EXERCISE,       // Guided exercises
    WORKSHEET,      // Printable worksheets
    TOOL,           // Interactive tools
    HOTLINE,        // Crisis hotlines
    APP,            // Recommended apps
    BOOK,           // Book recommendations
    PODCAST,        // Podcast episodes
    COMMUNITY,      // Support groups
    PROFESSIONAL    // Professional help
}

/**
 * Resource categories
 */
@Serializable
enum class ResourceCategory {
    ANXIETY,        // Anxiety resources
    DEPRESSION,     // Depression resources
    STRESS,         // Stress management
    SLEEP,          // Sleep resources
    MINDFULNESS,    // Mindfulness practices
    RELATIONSHIPS,  // Relationship help
    WORK,           // Workplace wellness
    TRAUMA,         // Trauma resources
    ADDICTION,      // Addiction support
    EATING,         // Eating disorders
    SELF_CARE,      // Self-care practices
    CRISIS,         // Crisis intervention
    GENERAL         // General wellness
}

/**
 * User profile for personalization
 */
@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val age: Int? = null,
    val gender: String? = null,
    val preferences: UserPreferences = UserPreferences(),
    val history: ConversationHistory = ConversationHistory(),
    val progress: UserProgress = UserProgress(),
    val riskFactors: List<String> = emptyList(),
    val strengths: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val lastSession: Long = 0,
    val totalSessions: Int = 0
)

/**
 * User preferences
 */
@Serializable
data class UserPreferences(
    val preferredTimeOfDay: String = "morning",
    val sessionDuration: Int = 10, // in minutes
    val exerciseTypes: List<ExerciseType> = listOf(ExerciseType.BREATHING),
    val communicationStyle: CommunicationStyle = CommunicationStyle.SUPPORTIVE,
    val crisisContacts: List<String> = emptyList(),
    val privacyLevel: PrivacyLevel = PrivacyLevel.STANDARD,
    val language: String = "en",
    val timezone: String = "UTC"
)

/**
 * Communication styles
 */
@Serializable
enum class CommunicationStyle {
    SUPPORTIVE,     // Warm and encouraging
    DIRECT,         // Straightforward and practical
    GENTLE,         // Soft and nurturing
    MOTIVATIONAL,   // Inspiring and empowering
    EDUCATIONAL,    // Informative and teaching
    CASUAL         // Friendly and relaxed
}

/**
 * Privacy levels
 */
@Serializable
enum class PrivacyLevel {
    MINIMAL,        // Minimal data collection
    STANDARD,       // Standard privacy settings
    STRICT,         // Enhanced privacy protection
    ANONYMOUS       // Anonymous mode
}

/**
 * Conversation history tracking
 */
@Serializable
data class ConversationHistory(
    val sessions: List<ConversationSession> = emptyList(),
    val totalMessages: Int = 0,
    val averageSessionDuration: Int = 0,
    val mostDiscussedTopics: List<String> = emptyList(),
    val emotionalPatterns: Map<EmotionalTone, Int> = emptyMap(),
    val crisisInterventions: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Individual conversation session
 */
@Serializable
data class ConversationSession(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int, // in minutes
    val messages: List<ConversationMessage>,
    val initialState: ConversationState,
    val finalState: ConversationState,
    val exercises: List<String>, // Exercise IDs
    val resources: List<String>,  // Resource IDs
    val userSatisfaction: Int? = null,
    val outcomes: List<String> = emptyList()
)

/**
 * User progress tracking
 */
@Serializable
data class UserProgress(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalSessions: Int = 0,
    val completedExercises: List<String> = emptyList(),
    val viewedResources: List<String> = emptyList(),
    val moodTrends: Map<String, List<Int>> = emptyMap(), // mood ratings over time
    val skillDevelopment: Map<String, Int> = emptyMap(), // skill levels
    val goalsAchieved: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
