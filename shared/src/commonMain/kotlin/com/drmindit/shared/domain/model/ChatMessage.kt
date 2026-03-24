package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Serializable
data class ChatMessage(
    val id: String,
    val text: String,
    val sender: MessageSender,
    val timestamp: Long,
    val messageType: MessageType = MessageType.TEXT,
    val metadata: MessageMetadata? = null,
    val isRead: Boolean = false,
    val sessionId: String? = null
)

@Serializable
enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

@Serializable
enum class MessageType {
    TEXT,
    SESSION_RECOMMENDATION,
    SAFETY_ALERT,
    QUICK_REPLY,
    MOOD_CHECK,
    HELPLINE_SUGGESTION
}

@Serializable
data class MessageMetadata(
    val recommendedSessions: List<RecommendedSession>? = null,
    val quickReplies: List<String>? = null,
    val moodTag: MoodCategory? = null,
    val riskLevel: RiskLevel? = null,
    val helplines: List<EmergencyHelpline>? = null,
    val confidence: Float? = null,
    val processingTime: Long? = null
)

@Serializable
data class RecommendedSession(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int,
    val category: String,
    val instructorName: String,
    val thumbnailUrl: String,
    val audioUrl: String,
    val relevanceScore: Float,
    val reason: String
)

@Serializable
data class ChatSession(
    val id: String,
    val userId: String,
    val title: String,
    val messages: List<ChatMessage>,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val moodTags: List<MoodCategory> = emptyList(),
    val summary: String? = null
)

@Serializable
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTyping: Boolean = false,
    val currentSession: ChatSession? = null,
    val suggestedReplies: List<String> = emptyList(),
    val riskDetected: Boolean = false,
    val safetyAlert: SafetyAlert? = null
)

@Serializable
data class SafetyAlert(
    val level: RiskLevel,
    val message: String,
    val suggestedHelplines: List<EmergencyHelpline>,
    val requiresImmediateAction: Boolean,
    val autoEscalation: Boolean = false
)

@Serializable
data class AIResponse(
    val text: String,
    val recommendedSessions: List<RecommendedSession> = emptyList(),
    val quickReplies: List<String> = emptyList(),
    val moodTag: MoodCategory? = null,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val confidence: Float = 0.0f,
    val processingTime: Long = 0L,
    val requiresEscalation: Boolean = false
)

@Serializable
enum class MoodCategory {
    ANXIOUS,
    STRESSED,
    SLEEPLESS,
    DEPRESSED,
    CALM,
    HAPPY,
    CONFUSED,
    OVERWHELMED,
    LONELY,
    FRUSTRATED,
    WORRIED,
    EXHAUSTED,
    MOTIVATED,
    GRATEFUL,
    NEUTRAL
}

@Serializable
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

// Chat intent detection
@Serializable
data class ChatIntent(
    val category: IntentCategory,
    val confidence: Float,
    val entities: Map<String, String> = emptyMap(),
    val sentiment: SentimentAnalysis
)

@Serializable
enum class IntentCategory {
    STRESS_RELIEF,
    ANXIETY_HELP,
    SLEEP_SUPPORT,
    DEPRESSION_SUPPORT,
    GENERAL_WELLNESS,
    MOOD_CHECK,
    SESSION_REQUEST,
    HELPLINE_REQUEST,
    CRISIS_SUPPORT,
    CONVERSATION,
    UNKNOWN
}

@Serializable
data class SentimentAnalysis(
    val score: Float, // -1.0 (negative) to 1.0 (positive)
    val magnitude: Float, // 0.0 to 1.0 (intensity)
    val label: SentimentLabel
)

@Serializable
enum class SentimentLabel {
    VERY_NEGATIVE,
    NEGATIVE,
    NEUTRAL,
    POSITIVE,
    VERY_POSITIVE
}

// Chat configuration
@Serializable
data class ChatConfig(
    val maxMessagesPerSession: Int = 100,
    val maxSessionDuration: Long = 24 * 60 * 60 * 1000L, // 24 hours
    val autoSaveInterval: Long = 30 * 1000L, // 30 seconds
    val sessionTimeout: Long = 60 * 60 * 1000L, // 1 hour
    val enableVoiceInput: Boolean = false,
    val enableTypingIndicator: Boolean = true,
    val enableQuickReplies: Boolean = true,
    val enableMoodDetection: Boolean = true,
    val enableRiskDetection: Boolean = true,
    val maxTokensPerResponse: Int = 500,
    val temperature: Float = 0.7f
)

// Chat analytics
@Serializable
data class ChatAnalytics(
    val sessionId: String,
    val userId: String,
    val messageCount: Int,
    val averageResponseTime: Long,
    val sessionDuration: Long,
    val moodChanges: List<MoodChange>,
    val sessionRecommendations: Int,
    val escalations: Int,
    val satisfactionScore: Float? = null,
    val completed: Boolean = false
)

@Serializable
data class MoodChange(
    val fromMood: MoodCategory?,
    val toMood: MoodCategory,
    val timestamp: Long,
    val trigger: String?
)

// Chat preferences
@Serializable
data class ChatPreferences(
    val userId: String,
    val preferredTone: ChatTone = ChatTone.EMPATHETIC,
    val responseLength: ResponseLength = ResponseLength.MEDIUM,
    val enableVoiceInput: Boolean = false,
    val enableNotifications: Boolean = true,
    val autoSave: Boolean = true,
    val language: String = "en",
    val timezone: String = "UTC",
    val customQuickReplies: List<String> = emptyList(),
    val blockedTopics: List<String> = emptyList()
)

@Serializable
enum class ChatTone {
    EMPATHETIC,
    PROFESSIONAL,
    CASUAL,
    SUPPORTIVE,
    MOTIVATIONAL
}

