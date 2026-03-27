package com.drmindit.android.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Audio Player Manager with production-grade lifecycle management
 * Fixes all critical issues: memory leaks, background playback, proper cleanup
 */
@Singleton
class EnhancedAudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _playerState = MutableStateFlow(EnhancedAudioPlayerState())
    val playerState: StateFlow<EnhancedAudioPlayerState> = _playerState.asStateFlow()
    
    // Single player instance - prevents multiple instances
    private var exoPlayer: ExoPlayer? = null
    private var currentSessionId: String? = null
    private var progressMonitoringJob: Job? = null
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Lifecycle tracking
    private var isInitialized = false
    private var lastStopTime = 0L
    
    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            trackSelector.buildUponParameters()
                .setMaxVideoSizeSd()
                .setPreferredAudioLanguage("en")
        )
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.d("🎵 Player state: $playbackState")
            
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = true,
                        isPlaying = false
                    )
                }
                Player.STATE_READY -> {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        duration = exoPlayer?.duration?.toInt() ?: 0,
                        isReady = true
                    )
                    Timber.d("🎵 Player ready - duration: ${exoPlayer?.duration}")
                }
                Player.STATE_ENDED -> {
                    Timber.d("🎵 Player ended")
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isCompleted = true,
                        currentPosition = exoPlayer?.duration?.toInt() ?: 0
                    )
                    stopProgressMonitoring()
                    onPlaybackCompleted()
                }
                Player.STATE_IDLE -> {
                    Timber.d("🎵 Player idle")
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        isReady = false
                    )
                }
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.d("🎵 Playing: $isPlaying")
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            
            if (isPlaying) {
                startProgressMonitoring()
            } else {
                stopProgressMonitoring()
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error, "🎵 Player error")
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = error.message ?: "Playback error"
            )
            stopProgressMonitoring()
        }
        
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Timber.d("🎵 Metadata: ${mediaMetadata.title}")
            _playerState.value = _playerState.value.copy(
                title = mediaMetadata.title?.toString(),
                artist = mediaMetadata.artist?.toString(),
                artworkUri = mediaMetadata.artworkUri
            )
        }
    }
    
    /**
     * Initialize player with safety checks
     */
    fun initializePlayer() {
        if (exoPlayer != null) {
            Timber.d("🎵 Player already initialized")
            return
        }
        
        try {
            Timber.d("🎵 Initializing ExoPlayer")
            
            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()
                .apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .build(),
                        true
                    )
                    addListener(playerListener)
                }
            
            isInitialized = true
            Timber.d("🎵 ExoPlayer initialized successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "🎵 Failed to initialize player")
            _playerState.value = _playerState.value.copy(
                error = "Failed to initialize player: ${e.message}"
            )
        }
    }
    
    /**
     * Load audio with validation and cleanup
     */
    fun loadAudio(
        sessionId: String,
        audioUrl: String,
        title: String? = null,
        artist: String? = null,
        artworkUri: Uri? = null
    ) {
        Timber.d("🎵 Loading audio - sessionId: $sessionId")
        
        // Stop any existing playback
        stopPlayback()
        
        // Initialize player if needed
        initializePlayer()
        
        currentSessionId = sessionId
        
        // Reset state
        _playerState.value = EnhancedAudioPlayerState(
            sessionId = sessionId,
            title = title,
            artist = artist,
            artworkUri = artworkUri,
            isBuffering = true,
            error = null,
            isCompleted = false
        )
        
        try {
            val mediaItem = MediaItem.Builder()
                .setUri(audioUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title ?: "Meditation Session")
                        .setArtist(artist ?: "DrMindit")
                        .setArtworkUri(artworkUri)
                        .build()
                )
                .build()
            
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            
            Timber.d("🎵 Audio loaded successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "🎵 Failed to load audio")
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio: ${e.message}",
                isBuffering = false
            )
        }
    }
    
    /**
     * Play with safety checks
     */
    fun play() {
        Timber.d("🎵 Play requested")
        
        exoPlayer?.let { player ->
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
            Timber.d("🎵 Playback started")
        } ?: run {
            Timber.w("🎵 Player not initialized")
            initializePlayer()
            // Retry after initialization
            exoPlayer?.play()
        }
    }
    
    /**
     * Pause with safety checks
     */
    fun pause() {
        Timber.d("🎵 Pause requested")
        
        exoPlayer?.let { player ->
            player.pause()
            Timber.d("🎵 Playback paused")
        } ?: run {
            Timber.w("🎵 Player not initialized")
        }
    }
    
    /**
     * Stop playback - CRITICAL for fixing background playback bug
     */
    fun stopPlayback() {
        Timber.d("🎵 STOP PLAYBACK - CRITICAL")
        
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            Timber.d("🎵 Playback stopped and media cleared")
        }
        
        _playerState.value = EnhancedAudioPlayerState()
        currentSessionId = null
        stopProgressMonitoring()
        lastStopTime = System.currentTimeMillis()
        
        // Stop service if needed
        stopForegroundService()
    }
    
    /**
     * Stop foreground service
     */
    private fun stopForegroundService() {
        try {
            val intent = Intent(context, MeditationAudioService::class.java).apply {
                action = MeditationAudioService.ACTION_STOP
            }
            context.startService(intent)
            Timber.d("🎵 Foreground service stop requested")
        } catch (e: Exception) {
            Timber.e(e, "🎵 Failed to stop foreground service")
        }
    }
    
    /**
     * Seek with validation
     */
    fun seekTo(position: Long) {
        Timber.d("🎵 Seek to: $position")
        
        exoPlayer?.let { player ->
            val clampedPosition = position.coerceIn(0, player.duration)
            player.seekTo(clampedPosition)
            _playerState.value = _playerState.value.copy(currentPosition = clampedPosition.toInt())
            Timber.d("🎵 Seek completed")
        } ?: run {
            Timber.w("🎵 Player not initialized")
        }
    }
    
    /**
     * Release all resources - CRITICAL for memory leak prevention
     */
    fun release() {
        Timber.d("🎵 RELEASING ALL RESOURCES")
        
        stopProgressMonitoring()
        
        exoPlayer?.let { player ->
            player.stop()
            player.release()
            Timber.d("🎵 ExoPlayer released")
        }
        
        exoPlayer = null
        currentSessionId = null
        isInitialized = false
        _playerState.value = EnhancedAudioPlayerState()
        
        playerScope.cancel()
        stopForegroundService()
        
        Timber.d("🎵 Audio player manager fully released")
    }
    
    /**
     * Force stop for lifecycle events
     */
    fun forceStopForLifecycle() {
        Timber.d("🎵 FORCE STOP - Lifecycle event")
        stopPlayback()
    }
    
    /**
     * Check if player is active
     */
    fun isPlayerActive(): Boolean {
        return exoPlayer != null && isInitialized
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? = currentSessionId
    
    // Private helper methods
    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        
        progressMonitoringJob = playerScope.launch {
            Timber.d("🎵 Starting progress monitoring")
            
            while (isActive && _playerState.value.isPlaying) {
                exoPlayer?.let { player ->
                    val currentPosition = player.currentPosition.toInt()
                    val duration = player.duration.toInt()
                    
                    _playerState.value = _playerState.value.copy(
                        currentPosition = currentPosition,
                        duration = duration,
                        progressPercentage = if (duration > 0) {
                            ((currentPosition.toFloat() / duration) * 100).toInt()
                        } else 0
                    )
                }
                
                delay(1000)
            }
            
            Timber.d("🎵 Progress monitoring stopped")
        }
    }
    
    private fun stopProgressMonitoring() {
        progressMonitoringJob?.cancel()
        progressMonitoringJob = null
        Timber.d("🎵 Progress monitoring stopped")
    }
    
    private fun onPlaybackCompleted() {
        Timber.d("🎵 Playback completed")
        
        playerScope.launch {
            delay(1000)
            
            if (_playerState.value.isCompleted) {
                _playerState.value = _playerState.value.copy(isPlaying = false)
                stopForegroundService()
            }
        }
    }
}

/**
 * Enhanced audio player state with debug info
 */
data class EnhancedAudioPlayerState(
    val sessionId: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val artworkUri: Uri? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val isCompleted: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val progressPercentage: Int = 0,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedCurrentPosition: String
        get() = formatTime(currentPosition)
    
    val formattedDuration: String
        get() = formatTime(duration)
    
    private fun formatTime(timeMs: Int): String {
        if (timeMs < 0) return "0:00"
        
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        
        return String.format("%d:%02d", minutes, seconds)
    }
}
