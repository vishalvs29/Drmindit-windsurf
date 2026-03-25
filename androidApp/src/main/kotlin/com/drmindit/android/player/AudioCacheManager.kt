package com.drmindit.android.player

import android.content.Context
import androidx.media3.common.*
import androidx.media3.datasource.*
import androidx.media3.datasource.cache.*
import androidx.media3.exoplayer.upstream.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AudioCacheManager(
    private val context: Context
) {
    companion object {
        private const val CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB
        private const val CACHE_FOLDER_NAME = "audio_cache"
    }
    
    private val cache: Cache
    private val dataSourceFactory: DataSource.Factory
    private val downloadTracker = ConcurrentHashMap<String, DownloadProgress>()
    
    init {
        val cacheDir = File(context.cacheDir, CACHE_FOLDER_NAME)
        cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES),
            DatabaseProvider(context)
        )
        
        dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCacheWriteDataSinkFactory(null) // Use default
    }
    
    fun getCachedDataSource(): DataSource.Factory {
        return dataSourceFactory
    }
    
    suspend fun downloadAudio(
        sessionId: String,
        audioUrl: String,
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            downloadTracker[sessionId] = DownloadProgress(0f)
            
            // Create a data source for downloading
            val upstreamDataSource = DefaultHttpDataSource.Factory().createDataSource()
            
            // Get the content length
            val response = upstreamDataSource.open(DataSpec(Uri.parse(audioUrl)))
            val contentLength = response.contentLength ?: -1
            
            // Create cache key
            val cacheKey = generateCacheKey(sessionId, audioUrl)
            
            // Download to cache
            val cacheDataSource = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setCacheWriteDataSinkFactory(CacheDataSinkFactory(cache, contentLength))
                .createDataSource()
            
            cacheDataSource.open(DataSpec(Uri.parse(audioUrl)))
            
            var totalBytesRead = 0L
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (cacheDataSource.read(buffer, 0, buffer.size).also { bytesRead = it } != -1) {
                totalBytesRead += bytesRead
                
                // Update progress
                if (contentLength > 0) {
                    val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                    downloadTracker[sessionId] = DownloadProgress(progress)
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
            }
            
            cacheDataSource.close()
            
            // Mark download as complete
            downloadTracker[sessionId] = DownloadProgress(1f, isComplete = true)
            
            Result.success(cacheKey)
        } catch (e: Exception) {
            downloadTracker[sessionId] = DownloadProgress(0f, error = e.message)
            Result.failure(e)
        }
    }
    
    fun isAudioCached(sessionId: String, audioUrl: String): Boolean {
        val cacheKey = generateCacheKey(sessionId, audioUrl)
        return cache.isCached(cacheKey, 0, Long.MAX_VALUE)
    }
    
    fun getCachedAudioSize(sessionId: String, audioUrl: String): Long {
        val cacheKey = generateCacheKey(sessionId, audioUrl)
        return cache.getCachedBytes(cacheKey, 0, Long.MAX_VALUE)
    }
    
    suspend fun deleteCachedAudio(sessionId: String, audioUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(sessionId, audioUrl)
            cache.removeResource(cacheKey)
            downloadTracker.remove(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCacheSize(): Long {
        return cache.cacheSpace
    }
    
    fun getCacheUsage(): Float {
        return cache.cacheSpace.toFloat() / CACHE_SIZE_BYTES.toFloat()
    }
    
    suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cache.release()
            downloadTracker.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeDownloadProgress(sessionId: String): Flow<Float> = flow {
        while (true) {
            val progress = downloadTracker[sessionId]?.progress ?: 0f
            emit(progress)
            if (progress >= 1f) break
            delay(100)
        }
    }
    
    fun getDownloadProgress(sessionId: String): Float {
        return downloadTracker[sessionId]?.progress ?: 0f
    }
    
    fun isDownloadComplete(sessionId: String): Boolean {
        return downloadTracker[sessionId]?.isComplete ?: false
    }
    
    fun getDownloadError(sessionId: String): String? {
        return downloadTracker[sessionId]?.error
    }
    
    private fun generateCacheKey(sessionId: String, audioUrl: String): String {
        return "${sessionId}_${audioUrl.hashCode()}"
    }
    
    // Cache statistics
    fun getCacheStats(): CacheStats {
        return CacheStats(
            totalSize = cache.cacheSpace,
            maxSize = CACHE_SIZE_BYTES,
            usagePercentage = getCacheUsage(),
            cachedItems = downloadTracker.size
        )
    }
    
    // Batch operations
    suspend fun downloadMultipleSessions(
        sessions: List<Pair<String, String>>, // (sessionId, audioUrl)
        onProgress: (String, Float) -> Unit = { _, _ -> }
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<String>()
            
            sessions.forEach { (sessionId, audioUrl) ->
                val result = downloadAudio(sessionId, audioUrl) { progress ->
                    onProgress(sessionId, progress)
                }
                
                if (result.isSuccess) {
                    results.add(result.getOrNull() ?: "")
                }
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Offline availability check
    suspend fun checkOfflineAvailability(sessionId: String, audioUrl: String): OfflineAvailability {
        return if (isAudioCached(sessionId, audioUrl)) {
            val size = getCachedAudioSize(sessionId, audioUrl)
            OfflineAvailability.AVAILABLE(size)
        } else {
            OfflineAvailability.NOT_AVAILABLE
        }
    }
}

data class DownloadProgress(
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null
)

data class CacheStats(
    val totalSize: Long,
    val maxSize: Long,
    val usagePercentage: Float,
    val cachedItems: Int
)

sealed class OfflineAvailability {
    data class AVAILABLE(val size: Long) : OfflineAvailability()
    object NOT_AVAILABLE : OfflineAvailability()
}

// Extension function for AudioPlayerManager to use cached content
fun AudioPlayerManager.loadCachedAudio(
    sessionId: String,
    audioUrl: String,
    cacheManager: AudioCacheManager,
    title: String? = null,
    artist: String? = null,
    artworkUri: Uri? = null
) {
    initializePlayer()
    
    currentSessionId = sessionId
    
    // Reset state
    _playerState.value = AudioPlayerState(
        sessionId = sessionId,
        title = title,
        artist = artist,
        artworkUri = artworkUri,
        isBuffering = true
    )
    
    try {
        // Check if audio is cached
        if (cacheManager.isAudioCached(sessionId, audioUrl)) {
            // Use cached data source
            val mediaSource = ProgressiveMediaSource.Factory(cacheManager.getCachedDataSource())
                .createMediaSource(MediaItem.fromUri(Uri.parse(audioUrl)))
            
            exoPlayer?.setMediaSource(mediaSource)
        } else {
            // Use regular data source (will cache automatically)
            val mediaSource = ProgressiveMediaSource.Factory(
                CacheDataSource.Factory()
                    .setCache(cacheManager.cache)
                    .setUpstreamDataSourceFactory(DefaultDataSource.Factory())
            ).createMediaSource(MediaItem.fromUri(Uri.parse(audioUrl)))
            
            exoPlayer?.setMediaSource(mediaSource)
        }
        
        // Set metadata if available
        title?.let { 
            exoPlayer?.setMediaItem(
                MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(it)
                            .setArtist(artist ?: "")
                            .setArtworkUri(artworkUri)
                            .build()
                    )
                    .build()
            )
        }
        
        exoPlayer?.prepare()
        
    } catch (e: Exception) {
        _playerState.value = _playerState.value.copy(
            error = "Failed to load audio: ${e.message}",
            isBuffering = false
        )
    }
}
