package com.drmindit.android.player

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LifecycleOwner
import com.drmindit.shared.domain.model.AudioSession
import com.drmindit.shared.data.repository.AudioSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Improved Audio Player Controller with proper lifecycle management
 */
@HiltViewModel
class ImprovedAudioPlayerController @Inject constructor(
    private val audioSessionRepository: AudioSessionRepository
) : ViewModel() {
    
    private val _currentSession = MutableStateFlow<AudioSession?>(null)
    val currentSession: StateFlow<AudioSession?> = _currentSession.asStateFlow()
    
    private val _playerState = MutableStateFlow(ImprovedAudioPlayerState())
    val playerState: StateFlow<ImprovedAudioPlayerState> = _playerState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    private val _isDownloaded = MutableStateFlow(false)
    val isDownloaded: StateFlow<Boolean> = _isDownloaded.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()
    
    private var audioPlayerManager: ImprovedAudioPlayerManager? = null
    
    /**
     * Initialize with lifecycle owner
     */
    fun initialize(lifecycleOwner: LifecycleOwner) {
        audioPlayerManager?.bindToLifecycle(lifecycleOwner.lifecycle)
    }
    
    /**
     * Load session with proper error handling
     */
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val sessionResult = audioSessionRepository.getSessionById(sessionId)
                if (sessionResult.isSuccess) {
                    val session = sessionResult.getOrNull()
                    if (session != null) {
                        _currentSession.value = session
                        loadAudioSession(session)
                        
                        // Check if session is favorite and downloaded
                        updateFavoriteStatus(sessionId)
                        updateDownloadStatus(sessionId)
                        loadSessionProgress(sessionId)
                    } else {
                        _error.value = "Session not found"
                    }
                } else {
                    _error.value = "Failed to load session: ${sessionResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading session: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load audio session into player manager
     */
    private fun loadAudioSession(session: AudioSession) {
        audioPlayerManager?.loadAudio(
            sessionId = session.id,
            audioUrl = session.audioUrl,
            title = session.title,
            artist = session.instructorName,
            artworkUri = Uri.parse(session.thumbnailUrl)
        )
    }
    
    /**
     * Update favorite status
     */
    private fun updateFavoriteStatus(sessionId: String) {
        viewModelScope.launch {
            try {
                val isFavorite = audioSessionRepository.isFavorite(sessionId)
                _isFavorite.value = isFavorite
            } catch (e: Exception) {
                // Log error but don't fail the operation
            }
        }
    }
    
    /**
     * Update download status
     */
    private fun updateDownloadStatus(sessionId: String) {
        viewModelScope.launch {
            try {
                val isDownloaded = audioSessionRepository.isDownloaded(sessionId)
                _isDownloaded.value = isDownloaded
            } catch (e: Exception) {
                // Log error but don't fail the operation
            }
        }
    }
    
    /**
     * Load session progress
     */
    private fun loadSessionProgress(sessionId: String) {
        viewModelScope.launch {
            try {
                val progress = audioSessionRepository.getSessionProgress(sessionId)
                _downloadProgress.value = progress
            } catch (e: Exception) {
                // Log error but don't fail the operation
            }
        }
    }
    
    /**
     * Play audio
     */
    fun play() {
        audioPlayerManager?.play()
    }
    
    /**
     * Pause audio
     */
    fun pause() {
        audioPlayerManager?.pause()
    }
    
    /**
     * Stop audio and clean up properly
     */
    fun stop() {
        audioPlayerManager?.stop()
        _currentSession.value = null
        _playerState.value = ImprovedAudioPlayerState()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioPlayerManager?.seekTo(position)
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        audioPlayerManager?.setPlaybackSpeed(speed)
    }
    
    /**
     * Skip forward
     */
    fun skipForward(seconds: Int = 10) {
        audioPlayerManager?.skipForward(seconds)
    }
    
    /**
     * Skip backward
     */
    fun skipBackward(seconds: Int = 10) {
        audioPlayerManager?.skipBackward(seconds)
    }
    
    /**
     * Toggle favorite status
     */
    fun toggleFavorite() {
        val session = _currentSession.value ?: return
        
        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    audioSessionRepository.unmarkAsFavorite(session.id)
                    _isFavorite.value = false
                } else {
                    audioSessionRepository.markAsFavorite(session.id)
                    _isFavorite.value = true
                }
            } catch (e: Exception) {
                _error.value = "Failed to toggle favorite: ${e.message}"
            }
        }
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? = _currentSession.value?.id
    
    /**
     * Get player for UI components
     */
    fun getPlayer(): ImprovedAudioPlayerManager? = audioPlayerManager
    
    /**
     * Cleanup when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        audioPlayerManager?.release()
    }
}
