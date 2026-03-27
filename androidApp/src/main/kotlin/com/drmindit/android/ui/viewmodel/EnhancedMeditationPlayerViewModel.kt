package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.*
import com.drmindit.android.player.EnhancedAudioPlayerManager
import com.drmindit.android.player.EnhancedAudioPlayerState
import com.drmindit.android.player.EnhancedAudioService
import com.drmindit.shared.domain.model.MeditationSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Enhanced ViewModel with comprehensive lifecycle management
 * Fixes all memory leaks and background playback issues
 */
@HiltViewModel
class EnhancedMeditationPlayerViewModel @Inject constructor(
    private val audioPlayerManager: EnhancedAudioPlayerManager
) : ViewModel() {
    
    // Player state from manager
    val playerState: StateFlow<EnhancedAudioPlayerState> = audioPlayerManager.playerState
    
    // UI state
    private val _uiState = MutableStateFlow(EnhancedMeditationPlayerUiState())
    val uiState: StateFlow<EnhancedMeditationPlayerUiState> = _uiState.asStateFlow()
    
    // Current session
    private val _currentSession = MutableStateFlow<MeditationSession?>(null)
    val currentSession: StateFlow<MeditationSession?> = _currentSession.asStateFlow()
    
    // Lifecycle tracking
    private var isScreenActive = true
    private var lastInteractionTime = System.currentTimeMillis()
    
    init {
        Timber.d("🎵 EnhancedMeditationPlayerViewModel: initialized")
        
        // Observe player state changes
        viewModelScope.launch {
            audioPlayerManager.playerState.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isPlayerReady = state.isReady,
                    hasError = state.error != null,
                    errorMessage = state.error
                )
                
                Timber.d("🎵 Player state updated - playing: ${state.isPlaying}, ready: ${state.isReady}")
            }
        }
    }
    
    /**
     * Load meditation session with validation
     */
    fun loadSession(session: MeditationSession) {
        Timber.d("🎵 Loading session: ${session.id}")
        
        // Stop any existing playback first
        stopPlayback()
        
        _currentSession.value = session
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        session.audioUrl?.let { audioUrl ->
            audioPlayerManager.loadAudio(
                sessionId = session.id,
                audioUrl = audioUrl,
                title = session.title,
                artist = session.instructor,
                artworkUri = session.imageUrl?.let { android.net.Uri.parse(it) }
            )
            
            _uiState.value = _uiState.value.copy(isLoading = false)
            lastInteractionTime = System.currentTimeMillis()
            Timber.d("🎵 Session loaded successfully")
            
        } ?: run {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasError = true,
                errorMessage = "No audio URL available for this session"
            )
            Timber.e("🎵 No audio URL for session: ${session.id}")
        }
    }
    
    /**
     * Start or resume playback
     */
    fun play() {
        Timber.d("🎵 Play requested")
        if (!isScreenActive) {
            Timber.w("🎵 Play requested but screen not active")
            return
        }
        
        audioPlayerManager.play()
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        Timber.d("🎵 Pause requested")
        audioPlayerManager.pause()
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Stop playback completely
     */
    fun stopPlayback() {
        Timber.d("🎵 Stop playback requested")
        audioPlayerManager.stopPlayback()
        _currentSession.value = null
        _uiState.value = EnhancedMeditationPlayerUiState()
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Toggle play/pause with safety
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
     * Seek to position
     */
    fun seekTo(position: Long) {
        Timber.d("🎵 Seek to: $position")
        audioPlayerManager.seekTo(position)
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Skip forward
     */
    fun skipForward(seconds: Int = 10) {
        Timber.d("🎵 Skip forward: $seconds")
        audioPlayerManager.skipForward(seconds)
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Skip backward
     */
    fun skipBackward(seconds: Int = 10) {
        Timber.d("🎵 Skip backward: $seconds")
        audioPlayerManager.skipBackward(seconds)
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        Timber.d("🎵 Set speed: $speed")
        audioPlayerManager.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
        lastInteractionTime = System.currentTimeMillis()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        Timber.d("🎵 Clear error")
        audioPlayerManager.clearError()
        _uiState.value = _uiState.value.copy(hasError = false, errorMessage = null)
    }
    
    /**
     * CRITICAL: Handle user leaving screen - FIXES BACKGROUND PLAYBACK BUG
     */
    fun onUserLeavingScreen() {
        Timber.d("🎵 USER LEAVING SCREEN - CRITICAL CLEANUP")
        
        isScreenActive = false
        
        // IMMEDIATELY stop playback
        audioPlayerManager.forceStopForLifecycle()
        
        // Clear all state
        _currentSession.value = null
        _uiState.value = EnhancedMeditationPlayerUiState()
        
        Timber.d("🎵 Screen exit cleanup completed")
    }
    
    /**
     * Handle screen becoming visible again
     */
    fun onScreenVisible() {
        Timber.d("🎵 Screen visible again")
        isScreenActive = true
        
        // Restore UI state
        val currentState = playerState.value
        _uiState.value = _uiState.value.copy(
            isPlayerReady = currentState.isReady,
            hasError = currentState.error != null,
            errorMessage = currentState.error
        )
    }
    
    /**
     * Handle app going to background
     */
    fun onAppBackgrounded() {
        Timber.d("🎵 App backgrounded")
        
        // Force stop playback when app backgrounds
        if (playerState.value.isPlaying) {
            stopPlayback()
        }
    }
    
    /**
     * Handle app coming to foreground
     */
    fun onAppForegrounded() {
        Timber.d("🎵 App foregrounded")
        
        // Restore state
        val currentState = playerState.value
        _uiState.value = _uiState.value.copy(
            isPlayerReady = currentState.isReady,
            hasError = currentState.error != null,
            errorMessage = currentState.error
        )
    }
    
    /**
     * Handle rapid screen switching
     */
    fun onRapidScreenSwitch() {
        Timber.d("🎵 Rapid screen switch detected")
        
        // Force stop to prevent issues
        if (System.currentTimeMillis() - lastInteractionTime < 1000) {
            stopPlayback()
        }
    }
    
    /**
     * Handle system media button events
     */
    fun handleMediaButtonAction(action: String) {
        Timber.d("🎵 Media button: $action")
        
        if (!isScreenActive) {
            Timber.w("🎵 Media button ignored - screen not active")
            return
        }
        
        when (action) {
            EnhancedAudioService.ACTION_PLAY -> play()
            EnhancedAudioService.ACTION_PAUSE -> pause()
            EnhancedAudioService.ACTION_STOP -> stopPlayback()
            else -> {
                Timber.w("🎵 Unknown media button: $action")
            }
        }
    }
    
    /**
     * Get formatted progress text
     */
    fun getProgressText(): String {
        val state = playerState.value
        return state.formattedCurrentPosition + " / " + state.formattedDuration
    }
    
    /**
     * Check if player is active
     */
    fun isPlayerActive(): Boolean {
        return audioPlayerManager.isPlayerActive()
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? {
        return audioPlayerManager.getCurrentSessionId()
    }
    
    /**
     * Handle edge case - no internet
     */
    fun handleNoInternet() {
        Timber.d("🎵 No internet - stopping playback")
        stopPlayback()
        _uiState.value = _uiState.value.copy(
            hasError = true,
            errorMessage = "No internet connection. Please check your network and try again."
        )
    }
    
    /**
     * Handle edge case - audio URL failure
     */
    fun handleAudioUrlFailure(url: String) {
        Timber.e("🎵 Audio URL failure: $url")
        stopPlayback()
        _uiState.value = _uiState.value.copy(
            hasError = true,
            errorMessage = "Failed to load audio. Please try again later."
        )
    }
    
    /**
     * CRITICAL: ViewModel cleanup - PREVENTS MEMORY LEAKS
     */
    override fun onCleared() {
        super.onCleared()
        Timber.d("🎵 ViewModel onCleared - CRITICAL CLEANUP")
        
        // Force stop playback
        audioPlayerManager.forceStopForLifecycle()
        
        // Release all resources
        audioPlayerManager.release()
        
        // Clear all state
        _currentSession.value = null
        _uiState.value = EnhancedMeditationPlayerUiState()
        isScreenActive = false
        
        Timber.d("🎵 ViewModel cleanup completed - no memory leaks")
    }
}

/**
 * Enhanced UI state with debugging info
 */
data class EnhancedMeditationPlayerUiState(
    val isLoading: Boolean = false,
    val isPlayerReady: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val showControls: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
