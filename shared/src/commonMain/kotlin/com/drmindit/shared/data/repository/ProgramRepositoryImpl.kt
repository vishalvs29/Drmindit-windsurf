package com.drmindit.shared.data.repository

import com.drmindit.shared.data.network.ApiException
import com.drmindit.shared.data.network.SupabaseService
import com.drmindit.shared.domain.model.Program
import com.drmindit.shared.domain.model.ProgramCategory
import com.drmindit.shared.domain.model.ProgramSession
import com.drmindit.shared.domain.model.ProgramProgress
import com.drmindit.shared.domain.model.Difficulty
import com.drmindit.shared.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class ProgramRepositoryImpl(
    private val supabaseService: SupabaseService = SupabaseService()
) : ProgramRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun getPrograms(category: ProgramCategory?): Result<List<Program>> {
        return try {
            val response = if (category != null) {
                supabaseService.getPrograms(category = category.name.lowercase())
            } else {
                supabaseService.getPrograms()
            }
            
            val programs = response.body as? List<Map<String, Any>>
            val programList = programs?.map { mapProgramToProgram(it) } ?: emptyList()
            
            Result.success(programList)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getProgramById(programId: String): Result<Program?> {
        return try {
            val response = supabaseService.getProgramById(programId)
            val programs = response.body as? List<Map<String, Any>>
            
            if (programs?.isNotEmpty() == true) {
                val program = mapProgramToProgram(programs.first())
                Result.success(program)
            } else {
                Result.success(null)
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getProgramSessions(programId: String): Result<List<ProgramSession>> {
        return try {
            val response = supabaseService.getProgramSessions(programId)
            val programSessions = response.body as? List<Map<String, Any>>
            
            val sessions = programSessions?.mapNotNull { programSessionData ->
                val sessionData = programSessionData["sessions"] as? Map<String, Any>
                if (sessionData != null) {
                    ProgramSession(
                        id = programSessionData["id"] as String,
                        dayNumber = programSessionData["day_number"] as Int,
                        sessionId = programSessionData["session_id"] as String,
                        sessionTitle = sessionData["title"] as String,
                        sessionDescription = sessionData["description"] as? String ?: "",
                        sessionDuration = sessionData["duration_minutes"] as Int,
                        sessionInstructor = sessionData["instructor"] as String,
                        sessionAudioUrl = sessionData["audio_url"] as String,
                        sessionImageUrl = sessionData["image_url"] as? String,
                        isUnlocked = programSessionData["is_unlocked"] as? Boolean ?: false,
                        isCompleted = false // Would be tracked in user_programs
                    )
                } else null
            } ?: emptyList()
            
            Result.success(sessions)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun startProgram(userId: String, programId: String): Result<ProgramProgress> {
        return try {
            // First get program details to know total days
            val programResponse = supabaseService.getProgramById(programId)
            val programs = programResponse.body as? List<Map<String, Any>>
            
            if (programs?.isNotEmpty() == true) {
                val program = programs.first()
                val totalDays = program["duration_days"] as Int
                
                // Start the program
                supabaseService.startProgram(userId, programId, totalDays)
                
                val programProgress = ProgramProgress(
                    userId = userId,
                    programId = programId,
                    currentDay = 1,
                    completedDays = 0,
                    totalDays = totalDays,
                    isCompleted = false,
                    completionPercentage = 0.0f,
                    startedAt = Clock.System.now().toString(),
                    completedAt = null,
                    lastActiveAt = Clock.System.now().toString()
                )
                
                Result.success(programProgress)
            } else {
                Result.failure(ApiException.NotFound("Program not found"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun updateProgramProgress(progress: ProgramProgress): Result<ProgramProgress> {
        return try {
            val updates = mapOf(
                "current_day" to progress.currentDay,
                "completed_days" to progress.completedDays,
                "is_completed" to progress.isCompleted,
                "completion_percentage" to progress.completionPercentage,
                "last_active_at" to Clock.System.now().toString(),
                "completed_at" to progress.completedAt
            )
            
            supabaseService.updateProgramProgress(progress.userId, progress.programId, updates)
            Result.success(progress)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getUserPrograms(userId: String): Result<List<ProgramProgress>> {
        return try {
            val response = supabaseService.getUserPrograms(userId)
            val userPrograms = response.body as? List<Map<String, Any>>
            
            val programProgressList = userPrograms?.map { mapUserProgramToProgress(it) } ?: emptyList()
            
            Result.success(programProgressList)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override suspend fun getProgramProgress(programId: String, userId: String): Result<ProgramProgress?> {
        return try {
            val response = supabaseService.getUserPrograms(userId)
            val userPrograms = response.body as? List<Map<String, Any>>
            
            val programProgress = userPrograms?.find { 
                (it["program_id"] as? String) == programId 
            }?.let { mapUserProgramToProgress(it) }
            
            Result.success(programProgress)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.NetworkError(e.message ?: "Network error"))
        }
    }
    
    override fun observeProgramProgress(programId: String, userId: String): Flow<ProgramProgress?> {
        // In a real implementation, this would use Supabase realtime subscriptions
        return MutableStateFlow<ProgramProgress?>(null).asStateFlow()
    }
    
    private fun mapProgramToProgram(programData: Map<String, Any>): Program {
        val targetAudiences = (programData["target_audiences"] as? List<String>) ?: emptyList()
        val category = try {
            ProgramCategory.valueOf((programData["category"] as? String)?.uppercase() ?: "ANXIETY_RESET")
        } catch (e: IllegalArgumentException) {
            ProgramCategory.ANXIETY_RESET
        }
        
        val difficulty = try {
            Difficulty.valueOf((programData["difficulty"] as? String)?.uppercase() ?: "BEGINNER")
        } catch (e: IllegalArgumentException) {
            Difficulty.BEGINNER
        }
        
        return Program(
            id = programData["id"] as String,
            title = programData["title"] as String,
            description = programData["description"] as? String ?: "",
            imageUrl = programData["image_url"] as? String,
            category = category,
            durationDays = programData["duration_days"] as Int,
            difficulty = difficulty,
            rating = (programData["rating"] as? Double)?.toFloat() ?: 0f,
            totalRatings = programData["total_ratings"] as? Int ?: 0,
            isPremium = programData["is_premium"] as? Boolean ?: false,
            targetAudiences = targetAudiences,
            sessions = emptyList() // Would be loaded separately
        )
    }
    
    private fun mapUserProgramToProgress(userProgramData: Map<String, Any>): ProgramProgress {
        return ProgramProgress(
            userId = userProgramData["user_id"] as String,
            programId = userProgramData["program_id"] as String,
            currentDay = userProgramData["current_day"] as Int,
            completedDays = userProgramData["completed_days"] as Int,
            totalDays = userProgramData["total_days"] as Int,
            isCompleted = userProgramData["is_completed"] as Boolean,
            completionPercentage = (userProgramData["completion_percentage"] as? Double)?.toFloat() ?: 0f,
            startedAt = userProgramData["started_at"] as? String ?: Clock.System.now().toString(),
            completedAt = userProgramData["completed_at"] as? String,
            lastActiveAt = userProgramData["last_active_at"] as? String ?: Clock.System.now().toString()
        )
    }
}
