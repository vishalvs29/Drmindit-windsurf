package com.drmindit.shared.data.network

import com.drmindit.shared.data.config.SecureConfig
import com.drmindit.shared.data.config.SecurityUtils
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.call.bodyText
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Secure API Client
 * 
 * SECURITY FEATURES:
 * - No API keys in client code
 * - All calls go through secure backend proxy
 * - Authentication tokens stored securely
 * - Rate limiting handled by backend
 * - Input validation and sanitization
 * - Request/response logging (no sensitive data)
 */
class SecureApiClient(
    private val httpClient: HttpClient,
    private val secureStorage: SecureStorage,
    private val config: SecureConfig
) {
    
    /**
     * Send chat message through secure backend proxy
     */
    suspend fun sendChatMessage(
        message: String,
        userId: String,
        sessionId: String? = null
    ): Result<ChatResponse> {
        return try {
            // Security: Validate and sanitize input
            val sanitizedMessage = SecurityUtils.sanitizeInput(message)
            if (sanitizedMessage.isEmpty()) {
                return Result.failure(IllegalArgumentException("Message cannot be empty"))
            }
            
            // Get auth token
            val authToken = secureStorage.getAuthToken()
            if (authToken == null || !SecurityUtils.isValidToken(authToken)) {
                return Result.failure(IllegalStateException("User not authenticated"))
            }
            
            // Prepare secure request
            val chatRequest = ChatRequest(
                message = sanitizedMessage,
                userId = userId,
                sessionId = sessionId
            )
            
            val response = httpClient.post("${config.apiEndpoints.chatApi}") {
                headers {
                    append("Authorization", "Bearer $authToken")
                    appendAll(SecurityUtils.generateSecureHeaders())
                }
                setBody(chatRequest)
                contentType(ContentType.Application.Json)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val chatResponse = response.body<ChatResponse>()
                    if (chatResponse != null) {
                        Result.success(chatResponse)
                    } else {
                        Result.failure(IllegalStateException("Invalid response format"))
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    // Clear invalid token
                    secureStorage.clearAuthToken()
                    Result.failure(IllegalStateException("Authentication failed"))
                }
                HttpStatusCode.TooManyRequests -> {
                    Result.failure(RateLimitException("Rate limit exceeded"))
                }
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(ApiException(
                        code = response.status.value,
                        message = errorBody?.message ?: "Unknown error",
                        details = errorBody
                    ))
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Authenticate user with secure backend
     */
    suspend fun authenticate(
        email: String,
        password: String
    ): Result<AuthResponse> {
        return try {
            // Security: Validate input
            if (email.isBlank() || password.length < 8) {
                return Result.failure(IllegalArgumentException("Invalid email or password"))
            }
            
            val authRequest = AuthRequest(
                email = email.trim().lowercase(),
                password = password
            )
            
            val response = httpClient.post("${config.apiEndpoints.authApi}") {
                headers {
                    appendAll(SecurityUtils.generateSecureHeaders())
                }
                setBody(authRequest)
                contentType(ContentType.Application.Json)
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse = response.body<AuthResponse>()
                    if (authResponse != null && authResponse.success) {
                        // Store auth token securely
                        authResponse.token?.let { token ->
                            secureStorage.storeAuthToken(token)
                        }
                        authResponse.user?.id?.let { userId ->
                            secureStorage.storeUserId(userId)
                        }
                        
                        Result.success(authResponse)
                    } else {
                        Result.failure(IllegalStateException("Invalid authentication response"))
                    }
                }
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(ApiException(
                        code = response.status.value,
                        message = errorBody?.message ?: "Authentication failed",
                        details = errorBody
                    ))
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate user session
     */
    suspend fun validateSession(): Result<SessionValidationResponse> {
        return try {
            val authToken = secureStorage.getAuthToken()
            val userId = secureStorage.getUserId()
            
            if (authToken == null || userId == null) {
                return Result.failure(IllegalStateException("No active session"))
            }
            
            if (!SecurityUtils.isValidToken(authToken)) {
                return Result.failure(IllegalStateException("Invalid session token"))
            }
            
            val response = httpClient.get("${config.apiEndpoints.sessionValidation}") {
                headers {
                    append("Authorization", "Bearer $authToken")
                    appendAll(SecurityUtils.generateSecureHeaders())
                }
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val validationResponse = response.body<SessionValidationResponse>()
                    if (validationResponse != null && validationResponse.valid) {
                        Result.success(validationResponse)
                    } else {
                        // Clear invalid session
                        secureStorage.clearAuthToken()
                        secureStorage.clearUserId()
                        Result.failure(IllegalStateException("Invalid session"))
                    }
                }
                HttpStatusCode.Unauthorized -> {
                    // Clear invalid session
                    secureStorage.clearAuthToken()
                    secureStorage.clearUserId()
                    Result.failure(IllegalStateException("Session expired"))
                }
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(ApiException(
                        code = response.status.value,
                        message = errorBody?.message ?: "Session validation failed",
                        details = errorBody
                    ))
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get rate limit status
     */
    suspend fun getRateLimitStatus(): Result<RateLimitStatusResponse> {
        return try {
            val response = httpClient.get("${config.apiEndpoints.rateLimitStatus}") {
                headers {
                    appendAll(SecurityUtils.generateSecureHeaders())
                }
            }
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    val rateLimitResponse = response.body<RateLimitStatusResponse>()
                    if (rateLimitResponse != null) {
                        Result.success(rateLimitResponse)
                    } else {
                        Result.failure(IllegalStateException("Invalid rate limit response"))
                    }
                }
                else -> {
                    val errorBody = response.body<ErrorResponse>()
                    Result.failure(ApiException(
                        code = response.status.value,
                        message = errorBody?.message ?: "Failed to get rate limit status",
                        details = errorBody
                    ))
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            // Clear all stored sensitive data
            secureStorage.clearAuthToken()
            secureStorage.clearUserId()
            secureStorage.clearSessionData()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Monitor API health
     */
    fun monitorApiHealth(): Flow<ApiHealthStatus> = flow {
        while (true) {
            try {
                val response = httpClient.get("${config.apiEndpoints.chatApi}/health")
                val isHealthy = response.status == HttpStatusCode.OK
                
                emit(ApiHealthStatus(
                    isHealthy = isHealthy,
                    timestamp = System.currentTimeMillis(),
                    endpoint = config.apiEndpoints.chatApi,
                    responseTime = 0 // TODO: Implement response time tracking
                ))
                
            } catch (e: Exception) {
                emit(ApiHealthStatus(
                    isHealthy = false,
                    timestamp = System.currentTimeMillis(),
                    endpoint = config.apiEndpoints.chatApi,
                    error = e.message
                ))
            }
            
            kotlinx.coroutines.delay(30000) // Check every 30 seconds
        }
    }
}

// Request/Response data classes
@Serializable
data class ChatRequest(
    val message: String,
    val userId: String,
    val sessionId: String? = null
)

@Serializable
data class ChatResponse(
    val success: Boolean,
    val response: String,
    val usage: TokenUsage? = null,
    val sessionId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserInfo? = null,
    val expiresAt: Long? = null,
    val message: String? = null
)

@Serializable
data class UserInfo(
    val id: String,
    val email: String,
    val name: String? = null
)

@Serializable
data class SessionValidationResponse(
    val valid: Boolean,
    val user: UserInfo? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

@Serializable
data class RateLimitStatusResponse(
    val windowMs: Long,
    val maxRequests: Int,
    val enabled: Boolean,
    val currentRequests: Int? = null,
    val resetTime: Long? = null
)

@Serializable
data class ErrorResponse(
    val error: String? = null,
    val message: String,
    val code: Int? = null,
    val details: Map<String, Any>? = null
)

@Serializable
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

@Serializable
data class ApiHealthStatus(
    val isHealthy: Boolean,
    val timestamp: Long,
    val endpoint: String,
    val responseTime: Long = 0,
    val error: String? = null
)

// Custom exceptions
class RateLimitException(message: String) : Exception(message)
class ApiException(
    val code: Int,
    message: String,
    details: ErrorResponse? = null
) : Exception("$code: $message")
