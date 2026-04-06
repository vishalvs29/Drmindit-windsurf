package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Consolidated Core Models
 * All domain models in one file to avoid redeclarations
 */

// ==================== USER MODELS ====================

@Serializable
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val type: UserType = UserType.USER,
    val preferences: UserPreferences = UserPreferences(),
    val profile: UserProfile? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserPreferences(
    val language: String = "en",
    val timezone: String = "UTC",
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false,
    val reminderTime: String = "09:00"
)

@Serializable
data class UserProfile(
    val avatar: String? = null,
    val bio: String? = null,
    val personalGoals: List<PersonalGoal> = emptyList(),
    val preferredSessionTypes: List<SessionCategory> = emptyList()
)

@Serializable
enum class UserType {
    USER,
    THERAPIST,
    ADMIN
}

@Serializable
enum class PersonalGoal {
    STRESS_REDUCTION,
    BETTER_SLEEP,
    EMOTIONAL_BALANCE,
    MINDFULNESS,
    ANXIETY_MANAGEMENT
}

// ==================== SESSION MODELS ====================

@Serializable
data class Session(
    val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val duration: Int, // in minutes
    val audioUrl: String? = null,
    val imageUrl: String? = null,
    val category: SessionCategory,
    val tags: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val totalRatings: Int = 0,
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SessionProgress(
    val sessionId: String,
    val userId: String,
    val completedDuration: Int,
    val totalDuration: Int,
    val isCompleted: Boolean = false,
    val completionDate: Long? = null,
    val rating: Int? = null,
    val notes: String? = null
) {
    val progressPercentage: Float
        get() = if (totalDuration > 0) {
            (completedDuration.toFloat() / totalDuration.toFloat()) * 100f
        } else 0f
}

@Serializable
enum class SessionCategory {
    MEDITATION,
    BREATHING,
    YOGA,
    MINDFULNESS,
    SLEEP,
    STRESS_RELIEF,
    ANXIETY_MANAGEMENT,
    EMOTIONAL_WELLNESS,
    BODY_SCAN,
    GUIDED_IMAGERY
}

@Serializable
enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

// ==================== PROGRAM MODELS ====================

@Serializable
data class Program(
    val id: String,
    val name: String,
    val description: String,
    val category: ProgramCategory,
    val duration: ProgramDuration,
    val difficulty: Difficulty,
    val sessions: List<Session>,
    val tags: List<String> = emptyList(),
    val rating: Float = 0.0f,
    val totalRatings: Int = 0,
    val enrollmentCount: Int = 0,
    val completionRate: Float = 0.0f,
    val instructor: String? = null,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class ProgramProgress(
    val programId: String,
    val userId: String,
    val currentSessionIndex: Int,
    val completedSessions: Set<Int>,
    val totalSessions: Int,
    val startDate: Long,
    val lastAccessDate: Long,
    val completionDate: Long? = null,
    val isCompleted: Boolean = false
) {
    val progressPercentage: Float
        get() = if (totalSessions > 0) {
            (completedSessions.size.toFloat() / totalSessions.toFloat()) * 100f
        } else 0f
}

@Serializable
enum class ProgramCategory {
    STRESS_MANAGEMENT,
    ANXIETY_RELIEF,
    SLEEP_IMPROVEMENT,
    MINDFULNESS,
    EMOTIONAL_WELLNESS,
    RESILIENCE_BUILDING,
    FOCUS_ENHANCEMENT,
    BODY_AWARENESS
}

@Serializable
enum class ProgramDuration {
    DAYS_7,
    DAYS_14,
    DAYS_21,
    DAYS_30,
    WEEKS_6,
    WEEKS_8,
    WEEKS_12,
    ONGOING
}

// ==================== ANALYTICS MODELS ====================

@Serializable
data class UserAnalytics(
    val userId: String,
    val totalMindfulMinutes: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val sessionsCompleted: Int,
    val averageSessionDuration: Float,
    val moodEntries: List<MoodEntry>,
    val weeklyProgress: List<WeeklyProgress>,
    val monthlyInsights: MonthlyInsights
)

@Serializable
data class MoodEntry(
    val id: String,
    val date: String,
    val mood: Mood,
    val stressLevel: StressLevel,
    val sleepQuality: SleepQuality,
    val notes: String? = null
)

@Serializable
data class WeeklyProgress(
    val week: String,
    val sessionsCompleted: Int,
    val totalMinutes: Int,
    val averageMood: Mood,
    val stressLevel: StressLevel
)

@Serializable
data class MonthlyInsights(
    val month: String,
    val year: Int,
    val averageMood: Mood,
    val improvementAreas: List<String>,
    val achievements: List<String>
)

@Serializable
enum class Mood {
    VERY_HAPPY,
    HAPPY,
    CALM,
    NEUTRAL,
    ANXIOUS,
    STRESSED,
    SAD,
    VERY_SAD
}

@Serializable
enum class StressLevel {
    VERY_LOW,
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH
}

@Serializable
enum class SleepQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    VERY_POOR
}

// ==================== CHAT MODELS ====================

@Serializable
data class ChatMessage(
    val id: String,
    val sessionId: String,
    val sender: ChatSender,
    val messageType: ChatMessageType,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: MessageMetadata? = null
)

@Serializable
data class MessageMetadata(
    val moodTag: MoodCategory? = null,
    val riskLevel: RiskLevel? = null,
    val requiresEscalation: Boolean = false,
    val processingTime: Long = 0,
    val confidence: Float = 0.0f
)

@Serializable
data class ChatSession(
    val id: String,
    val userId: String,
    val title: String,
    val messages: List<ChatMessage>,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true
)

@Serializable
enum class ChatSender {
    USER,
    AI,
    SYSTEM
}

@Serializable
enum class ChatMessageType {
    USER,
    AI_RESPONSE,
    AI_WELCOME,
    ERROR,
    SYSTEM
}

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
    FRUSTRATED
}

