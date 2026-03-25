package com.drmindit.android.player

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.domain.model.AudioSession
import com.drmindit.shared.data.repository.AudioSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioPlayerController @Inject constructor(
    private val audioSessionRepository: AudioSessionRepository
) : ViewModel() {
    
    private val _currentSession = MutableStateFlow<AudioSession?>(null)
    val currentSession: StateFlow<AudioSession?> = _currentSession.asStateFlow()
    
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
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
    
    private val _sessionProgress = MutableStateFlow(0f)
    val sessionProgress: StateFlow<Float> = _sessionProgress.asStateFlow()
    
    private var audioPlayerManager: AudioPlayerManager? = null
    
    fun initializePlayer(audioPlayerManager: AudioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager
        
        // Observe player state changes
        viewModelScope.launch {
            audioPlayerManager.playerState.collect { state ->
                _playerState.value = state
                
                // Update session progress
                if (state.duration > 0) {
                    _sessionProgress.value = state.currentPosition.toFloat() / state.duration.toFloat()
                }
                
                // Handle session completion
                if (state.isCompleted) {
                    markSessionAsCompleted()
                }
            }
        }
    }
    
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
    
    private fun loadAudioSession(session: AudioSession) {
        audioPlayerManager?.loadAudio(
            sessionId = session.id,
            audioUrl = session.audioUrl,
            title = session.title,
            artist = session.instructorName,
            artworkUri = Uri.parse(session.thumbnailUrl)
        )
    }
    
    fun play() {
        audioPlayerManager?.play()
    }
    
    fun pause() {
        audioPlayerManager?.pause()
    }
    
    fun stop() {
        audioPlayerManager?.stop()
        _currentSession.value = null
        _playerState.value = AudioPlayerState()
    }
    
    fun seekTo(position: Long) {
        audioPlayerManager?.seekTo(position)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        audioPlayerManager?.setPlaybackSpeed(speed)
    }
    
    fun skipForward(seconds: Int = 10) {
        audioPlayerManager?.skipForward(seconds)
    }
    
    fun skipBackward(seconds: Int = 10) {
        audioPlayerManager?.skipBackward(seconds)
    }
    
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
                _error.value = "Failed to update favorite status: ${e.message}"
            }
        }
    }
    
    fun downloadSession() {
        val session = _currentSession.value ?: return
        
        viewModelScope.launch {
            try {
                audioSessionRepository.downloadSession(session.id)
                _isDownloaded.value = true
                
                // Observe download progress
                audioSessionRepository.observeDownloadProgress(session.id)
                    .collect { progress ->
                        _downloadProgress.value = progress
                        if (progress >= 1.0f) {
                            _downloadProgress.value = 0f
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to download session: ${e.message}"
            }
        }
    }
    
    fun deleteDownloadedSession() {
        val session = _currentSession.value ?: return
        
        viewModelScope.launch {
            try {
                audioSessionRepository.deleteDownloadedSession(session.id)
                _isDownloaded.value = false
            } catch (e: Exception) {
                _error.value = "Failed to delete downloaded session: ${e.message}"
            }
        }
    }
    
    private fun updateFavoriteStatus(sessionId: String) {
        viewModelScope.launch {
            try {
                val favoriteSessions = audioSessionRepository.getFavoriteSessions()
                if (favoriteSessions.isSuccess) {
                    val isFav = favoriteSessions.getOrNull()?.any { it.id == sessionId } ?: false
                    _isFavorite.value = isFav
                }
            } catch (e: Exception) {
                // Silently handle error
            }
        }
    }
    
    private fun updateDownloadStatus(sessionId: String) {
        viewModelScope.launch {
            try {
                val downloadedSessions = audioSessionRepository.getDownloadedSessions()
                if (downloadedSessions.isSuccess) {
                    val isDownloaded = downloadedSessions.getOrNull()?.any { it.id == sessionId } ?: false
                    _isDownloaded.value = isDownloaded
                }
            } catch (e: Exception) {
                // Silently handle error
            }
        }
    }
    
    private fun loadSessionProgress(sessionId: String) {
        viewModelScope.launch {
            try {
                val progressResult = audioSessionRepository.getSessionProgress(sessionId)
                if (progressResult.isSuccess) {
                    val (progress, total) = progressResult.getOrNull() ?: Pair(0, 0)
                    if (total > 0) {
                        _sessionProgress.value = progress.toFloat() / total.toFloat()
                    }
                }
            } catch (e: Exception) {
                // Silently handle error
            }
        }
    }
    
    private fun markSessionAsCompleted() {
        val session = _currentSession.value ?: return
        val playerState = _playerState.value
        
        viewModelScope.launch {
            try {
                audioSessionRepository.updateSessionProgress(
                    sessionId = session.id,
                    progressSeconds = playerState.duration,
                    totalSeconds = playerState.duration
                )
            } catch (e: Exception) {
                // Silently handle error
            }
        }
    }
    
    fun updateProgress() {
        val playerState = _playerState.value
        val session = _currentSession.value ?: return
        
        if (playerState.duration > 0 && playerState.currentPosition > 0) {
            viewModelScope.launch {
                try {
                    audioSessionRepository.updateSessionProgress(
                        sessionId = session.id,
                        progressSeconds = playerState.currentPosition,
                        totalSeconds = playerState.duration
                    )
                } catch (e: Exception) {
                    // Silently handle error
                }
            }
        }
    }
    
    fun clearError() {
        _error.value = null
        audioPlayerManager?.clearError()
    }
    
    fun retryLoad() {
        val session = _currentSession.value
        if (session != null) {
            loadAudioSession(session)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayerManager?.release()
    }
    
    // Helper methods for UI
    fun getFormattedProgress(): String {
        return _playerState.value.formattedProgress
    }
    
    fun getFormattedDuration(): String {
        return _playerState.value.formattedDuration
    }
    
    fun getFormattedCurrentPosition(): String {
        return _playerState.value.formattedCurrentPosition
    }
    
    fun isPlaying(): Boolean {
        return _playerState.value.isPlaying
    }
    
    fun isBuffering(): Boolean {
        return _playerState.value.isBuffering
    }
    
    fun hasError(): Boolean {
        return _playerState.value.error != null || _error.value != null
    }
    
    fun getErrorMessage(): String? {
        return _playerState.value.error ?: _error.value
    }
    
    fun canSeek(): Boolean {
        return _playerState.value.duration > 0
    }
    
    fun getRemainingTime(): String {
        val playerState = _playerState.value
        val remaining = playerState.duration - playerState.currentPosition
        return formatTime(remaining)
    }
    
    private fun formatTime(timeMs: Int): String {
        if (timeMs < 0) return "0:00"
        
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}

// Extension function to connect AudioPlayerController with AudioPlayerManager
fun AudioPlayerController.connectToAudioPlayerManager(audioPlayerManager: AudioPlayerManager) {
    initializePlayer(audioPlayerManager)
}
