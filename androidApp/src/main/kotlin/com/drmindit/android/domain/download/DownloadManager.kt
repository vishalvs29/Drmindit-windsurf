package com.drmindit.android.domain.download

import android.content.Context
import androidx.media3.common.C
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Manages offline audio downloads for DrMindit sessions
 */
class SessionDownloadManager(
    private val context: Context
) {
    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: Flow<Map<String, Float>> = _downloadProgress.asStateFlow()
    
    private val _downloadedFiles = MutableStateFlow<Set<String>>(emptySet())
    val downloadedFiles: Flow<Set<String>> = _downloadedFiles.asStateFlow()
    
    init {
        // TODO: Setup download listener
    }
    
    fun downloadSession(
        sessionId: String,
        audioUrl: String,
        title: String
    ): Boolean {
        return try {
            // Check if already downloaded
            if (_downloadedFiles.value.contains(sessionId)) {
                return true
            }
            
            // TODO: Implement actual download
            false
        } catch (e: Exception) {
            false
        }
    }
    
    fun isSessionDownloaded(sessionId: String): Boolean {
        return _downloadedFiles.value.contains(sessionId)
    }
    
    fun getDownloadedFile(sessionId: String): File? {
        val downloadFile = File(context.getExternalFilesDir(null), "downloads/$sessionId.mp3")
        return if (downloadFile.exists()) downloadFile else null
    }
    
    private fun getDownloadUri(sessionId: String): android.net.Uri {
        val downloadDir = File(context.getExternalFilesDir(null), "downloads")
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
        val downloadFile = File(downloadDir, "$sessionId.mp3")
        return android.net.Uri.fromFile(downloadFile)
    }
    
    fun getDownloadProgress(sessionId: String): Float {
        return _downloadProgress.value[sessionId] ?: 0f
    }
    
    fun removeDownloadedFile(sessionId: String) {
        val file = getDownloadedFile(sessionId)
        file?.delete()
        
        // Update downloaded files set
        val updatedSet = _downloadedFiles.value - sessionId
        _downloadedFiles.value = updatedSet
        
        // TODO: Cancel download if in progress
    }
    
    fun clearAllDownloads() {
        _downloadedFiles.value.forEach { sessionId ->
            removeDownloadedFile(sessionId)
        }
    }
    
    fun getTotalDownloadSize(): Long {
        val downloadDir = File(context.getExternalFilesDir(null), "downloads")
        return if (downloadDir.exists()) {
            downloadDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } else 0L
    }
}
