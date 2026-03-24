package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.model.AudioSession
import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.model.RealAudioContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AudioSessionRepository {
    suspend fun getAllSessions(): Result<List<AudioSession>>
    suspend fun getSessionsByCategory(categoryId: String): Result<List<AudioSession>>
    suspend fun getSessionById(sessionId: String): Result<AudioSession>
    suspend fun getFeaturedSessions(): Result<List<AudioSession>>
    suspend fun getSessionOfDay(): Result<AudioSession>
    suspend fun getRecentlyPlayed(): Result<List<AudioSession>>
    suspend fun getContinueListening(): Result<List<AudioSession>>
    suspend fun searchSessions(query: String): Result<List<AudioSession>>
    suspend fun markAsFavorite(sessionId: String): Result<Unit>
    suspend fun unmarkAsFavorite(sessionId: String): Result<Unit>
    suspend fun getFavoriteSessions(): Result<List<AudioSession>>
    suspend fun downloadSession(sessionId: String): Result<Unit>
    suspend fun deleteDownloadedSession(sessionId: String): Result<Unit>
    suspend fun getDownloadedSessions(): Result<List<AudioSession>>
    fun observeSessionUpdates(): Flow<List<AudioSession>>
    fun observeDownloadProgress(sessionId: String): Flow<Float>
    suspend fun updateSessionProgress(sessionId: String, progressSeconds: Int, totalSeconds: Int): Result<Unit>
    suspend fun getSessionProgress(sessionId: String): Result<Pair<Int, Int>>
}

class AudioSessionRepositoryImpl : AudioSessionRepository {
    
    private val _allSessions = MutableStateFlow<List<AudioSession>>(emptyList())
    private val _favoriteSessions = MutableStateFlow<Set<String>>(emptySet())
    private val _downloadedSessions = MutableStateFlow<Set<String>>(emptySet())
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val _sessionProgress = MutableStateFlow<Map<String, Pair<Int, Int>>>(emptyMap())
    
    // Cache for real content
    private var cachedSessions: List<AudioSession> = emptyList()
    private var lastFetchTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes
    
    init {
        // Load initial data
        loadInitialData()
    }
    
    private fun loadInitialData() {
        val sessions = RealAudioContent.getSampleSessions()
        cachedSessions = sessions
        _allSessions.value = sessions
        lastFetchTime = System.currentTimeMillis()
    }
    
