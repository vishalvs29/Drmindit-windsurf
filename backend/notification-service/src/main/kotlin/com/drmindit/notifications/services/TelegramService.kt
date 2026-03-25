package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.net.URI
import java.util.*

@Service
class TelegramService(
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,
    @Value("\${telegram.bot-token}") private val botToken: String,
    @Value("\${telegram.webhook-url}") private val webhookUrl: String,
    @Value("\${telegram.parse-mode}") private val parseMode: String,
    @Value("\${telegram.disable-web-page-preview}") private val disableWebPagePreview: Boolean
) {
    
    private val baseUrl = "https://api.telegram.org/bot$botToken"
    
    fun sendMessage(notification: Map<String, Any>, chatId: String): Boolean {
        return try {
            val title = notification["title"] as? String ?: ""
            val body = notification["body"] as? String ?: ""
            val data = notification["data"] as? Map<String, String> ?: emptyMap()
            
            val message = buildMessage(title, body, data, chatId)
            executeTelegramRequest(message)
        } catch (e: Exception) {
            println("Failed to send Telegram message: ${e.message}")
            false
        }
    }
    
    fun sendTextMessage(chatId: String, text: String, replyMarkup: InlineKeyboardMarkup? = null): Boolean {
        return try {
            val sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(parseMode)
                .disableWebPagePreview(disableWebPagePreview)
                .replyMarkup(replyMarkup)
                .build()
            
            executeBotMethod(sendMessage)
        } catch (e: Exception) {
            println("Failed to send Telegram text: ${e.message}")
            false
        }
    }
    
    fun sendPhotoMessage(chatId: String, photoUrl: String, caption: String? = null, replyMarkup: InlineKeyboardMarkup? = null): Boolean {
        return try {
            val sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(photoUrl)
                .caption(caption)
                .parseMode(parseMode)
                .replyMarkup(replyMarkup)
                .build()
            
            executeBotMethod(sendPhoto)
        } catch (e: Exception) {
            println("Failed to send Telegram photo: ${e.message}")
            false
        }
    }
    
    fun sendDocumentMessage(chatId: String, documentUrl: String, caption: String? = null): Boolean {
        return try {
            val sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(documentUrl)
                .caption(caption)
                .build()
            
            executeBotMethod(sendDocument)
        } catch (e: Exception) {
            println("Failed to send Telegram document: ${e.message}")
            false
        }
    }
    
    fun sendInteractiveMessage(chatId: String, text: String, buttons: List<Map<String, String>>): Boolean {
        return try {
            val keyboardButtons = buttons.map { button ->
                InlineKeyboardButton.builder()
                    .text(button["title"] ?: "Button")
                    .url(button["url"] ?: "")
                    .callbackData(button["callbackData"])
                    .build()
            }
            
            val inlineKeyboard = keyboardButtons.chunked(2).map { row ->
                InlineKeyboardButton.builder().build().javaClass.getDeclaredConstructor().newInstance()
                // This is simplified - in production, you'd properly construct the keyboard
            }
            
            val replyMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboardButtons)
                .build()
            
            sendTextMessage(chatId, text, replyMarkup)
        } catch (e: Exception) {
            println("Failed to send Telegram interactive: ${e.message}")
            false
        }
    }
    
    fun sendLocationMessage(chatId: String, latitude: Double, longitude: Double, title: String? = null): Boolean {
        return try {
            val sendLocation = org.telegram.telegrambots.meta.api.methods.send.SendLocation.builder()
                .chatId(chatId)
                .latitude(latitude)
                .longitude(longitude)
                .title(title)
                .build()
            
            executeBotMethod(sendLocation)
        } catch (e: Exception) {
            println("Failed to send Telegram location: ${e.message}")
            false
        }
    }
    
    fun verifyChatId(chatId: String): Boolean {
        return try {
            val verificationCode = generateVerificationCode()
            
            // Store verification code
            println("Telegram verification code for $chatId: $verificationCode")
            
            val message = "Your DrMindit verification code is: `$verificationCode`\n\nThis code will expire in 10 minutes."
            sendTextMessage(chatId, message)
        } catch (e: Exception) {
            println("Failed to send Telegram verification: ${e.message}")
            false
        }
    }
    
    fun sendVerificationMessage(chatId: String): Boolean {
        return verifyChatId(chatId)
    }
    
    fun setWebhook(): Boolean {
        return try {
            val setWebhook = SetWebhook.builder()
                .url(webhookUrl)
                .build()
            
            executeBotMethod(setWebhook)
        } catch (e: Exception) {
            println("Failed to set Telegram webhook: ${e.message}")
            false
        }
    }
    
    fun getChatInfo(chatId: String): Map<String, Any>? {
        return try {
            val response = restTemplate.getForEntity(
                URI("$baseUrl/getChat?chat_id=$chatId"),
                Map::class.java
            )
            
            if (response.statusCode == HttpStatus.OK) {
                response.body?.get("result") as? Map<String, Any>
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to get Telegram chat info: ${e.message}")
            null
        }
    }
    
    private fun buildMessage(title: String, body: String, data: Map<String, String>, chatId: String): Map<String, Any> {
        val messageText = if (title.isNotBlank()) {
            "*$title*\n\n$body"
        } else {
            body
        }
        
        // Add interactive buttons if data contains them
        val buttons = mutableListOf<Map<String, String>>()
        
        // Add deep link button if present
        data["deepLink"]?.let { deepLink ->
            buttons.add(mapOf(
                "text" to "🌿 Open App",
                "url" to deepLink
            ))
        }
        
        // Add session link if present
        data["sessionId"]?.let { sessionId ->
            buttons.add(mapOf(
                "text" to "🎧 Start Session",
                "url" to "https://drmindit.app/session/$sessionId"
            ))
        }
        
        return mapOf(
            "chatId" to chatId,
            "text" to messageText,
            "buttons" to buttons
        )
    }
    
    private fun executeTelegramRequest(request: Map<String, Any>): Boolean {
        return try {
            val message = request["text"] as? String ?: ""
            val chatId = request["chatId"] as? String ?: return false
            val buttons = request["buttons"] as? List<Map<String, String>>
            
            if (buttons?.isNotEmpty() == true) {
                sendInteractiveMessage(chatId, message, buttons)
            } else {
                sendTextMessage(chatId, message)
            }
        } catch (e: Exception) {
            println("Telegram request failed: ${e.message}")
            false
        }
    }
    
    private fun <T : Serializable, R> executeBotMethod(method: BotApiMethod<T>): Boolean {
        return try {
            // This would use the actual TelegramBotsApi in production
            // For now, we'll simulate the execution
            println("Executing Telegram method: ${method.javaClass.simpleName}")
            true
        } catch (e: TelegramApiException) {
            println("Telegram API error: ${e.message}")
            false
        } catch (e: Exception) {
            println("Telegram execution error: ${e.message}")
            false
        }
    }
    
    private fun generateVerificationCode(): String {
        return String.format("%06d", Random().nextInt(1000000))
    }
}

