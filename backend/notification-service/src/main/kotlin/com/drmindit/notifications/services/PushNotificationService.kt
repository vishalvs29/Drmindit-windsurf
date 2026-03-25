package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@Service
class PushNotificationService(
    private val objectMapper: ObjectMapper,
    @Value("\${fcm.server-key}") private val fcmServerKey: String,
    @Value("\${fcm.service-account-path}") private val serviceAccountPath: String
) {
    
    private lateinit var firebaseMessaging: FirebaseMessaging
    
    init {
        initializeFirebase()
    }
    
    private fun initializeFirebase() {
        try {
            val serviceAccount = FileInputStream(serviceAccountPath)
            val credentials = GoogleCredentials.fromStream(serviceAccount)
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("drmindit-notifications")
                .build()
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }
            
            firebaseMessaging = FirebaseMessaging.getInstance()
        } catch (e: Exception) {
            println("Failed to initialize Firebase: ${e.message}")
        }
    }
    
    fun sendNotification(notification: Map<String, Any>, fcmToken: String): Boolean {
        return try {
            val message = buildFCMMessage(notification, fcmToken)
            val future = firebaseMessaging.sendAsync(message)
            future.get()
            true
        } catch (e: Exception) {
            println("Failed to send push notification: ${e.message}")
            false
        }
    }
    
    fun sendTopicNotification(notification: Map<String, Any>, topic: String): Boolean {
        return try {
            val message = buildTopicMessage(notification, topic)
            val future = firebaseMessaging.sendAsync(message)
            future.get()
            true
        } catch (e: Exception) {
            println("Failed to send topic notification: ${e.message}")
            false
        }
    }
    
    fun sendMulticastNotification(notification: Map<String, Any>, fcmTokens: List<String>): Map<String, Any> {
        return try {
            val message = buildMulticastMessage(notification, fcmTokens)
            val response = firebaseMessaging.sendMulticast(message)
            
            mapOf(
                "successCount" to response.successCount,
                "failureCount" to response.failureCount,
                "responses" to response.responses.map { resp ->
                    mapOf(
                        "messageId" to resp.messageId,
                        "success" to !resp.exception.isNullOrEmpty(),
                        "error" to resp.exception?.message
                    )
                }
            )
        } catch (e: Exception) {
            println("Failed to send multicast notification: ${e.message}")
            mapOf("error" to e.message)
        }
    }
    
    fun subscribeToTopic(fcmTokens: List<String>, topic: String): Boolean {
        return try {
            val response = firebaseMessaging.subscribeToTopic(fcmTokens, topic)
            response.failureCount == 0
        } catch (e: Exception) {
            println("Failed to subscribe to topic: ${e.message}")
            false
        }
    }
    
    fun unsubscribeFromTopic(fcmTokens: List<String>, topic: String): Boolean {
        return try {
            val response = firebaseMessaging.unsubscribeFromTopic(fcmTokens, topic)
            response.failureCount == 0
        } catch (e: Exception) {
            println("Failed to unsubscribe from topic: ${e.message}")
            false
        }
    }
    
    private fun buildFCMMessage(notification: Map<String, Any>, fcmToken: String): Message {
        val title = notification["title"] as? String ?: ""
        val body = notification["body"] as? String ?: ""
        val data = notification["data"] as? Map<String, String> ?: emptyMap()
        val imageUrl = (notification["metadata"] as? Map<String, Any>)?.get("imageUrl") as? String
        
        val builder = Message.builder()
            .setToken(fcmToken)
            .putAllData(data)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setNotification(
                        AndroidNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setIcon("@drawable/ic_notification")
                            .setColor("#2196F3")
                            .setSound("default")
                            .setClickAction("FLUTTER_NOTIFICATION_CLICK")
                            .setImage(imageUrl)
                            .build()
                    )
                    .setPriority(getAndroidPriority(notification["priority"] as? String))
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setAlert(
                                ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                            )
                            .setSound("default")
                            .setBadge(1)
                            .build()
                    )
                    .build()
            )
        
        // Add deep link if present
        val deepLink = (notification["metadata"] as? Map<String, Any>)?.get("deepLink") as? String
        if (!deepLink.isNullOrEmpty()) {
            builder.putData("deep_link", deepLink)
        }
        
        return builder.build()
    }
    
    private fun buildTopicMessage(notification: Map<String, Any>, topic: String): Message {
        val title = notification["title"] as? String ?: ""
        val body = notification["body"] as? String ?: ""
        val data = notification["data"] as? Map<String, String> ?: emptyMap()
        
        return Message.builder()
            .setTopic("drmindit_$topic")
            .putAllData(data)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()
    }
    
    private fun buildMulticastMessage(notification: Map<String, Any>, fcmTokens: List<String>): MulticastMessage {
        val title = notification["title"] as? String ?: ""
        val body = notification["body"] as? String ?: ""
        val data = notification["data"] as? Map<String, String> ?: emptyMap()
        
        return MulticastMessage.builder()
            .addAllTokens(fcmTokens)
            .putAllData(data)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(getAndroidPriority(notification["priority"] as? String))
                    .build()
            )
            .build()
    }
    
    private fun getAndroidPriority(priority: String?): AndroidConfig.Priority {
        return when (priority?.uppercase()) {
            "HIGH" -> AndroidConfig.Priority.HIGH
            "URGENT" -> AndroidConfig.Priority.HIGH
            "LOW" -> AndroidConfig.Priority.LOW
            else -> AndroidConfig.Priority.NORMAL
        }
    }
}

