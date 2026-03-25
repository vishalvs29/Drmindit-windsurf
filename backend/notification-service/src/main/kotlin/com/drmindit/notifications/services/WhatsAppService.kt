package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import java.net.URI
import java.util.*

@Service
class WhatsAppService(
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,
    @Value("\${whatsapp.access-token}") private val accessToken: String,
    @Value("\${whatsapp.phone-number-id}") private val phoneNumberId: String,
    @Value("\${whatsapp.api-version}") private val apiVersion: String,
    @Value("\${whatsapp.webhook-url}") private val webhookUrl: String
) {
    
    private val baseUrl = "https://graph.facebook.com/$apiVersion"
    
    fun sendMessage(notification: Map<String, Any>, phoneNumber: String): Boolean {
        return try {
            val title = notification["title"] as? String ?: ""
            val body = notification["body"] as? String ?: ""
            val data = notification["data"] as? Map<String, String> ?: emptyMap()
            
            // Try template message first
            if (data.containsKey("templateName")) {
                sendTemplateMessage(phoneNumber, data["templateName"]!!, data)
            } else {
                sendTextMessage(phoneNumber, "$title\n\n$body")
            }
        } catch (e: Exception) {
            println("Failed to send WhatsApp message: ${e.message}")
            false
        }
    }
    
    fun sendTemplateMessage(phoneNumber: String, templateName: String, variables: Map<String, String>): Boolean {
        return try {
            val templateData = buildTemplateData(templateName, variables)
            val payload = mapOf(
                "messaging_product" to "whatsapp",
                "to" to formatPhoneNumber(phoneNumber),
                "type" to "template",
                "template" to templateData
            )
            
            val response = makeWhatsAppRequest("/$phoneNumberId/messages", payload)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            println("Failed to send WhatsApp template: ${e.message}")
            false
        }
    }
    
    fun sendTextMessage(phoneNumber: String, message: String): Boolean {
        return try {
            val payload = mapOf(
                "messaging_product" to "whatsapp",
                "to" to formatPhoneNumber(phoneNumber),
                "type" to "text",
                "text" to mapOf(
                    "preview_url" to false,
                    "body" to message
                )
            )
            
            val response = makeWhatsAppRequest("/$phoneNumberId/messages", payload)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            println("Failed to send WhatsApp text: ${e.message}")
            false
        }
    }
    
    fun sendMediaMessage(phoneNumber: String, mediaUrl: String, mediaType: String, caption: String? = null): Boolean {
        return try {
            val payload = mapOf(
                "messaging_product" to "whatsapp",
                "to" to formatPhoneNumber(phoneNumber),
                "type" to mediaType,
                mediaType to mapOf(
                    "link" to mediaUrl,
                    "caption" to (caption ?: "")
                )
            )
            
            val response = makeWhatsAppRequest("/$phoneNumberId/messages", payload)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            println("Failed to send WhatsApp media: ${e.message}")
            false
        }
    }
    
    fun sendInteractiveMessage(phoneNumber: String, text: String, buttons: List<Map<String, String>>): Boolean {
        return try {
            val payload = mapOf(
                "messaging_product" to "whatsapp",
                "to" to formatPhoneNumber(phoneNumber),
                "type" to "interactive",
                "interactive" to mapOf(
                    "type" to "button",
                    "body" to mapOf(
                        "text" to text
                    ),
                    "action" to mapOf(
                        "buttons" to buttons.mapIndexed { index, button ->
                            mapOf(
                                "type" to "reply",
                                "reply" to mapOf(
                                    "id" to button["id"] ?: "btn_$index",
                                    "title" to button["title"] ?: "Button $index"
                                )
                            )
                        }
                    )
                )
            )
            
            val response = makeWhatsAppRequest("/$phoneNumberId/messages", payload)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            println("Failed to send WhatsApp interactive: ${e.message}")
            false
        }
    }
    
    fun sendLocationMessage(phoneNumber: String, latitude: Double, longitude: Double, name: String, address: String): Boolean {
        return try {
            val payload = mapOf(
                "messaging_product" to "whatsapp",
                "to" to formatPhoneNumber(phoneNumber),
                "type" to "location",
                "location" to mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "name" to name,
                    "address" to address
                )
            )
            
            val response = makeWhatsAppRequest("/$phoneNumberId/messages", payload)
            response.statusCode == HttpStatus.OK
        } catch (e: Exception) {
            println("Failed to send WhatsApp location: ${e.message}")
            false
        }
    }
    
    fun verifyPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val verificationCode = generateVerificationCode()
            
            // Store verification code
            // In production, store in Redis with expiration
            println("WhatsApp verification code for $phoneNumber: $verificationCode")
            
            val message = "Your DrMindit verification code is: $verificationCode. This code will expire in 10 minutes."
            sendTextMessage(phoneNumber, message)
        } catch (e: Exception) {
            println("Failed to send WhatsApp verification: ${e.message}")
            false
        }
    }
    
    fun sendVerificationMessage(phoneNumber: String): Boolean {
        return verifyPhoneNumber(phoneNumber)
    }
    
    private fun buildTemplateData(templateName: String, variables: Map<String, String>): Map<String, Any> {
        val components = mutableListOf<Map<String, Any>>()
        
        // Header component (if image/media is needed)
        if (variables.containsKey("headerImageUrl")) {
            components.add(mapOf(
                "type" to "header",
                "parameters" to listOf(mapOf(
                    "type" to "image",
                    "image" to mapOf(
                        "link" to variables["headerImageUrl"]
                    )
                ))
            ))
        }
        
        // Body component with variables
        val bodyComponent = mutableMapOf<String, Any>(
            "type" to "body"
        )
        
        val bodyParameters = mutableListOf<Map<String, Any>>()
        variables.filter { it.key.startsWith("body_") }.forEach { (key, value) ->
            bodyParameters.add(mapOf(
                "type" to "text",
                "text" to value
            ))
        }
        
        if (bodyParameters.isNotEmpty()) {
            bodyComponent["parameters"] = bodyParameters
        }
        components.add(bodyComponent)
        
        // Button component (if buttons are needed)
        if (variables.containsKey("buttonText")) {
            components.add(mapOf(
                "type" to "button",
                "sub_type" to "url",
                "index" to "0",
                "parameters" to listOf(mapOf(
                    "type" to "text",
                    "text" to (variables["buttonText"] ?: "Open App"),
                    "url" to (variables["buttonUrl"] ?: "https://drmindit.app")
                ))
            ))
        }
        
        return mapOf(
            "name" to templateName,
            "language" to mapOf(
                "code" to (variables["language"] ?: "en")
            ),
            "components" to components
        )
    }
    
    private fun makeWhatsAppRequest(endpoint: String, payload: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $accessToken")
        
        val entity = HttpEntity(payload, headers)
        
        return try {
            restTemplate.exchange(
                URI("$baseUrl$endpoint"),
                HttpMethod.POST,
                entity,
                object : org.springframework.core.ParameterizedTypeReference<Map<String, Any>>() {}
            )
        } catch (e: HttpClientErrorException) {
            println("WhatsApp API error: ${e.responseBodyAsString}")
            ResponseEntity.status(e.statusCode).body(mapOf("error" to e.responseBodyAsString))
        } catch (e: Exception) {
            println("WhatsApp request failed: ${e.message}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
        }
    }
    
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Remove any non-digit characters
        val cleaned = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        // Ensure it starts with country code
        return when {
            cleaned.startsWith("+") -> cleaned
            cleaned.startsWith("1") && cleaned.length == 10 -> "+1$cleaned"
            cleaned.length == 10 -> "+1$cleaned" // Assume US number
            else -> cleaned
        }
    }
    
    private fun generateVerificationCode(): String {
        return String.format("%06d", Random().nextInt(1000000))
    }
}

