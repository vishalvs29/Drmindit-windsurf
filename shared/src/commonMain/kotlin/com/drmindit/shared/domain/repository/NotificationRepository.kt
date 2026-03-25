package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.*
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    // Notification management
    suspend fun sendNotification(notification: Notification): Result<Notification>
    suspend fun scheduleNotification(notification: Notification): Result<Notification>
    suspend fun sendBatchNotifications(notifications: List<Notification>): Result<NotificationBatch>
    suspend fun cancelNotification(notificationId: String): Result<Unit>
    suspend fun updateNotificationStatus(notificationId: String, status: NotificationStatus): Result<Unit>
    
    // User preferences
    suspend fun getNotificationPreferences(userId: String): Result<NotificationPreference?>
    suspend fun updateNotificationPreferences(preferences: NotificationPreference): Result<NotificationPreference>
    suspend fun enableChannel(userId: String, channel: NotificationChannel): Result<Unit>
    suspend fun disableChannel(userId: String, channel: NotificationChannel): Result<Unit>
    suspend fun enableTopic(userId: String, topic: NotificationTopic): Result<Unit>
    suspend fun disableTopic(userId: String, topic: NotificationTopic): Result<Unit>
    
    // User channels
    suspend fun registerUserChannel(userChannel: UserChannel): Result<UserChannel>
    suspend fun updateUserChannel(userChannel: UserChannel): Result<UserChannel>
    suspend fun getUserChannels(userId: String): Result<List<UserChannel>>
    suspend fun deleteUserChannel(userId: String, channel: NotificationChannel): Result<Unit>
    suspend fun verifyChannel(userId: String, channel: NotificationChannel): Result<Boolean>
    
    // Templates and scheduling
    suspend fun getNotificationTemplate(templateId: String): Result<NotificationTemplate?>
    suspend fun getAllNotificationTemplates(): Result<List<NotificationTemplate>>
    suspend fun createNotificationTemplate(template: NotificationTemplate): Result<NotificationTemplate>
    suspend fun scheduleNotificationFromTemplate(
        userId: String,
        templateId: String,
        variables: Map<String, String>,
        scheduledAt: Long? = null
    ): Result<Notification>
    
    // Scheduling
    suspend fun createNotificationSchedule(schedule: NotificationSchedule): Result<NotificationSchedule>
    suspend fun updateNotificationSchedule(schedule: NotificationSchedule): Result<NotificationSchedule>
    suspend fun getUserSchedules(userId: String): Result<List<NotificationSchedule>>
    suspend fun deleteNotificationSchedule(scheduleId: String): Result<Unit>
    suspend fun processScheduledNotifications(): Result<List<Notification>>
    
    // History and analytics
    suspend fun getNotificationHistory(userId: String, limit: Int = 50): Result<List<Notification>>
    suspend fun getNotificationLogs(userId: String, limit: Int = 50): Result<List<NotificationLog>>
    suspend fun getNotificationAnalytics(userId: String, startDate: Long, endDate: Long): Result<List<NotificationAnalytics>>
    suspend fun trackNotificationEvent(
        notificationId: String,
        event: String,
        metadata: Map<String, String> = emptyMap()
    ): Result<Unit>
    
    // Bulk operations
    suspend fun sendToTopic(
        topic: NotificationTopic,
        notification: Notification,
        excludeUsers: Set<String> = emptySet()
    ): Result<Int> // Returns count of successful sends
    
    suspend fun sendToAllUsers(
        notification: Notification,
        excludeUsers: Set<String> = emptySet()
    ): Result<Int>
    
    // Real-time updates
    fun observeNotifications(userId: String): Flow<List<Notification>>
    fun observeNotificationPreferences(userId: String): Flow<NotificationPreference?>
    fun observeNotificationLogs(userId: String): Flow<List<NotificationLog>>
    
    // Channel-specific operations
    suspend fun sendPushNotification(notification: Notification, fcmToken: String): Result<Unit>
    suspend fun sendWhatsAppMessage(notification: Notification, phoneNumber: String): Result<Unit>
    suspend fun sendTelegramMessage(notification: Notification, chatId: String): Result<Unit>
    suspend fun sendEmail(notification: Notification, emailAddress: String): Result<Unit>
    
    // Personalization
    suspend fun getPersonalizedNotifications(userId: String, limit: Int = 10): Result<List<Notification>>
    suspend fun updatePersonalizationScore(userId: String, notificationId: String, score: Float): Result<Unit>
    suspend fun getOptimalSendTime(userId: String): Result<Long>
    
    // Quiet hours and rate limiting
    suspend fun isQuietHours(userId: String): Result<Boolean>
    suspend fun canSendNotification(userId: String, channel: NotificationChannel): Result<Boolean>
    suspend fun updateLastSentTime(userId: String, channel: NotificationChannel): Result<Unit>
    
    // A/B testing
    suspend fun createNotificationTest(test: NotificationTest): Result<NotificationTest>
    suspend fun getNotificationTestResults(testId: String): Result<NotificationTest?>
    suspend fun assignUserToTestGroup(userId: String, testId: String): Result<String>
}

