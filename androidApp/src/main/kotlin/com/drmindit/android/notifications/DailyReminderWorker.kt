package com.drmindit.android.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.android.HiltWorker
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Background worker for daily meditation reminders
 */
@HiltWorker
class DailyReminderWorker @Inject constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val notificationManager: NotificationManager
) : Worker(context, workerParams) {
    
    override fun doWork(): Result {
        return try {
            val hour = inputData.getInt("hour", 9)
            val minute = inputData.getInt("minute", 0)
            val type = inputData.getString("type") ?: "daily_reminder"
            
            when (type) {
                "daily_reminder" -> sendDailyReminder()
                "progress_check" -> sendProgressCheck()
                "streak_check" -> sendStreakCheck()
                else -> sendDailyReminder()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun sendDailyReminder() {
        val preferences = notificationManager.notificationPreferences.first()
        
        if (preferences.dailyReminders && !isInQuietHours(preferences)) {
            val reminderContent = generateDailyReminderContent()
            
            notificationManager.sendLocalNotification(
                reminderContent.title,
                reminderContent.message,
                mapOf(
                    "notification_type" to "daily_reminder",
                    "reminder_time" to System.currentTimeMillis()
                )
            )
        }
    }
    
    private fun sendProgressCheck() {
        val progress = notificationManager.dailyProgress.first()
        val preferences = notificationManager.notificationPreferences.first()
        
        if (preferences.progressNotifications && !isInQuietHours(preferences)) {
            when {
                progress.completionPercentage == 0 -> {
                    notificationManager.sendPersonalizedNudge(
                        UserState("low_activity", mapOf("progress" to progress))
                    )
                }
                progress.completionPercentage >= 100 -> {
                    notificationManager.sendLocalNotification(
                        "Daily Goal Achieved! 🎯",
                        "You've completed your daily meditation goal! Great job!"
                    )
                }
            }
        }
    }
    
    private fun sendStreakCheck() {
        val streak = notificationManager.streakData.first()
        val preferences = notificationManager.notificationPreferences.first()
        
        if (preferences.streakNotifications && !isInQuietHours(preferences)) {
            val daysSinceLastActive = calculateDaysSinceLastActive(streak.lastActiveDate)
            
            when {
                daysSinceLastActive == 1 -> {
                    notificationManager.sendPersonalizedNudge(
                        UserState("streak_risk", mapOf("streak" to streak.currentStreak))
                    )
                }
                daysSinceLastActive >= 2 -> {
                    notificationManager.sendLocalNotification(
                        "Missed Sessions",
                        "You've missed meditation for $daysSinceLastActive days. Ready to get back on track?"
                    )
                }
            }
        }
    }
    
    private fun generateDailyReminderContent(): NudgeContent {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> NudgeContent(
                title = "Morning Mindfulness ☀️",
                message = "Good morning! Start your day with a moment of peace. A meditation session can help set a positive tone for today."
            )
            in 12..17 -> NudgeContent(
                title = "Afternoon Break 🌤️",
                message = "Take a mindful moment this afternoon. A short meditation can help reduce stress and improve focus."
            )
            in 18..22 -> NudgeContent(
                title = "Evening Reflection 🌙",
                message = "Wind down your day with peaceful meditation. Let go of today's stress and prepare for restful sleep."
            )
            else -> NudgeContent(
                title = "Nighttime Peace 🌟",
                message = "Even late at night, a few minutes of mindfulness can help calm your mind. Ready for a peaceful session?"
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
    
    private fun calculateDaysSinceLastActive(lastActiveDate: Long): Int {
        val now = System.currentTimeMillis()
        val diffInMillis = now - lastActiveDate
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}
