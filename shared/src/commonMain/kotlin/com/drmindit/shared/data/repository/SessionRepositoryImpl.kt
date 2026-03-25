package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.model.SessionProgress
import com.drmindit.shared.domain.model.Difficulty
import com.drmindit.shared.domain.repository.SessionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class SessionRow(
    val id: String,
    val title: String,
    val description: String? = null,
    val instructor: String,
    val duration_minutes: Int,
    val audio_url: String,
    val image_url: String? = null,
    val category: String,
    val tags: List<String> = emptyList(),
    val difficulty: String,
    val rating: Double = 0.0,
    val total_ratings: Int = 0,
    val is_premium: Boolean = false,
    val language: String = "en",
    val transcript: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class UserSessionRow(
    val id: String? = null,
    val user_id: String,
    val session_id: String,
    val program_id: String? = null,
    val progress_seconds: Int = 0,
    val total_seconds: Int,
    val is_completed: Boolean = false,
    val is_favorite: Boolean = false,
    val last_played_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

class SessionRepositoryImpl(
    private val supabase: SupabaseClient = SupabaseClient.client,
    private val database: Postgrest = supabase.database
) : SessionRepository {
    
    override suspend fun getSessions(category: SessionCategory?): Result<List<Session>> {
        return try {
            val sessions = if (category != null) {
                database.from("sessions")
                    .select {
                        SessionRow::id
                        SessionRow::title
                        SessionRow::description
                        SessionRow::instructor
                        SessionRow::duration_minutes
                        SessionRow::audio_url
                        SessionRow::image_url
                        SessionRow::category
                        SessionRow::tags
                        SessionRow::difficulty
                        SessionRow::rating
                        SessionRow::total_ratings
                        SessionRow::is_premium
                        SessionRow::language
                        SessionRow::transcript
                    }
                    .eq("is_active", true)
                    .eq("category", category.name.lowercase())
                    .order("rating", ascending = false)
                    .data
            } else {
                database.from("sessions")
                    .select {
                        SessionRow::id
                        SessionRow::title
                        SessionRow::description
                        SessionRow::instructor
                        SessionRow::duration_minutes
                        SessionRow::audio_url
                        SessionRow::image_url
                        SessionRow::category
                        SessionRow::tags
                        SessionRow::difficulty
                        SessionRow::rating
                        SessionRow::total_ratings
                        SessionRow::is_premium
                        SessionRow::language
                        SessionRow::transcript
                    }
                    .eq("is_active", true)
                    .order("rating", ascending = false)
                    .data
            }
            
            Result.success(
                sessions?.map { mapSessionRowToSession(it) } ?: emptyList()
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSessionById(sessionId: String): Result<Session?> {
        return try {
            val session = database.from("sessions")
                .select()
                .eq("id", sessionId)
                .eq("is_active", true)
                .single()
                .data
                ?.let { mapSessionRowToSession(it) }
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchSessions(query: String): Result<List<Session>> {
        return try {
            val searchQuery = "%$query%"
            
            val sessions = database.from("sessions")
                .select()
                .eq("is_active", true)
                .or {
                    SessionRow::title like searchQuery
                    SessionRow::description like searchQuery
                    SessionRow::instructor like searchQuery
                    SessionRow::tags cs listOf(query)
                }
                .order("rating", ascending = false)
                .data
                ?.map { mapSessionRowToSession(it) }
                ?: emptyList()
            
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSessionOfTheDay(): Result<Session> {
        return try {
            // Get a random session as session of the day
            // In production, this could be based on user preferences, date, etc.
            val sessions = database.from("sessions")
                .select()
                .eq("is_active", true)
                .gte("rating", 4.0)
                .data
                ?.map { mapSessionRowToSession(it) }
                ?: emptyList()
            
            if (sessions.isEmpty()) {
                return Result.failure(Exception("No sessions available"))
            }
            
            val sessionOfTheDay = sessions[Random.nextInt(sessions.size)]
            Result.success(sessionOfTheDay)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleFavorite(sessionId: String): Result<Unit> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Check if user session exists
            val existingUserSession = database.from("user_sessions")
                .select()
                .eq("user_id", currentUser.id)
                .eq("session_id", sessionId)
                .single()
                .data
            
            if (existingUserSession != null) {
                // Update existing record
                database.from("user_sessions")
                    .update(
                        mapOf(
                            "is_favorite" to !(existingUserSession["is_favorite"] as Boolean)
                        )
                    )
                    .eq("user_id", currentUser.id)
                    .eq("session_id", sessionId)
            } else {
                // Create new record
                val session = getSessionById(sessionId).getOrNull()
                    ?: return Result.failure(Exception("Session not found"))
                
                val userSession = UserSessionRow(
                    user_id = currentUser.id,
                    session_id = sessionId,
                    total_seconds = session.duration * 60,
                    is_favorite = true
                )
                
                database.from("user_sessions").insert(userSession)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFavoriteSessions(): Result<List<Session>> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull
                ?: return Result.success(emptyList())
            
            val favoriteSessions = database.from("user_sessions")
                .select {
                    UserSessionRow::session_id
                }
                .eq("user_id", currentUser.id)
                .eq("is_favorite", true)
                .data
                ?.mapNotNull { it["session_id"] as? String }
                ?: emptyList()
            
            // Get full session details
            val sessions = if (favoriteSessions.isNotEmpty()) {
                database.from("sessions")
                    .select()
                    .inList("id", favoriteSessions)
                    .eq("is_active", true)
                    .data
                    ?.map { mapSessionRowToSession(it) }
                    ?: emptyList()
            } else {
                emptyList()
            }
            
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSessionProgress(progress: SessionProgress): Result<Unit> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Check if user session exists
            val existingUserSession = database.from("user_sessions")
                .select()
                .eq("user_id", currentUser.id)
                .eq("session_id", progress.sessionId)
                .single()
                .data
            
            if (existingUserSession != null) {
                // Update existing record
                database.from("user_sessions")
                    .update(
                        mapOf(
                            "progress_seconds" to progress.completedDuration,
                            "is_completed" to progress.isCompleted,
                            "last_played_at" to progress.lastPlayedDate
                        )
                    )
                    .eq("user_id", currentUser.id)
                    .eq("session_id", progress.sessionId)
            } else {
                // Create new record
                val userSession = UserSessionRow(
                    user_id = currentUser.id,
                    session_id = progress.sessionId,
                    progress_seconds = progress.completedDuration,
                    total_seconds = progress.totalDuration,
                    is_completed = progress.isCompleted,
                    last_played_at = progress.lastPlayedDate
                )
                
                database.from("user_sessions").insert(userSession)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSessionProgress(sessionId: String, userId: String): Result<SessionProgress?> {
        return try {
            val userSession = database.from("user_sessions")
                .select()
                .eq("user_id", userId)
                .eq("session_id", sessionId)
                .single()
                .data
                ?.let { mapUserSessionRowToProgress(it) }
            
            Result.success(userSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeSessionProgress(sessionId: String, userId: String): Flow<SessionProgress?> {
        // In a real implementation, you would use Supabase realtime subscriptions
        // For now, return a flow that can be updated manually
        return kotlinx.coroutines.flow.MutableStateFlow<SessionProgress?>(null)
    }
    
    private fun mapSessionRowToSession(row: SessionRow): Session {
        return Session(
            id = row.id,
            title = row.title,
            description = row.description ?: "",
            instructor = row.instructor,
            duration = row.duration_minutes,
            audioUrl = row.audio_url,
            imageUrl = row.image_url,
            category = SessionCategory.valueOf(row.category.uppercase()),
            tags = row.tags,
            rating = row.rating.toFloat(),
            totalRatings = row.total_ratings,
            isPremium = row.is_premium,
            isDownloaded = false, // Would be tracked separately
            isFavorite = false, // Would be fetched from user_sessions
            difficulty = Difficulty.valueOf(row.difficulty.uppercase()),
            language = row.language,
            transcript = row.transcript
        )
    }
    
    private fun mapUserSessionRowToProgress(row: Map<String, Any>): SessionProgress {
        return SessionProgress(
            sessionId = row["session_id"] as String,
            userId = row["user_id"] as String,
            completedDuration = row["progress_seconds"] as Int,
            totalDuration = row["total_seconds"] as Int,
            isCompleted = row["is_completed"] as Boolean,
            lastPlayedDate = row["last_played_at"] as? String ?: "",
            playbackSpeed = 1.0f
        )
    }
}