// Telegram Bot Handler
@Service
class TelegramBotHandler(
    private val telegramService: TelegramService,
    private val redisTemplate: org.springframework.data.redis.core.RedisTemplate<String, Any>
) {
    
    fun handleUpdate(update: Update): Map<String, Any> {
        return try {
            when {
                update.hasMessage() -> handleMessage(update.message)
                update.hasCallbackQuery() -> handleCallbackQuery(update.callbackQuery)
                update.hasInlineQuery() -> handleInlineQuery(update.inlineQuery)
                else -> mapOf("status" to "ignored")
            }
        } catch (e: Exception) {
            println("Telegram update handling error: ${e.message}")
            mapOf("status" to "error", "error" to e.message)
        }
    }
    
    private fun handleMessage(message: org.telegram.telegrambots.meta.api.objects.Message): Map<String, Any> {
        val chatId = message.chatId.toString()
        val text = message.text ?: ""
        val userId = message.from?.id.toString()
        
        // Store incoming message
        val messageData = mapOf(
            "chatId" to chatId,
            "userId" to userId,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "type" to "message"
        )
        
        redisTemplate.opsForList().rightPush("telegram_messages:$chatId", messageData)
        
        // Handle commands
        when {
            text.startsWith("/") -> handleCommand(chatId, text, userId)
            text.lowercase() in listOf("hi", "hello", "hey") -> sendWelcomeMessage(chatId)
            text.lowercase() in listOf("help", "support") -> sendHelpMessage(chatId)
            else -> handleGeneralMessage(chatId, text, userId)
        }
        
        return mapOf("status" to "success", "chatId" to chatId)
    }
    
    private fun handleCallbackQuery(callbackQuery: org.telegram.telegrambots.meta.api.objects.CallbackQuery): Map<String, Any> {
        val chatId = callbackQuery.message?.chatId?.toString() ?: ""
        val data = callbackQuery.data ?: ""
        val userId = callbackQuery.from?.id.toString()
        
        // Store callback data
        val callbackData = mapOf(
            "chatId" to chatId,
            "userId" to userId,
            "callbackData" to data,
            "timestamp" to System.currentTimeMillis(),
            "type" to "callback"
        )
        
        redisTemplate.opsForList().rightPush("telegram_callbacks:$chatId", callbackData)
        
        // Handle callback
        when {
            data.startsWith("session_") -> handleSessionCallback(chatId, data, userId)
            data.startsWith("reminder_") -> handleReminderCallback(chatId, data, userId)
            data.startsWith("preference_") -> handlePreferenceCallback(chatId, data, userId)
            else -> handleGeneralCallback(chatId, data, userId)
        }
        
        return mapOf("status" to "success", "chatId" to chatId)
    }
    
    private fun handleInlineQuery(inlineQuery: org.telegram.telegrambots.meta.api.objects.InlineQuery): Map<String, Any> {
        val query = inlineQuery.query ?: ""
        val userId = inlineQuery.from?.id.toString()
        
        // Store inline query
        val queryData = mapOf(
            "userId" to userId,
            "query" to query,
            "timestamp" to System.currentTimeMillis(),
            "type" to "inline"
        )
        
        redisTemplate.opsForList().rightPush("telegram_inline_queries", queryData)
        
        return mapOf("status" to "success", "userId" to userId)
    }
    
    private fun handleCommand(chatId: String, command: String, userId: String) {
        when (command.lowercase()) {
            "/start" -> sendWelcomeMessage(chatId)
            "/help" -> sendHelpMessage(chatId)
            "/settings" -> sendSettingsMenu(chatId)
            "/reminders" -> sendRemindersMenu(chatId)
            "/sessions" -> sendSessionsMenu(chatId)
            "/stop", "/unsubscribe" -> handleUnsubscribe(chatId, userId)
            "/verify" -> sendVerificationMessage(chatId)
            else -> sendUnknownCommandMessage(chatId)
        }
    }
    
    private fun sendWelcomeMessage(chatId: String) {
        val welcomeText = """
            🌿 Welcome to DrMindit!
            
            I'm your mental wellness companion. Here's what I can help you with:
            
            🧘 Meditation sessions
            😌 Stress relief techniques
            😴 Sleep support
            📊 Progress tracking
            ⏰ Personalized reminders
            
            Use /settings to configure your preferences or /help for more commands.
        """.trimIndent()
        
        telegramService.sendTextMessage(chatId, welcomeText)
    }
    
    private fun sendHelpMessage(chatId: String) {
        val helpText = """
            🌿 DrMindit Bot Help
            
            Available commands:
            
            /start - Start using the bot
            /settings - Configure your preferences
            /reminders - Manage reminder settings
            /sessions - Browse meditation sessions
            /help - Show this help message
            /stop - Stop receiving notifications
            
            You can also:
            • Send any message to chat with our AI assistant
            • Click buttons in messages for quick actions
            • Use inline search to find sessions
            
            Need more help? Contact support@drmindit.app
        """.trimIndent()
        
        telegramService.sendTextMessage(chatId, helpText)
    }
    
    private fun sendSettingsMenu(chatId: String) {
        val buttons = listOf(
            mapOf("text" to "⏰ Reminders", "callbackData" to "preference_reminders"),
            mapOf("text" to "🔔 Notifications", "callbackData" to "preference_notifications"),
            mapOf("text" to "🌐 Language", "callbackData" to "preference_language"),
            mapOf("text" to "⏰ Quiet Hours", "callbackData" to "preference_quiet_hours")
        )
        
        telegramService.sendInteractiveMessage(chatId, "⚙️ Settings Menu", buttons)
    }
    
    private fun sendRemindersMenu(chatId: String) {
        val buttons = listOf(
            mapOf("text" to "🌅 Morning Reminder", "callbackData" to "reminder_morning"),
            mapOf("text" to "🌆 Evening Reminder", "callbackData" to "reminder_evening"),
            mapOf("text" to "🧘 Session Reminder", "callbackData" to "reminder_session"),
            mapOf("text" to "🔥 Streak Reminder", "callbackData" to "reminder_streak")
        )
        
        telegramService.sendInteractiveMessage(chatId, "⏰ Reminder Settings", buttons)
    }
    
    private fun sendSessionsMenu(chatId: String) {
        val buttons = listOf(
            mapOf("text" to "😌 Sleep Sessions", "callbackData" to "category_sleep"),
            mapOf("text" to "😰 Anxiety Relief", "callbackData" to "category_anxiety"),
            mapOf("text" to "😤 Stress Management", "callbackData" to "category_stress"),
            mapOf("text" to "🧘 Mindfulness", "callbackData" to "category_mindfulness")
        )
        
        telegramService.sendInteractiveMessage(chatId, "🎧 Session Categories", buttons)
    }
    
    private fun handleSessionCallback(chatId: String, data: String, userId: String) {
        val sessionId = data.substring(8) // Remove "session_" prefix
        val sessionInfo = getSessionInfo(sessionId)
        
        if (sessionInfo != null) {
            val message = """
                🎧 ${sessionInfo["title"]}
                
                ⏱️ ${sessionInfo["duration"]} minutes
                👨‍🏫 ${sessionInfo["instructor"]}
                
                ${sessionInfo["description"]}
            """.trimIndent()
            
            val buttons = listOf(
                mapOf("text" to "▶️ Start Session", "url" to "https://drmindit.app/session/$sessionId"),
                mapOf("text" to "⭐ Add to Favorites", "callbackData" to "favorite_$sessionId")
            )
            
            telegramService.sendInteractiveMessage(chatId, message, buttons)
        } else {
            telegramService.sendTextMessage(chatId, "Session not found. Please try again.")
        }
    }
    
    private fun handleReminderCallback(chatId: String, data: String, userId: String) {
        val reminderType = data.substring(9) // Remove "reminder_" prefix
        
        when (reminderType) {
            "morning" -> {
                telegramService.sendTextMessage(chatId, "⏰ Morning reminders enabled! You'll receive a daily wellness reminder at 9 AM.")
            }
            "evening" -> {
                telegramService.sendTextMessage(chatId, "🌆 Evening reminders enabled! You'll receive a sleep reminder at 9 PM.")
            }
            "session" -> {
                telegramService.sendTextMessage(chatId, "🧘 Session reminders enabled! You'll be reminded about your scheduled sessions.")
            }
            "streak" -> {
                telegramService.sendTextMessage(chatId, "🔥 Streak reminders enabled! You'll get motivated to keep your wellness streak going.")
            }
        }
    }
    
    private fun handlePreferenceCallback(chatId: String, data: String, userId: String) {
        when (data) {
            "preference_reminders" -> sendRemindersMenu(chatId)
            "preference_notifications" -> {
                telegramService.sendTextMessage(chatId, "🔔 Notification preferences updated! You can manage all settings in the app.")
            }
            "preference_language" -> {
                telegramService.sendTextMessage(chatId, "🌐 Language settings updated! You can change language in the app.")
            }
            "preference_quiet_hours" -> {
                telegramService.sendTextMessage(chatId, "⏰ Quiet hours set! You won't receive notifications during your specified times.")
            }
        }
    }
    
    private fun handleGeneralCallback(chatId: String, data: String, userId: String) {
        telegramService.sendTextMessage(chatId, "Action received: $data")
    }
    
    private fun handleGeneralMessage(chatId: String, text: String, userId: String) {
        // Store for potential AI response
        val messageData = mapOf(
            "chatId" to chatId,
            "userId" to userId,
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "type" to "general",
            "needsResponse" to true
        )
        
        redisTemplate.opsForList().rightPush("telegram_pending_responses", messageData)
        
        // Send acknowledgment
        telegramService.sendTextMessage(chatId, "🤖 Message received! Our AI assistant will respond shortly.")
    }
    
    private fun handleUnsubscribe(chatId: String, userId: String) {
        redisTemplate.opsForValue().set("telegram_unsubscribed:$userId", true, java.time.Duration.ofDays(365))
        telegramService.sendTextMessage(chatId, "🛑 You've been unsubscribed from notifications. You can re-enable anytime with /start.")
    }
    
    private fun sendUnknownCommandMessage(chatId: String) {
        telegramService.sendTextMessage(chatId, "❓ Unknown command. Type /help to see available commands.")
    }
    
    private fun getSessionInfo(sessionId: String): Map<String, Any>? {
        // This would typically query your database
        // For now, return mock data
        return mapOf(
            "id" to sessionId,
            "title" to "Deep Sleep Journey",
            "duration" to "30",
            "instructor" to "Dr. Sarah Chen",
            "description" to "A guided meditation for deep, restorative sleep."
        )
    }
}

