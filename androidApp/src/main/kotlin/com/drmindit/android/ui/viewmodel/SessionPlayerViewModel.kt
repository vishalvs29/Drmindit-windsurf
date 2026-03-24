package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SessionPlayerViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(SessionPlayerUiState())
    val uiState: StateFlow<SessionPlayerUiState> = _uiState.asStateFlow()
    
    fun playPause() {
        _uiState.value = _uiState.value.copy(
            isPlaying = !_uiState.value.isPlaying
        )
    }
    
    fun seekTo(position: Float) {
        _uiState.value = _uiState.value.copy(
            currentPosition = position
        )
    }
    
    fun skipForward(seconds: Int = 10) {
        val newPosition = (_uiState.value.currentPosition + seconds).coerceAtMost(_uiState.value.duration.toFloat())
        _uiState.value = _uiState.value.copy(
            currentPosition = newPosition
        )
    }
    
    fun skipBackward(seconds: Int = 10) {
        val newPosition = (_uiState.value.currentPosition - seconds).coerceAtLeast(0f)
        _uiState.value = _uiState.value.copy(
            currentPosition = newPosition
        )
    }
    
    fun changePlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(
            playbackSpeed = speed
        )
    }
    
    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(
            isFavorite = !_uiState.value.isFavorite
        )
    }
    
    fun completeSession() {
        _uiState.value = _uiState.value.copy(
            isCompleted = true
        )
        // Here you would mark the session as completed in the repository
    }
    
    // Simulate playback progress
    fun startPlaybackProgress() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying && _uiState.value.currentPosition < _uiState.value.duration) {
                kotlinx.coroutines.delay(1000)
                val newPosition = _uiState.value.currentPosition + 1
                _uiState.value = _uiState.value.copy(
                    currentPosition = newPosition
                )
            }
            
            if (_uiState.value.currentPosition >= _uiState.value.duration) {
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    isCompleted = true
                )
            }
        }
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
