package com.drmindit.android.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.HiltWorker
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Background worker for session-specific reminders
 */
@HiltWorker
class SessionReminderWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val notificationManager: NotificationManager
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        return try {
            val sessionId = inputData.getString("session_id") ?: ""
            val sessionTitle = inputData.getString("session_title") ?: ""
            val scheduledTime = inputData.getLong("scheduled_time", 0)
            
            sendSessionReminder(sessionId, sessionTitle, scheduledTime)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun sendSessionReminder(sessionId: String, sessionTitle: String, scheduledTime: Long) {
        val preferences = notificationManager.notificationPreferences.first()
        
        if (preferences.sessionReminders && !isInQuietHours(preferences)) {
            val reminderContent = generateSessionReminderContent(sessionTitle, scheduledTime)
            
            notificationManager.sendLocalNotification(
                reminderContent.title,
                reminderContent.message,
                mapOf(
                    "notification_type" to "session_reminder",
                    "session_id" to sessionId,
                    "session_title" to sessionTitle
                )
            )
        }
    }
    
    private fun generateSessionReminderContent(sessionTitle: String, scheduledTime: Long): NudgeContent {
        val timeUntilSession = scheduledTime - System.currentTimeMillis()
        val minutesUntil = timeUntilSession / (1000 * 60)
        
        return when {
            minutesUntil <= 15 -> NudgeContent(
                title = "Session Starting Soon! ⏰",
                message = "Your meditation session '$sessionTitle' starts in just a few minutes. Find a comfortable spot and prepare for mindfulness."
            )
            minutesUntil <= 60 -> NudgeContent(
                title = "Session Reminder 🧘‍♂️",
                message = "Your meditation session '$sessionTitle' starts in about an hour. Take a moment to prepare and set aside this time for yourself."
            )
            else -> NudgeContent(
                title = "Upcoming Session 📅",
                message = "Don't forget about your meditation session '$sessionTitle' scheduled for today. Mark your calendar and get ready for some peaceful time."
            )
        }
    }
    
    private fun isInQuietHours(preferences: NotificationPreferences): Boolean {
        if (!preferences.quietHours) return false
        
        val currentTime = java.util.Calendar.getInstance()
        val currentHour = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(java.util.Calendar.MINUTE)
        val currentMinutes = currentHour * 60 + currentMinute
        
        val quietStart = preferences.quietHoursStart.split(":")
        val quietEnd = preferences.quietHoursEnd.split(":")
        
        val quietStartMinutes = quietStart[0].toInt() * 60 + quietStart[1].toInt()
        val quietEndMinutes = quietEnd[0].toInt() * 60 + quietEnd[1].toInt()
        
        return if (quietStartMinutes <= quietEndMinutes) {
            currentMinutes in quietStartMinutes..quietEndMinutes
        } else {
            currentMinutes >= quietStartMinutes || currentMinutes <= quietEndMinutes
        }
    }
}