// WhatsApp Template Manager
@Service
class WhatsAppTemplateManager(
    private val whatsAppService: WhatsAppService,
    private val redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>
) {
    
    fun createDailyReminderTemplate(): Map<String, Any> {
        return mapOf(
            "name" to "daily_wellness_reminder",
            "category" to "MARKETING",
            "language" to "en",
            "components" to listOf(
                mapOf(
                    "type" to "header",
                    "format" to "TEXT",
                    "text" to "Daily Wellness Reminder 🌿"
                ),
                mapOf(
                    "type" to "body",
                    "text" to "Hi {{1}}, time to relax! Take 5 minutes for your mental wellness today."
                ),
                mapOf(
                    "type" to "button",
                    "text" to "Open App",
                    "url" to "https://drmindit.app/session"
                )
            )
        )
    }
    
    fun createSessionReminderTemplate(): Map<String, Any> {
        return mapOf(
            "name" to "session_reminder",
            "category" to "UTILITY",
            "language" to "en",
            "components" to listOf(
                mapOf(
                    "type" to "header",
                    "format" to "TEXT",
                    "text" to "{{1}} is waiting for you"
                ),
                mapOf(
                    "type" to "body",
                    "text" to "Your {{2}} minute session with {{3}} is ready. Take a moment for yourself."
                ),
                mapOf(
                    "type" to "button",
                    "text" to "Start Session",
                    "url" to "https://drmindit.app/session/{{4}}"
                )
            )
        )
    }
    
    fun createStreakReminderTemplate(): Map<String, Any> {
        return mapOf(
            "name" to "streak_reminder",
            "category" to "UTILITY",
            "language" to "en",
            "components" to listOf(
                mapOf(
                    "type" to "header",
                    "format" to "TEXT",
                    "text" to "{{1}} day streak! 🔥"
                ),
                mapOf(
                    "type" to "body",
                    "text" to "You're on fire! Keep up the amazing work with today's wellness session."
                ),
                mapOf(
                    "type" to "button",
                    "text" to "Continue Streak",
                    "url" to "https://drmindit.app/home"
                )
            )
        )
    }
    
    fun sendPersonalizedReminder(userId: String, phoneNumber: String, userName: String, templateName: String, variables: Map<String, String>) {
        val allVariables = mutableMapOf<String, String>()
        allVariables["body_1"] = userName
        allVariables.putAll(variables)
        
        whatsAppService.sendTemplateMessage(phoneNumber, templateName, allVariables)
    }
}