    private fun refreshCacheIfNeeded() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFetchTime > CACHE_DURATION) {
            loadInitialData()
        }
    }
    
    override suspend fun getAllSessions(): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        return Result.success(cachedSessions)
    }
    
    override suspend fun getSessionsByCategory(categoryId: String): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        val sessions = cachedSessions.filter { it.category.id == categoryId }
        return Result.success(sessions)
    }
    
    override suspend fun getSessionById(sessionId: String): Result<AudioSession> {
        refreshCacheIfNeeded()
        val session = cachedSessions.find { it.id == sessionId }
        return if (session != null) {
            Result.success(session)
        } else {
            Result.failure(Exception("Session not found: $sessionId"))
        }
    }
    
    override suspend fun getFeaturedSessions(): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        val featured = cachedSessions
            .filter { it.rating >= 4.7f }
            .sortedByDescending { it.rating }
            .take(10)
        return Result.success(featured)
    }
    
    override suspend fun getSessionOfDay(): Result<AudioSession> {
        refreshCacheIfNeeded()
        val sessionOfDay = RealAudioContent.getSessionOfDay()
        return Result.success(sessionOfDay)
    }
    
    override suspend fun getRecentlyPlayed(): Result<List<AudioSession>> {
        // In a real app, this would come from user preferences/local storage
        // For now, return some random sessions as placeholder
        refreshCacheIfNeeded()
        val recent = cachedSessions.shuffled().take(5)
        return Result.success(recent)
    }
    
    override suspend fun getContinueListening(): Result<List<AudioSession>> {
        // In a real app, this would come from user progress
        // For now, return some sessions with progress
        refreshCacheIfNeeded()
        val continueListening = cachedSessions.shuffled().take(4)
        return Result.success(continueListening)
    }
    
    override suspend fun searchSessions(query: String): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        val lowercaseQuery = query.lowercase()
        val filtered = cachedSessions.filter { session ->
            session.title.lowercase().contains(lowercaseQuery) ||
            session.description.lowercase().contains(lowercaseQuery) ||
            session.instructorName.lowercase().contains(lowercaseQuery) ||
            session.tags.any { it.lowercase().contains(lowercaseQuery) } ||
            session.category.name.lowercase().contains(lowercaseQuery)
        }
        return Result.success(filtered)
    }
    
    override suspend fun markAsFavorite(sessionId: String): Result<Unit> {
        val currentFavorites = _favoriteSessions.value.toMutableSet()
        currentFavorites.add(sessionId)
        _favoriteSessions.value = currentFavorites
        return Result.success(Unit)
    }
    
    override suspend fun unmarkAsFavorite(sessionId: String): Result<Unit> {
        val currentFavorites = _favoriteSessions.value.toMutableSet()
        currentFavorites.remove(sessionId)
        _favoriteSessions.value = currentFavorites
        return Result.success(Unit)
    }
    
    override suspend fun getFavoriteSessions(): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        val favoriteIds = _favoriteSessions.value
        val favoriteSessions = cachedSessions.filter { it.id in favoriteIds }
        return Result.success(favoriteSessions)
    }
    
    override suspend fun downloadSession(sessionId: String): Result<Unit> {
        // Simulate download process
        val currentProgress = _downloadProgress.value.toMutableMap()
        
        // Simulate download progress updates
        for (progress in 0..100 step 10) {
            currentProgress[sessionId] = progress.toFloat() / 100f
            _downloadProgress.value = currentProgress
            kotlinx.coroutines.delay(200) // Simulate download time
        }
        
        // Mark as downloaded
        val currentDownloaded = _downloadedSessions.value.toMutableSet()
        currentDownloaded.add(sessionId)
        _downloadedSessions.value = currentDownloaded
        
        // Clear progress
        currentProgress.remove(sessionId)
        _downloadProgress.value = currentProgress
        
        return Result.success(Unit)
    }
    
    override suspend fun deleteDownloadedSession(sessionId: String): Result<Unit> {
        val currentDownloaded = _downloadedSessions.value.toMutableSet()
        currentDownloaded.remove(sessionId)
        _downloadedSessions.value = currentDownloaded
        
        // Clear progress if any
        val currentProgress = _downloadProgress.value.toMutableMap()
        currentProgress.remove(sessionId)
        _downloadProgress.value = currentProgress
        
        return Result.success(Unit)
    }
    
    override suspend fun getDownloadedSessions(): Result<List<AudioSession>> {
        refreshCacheIfNeeded()
        val downloadedIds = _downloadedSessions.value
        val downloadedSessions = cachedSessions.filter { it.id in downloadedIds }
        return Result.success(downloadedSessions)
    }
    
    override fun observeSessionUpdates(): Flow<List<AudioSession>> {
        return _allSessions.asStateFlow()
    }
    
    override fun observeDownloadProgress(sessionId: String): Flow<Float> {
        return kotlinx.coroutines.flow.flow {
            while (true) {
                val progress = _downloadProgress.value[sessionId] ?: 0f
                emit(progress)
                kotlinx.coroutines.delay(100)
            }
        }
    }
    
    override suspend fun updateSessionProgress(sessionId: String, progressSeconds: Int, totalSeconds: Int): Result<Unit> {
        val currentProgress = _sessionProgress.value.toMutableMap()
        currentProgress[sessionId] = Pair(progressSeconds, totalSeconds)
        _sessionProgress.value = currentProgress
        return Result.success(Unit)
    }
    
    override suspend fun getSessionProgress(sessionId: String): Result<Pair<Int, Int>> {
        val progress = _sessionProgress.value[sessionId] ?: Pair(0, 0)
        return Result.success(progress)
    }
    
    // Helper methods for UI state
    fun isSessionFavorite(sessionId: String): Boolean {
        return sessionId in _favoriteSessions.value
    }
    
    fun isSessionDownloaded(sessionId: String): Boolean {
        return sessionId in _downloadedSessions.value
    }
    
    fun getSessionDownloadProgress(sessionId: String): Float {
        return _downloadProgress.value[sessionId] ?: 0f
    }
    
    fun getSessionProgressPercentage(sessionId: String): Float {
        val (progress, total) = _sessionProgress.value[sessionId] ?: return 0f
        return if (total > 0) progress.toFloat() / total.toFloat() else 0f
    }
}

