package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.app.Application

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
            
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean) {
                // Handle play when ready
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
        val currentState = _uiState.value
        if (currentState.isPlaying) {
            exoPlayer.pause()
            _uiState.value = currentState.copy(isPlaying = false)
        } else {
            exoPlayer.play()
            _uiState.value = currentState.copy(isPlaying = true)
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
                _moodBefore.value = 5.0f // Neutral mood before session
                _moodAfter.value = 5.0f // Will be updated after session
                
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
        val newSpeed = when (speed) {
            0.5f -> 0.5f
            0.75f -> 0.75f
            1.0f -> 1.0f
            1.25f -> 1.25f
            1.5f -> 1.5f
            2.0f -> 2.0f
            else -> 1.0f
        }
        _uiState.value = _uiState.value.copy(playbackSpeed = newSpeed)
        exoPlayer.playbackParameters = exoPlayer.playbackParameters
            .buildUpon()
            .setSpeed(newSpeed)
            .build()
    }
    
    fun stop() {
        exoPlayer.stop()
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            currentPosition = 0f
        )
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
        // Here you would mark the session as completed in the repository
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
