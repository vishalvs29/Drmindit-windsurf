package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.*
import com.drmindit.android.player.MeditationAudioPlayerManager
import com.drmindit.android.player.MeditationAudioPlayerState
import com.drmindit.android.player.MeditationAudioService
import com.drmindit.shared.domain.model.MeditationSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Meditation Audio Player with proper lifecycle management
 * Ensures no memory leaks and proper cleanup when user exits screen
 */
@HiltViewModel
class MeditationPlayerViewModel @Inject constructor(
    private val audioPlayerManager: MeditationAudioPlayerManager
) : ViewModel() {
    
    // Player state from manager
    val playerState: StateFlow<MeditationAudioPlayerState> = audioPlayerManager.playerState
    
    // UI state
    private val _uiState = MutableStateFlow(MeditationPlayerUiState())
    val uiState: StateFlow<MeditationPlayerUiState> = _uiState.asStateFlow()
    
    // Current session
    private val _currentSession = MutableStateFlow<MeditationSession?>(null)
    val currentSession: StateFlow<MeditationSession?> = _currentSession.asStateFlow()
    
    init {
        Timber.d("MeditationPlayerViewModel: initialized")
        
        // Observe player state changes
        viewModelScope.launch {
            audioPlayerManager.playerState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isPlayerReady = state.isReady,
                    hasError = state.error != null,
                    errorMessage = state.error
                )
                
                Timber.d("Player state updated - isPlaying: ${state.isPlaying}, sessionId: ${state.sessionId}")
            }
        }
    }
    
    /**
     * Load meditation session for playback
     */
    fun loadSession(session: MeditationSession) {
        Timber.d("Loading meditation session: ${session.id}")
        
        _currentSession.value = session
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        session.audioUrl?.let { audioUrl ->
            audioPlayerManager.loadAudio(
                sessionId = session.id,
                audioUrl = audioUrl,
                title = session.title,
                artist = session.instructor,
                artworkUri = session.imageUrl?.let { Uri.parse(it) }
            )
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            Timber.d("Session loaded successfully")
        } ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasError = true,
                errorMessage = "No audio URL available for this session"
            )
            Timber.e("No audio URL for session: ${session.id}")
        }
    }
    
    /**
     * Start or resume playback
     */
    fun play() {
        Timber.d("Play requested from ViewModel")
        audioPlayerManager.play()
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        Timber.d("Pause requested from ViewModel")
        audioPlayerManager.pause()
    }
    
    /**
     * Stop playback and cleanup
     */
    fun stop() {
        Timber.d("Stop requested from ViewModel")
        audioPlayerManager.stop()
        _currentSession.value = null
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        Timber.d("Seek to position: $position from ViewModel")
        audioPlayerManager.seekTo(position)
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        Timber.d("Set playback speed: $speed from ViewModel")
        audioPlayerManager.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }
    
    /**
     * Skip forward
     */
    fun skipForward(seconds: Int = 10) {
        Timber.d("Skip forward: $seconds seconds from ViewModel")
        audioPlayerManager.skipForward(seconds)
    }
    
    /**
     * Skip backward
     */
    fun skipBackward(seconds: Int = 10) {
        Timber.d("Skip backward: $seconds seconds from ViewModel")
        audioPlayerManager.skipBackward(seconds)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        Timber.d("Clear error from ViewModel")
        audioPlayerManager.clearError()
        _uiState.value = _uiState.value.copy(hasError = false, errorMessage = null)
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        val currentState = playerState.value
        if (currentState.isPlaying) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Get formatted progress text
     */
    fun getProgressText(): String {
        val state = playerState.value
        return state.formattedProgress
    }
    
    /**
     * Check if player is currently playing
     */
    fun isPlaying(): Boolean {
        return audioPlayerManager.isPlaying()
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? {
        return audioPlayerManager.getCurrentSessionId()
    }
    
    /**
     * Handle user leaving the screen - CRITICAL for fixing the background playback bug
     */
    fun onUserLeavingScreen() {
        Timber.d("User leaving screen - stopping playback")
        
        // Stop playback immediately when user exits
        audioPlayerManager.stop()
        
        // Clear current session
        _currentSession.value = null
        
        // Reset UI state
        _uiState.value = MeditationPlayerUiState()
        
        Timber.d("Screen exit cleanup completed")
    }
    
    /**
     * Handle app going to background
     */
    fun onAppBackgrounded() {
        Timber.d("App going to background")
        
        // Optionally pause playback when app goes to background
        // This can be configured based on user preferences
        val currentState = playerState.value
        if (currentState.isPlaying) {
            // For meditation apps, typically pause when app backgrounds
            // unless user explicitly enabled background play
            pause()
        }
    }
    
    /**
     * Handle app coming to foreground
     */
    fun onAppForegrounded() {
        Timber.d("App coming to foreground")
        
        // Restore state when app comes back to foreground
        val currentState = playerState.value
        _uiState.value = _uiState.value.copy(
            isPlayerReady = currentState.isReady,
            hasError = currentState.error != null,
            errorMessage = currentState.error
        )
    }
    
    /**
     * Handle system media button events
     */
    fun handleMediaButtonAction(action: String) {
        Timber.d("Media button action: $action")
        
        when (action) {
            MeditationAudioService.ACTION_PLAY -> play()
            MeditationAudioService.ACTION_PAUSE -> pause()
            MeditationAudioService.ACTION_STOP -> stop()
            else -> {
                Timber.w("Unknown media button action: $action")
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Timber.d("MeditationPlayerViewModel: onCleared - releasing resources")
        
        // CRITICAL: Release player when ViewModel is cleared
        audioPlayerManager.release()
        
        // Clear all state
        _currentSession.value = null
        _uiState.value = MeditationPlayerUiState()
        
        Timber.d("MeditationPlayerViewModel: cleanup completed")
    }
}

/**
 * UI state for meditation player
 */
data class MeditationPlayerUiState(
    val isLoading: Boolean = false,
    val isPlayerReady: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val showControls: Boolean = true
)
