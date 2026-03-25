package com.drmindit.notifications

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.config.EnableRabbit
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@SpringBootApplication
@EnableScheduling
@EnableRabbit
class NotificationServiceApplication

fun main(args: Array<String>) {
    runApplication<NotificationServiceApplication>(*args)
}

// Configuration
@Configuration
class NotificationConfig {
    
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().registerKotlinModule()
    }
    
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.defaultSerializer = GenericJackson2JsonRedisSerializer()
        return template
    }
    
    @Bean
    fun notificationQueue(): Queue {
        return Queue("notifications", true)
    }
    
    @Bean
    fun messageConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }
    
    @Bean
    fun scheduledExecutorService(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(10)
    }
}

// Data Transfer Objects
data class NotificationRequest(
    val userId: String,
    val type: String,
    val title: String,
    val body: String,
    val channels: Set<String>,
    val data: Map<String, String> = emptyMap(),
    val scheduledAt: Long? = null,
    val priority: String = "NORMAL"
)

data class BatchNotificationRequest(
    val userIds: List<String>,
    val notification: NotificationRequest
)

data class ScheduleRequest(
    val userId: String,
    val templateId: String,
    val variables: Map<String, String>,
    val schedule: Map<String, Any>
)

data class PreferencesUpdateRequest(
    val userId: String,
    val channels: Map<String, Map<String, Any>>,
    val topics: Map<String, Map<String, Any>>,
    val quietHours: Map<String, Any>? = null
)

data class ChannelRegistrationRequest(
    val userId: String,
    val channel: String,
    val identifier: String,
    val preferences: Map<String, String> = emptyMap()
)

