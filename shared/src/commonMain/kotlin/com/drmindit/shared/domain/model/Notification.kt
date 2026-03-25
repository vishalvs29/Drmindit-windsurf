package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable
import kotlin.js.JsName

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
data class ChannelPreference(
    val enabled: Boolean = true,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val minTimeBetween: Long = 30 * 60 * 1000L, // 30 minutes
    val maxPerDay: Int = 10,
    val customSettings: Map<String, String> = emptyMap()
)

@Serializable
data class TopicPreference(
    val enabled: Boolean = true,
    val frequency: NotificationFrequency = NotificationFrequency.DAILY,
    val preferredTimes: List<PreferredTime> = emptyList(),
    val lastSent: Long? = null
)

@Serializable
data class PreferredTime(
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7) // 1=Monday, 7=Sunday
)

@Serializable
data class QuietHours(
    val enabled: Boolean = true,
    val startTime: String, // "22:00"
    val endTime: String,   // "08:00"
    val timezone: String = "UTC",
    val allowEmergency: Boolean = false
)

@Serializable
data class UserChannel(
    val userId: String,
    val channel: NotificationChannel,
    val identifier: String, // FCM token, phone number, email, telegram chat ID
    val isActive: Boolean = true,
    val verified: Boolean = false,
    val preferences: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class NotificationLog(
    val id: String,
    val notificationId: String,
    val userId: String,
    val channel: NotificationChannel,
    val status: DeliveryStatus,
    val sentAt: Long,
    val deliveredAt: Long? = null,
    val openedAt: Long? = null,
    val clickedAt: Long? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class NotificationTemplate(
    val id: String,
    val name: String,
    val type: NotificationType,
    val channels: Set<NotificationChannel>,
    val titleTemplate: String, // "Hi {{name}}, time to relax!"
    val bodyTemplate: String,   // "Take 5 minutes for your mental wellness today 🌿"
    val variables: Set<String> = emptySet(), // ["name", "session_title"]
    val metadata: Map<String, String> = emptyMap(),
    val isActive: Boolean = true,
    val language: String = "en"
)

@Serializable
data class NotificationSchedule(
    val id: String,
    val userId: String,
    val templateId: String,
    val type: NotificationType,
    val schedule: SchedulePattern,
    val isActive: Boolean = true,
    val nextRun: Long,
    val lastRun: Long? = null,
    val timezone: String = "UTC",
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class SchedulePattern(
    val type: ScheduleType,
    val interval: Int? = null, // For RECURRING
    val cronExpression: String? = null, // For CRON
    val specificTimes: List<Long>? = null, // For SPECIFIC_TIMES
    val daysOfWeek: Set<Int>? = null, // 1-7
    val timeOfDay: String? = null // "09:00"
)

@Serializable
data class NotificationAnalytics(
    val notificationId: String,
    val userId: String,
    val channel: NotificationChannel,
    val type: NotificationType,
    val sentAt: Long,
    val deliveredAt: Long? = null,
    val openedAt: Long? = null,
    val clickedAt: Long? = null,
    val timeToOpen: Long? = null, // milliseconds
    val timeToClick: Long? = null, // milliseconds
    val deviceInfo: Map<String, String>? = null,
    val location: String? = null
)

@Serializable
data class NotificationBatch(
    val id: String,
    val notifications: List<Notification>,
    val scheduledAt: Long,
    val status: BatchStatus = BatchStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val failedCount: Int = 0,
    val successCount: Int = 0
)

// Enums
@Serializable
enum class NotificationType {
    DAILY_REMINDER,
    SESSION_REMINDER,
    STREAK_REMINDER,
    PROGRAM_REMINDER,
    MOOD_CHECK,
    RE_ENGAGEMENT,
    RECOMMENDATION,
    ACHIEVEMENT,
    WELCOME,
    WEEKLY_REPORT,
    SLEEP_REMINDER,
    STRESS_RELIEF_REMINDER,
    ANXIETY_SUPPORT,
    MOTIVATION,
    EMERGENCY
}

@Serializable
enum class NotificationChannel {
    IN_APP,
    PUSH_NOTIFICATION,
    WHATSAPP,
    TELEGRAM,
    EMAIL
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
    SCHEDULED,
    SENDING,
    SENT,
    DELIVERED,
    OPENED,
    CLICKED,
    FAILED,
    EXPIRED,
    CANCELLED
}

@Serializable
enum class DeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    OPENED,
    CLICKED,
    FAILED,
    BOUNCED,
    UNSUBSCRIBED,
    RATE_LIMITED
}

@Serializable
enum class NotificationFrequency {
    IMMEDIATELY,
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER
}

@Serializable
enum class ScheduleType {
    IMMEDIATE,
    DELAYED,
    RECURRING,
    CRON,
    SPECIFIC_TIMES,
    INTELLIGENT
}

@Serializable
enum class BatchStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    PARTIALLY_FAILED,
    FAILED,
    CANCELLED
}

// Helper functions
fun Notification.isExpired(): Boolean {
    return expiresAt?.let { System.currentTimeMillis() > it } ?: false
}

fun Notification.isScheduled(): Boolean {
    return scheduledAt?.let { System.currentTimeMillis() < it } ?: false
}

fun Notification.canRetry(maxRetries: Int = 3): Boolean {
    return retryCount < maxRetries && status == NotificationStatus.FAILED
}

fun Notification.getEstimatedDeliveryTime(): Long {
    return scheduledAt ?: createdAt
}

fun NotificationPreference.isChannelEnabled(channel: NotificationChannel): Boolean {
    return channels[channel]?.enabled ?: false
}

fun NotificationPreference.isTopicEnabled(topic: NotificationTopic): Boolean {
    return topics[topic]?.enabled ?: false
}

fun QuietHours.isQuietNow(): Boolean {
    if (!enabled) return false
    
    val now = java.time.LocalTime.now()
    val start = java.time.LocalTime.parse(startTime)
    val end = java.time.LocalTime.parse(endTime)
    
    return if (start.isBefore(end)) {
        now.isAfter(start) && now.isBefore(end)
    } else {
        // Overnight quiet hours (e.g., 22:00 to 08:00)
        now.isAfter(start) || now.isBefore(end)
    }
}

fun NotificationLog.getDeliveryTime(): Long? {
    return deliveredAt ?: sentAt
}

fun NotificationLog.getEngagementTime(): Long? {
    return openedAt ?: clickedAt
}

fun NotificationLog.isEngaged(): Boolean {
    return openedAt != null || clickedAt != null
}

fun NotificationAnalytics.getEngagementRate(): Float {
    return if (deliveredAt != null) {
        if (openedAt != null || clickedAt != null) 1.0f else 0.0f
    } else 0.0f
}

fun NotificationAnalytics.getTimeToEngagement(): Long? {
    return if (deliveredAt != null) {
        openedAt?.let { opened -> opened - deliveredAt }
            ?: clickedAt?.let { clicked -> clicked - deliveredAt }
    } else null
}

// Notification topics
@Serializable
enum class NotificationTopic {
    SLEEP,
    ANXIETY,
    STRESS,
    MINDFULNESS,
    FOCUS,
    BREATHING,
    YOGA,
    DEPRESSION,
    MOTIVATION,
    ACHIEVEMENTS,
    PROGRAMS,
    DAILY_WELLNESS,
    WEEKLY_REPORT,
    EMERGENCY
}

// Channel-specific configs
data class PushNotificationConfig(
    val fcmServerKey: String,
    val topicPrefix: String = "drmindit_",
    val defaultIcon: String = "@drawable/ic_notification",
    val defaultSound: String = "default",
    val defaultColor: String = "#2196F3"
)

data class WhatsAppConfig(
    val accessToken: String,
    val phoneNumberId: String,
    val webhookUrl: String,
    val version: String = "v18.0",
    val templateNamespace: String = "drmindit"
)

data class TelegramConfig(
    val botToken: String,
    val webhookUrl: String,
    val parseMode: String = "HTML",
    val disableWebPagePreview: Boolean = false
)

data class EmailConfig(
    val provider: EmailProvider,
    val apiKey: String,
    val fromEmail: String,
    val fromName: String = "DrMindit",
    val replyTo: String? = null
)

enum class EmailProvider {
    SENDGRID,
    SMTP,
    AWS_SES,
    MAILGUN
}

// Notification templates
object NotificationTemplates {
    val DAILY_REMINDER = NotificationTemplate(
        id = "daily_reminder",
        name = "Daily Wellness Reminder",
        type = NotificationType.DAILY_REMINDER,
        channels = setOf(
            NotificationChannel.IN_APP,
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.WHATSAPP,
            NotificationChannel.TELEGRAM,
            NotificationChannel.EMAIL
        ),
        titleTemplate = "Hi {{name}}, time to relax! 🌿",
        bodyTemplate = "Take 5 minutes for your mental wellness today. Your mind will thank you.",
        variables = setOf("name"),
        metadata = mapOf(
            "category" to "daily_wellness",
            "importance" to "medium"
        )
    )
    
    val SESSION_REMINDER = NotificationTemplate(
        id = "session_reminder",
        name = "Session Reminder",
        type = NotificationType.SESSION_REMINDER,
        channels = setOf(
            NotificationChannel.IN_APP,
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.WHATSAPP,
            NotificationChannel.TELEGRAM
        ),
        titleTemplate = "{{session_title}} is waiting for you",
        bodyTemplate = "Your {{duration}} minute session with {{instructor}} is ready. Take a moment for yourself.",
        variables = setOf("session_title", "duration", "instructor"),
        metadata = mapOf(
            "category" to "session",
            "importance" to "high"
        )
    )
    
    val STREAK_REMINDER = NotificationTemplate(
        id = "streak_reminder",
        name = "Streak Reminder",
        type = NotificationType.STREAK_REMINDER,
        channels = setOf(
            NotificationChannel.IN_APP,
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.EMAIL
        ),
        titleTemplate = "{{streak_count}} day streak! 🔥",
        bodyTemplate = "You're on fire! Keep up the amazing work with today's wellness session.",
        variables = setOf("streak_count"),
        metadata = mapOf(
            "category" to "achievement",
            "importance" to "medium"
        )
    )
    
    val RE_ENGAGEMENT = NotificationTemplate(
        id = "re_engagement",
        name = "Re-engagement",
        type = NotificationType.RE_ENGAGEMENT,
        channels = setOf(
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.EMAIL,
            NotificationChannel.WHATSAPP
        ),
        titleTemplate = "We miss you, {{name}}! 🌟",
        bodyTemplate = "It's been {{days_since_last_visit}} days since your last session. Your wellness journey continues when you're ready.",
        variables = setOf("name", "days_since_last_visit"),
        metadata = mapOf(
            "category" to "engagement",
            "importance" to "low"
        )
    )
    
    val SLEEP_REMINDER = NotificationTemplate(
        id = "sleep_reminder",
        name = "Sleep Reminder",
        type = NotificationType.SLEEP_REMINDER,
        channels = setOf(
            NotificationChannel.IN_APP,
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.WHATSAPP
        ),
        titleTemplate = "Time for sweet dreams 😴",
        bodyTemplate = "Wind down with our sleep meditation for a restful night. Sweet dreams await.",
        variables = emptySet(),
        metadata = mapOf(
            "category" to "sleep",
            "importance" to "medium"
        )
    )
    
    val ANXIETY_SUPPORT = NotificationTemplate(
        id = "anxiety_support",
        name = "Anxiety Support",
        type = NotificationType.ANXIETY_SUPPORT,
        channels = setOf(
            NotificationChannel.IN_APP,
            NotificationChannel.PUSH_NOTIFICATION,
            NotificationChannel.EMAIL
        ),
        titleTemplate = "Feeling anxious? We're here for you 💙",
        bodyTemplate = "Take a deep breath. We have guided exercises to help you find calm when anxiety strikes.",
        variables = emptySet(),
        metadata = mapOf(
            "category" to "anxiety",
            "importance" to "high"
        )
    )
    
    val WEEKLY_REPORT = NotificationTemplate(
        id = "weekly_report",
        name = "Weekly Wellness Report",
        type = NotificationType.WEEKLY_REPORT,
        channels = setOf(NotificationChannel.EMAIL),
        titleTemplate = "Your weekly wellness report 📊",
        bodyTemplate = "You completed {{sessions_completed}} sessions this week! {{encouragement_message}}",
        variables = setOf("sessions_completed", "encouragement_message"),
        metadata = mapOf(
            "category" to "report",
            "importance" to "low"
        )
    )
}
