package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

@Service
class NotificationReliabilityService(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val notificationService: NotificationService,
    private val analyticsService: NotificationAnalyticsService,
    @Value("\${notification.retry.max-attempts}") private val maxRetryAttempts: Int,
    @Value("\${notification.retry.base-delay}") private val baseRetryDelay: Long,
    @Value("\${notification.rate-limit.per-minute}") private val rateLimitPerMinute: Int,
    @Value("\${notification.rate-limit.per-hour}") private val rateLimitPerHour: Int,
    @Value("\${notification.rate-limit.per-day}") private val rateLimitPerDay: Int
) {
    
    private val retryExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    private val failedNotifications = ConcurrentHashMap<String, FailedNotification>()
    private val rateLimiters = ConcurrentHashMap<String, RateLimiter>()
    
    // Process failed notifications every 5 minutes
    @Scheduled(fixedDelay = 300000) // 5 minutes
    fun processFailedNotifications() {
        try {
            val failedNotifs = getFailedNotifications()
            
            failedNotifs.forEach { failedNotif ->
                if (shouldRetryNotification(failedNotif)) {
                    retryNotification(failedNotif)
                }
            }
        } catch (e: Exception) {
            println("Error processing failed notifications: ${e.message}")
        }
    }
    
    // Clean up old failed notifications every hour
    @Scheduled(fixedDelay = 3600000) // 1 hour
    fun cleanupOldFailedNotifications() {
        try {
            val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // 24 hours ago
            val keys = redisTemplate.keys("failed_notification:*") ?: emptyList()
            
            keys.forEach { key ->
                val failedNotif = redisTemplate.opsForValue().get(key) as? FailedNotification
                if (failedNotif != null && failedNotif.lastAttemptTime < cutoffTime) {
                    redisTemplate.delete(key)
                    failedNotifications.remove(failedNotif.notificationId)
                }
            }
        } catch (e: Exception) {
            println("Error cleaning up failed notifications: ${e.message}")
        }
    }
    
    // Update rate limit counters every minute
    @Scheduled(fixedDelay = 60000) // 1 minute
    fun updateRateLimiters() {
        try {
            val now = System.currentTimeMillis()
            rateLimiters.values.forEach { rateLimiter ->
                rateLimiter.updateCounters(now)
            }
        } catch (e: Exception) {
            println("Error updating rate limiters: ${e.message}")
        }
    }
    
    // Monitor system health every 10 minutes
    @Scheduled(fixedDelay = 600000) // 10 minutes
    fun monitorSystemHealth() {
        try {
            val healthMetrics = collectHealthMetrics()
            
            if (healthMetrics.requiresAlert()) {
                sendHealthAlert(healthMetrics)
            }
            
            // Store health metrics for monitoring
            redisTemplate.opsForValue().set(
                "system_health:${System.currentTimeMillis()}",
                healthMetrics.toMap(),
                Duration.ofHours(24)
            )
        } catch (e: Exception) {
            println("Error monitoring system health: ${e.message}")
        }
    }
    
    fun handleNotificationFailure(notificationId: String, error: String, channel: String) {
        val failedNotif = FailedNotification(
            notificationId = notificationId,
            error = error,
            channel = channel,
            attemptCount = 1,
            lastAttemptTime = System.currentTimeMillis(),
            nextRetryTime = calculateNextRetryTime(1)
        )
        
        failedNotifications[notificationId] = failedNotif
        
        // Store in Redis
        redisTemplate.opsForValue().set(
            "failed_notification:$notificationId",
            failedNotif,
            Duration.ofHours(24)
        )
        
        // Log failure
        logNotificationFailure(notificationId, error, channel)
    }
    
    fun handleNotificationSuccess(notificationId: String) {
        // Remove from failed notifications
        failedNotifications.remove(notificationId)
        redisTemplate.delete("failed_notification:$notificationId")
        
        // Log success
        logNotificationSuccess(notificationId)
    }
    
    fun checkRateLimit(userId: String, channel: String): RateLimitResult {
        val rateLimiterKey = "$userId:$channel"
        val rateLimiter = rateLimiters.getOrPut(rateLimiterKey) {
            RateLimiter(rateLimitPerMinute, rateLimitPerHour, rateLimitPerDay)
        }
        
        return rateLimiter.checkLimit()
    }
    
    fun recordNotificationSent(userId: String, channel: String) {
        val rateLimiterKey = "$userId:$channel"
        rateLimiters[rateLimiterKey]?.recordSent()
    }
    
    private fun retryNotification(failedNotif: FailedNotification) {
        val notification = getNotificationFromId(failedNotif.notificationId)
        if (notification != null) {
            // Update attempt count
            val updatedFailedNotif = failedNotif.copy(
                attemptCount = failedNotif.attemptCount + 1,
                lastAttemptTime = System.currentTimeMillis()
            )
            
            failedNotifications[failedNotif.notificationId] = updatedFailedNotif
            
            // Store updated failed notification
            redisTemplate.opsForValue().set(
                "failed_notification:$failedNotif.notificationId",
                updatedFailedNotif,
                Duration.ofHours(24)
            )
            
            // Schedule retry with exponential backoff
            val delay = calculateRetryDelay(updatedFailedNotif.attemptCount)
            
            retryExecutorService.schedule({
                val result = notificationService.sendNotification(notification)
                if (result.isSuccess) {
                    handleNotificationSuccess(failedNotif.notificationId)
                } else {
                    handleNotificationFailure(failedNotif.notificationId, result.exceptionOrNull()?.message ?: "Unknown error", updatedFailedNotif.channel)
                }
            }, delay, java.util.concurrent.TimeUnit.MILLISECONDS)
        }
    }
    
    private fun shouldRetryNotification(failedNotif: FailedNotification): Boolean {
        return failedNotif.attemptCount < maxRetryAttempts &&
               System.currentTimeMillis() >= failedNotif.nextRetryTime
    }
    
    private fun getFailedNotifications(): List<FailedNotification> {
        val keys = redisTemplate.keys("failed_notification:*") ?: emptyList()
        val failedNotifs = mutableListOf<FailedNotification>()
        
        keys.forEach { key ->
            val failedNotif = redisTemplate.opsForValue().get(key) as? FailedNotification
            if (failedNotif != null) {
                failedNotifs.add(failedNotif)
            }
        }
        
        return failedNotifs
    }
    
    private fun calculateNextRetryTime(attemptCount: Int): Long {
        val delay = calculateRetryDelay(attemptCount)
        return System.currentTimeMillis() + delay
    }
    
    private fun calculateRetryDelay(attemptCount: Int): Long {
        // Exponential backoff with jitter
        val baseDelay = baseRetryDelay
        val exponentialDelay = baseDelay * Math.pow(2.0, (attemptCount - 1).toDouble()).toLong()
        val maxDelay = 30 * 60 * 1000L // 30 minutes max
        
        // Add jitter to prevent thundering herd
        val jitter = (Math.random() * 0.1 * exponentialDelay).toLong()
        
        return minOf(exponentialDelay + jitter, maxDelay)
    }
    
    private fun getNotificationFromId(notificationId: String): Map<String, Any>? {
        return redisTemplate.opsForValue().get("notification:$notificationId") as? Map<String, Any>
    }
    
    private fun logNotificationFailure(notificationId: String, error: String, channel: String) {
        val logEntry = mapOf(
            "notificationId" to notificationId,
            "type" to "failure",
            "error" to error,
            "channel" to channel,
            "timestamp" to System.currentTimeMillis(),
            "severity" to determineErrorSeverity(error)
        )
        
        redisTemplate.opsForList().rightPush("notification_logs", logEntry)
        
        // Keep only last 1000 log entries
        redisTemplate.opsForList().trim("notification_logs", 1000)
    }
    
    private fun logNotificationSuccess(notificationId: String) {
        val logEntry = mapOf(
            "notificationId" to notificationId,
            "type" to "success",
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("notification_logs", logEntry)
        
        // Keep only last 1000 log entries
        redisTemplate.opsForList().trim("notification_logs", 1000)
    }
    
    private fun determineErrorSeverity(error: String): String {
        return when {
            error.contains("timeout", ignoreCase = true) -> "medium"
            error.contains("rate limit", ignoreCase = true) -> "low"
            error.contains("authentication", ignoreCase = true) -> "high"
            error.contains("permission", ignoreCase = true) -> "high"
            error.contains("network", ignoreCase = true) -> "medium"
            else -> "low"
        }
    }
    
    private fun collectHealthMetrics(): SystemHealthMetrics {
        val now = System.currentTimeMillis()
        val last5Minutes = now - (5 * 60 * 1000L)
        val last1Hour = now - (60 * 60 * 1000L)
        
        val recentLogs = redisTemplate.opsForList().range("notification_logs", 0, -1) ?: emptyList<Any>()
        
        val last5MinLogs = recentLogs.filter { log ->
            val timestamp = (log as Map<String, Any>)["timestamp"] as? Long ?: 0L
            timestamp >= last5Minutes
        }
        
        val last1HourLogs = recentLogs.filter { log ->
            val timestamp = (log as Map<String, Any>)["timestamp"] as? Long ?: 0L
            timestamp >= last1Hour
        }
        
        val successCount = last1HourLogs.count { log ->
            (log as Map<String, Any>)["type"] == "success"
        }
        
        val failureCount = last1HourLogs.count { log ->
            (log as Map<String, Any>)["type"] == "failure"
        }
        
        val highSeverityCount = last1HourLogs.count { log ->
            (log as Map<String, Any>)["severity"] == "high"
        }
        
        val totalAttempts = successCount + failureCount
        val successRate = if (totalAttempts > 0) successCount.toFloat() / totalAttempts else 1.0f
        
        return SystemHealthMetrics(
            totalNotifications = totalAttempts,
            successRate = successRate,
            failureRate = 1.0f - successRate,
            highSeverityErrors = highSeverityCount,
            averageResponseTime = calculateAverageResponseTime(last1HourLogs),
            queueSize = getQueueSize(),
            activeRateLimiters = rateLimiters.values.count { it.isCurrentlyLimited() },
            timestamp = now
        )
    }
    
    private fun calculateAverageResponseTime(logs: List<Any>): Long {
        val responseTimes = logs.mapNotNull { log ->
            val logMap = log as Map<String, Any>
            logMap["responseTime"] as? Long
        }
        
        return if (responseTimes.isNotEmpty()) {
            responseTimes.average().toLong()
        } else {
            0L
        }
    }
    
    private fun getQueueSize(): Int {
        // This would typically check the actual queue size
        // For now, return a mock value
        return 0
    }
    
    private fun sendHealthAlert(metrics: SystemHealthMetrics) {
        val alertMessage = buildHealthAlertMessage(metrics)
        
        // Send alert through monitoring system
        val alert = mapOf(
            "type" to "health_alert",
            "severity" to if (metrics.requiresAlert()) "high" else "medium",
            "message" to alertMessage,
            "metrics" to metrics.toMap(),
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("health_alerts", alert)
        
        // In production, this would send to monitoring service like PagerDuty, DataDog, etc.
        println("HEALTH ALERT: $alertMessage")
    }
    
    private fun buildHealthAlertMessage(metrics: SystemHealthMetrics): String {
        return when {
            metrics.successRate < 0.8 -> "Low success rate detected: ${String.format("%.1f%%", metrics.successRate * 100)}"
            metrics.highSeverityErrors > 5 -> "High number of severe errors: ${metrics.highSeverityErrors}"
            metrics.activeRateLimiters > 10 -> "High rate limiting activity: ${metrics.activeRateLimiters} users limited"
            metrics.averageResponseTime > 5000 -> "High response time: ${metrics.averageResponseTime}ms"
            metrics.queueSize > 1000 -> "High queue size: ${metrics.queueSize} notifications"
            else -> "System health degraded"
        }
    }
}

// Data classes
data class FailedNotification(
    val notificationId: String,
    val error: String,
    val channel: String,
    var attemptCount: Int,
    var lastAttemptTime: Long,
    var nextRetryTime: Long
)

data class RateLimitResult(
    val allowed: Boolean,
    val reason: String? = null,
    val retryAfter: Long? = null
)

data class RateLimiter(
    private val perMinuteLimit: Int,
    private val perHourLimit: Int,
    private val perDayLimit: Int
) {
    private var minuteCount = 0
    private var hourCount = 0
    private var dayCount = 0
    private var lastMinuteReset = 0L
    private var lastHourReset = 0L
    private var lastDayReset = 0L
    
    fun checkLimit(): RateLimitResult {
        val now = System.currentTimeMillis()
        
        // Reset counters if needed
        if (now - lastMinuteReset >= 60 * 1000L) {
            minuteCount = 0
            lastMinuteReset = now
        }
        
        if (now - lastHourReset >= 60 * 60 * 1000L) {
            hourCount = 0
            lastHourReset = now
        }
        
        if (now - lastDayReset >= 24 * 60 * 60 * 1000L) {
            dayCount = 0
            lastDayReset = now
        }
        
        return when {
            minuteCount >= perMinuteLimit -> RateLimitResult(
                allowed = false,
                reason = "Rate limit per minute exceeded",
                retryAfter = lastMinuteReset + 60 * 1000L
            )
            hourCount >= perHourLimit -> RateLimitResult(
                allowed = false,
                reason = "Rate limit per hour exceeded",
                retryAfter = lastHourReset + 60 * 60 * 1000L
            )
            dayCount >= perDayLimit -> RateLimitResult(
                allowed = false,
                reason = "Rate limit per day exceeded",
                retryAfter = lastDayReset + 24 * 60 * 60 * 1000L
            )
            else -> RateLimitResult(allowed = true)
        }
    }
    
    fun recordSent() {
        minuteCount++
        hourCount++
        dayCount++
    }
    
    fun updateCounters(now: Long) {
        if (now - lastMinuteReset >= 60 * 1000L) {
            minuteCount = 0
            lastMinuteReset = now
        }
        
        if (now - lastHourReset >= 60 * 60 * 1000L) {
            hourCount = 0
            lastHourReset = now
        }
        
        if (now - lastDayReset >= 24 * 60 * 60 * 1000L) {
            dayCount = 0
            lastDayReset = now
        }
    }
    
    fun isCurrentlyLimited(): Boolean {
        val now = System.currentTimeMillis()
        return minuteCount >= perMinuteLimit ||
               hourCount >= perHourLimit ||
               dayCount >= perDayLimit
    }
}

data class SystemHealthMetrics(
    val totalNotifications: Int,
    val successRate: Float,
    val failureRate: Float,
    val highSeverityErrors: Int,
    val averageResponseTime: Long,
    val queueSize: Int,
    val activeRateLimiters: Int,
    val timestamp: Long
) {
    fun requiresAlert(): Boolean {
        return successRate < 0.8 ||
               highSeverityErrors > 5 ||
               activeRateLimiters > 10 ||
               averageResponseTime > 5000 ||
               queueSize > 1000
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "totalNotifications" to totalNotifications,
            "successRate" to successRate,
            "failureRate" to failureRate,
            "highSeverityErrors" to highSeverityErrors,
            "averageResponseTime" to averageResponseTime,
            "queueSize" to queueSize,
            "activeRateLimiters" to activeRateLimiters,
            "timestamp" to timestamp
        )
    }
}

// Circuit breaker pattern for channel resilience
class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val timeoutDuration: Long = 60000 // 1 minute
) {
    private var failureCount = 0
    private var lastFailureTime = 0L
    private var state: CircuitState = CircuitState.CLOSED
    
    enum class CircuitState {
        CLOSED,    // Normal operation
        OPEN,      // Circuit is open, calls fail fast
        HALF_OPEN   // Testing if circuit should close
    }
    
    fun <T> execute(operation: () -> T): Result<T> {
        return when (state) {
            CircuitState.OPEN -> {
                if (System.currentTimeMillis() - lastFailureTime > timeoutDuration) {
                    state = CircuitState.HALF_OPEN
                    executeWithCircuitProtection(operation)
                } else {
                    Result.failure(Exception("Circuit breaker is OPEN"))
                }
            }
            CircuitState.HALF_OPEN -> {
                executeWithCircuitProtection(operation)
            }
            CircuitState.CLOSED -> {
                executeWithCircuitProtection(operation)
            }
        }
    }
    
    private fun <T> executeWithCircuitProtection(operation: () -> T): Result<T> {
        return try {
            val result = operation()
            onSuccess()
            Result.success(result)
        } catch (e: Exception) {
            onFailure()
            Result.failure(e)
        }
    }
    
    private fun onSuccess() {
        failureCount = 0
        if (state == CircuitState.HALF_OPEN) {
            state = CircuitState.CLOSED
        }
    }
    
    private fun onFailure() {
        failureCount++
        lastFailureTime = System.currentTimeMillis()
        
        if (failureCount >= failureThreshold) {
            state = CircuitState.OPEN
        }
    }
}
