package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Service
class NotificationSchedulingService(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val notificationService: NotificationService,
    private val analyticsService: NotificationAnalyticsService,
    @Value("\${notification.timezone.default}") private val defaultTimezone: String
) {
    
    private val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(10)
    private val userTimeZones = ConcurrentHashMap<String, String>()
    private val userOptimalTimes = ConcurrentHashMap<String, Long>()
    private val userEngagementPatterns = ConcurrentHashMap<String, UserEngagementPattern>()
    
    // Main scheduler that runs every minute
    @Scheduled(fixedDelay = 60000) // Every minute
    fun processScheduledNotifications() {
        try {
            val now = System.currentTimeMillis()
            val dueSchedules = getDueSchedules(now)
            
            dueSchedules.forEach { schedule ->
                processSchedule(schedule)
            }
        } catch (e: Exception) {
            println("Error processing scheduled notifications: ${e.message}")
        }
    }
    
    // Daily optimization at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    fun optimizeSendTimes() {
        try {
            optimizeUserSendTimes()
            updateEngagementPatterns()
        } catch (e: Exception) {
            println("Error optimizing send times: ${e.message}")
        }
    }
    
    // Weekly report generation
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    fun generateWeeklyReports() {
        try {
            generateUserWeeklyReports()
        } catch (e: Exception) {
            println("Error generating weekly reports: ${e.message}")
        }
    }
    
    // Streak reminders at 8 PM
    @Scheduled(cron = "0 0 20 * * ?")
    fun sendStreakReminders() {
        try {
            sendStreakReminderNotifications()
        } catch (e: Exception) {
            println("Error sending streak reminders: ${e.message}")
        }
    }
    
    // Sleep reminders at 9 PM
    @Scheduled(cron = "0 0 21 * * ?")
    fun sendSleepReminders() {
        try {
            sendSleepReminderNotifications()
        } catch (e: Exception) {
            println("Error sending sleep reminders: ${e.message}")
        }
    }
    
    // Re-engagement campaign
    @Scheduled(cron = "0 0 10 * * ?") // Daily at 10 AM
    fun runReEngagementCampaign() {
        try {
            sendReEngagementNotifications()
        } catch (e: Exception) {
            println("Error running re-engagement campaign: ${e.message}")
        }
    }
    
    private fun getDueSchedules(now: Long): List<Map<String, Any>> {
        val schedules = mutableListOf<Map<String, Any>>()
        
        // Get all active schedules from Redis
        val scheduleKeys = redisTemplate.keys("schedule:*") ?: emptyList()
        
        scheduleKeys.forEach { key ->
            val schedule = redisTemplate.opsForValue().get(key) as? Map<String, Any>
            if (schedule != null && schedule["isActive"] == true) {
                val nextRun = schedule["nextRun"] as? Long ?: 0L
                if (nextRun <= now) {
                    schedules.add(schedule)
                }
            }
        }
        
        return schedules
    }
    
    private fun processSchedule(schedule: Map<String, Any>) {
        val userId = schedule["userId"] as? String ?: return
        val templateId = schedule["templateId"] as? String ?: return
        val variables = schedule["variables"] as? Map<String, String> ?: emptyMap()
        val scheduleConfig = schedule["schedule"] as? Map<String, Any> ?: emptyMap()
        
        // Check user preferences and quiet hours
        if (!shouldSendNotification(userId, scheduleConfig)) {
            // Reschedule for next valid time
            rescheduleNotification(schedule)
            return
        }
        
        // Personalize variables
        val personalizedVariables = personalizeVariables(userId, variables, templateId)
        
        // Create and send notification
        val notification = createNotificationFromTemplate(userId, templateId, personalizedVariables)
        if (notification != null) {
            sendNotificationWithPersonalization(notification, userId)
            
            // Update next run time
            updateNextRunTime(schedule)
            
            // Track engagement
            trackScheduledNotificationSent(schedule, notification)
        }
    }
    
    private fun shouldSendNotification(userId: String, scheduleConfig: Map<String, Any>): Boolean {
        // Check quiet hours
        val isQuietHours = isUserQuietHours(userId)
        if (isQuietHours) {
            val quietHoursConfig = getQuietHoursConfig(userId)
            val allowEmergency = quietHoursConfig?.get("allowEmergency") as? Boolean ?: false
            val isEmergency = scheduleConfig["isEmergency"] as? Boolean ?: false
            
            if (!allowEmergency || !isEmergency) {
                return false
            }
        }
        
        // Check rate limiting
        if (isRateLimited(userId, scheduleConfig)) {
            return false
        }
        
        // Check user preferences
        val preferences = getUserPreferences(userId)
        if (preferences != null) {
            val topic = getTopicFromSchedule(scheduleConfig)
            if (topic != null && !preferences.isTopicEnabled(topic)) {
                return false
            }
        }
        
        return true
    }
    
    private fun personalizeVariables(userId: String, variables: Map<String, String>, templateId: String): Map<String, String> {
        val personalized = variables.toMutableMap()
        
        // Add user-specific variables
        val userInfo = getUserInfo(userId)
        personalized["name"] = userInfo["name"] ?: "there"
        personalized["firstName"] = userInfo["firstName"] ?: ""
        
        // Add contextual variables based on user behavior
        when (templateId) {
            "daily_reminder" -> {
                personalized["greeting"] = getPersonalizedGreeting(userId)
                personalized["suggestion"] = getPersonalizedSuggestion(userId)
            }
            "streak_reminder" -> {
                personalized["streak_count"] = getUserStreakCount(userId).toString()
                personalized["encouragement"] = getStreakEncouragement(userId)
            }
            "re_engagement" -> {
                personalized["days_since_last_visit"] = getDaysSinceLastVisit(userId).toString()
                personalized["missed_sessions"] = getMissedSessionCount(userId).toString()
            }
            "weekly_report" -> {
                val weeklyStats = getWeeklyStats(userId)
                personalized["sessions_completed"] = weeklyStats["sessionsCompleted"]?.toString() ?: "0"
                personalized["total_minutes"] = weeklyStats["totalMinutes"]?.toString() ?: "0"
                personalized["favorite_category"] = weeklyStats["favoriteCategory"] ?: "meditation"
                personalized["encouragement_message"] = getWeeklyEncouragement(weeklyStats)
            }
        }
        
        return personalized
    }
    
    private fun createNotificationFromTemplate(userId: String, templateId: String, variables: Map<String, String>): Map<String, Any>? {
        val template = getNotificationTemplate(templateId) ?: return null
        
        val title = replaceTemplateVariables(template["titleTemplate"] as? String ?: "", variables)
        val body = replaceTemplateVariables(template["bodyTemplate"] as? String ?: "", variables)
        
        return mapOf(
            "id" to "notif_${System.currentTimeMillis()}_${(1000..9999).random()}",
            "userId" to userId,
            "type" to template["type"],
            "title" to title,
            "body" to body,
            "channels" to template["channels"],
            "data" to variables,
            "priority" to (template["priority"] ?: "NORMAL"),
            "scheduledAt" to System.currentTimeMillis(),
            "createdAt" to System.currentTimeMillis(),
            "status" to "PENDING",
            "metadata" to mapOf(
                "templateId" to templateId,
                "personalizationScore" to calculatePersonalizationScore(userId, variables),
                "timezone" to getUserTimezone(userId)
            )
        )
    }
    
    private fun sendNotificationWithPersonalization(notification: Map<String, Any>, userId: String) {
        // Send through notification service
        val result = notificationService.sendNotification(notification)
        
        if (result.isSuccess) {
            // Update user engagement pattern
            updateUserEngagementPattern(userId, notification)
            
            // Update optimal send time
            updateOptimalSendTime(userId, System.currentTimeMillis())
        }
    }
    
    private fun rescheduleNotification(schedule: Map<String, Any>) {
        val scheduleConfig = schedule["schedule"] as? Map<String, Any> ?: return
        val userId = schedule["userId"] as? String ?: return
        
        // Calculate next valid send time
        val nextValidTime = calculateNextValidSendTime(userId, scheduleConfig)
        
        val updatedSchedule = schedule.toMutableMap()
        updatedSchedule["nextRun"] = nextValidTime
        
        // Update in Redis
        val scheduleId = schedule["id"] as? String ?: return
        redisTemplate.opsForValue().set(
            "schedule:$scheduleId",
            updatedSchedule,
            java.time.Duration.ofDays(365)
        )
    }
    
    private fun updateNextRunTime(schedule: Map<String, Any>) {
        val scheduleConfig = schedule["schedule"] as? Map<String, Any> ?: return
        val scheduleType = scheduleConfig["type"] as? String ?: return
        
        val nextRun = when (scheduleType) {
            "RECURRING" -> calculateNextRecurringTime(scheduleConfig)
            "DAILY" -> calculateNextDailyTime(scheduleConfig)
            "WEEKLY" -> calculateNextWeeklyTime(scheduleConfig)
            "MONTHLY" -> calculateNextMonthlyTime(scheduleConfig)
            else -> System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // Default to tomorrow
        }
        
        val scheduleId = schedule["id"] as? String ?: return
        val updatedSchedule = schedule.toMutableMap()
        updatedSchedule["nextRun"] = nextRun
        updatedSchedule["lastRun"] = System.currentTimeMillis()
        
        redisTemplate.opsForValue().set(
            "schedule:$scheduleId",
            updatedSchedule,
            java.time.Duration.ofDays(365)
        )
    }
    
    private fun calculateNextValidSendTime(userId: String, scheduleConfig: Map<String, Any>): Long {
        val now = System.currentTimeMillis()
        val userTimezone = getUserTimezone(userId)
        val userZonedDateTime = Instant.ofEpochMilli(now).atZone(ZoneId.of(userTimezone))
        
        // Get quiet hours
        val quietHours = getQuietHoursConfig(userId)
        if (quietHours != null) {
            val startTime = LocalTime.parse(quietHours["startTime"] as String)
            val endTime = LocalTime.parse(quietHours["endTime"] as String)
            
            val scheduledTime = getScheduledTimeFromConfig(scheduleConfig)
            var scheduledZonedDateTime = userZonedDateTime.with(
                scheduledTime.hour,
                scheduledTime.minute,
                scheduledTime.second
            )
            
            // If scheduled time is during quiet hours, move to after quiet hours
            if (isTimeInQuietHours(scheduledZonedDateTime.toLocalTime(), startTime, endTime)) {
                scheduledZonedDateTime = if (startTime.isBefore(endTime)) {
                    // Same day quiet hours (e.g., 22:00 - 08:00)
                    if (scheduledZonedDateTime.toLocalTime().isBefore(startTime)) {
                        // Schedule for after quiet hours same day
                        scheduledZonedDateTime.withHour(endTime.hour).withMinute(endTime.minute)
                    } else {
                        // Schedule for next day after quiet hours
                        scheduledZonedDateTime.plusDays(1).withHour(endTime.hour).withMinute(endTime.minute)
                    }
                } else {
                    // Overnight quiet hours - schedule for next day
                    scheduledZonedDateTime.plusDays(1).withHour(scheduledTime.hour).withMinute(scheduledTime.minute)
                }
            }
            
            return scheduledZonedDateTime.toInstant().toEpochMilli()
        }
        
        return now + (60 * 60 * 1000L) // Default to 1 hour later
    }
    
    private fun optimizeUserSendTimes() {
        // Analyze user engagement patterns to find optimal send times
        val userKeys = redisTemplate.keys("user_engagement:*") ?: emptyList()
        
        userKeys.forEach { key ->
            val userId = key.substringAfter("user_engagement:")
            val pattern = getUserEngagementPattern(userId)
            
            if (pattern != null) {
                val optimalTime = calculateOptimalSendTime(pattern)
                userOptimalTimes[userId] = optimalTime
            }
        }
    }
    
    private fun updateEngagementPatterns() {
        // Update engagement patterns based on recent notification interactions
        val lastWeek = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        
        // This would typically query analytics database
        // For now, simulate pattern updates
    }
    
    private fun sendStreakReminderNotifications() {
        val users = getActiveUsers()
        
        users.forEach { userId ->
            val streakCount = getUserStreakCount(userId)
            if (streakCount > 0) {
                val variables = mapOf(
                    "name" to getUserInfo(userId)["name"] ?: "there",
                    "streak_count" to streakCount.toString()
                )
                
                val notification = createNotificationFromTemplate(userId, "streak_reminder", variables)
                if (notification != null) {
                    sendNotificationWithPersonalization(notification, userId)
                }
            }
        }
    }
    
    private fun sendSleepReminderNotifications() {
        val users = getActiveUsers()
        
        users.forEach { userId ->
            val preferences = getUserPreferences(userId)
            if (preferences?.isTopicEnabled(NotificationTopic.SLEEP) == true) {
                val variables = mapOf(
                    "name" to getUserInfo(userId)["name"] ?: "there"
                )
                
                val notification = createNotificationFromTemplate(userId, "sleep_reminder", variables)
                if (notification != null) {
                    sendNotificationWithPersonalization(notification, userId)
                }
            }
        }
    }
    
    private fun sendReEngagementNotifications() {
        val inactiveUsers = getInactiveUsers(3) // Users inactive for 3+ days
        
        inactiveUsers.forEach { userId ->
            val daysSinceLastVisit = getDaysSinceLastVisit(userId)
            if (daysSinceLastVisit >= 3) {
                val variables = mapOf(
                    "name" to getUserInfo(userId)["name"] ?: "there",
                    "days_since_last_visit" to daysSinceLastVisit.toString()
                )
                
                val notification = createNotificationFromTemplate(userId, "re_engagement", variables)
                if (notification != null) {
                    sendNotificationWithPersonalization(notification, userId)
                }
            }
        }
    }
    
    private fun generateUserWeeklyReports() {
        val users = getActiveUsers()
        val lastWeek = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        
        users.forEach { userId ->
            val weeklyStats = getWeeklyStats(userId)
            val variables = mapOf(
                "name" to getUserInfo(userId)["name"] ?: "there",
                "sessions_completed" to weeklyStats["sessionsCompleted"]?.toString() ?: "0",
                "total_minutes" to weeklyStats["totalMinutes"]?.toString() ?: "0",
                "favorite_category" to weeklyStats["favoriteCategory"] ?: "meditation",
                "encouragement_message" to getWeeklyEncouragement(weeklyStats)
            )
            
            val notification = createNotificationFromTemplate(userId, "weekly_report", variables)
            if (notification != null) {
                sendNotificationWithPersonalization(notification, userId)
            }
        }
    }
    
    // Helper functions
    private fun replaceTemplateVariables(template: String, variables: Map<String, String>): String {
        var result = template
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        return result
    }
    
    private fun calculatePersonalizationScore(userId: String, variables: Map<String, String>): Float {
        // Score based on how well variables match user preferences
        val userInfo = getUserInfo(userId)
        val name = variables["name"] ?: ""
        
        var score = 0.5f // Base score
        
        if (name.contains(userInfo["firstName"] ?: "", ignoreCase = true)) {
            score += 0.3f
        }
        
        if (variables.containsKey("personalized_suggestion")) {
            score += 0.2f
        }
        
        return score.coerceAtMost(1.0f)
    }
    
    private fun getPersonalizedGreeting(userId: String): String {
        val hour = LocalDateTime.now(ZoneId.of(getUserTimezone(userId))).hour
        
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..22 -> "Good evening"
            else -> "Hello"
        }
    }
    
    private fun getPersonalizedSuggestion(userId: String): String {
        val pattern = getUserEngagementPattern(userId)
        return when {
            pattern?.preferredSessionType == "sleep" -> "Try our sleep meditation tonight"
            pattern?.preferredSessionType == "anxiety" -> "Take a moment for anxiety relief"
            pattern?.preferredSessionType == "stress" -> "Release tension with a stress relief session"
            else -> "Take 5 minutes for your mental wellness"
        }
    }
    
    private fun getStreakEncouragement(userId: String): String {
        val streakCount = getUserStreakCount(userId)
        return when {
            streakCount >= 30 -> "Amazing month-long commitment! You're building life-changing habits."
            streakCount >= 14 -> "Two weeks of consistency! Your dedication is inspiring."
            streakCount >= 7 -> "One week strong! You're creating lasting positive change."
            streakCount >= 3 -> "Three days in a row! Keep up the great work!"
            else -> "Every day counts! You're doing amazing."
        }
    }
    
    private fun getWeeklyEncouragement(weeklyStats: Map<String, Any>): String {
        val sessionsCompleted = weeklyStats["sessionsCompleted"] as? Int ?: 0
        return when {
            sessionsCompleted >= 7 -> "Incredible consistency! You're making wellness a daily habit."
            sessionsCompleted >= 5 -> "Great week! You're building strong momentum."
            sessionsCompleted >= 3 -> "Good progress! Every session brings you closer to your goals."
            else -> "Every step counts. Ready to make next week even better?"
        }
    }
    
    private fun trackScheduledNotificationSent(schedule: Map<String, Any>, notification: Map<String, Any>) {
        val analyticsData = mapOf(
            "scheduleId" to schedule["id"],
            "notificationId" to notification["id"],
            "userId" to schedule["userId"],
            "templateId" to schedule["templateId"],
            "sentAt" to System.currentTimeMillis(),
            "personalizationScore" to notification["metadata"]?.let { meta ->
                (meta as Map<String, Any>)["personalizationScore"]
            }
        )
        
        analyticsService.trackScheduledNotification(analyticsData)
    }
    
    // Data access functions (would typically use database)
    private fun getUserPreferences(userId: String): NotificationPreference? {
        return redisTemplate.opsForValue().get("preferences:$userId") as? NotificationPreference
    }
    
    private fun getUserInfo(userId: String): Map<String, String> {
        return redisTemplate.opsForValue().get("user_info:$userId") as? Map<String, String>
            ?: mapOf("name" to "there", "firstName" to "")
    }
    
    private fun getUserTimezone(userId: String): String {
        return userTimeZones.getOrDefault(userId, defaultTimezone)
    }
    
    private fun getUserEngagementPattern(userId: String): UserEngagementPattern? {
        return userEngagementPatterns[userId]
    }
    
    private fun getQuietHoursConfig(userId: String): Map<String, String>? {
        val preferences = getUserPreferences(userId)
        return preferences?.quietHours?.let { quietHours ->
            mapOf(
                "startTime" to quietHours.startTime,
                "endTime" to quietHours.endTime,
                "allowEmergency" to quietHours.allowEmergency.toString()
            )
        }
    }
    
    private fun getNotificationTemplate(templateId: String): Map<String, Any>? {
        return redisTemplate.opsForValue().get("template:$templateId") as? Map<String, Any>
    }
    
    private fun isUserQuietHours(userId: String): Boolean {
        val quietHours = getQuietHoursConfig(userId)
        if (quietHours == null) return false
        
        val now = LocalTime.now(ZoneId.of(getUserTimezone(userId)))
        val startTime = LocalTime.parse(quietHours["startTime"] as String)
        val endTime = LocalTime.parse(quietHours["endTime"] as String)
        
        return isTimeInQuietHours(now, startTime, endTime)
    }
    
    private fun isTimeInQuietHours(time: LocalTime, startTime: LocalTime, endTime: LocalTime): Boolean {
        return if (startTime.isBefore(endTime)) {
            time.isAfter(startTime) && time.isBefore(endTime)
        } else {
            time.isAfter(startTime) || time.isBefore(endTime)
        }
    }
    
    private fun isRateLimited(userId: String, scheduleConfig: Map<String, Any>): Boolean {
        // Check if user has received too many notifications recently
        val now = System.currentTimeMillis()
        val recentNotifications = redisTemplate.opsForList().range(
            "user_notifications:$userId",
            0,
            -1
        ) ?: emptyList<Any>()
        
        val recentCount = recentNotifications.count { notification ->
            val timestamp = (notification as Map<String, Any>)["timestamp"] as? Long ?: 0L
            now - timestamp < (24 * 60 * 60 * 1000L) // Last 24 hours
        }
        
        return recentCount >= 10 // Max 10 notifications per day
    }
    
    private fun getTopicFromSchedule(scheduleConfig: Map<String, Any>): NotificationTopic? {
        val templateId = scheduleConfig["templateId"] as? String ?: return null
        
        return when (templateId) {
            "daily_reminder" -> NotificationTopic.DAILY_WELLNESS
            "sleep_reminder" -> NotificationTopic.SLEEP
            "anxiety_support" -> NotificationTopic.ANXIETY
            "stress_relief" -> NotificationTopic.STRESS
            else -> null
        }
    }
    
    private fun getScheduledTimeFromConfig(scheduleConfig: Map<String, Any>): LocalTime {
        val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
        return LocalTime.parse(timeOfDay)
    }
    
    private fun calculateNextRecurringTime(scheduleConfig: Map<String, Any>): Long {
        val interval = scheduleConfig["interval"] as? Int ?: 24 // Default to 24 hours
        return System.currentTimeMillis() + (interval * 60 * 60 * 1000L)
    }
    
    private fun calculateNextDailyTime(scheduleConfig: Map<String, Any>): Long {
        val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
        val scheduledTime = LocalTime.parse(timeOfDay)
        val now = LocalDateTime.now()
        var scheduledDateTime = now.with(scheduledTime)
        
        if (scheduledDateTime.isBefore(now)) {
            scheduledDateTime = scheduledDateTime.plusDays(1)
        }
        
        return scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    private fun calculateNextWeeklyTime(scheduleConfig: Map<String, Any>): Long {
        val dayOfWeek = scheduleConfig["dayOfWeek"] as? Int ?: 1 // Monday
        val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
        val scheduledTime = LocalTime.parse(timeOfDay)
        
        var scheduledDateTime = LocalDateTime.now()
            .with(java.time.DayOfWeek.of(dayOfWeek))
            .with(scheduledTime)
        
        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            scheduledDateTime = scheduledDateTime.plusWeeks(1)
        }
        
        return scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    private fun calculateNextMonthlyTime(scheduleConfig: Map<String, Any>): Long {
        val dayOfMonth = scheduleConfig["dayOfMonth"] as? Int ?: 1
        val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
        val scheduledTime = LocalTime.parse(timeOfDay)
        
        var scheduledDateTime = LocalDateTime.now()
            .withDayOfMonth(dayOfMonth)
            .with(scheduledTime)
        
        if (scheduledDateTime.isBefore(LocalDateTime.now())) {
            scheduledDateTime = scheduledDateTime.plusMonths(1)
        }
        
        return scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    private fun calculateOptimalSendTime(pattern: UserEngagementPattern): Long {
        // Find the time when user is most likely to engage
        val bestHour = pattern.mostEngagedHour
        val today = LocalDateTime.now()
        
        return today.withHour(bestHour).withMinute(0).withSecond(0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    private fun updateOptimalSendTime(userId: String, sendTime: Long) {
        userOptimalTimes[userId] = sendTime
        redisTemplate.opsForValue().set(
            "optimal_time:$userId",
            sendTime,
            java.time.Duration.ofDays(30)
        )
    }
    
    private fun updateUserEngagementPattern(userId: String, notification: Map<String, Any>) {
        val pattern = userEngagementPatterns.getOrPut(userId) {
            UserEngagementPattern(userId)
        }
        
        val hour = LocalDateTime.now().hour
        pattern.updateEngagement(hour)
    }
    
    // Mock data functions (would use real database in production)
    private fun getActiveUsers(): List<String> {
        return listOf("user1", "user2", "user3") // Mock data
    }
    
    private fun getInactiveUsers(days: Int): List<String> {
        return listOf("user4", "user5") // Mock data
    }
    
    private fun getUserStreakCount(userId: String): Int {
        return (1..30).random() // Mock data
    }
    
    private fun getDaysSinceLastVisit(userId: String): Int {
        return (1..14).random() // Mock data
    }
    
    private fun getMissedSessionCount(userId: String): Int {
        return (0..5).random() // Mock data
    }
    
    private fun getWeeklyStats(userId: String): Map<String, Any> {
        return mapOf(
            "sessionsCompleted" to (1..7).random(),
            "totalMinutes" to (15..180).random(),
            "favoriteCategory" to listOf("sleep", "anxiety", "stress", "mindfulness").random()
        )
    }
}

// Data class for user engagement patterns
data class UserEngagementPattern(
    val userId: String,
    var mostEngagedHour: Int = 19, // Default to 7 PM
    var preferredSessionType: String = "mindfulness",
    var averageEngagementTime: Long = 5 * 60 * 1000L, // 5 minutes
    val engagementByHour: MutableMap<Int, Int> = mutableMapOf()
) {
    fun updateEngagement(hour: Int) {
        val currentCount = engagementByHour[hour] ?: 0
        engagementByHour[hour] = currentCount + 1
        
        // Update most engaged hour
        val maxEngagementHour = engagementByHour.maxByOrNull { it.value }?.key ?: 19
        if (engagementByHour[maxEngagementHour] ?: 0 > (engagementByHour[mostEngagedHour] ?: 0)) {
            mostEngagedHour = maxEngagementHour
        }
    }
}