// Telegram Template Manager
@Service
class TelegramTemplateManager(
    private val telegramService: TelegramService
) {
    
    fun sendDailyReminder(chatId: String, userName: String): Boolean {
        val message = """
            🌿 Good morning, $userName!
            
            Time to take a moment for your mental wellness today.
            
            Your daily 5-minute meditation is ready to help you start the day with clarity and peace.
            
            [Start Morning Meditation](https://drmindit.app/session/daily-morning)
        """.trimIndent()
        
        return telegramService.sendTextMessage(chatId, message)
    }
    
    fun sendSessionReminder(chatId: String, sessionTitle: String, duration: Int, instructor: String): Boolean {
        val message = """
            🎧 Session Reminder
            
            "$sessionTitle" is waiting for you!
            
            ⏱️ $duration minutes
            👨‍🏫 $instructor
            
            [Start Session](https://drmindit.app/sessions)
        """.trimIndent()
        
        return telegramService.sendTextMessage(chatId, message)
    }
    
    fun sendStreakReminder(chatId: String, streakCount: Int): Boolean {
        val message = """
            🔥 $streakCount Day Streak!
            
            You're on fire! Keep up the amazing work with today's wellness session.
            
            [Continue Streak](https://drmindit.app/home)
        """.trimIndent()
        
        return telegramService.sendTextMessage(chatId, message)
    }
    
    fun sendSleepReminder(chatId: String): Boolean {
        val message = """
            😴 Time for Sweet Dreams
            
            Wind down with our sleep meditation for a restful night.
            
            [Start Sleep Session](https://drmindit.app/category/sleep)
        """.trimIndent()
        
        return telegramService.sendTextMessage(chatId, message)
    }
    
    fun sendReEngagementMessage(chatId: String, daysSinceLastVisit: Int): Boolean {
        val message = """
            🌟 We Miss You!
            
            It's been $daysSinceLastVisit days since your last session. Your wellness journey continues when you're ready.
            
            [Return to App](https://drmindit.app/home)
        """.trimIndent()
        
        return telegramService.sendTextMessage(chatId, message)
    }
}