// Main Service
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val queueService: QueueService,
    private val schedulingService: SchedulingService,
    private val analyticsService: AnalyticsService
) {
    
    @PostMapping("/send")
    fun sendNotification(@RequestBody request: NotificationRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val notification = notificationService.createNotification(request)
            val result = queueService.enqueueNotification(notification)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "notificationId" to notification.id,
                "queued" to result
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/batch")
    fun sendBatchNotifications(@RequestBody request: BatchNotificationRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val notifications = request.userIds.map { userId ->
                notificationService.createNotification(request.notification.copy(userId = userId))
            }
            
            val batchId = queueService.enqueueBatch(notifications)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "batchId" to batchId,
                "count" to notifications.size
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/schedule")
    fun scheduleNotification(@RequestBody request: ScheduleRequest): ResponseEntity<Map<String, Any>> {
        return try {
            val schedule = schedulingService.createSchedule(request)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "scheduleId" to schedule.id,
                "nextRun" to schedule.nextRun
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/topic/{topic}")
    fun sendToTopic(
        @PathVariable topic: String,
        @RequestBody request: NotificationRequest,
        @RequestParam excludeUsers: List<String> = emptyList()
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val result = notificationService.sendToTopic(topic, request, excludeUsers.toSet())
            ResponseEntity.ok(mapOf(
                "success" to true,
                "sentCount" to result
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/user/{userId}")
    fun getUserNotifications(
        @PathVariable userId: String,
        @RequestParam limit: Int = 50
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val notifications = notificationService.getUserNotifications(userId, limit)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "notifications" to notifications,
                "count" to notifications.size
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/user/{userId}/preferences")
    fun getUserPreferences(@PathVariable userId: String): ResponseEntity<Map<String, Any>> {
        return try {
            val preferences = notificationService.getUserPreferences(userId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "preferences" to preferences
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PutMapping("/user/{userId}/preferences")
    fun updateUserPreferences(
        @PathVariable userId: String,
        @RequestBody request: PreferencesUpdateRequest
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val preferences = notificationService.updateUserPreferences(userId, request)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "preferences" to preferences
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/user/{userId}/channel")
    fun registerChannel(
        @PathVariable userId: String,
        @RequestBody request: ChannelRegistrationRequest
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val channel = notificationService.registerUserChannel(userId, request)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "channel" to channel
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/user/{userId}/channels")
    fun getUserChannels(@PathVariable userId: String): ResponseEntity<Map<String, Any>> {
        return try {
            val channels = notificationService.getUserChannels(userId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "channels" to channels
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @DeleteMapping("/user/{userId}/channel/{channel}")
    fun deleteChannel(
        @PathVariable userId: String,
        @PathVariable channel: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            notificationService.deleteUserChannel(userId, channel)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Channel deleted successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/templates")
    fun getTemplates(): ResponseEntity<Map<String, Any>> {
        return try {
            val templates = notificationService.getAllTemplates()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "templates" to templates
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/template")
    fun createTemplate(@RequestBody template: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        return try {
            val created = notificationService.createTemplate(template)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "template" to created
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/track")
    fun trackEvent(@RequestBody event: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        return try {
            analyticsService.trackEvent(event)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Event tracked successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/analytics/{userId}")
    fun getAnalytics(
        @PathVariable userId: String,
        @RequestParam startDate: Long,
        @RequestParam endDate: Long
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val analytics = analyticsService.getUserAnalytics(userId, startDate, endDate)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "analytics" to analytics
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @PostMapping("/cancel/{notificationId}")
    fun cancelNotification(@PathVariable notificationId: String): ResponseEntity<Map<String, Any>> {
        return try {
            notificationService.cancelNotification(notificationId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Notification cancelled successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
                "success" to false,
                "error" to e.message
            ))
        }
    }
    
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "healthy",
            "timestamp" to System.currentTimeMillis(),
            "service" to "notification-service"
        ))
    }
}

// Core Service
@Service
class NotificationService(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val rabbitTemplate: RabbitTemplate,
    private val pushService: PushNotificationService,
    private val whatsappService: WhatsAppService,
    private val telegramService: TelegramService,
    private val emailService: EmailService,
    private val userRepository: UserRepository
) {
    
    fun createNotification(request: NotificationRequest): Map<String, Any> {
        val notification = mapOf(
            "id" to "notif_${System.currentTimeMillis()}_${(1000..9999).random()}",
            "userId" to request.userId,
            "type" to request.type,
            "title" to request.title,
            "body" to request.body,
            "channels" to request.channels,
            "data" to request.data,
            "priority" to request.priority,
            "scheduledAt" to request.scheduledAt,
            "createdAt" to System.currentTimeMillis(),
            "status" to "PENDING",
            "retryCount" to 0
        )
        
        // Store in Redis for persistence
        redisTemplate.opsForValue().set(
            "notification:${notification["id"]}",
            notification,
            java.time.Duration.ofDays(7)
        )
        
        return notification
    }
    
    fun sendToTopic(topic: String, request: NotificationRequest, excludeUsers: Set<String>): Int {
        val users = userRepository.getUsersWithTopicEnabled(topic, excludeUsers)
        val notifications = users.map { userId ->
            createNotification(request.copy(userId = userId))
        }
        
        return notifications.size
    }
    
    fun getUserNotifications(userId: String, limit: Int): List<Map<String, Any>> {
        val keys = redisTemplate.keys("notification:*") ?: emptyList()
        val notifications = mutableListOf<Map<String, Any>>()
        
        keys.take(limit).forEach { key ->
            val notification = redisTemplate.opsForValue().get(key) as? Map<String, Any>
            if (notification != null && notification["userId"] == userId) {
                notifications.add(notification)
            }
        }
        
        return notifications.sortedByDescending { it["createdAt"] as Long }
    }
    
    fun getUserPreferences(userId: String): Map<String, Any>? {
        return redisTemplate.opsForValue().get("preferences:$userId") as? Map<String, Any>
    }
    
    fun updateUserPreferences(userId: String, request: PreferencesUpdateRequest): Map<String, Any> {
        val preferences = mapOf(
            "userId" to userId,
            "channels" to request.channels,
            "topics" to request.topics,
            "quietHours" to request.quietHours,
            "updatedAt" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForValue().set(
            "preferences:$userId",
            preferences,
            java.time.Duration.ofDays(365)
        )
        
        return preferences
    }
    
    fun registerUserChannel(userId: String, request: ChannelRegistrationRequest): Map<String, Any> {
        val channel = mapOf(
            "id" to "channel_${System.currentTimeMillis()}",
            "userId" to userId,
            "channel" to request.channel,
            "identifier" to request.identifier,
            "isActive" to true,
            "verified" to false,
            "preferences" to request.preferences,
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForValue().set(
            "channel:${userId}:${request.channel}",
            channel,
            java.time.Duration.ofDays(365)
        )
        
        // Trigger verification if needed
        when (request.channel.uppercase()) {
            "EMAIL" -> emailService.sendVerificationEmail(request.identifier)
            "WHATSAPP" -> whatsappService.sendVerificationMessage(request.identifier)
            "TELEGRAM" -> telegramService.sendVerificationMessage(request.identifier)
        }
        
        return channel
    }
    
    fun getUserChannels(userId: String): List<Map<String, Any>> {
        val keys = redisTemplate.keys("channel:$userId:*") ?: emptyList()
        val channels = mutableListOf<Map<String, Any>>()
        
        keys.forEach { key ->
            val channel = redisTemplate.opsForValue().get(key) as? Map<String, Any>
            if (channel != null) {
                channels.add(channel)
            }
        }
        
        return channels
    }
    
    fun deleteUserChannel(userId: String, channel: String) {
        redisTemplate.delete("channel:$userId:$channel")
    }
    
    fun getAllTemplates(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "id" to "daily_reminder",
                "name" to "Daily Wellness Reminder",
                "type" to "DAILY_REMINDER",
                "channels" to listOf("IN_APP", "PUSH_NOTIFICATION", "WHATSAPP", "TELEGRAM", "EMAIL"),
                "titleTemplate" to "Hi {{name}}, time to relax! 🌿",
                "bodyTemplate" to "Take 5 minutes for your mental wellness today.",
                "variables" to listOf("name"),
                "isActive" to true
            ),
            mapOf(
                "id" to "session_reminder",
                "name" to "Session Reminder",
                "type" to "SESSION_REMINDER",
                "channels" to listOf("IN_APP", "PUSH_NOTIFICATION", "WHATSAPP", "TELEGRAM"),
                "titleTemplate" to "{{session_title}} is waiting for you",
                "bodyTemplate" to "Your {{duration}} minute session with {{instructor}} is ready.",
                "variables" to listOf("session_title", "duration", "instructor"),
                "isActive" to true
            ),
            mapOf(
                "id" to "streak_reminder",
                "name" to "Streak Reminder",
                "type" to "STREAK_REMINDER",
                "channels" to listOf("IN_APP", "PUSH_NOTIFICATION", "EMAIL"),
                "titleTemplate" to "{{streak_count}} day streak! 🔥",
                "bodyTemplate" to "You're on fire! Keep up the amazing work.",
                "variables" to listOf("streak_count"),
                "isActive" to true
            )
        )
    }
    
    fun createTemplate(template: Map<String, Any>): Map<String, Any> {
        val templateWithId = template + ("id" to "template_${System.currentTimeMillis()}")
        
        redisTemplate.opsForValue().set(
            "template:${templateWithId["id"]}",
            templateWithId,
            java.time.Duration.ofDays(365)
        )
        
        return templateWithId
    }
    
    fun cancelNotification(notificationId: String) {
        val notification = redisTemplate.opsForValue().get("notification:$notificationId") as? Map<String, Any>
        if (notification != null) {
            val updatedNotification = notification + ("status" to "CANCELLED")
            redisTemplate.opsForValue().set(
                "notification:$notificationId",
                updatedNotification,
                java.time.Duration.ofDays(7)
            )
        }
    }
}

// Queue Service
@Service
class QueueService(
    private val rabbitTemplate: RabbitTemplate,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    fun enqueueNotification(notification: Map<String, Any>): Boolean {
        return try {
            rabbitTemplate.convertAndSend("notifications", notification)
            true
        } catch (e: Exception) {
            println("Failed to enqueue notification: ${e.message}")
            false
        }
    }
    
    fun enqueueBatch(notifications: List<Map<String, Any>>): String {
        val batchId = "batch_${System.currentTimeMillis()}"
        val batch = mapOf(
            "id" to batchId,
            "notifications" to notifications,
            "createdAt" to System.currentTimeMillis(),
            "status" to "PENDING"
        )
        
        redisTemplate.opsForValue().set(
            "batch:$batchId",
            batch,
            java.time.Duration.ofDays(7)
        )
        
        notifications.forEach { notification ->
            enqueueNotification(notification)
        }
        
        return batchId
    }
}

// Scheduling Service
@Service
class SchedulingService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val notificationService: NotificationService
) {
    
    fun createSchedule(request: ScheduleRequest): Map<String, Any> {
        val schedule = mapOf(
            "id" to "schedule_${System.currentTimeMillis()}",
            "userId" to request.userId,
            "templateId" to request.templateId,
            "variables" to request.variables,
            "schedule" to request.schedule,
            "isActive" to true,
            "nextRun" to calculateNextRun(request.schedule),
            "createdAt" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForValue().set(
            "schedule:${schedule["id"]}",
            schedule,
            java.time.Duration.ofDays(365)
        )
        
        return schedule
    }
    
    private fun calculateNextRun(scheduleConfig: Map<String, Any>): Long {
        val type = scheduleConfig["type"] as? String ?: "IMMEDIATE"
        
        return when (type) {
            "IMMEDIATE" -> System.currentTimeMillis()
            "DAILY" -> {
                val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
                val (hour, minute) = timeOfDay.split(":").map { it.toInt() }
                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                val nextRun = now.withHour(hour).withMinute(minute).withSecond(0)
                if (nextRun.isBefore(now)) {
                    nextRun.plusDays(1).toInstant().toEpochMilli()
                } else {
                    nextRun.toInstant().toEpochMilli()
                }
            }
            "WEEKLY" -> {
                val dayOfWeek = (scheduleConfig["dayOfWeek"] as? Number)?.toInt() ?: 1
                val timeOfDay = scheduleConfig["timeOfDay"] as? String ?: "09:00"
                val (hour, minute) = timeOfDay.split(":").map { it.toInt() }
                val now = ZonedDateTime.now(ZoneId.of("UTC"))
                var nextRun = now.withHour(hour).withMinute(minute).withSecond(0)
                
                // Adjust to the specified day of week
                while (nextRun.dayOfWeek.value != dayOfWeek) {
                    nextRun = nextRun.plusDays(1)
                }
                
                if (nextRun.isBefore(now)) {
                    nextRun = nextRun.plusWeeks(1)
                }
                
                nextRun.toInstant().toEpochMilli()
            }
            else -> System.currentTimeMillis()
        }
    }
}

// Analytics Service
@Service
class AnalyticsService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    fun trackEvent(event: Map<String, Any>) {
        val eventWithTimestamp = event + ("timestamp" to System.currentTimeMillis())
        
        redisTemplate.opsForList().rightPush(
            "events:${event["notificationId"]}",
            eventWithTimestamp
        )
        
        // Also store in analytics database
        redisTemplate.opsForValue().set(
            "analytics:event:${System.currentTimeMillis()}_${(1000..9999).random()}",
            eventWithTimestamp,
            java.time.Duration.ofDays(30)
        )
    }
    
    fun getUserAnalytics(userId: String, startDate: Long, endDate: Long): Map<String, Any> {
        // This would typically query a time-series database
        // For now, return mock analytics
        return mapOf(
            "userId" to userId,
            "startDate" to startDate,
            "endDate" to endDate,
            "totalSent" to 25,
            "totalDelivered" to 23,
            "totalOpened" to 18,
            "totalClicked" to 12,
            "openRate" to 0.78,
            "clickRate" to 0.52,
            "engagementRate" to 0.48
        )
    }
}

// User Repository
@Repository
class UserRepository(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    
    fun getUsersWithTopicEnabled(topic: String, excludeUsers: Set<String>): List<String> {
        // This would typically query a database
        // For now, return mock data
        return listOf("user1", "user2", "user3").filter { it !in excludeUsers }
    }
}