@Serializable
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
enum class Sentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL
}

// ==================== EMERGENCY MODELS ====================

@Serializable
data class EmergencyHelpline(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val country: String,
    val region: String? = null,
    val available24Hours: Boolean = true,
    val languages: List<String> = emptyList(),
    val services: List<String> = emptyList(),
    val website: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class CrisisEvent(
    val id: String,
    val userId: String,
    val type: CrisisType,
    val severity: CrisisSeverity,
    val timestamp: Long,
    val description: String,
    val resolved: Boolean = false,
    val resolutionNotes: String? = null
)

@Serializable
enum class CrisisType {
    SUICIDAL_IDEATION,
    PANIC_ATTACK,
    SEVERE_ANXIETY,
    DEPRESSION,
    TRAUMA_TRIGGER,
    SUBSTANCE_ABUSE,
    DOMESTIC_VIOLENCE,
    SELF_HARM
}

@Serializable
enum class CrisisSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

@Serializable
enum class InterventionType {
    HELPLINE_CONTACT,
    EMERGENCY_SERVICES,
    SAFETY_PLAN,
    COUNSELING_SESSION,
    PEER_SUPPORT,
    GROUNDING_TECHNIQUE
}

@Serializable
enum class InterventionOutcome {
    SUCCESSFUL,
    PARTIALLY_SUCCESSFUL,
    UNSUCCESSFUL,
    ESCALATED,
    FOLLOW_UP_REQUIRED
}

// ==================== NOTIFICATION MODELS ====================

@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
    val channels: Set<NotificationChannel>,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val scheduledAt: Long? = null,
    val expiresAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sentAt: Long? = null,
    val status: NotificationStatus = NotificationStatus.PENDING,
    val retryCount: Int = 0,
    val metadata: NotificationMetadata? = null
)

