package com.drmindit.android.data.service

import android.content.Context
import androidx.media3.common.*
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.source.*
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.drmindit.shared.domain.model.AudioQuality
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Audio Streaming Service
 * Handles audio streaming with fallback to local files
 */
@Singleton
class AudioStreamingService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()
    
    private var exoPlayer: ExoPlayer? = null
    private var currentMediaSource: MediaSource? = null
    
    private val trackSelector = DefaultTrackSelector(context).apply {
        setParameters(
            trackSelector.buildUponParameters()
                .setMaxAudioBitrate(getMaxBitrate(AudioQuality.STANDARD))
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
                        isCompleted = true
                    )
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
        }
        
        override fun onPlayerError(error: PlaybackException) {
            _playerState.value = _playerState.value.copy(
                isPlaying = false,
                isBuffering = false,
                error = error.message ?: "Playback error occurred"
            )
        }
    }
    
    /**
     * Initialize ExoPlayer
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
     * Load audio from URL with quality selection
     */
    fun loadAudio(
        url: String,
        quality: AudioQuality = AudioQuality.STANDARD,
        fallbackUrl: String? = null,
        title: String? = null,
        artist: String? = null
    ) {
        initializePlayer()
        
        // Update track selector for quality
        updateTrackSelector(quality)
        
        try {
            val mediaSource = createMediaSource(url)
            
            // Set metadata if available
            title?.let { 
                exoPlayer?.setMediaItem(
                    MediaItem.Builder()
                        .setUri(url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(it)
                                .setArtist(artist ?: "")
                                .build()
                        )
                        .build()
                )
            } ?: run {
                exoPlayer?.setMediaSource(mediaSource)
            }
            
            exoPlayer?.prepare()
            
            _playerState.value = _playerState.value.copy(
                title = title,
                artist = artist,
                isBuffering = true,
                error = null
            )
            
        } catch (e: Exception) {
            // Try fallback URL if available
            if (fallbackUrl != null) {
                loadAudio(fallbackUrl, quality, null, title, artist)
            } else {
                _playerState.value = _playerState.value.copy(
                    error = "Failed to load audio: ${e.message}",
                    isBuffering = false
                )
            }
        }
    }
    
    /**
     * Load local audio file
     */
    fun loadLocalAudio(
        filePath: String,
        title: String? = null,
        artist: String? = null
    ) {
        initializePlayer()
        
        try {
            val mediaSource = ProgressiveMediaSource.Factory(
                DefaultDataSource.Factory(context)
            ).createMediaSource(MediaItem.fromUri("file://$filePath"))
            
            exoPlayer?.setMediaSource(mediaSource)
            exoPlayer?.prepare()
            
            _playerState.value = _playerState.value.copy(
                title = title,
                artist = artist,
                isBuffering = true,
                isLocal = true
            )
            
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                error = "Failed to load local audio: ${e.message}",
                isBuffering = false
            )
        }
    }
    
    /**
     * Play audio
     */
    fun play() {
        exoPlayer?.play()
    }
    
    /**
     * Pause audio
     */
    fun pause() {
        exoPlayer?.pause()
    }
    
    /**
     * Stop audio
     */
    fun stop() {
        exoPlayer?.stop()
        _playerState.value = AudioPlayerState()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    /**
     * Set playback speed
     */
    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }
    
    /**
     * Update audio quality
     */
    fun updateQuality(quality: AudioQuality) {
        updateTrackSelector(quality)
        
        // Reload current media with new quality
        val currentUrl = _playerState.value.audioUrl
        if (currentUrl != null) {
            loadAudio(currentUrl, quality, null, _playerState.value.title, _playerState.value.artist)
        }
    }
    
    /**
     * Get current position
     */
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }
    
    /**
     * Get duration
     */
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }
    
    /**
     * Get buffered position
     */
    fun getBufferedPosition(): Long {
        return exoPlayer?.bufferedPosition ?: 0
    }
    
    /**
     * Release player
     */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
    
    /**
     * Create media source based on URL format
     */
    private fun createMediaSource(url: String): MediaSource {
        val uri = android.net.Uri.parse(url)
        val dataSourceFactory = DefaultDataSource.Factory(context)
        
        return when {
            url.contains(".dash") || url.contains("mpd") -> {
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            url.contains(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            url.contains(".ism") || url.contains("SmoothStreaming") -> {
                SsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            }
        }
    }
    
    /**
     * Update track selector for quality
     */
    private fun updateTrackSelector(quality: AudioQuality) {
        val maxBitrate = getMaxBitrate(quality)
        
        trackSelector.setParameters(
            trackSelector.buildUponParameters()
                .setMaxAudioBitrate(maxBitrate)
                .setPreferredAudioLanguage("en")
        )
    }
    
    /**
     * Get maximum bitrate for quality level
     */
    private fun getMaxBitrate(quality: AudioQuality): Int {
        return when (quality) {
            AudioQuality.LOW -> 64000      // 64kbps
            AudioQuality.STANDARD -> 128000 // 128kbps
            AudioQuality.HIGH -> 256000    // 256kbps
            AudioQuality.PREMIUM -> 320000 // 320kbps
        }
    }
    
    /**
     * Get available audio qualities for current media
     */
    fun getAvailableQualities(): List<AudioQuality> {
        return listOf(AudioQuality.LOW, AudioQuality.STANDARD, AudioQuality.HIGH, AudioQuality.PREMIUM)
    }
    
    /**
     * Check if current media is local
     */
    fun isLocalMedia(): Boolean {
        return _playerState.value.isLocal
    }
    
    /**
     * Get streaming info for current media
     */
    fun getStreamingInfo(): StreamingInfo? {
        return StreamingInfo(
            url = _playerState.value.audioUrl,
            quality = _playerState.value.quality,
            isLocal = _playerState.value.isLocal,
            bitrate = getCurrentBitrate(),
            format = getCurrentFormat()
        )
    }
    
    /**
     * Get current bitrate
     */
    private fun getCurrentBitrate(): Int {
        // This would require accessing the current track's bitrate
        // For now, return estimated based on quality
        return getMaxBitrate(_playerState.value.quality)
    }
    
    /**
     * Get current format
     */
    private fun getCurrentFormat(): String {
        // This would require accessing the current track's format
        // For now, return based on URL
        val url = _playerState.value.audioUrl ?: return "unknown"
        
        return when {
            url.contains(".mp3") -> "MP3"
            url.contains(".aac") -> "AAC"
            url.contains(".m4a") -> "M4A"
            url.contains(".wav") -> "WAV"
            url.contains(".flac") -> "FLAC"
            else -> "unknown"
        }
    }
}

/**
 * Audio player state
 */
data class AudioPlayerState(
    val title: String? = null,
    val artist: String? = null,
    val audioUrl: String? = null,
    val quality: AudioQuality = AudioQuality.STANDARD,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val isCompleted: Boolean = false,
    val isLocal: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val error: String? = null
) {
    val formattedCurrentPosition: String
        get() = formatTime(currentPosition)
    
    val formattedDuration: String
        get() = formatTime(duration)
    
    private fun formatTime(timeMs: Long): String {
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

/**
 * Streaming information
 */
data class StreamingInfo(
    val url: String?,
    val quality: AudioQuality,
    val isLocal: Boolean,
    val bitrate: Int,
    val format: String
)

/**
 * Download progress
 */
data class DownloadProgress(
    val url: String,
    val progress: Int,
    val total: Int,
    val message: String,
    val error: String? = null
)
