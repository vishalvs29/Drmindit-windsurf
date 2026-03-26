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
 * Production-Grade Meditation Audio Player Manager
 * Handles audio playback with proper lifecycle management and no memory leaks
 */
@Singleton
class MeditationAudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _playerState = MutableStateFlow(MeditationAudioPlayerState())
    val playerState: StateFlow<MeditationAudioPlayerState> = _playerState.asStateFlow()
    
    private var exoPlayer: ExoPlayer? = null
    private var currentSessionId: String? = null
    private var progressMonitoringJob: Job? = null
    private val playerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            trackSelector.buildUponParameters()
                .setMaxVideoSizeSd()
                .setPreferredAudioLanguage("en")
        )
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.d("Player state changed to: $playbackState")
            
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
                    Timber.d("Player ready - duration: ${exoPlayer?.duration}")
                }
                Player.STATE_ENDED -> {
                    Timber.d("Player ended")
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isCompleted = true,
                        currentPosition = exoPlayer?.duration?.toInt() ?: 0
                    )
                    stopProgressMonitoring()
                    onPlaybackCompleted()
                }
                Player.STATE_IDLE -> {
                    Timber.d("Player idle")
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        isReady = false
                    )
                }
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Timber.d("IsPlaying changed to: $isPlaying")
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            
            if (isPlaying) {
                startProgressMonitoring()
            } else {
                stopProgressMonitoring()
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error, "Player error occurred")
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = error.message ?: "Playback error occurred"
            )
            stopProgressMonitoring()
        }
        
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Timber.d("Media metadata changed: ${mediaMetadata.title}")
            _playerState.value = _playerState.value.copy(
                title = mediaMetadata.title?.toString(),
                artist = mediaMetadata.artist?.toString(),
                artworkUri = mediaMetadata.artworkUri
            )
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            Timber.d("Position discontinuity - reason: $reason")
            _playerState.value = _playerState.value.copy(
                currentPosition = exoPlayer?.currentPosition?.toInt() ?: 0
            )
        }
    }
    
    /**
     * Initialize the ExoPlayer instance
     */
    fun initializePlayer() {
        if (exoPlayer == null) {
            Timber.d("Initializing ExoPlayer")
            
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
        }
    }
    
    /**
     * Load audio for meditation session
     */
    fun loadAudio(
        sessionId: String,
        audioUrl: String,
        title: String? = null,
        artist: String? = null,
        artworkUri: Uri? = null
    ) {
        Timber.d("Loading audio - sessionId: $sessionId, audioUrl: $audioUrl")
        
        initializePlayer()
        
        currentSessionId = sessionId
        
        // Reset state
        _playerState.value = MeditationAudioPlayerState(
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
            
            Timber.d("Audio loaded successfully")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load audio")
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio: ${e.message}",
                isBuffering = false
            )
        }
    }
    
    /**
     * Start or resume playback
     */
    fun play() {
        Timber.d("Play requested")
        
        exoPlayer?.let { player ->
            if (player.playbackState == Player.STATE_ENDED) {
                // Restart from beginning if ended
                player.seekTo(0)
            }
            player.play()
            Timber.d("Playback started")
        } ?: run {
            Timber.w("Player not initialized when play requested")
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        Timber.d("Pause requested")
        
        exoPlayer?.let { player ->
            player.pause()
            Timber.d("Playback paused")
        } ?: run {
            Timber.w("Player not initialized when pause requested")
        }
    }
    
    /**
     * Stop playback and reset player
     */
    fun stop() {
        Timber.d("Stop requested")
        
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            Timber.d("Playback stopped and media cleared")
        }
        
        _playerState.value = MeditationAudioPlayerState()
        currentSessionId = null
        stopProgressMonitoring()
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        Timber.d("Seek to position: $position")
        
        exoPlayer?.let { player ->
            player.seekTo(position)
            _playerState.value = _playerState.value.copy(currentPosition = position.toInt())
            Timber.d("Seek completed")
        } ?: run {
            Timber.w("Player not initialized when seek requested")
        }
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        Timber.d("Set playback speed: $speed")
        
        exoPlayer?.let { player ->
            player.setPlaybackSpeed(speed)
            _playerState.value = _playerState.value.copy(playbackSpeed = speed)
            Timber.d("Playback speed set")
        } ?: run {
            Timber.w("Player not initialized when speed change requested")
        }
    }
    
    /**
     * Skip forward by specified seconds
     */
    fun skipForward(seconds: Int = 10) {
        Timber.d("Skip forward: $seconds seconds")
        
        exoPlayer?.let { player ->
            val newPosition = player.currentPosition + (seconds * 1000)
            val clampedPosition = newPosition.coerceAtMost(player.duration)
            seekTo(clampedPosition)
            Timber.d("Skip forward completed")
        } ?: run {
            Timber.w("Player not initialized when skip forward requested")
        }
    }
    
    /**
     * Skip backward by specified seconds
     */
    fun skipBackward(seconds: Int = 10) {
        Timber.d("Skip backward: $seconds seconds")
        
        exoPlayer?.let { player ->
            val newPosition = player.currentPosition - (seconds * 1000)
            val clampedPosition = newPosition.coerceAtLeast(0)
            seekTo(clampedPosition)
            Timber.d("Skip backward completed")
        } ?: run {
            Timber.w("Player not initialized when skip backward requested")
        }
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? = currentSessionId
    
    /**
     * Get ExoPlayer instance (for service integration)
     */
    fun getPlayer(): ExoPlayer? = exoPlayer
    
    /**
     * Check if player is currently playing
     */
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }
    
    /**
     * Start monitoring playback progress
     */
    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        
        progressMonitoringJob = playerScope.launch {
            Timber.d("Starting progress monitoring")
            
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
                
                delay(1000) // Update every second
            }
            
            Timber.d("Progress monitoring stopped")
        }
    }
    
    /**
     * Stop monitoring playback progress
     */
    private fun stopProgressMonitoring() {
        progressMonitoringJob?.cancel()
        progressMonitoringJob = null
        Timber.d("Progress monitoring stopped")
    }
    
    /**
     * Handle playback completion
     */
    private fun onPlaybackCompleted() {
        Timber.d("Playback completed for session: $currentSessionId")
        
        // Auto-stop and cleanup after completion
        playerScope.launch {
            delay(1000) // Brief delay to ensure final state is updated
            
            if (_playerState.value.isCompleted) {
                // Don't auto-stop immediately to allow user to restart
                // But ensure service is notified
                _playerState.value = _playerState.value.copy(
                    isPlaying = false
                )
            }
        }
    }
    
    /**
     * Release all resources and cleanup
     */
    fun release() {
        Timber.d("Releasing audio player resources")
        
        stopProgressMonitoring()
        
        exoPlayer?.let { player ->
            player.stop()
            player.release()
            Timber.d("ExoPlayer released")
        }
        
        exoPlayer = null
        currentSessionId = null
        _playerState.value = MeditationAudioPlayerState()
        
        playerScope.cancel()
        Timber.d("Audio player manager released")
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _playerState.value = _playerState.value.copy(error = null)
        Timber.d("Error state cleared")
    }
    
    /**
     * Create media source from URL
     */
    private fun createMediaSource(audioUrl: String): MediaSource {
        val uri = Uri.parse(audioUrl)
        val dataSourceFactory = DefaultDataSource.Factory(context)
        
        return when {
            audioUrl.contains(".dash") || audioUrl.contains("mpd") -> {
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            audioUrl.contains(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            audioUrl.contains(".ism") || audioUrl.contains("SmoothStreaming") -> {
                SsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
        }
    }
}

/**
 * Enhanced audio player state with additional metadata
 */
data class MeditationAudioPlayerState(
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
    val playbackSpeed: Float = 1.0f,
    val error: String? = null
) {
    val formattedCurrentPosition: String
        get() = formatTime(currentPosition)
    
    val formattedDuration: String
        get() = formatTime(duration)
    
    val formattedProgress: String
        get() = "$formattedCurrentPosition / $formattedDuration"
    
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
