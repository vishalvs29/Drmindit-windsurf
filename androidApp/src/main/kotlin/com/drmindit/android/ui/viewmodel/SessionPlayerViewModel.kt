package com.drmindit.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionPlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val exoPlayer = ExoPlayer.Builder(application).build()
    
    private val _uiState = MutableStateFlow(SessionPlayerUiState())
    val uiState: StateFlow<SessionPlayerUiState> = _uiState.asStateFlow()
    
    private val _moodBefore = MutableStateFlow(5.0f) // Default neutral mood
    private val _moodAfter = MutableStateFlow(5.0f) // Default neutral mood
    val moodBefore: StateFlow<Float> = _moodBefore.asStateFlow()
    val moodAfter: StateFlow<Float> = _moodAfter.asStateFlow()
    
    init {
        setupExoPlayer()
    }
    
    private fun setupExoPlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    }
                    Player.STATE_READY -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    }
                    Player.STATE_ENDED -> {
                        _uiState.value = _uiState.value.copy(
                            isPlaying = false,
                            currentPosition = _uiState.value.duration.toFloat(),
                            isCompleted = true
                        )
                    }
                    Player.STATE_IDLE -> {
                        _uiState.value = _uiState.value.copy(isPlaying = false, isLoading = false)
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
            }
            
            override fun onPlayerError(error: PlaybackException) {
                _uiState.value = _uiState.value.copy(
                    error = error.message,
                    isPlaying = false,
                    isLoading = false
                )
            }
        })
        
        // Update current time periodically
        viewModelScope.launch {
            while (true) {
                delay(100) // Update every 100ms
                if (exoPlayer.isPlaying) {
                    _uiState.value = _uiState.value.copy(
                        currentPosition = exoPlayer.currentPosition.toFloat() / 1000f
                    )
                }
            }
        }
    }
    
    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }
    
    fun loadSession(
        title: String,
        audioUrl: String,
        duration: Float = 900f
    ) {
        viewModelScope.launch {
            try {
                // Reset mood tracking for new session
                _moodBefore.value = 5.0f
                _moodAfter.value = 5.0f
                
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    isCompleted = false
                )
                
                val mediaItem = MediaItem.Builder()
                    .setUri(audioUrl)
                    .setMediaId(title)
                    .build()
                
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                
                _uiState.value = _uiState.value.copy(
                    duration = duration.toInt(),
                    currentPosition = 0f,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load session: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun seekTo(position: Float) {
        exoPlayer.seekTo((position * 1000).toLong())
        _uiState.value = _uiState.value.copy(currentPosition = position)
    }
    
    fun skipForward(seconds: Int = 10) {
        val newPosition = (_uiState.value.currentPosition + seconds).coerceAtMost(_uiState.value.duration.toFloat())
        seekTo(newPosition)
    }
    
    fun skipBackward(seconds: Int = 10) {
        val newPosition = (_uiState.value.currentPosition - seconds).coerceAtLeast(0f)
        seekTo(newPosition)
    }
    
    fun changePlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
        exoPlayer.setPlaybackSpeed(speed)
    }
    
    fun stop() {
        exoPlayer.stop()
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            currentPosition = 0f
        )
    }
    
    fun setMoodBefore(mood: Float) {
        _moodBefore.value = mood
    }
    
    fun setMoodAfter(mood: Float) {
        _moodAfter.value = mood
    }
    
    fun release() {
        exoPlayer.release()
    }
    
    override fun onCleared() {
        super.onCleared()
        release()
    }
    
    fun completeSession() {
        _uiState.value = _uiState.value.copy(
            isCompleted = true
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    // Helper properties for UI
    val progress: Float get() = if (_uiState.value.duration > 0) _uiState.value.currentPosition / _uiState.value.duration else 0f
    val formattedCurrentTime: String get() = formatTime(_uiState.value.currentPosition)
    val formattedTotalTime: String get() = formatTime(_uiState.value.duration.toFloat())
    
    private fun formatTime(seconds: Float): String {
        val totalSeconds = seconds.toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}

data class SessionPlayerUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val duration: Int = 600, // 10 minutes default
    val playbackSpeed: Float = 1.0f,
    val isFavorite: Boolean = false,
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