// WhatsApp Webhook Handler
@Service
class WhatsAppWebhookHandler(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>
) {
    
    fun handleWebhook(payload: Map<String, Any>): Map<String, Any> {
        try {
            val objectData = payload["object"] as? Map<String, Any>
            val entry = objectData?.get("entry") as? List<Map<String, Any>>
            
            entry?.forEach { entryItem ->
                val changes = entryItem["changes"] as? List<Map<String, Any>>
                changes?.forEach { change ->
                    val value = change["value"] as? Map<String, Any>
                    val messages = value?.get("messages") as? List<Map<String, Any>>
                    
                    messages?.forEach { message ->
                        processMessage(message)
                    }
                    
                    // Handle status updates
                    val statuses = value?.get("statuses") as? List<Map<String, Any>>
                    statuses?.forEach { status ->
                        processStatusUpdate(status)
                    }
                }
            }
            
            return mapOf("status" to "success")
        } catch (e: Exception) {
            println("Webhook processing error: ${e.message}")
            return mapOf("status" to "error", "error" to e.message)
        }
    }
    
    private fun processMessage(message: Map<String, Any>) {
        val from = message["from"] as? String
        val messageType = message["type"] as? String
        
        when (messageType) {
            "text" -> {
                val text = (message["text"] as? Map<String, Any>)?.get("body") as? String
                handleTextMessage(from ?: "", text ?: "")
            }
            "interactive" -> {
                val interactive = message["interactive"] as? Map<String, Any>
                handleInteractiveMessage(from ?: "", interactive ?: emptyMap())
            }
            "button" -> {
                val button = message["button"] as? Map<String, Any>
                handleButtonMessage(from ?: "", button ?: emptyMap())
            }
        }
    }
    
    private fun processStatusUpdate(status: Map<String, Any>) {
        val messageId = status["id"] as? String
        val statusValue = status["status"] as? String
        val recipientId = status["recipient_id"] as? String
        
        // Store status update for analytics
        val statusData = mapOf(
            "messageId" to messageId,
            "status" to statusValue,
            "recipientId" to recipientId,
            "timestamp" to System.currentTimeMillis()
        )
        
        redisTemplate.opsForList().rightPush("whatsapp_status_updates", statusData)
    }
    
    private fun handleTextMessage(from: String, text: String) {
        // Store incoming message
        val messageData = mapOf(
            "from" to from,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "type" to "incoming"
        )
        
        redisTemplate.opsForList().rightPush("whatsapp_messages:$from", messageData)
        
        // Handle commands
        when (text.lowercase()) {
            "start", "hi", "hello" -> sendWelcomeMessage(from)
            "stop", "unsubscribe" -> handleUnsubscribe(from)
            "help" -> sendHelpMessage(from)
            else -> handleGeneralMessage(from, text)
        }
    }
    
    private fun handleInteractiveMessage(from: String, interactive: Map<String, Any>) {
        val type = interactive["type"] as? String
        when (type) {
            "button_reply" -> {
                val buttonReply = interactive["button_reply"] as? Map<String, Any>
                val buttonId = buttonReply?.get("id") as? String
                handleButtonReply(from, buttonId ?: "")
            }
        }
    }
    
    private fun handleButtonMessage(from: String, button: Map<String, Any>) {
        val buttonId = button["payload"] as? String
        handleButtonReply(from, buttonId ?: "")
    }
    
    private fun sendWelcomeMessage(phoneNumber: String) {
        // Implementation for welcome message
        println("Sending welcome message to $phoneNumber")
    }
    
    private fun handleUnsubscribe(phoneNumber: String) {
        // Mark user as unsubscribed
        redisTemplate.opsForValue().set("whatsapp_unsubscribed:$phoneNumber", true, java.time.Duration.ofDays(365))
    }
    
    private fun sendHelpMessage(phoneNumber: String) {
        // Implementation for help message
        println("Sending help message to $phoneNumber")
    }
    
    private fun handleGeneralMessage(phoneNumber: String, text: String) {
        // Store for potential AI response
        val messageData = mapOf(
            "from" to phoneNumber,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "type" to "general",
            "needsResponse" to true
        )
        
        redisTemplate.opsForList().rightPush("whatsapp_pending_responses", messageData)
    }
    
    private fun handleButtonReply(phoneNumber: String, buttonId: String) {
        // Handle button interactions
        val replyData = mapOf(
            "from" to phoneNumber,
            "buttonId" to buttonId,
            "timestamp" to System.currentTimeMillis(),
            "type" to "button_reply"
        )
        
        redisTemplate.opsForList().rightPush("whatsapp_button_replies", replyData)
    }
}
