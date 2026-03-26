package com.drmindit.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.drmindit.android.MainActivity
import com.drmindit.shared.domain.model.UserPreferences
import com.drmindit.android.config.SecureConfigManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive Notification Manager
 * Handles in-app, push, email, and third-party integrations
 */
@Singleton
class NotificationManager @Inject constructor(
    private val context: Context,
    private val secureConfigManager: SecureConfigManager,
    private val workManager: WorkManager
) {
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val firebaseMessaging = FirebaseMessaging.getInstance()
    
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    val notificationPreferences: StateFlow<NotificationPreferences> = _notificationPreferences.asStateFlow()
    
    private val _streakData = MutableStateFlow(StreakData())
    val streakData: StateFlow<StreakData> = _streakData.asStateFlow()
    
    private val _dailyProgress = MutableStateFlow(DailyProgress())
    val dailyProgress: StateFlow<DailyProgress> = _dailyProgress.asStateFlow()
    
    init {
        createNotificationChannels()
        initializeFirebase()
    }
    
    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Session reminders
            val sessionChannel = NotificationChannel(
                SESSION_REMINDER_CHANNEL_ID,
                "Session Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for meditation sessions"
                enableLights(true)
                enableVibration(true)
            }
            
            // Daily reminders
            val dailyChannel = NotificationChannel(
                DAILY_REMINDER_CHANNEL_ID,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily meditation reminders"
                enableLights(true)
                enableVibration(true)
            }
            
            // Streak notifications
            val streakChannel = NotificationChannel(
                STREAK_CHANNEL_ID,
                "Streak Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Streak milestones and achievements"
                enableLights(true)
                enableVibration(true)
            }
            
            // Push notifications
            val pushChannel = NotificationChannel(
                PUSH_NOTIFICATION_CHANNEL_ID,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "App notifications and updates"
                enableLights(true)
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannels(
                listOf(sessionChannel, dailyChannel, streakChannel, pushChannel)
            )
        }
    }
    
    /**
     * Initialize Firebase Cloud Messaging
     */
    private fun initializeFirebase() {
        firebaseMessaging.token.addOnSuccessListener { token ->
            // Store FCM token for push notifications
            secureConfigManager.storeFCMToken(token)
        }
        
        firebaseMessaging.subscribeToTopic("daily_reminders")
        firebaseMessaging.subscribeToTopic("session_updates")
    }
    
    /**
     * Schedule daily meditation reminder
     */
    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).apply {
            setInputData(
                workDataOf(
                    "hour" to hour,
                    "minute" to minute,
                    "type" to "daily_reminder"
                )
            )
            setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }.build()
        
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
    
    /**
     * Schedule session reminder
     */
    fun scheduleSessionReminder(sessionId: String, sessionTitle: String, scheduledTime: Long) {
        val delay = scheduledTime - System.currentTimeMillis()
        
        val sessionWorkRequest = OneTimeWorkRequestBuilder<SessionReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "session_id" to sessionId,
                    "session_title" to sessionTitle,
                    "scheduled_time" to scheduledTime
                )
            )
            .build()
        
        workManager.enqueueUniqueWork(
            "session_reminder_$sessionId",
            ExistingWorkPolicy.REPLACE,
            sessionWorkRequest
        )
    }
    
    /**
     * Send streak achievement notification
     */
    fun sendStreakNotification(streak: Int, milestone: String) {
        if (!_notificationPreferences.value.streakNotifications) return
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "streak")
            putExtra("streak", streak)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, STREAK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_streak)
            .setContentTitle("🎉 Streak Milestone!")
            .setContentText("Congratulations! You've maintained a $streak-day streak: $milestone")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Congratulations! You've maintained a $streak-day streak: $milestone\n\nKeep up the amazing work!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_share,
                "Share",
                createShareIntent(streak, milestone)
            )
            .build()
        
        notificationManager.notify(STREAK_NOTIFICATION_ID, notification)
    }
    
    /**
     * Send personalized nudge notification
     */
    fun sendPersonalizedNudge(userState: UserState) {
        if (!_notificationPreferences.value.personalizedNudges) return
        
        val nudgeContent = generatePersonalizedNudge(userState)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "nudge")
            putExtra("nudge_type", userState.type)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, DAILY_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_nudge)
            .setContentTitle(nudgeContent.title)
            .setContentText(nudgeContent.message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(nudgeContent.message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NUDGE_NOTIFICATION_ID, notification)
    }
    
    /**
     * Send push notification via Firebase
     */
    suspend fun sendPushNotification(
        userId: String,
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ) {
        if (!_notificationPreferences.value.pushNotifications) return
        
        // This would integrate with Firebase Cloud Messaging
        // For now, showing local notification as fallback
        sendLocalNotification(title, message, data)
    }
    
    /**
     * Send email notification via backend service
     */
    suspend fun sendEmailNotification(
        userEmail: String,
        subject: String,
        content: String,
        template: String = "default"
    ) {
        if (!_notificationPreferences.value.emailNotifications) return
        
        // This would integrate with backend email service
        // For now, just logging the email would be sent
        println("Email would be sent to $userEmail: $subject")
    }
    
    /**
     * Send WhatsApp notification via Twilio
     */
    suspend fun sendWhatsAppNotification(
        phoneNumber: String,
        message: String,
        template: String = "default"
    ) {
        if (!_notificationPreferences.value.whatsappNotifications) return
        
        // This would integrate with Twilio WhatsApp API
        // For now, just logging the message would be sent
        println("WhatsApp message would be sent to $phoneNumber: $message")
    }
    
    /**
     * Send Telegram notification via bot
     */
    suspend fun sendTelegramNotification(
        chatId: String,
        message: String,
        parseMode: String = "HTML"
    ) {
        if (!_notificationPreferences.value.telegramNotifications) return
        
        // This would integrate with Telegram Bot API
        // For now, just logging the message would be sent
        println("Telegram message would be sent to $chatId: $message")
    }
    
    /**
     * Update notification preferences
     */
    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        _notificationPreferences.value = preferences
        
        // Update Firebase subscriptions based on preferences
        updateFirebaseSubscriptions(preferences)
    }
    
    /**
     * Update streak data
     */
    fun updateStreakData(streak: Int, lastActiveDate: Long, milestones: List<String>) {
        _streakData.value = StreakData(
            currentStreak = streak,
            lastActiveDate = lastActiveDate,
            milestones = milestones
        )
        
        // Check for new milestones
        checkStreakMilestones(streak)
    }
    
    /**
     * Update daily progress
     */
    fun updateDailyProgress(
        sessionsCompleted: Int,
        totalSessions: Int,
        minutesMeditated: Int,
        targetMinutes: Int
    ) {
        _dailyProgress.value = DailyProgress(
            sessionsCompleted = sessionsCompleted,
            totalSessions = totalSessions,
            minutesMeditated = minutesMeditated,
            targetMinutes = targetMinutes,
            completionPercentage = if (targetMinutes > 0) {
                (minutesMeditated.toFloat() / targetMinutes * 100).toInt()
            } else 0
        )
        
        // Send progress notifications if enabled
        if (_notificationPreferences.value.progressNotifications) {
            checkProgressMilestones()
        }
    }
    
    /**
     * Generate personalized nudge content
     */
    private fun generatePersonalizedNudge(userState: UserState): NudgeContent {
        return when (userState.type) {
            "missed_session" -> NudgeContent(
                title = "Miss Session Today?",
                message = "It's okay to miss a session! Even 5 minutes of mindfulness can help. Would you like to try a quick breathing exercise?"
            )
            "low_activity" -> NudgeContent(
                title = "Time for Mindfulness?",
                message = "You haven't meditated today. A short session could help reduce stress and improve focus. Ready to begin?"
            )
            "streak_risk" -> NudgeContent(
                title = "Protect Your Streak!",
                message = "You're close to breaking your meditation streak! Just 5 minutes today will keep it going. You can do this!"
            )
            "celebration" -> NudgeContent(
                title = "Amazing Progress!",
                message = "You've been consistent with your practice! Your dedication to mental wellness is inspiring. Keep up the great work!"
            )
            else -> NudgeContent(
                title = "Mindful Moment",
                message = "Taking a moment for yourself can make a big difference. How about a quick meditation session?"
            )
        }
    }
    
    /**
     * Check for streak milestones
     */
    private fun checkStreakMilestones(streak: Int) {
        val milestones = listOf(3, 7, 14, 21, 30, 60, 90, 180, 365)
        
        if (milestones.contains(streak)) {
            val milestoneText = when (streak) {
                3 -> "First week completed!"
                7 -> "One week streak!"
                14 -> "Two week streak!"
                21 -> "Three week streak!"
                30 -> "One month streak!"
                60 -> "Two month streak!"
                90 -> "Three month streak!"
                180 -> "Six month streak!"
                365 -> "One year streak!"
                else -> "$streak days!"
            }
            
            sendStreakNotification(streak, milestoneText)
        }
    }
    
    /**
     * Check for progress milestones
     */
    private fun checkProgressMilestones() {
        val progress = _dailyProgress.value
        
        if (progress.completionPercentage >= 100 && progress.sessionsCompleted > 0) {
            sendLocalNotification(
                "Daily Goal Achieved! 🎯",
                "You've completed your daily meditation goal! You've meditated for ${progress.minutesMeditated} minutes today."
            )
        }
    }
    
    /**
     * Update Firebase subscriptions
     */
    private fun updateFirebaseSubscriptions(preferences: NotificationPreferences) {
        if (preferences.pushNotifications) {
            firebaseMessaging.subscribeToTopic("daily_reminders")
            firebaseMessaging.subscribeToTopic("session_updates")
            firebaseMessaging.subscribeToTopic("streak_achievements")
        } else {
            firebaseMessaging.unsubscribeFromTopic("daily_reminders")
            firebaseMessaging.unsubscribeFromTopic("session_updates")
            firebaseMessaging.unsubscribeFromTopic("streak_achievements")
        }
    }
    
    /**
     * Send local notification
     */
    private fun sendLocalNotification(
        title: String,
        message: String,
        data: Map<String, String> = emptyMap()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, PUSH_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_default)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Create share intent for streak achievements
     */
    private fun createShareIntent(streak: Int, milestone: String): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "I've maintained a $streak-day meditation streak with DrMindit! $milestone 🧘‍♂️")
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share your achievement")
        return PendingIntent.getActivity(
            context,
            0,
            chooserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    companion object {
        // Notification Channel IDs
        private const val SESSION_REMINDER_CHANNEL_ID = "session_reminders"
        private const val DAILY_REMINDER_CHANNEL_ID = "daily_reminders"
        private const val STREAK_CHANNEL_ID = "streak_achievements"
        private const val PUSH_NOTIFICATION_CHANNEL_ID = "push_notifications"
        
        // Notification IDs
        private const val NOTIFICATION_ID = 1
        private const val STREAK_NOTIFICATION_ID = 2
        private const val NUDGE_NOTIFICATION_ID = 3
    }
}

/**
 * Notification preferences data class
 */
data class NotificationPreferences(
    val inAppNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val whatsappNotifications: Boolean = false,
    val telegramNotifications: Boolean = false,
    val dailyReminders: Boolean = true,
    val sessionReminders: Boolean = true,
    val streakNotifications: Boolean = true,
    val progressNotifications: Boolean = true,
    val personalizedNudges: Boolean = true,
    val reminderTime: String = "09:00",
    val quietHours: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)

/**
 * Streak tracking data
 */
data class StreakData(
    val currentStreak: Int = 0,
    val lastActiveDate: Long = 0,
    val milestones: List<String> = emptyList()
)

/**
 * Daily progress tracking
 */
data class DailyProgress(
    val sessionsCompleted: Int = 0,
    val totalSessions: Int = 0,
    val minutesMeditated: Int = 0,
    val targetMinutes: Int = 15,
    val completionPercentage: Int = 0
)

/**
 * User state for personalized nudges
 */
data class UserState(
    val type: String,
    val data: Map<String, Any> = emptyMap()
)

/**
 * Nudge content for notifications
 */
data class NudgeContent(
    val title: String,
    val message: String
)
