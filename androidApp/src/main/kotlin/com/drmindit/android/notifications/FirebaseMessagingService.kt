package com.drmindit.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications
 */
@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var notificationManager: NotificationManager
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val preferences = notificationManager.notificationPreferences.first()
        
        if (!preferences.pushNotifications) return
        
        // Handle different message types
        when (remoteMessage.data["type"]) {
            "daily_reminder" -> handleDailyReminder(remoteMessage)
            "session_reminder" -> handleSessionReminder(remoteMessage)
            "streak_achievement" -> handleStreakAchievement(remoteMessage)
            "personalized_nudge" -> handlePersonalizedNudge(remoteMessage)
            "app_update" -> handleAppUpdate(remoteMessage)
            else -> handleGeneralMessage(remoteMessage)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Store new FCM token
        notificationManager.secureConfigManager.storeFCMToken(token)
        
        // Update backend with new token
        updateFCMTokenInBackend(token)
    }
    
    private fun handleDailyReminder(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Daily Reminder"
        val message = remoteMessage.data["message"] ?: "Time for your daily meditation!"
        
        sendNotification(
            title = title,
            message = message,
            type = "daily_reminder",
            data = remoteMessage.data
        )
    }
    
    private fun handleSessionReminder(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Session Reminder"
        val message = remoteMessage.data["message"] ?: "Your meditation session is starting soon!"
        val sessionId = remoteMessage.data["session_id"] ?: ""
        
        sendNotification(
            title = title,
            message = message,
            type = "session_reminder",
            data = remoteMessage.data + mapOf("session_id" to sessionId)
        )
    }
    
    private fun handleStreakAchievement(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Streak Achievement!"
        val message = remoteMessage.data["message"] ?: "Congratulations on your milestone!"
        val streak = remoteMessage.data["streak"] ?: "0"
        
        sendNotification(
            title = title,
            message = message,
            type = "streak_achievement",
            data = remoteMessage.data + mapOf("streak" to streak)
        )
    }
    
    private fun handlePersonalizedNudge(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Mindful Moment"
        val message = remoteMessage.data["message"] ?: "How about a quick meditation?"
        val nudgeType = remoteMessage.data["nudge_type"] ?: "general"
        
        sendNotification(
            title = title,
            message = message,
            type = "personalized_nudge",
            data = remoteMessage.data + mapOf("nudge_type" to nudgeType)
        )
    }
    
    private fun handleAppUpdate(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "App Update"
        val message = remoteMessage.data["message"] ?: "New features available!"
        
        sendNotification(
            title = title,
            message = message,
            type = "app_update",
            data = remoteMessage.data
        )
    }
    
    private fun handleGeneralMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val message = remoteMessage.notification?.body ?: remoteMessage.data["message"]
        
        if (title != null && message != null) {
            sendNotification(
                title = title,
                message = message,
                type = "general",
                data = remoteMessage.data
            )
        }
    }
    
    private fun sendNotification(
        title: String,
        message: String,
        type: String,
        data: Map<String, String>
    ) {
        val channelId = when (type) {
            "daily_reminder" -> "daily_reminders"
            "session_reminder" -> "session_reminders"
            "streak_achievement" -> "streak_achievements"
            "personalized_nudge" -> "daily_reminders"
            "app_update" -> "push_notifications"
            else -> "push_notifications"
        }
        
        // Create notification channel if needed
        createNotificationChannel(channelId)
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_default)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent(type, data))
            .build()
        
        val notificationId = when (type) {
            "daily_reminder" -> 1001
            "session_reminder" -> 1002
            "streak_achievement" -> 1003
            "personalized_nudge" -> 1004
            "app_update" -> 1005
            else -> 1000
        }
        
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
    
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = when (channelId) {
                "daily_reminders" -> "Daily Reminders"
                "session_reminders" -> "Session Reminders"
                "streak_achievements" -> "Streak Achievements"
                "push_notifications" -> "Push Notifications"
                else -> "Notifications"
            }
            
            val channelDescription = when (channelId) {
                "daily_reminders" -> "Daily meditation reminders"
                "session_reminders" -> "Meditation session reminders"
                "streak_achievements" -> "Streak milestones and achievements"
                "push_notifications" -> "App notifications and updates"
                else -> "App notifications"
            }
            
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }
            
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
    }
    
    private fun createPendingIntent(type: String, data: Map<String, String>): android.app.PendingIntent {
        val intent = android.content.Intent(this, com.drmindit.android.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        return android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun updateFCMTokenInBackend(token: String) {
        // This would integrate with your backend service
        // For now, just logging the token update
        println("FCM token updated: $token")
    }
}
