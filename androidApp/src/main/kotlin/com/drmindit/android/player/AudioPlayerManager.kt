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
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class AudioPlayerManager(
    private val context: Context
) {
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
    private var exoPlayer: ExoPlayer? = null
    private var currentSessionId: String? = null
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
                        duration = exoPlayer?.duration?.toInt() ?: 0,
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
    
    fun loadAudio(
        sessionId: String,
        audioUrl: String,
        title: String? = null,
        artist: String? = null,
        artworkUri: Uri? = null
    ) {
        initializePlayer()
        
        currentSessionId = sessionId
        
        // Reset state
        _playerState.value = AudioPlayerState(
            sessionId = sessionId,
            title = title,
            artist = artist,
            artworkUri = artworkUri,
            isBuffering = true
        )
        
        try {
            val mediaSource = createMediaSource(audioUrl)
            
            // Set metadata if available
            title?.let { 
                exoPlayer?.setMediaItem(
                    MediaItem.Builder()
                        .setUri(audioUrl)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(it)
                                .setArtist(artist ?: "")
                                .setArtworkUri(artworkUri)
                                .build()
                        )
                        .build()
                )
            } ?: run {
                exoPlayer?.setMediaSource(mediaSource)
            }
            
            exoPlayer?.prepare()
            
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                error = "Failed to load audio: ${e.message}",
                isBuffering = false
            )
        }
    }
    
    fun play() {
        exoPlayer?.play()
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun stop() {
        exoPlayer?.stop()
        _playerState.value = AudioPlayerState()
        currentSessionId = null
        stopProgressMonitoring()
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        _playerState.value = _playerState.value.copy(currentPosition = position.toInt())
    }
    
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }
    
    fun skipForward(seconds: Int = 10) {
        val newPosition = (exoPlayer?.currentPosition ?: 0) + (seconds * 1000)
        seekTo(newPosition.coerceAtMost(exoPlayer?.duration ?: 0))
    }
    
    fun skipBackward(seconds: Int = 10) {
        val newPosition = (exoPlayer?.currentPosition ?: 0) - (seconds * 1000)
        seekTo(newPosition.coerceAtLeast(0))
    }
    
    fun getCurrentSessionId(): String? = currentSessionId
    
    fun getPlayer(): ExoPlayer? = exoPlayer
    
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
    
    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        
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
    
    private fun stopProgressMonitoring() {
        playbackScope?.cancel()
        playbackScope = null
    }
    
    fun release() {
        stopProgressMonitoring()
        exoPlayer?.release()
        exoPlayer = null
        currentSessionId = null
    }
    
    fun clearError() {
        _playerState.value = _playerState.value.copy(error = null)
    }
}

data class AudioPlayerState(
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

// Composable for easy integration with Jetpack Compose
@Composable
fun rememberAudioPlayerManager(context: Context): AudioPlayerManager {
    return remember { AudioPlayerManager(context) }
}