@Serializable
data class NotificationMetadata(
    val sessionId: String? = null,
    val programId: String? = null,
    val streakCount: Int? = null,
    val moodCategory: MoodCategory? = null,
    val personalizationScore: Float = 0.0f,
    val timezone: String? = null,
    val language: String = "en",
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val deepLink: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class NotificationPreference(
    val userId: String,
    val channels: Map<NotificationChannel, ChannelPreference>,
    val topics: Map<NotificationTopic, TopicPreference>,
    val quietHours: QuietHours? = null,
    val frequency: NotificationFrequency = NotificationFrequency.DAILY,
    val timezone: String = "UTC",
    val language: String = "en",
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class NotificationBatch(
    val id: String,
    val notifications: List<Notification>,
    val scheduledAt: Long? = null,
    val processedAt: Long? = null,
    val status: BatchStatus = BatchStatus.PENDING,
    val successCount: Int = 0,
    val failedCount: Int = 0
)

@Serializable
data class NotificationAnalytics(
    val notificationId: String,
    val userId: String,
    val channel: NotificationChannel,
    val type: String,
    val sentAt: Long,
    val deliveryRate: Float = 0.0f,
    val readRate: Float = 0.0f,
    val clickRate: Float = 0.0f
)

@Serializable
data class NotificationEvent(
    val id: String,
    val notificationId: String,
    val userId: String,
    val eventType: EventType,
    val timestamp: Long,
    val data: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationTest(
    val id: String,
    val name: String,
    val description: String,
    val variants: List<NotificationVariant>,
    val targetAudience: String,
    val startDate: Long,
    val endDate: Long,
    val status: TestStatus = TestStatus.DRAFT
)

@Serializable
data class NotificationVariant(
    val id: String,
    val name: String,
    val notification: Notification,
    val weight: Float = 1.0f
)

@Serializable
data class QuietHours(
    val start: String,
    val end: String,
    val timezone: String = "UTC"
)

// Notification Enums
@Serializable
enum class NotificationType {
    SESSION_REMINDER,
    MOOD_CHECK,
    ACHIEVEMENT,
    PROGRAM_UPDATE,
    CRISIS_ALERT,
    WEEKLY_REPORT,
    RE_ENGAGEMENT,
    SLEEP_REMINDER,
    ANXIETY_SUPPORT,
    SYSTEM_NOTIFICATION
}

@Serializable
enum class NotificationChannel {
    IN_APP,
    PUSH_NOTIFICATION,
    EMAIL,
    SMS,
    WHATSAPP,
    TELEGRAM
}

@Serializable
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

@Serializable
enum class NotificationStatus {
    PENDING,
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    CANCELLED,
    SCHEDULED
}

@Serializable
enum class ChannelPreference {
    ENABLED,
    DISABLED,
    QUIET_HOURS_ONLY
}

@Serializable
enum class TopicPreference {
    ENABLED,
    DISABLED,
    HIGH_PRIORITY,
    LOW_PRIORITY
}

@Serializable
enum class NotificationTopic {
    MEDITATION,
    WELLNESS,
    SLEEP,
    ANXIETY,
    STRESS,
    MOOD_TRACKING,
    ACHIEVEMENTS,
    REMINDERS,
    CRISIS_SUPPORT,
    COMMUNITY,
    EDUCATION
}

@Serializable
enum class BatchStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    PARTIALLY_FAILED,
    FAILED
}

@Serializable
enum class EventType {
    SENT,
    DELIVERED,
    READ,
    CLICKED,
    DISMISSED,
    FAILED
}

@Serializable
enum class TestStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

@Serializable
enum class NotificationFrequency {
    IMMEDIATE,
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER
}

@Serializable
data class UserChannel(
    val id: String,
    val userId: String,
    val channel: NotificationChannel,
    val address: String, // email, phone number, etc.
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val preferences: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class NotificationTemplate(
    val id: String,
    val name: String,
    val type: NotificationType,
    val channels: Set<NotificationChannel>,
    val titleTemplate: String,
    val bodyTemplate: String,
    val variables: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ==================== CHAT MODELS (ADDITIONAL) ====================

@Serializable
data class AIResponse(
    val messageId: String,
    val response: String,
    val confidence: Float = 0.0f,
    val processingTime: Long = 0,
    val suggestions: List<String> = emptyList(),
    val moodTag: MoodCategory? = null,
    val riskLevel: RiskLevel? = null,
    val requiresEscalation: Boolean = false
)

@Serializable
data class ChatPreferences(
    val userId: String,
    val language: String = "en",
    val voiceEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val autoSave: Boolean = true,
    val theme: String = "light"
)

@Serializable
data class ChatAnalytics(
    val sessionId: String,
    val userId: String,
    val messageCount: Int,
    val avgResponseTime: Long,
    val moodTrend: List<MoodCategory>,
    val riskEvents: Int,
    val date: String
)