@Serializable
enum class ResponseLength {
    SHORT,
    MEDIUM,
    LONG
}

// Helper functions
fun ChatMessage.isUserMessage(): Boolean = sender == MessageSender.USER
fun ChatMessage.isAIMessage(): Boolean = sender == MessageSender.AI
fun ChatMessage.isSystemMessage(): Boolean = sender == MessageSender.SYSTEM

fun ChatMessage.hasRecommendedSessions(): Boolean {
    return metadata?.recommendedSessions?.isNotEmpty() == true
}

fun ChatMessage.getRecommendedSessions(): List<RecommendedSession> {
    return metadata?.recommendedSessions ?: emptyList()
}

fun ChatMessage.hasQuickReplies(): Boolean {
    return metadata?.quickReplies?.isNotEmpty() == true
}

fun ChatMessage.getQuickReplies(): List<String> {
    return metadata?.quickReplies ?: emptyList()
}

fun ChatMessage.isSafetyAlert(): Boolean {
    return messageType == MessageType.SAFETY_ALERT || 
           metadata?.riskLevel != null && metadata.riskLevel != RiskLevel.LOW
}

fun ChatMessage.getRiskLevel(): RiskLevel {
    return metadata?.riskLevel ?: RiskLevel.LOW
}

fun ChatSession.getLatestMessage(): ChatMessage? {
    return messages.maxByOrNull { it.timestamp }
}

fun ChatSession.getMessageCount(): Int = messages.size

fun ChatSession.getUserMessageCount(): Int {
    return messages.count { it.isUserMessage() }
}

fun ChatSession.getAIMessageCount(): Int {
    return messages.count { it.isAIMessage() }
}

fun ChatSession.getDuration(): Long {
    if (messages.isEmpty()) return 0L
    val firstMessage = messages.minByOrNull { it.timestamp }?.timestamp ?: 0L
    val lastMessage = messages.maxByOrNull { it.timestamp }?.timestamp ?: 0L
    return lastMessage - firstMessage
}

fun ChatSession.hasRiskDetected(): Boolean {
    return messages.any { it.isSafetyAlert() }
}

fun ChatSession.getHighestRiskLevel(): RiskLevel {
    return messages.map { it.getRiskLevel() }.maxOrNull() ?: RiskLevel.LOW
}

fun MoodCategory.getDisplayName(): String {
    return when (this) {
        MoodCategory.ANXIOUS -> "Anxious"
        MoodCategory.STRESSED -> "Stressed"
        MoodCategory.SLEEPLESS -> "Sleepless"
        MoodCategory.DEPRESSED -> "Low Mood"
        MoodCategory.CALM -> "Calm"
        MoodCategory.HAPPY -> "Happy"
        MoodCategory.CONFUSED -> "Confused"
        MoodCategory.OVERWHELMED -> "Overwhelmed"
        MoodCategory.LONELY -> "Lonely"
        MoodCategory.FRUSTRATED -> "Frustrated"
        MoodCategory.WORRIED -> "Worried"
        MoodCategory.EXHAUSTED -> "Exhausted"
        MoodCategory.MOTIVATED -> "Motivated"
        MoodCategory.GRATEFUL -> "Grateful"
        MoodCategory.NEUTRAL -> "Neutral"
    }
}

fun MoodCategory.getAssociatedSessionCategory(): String {
    return when (this) {
        MoodCategory.ANXIOUS -> "anxiety"
        MoodCategory.STRESSED -> "stress"
        MoodCategory.SLEEPLESS -> "sleep"
        MoodCategory.DEPRESSED -> "depression"
        MoodCategory.CALM -> "mindfulness"
        MoodCategory.HAPPY -> "mindfulness"
        MoodCategory.CONFUSED -> "focus"
        MoodCategory.OVERWHELMED -> "stress"
        MoodCategory.LONELY -> "depression"
        MoodCategory.FRUSTRATED -> "stress"
        MoodCategory.WORRIED -> "anxiety"
        MoodCategory.EXHAUSTED -> "sleep"
        MoodCategory.MOTIVATED -> "focus"
        MoodCategory.GRATEFUL -> "mindfulness"
        MoodCategory.NEUTRAL -> "mindfulness"
    }
}

fun RiskLevel.getColor(): String {
    return when (this) {
        RiskLevel.LOW -> "#4CAF50"      // Green
        RiskLevel.MEDIUM -> "#FF9800"    // Orange
        RiskLevel.HIGH -> "#F44336"      // Red
        RiskLevel.CRITICAL -> "#9C27B0"  // Purple
    }
}

fun RiskLevel.getDisplayName(): String {
    return when (this) {
        RiskLevel.LOW -> "Low Risk"
        RiskLevel.MEDIUM -> "Medium Risk"
        RiskLevel.HIGH -> "High Risk"
        RiskLevel.CRITICAL -> "Critical"
    }
}

fun IntentCategory.getDisplayName(): String {
    return when (this) {
        IntentCategory.STRESS_RELIEF -> "Stress Relief"
        IntentCategory.ANXIETY_HELP -> "Anxiety Support"
        IntentCategory.SLEEP_SUPPORT -> "Sleep Help"
        IntentCategory.DEPRESSION_SUPPORT -> "Depression Support"
        IntentCategory.GENERAL_WELLNESS -> "General Wellness"
        IntentCategory.MOOD_CHECK -> "Mood Check"
        IntentCategory.SESSION_REQUEST -> "Session Request"
        IntentCategory.HELPLINE_REQUEST -> "Helpline Request"
        IntentCategory.CRISIS_SUPPORT -> "Crisis Support"
        IntentCategory.CONVERSATION -> "Conversation"
        IntentCategory.UNKNOWN -> "Unknown"
    }
}