// FCM Token Management Service
@Service
class FCMTokenService(
    private val redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>
) {
    
    fun registerToken(userId: String, fcmToken: String, deviceInfo: Map<String, String> = emptyMap()) {
        val tokenData = mapOf(
            "userId" to userId,
            "fcmToken" to fcmToken,
            "deviceInfo" to deviceInfo,
            "registeredAt" to System.currentTimeMillis(),
            "isActive" to true
        )
        
        redisTemplate.opsForValue().set(
            "fcm_token:$userId:$fcmToken",
            tokenData,
            java.time.Duration.ofDays(365)
        )
        
        // Also maintain a list of user's tokens
        redisTemplate.opsForSet().add("user_tokens:$userId", fcmToken)
    }
    
    fun unregisterToken(userId: String, fcmToken: String) {
        redisTemplate.delete("fcm_token:$userId:$fcmToken")
        redisTemplate.opsForSet().remove("user_tokens:$userId", fcmToken)
    }
    
    fun getUserTokens(userId: String): List<String> {
        val tokens = redisTemplate.opsForSet().members("user_tokens:$userId")
        return tokens?.map { it.toString() } ?: emptyList()
    }
    
    fun isTokenValid(fcmToken: String): Boolean {
        // This would typically validate with FCM API
        // For now, check if token exists in our system
        val keys = redisTemplate.keys("fcm_token:*:$fcmToken") ?: emptyList()
        return keys.isNotEmpty()
    }
    
    fun cleanupExpiredTokens() {
        val keys = redisTemplate.keys("fcm_token:*") ?: emptyList()
        val now = System.currentTimeMillis()
        val expirationTime = 30 * 24 * 60 * 60 * 1000L // 30 days
        
        keys.forEach { key ->
            val tokenData = redisTemplate.opsForValue().get(key) as? Map<String, Any>
            if (tokenData != null) {
                val registeredAt = tokenData["registeredAt"] as? Long ?: 0L
                if (now - registeredAt > expirationTime) {
                    val parts = key.split(":")
                    if (parts.size >= 3) {
                        val userId = parts[1]
                        val token = parts[2]
                        redisTemplate.delete(key)
                        redisTemplate.opsForSet().remove("user_tokens:$userId", token)
                    }
                }
            }
        }
    }
}

// Topic Management Service
@Service
class TopicManagementService(
    private val fcmTokenService: FCMTokenService,
    private val pushNotificationService: PushNotificationService
) {
    
    fun subscribeUserToTopics(userId: String, topics: List<String>) {
        val tokens = fcmTokenService.getUserTokens(userId)
        topics.forEach { topic ->
            pushNotificationService.subscribeToTopic(tokens, "drmindit_$topic")
        }
    }
    
    fun unsubscribeUserFromTopics(userId: String, topics: List<String>) {
        val tokens = fcmTokenService.getUserTokens(userId)
        topics.forEach { topic ->
            pushNotificationService.unsubscribeFromTopic(tokens, "drmindit_$topic")
        }
    }
    
    fun updateTopicSubscription(userId: String, topic: String, subscribed: Boolean) {
        val tokens = fcmTokenService.getUserTokens(userId)
        if (subscribed) {
            pushNotificationService.subscribeToTopic(tokens, "drmindit_$topic")
        } else {
            pushNotificationService.unsubscribeFromTopic(tokens, "drmindit_$topic")
        }
    }
    
    fun broadcastToTopic(topic: String, notification: Map<String, Any>): Map<String, Any> {
        val success = pushNotificationService.sendTopicNotification(notification, topic)
        return mapOf(
            "success" to success,
            "topic" to topic,
            "notificationId" to notification["id"],
            "timestamp" to System.currentTimeMillis()
        )
    }
}

// Push Notification Analytics
@Service
class PushNotificationAnalytics(
    private val redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>
) {
    
    fun trackDelivery(notificationId: String, fcmToken: String, success: Boolean, error: String? = null) {
        val event = mapOf(
            "notificationId" to notificationId,
            "fcmToken" to fcmToken,
            "event" to "delivery",
            "success" to success,
            "error" to error,
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("push_analytics:$notificationId", event)
    }
    
    fun trackOpen(notificationId: String, fcmToken: String) {
        val event = mapOf(
            "notificationId" to notificationId,
            "fcmToken" to fcmToken,
            "event" to "open",
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("push_analytics:$notificationId", event)
    }
    
    fun trackClick(notificationId: String, fcmToken: String, action: String? = null) {
        val event = mapOf(
            "notificationId" to notificationId,
            "fcmToken" to fcmToken,
            "event" to "click",
            "action" to action,
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("push_analytics:$notificationId", event)
    }
    
    fun getNotificationAnalytics(notificationId: String): Map<String, Any> {
        val events = redisTemplate.opsForList().range("push_analytics:$notificationId", 0, -1) ?: emptyList()
        
        val deliveries = events.filter { (it as Map<String, Any>)["event"] == "delivery" }
        val opens = events.filter { (it as Map<String, Any>)["event"] == "open" }
        val clicks = events.filter { (it as Map<String, Any>)["event"] == "click" }
        
        val successfulDeliveries = deliveries.filter { (it as Map<String, Any>)["success"] == true }
        
        return mapOf(
            "notificationId" to notificationId,
            "totalSent" to deliveries.size,
            "successfulDeliveries" to successfulDeliveries.size,
            "opens" to opens.size,
            "clicks" to clicks.size,
            "deliveryRate" to if (deliveries.isNotEmpty()) successfulDeliveries.size.toFloat() / deliveries.size else 0f,
            "openRate" to if (successfulDeliveries.isNotEmpty()) opens.size.toFloat() / successfulDeliveries.size else 0f,
            "clickRate" to if (opens.isNotEmpty()) clicks.size.toFloat() / opens.size else 0f
        )
    }
}
