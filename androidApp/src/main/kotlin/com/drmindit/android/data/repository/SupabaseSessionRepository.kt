package com.drmindit.android.data.repository

import com.drmindit.android.data.supabase.SupabaseClient
import com.drmindit.android.domain.model.Session
import com.drmindit.android.domain.model.SessionStep
import com.drmindit.android.domain.model.UserSessionProgress
import com.drmindit.android.domain.repository.SessionRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of SessionRepository
 * Direct database access without custom backend
 */
class SupabaseSessionRepository : SessionRepository {
    
    override suspend fun getSessions(): Result<List<Session>> {
        return try {
            val database = SupabaseClient.database()
            val result = database
                .from("sessions")
                .select {
                    eq("is_active", true)
                }
                .data
            
            val sessions = result?.map { it.toSession() } ?: emptyList()
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSession(sessionId: String): Result<Session?> {
        return try {
            val database = SupabaseClient.database()
            val result = database
                .from("sessions")
                .select {
                    eq("id", sessionId)
                    eq("is_active", true)
                    single()
                }
                .data
            
            val session = result?.toSession()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSessionSteps(sessionId: String): Result<List<SessionStep>> {
        return try {
            val database = SupabaseClient.database()
            val result = database
                .from("session_steps")
                .select {
                    eq("session_id", sessionId)
                    order("order_index")
                }
                .data
            
            val steps = result?.map { it.toSessionStep() } ?: emptyList()
            Result.success(steps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserSessionProgress(sessionId: String): Result<UserSessionProgress?> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))
            
            val database = SupabaseClient.database()
            val result = database
                .from("user_session_progress")
                .select {
                    eq("user_id", userId)
                    eq("session_id", sessionId)
                    single()
                }
                .data
            
            val progress = result?.toUserSessionProgress()
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSessionProgress(progress: UserSessionProgress): Result<UserSessionProgress> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))
            
            val database = SupabaseClient.database()
            val result = database
                .from("user_session_progress")
                .upsert(
                    progress.toSupabaseMap().apply {
                        put("user_id", userId)
                    }
                )
                .select()
                .single()
                .data
            
            val updatedProgress = result?.toUserSessionProgress()
            Result.success(updatedProgress ?: progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun startSession(sessionId: String): Result<UserSessionProgress> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))
            
            val progress = UserSessionProgress(
                id = "",
                userId = userId,
                sessionId = sessionId,
                currentStepIndex = 0,
                progressSeconds = 0,
                completedSteps = emptyList(),
                isCompleted = false,
                startedAt = System.currentTimeMillis(),
                completedAt = null,
                updatedAt = System.currentTimeMillis()
            )
            
            updateSessionProgress(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSessionProgressStream(sessionId: String): Flow<UserSessionProgress?> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return flowOf(null)
            
            // In a real implementation, you'd use Supabase realtime subscriptions
            // For now, return a flow that can be refreshed
            flowOf(null)
        } catch (e: Exception) {
            flowOf(null)
        }
    }
}

// Extension functions for data conversion
@Serializable
data class SupabaseSession(
    val id: String,
    val title: String,
    val description: String? = null,
    val total_duration: Int,
    val category: String? = null,
    val difficulty_level: String? = null,
    val thumbnail_url: String? = null,
    val audio_url: String,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class SupabaseSessionStep(
    val id: String,
    val session_id: String,
    val title: String,
    val description: String? = null,
    val audio_url: String,
    val duration: Int,
    val order_index: Int,
    val step_type: String? = "audio",
    val created_at: String? = null
)

@Serializable
data class SupabaseUserSessionProgress(
    val id: String,
    val user_id: String,
    val session_id: String,
    val current_step_index: Int = 0,
    val progress_seconds: Int = 0,
    val completed_steps: List<String> = emptyList(),
    val is_completed: Boolean = false,
    val started_at: String? = null,
    val completed_at: String? = null,
    val updated_at: String? = null
)

fun SupabaseSession.toSession() = Session(
    id = id,
    title = title,
    description = description ?: "",
    duration = total_duration,
    category = category ?: "meditation",
    difficulty = difficulty_level ?: "beginner",
    thumbnailUrl = thumbnail_url ?: "",
    audioUrl = audio_url,
    isActive = is_active
)

fun SupabaseSessionStep.toSessionStep() = SessionStep(
    id = id,
    sessionId = session_id,
    title = title,
    description = description ?: "",
    audioUrl = audio_url,
    duration = duration,
    orderIndex = order_index,
    stepType = step_type ?: "audio"
)

fun SupabaseUserSessionProgress.toUserSessionProgress() = UserSessionProgress(
    id = id,
    userId = user_id,
    sessionId = session_id,
    currentStepIndex = current_step_index,
    progressSeconds = progress_seconds,
    completedSteps = completed_steps,
    isCompleted = is_completed,
    startedAt = started_at?.let { /* parse timestamp */ } ?: System.currentTimeMillis(),
    completedAt = completed_at?.let { /* parse timestamp */ },
    updatedAt = updated_at?.let { /* parse timestamp */ } ?: System.currentTimeMillis()
)

fun UserSessionProgress.toSupabaseMap() = mapOf(
    "id" to id,
    "session_id" to sessionId,
    "current_step_index" to currentStepIndex,
    "progress_seconds" to progressSeconds,
    "completed_steps" to completedSteps,
    "is_completed" to isCompleted,
    "started_at" to startedAt.let { /* format timestamp */ },
    "completed_at" to completedAt?.let { /* format timestamp */ },
    "updated_at" to updatedAt.let { /* format timestamp */ }
)
