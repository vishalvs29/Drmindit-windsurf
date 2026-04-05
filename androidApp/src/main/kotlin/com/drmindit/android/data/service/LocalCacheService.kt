package com.drmindit.android.data.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.drmindit.shared.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJson
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local Cache Service
 * Handles local storage of sessions, programs, and audio files
 */
@Singleton
class LocalCacheService @Inject constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Cache directories
    private val audioCacheDir = File(context.cacheDir, "audio")
    private val sessionCacheDir = File(context.cacheDir, "sessions")
    private val programCacheDir = File(context.cacheDir, "programs")
    
    init {
        // Create cache directories
        audioCacheDir.mkdirs()
        sessionCacheDir.mkdirs()
        programCacheDir.mkdirs()
    }
    
    /**
     * Cache session data
     */
    suspend fun cacheSession(session: MeditationSession) {
        try {
            val sessionFile = File(sessionCacheDir, "${session.id}.json")
            sessionFile.writeText(json.encodeToString(session))
            
            // Update cache index
            updateSessionCacheIndex(session.id, System.currentTimeMillis())
        } catch (e: IOException) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cached session
     */
    suspend fun getSession(sessionId: String): MeditationSession? {
        return try {
            val sessionFile = File(sessionCacheDir, "$sessionId.json")
            if (sessionFile.exists()) {
                json.decodeFromString<MeditationSession>(sessionFile.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cache program data
     */
    suspend fun cacheProgram(program: MeditationProgram) {
        try {
            val programFile = File(programCacheDir, "${program.id}.json")
            programFile.writeText(json.encodeToString(program))
            
            // Update cache index
            updateProgramCacheIndex(program.id, System.currentTimeMillis())
        } catch (e: IOException) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cached program
     */
    suspend fun getProgram(programId: String): MeditationProgram? {
        return try {
            val programFile = File(programCacheDir, "$programId.json")
            if (programFile.exists()) {
                json.decodeFromString<MeditationProgram>(programFile.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cache audio file
     */
    suspend fun cacheAudioFile(sessionId: String, localPath: String) {
        try {
            val audioFile = File(audioCacheDir, "$sessionId.mp3")
            
            // Create placeholder file (in real implementation, this would download the actual audio)
            audioFile.createNewFile()
            
            // Update cache index
            updateAudioCacheIndex(sessionId, localPath, audioFile.absolutePath, System.currentTimeMillis())
        } catch (e: IOException) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cached audio file path
     */
    suspend fun getCachedAudioPath(sessionId: String): String? {
        return try {
            val audioFile = File(audioCacheDir, "$sessionId.mp3")
            if (audioFile.exists()) {
                audioFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cache content bundle
     */
    suspend fun cacheContentBundle(bundle: ContentBundle) {
        try {
            val bundleFile = File(context.cacheDir, "bundles/${bundle.id}.json")
            bundleFile.parentFile?.mkdirs()
            bundleFile.writeText(json.encodeToString(bundle))
            
            // Update cache index
            updateBundleCacheIndex(bundle.id, System.currentTimeMillis())
        } catch (e: IOException) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cached content bundle
     */
    suspend fun getContentBundle(bundleId: String): ContentBundle? {
        return try {
            val bundleFile = File(context.cacheDir, "bundles/$bundleId.json")
            if (bundleFile.exists()) {
                json.decodeFromString<ContentBundle>(bundleFile.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Mark bundle as downloaded
     */
    suspend fun markBundleAsDownloaded(bundleId: String) {
        try {
            val bundle = getContentBundle(bundleId)
            if (bundle != null) {
                val updatedBundle = bundle.copy(
                    isDownloaded = true,
                    localPath = context.cacheDir.absolutePath + "/bundles/$bundleId",
                    downloadedAt = System.currentTimeMillis()
                )
                cacheContentBundle(updatedBundle)
            }
        } catch (e: Exception) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cache size
     */
    fun getCacheSize(): Long {
        return try {
            var totalSize = 0L
            
            // Calculate audio cache size
            audioCacheDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            
            // Calculate session cache size
            sessionCacheDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            
            // Calculate program cache size
            programCacheDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    totalSize += file.length()
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Clear cache
     */
    suspend fun clearCache() {
        try {
            // Clear audio cache
            audioCacheDir.deleteRecursively()
            audioCacheDir.mkdirs()
            
            // Clear session cache
            sessionCacheDir.deleteRecursively()
            sessionCacheDir.mkdirs()
            
            // Clear program cache
            programCacheDir.deleteRecursively()
            programCacheDir.mkdirs()
            
            // Clear cache indices
            clearCacheIndices()
        } catch (e: Exception) {
            // Log error but don't fail
        }
    }
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache(maxAge: Long = 7 * 24 * 60 * 60 * 1000L) { // 7 days
        val currentTime = System.currentTimeMillis()
        
        try {
            // Clear expired audio files
            audioCacheDir.listFiles()?.forEach { file ->
                if (currentTime - file.lastModified() > maxAge) {
                    file.delete()
                }
            }
            
            // Clear expired session files
            sessionCacheDir.listFiles()?.forEach { file ->
                if (currentTime - file.lastModified() > maxAge) {
                    file.delete()
                }
            }
            
            // Clear expired program files
            programCacheDir.listFiles()?.forEach { file ->
                if (currentTime - file.lastModified() > maxAge) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail
        }
    }
    
    /**
     * Get cached sessions list
     */
    suspend fun getCachedSessions(): List<String> {
        return try {
            sessionCacheDir.listFiles()
                ?.mapNotNull { file ->
                    val sessionId = file.nameWithoutExtension
                    if (file.exists()) sessionId else null
                }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get cached programs list
     */
    suspend fun getCachedPrograms(): List<String> {
        return try {
            programCacheDir.listFiles()
                ?.mapNotNull { file ->
                    val programId = file.nameWithoutExtension
                    if (file.exists()) programId else null
                }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Check if session is cached
     */
    suspend fun isSessionCached(sessionId: String): Boolean {
        return try {
            File(sessionCacheDir, "$sessionId.json").exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if audio is cached
     */
    suspend fun isAudioCached(sessionId: String): Boolean {
        return try {
            File(audioCacheDir, "$sessionId.mp3").exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Update session cache index
     */
    private suspend fun updateSessionCacheIndex(sessionId: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("session_cache_$sessionId")] = timestamp.toString()
        }
    }
    
    /**
     * Update program cache index
     */
    private suspend fun updateProgramCacheIndex(programId: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("program_cache_$programId")] = timestamp.toString()
        }
    }
    
    /**
     * Update audio cache index
     */
    private suspend fun updateAudioCacheIndex(sessionId: String, originalPath: String, localPath: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("audio_cache_$sessionId")] = "$originalPath|$localPath|$timestamp"
        }
    }
    
    /**
     * Update bundle cache index
     */
    private suspend fun updateBundleCacheIndex(bundleId: String, timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("bundle_cache_$bundleId")] = timestamp.toString()
        }
    }
    
    /**
     * Clear cache indices
     */
    private suspend fun clearCacheIndices() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats {
        val totalSize = getCacheSize()
        val sessionCount = getCachedSessions().size
        val programCount = getCachedPrograms().size
        val audioCount = audioCacheDir.listFiles()?.size ?: 0
        
        return CacheStats(
            totalSize = totalSize,
            sessionCount = sessionCount,
            programCount = programCount,
            audioCount = audioCount,
            formattedSize = formatFileSize(totalSize)
        )
    }
    
    /**
     * Format file size for display
     */
    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.1f KB".format(kb)
        val mb = kb / 1024.0
        if (mb < 1024) return "%.1f MB".format(mb)
        val gb = mb / 1024.0
        return "%.1f GB".format(gb)
    }
}

/**
 * Cache statistics
 */
data class CacheStats(
    val totalSize: Long,
    val sessionCount: Int,
    val programCount: Int,
    val audioCount: Int,
    val formattedSize: String
)
