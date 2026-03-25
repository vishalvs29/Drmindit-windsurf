package com.drmindit.notifications.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.HttpClientErrorException
import java.net.URI
import javax.mail.internet.MimeMessage

@Service
class EmailService(
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate,
    private val javaMailSender: JavaMailSender,
    @Value("\${email.provider}") private val provider: String,
    @Value("\${email.api-key}") private val apiKey: String,
    @Value("\${email.from-email}") private val fromEmail: String,
    @Value("\${email.from-name}") private val fromName: String,
    @Value("\${email.reply-to}") private val replyTo: String?
) {
    
    fun sendNotification(notification: Map<String, Any>, emailAddress: String): Boolean {
        return try {
            val title = notification["title"] as? String ?: ""
            val body = notification["body"] as? String ?: ""
            val data = notification["data"] as? Map<String, String> ?: emptyMap()
            
            when (provider.uppercase()) {
                "SENDGRID" -> sendViaSendGrid(emailAddress, title, body, data)
                "SMTP" -> sendViaSMTP(emailAddress, title, body, data)
                "AWS_SES" -> sendViaSES(emailAddress, title, body, data)
                "MAILGUN" -> sendViaMailgun(emailAddress, title, body, data)
                else -> false
            }
        } catch (e: Exception) {
            println("Failed to send email: ${e.message}")
            false
        }
    }
    
    fun sendHTMLEmail(emailAddress: String, subject: String, htmlContent: String, textContent: String? = null): Boolean {
        return try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            
            helper.setFrom(fromEmail, fromName)
            helper.setTo(emailAddress)
            replyTo?.let { helper.setReplyTo(it) }
            helper.setSubject(subject)
            helper.setText(textContent ?: htmlContent, htmlContent, true)
            
            javaMailSender.send(message)
            true
        } catch (e: Exception) {
            println("Failed to send HTML email: ${e.message}")
            false
        }
    }
    
    fun sendTextEmail(emailAddress: String, subject: String, textContent: String): Boolean {
        return try {
            val message = SimpleMailMessage()
            message.from = "$fromName <$fromEmail>"
            message.setTo(emailAddress)
            replyTo?.let { message.replyTo = it }
            message.subject = subject
            message.text = textContent
            
            javaMailSender.send(message)
            true
        } catch (e: Exception) {
            println("Failed to send text email: ${e.message}")
            false
        }
    }
    
    fun sendVerificationEmail(emailAddress: String): Boolean {
        return try {
            val verificationCode = generateVerificationCode()
            
            // Store verification code (in production, store in Redis with expiration)
            println("Email verification code for $emailAddress: $verificationCode")
            
            val subject = "Verify Your DrMindit Account"
            val htmlContent = buildVerificationEmailTemplate(emailAddress, verificationCode)
            
            sendHTMLEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            println("Failed to send verification email: ${e.message}")
            false
        }
    }
    
    fun sendWelcomeEmail(emailAddress: String, userName: String): Boolean {
        return try {
            val subject = "Welcome to DrMindit! 🌿"
            val htmlContent = buildWelcomeEmailTemplate(userName)
            
            sendHTMLEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            println("Failed to send welcome email: ${e.message}")
            false
        }
    }
    
    fun sendWeeklyReport(emailAddress: String, userName: String, reportData: Map<String, Any>): Boolean {
        return try {
            val subject = "Your Weekly Wellness Report 📊"
            val htmlContent = buildWeeklyReportTemplate(userName, reportData)
            
            sendHTMLEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            println("Failed to send weekly report: ${e.message}")
            false
        }
    }
    
    fun sendStreakAchievement(emailAddress: String, userName: String, streakCount: Int): Boolean {
        return try {
            val subject = "🔥 $streakCount Day Streak Achievement!"
            val htmlContent = buildStreakEmailTemplate(userName, streakCount)
            
            sendHTMLEmail(emailAddress, subject, htmlContent)
        } catch (e: Exception) {
            println("Failed to send streak email: ${e.message}")
            false
        }
    }
    
    private fun sendViaSendGrid(emailAddress: String, subject: String, content: String, data: Map<String, String>): Boolean {
        return try {
            val payload = mapOf(
                "personalizations" to listOf(mapOf(
                    "to" to listOf(mapOf("email" to emailAddress)),
                    "subject" to subject
                )),
                "from" to mapOf(
                    "email" to fromEmail,
                    "name" to fromName
                ),
                "content" to listOf(mapOf(
                    "type" to "text/html",
                    "value" to content
                )),
                "tracking_settings" to mapOf(
                    "click_tracking" to mapOf("enable" to true),
                    "open_tracking" to mapOf("enable" to true)
                )
            )
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set("Authorization", "Bearer $apiKey")
            
            val entity = HttpEntity(payload, headers)
            
            val response = restTemplate.exchange(
                URI("https://api.sendgrid.com/v3/mail/send"),
                HttpMethod.POST,
                entity,
                String::class.java
            )
            
            response.statusCode == HttpStatus.ACCEPTED
        } catch (e: Exception) {
            println("SendGrid error: ${e.message}")
            false
        }
    }
    
    private fun sendViaSMTP(emailAddress: String, subject: String, content: String, data: Map<String, String>): Boolean {
        return try {
            sendHTMLEmail(emailAddress, subject, content)
        } catch (e: Exception) {
            println("SMTP error: ${e.message}")
            false
        }
    }
    
    private fun sendViaSES(emailAddress: String, subject: String, content: String, data: Map<String, String>): Boolean {
        // AWS SES implementation would go here
        return sendHTMLEmail(emailAddress, subject, content)
    }
    
    private fun sendViaMailgun(emailAddress: String, subject: String, content: String, data: Map<String, String>): Boolean {
        // Mailgun implementation would go here
        return sendHTMLEmail(emailAddress, subject, content)
    }
    
    private fun buildVerificationEmailTemplate(emailAddress: String, verificationCode: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verify Your DrMindit Account</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2196F3, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 24px; font-weight: bold; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .verification-code { background: #2196F3; color: white; font-size: 32px; font-weight: bold; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; letter-spacing: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    .button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🌿 DrMindit</div>
                    <h2>Verify Your Email Address</h2>
                </div>
                <div class="content">
                    <p>Hello,</p>
                    <p>Thank you for signing up for DrMindit! To complete your registration, please verify your email address using the code below:</p>
                    
                    <div class="verification-code">$verificationCode</div>
                    
                    <p>This code will expire in 10 minutes for your security.</p>
                    
                    <p>If you didn't create an account with DrMindit, please ignore this email.</p>
                    
                    <div class="footer">
                        <p>Best regards,<br>The DrMindit Team</p>
                        <p>🌿 Your mental wellness companion</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun buildWelcomeEmailTemplate(userName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to DrMindit</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2196F3, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 24px; font-weight: bold; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .feature { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #4CAF50; }
                    .button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🌿 DrMindit</div>
                    <h1>Welcome to Your Wellness Journey!</h1>
                </div>
                <div class="content">
                    <p>Hello $userName,</p>
                    <p>We're thrilled to have you join the DrMindit community! Your mental wellness journey starts now.</p>
                    
                    <div class="feature">
                        <h3>🧘 Guided Meditation</h3>
                        <p>Access hundreds of guided meditation sessions for stress relief, better sleep, and mindfulness.</p>
                    </div>
                    
                    <div class="feature">
                        <h3>🎵 Personalized Content</h3>
                        <p>Get recommendations tailored to your specific needs and preferences.</p>
                    </div>
                    
                    <div class="feature">
                        <h3>📊 Progress Tracking</h3>
                        <p>Monitor your wellness journey with detailed insights and achievements.</p>
                    </div>
                    
                    <div style="text-align: center;">
                        <a href="https://drmindit.app" class="button">Start Your Journey</a>
                    </div>
                    
                    <p>Here are some quick tips to get started:</p>
                    <ul>
                        <li>Complete your wellness profile for personalized recommendations</li>
                        <li>Try our 5-minute daily meditation</li>
                        <li>Set up your notification preferences</li>
                        <li>Explore different session categories</li>
                    </ul>
                    
                    <div class="footer">
                        <p>We're here to support you every step of the way.</p>
                        <p>Best regards,<br>The DrMindit Team</p>
                        <p>🌿 Your mental wellness companion</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun buildWeeklyReportTemplate(userName: String, reportData: Map<String, Any>): String {
        val sessionsCompleted = reportData["sessionsCompleted"] as? Int ?: 0
        val totalMinutes = reportData["totalMinutes"] as? Int ?: 0
        val streakDays = reportData["streakDays"] as? Int ?: 0
        val favoriteCategory = reportData["favoriteCategory"] as? String ?: "Meditation"
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Your Weekly Wellness Report</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2196F3, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 24px; font-weight: bold; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .stats { display: flex; justify-content: space-around; margin: 20px 0; }
                    .stat { text-align: center; padding: 20px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .stat-number { font-size: 32px; font-weight: bold; color: #2196F3; }
                    .stat-label { color: #666; font-size: 14px; }
                    .achievement { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #4CAF50; }
                    .button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🌿 DrMindit</div>
                    <h1>Your Weekly Wellness Report 📊</h1>
                </div>
                <div class="content">
                    <p>Hello $userName,</p>
                    <p>Here's your wellness summary for this past week. Great job prioritizing your mental health!</p>
                    
                    <div class="stats">
                        <div class="stat">
                            <div class="stat-number">$sessionsCompleted</div>
                            <div class="stat-label">Sessions Completed</div>
                        </div>
                        <div class="stat">
                            <div class="stat-number">$totalMinutes</div>
                            <div class="stat-label">Total Minutes</div>
                        </div>
                        <div class="stat">
                            <div class="stat-number">$streakDays</div>
                            <div class="stat-label">Day Streak 🔥</div>
                        </div>
                    </div>
                    
                    <div class="achievement">
                        <h3>🌟 Favorite Category</h3>
                        <p>You've been enjoying $favoriteCategory sessions the most this week!</p>
                    </div>
                    
                    <div style="text-align: center;">
                        <a href="https://drmindit.app/reports" class="button">View Detailed Report</a>
                    </div>
                    
                    <p>Keep up the amazing work! Every minute you invest in your wellness counts.</p>
                    
                    <div class="footer">
                        <p>Looking forward to supporting your journey next week!</p>
                        <p>Best regards,<br>The DrMindit Team</p>
                        <p>🌿 Your mental wellness companion</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun buildStreakEmailTemplate(userName: String, streakCount: Int): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Congratulations on Your Streak!</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #FF6B6B, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 24px; font-weight: bold; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .streak-display { text-align: center; margin: 30px 0; }
                    .streak-number { font-size: 72px; font-weight: bold; color: #FF6B6B; line-height: 1; }
                    .streak-text { font-size: 24px; color: #666; margin-top: 10px; }
                    .achievement { background: white; padding: 20px; margin: 15px 0; border-radius: 8px; border-left: 4px solid #4CAF50; }
                    .button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="logo">🌿 DrMindit</div>
                    <h1>Amazing Achievement! 🔥</h1>
                </div>
                <div class="content">
                    <p>Hello $userName,</p>
                    <p>You're absolutely on fire! We're thrilled to celebrate your incredible dedication to your wellness journey.</p>
                    
                    <div class="streak-display">
                        <div class="streak-number">$streakCount</div>
                        <div class="streak-text">Day Streak!</div>
                    </div>
                    
                    <div class="achievement">
                        <h3>🏆 What This Means</h3>
                        <p>You've shown up for yourself consistently, building habits that create lasting positive change in your mental wellness.</p>
                    </div>
                    
                    <div class="achievement">
                        <h3>💪 Keep It Going</h3>
                        <p>Every day you practice mindfulness, you're strengthening your mental resilience and creating a foundation for lasting peace.</p>
                    </div>
                    
                    <div style="text-align: center;">
                        <a href="https://drmindit.app" class="button">Continue Your Journey</a>
                    </div>
                    
                    <div class="footer">
                        <p>We're so proud of your commitment to wellness!</p>
                        <p>Best regards,<br>The DrMindit Team</p>
                        <p>🌿 Your mental wellness companion</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    private fun generateVerificationCode(): String {
        return String.format("%06d", kotlin.random.Random.nextInt(1000000))
    }
}

// Email Template Manager
@Service
class EmailTemplateManager(
    private val emailService: EmailService
) {
    
    fun sendDailyReminder(emailAddress: String, userName: String): Boolean {
        val subject = "🌿 Your Daily Wellness Reminder"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #2196F3, #4CAF50); color: white; padding: 30px; text-align: center; border-radius: 10px;">
                    <h2>🌿 Daily Wellness Reminder</h2>
                    <h3>Good morning, $userName!</h3>
                </div>
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p>Time to take a moment for your mental wellness today.</p>
                    <p>Your daily 5-minute meditation is ready to help you start the day with clarity and peace.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://drmindit.app/session/daily-morning" style="background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px;">Start Morning Meditation</a>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return emailService.sendHTMLEmail(emailAddress, subject, htmlContent)
    }
    
    fun sendSessionReminder(emailAddress: String, sessionTitle: String, instructor: String, duration: Int): Boolean {
        val subject = "🎧 Session Reminder: $sessionTitle"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #9C27B0, #2196F3); color: white; padding: 30px; text-align: center; border-radius: 10px;">
                    <h2>🎧 Session Reminder</h2>
                    <h3>$sessionTitle</h3>
                </div>
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p>Your session is waiting for you!</p>
                    <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <p><strong>Duration:</strong> $duration minutes</p>
                        <p><strong>Instructor:</strong> $instructor</p>
                    </div>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://drmindit.app/sessions" style="background: #9C27B0; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px;">Start Session</a>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return emailService.sendHTMLEmail(emailAddress, subject, htmlContent)
    }
    
    fun sendReEngagement(emailAddress: String, userName: String, daysSinceLastVisit: Int): Boolean {
        val subject = "🌟 We Miss You!"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #FF9800, #FF5722); color: white; padding: 30px; text-align: center; border-radius: 10px;">
                    <h2>🌟 We Miss You!</h2>
                    <h3>It's been $daysSinceLastVisit days</h3>
                </div>
                <div style="background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px;">
                    <p>Hello $userName,</p>
                    <p>Your wellness journey continues when you're ready. We've saved your progress and are here to support you whenever you return.</p>
                    <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #FF9800;">
                        <h3>🌿 What You've Missed</h3>
                        <ul>
                            <li>New sleep meditation sessions</li>
                            <li>Updated anxiety relief techniques</li>
                            <li>Personalized recommendations based on your progress</li>
                        </ul>
                    </div>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://drmindit.app/home" style="background: #FF9800; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px;">Return to Your Journey</a>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        
        return emailService.sendHTMLEmail(emailAddress, subject, htmlContent)
    }
}
