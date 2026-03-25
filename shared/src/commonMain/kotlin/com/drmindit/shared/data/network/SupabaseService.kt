package com.drmindit.shared.data.network

import com.drmindit.shared.data.config.EnvironmentConfig
import io.ktor.client.*
import kotlinx.coroutines.flow.Flow

class SupabaseService(
    private val apiClient: ApiClient = ApiClient(
        baseUrl = EnvironmentConfig.getBaseUrl(),
        apiKey = EnvironmentConfig.getAnonKey()
    )
) {
    
    // Authentication endpoints
    suspend fun signIn(email: String, password: String) = apiClient.post(
        path = "/auth/v1/token?grant_type=password",
        body = mapOf(
            "email" to email,
            "password" to password
        )
    )

    suspend fun signUp(email: String, password: String, metadata: Map<String, String> = emptyMap()) = apiClient.post(
        path = "/auth/v1/signup",
        body = mapOf(
            "email" to email,
            "password" to password,
            "data" to metadata
        )
    )

    suspend fun signOut() = apiClient.post(
        path = "/auth/v1/logout"
    )

    suspend fun refreshToken(refreshToken: String) = apiClient.post(
        path = "/auth/v1/token?grant_type=refresh_token",
        body = mapOf(
            "refresh_token" to refreshToken
        )
    )

    // Users table endpoints
    suspend fun getUserProfile(userId: String) = apiClient.get(
        path = "/rest/v1/profiles",
        parameters = mapOf(
            "id" to "eq.$userId",
            "select" to "*"
        )
    )

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) = apiClient.patch(
        path = "/rest/v1/profiles",
        body = updates
    )

    suspend fun createUserProfile(profile: Map<String, Any>) = apiClient.post(
        path = "/rest/v1/profiles",
        body = profile
    )

    // Sessions table endpoints
    suspend fun getSessions(
        category: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ) = apiClient.get(
        path = "/rest/v1/sessions",
        parameters = buildMap {
            put("select", "*")
            put("is_active", "eq.true")
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("order", "rating.desc")
            category?.let { put("category", "eq.$it") }
        }
    )

    suspend fun getSessionById(sessionId: String) = apiClient.get(
        path = "/rest/v1/sessions",
        parameters = mapOf(
            "id" to "eq.$sessionId",
            "is_active" to "eq.true",
            "select" to "*"
        )
    )

    suspend fun searchSessions(query: String, limit: Int = 20) = apiClient.get(
        path = "/rest/v1/sessions",
        parameters = mapOf(
            "select" to "*",
            "is_active" to "eq.true",
            "or" to "title.ilike.*$query*,description.ilike.*$query*,instructor.ilike.*$query*",
            "limit" to limit.toString(),
            "order" to "rating.desc"
        )
    )

    suspend fun getSessionOfTheDay() = apiClient.get(
        path = "/rest/v1/sessions",
        parameters = mapOf(
            "select" to "*",
            "is_active" to "eq.true",
            "rating" to "gte.4.0",
            "limit" to "1",
            "order" to "random()"
        )
    )

    // Programs table endpoints
    suspend fun getPrograms(
        category: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ) = apiClient.get(
        path = "/rest/v1/programs",
        parameters = buildMap {
            put("select", "*")
            put("is_active", "eq.true")
            put("limit", limit.toString())
            put("offset", offset.toString())
            put("order", "rating.desc")
            category?.let { put("category", "eq.$it") }
        }
    )

    suspend fun getProgramById(programId: String) = apiClient.get(
        path = "/rest/v1/programs",
        parameters = mapOf(
            "id" to "eq.$programId",
            "is_active" to "eq.true",
            "select" to "*"
        )
    )

    suspend fun getProgramSessions(programId: String) = apiClient.get(
        path = "/rest/v1/program_sessions",
        parameters = mapOf(
            "program_id" to "eq.$programId",
            "select" to "*,sessions(*)",
            "order" to "day_number.asc"
        )
    )

    // User sessions endpoints
    suspend fun getUserSessions(userId: String, limit: Int = 50) = apiClient.get(
        path = "/rest/v1/user_sessions",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "select" to "*,sessions(*)",
            "order" to "last_played_at.desc",
            "limit" to limit.toString()
        )
    )

    suspend fun updateSessionProgress(userId: String, sessionId: String, progress: Map<String, Any>) = apiClient.post(
        path = "/rest/v1/user_sessions",
        body = mapOf(
            "user_id" to userId,
            "session_id" to sessionId,
            *progress.toList().toTypedArray()
        )
    )

    suspend fun toggleSessionFavorite(userId: String, sessionId: String, isFavorite: Boolean) = apiClient.patch(
        path = "/rest/v1/user_sessions",
        body = mapOf(
            "is_favorite" to isFavorite
        ),
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "session_id" to "eq.$sessionId"
        )
    )

    suspend fun getFavoriteSessions(userId: String) = apiClient.get(
        path = "/rest/v1/user_sessions",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "is_favorite" to "eq.true",
            "select" to "*,sessions(*)",
            "order" to "last_played_at.desc"
        )
    )

    // User programs endpoints
    suspend fun getUserPrograms(userId: String) = apiClient.get(
        path = "/rest/v1/user_programs",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "select" to "*,programs(*)",
            "order" to "last_active_at.desc"
        )
    )

    suspend fun startProgram(userId: String, programId: String, totalDays: Int) = apiClient.post(
        path = "/rest/v1/user_programs",
        body = mapOf(
            "user_id" to userId,
            "program_id" to programId,
            "current_day" to 1,
            "completed_days" to 0,
            "total_days" to totalDays,
            "is_completed" to false,
            "completion_percentage" to 0.0
        )
    )

    suspend fun updateProgramProgress(userId: String, programId: String, updates: Map<String, Any>) = apiClient.patch(
        path = "/rest/v1/user_programs",
        body = updates,
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "program_id" to "eq.$programId"
        )
    )

    // Mood logs endpoints
    suspend fun createMoodLog(userId: String, moodLog: Map<String, Any>) = apiClient.post(
        path = "/rest/v1/mood_logs",
        body = mapOf(
            "user_id" to userId,
            *moodLog.toList().toTypedArray()
        )
    )

    suspend fun getMoodLogs(userId: String, limit: Int = 30) = apiClient.get(
        path = "/rest/v1/mood_logs",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "order" to "created_at.desc",
            "limit" to limit.toString()
        )
    )

    // Analytics endpoints
    suspend fun getUserAnalytics(userId: String) = apiClient.get(
        path = "/rest/v1/user_analytics",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "order" to "week_start.desc",
            "limit" to "1"
        )
    )

    suspend fun updateUserAnalytics(userId: String, analytics: Map<String, Any>) = apiClient.post(
        path = "/rest/v1/user_analytics",
        body = mapOf(
            "user_id" to userId,
            *analytics.toList().toTypedArray()
        )
    )

    // Emergency helplines endpoints
    suspend fun getEmergencyHelplines(country: String = "IN") = apiClient.get(
        path = "/rest/v1/emergency_helplines",
        parameters = mapOf(
            "country" to "eq.$country",
            "is_active" to "eq.true",
            "order" to "priority.asc"
        )
    )

    // Crisis events endpoints
    suspend fun logCrisisEvent(userId: String, crisisEvent: Map<String, Any>) = apiClient.post(
        path = "/rest/v1/crisis_events",
        body = mapOf(
            "user_id" to userId,
            *crisisEvent.toList().toTypedArray()
        )
    )

    suspend fun getUserCrisisHistory(userId: String, limit: Int = 10) = apiClient.get(
        path = "/rest/v1/crisis_events",
        parameters = mapOf(
            "user_id" to "eq.$userId",
            "order" to "created_at.desc",
            "limit" to limit.toString()
        )
    )

    // RPC endpoints for complex operations
    suspend fun updateUserRiskStatus(userId: String) = apiClient.post(
        path = "/rest/v1/rpc/update_user_risk_status",
        body = mapOf("user_uuid" to userId)
    )
}
