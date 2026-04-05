package com.drmindit.shared.data.config

import kotlinx.serialization.Serializable

/**
 * Secure Configuration Manager
 * 
 * SECURITY CRITICAL:
 * - NO API keys in source code
 * - All sensitive data from environment variables
 * - Build-time configuration only
 * - Runtime secrets from secure storage
 */
@Serializable
data class SecureConfig(
    val environment: Environment,
    val apiEndpoints: ApiEndpoints,
    val securitySettings: SecuritySettings,
    val rateLimitSettings: RateLimitSettings
) {
    companion object {
        fun create(): SecureConfig {
            val environment = when {
                BuildConfig.DEBUG -> Environment.DEVELOPMENT
                else -> Environment.PRODUCTION
            }
            
            val apiEndpoints = when (environment) {
                Environment.DEVELOPMENT -> ApiEndpoints(
                    chatApi = "http://10.0.2.2:3001/api/chat",
                    authApi = "http://10.0.2.2:3001/api/auth/login",
                    sessionValidation = "http://10.0.2.2:3001/api/session/validate",
                    rateLimitStatus = "http://10.0.2.2:3001/api/rate-limit/status"
                )
                Environment.PRODUCTION -> ApiEndpoints(
                    chatApi = "https://api.drmindit.com/api/chat",
                    authApi = "https://api.drmindit.com/api/auth/login",
                    sessionValidation = "https://api.drmindit.com/api/session/validate",
                    rateLimitStatus = "https://api.drmindit.com/api/rate-limit/status"
                )
                Environment.STAGING -> ApiEndpoints(
                    chatApi = "https://staging.drmindit.com/api/chat",
                    authApi = "https://staging.drmindit.com/api/auth/login",
                    sessionValidation = "https://staging.drmindit.com/api/session/validate",
                    rateLimitStatus = "https://staging.drmindit.com/api/rate-limit/status"
                )
            }
            
            val securitySettings = SecuritySettings(
                enableAuthentication = true,
                enableRateLimiting = true,
                enableRequestValidation = true,
                enableSecurityHeaders = true,
                tokenExpirationHours = 24,
                maxRetries = 3,
                retryDelayMs = 1000
            )
            
            val rateLimitSettings = RateLimitSettings(
                windowMs = 60000, // 1 minute
                maxRequests = 20,
                skipSuccessfulRequests = false
            )
            
            return SecureConfig(
                environment = environment,
                apiEndpoints = apiEndpoints,
                securitySettings = securitySettings,
                rateLimitSettings = rateLimitSettings
            )
        }
    }
}

@Serializable
data class ApiEndpoints(
    val chatApi: String,
    val authApi: String,
    val sessionValidation: String,
    val rateLimitStatus: String
)

@Serializable
data class SecuritySettings(
    val enableAuthentication: Boolean,
    val enableRateLimiting: Boolean,
    val enableRequestValidation: Boolean,
    val enableSecurityHeaders: Boolean,
    val tokenExpirationHours: Int,
    val maxRetries: Int,
    val retryDelayMs: Int
)

@Serializable
data class RateLimitSettings(
    val windowMs: Long,
    val maxRequests: Int,
    val skipSuccessfulRequests: Boolean
)

/**
 * Runtime secure storage interface
 */
interface SecureStorage {
    suspend fun storeAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun clearAuthToken()
    suspend fun storeUserId(userId: String)
    suspend fun getUserId(): String?
    suspend fun clearUserId()
    suspend fun storeSessionData(sessionData: SessionData)
    suspend fun getSessionData(): SessionData?
    suspend fun clearSessionData()
}

@Serializable
data class SessionData(
    val userId: String,
    val token: String,
    val expiresAt: Long,
    val refreshToken: String? = null
)

/**
 * Security utilities
 */
object SecurityUtils {
    fun isValidToken(token: String): Boolean {
        return token.isNotEmpty() && token.length >= 20 && token.startsWith("eyJ")
    }
    
    fun isExpired(expiresAt: Long): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .trim()
            .replace(Regex("<script[^>]*>.*?</script>"), "")
            .replace(Regex("javascript:"), "")
            .substring(0, 1000) // Max length
    }
    
    fun generateSecureHeaders(): Map<String, String> {
        return mapOf(
            "Content-Type" to "application/json",
            "User-Agent" to "DrMindit-Android/${BuildConfig.VERSION_NAME}",
            "X-API-Version" to (try { BuildConfig.API_VERSION } catch(e: Exception) { "v1" }),
            "X-Platform" to "android",
            "X-App-Version" to BuildConfig.VERSION_NAME
        )
    }
    
    fun createRateLimitKey(userId: String): String {
        return "rate_limit_${userId}_${System.currentTimeMillis() / 60000}" // Per user per minute
    }
}