class NotificationRepositoryImpl(
    private val notificationService: NotificationService,
    private val pushService: PushNotificationService,
    private val whatsappService: WhatsAppService,
    private val telegramService: TelegramService,
    private val emailService: EmailService,
    private val schedulingService: NotificationSchedulingService,
    private val analyticsService: NotificationAnalyticsService,
    private val localDataSource: NotificationLocalDataSource
) : NotificationRepository {
    
    override suspend fun sendNotification(notification: Notification): Result<Notification> {
        return try {
            // Check if user can receive notification
            val canSend = canSendNotification(notification.userId, notification.channels.firstOrNull() ?: NotificationChannel.IN_APP)
            if (!canSend.getOrDefault(false)) {
                return Result.failure(Exception("User cannot receive notifications at this time"))
            }
            
            // Update status to sending
            val updatedNotification = notification.copy(status = NotificationStatus.SENDING)
            updateNotificationStatus(notification.id, NotificationStatus.SENDING)
            
            // Send through appropriate channels
            val results = notification.channels.map { channel ->
                when (channel) {
                    NotificationChannel.IN_APP -> sendInAppNotification(updatedNotification)
                    NotificationChannel.PUSH_NOTIFICATION -> sendPushNotificationToUser(updatedNotification)
                    NotificationChannel.WHATSAPP -> sendWhatsAppToUser(updatedNotification)
                    NotificationChannel.TELEGRAM -> sendTelegramToUser(updatedNotification)
                    NotificationChannel.EMAIL -> sendEmailToUser(updatedNotification)
                }
            }
            
            // Check if any channel succeeded
            if (results.any { it.isSuccess }) {
                val finalNotification = updatedNotification.copy(
                    status = NotificationStatus.SENT,
                    sentAt = System.currentTimeMillis()
                )
                updateNotificationStatus(notification.id, NotificationStatus.SENT)
                Result.success(finalNotification)
            } else {
                updateNotificationStatus(notification.id, NotificationStatus.FAILED)
                Result.failure(Exception("All channels failed"))
            }
        } catch (e: Exception) {
            updateNotificationStatus(notification.id, NotificationStatus.FAILED)
            Result.failure(e)
        }
    }
    
    override suspend fun scheduleNotification(notification: Notification): Result<Notification> {
        return try {
            val scheduledNotification = notification.copy(
                status = NotificationStatus.SCHEDULED,
                scheduledAt = notification.scheduledAt ?: System.currentTimeMillis()
            )
            
            // Schedule in the backend
            schedulingService.scheduleNotification(scheduledNotification)
            
            // Save to local storage
            localDataSource.saveNotification(scheduledNotification)
            
            Result.success(scheduledNotification)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendBatchNotifications(notifications: List<Notification>): Result<NotificationBatch> {
        return try {
            val batchId = "batch_${System.currentTimeMillis()}"
            val batch = NotificationBatch(
                id = batchId,
                notifications = notifications,
                scheduledAt = System.currentTimeMillis(),
                status = BatchStatus.PROCESSING
            )
            
            var successCount = 0
            var failedCount = 0
            
            notifications.forEach { notification ->
                val result = sendNotification(notification)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failedCount++
                }
            }
            
            val finalBatch = batch.copy(
                status = if (failedCount == 0) BatchStatus.COMPLETED else BatchStatus.PARTIALLY_FAILED,
                processedAt = System.currentTimeMillis(),
                successCount = successCount,
                failedCount = failedCount
            )
            
            Result.success(finalBatch)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelNotification(notificationId: String): Result<Unit> {
        return try {
            schedulingService.cancelNotification(notificationId)
            updateNotificationStatus(notificationId, NotificationStatus.CANCELLED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotificationStatus(notificationId: String, status: NotificationStatus): Result<Unit> {
        return try {
            localDataSource.updateNotificationStatus(notificationId, status)
            analyticsService.trackNotificationEvent(notificationId, "status_changed", mapOf("status" to status.name))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationPreferences(userId: String): Result<NotificationPreference?> {
        return try {
            val preferences = localDataSource.getNotificationPreferences(userId)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotificationPreferences(preferences: NotificationPreference): Result<NotificationPreference> {
        return try {
            localDataSource.saveNotificationPreferences(preferences)
            analyticsService.trackNotificationEvent("preferences_updated", mapOf("userId" to preferences.userId))
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enableChannel(userId: String, channel: NotificationChannel): Result<Unit> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
                ?: NotificationPreference(userId, emptyMap(), emptyMap())
            
            val updatedChannels = preferences.channels.toMutableMap()
            updatedChannels[channel] = ChannelPreference(enabled = true)
            
            val updatedPreferences = preferences.copy(
                channels = updatedChannels,
                updatedAt = System.currentTimeMillis()
            )
            
            updateNotificationPreferences(updatedPreferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun disableChannel(userId: String, channel: NotificationChannel): Result<Unit> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
                ?: NotificationPreference(userId, emptyMap(), emptyMap())
            
            val updatedChannels = preferences.channels.toMutableMap()
            updatedChannels[channel] = ChannelPreference(enabled = false)
            
            val updatedPreferences = preferences.copy(
                channels = updatedChannels,
                updatedAt = System.currentTimeMillis()
            )
            
            updateNotificationPreferences(updatedPreferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun enableTopic(userId: String, topic: NotificationTopic): Result<Unit> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
                ?: NotificationPreference(userId, emptyMap(), emptyMap())
            
            val updatedTopics = preferences.topics.toMutableMap()
            updatedTopics[topic] = TopicPreference(enabled = true)
            
            val updatedPreferences = preferences.copy(
                topics = updatedTopics,
                updatedAt = System.currentTimeMillis()
            )
            
            updateNotificationPreferences(updatedPreferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun disableTopic(userId: String, topic: NotificationTopic): Result<Unit> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
                ?: NotificationPreference(userId, emptyMap(), emptyMap())
            
            val updatedTopics = preferences.topics.toMutableMap()
            updatedTopics[topic] = TopicPreference(enabled = false)
            
            val updatedPreferences = preferences.copy(
                topics = updatedTopics,
                updatedAt = System.currentTimeMillis()
            )
            
            updateNotificationPreferences(updatedPreferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun registerUserChannel(userChannel: UserChannel): Result<UserChannel> {
        return try {
            localDataSource.saveUserChannel(userChannel)
            
            // Verify channel if needed
            if (!userChannel.verified) {
                when (userChannel.channel) {
                    NotificationChannel.EMAIL -> emailService.verifyEmail(userChannel.identifier)
                    NotificationChannel.WHATSAPP -> whatsappService.verifyPhoneNumber(userChannel.identifier)
                    NotificationChannel.TELEGRAM -> telegramService.verifyChatId(userChannel.identifier)
                    else -> Result.success(true)
                }
            }
            
            Result.success(userChannel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserChannel(userChannel: UserChannel): Result<UserChannel> {
        return try {
            localDataSource.updateUserChannel(userChannel)
            Result.success(userChannel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserChannels(userId: String): Result<List<UserChannel>> {
        return try {
            val channels = localDataSource.getUserChannels(userId)
            Result.success(channels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteUserChannel(userId: String, channel: NotificationChannel): Result<Unit> {
        return try {
            localDataSource.deleteUserChannel(userId, channel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun verifyChannel(userId: String, channel: NotificationChannel): Result<Boolean> {
        return try {
            val userChannels = getUserChannels(userId).getOrNull() ?: emptyList()
            val userChannel = userChannels.find { it.channel == channel }
            
            if (userChannel != null) {
                when (channel) {
                    NotificationChannel.EMAIL -> emailService.verifyEmail(userChannel.identifier)
                    NotificationChannel.WHATSAPP -> whatsappService.verifyPhoneNumber(userChannel.identifier)
                    NotificationChannel.TELEGRAM -> telegramService.verifyChatId(userChannel.identifier)
                    else -> Result.success(true)
                }
            } else {
                Result.failure(Exception("Channel not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationTemplate(templateId: String): Result<NotificationTemplate?> {
        return try {
            val template = localDataSource.getNotificationTemplate(templateId)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllNotificationTemplates(): Result<List<NotificationTemplate>> {
        return try {
            val templates = localDataSource.getAllNotificationTemplates()
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createNotificationTemplate(template: NotificationTemplate): Result<NotificationTemplate> {
        return try {
            localDataSource.saveNotificationTemplate(template)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun scheduleNotificationFromTemplate(
        userId: String,
        templateId: String,
        variables: Map<String, String>,
        scheduledAt: Long?
    ): Result<Notification> {
        return try {
            val template = getNotificationTemplate(templateId).getOrNull()
                ?: return Result.failure(Exception("Template not found"))
            
            val title = replaceVariables(template.titleTemplate, variables)
            val body = replaceVariables(template.bodyTemplate, variables)
            
            val notification = Notification(
                id = "notif_${System.currentTimeMillis()}_${(1000..9999).random()}",
                userId = userId,
                type = template.type,
                title = title,
                body = body,
                data = variables,
                channels = template.channels,
                scheduledAt = scheduledAt,
                metadata = NotificationMetadata(
                    sessionId = variables["session_id"],
                    programId = variables["program_id"],
                    timezone = variables["timezone"],
                    deepLink = variables["deep_link"]
                )
            )
            
            if (scheduledAt != null) {
                scheduleNotification(notification)
            } else {
                sendNotification(notification)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createNotificationSchedule(schedule: NotificationSchedule): Result<NotificationSchedule> {
        return try {
            localDataSource.saveNotificationSchedule(schedule)
            schedulingService.scheduleNotification(schedule)
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateNotificationSchedule(schedule: NotificationSchedule): Result<NotificationSchedule> {
        return try {
            localDataSource.updateNotificationSchedule(schedule)
            schedulingService.updateNotificationSchedule(schedule)
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserSchedules(userId: String): Result<List<NotificationSchedule>> {
        return try {
            val schedules = localDataSource.getUserSchedules(userId)
            Result.success(schedules)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotificationSchedule(scheduleId: String): Result<Unit> {
        return try {
            localDataSource.deleteNotificationSchedule(scheduleId)
            schedulingService.cancelNotification(scheduleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun processScheduledNotifications(): Result<List<Notification>> {
        return try {
            val notifications = schedulingService.getDueNotifications()
            val sentNotifications = mutableListOf<Notification>()
            
            notifications.forEach { notification ->
                val result = sendNotification(notification)
                if (result.isSuccess) {
                    sentNotifications.add(result.getOrNull()!!)
                }
            }
            
            Result.success(sentNotifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationHistory(userId: String, limit: Int): Result<List<Notification>> {
        return try {
            val notifications = localDataSource.getNotificationHistory(userId, limit)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationLogs(userId: String, limit: Int): Result<List<NotificationLog>> {
        return try {
            val logs = localDataSource.getNotificationLogs(userId, limit)
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationAnalytics(userId: String, startDate: Long, endDate: Long): Result<List<NotificationAnalytics>> {
        return try {
            val analytics = analyticsService.getNotificationAnalytics(userId, startDate, endDate)
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun trackNotificationEvent(
        notificationId: String,
        event: String,
        metadata: Map<String, String>
    ): Result<Unit> {
        return try {
            analyticsService.trackNotificationEvent(notificationId, event, metadata)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendToTopic(
        topic: NotificationTopic,
        notification: Notification,
        excludeUsers: Set<String>
    ): Result<Int> {
        return try {
            val users = localDataSource.getUsersWithTopicEnabled(topic, excludeUsers)
            val notifications = users.map { userId ->
                notification.copy(userId = userId)
            }
            
            val batchResult = sendBatchNotifications(notifications)
            Result.success(batchResult.getOrNull()?.successCount ?: 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sendToAllUsers(
        notification: Notification,
        excludeUsers: Set<String>
    ): Result<Int> {
        return try {
            val users = localDataSource.getAllActiveUsers(excludeUsers)
            val notifications = users.map { userId ->
                notification.copy(userId = userId)
            }
            
            val batchResult = sendBatchNotifications(notifications)
            Result.success(batchResult.getOrNull()?.successCount ?: 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeNotifications(userId: String): Flow<List<Notification>> {
        return localDataSource.observeNotifications(userId)
    }
    
    override fun observeNotificationPreferences(userId: String): Flow<NotificationPreference?> {
        return localDataSource.observeNotificationPreferences(userId)
    }
    
    override fun observeNotificationLogs(userId: String): Flow<List<NotificationLog>> {
        return localDataSource.observeNotificationLogs(userId)
    }
    
    // Channel-specific implementations
    private suspend fun sendInAppNotification(notification: Notification): Result<Unit> {
        return notificationService.sendInAppNotification(notification)
    }
    
    private suspend fun sendPushNotificationToUser(notification: Notification): Result<Unit> {
        val userChannels = getUserChannels(notification.userId).getOrNull() ?: emptyList()
        val fcmToken = userChannels
            .find { it.channel == NotificationChannel.PUSH_NOTIFICATION }
            ?.identifier
        
        return if (fcmToken != null) {
            sendPushNotification(notification, fcmToken)
        } else {
            Result.failure(Exception("FCM token not found"))
        }
    }
    
    private suspend fun sendWhatsAppToUser(notification: Notification): Result<Unit> {
        val userChannels = getUserChannels(notification.userId).getOrNull() ?: emptyList()
        val phoneNumber = userChannels
            .find { it.channel == NotificationChannel.WHATSAPP }
            ?.identifier
        
        return if (phoneNumber != null) {
            sendWhatsAppMessage(notification, phoneNumber)
        } else {
            Result.failure(Exception("WhatsApp number not found"))
        }
    }
    
    private suspend fun sendTelegramToUser(notification: Notification): Result<Unit> {
        val userChannels = getUserChannels(notification.userId).getOrNull() ?: emptyList()
        val chatId = userChannels
            .find { it.channel == NotificationChannel.TELEGRAM }
            ?.identifier
        
        return if (chatId != null) {
            sendTelegramMessage(notification, chatId)
        } else {
            Result.failure(Exception("Telegram chat ID not found"))
        }
    }
    
    private suspend fun sendEmailToUser(notification: Notification): Result<Unit> {
        val userChannels = getUserChannels(notification.userId).getOrNull() ?: emptyList()
        val email = userChannels
            .find { it.channel == NotificationChannel.EMAIL }
            ?.identifier
        
        return if (email != null) {
            sendEmail(notification, email)
        } else {
            Result.failure(Exception("Email not found"))
        }
    }
    
    // Interface methods that will be implemented by services
    override suspend fun sendPushNotification(notification: Notification, fcmToken: String): Result<Unit> {
        return pushService.sendNotification(notification, fcmToken)
    }
    
    override suspend fun sendWhatsAppMessage(notification: Notification, phoneNumber: String): Result<Unit> {
        return whatsappService.sendMessage(notification, phoneNumber)
    }
    
    override suspend fun sendTelegramMessage(notification: Notification, chatId: String): Result<Unit> {
        return telegramService.sendMessage(notification, chatId)
    }
    
    override suspend fun sendEmail(notification: Notification, emailAddress: String): Result<Unit> {
        return emailService.sendEmail(notification, emailAddress)
    }
    
    // Personalization methods
    override suspend fun getPersonalizedNotifications(userId: String, limit: Int): Result<List<Notification>> {
        return try {
            val personalized = notificationService.getPersonalizedNotifications(userId, limit)
            Result.success(personalized)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePersonalizationScore(userId: String, notificationId: String, score: Float): Result<Unit> {
        return try {
            analyticsService.updatePersonalizationScore(userId, notificationId, score)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOptimalSendTime(userId: String): Result<Long> {
        return try {
            val optimalTime = analyticsService.getOptimalSendTime(userId)
            Result.success(optimalTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Quiet hours and rate limiting
    override suspend fun isQuietHours(userId: String): Result<Boolean> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
            val isQuiet = preferences?.quietHours?.isQuietNow() ?: false
            Result.success(isQuiet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun canSendNotification(userId: String, channel: NotificationChannel): Result<Boolean> {
        return try {
            val preferences = getNotificationPreferences(userId).getOrNull()
                ?: return Result.success(false)
            
            val isChannelEnabled = preferences.isChannelEnabled(channel)
            val isQuietHours = preferences.quietHours?.isQuietNow() ?: false
            val canSendDuringQuiet = preferences.quietHours?.allowEmergency ?: false
            
            val canSend = isChannelEnabled && (!isQuietHours || canSend)
            
            // Check rate limiting
            if (canSend) {
                val lastSent = localDataSource.getLastSentTime(userId, channel)
                val minTimeBetween = preferences.channels[channel]?.minTimeBetween ?: 30 * 60 * 1000L
                val timeSinceLastSend = System.currentTimeMillis() - (lastSent ?: 0)
                val respectsRateLimit = timeSinceLastSend >= minTimeBetween
                
                if (respectsRateLimit) {
                    updateLastSentTime(userId, channel)
                }
                
                Result.success(respectsRateLimit)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateLastSentTime(userId: String, channel: NotificationChannel): Result<Unit> {
        return try {
            localDataSource.updateLastSentTime(userId, channel)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // A/B testing
    override suspend fun createNotificationTest(test: NotificationTest): Result<NotificationTest> {
        return try {
            localDataSource.saveNotificationTest(test)
            Result.success(test)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationTestResults(testId: String): Result<NotificationTest?> {
        return try {
            val test = localDataSource.getNotificationTest(testId)
            Result.success(test)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun assignUserToTestGroup(userId: String, testId: String): Result<String> {
        return try {
            val group = analyticsService.assignUserToTestGroup(userId, testId)
            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper functions
    private fun replaceVariables(template: String, variables: Map<String, String>): String {
        var result = template
        variables.forEach { (key, value) ->
            result = result.replace("{{$key}}", value)
        }
        return result
    }
}

// Local data source interface
interface NotificationLocalDataSource {
    suspend fun saveNotification(notification: Notification)
    suspend fun updateNotification(notification: Notification)
    suspend fun updateNotificationStatus(notificationId: String, status: NotificationStatus)
    suspend fun getNotificationHistory(userId: String, limit: Int): List<Notification>
    suspend fun getNotificationLogs(userId: String, limit: Int): List<NotificationLog>
    suspend fun saveNotificationLog(log: NotificationLog)
    suspend fun saveNotificationPreferences(preferences: NotificationPreference)
    suspend fun getNotificationPreferences(userId: String): NotificationPreference?
    suspend fun saveUserChannel(userChannel: UserChannel)
    suspend fun updateUserChannel(userChannel: UserChannel)
    suspend fun getUserChannels(userId: String): List<UserChannel>
    suspend fun deleteUserChannel(userId: String, channel: NotificationChannel)
    suspend fun saveNotificationTemplate(template: NotificationTemplate)
    suspend fun getNotificationTemplate(templateId: String): NotificationTemplate?
    suspend fun getAllNotificationTemplates(): List<NotificationTemplate>
    suspend fun saveNotificationSchedule(schedule: NotificationSchedule)
    suspend fun updateNotificationSchedule(schedule: NotificationSchedule)
    suspend fun getUserSchedules(userId: String): List<NotificationSchedule>
    suspend fun deleteNotificationSchedule(scheduleId: String)
    suspend fun saveNotificationTest(test: NotificationTest)
    suspend fun getNotificationTest(testId: String): NotificationTest?
    suspend fun getUsersWithTopicEnabled(topic: NotificationTopic, excludeUsers: Set<String>): List<String>
    suspend fun getAllActiveUsers(excludeUsers: Set<String>): List<String>
    suspend fun getLastSentTime(userId: String, channel: NotificationChannel): Long?
    suspend fun updateLastSentTime(userId: String, channel: NotificationChannel)
    
    fun observeNotifications(userId: String): Flow<List<Notification>>
    fun observeNotificationPreferences(userId: String): Flow<NotificationPreference?>
    fun observeNotificationLogs(userId: String): Flow<List<NotificationLog>>
}

// A/B testing model
@kotlinx.serialization.Serializable
data class NotificationTest(
    val id: String,
    val name: String,
    val description: String,
    val variants: List<NotificationVariant>,
    val trafficSplit: Map<String, Float>, // variant_id -> percentage
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true,
    val targetCriteria: Map<String, String> = emptyMap(),
    val results: NotificationTestResults? = null
)

@kotlinx.serialization.Serializable
data class NotificationVariant(
    val id: String,
    val name: String,
    val notification: Notification,
    val metadata: Map<String, String> = emptyMap()
)

@kotlinx.serialization.Serializable
data class NotificationTestResults(
    val sentCount: Int,
    val deliveredCount: Int,
    val openedCount: Int,
    val clickedCount: Int,
    val conversionRate: Float,
    val variantResults: Map<String, VariantResults>
)

@kotlinx.serialization.Serializable
data class VariantResults(
    val sentCount: Int,
    val deliveredCount: Int,
    val openedCount: Int,
    val clickedCount: Int,
    val conversionRate: Float
)
