package com.drmindit.shared.data.repository

import com.drmindit.shared.data.network.ApiException
import com.drmindit.shared.data.network.SupabaseService
import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.model.SessionProgress
import com.drmindit.shared.domain.model.Difficulty
import com.drmindit.shared.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class SessionRepositoryReal(
    private val supabaseService: SupabaseService = SupabaseService()
) : SessionRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun getSessions(category: SessionCategory?): Result<List<Session>> {
        return try {
            val response = if (category != null) {
                supabaseService.getSessions(category = category.name.lowercase())
            } else {
                supabaseService.getSessions()
            }
            
            val sessions = response.body as? List<Map<String, Any>>
            val sessionList = sessions?.map { mapSessionToSession(it) } ?: emptyList()
            
            Result.success(sessionList)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getSessionById(sessionId: String): Result<Session?> {
        return try {
            val response = supabaseService.getSessionById(sessionId)
            val sessions = response.body as? List<Map<String, Any>>
            
            if (sessions?.isNotEmpty() == true) {
                val session = mapSessionToSession(sessions.first())
                Result.success(session)
            } else {
                Result.success(null)
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun searchSessions(query: String): Result<List<Session>> {
        return try {
            val response = supabaseService.searchSessions(query)
            val sessions = response.body as? List<Map<String, Any>>
            val sessionList = sessions?.map { mapSessionToSession(it) } ?: emptyList()
            
            Result.success(sessionList)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getSessionOfTheDay(): Result<Session> {
        return try {
            val response = supabaseService.getSessionOfTheDay()
            val sessions = response.body as? List<Map<String, Any>>
            
            if (sessions?.isNotEmpty() == true) {
                val session = mapSessionToSession(sessions.first())
                Result.success(session)
            } else {
                // Fallback to any session if session of the day is not available
                getSessions().fold(
                    onSuccess = { sessions ->
                        if (sessions.isNotEmpty()) {
                            Result.success(sessions.first())
                        } else {
                            Result.failure(ApiException.NotFound("No sessions available"))
                        }
                    },
                    onFailure = { Result.failure(it) }
                )
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun toggleFavorite(sessionId: String): Result<Unit> {
        return try {
            // This would require the current user ID
            // For now, we'll simulate the toggle
            val userId = "current_user_id" // This should come from auth repository
            
            // First check if session is already favorited
            val favoriteResponse = supabaseService.getFavoriteSessions(userId)
            val favoriteSessions = favoriteResponse.body as? List<Map<String, Any>>
            val isCurrentlyFavorite = favoriteSessions?.any { 
                (it["sessions"] as? Map<String, Any>)?.get("id") == sessionId 
            } ?: false
            
            // Toggle the favorite status
            supabaseService.toggleSessionFavorite(userId, sessionId, !isCurrentlyFavorite)
            
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getFavoriteSessions(): Result<List<Session>> {
        return try {
            val userId = "current_user_id" // This should come from auth repository
            
            val response = supabaseService.getFavoriteSessions(userId)
            val userSessions = response.body as? List<Map<String, Any>>
            
            val sessions = userSessions?.mapNotNull { userSession ->
                val sessionData = userSession["sessions"] as? Map<String, Any>
                sessionData?.let { mapSessionToSession(it) }
            } ?: emptyList()
            
            Result.success(sessions)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun updateSessionProgress(progress: SessionProgress): Result<Unit> {
        return try {
            val progressData = mapOf(
                "progress_seconds" to progress.completedDuration,
                "total_seconds" to progress.totalDuration,
                "is_completed" to progress.isCompleted,
                "last_played_at" to progress.lastPlayedDate,
                "updated_at" to Clock.System.now().toString()
            )
            
            supabaseService.updateSessionProgress(progress.userId, progress.sessionId, progressData)
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getSessionProgress(sessionId: String, userId: String): Result<SessionProgress?> {
        return try {
            val response = supabaseService.getUserSessions(userId)
            val userSessions = response.body as? List<Map<String, Any>>
            
            val sessionProgress = userSessions?.find { 
                (it["session_id"] as? String) == sessionId 
            }?.let { mapUserSessionToProgress(it) }
            
            Result.success(sessionProgress)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override fun observeSessionProgress(sessionId: String, userId: String): Flow<SessionProgress?> {
        // In a real implementation, this would use Supabase realtime subscriptions
        // For now, return a flow that can be updated manually
        return MutableStateFlow<SessionProgress?>(null).asStateFlow()
    }
    
    private fun mapSessionToSession(sessionData: Map<String, Any>): Session {
        val tags = (sessionData["tags"] as? List<String>) ?: emptyList()
        val category = try {
            SessionCategory.valueOf((sessionData["category"] as? String)?.uppercase() ?: "MINDFULNESS")
        } catch (e: IllegalArgumentException) {
            SessionCategory.MINDFULNESS
        }
        
        val difficulty = try {
            Difficulty.valueOf((sessionData["difficulty"] as? String)?.uppercase() ?: "BEGINNER")
        } catch (e: IllegalArgumentException) {
            Difficulty.BEGINNER
        }
        
        return Session(
            id = sessionData["id"] as String,
            title = sessionData["title"] as String,
            description = sessionData["description"] as? String ?: "",
            instructor = sessionData["instructor"] as String,
            duration = sessionData["duration_minutes"] as Int,
            audioUrl = sessionData["audio_url"] as String,
            imageUrl = sessionData["image_url"] as? String,
            category = category,
            tags = tags,
            rating = (sessionData["rating"] as? Double)?.toFloat() ?: 0f,
            totalRatings = sessionData["total_ratings"] as? Int ?: 0,
            isPremium = sessionData["is_premium"] as? Boolean ?: false,
            isDownloaded = false, // Would be tracked separately
            isFavorite = false, // Would be fetched from user_sessions
            difficulty = difficulty,
            language = sessionData["language"] as? String ?: "en",
            transcript = sessionData["transcript"] as? String
        )
    }
    
    private fun mapUserSessionToProgress(userSessionData: Map<String, Any>): SessionProgress {
        return SessionProgress(
            sessionId = userSessionData["session_id"] as String,
            userId = userSessionData["user_id"] as String,
            completedDuration = userSessionData["progress_seconds"] as Int,
            totalDuration = userSessionData["total_seconds"] as Int,
            isCompleted = userSessionData["is_completed"] as Boolean,
            lastPlayedDate = userSessionData["last_played_at"] as? String ?: Clock.System.now().toString(),
            playbackSpeed = 1.0f // Default playback speed
        )
    }
}
