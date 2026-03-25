package com.drmindit.android.player

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Improved Audio Player Manager using Android Media3/ExoPlayer
 * Provides stable audio playback with proper lifecycle management
 */
@Singleton
class ImprovedAudioPlayerManager @Inject constructor(
    private val context: Context
) {
    
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()
    
    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playbackScope: CoroutineScope? = null
    
    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            trackSelector.buildUponParameters()
                .setMaxVideoSizeSd()
                .setPreferredAudioLanguage("en")
        )
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
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
                        isReady = true
                    )
                }
                Player.STATE_ENDED -> {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isCompleted = true,
                        currentPosition = exoPlayer?.duration?.toInt() ?: 0
                    )
                    stopProgressMonitoring()
                }
                Player.STATE_IDLE -> {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        isReady = false
                    )
                }
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            if (isPlaying) {
                startProgressMonitoring()
            } else {
                stopProgressMonitoring()
            }
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = error.message ?: "Playback error occurred"
            )
            stopProgressMonitoring()
        }
        
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _playerState.value = _playerState.value.copy(
                title = mediaMetadata.title?.toString(),
                artist = mediaMetadata.artist?.toString(),
                artworkUri = mediaMetadata.artworkUri
            )
        }
    }
    
    /**
     * Initialize the ExoPlayer with proper setup
     */
    fun initializePlayer() {
        if (exoPlayer == null) {
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
     * Load audio session with proper lifecycle management
     */
    fun loadAudio(
        sessionId: String,
        audioUrl: String,
        title: String? = null,
        artist: String? = null,
        artworkUri: Uri? = null,
        lifecycleOwner: LifecycleOwner? = null
    ) {
        initializePlayer()
        
        _currentSessionId.value = sessionId
        
        // Reset state
        _playerState.value = AudioPlayerState(
            sessionId = sessionId,
            title = title,
            artist = artist,
            artworkUri = artworkUri,
            isBuffering = true,
            isReady = false,
            isPlaying = false,
            isCompleted = false,
            currentPosition = 0,
            duration = 0
        )
        
        try {
            val mediaSource = createMediaSource(audioUrl)
            
            // Set metadata if available
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(audioUrl))
                .apply {
                    title?.let { setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(it)
                            .setArtist(artist ?: "")
                            .setArtworkUri(artworkUri)
                            .build()
                    )
                }
                }
                .build()
            
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            
            // Set up media session for background playback
            setupMediaSession(sessionId, title)
            
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio: ${e.message}",
                isBuffering = false
            )
        }
    }
    
    /**
     * Set up media session for background playback
     */
    private fun setupMediaSession(sessionId: String, title: String?) {
        mediaSession?.release()
        
        mediaSession = MediaSession.Builder(context, "DrMinditAudioPlayer")
            .setSessionActivity(MediaSession.SessionActivity.SUPPORTS_CONTROLS)
            .build()
            .apply {
                setCallback(object : MediaSession.Callback {
                    override fun onPlay() {
                        exoPlayer?.play()
                    }
                    
                    override fun onPause() {
                        exoPlayer?.pause()
                    }
                    
                    override fun onStop() {
                        exoPlayer?.stop()
                        _playerState.value = AudioPlayerState()
                        _currentSessionId.value = null
                    }
                    
                    override fun onSeekTo(position: Long) {
                        exoPlayer?.seekTo(position)
                    }
                    
                    override fun onSkipToNext() {
                        // Not implemented for single session
                    }
                    
                    override fun onSkipToPrevious() {
                        // Not implemented for single session
                    }
                })
            }
        
        mediaSession?.isActive = true
    }
    
    /**
     * Start playback
     */
    fun play() {
        exoPlayer?.play()
        mediaSession?.isActive = true
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        exoPlayer?.pause()
        mediaSession?.isActive = true
    }
    
    /**
     * Stop playback and clean up
     */
    fun stop() {
        exoPlayer?.stop()
        mediaSession?.isActive = false
        _playerState.value = AudioPlayerState()
        _currentSessionId.value = null
        stopProgressMonitoring()
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        _playerState.value = _playerState.value.copy(currentPosition = position.toInt())
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }
    
    /**
     * Skip forward by specified seconds
     */
    fun skipForward(seconds: Int = 10) {
        val newPosition = (exoPlayer?.currentPosition ?: 0) + (seconds * 1000)
        seekTo(newPosition.coerceAtMost(exoPlayer?.duration ?: 0))
    }
    
    /**
     * Skip backward by specified seconds
     */
    fun skipBackward(seconds: Int = 10) {
        val newPosition = (exoPlayer?.currentPosition ?: 0) - (seconds * 1000)
        seekTo(newPosition.coerceAtLeast(0))
    }
    
    /**
     * Get current session ID
     */
    fun getCurrentSessionId(): String? = _currentSessionId.value
    
    /**
     * Get ExoPlayer instance for UI components
     */
    fun getPlayer(): ExoPlayer? = exoPlayer
    
    /**
     * Bind to lifecycle for proper cleanup
     */
    fun bindToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(object : LifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                release()
            }
        })
    }
    
    /**
     * Create media source based on URL format
     */
    private fun createMediaSource(audioUrl: String): MediaSource {
        val uri = Uri.parse(audioUrl)
        val dataSourceFactory = DefaultDataSource.Factory()
        
        return when {
            audioUrl.contains(".dash") -> {
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
    
    /**
     * Start progress monitoring
     */
    private fun startProgressMonitoring() {
        playbackScope = CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            while (isActive && _playerState.value.isPlaying) {
                delay(1000) // Update every second
                
                exoPlayer?.let { player ->
                    val currentPosition = player.currentPosition.toInt()
                    val duration = player.duration.toInt()
                    
                    _playerState.value = _playerState.value.copy(
                        currentPosition = currentPosition,
                        duration = duration,
                        progressPercentage = if (duration > 0) {
                            (currentPosition.toFloat() / duration * 100).roundToInt()
                        } else 0
                    )
                }
            }
        }
    }
    
    /**
     * Stop progress monitoring
     */
    private fun stopProgressMonitoring() {
        playbackScope?.cancel()
        playbackScope = null
    }
    
    /**
     * Release all resources
     */
    fun release() {
        stopProgressMonitoring()
        mediaSession?.release()
        mediaSession = null
        exoPlayer?.release()
        exoPlayer = null
    }
}
