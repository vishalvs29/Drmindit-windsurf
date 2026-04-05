package com.drmindit.android.data.repository

import com.drmindit.shared.domain.model.*
import com.drmindit.shared.data.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient

/**
 * Content Management Repository
 * Handles meditation sessions, programs, and audio content
 */
@Singleton
class ContentManagementRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val audioStreamingService: AudioStreamingService,
    private val localCacheService: LocalCacheService
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Get all meditation sessions with dynamic content loading
     */
    fun getAllSessions(): Flow<Result<List<MeditationSession>>> = flow {
        try {
            val response = supabaseClient.from("meditation_sessions")
                .select("*")
                .order("created_at", ascending = false)
                .execute()
            
            val sessions = response.data.mapNotNull { sessionData ->
                parseSession(sessionData)
            }
            
            emit(Result.success(sessions))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get session by ID with audio streaming info
     */
    fun getSessionById(sessionId: String): Flow<Result<MeditationSession>> = flow {
        try {
            // Try cache first
            val cachedSession = localCacheService.getSession(sessionId)
            if (cachedSession != null) {
                emit(Result.success(cachedSession))
                return@flow
            }
            
            // Fetch from Supabase
            val response = supabaseClient.from("meditation_sessions")
                .select("*")
                .eq("id", sessionId)
                .single()
                .execute()
            
            val session = parseSession(response.data) ?: throw Exception("Session not found")
            
            // Cache the session
            localCacheService.cacheSession(session)
            
            emit(Result.success(session))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get sessions by category
     */
    fun getSessionsByCategory(category: SessionCategory): Flow<Result<List<MeditationSession>>> = flow {
        try {
            val response = supabaseClient.from("meditation_sessions")
                .select("*")
                .eq("category", category.name)
                .order("created_at", ascending = false)
                .execute()
            
            val sessions = response.data.mapNotNull { sessionData ->
                parseSession(sessionData)
            }
            
            emit(Result.success(sessions))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get all meditation programs
     */
    fun getAllPrograms(): Flow<Result<List<MeditationProgram>>> = flow {
        try {
            val response = supabaseClient.from("meditation_programs")
                .select("*")
                .order("created_at", ascending = false)
                .execute()
            
            val programs = response.data.mapNotNull { programData ->
                parseProgram(programData)
            }
            
            emit(Result.success(programs))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get program by ID with session details
     */
    fun getProgramById(programId: String): Flow<Result<MeditationProgram>> = flow {
        try {
            // Try cache first
            val cachedProgram = localCacheService.getProgram(programId)
            if (cachedProgram != null) {
                emit(Result.success(cachedProgram))
                return@flow
            }
            
            // Fetch program details
            val programResponse = supabaseClient.from("meditation_programs")
                .select("*")
                .eq("id", programId)
                .single()
                .execute()
            
            val program = parseProgram(programResponse.data) ?: throw Exception("Program not found")
            
            // Fetch program sessions
            val sessionsResponse = supabaseClient.from("program_sessions")
                .select("*")
                .eq("program_id", programId)
                .order("day", ascending = true)
                .execute()
            
            val programSessions = sessionsResponse.data.mapNotNull { sessionData ->
                parseProgramSession(sessionData)
            }
            
            // Update program with sessions
            val fullProgram = program.copy(sessions = programSessions)
            
            // Cache the program
            localCacheService.cacheProgram(fullProgram)
            
            emit(Result.success(fullProgram))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get 21-day foundation program
     */
    fun getFoundationProgram(): Flow<Result<MeditationProgram>> = flow {
        try {
            val response = supabaseClient.from("meditation_programs")
                .select("*")
                .eq("category", ProgramCategory.FOUNDATION.name)
                .eq("duration", 21)
                .single()
                .execute()
            
            val program = parseProgram(response.data)
            
            if (program != null) {
                emit(Result.success(program))
            } else {
                emit(Result.failure(Exception("Foundation program not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Search sessions by query
     */
    fun searchSessions(query: String): Flow<Result<List<MeditationSession>>> = flow {
        try {
            val response = supabaseClient.from("meditation_sessions")
                .select("*")
                .or("title.ilike.%$query%,description.ilike.%$query%,instructor_name.ilike.%$query%")
                .order("rating", ascending = false)
                .execute()
            
            val sessions = response.data.mapNotNull { sessionData ->
                parseSession(sessionData)
            }
            
            emit(Result.success(sessions))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get audio streaming URL with fallback
     */
    fun getAudioStreamingUrl(sessionId: String, quality: AudioQuality = AudioQuality.STANDARD): Flow<Result<AudioStreamingInfo>> = flow {
        try {
            val session = localCacheService.getSession(sessionId)
                ?: throw Exception("Session not found")
            
            // Try streaming URL first
            if (session.audioUrl != null) {
                val streamingInfo = AudioStreamingInfo(
                    url = session.audioUrl,
                    quality = quality,
                    isLocal = false,
                    fallbackUrl = session.localAudioPath,
                    downloadSize = session.downloadSize
                )
                
                emit(Result.success(streamingInfo))
            } else if (session.localAudioPath != null) {
                // Use local fallback
                val streamingInfo = AudioStreamingInfo(
                    url = session.localAudioPath,
                    quality = quality,
                    isLocal = true,
                    fallbackUrl = null,
                    downloadSize = session.downloadSize
                )
                
                emit(Result.success(streamingInfo))
            } else {
                emit(Result.failure(Exception("No audio available for this session")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Download session for offline access
     */
    fun downloadSession(sessionId: String): Flow<Result<DownloadProgress>> = flow {
        try {
            val session = localCacheService.getSession(sessionId)
                ?: throw Exception("Session not found")
            
            if (session.audioUrl == null) {
                emit(Result.failure(Exception("No streaming URL available")))
                return@flow
            }
            
            // Start download
            emit(Result.success(DownloadProgress(sessionId, 0, 100, "Starting download...")))
            
            // Simulate download progress
            for (progress in 10..100 step 10) {
                emit(Result.success(DownloadProgress(sessionId, progress, 100, "Downloading...")))
                kotlinx.coroutines.delay(500)
            }
            
            // Cache downloaded file
            val localPath = "/local/audio/${sessionId}.mp3"
            localCacheService.cacheAudioFile(sessionId, localPath)
            
            emit(Result.success(DownloadProgress(sessionId, 100, 100, "Download complete")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get content bundles for offline access
     */
    fun getContentBundles(): Flow<Result<List<ContentBundle>>> = flow {
        try {
            val response = supabaseClient.from("content_bundles")
                .select("*")
                .order("created_at", ascending = false)
                .execute()
            
            val bundles = response.data.mapNotNull { bundleData ->
                parseContentBundle(bundleData)
            }
            
            emit(Result.success(bundles))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Download content bundle
     */
    fun downloadContentBundle(bundleId: String): Flow<Result<BundleDownloadProgress>> = flow {
        try {
            val bundle = localCacheService.getContentBundle(bundleId)
                ?: throw Exception("Bundle not found")
            
            emit(Result.success(BundleDownloadProgress(bundleId, 0, bundle.sessions.size, "Starting bundle download...")))
            
            // Download each session in the bundle
            bundle.sessions.forEachIndexed { index, sessionId ->
                emit(Result.success(BundleDownloadProgress(bundleId, index, bundle.sessions.size, "Downloading session ${index + 1}/${bundle.sessions.size}")))
                
                // Download session audio
                val downloadResult = downloadSession(sessionId).first()
                if (downloadResult.isFailure) {
                    emit(Result.failure(downloadResult.exceptionOrNull() ?: Exception("Failed to download session")))
                    return@flow
                }
            }
            
            // Mark bundle as downloaded
            localCacheService.markBundleAsDownloaded(bundleId)
            
            emit(Result.success(BundleDownloadProgress(bundleId, bundle.sessions.size, bundle.sessions.size, "Bundle download complete")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Parse session data from Supabase response
     */
    private fun parseSession(data: Map<String, Any>): MeditationSession? {
        return try {
            MeditationSession(
                id = data["id"] as String,
                title = data["title"] as String,
                description = data["description"] as String,
                instructorName = data["instructor_name"] as String,
                instructorBio = data["instructor_bio"] as String,
                duration = (data["duration"] as Double).toInt(),
                audioUrl = data["audio_url"] as? String,
                localAudioPath = data["local_audio_path"] as? String,
                thumbnailUrl = data["thumbnail_url"] as String,
                category = SessionCategory.valueOf(data["category"] as String),
                difficulty = DifficultyLevel.valueOf(data["difficulty"] as String),
                tags = (data["tags"] as List<String>).map { it },
                transcript = data["transcript"] as? String,
                keyPoints = (data["key_points"] as List<String>).map { it },
                breathingInstructions = data["breathing_instructions"] as? String,
                isPremium = data["is_premium"] as Boolean,
                downloadSize = (data["download_size"] as? Double)?.toLong(),
                streamingQuality = AudioQuality.valueOf(data["streaming_quality"] as String),
                createdAt = (data["created_at"] as String).toLong(),
                updatedAt = (data["updated_at"] as String).toLong(),
                viewCount = (data["view_count"] as Double).toInt(),
                rating = (data["rating"] as Double).toFloat(),
                reviewCount = (data["review_count"] as Double).toInt()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse program data from Supabase response
     */
    private fun parseProgram(data: Map<String, Any>): MeditationProgram? {
        return try {
            MeditationProgram(
                id = data["id"] as String,
                title = data["title"] as String,
                description = data["description"] as String,
                duration = (data["duration"] as Double).toInt(),
                category = ProgramCategory.valueOf(data["category"] as String),
                difficulty = DifficultyLevel.valueOf(data["difficulty"] as String),
                instructorName = data["instructor_name"] as String,
                instructorBio = data["instructor_bio"] as String,
                thumbnailUrl = data["thumbnail_url"] as String,
                sessions = emptyList(), // Will be populated separately
                benefits = (data["benefits"] as List<String>).map { it },
                requirements = (data["requirements"] as List<String>).map { it },
                isPremium = data["is_premium"] as Boolean,
                price = data["price"] as? String,
                rating = (data["rating"] as Double).toFloat(),
                reviewCount = (data["review_count"] as Double).toInt(),
                enrollmentCount = (data["enrollment_count"] as Double).toInt(),
                createdAt = (data["created_at"] as String).toLong(),
                updatedAt = (data["updated_at"] as String).toLong()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse program session data
     */
    private fun parseProgramSession(data: Map<String, Any>): ProgramSession? {
        return try {
            ProgramSession(
                day = (data["day"] as Double).toInt(),
                order = (data["order"] as Double).toInt(),
                sessionId = data["session_id"] as String,
                title = data["title"] as String,
                description = data["description"] as String,
                duration = (data["duration"] as Double).toInt(),
                objectives = (data["objectives"] as List<String>).map { it },
                prerequisites = (data["prerequisites"] as List<String>).map { it },
                isOptional = data["is_optional"] as Boolean
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse content bundle data
     */
    private fun parseContentBundle(data: Map<String, Any>): ContentBundle? {
        return try {
            ContentBundle(
                id = data["id"] as String,
                title = data["title"] as String,
                description = data["description"] as String,
                sessions = (data["sessions"] as List<String>).map { it },
                totalSize = (data["total_size"] as Double).toLong(),
                downloadUrl = data["download_url"] as String,
                version = data["version"] as String,
                isDownloaded = data["is_downloaded"] as Boolean,
                localPath = data["local_path"] as? String,
                downloadedAt = (data["downloaded_at"] as? String)?.toLong(),
                lastUpdated = (data["last_updated"] as String).toLong()
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Audio streaming information
 */
data class AudioStreamingInfo(
    val url: String,
    val quality: AudioQuality,
    val isLocal: Boolean,
    val fallbackUrl: String?,
    val downloadSize: Long?
)

/**
 * Download progress information
 */
data class DownloadProgress(
    val sessionId: String,
    val progress: Int,
    val total: Int,
    val message: String
)

/**
 * Bundle download progress
 */
data class BundleDownloadProgress(
    val bundleId: String,
    val completed: Int,
    val total: Int,
    val message: String
)