// Backend integration for real audio content
class BackendAudioSessionRepository(
    private val supabaseService: com.drmindit.shared.data.network.SupabaseService
) : AudioSessionRepository {
    
    private val localRepository = AudioSessionRepositoryImpl()
    
    override suspend fun getAllSessions(): Result<List<AudioSession>> {
        return try {
            // Try to fetch from backend first
            val response = supabaseService.getSessions()
            if (response.isSuccess) {
                val sessions = response.getOrNull()?.map { sessionDto ->
                    // Convert backend DTO to AudioSession model
                    sessionDto.toAudioSession()
                } ?: emptyList()
                Result.success(sessions)
            } else {
                // Fallback to local cached content
                localRepository.getAllSessions()
            }
        } catch (e: Exception) {
            // Fallback to local cached content on network error
            localRepository.getAllSessions()
        }
    }
    
    override suspend fun getSessionsByCategory(categoryId: String): Result<List<AudioSession>> {
        return try {
            val response = supabaseService.getSessionsByCategory(categoryId)
            if (response.isSuccess) {
                val sessions = response.getOrNull()?.map { it.toAudioSession() } ?: emptyList()
                Result.success(sessions)
            } else {
                localRepository.getSessionsByCategory(categoryId)
            }
        } catch (e: Exception) {
            localRepository.getSessionsByCategory(categoryId)
        }
    }
    
    override suspend fun getSessionById(sessionId: String): Result<AudioSession> {
        return try {
            val response = supabaseService.getSessionById(sessionId)
            if (response.isSuccess) {
                val session = response.getOrNull()?.toAudioSession()
                if (session != null) {
                    Result.success(session)
                } else {
                    localRepository.getSessionById(sessionId)
                }
            } else {
                localRepository.getSessionById(sessionId)
            }
        } catch (e: Exception) {
            localRepository.getSessionById(sessionId)
        }
    }
    
    override suspend fun getFeaturedSessions(): Result<List<AudioSession>> {
        return try {
            val response = supabaseService.getFeaturedSessions()
            if (response.isSuccess) {
                val sessions = response.getOrNull()?.map { it.toAudioSession() } ?: emptyList()
                Result.success(sessions)
            } else {
                localRepository.getFeaturedSessions()
            }
        } catch (e: Exception) {
            localRepository.getFeaturedSessions()
        }
    }
    
    override suspend fun getSessionOfDay(): Result<AudioSession> {
        return try {
            val response = supabaseService.getSessionOfDay()
            if (response.isSuccess) {
                val session = response.getOrNull()?.toAudioSession()
                if (session != null) {
                    Result.success(session)
                } else {
                    localRepository.getSessionOfDay()
                }
            } else {
                localRepository.getSessionOfDay()
            }
        } catch (e: Exception) {
            localRepository.getSessionOfDay()
        }
    }
    
    override suspend fun getRecentlyPlayed(): Result<List<AudioSession>> {
        // This would typically come from user-specific backend data
        return localRepository.getRecentlyPlayed()
    }
    
    override suspend fun getContinueListening(): Result<List<AudioSession>> {
        // This would typically come from user-specific backend data
        return localRepository.getContinueListening()
    }
    
    override suspend fun searchSessions(query: String): Result<List<AudioSession>> {
        return try {
            val response = supabaseService.searchSessions(query)
            if (response.isSuccess) {
                val sessions = response.getOrNull()?.map { it.toAudioSession() } ?: emptyList()
                Result.success(sessions)
            } else {
                localRepository.searchSessions(query)
            }
        } catch (e: Exception) {
            localRepository.searchSessions(query)
        }
    }
    
    override suspend fun markAsFavorite(sessionId: String): Result<Unit> {
        return try {
            val response = supabaseService.markSessionAsFavorite(sessionId)
            if (response.isSuccess) {
                localRepository.markAsFavorite(sessionId)
            } else {
                Result.failure(Exception("Failed to mark as favorite"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unmarkAsFavorite(sessionId: String): Result<Unit> {
        return try {
            val response = supabaseService.unmarkSessionAsFavorite(sessionId)
            if (response.isSuccess) {
                localRepository.unmarkAsFavorite(sessionId)
            } else {
                Result.failure(Exception("Failed to unmark as favorite"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFavoriteSessions(): Result<List<AudioSession>> {
        // This would typically come from user-specific backend data
        return localRepository.getFavoriteSessions()
    }
    
    override suspend fun downloadSession(sessionId: String): Result<Unit> {
        // Download functionality would be handled locally
        return localRepository.downloadSession(sessionId)
    }
    
    override suspend fun deleteDownloadedSession(sessionId: String): Result<Unit> {
        return localRepository.deleteDownloadedSession(sessionId)
    }
    
    override suspend fun getDownloadedSessions(): Result<List<AudioSession>> {
        return localRepository.getDownloadedSessions()
    }
    
    override fun observeSessionUpdates(): Flow<List<AudioSession>> {
        return localRepository.observeSessionUpdates()
    }
    
    override fun observeDownloadProgress(sessionId: String): Flow<Float> {
        return localRepository.observeDownloadProgress(sessionId)
    }
    
    override suspend fun updateSessionProgress(sessionId: String, progressSeconds: Int, totalSeconds: Int): Result<Unit> {
        return try {
            val response = supabaseService.updateSessionProgress(sessionId, progressSeconds, totalSeconds)
            if (response.isSuccess) {
                localRepository.updateSessionProgress(sessionId, progressSeconds, totalSeconds)
            } else {
                Result.failure(Exception("Failed to update progress"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSessionProgress(sessionId: String): Result<Pair<Int, Int>> {
        return try {
            val response = supabaseService.getSessionProgress(sessionId)
            if (response.isSuccess) {
                val progress = response.getOrNull()
                if (progress != null) {
                    Result.success(Pair(progress.progressSeconds, progress.totalSeconds))
                } else {
                    localRepository.getSessionProgress(sessionId)
                }
            } else {
                localRepository.getSessionProgress(sessionId)
            }
        } catch (e: Exception) {
            localRepository.getSessionProgress(sessionId)
        }
    }
}

// Extension function to convert backend DTO to AudioSession (placeholder)
private fun Any.toAudioSession(): AudioSession {
    // In a real implementation, this would convert from your backend DTO
    // For now, return a placeholder from the real content
    return RealAudioContent.getSampleSessions().first()
}
