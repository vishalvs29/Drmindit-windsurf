package com.drmindit.android.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerManager(private val context: Context) {
    
    private var audioPlayerService: AudioPlayerService? = null
    private var isBound = false
    
    private val _playerState = MutableStateFlow(AudioPlayerState())
    val playerState: StateFlow<AudioPlayerState> = _playerState.asStateFlow()
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.LocalBinder
            audioPlayerService = binder.getService()
            isBound = true
            
            // Start monitoring playback state
            startPlaybackMonitoring()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            audioPlayerService = null
        }
    }
    
    fun bind() {
        Intent(context, AudioPlayerService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }
    
    fun unbind() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }
    
    fun play(audioUrl: String) {
        if (isBound) {
            audioPlayerService?.playAudio(audioUrl)
            _playerState.value = _playerState.value.copy(
                isPlaying = true,
                audioUrl = audioUrl
            )
        } else {
            // Start service and play
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = AudioPlayerService.ACTION_PLAY
                putExtra(AudioPlayerService.EXTRA_AUDIO_URL, audioUrl)
            }
            context.startService(intent)
        }
    }
    
    fun pause() {
        if (isBound) {
            audioPlayerService?.pauseAudio()
            _playerState.value = _playerState.value.copy(isPlaying = false)
        } else {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = AudioPlayerService.ACTION_PAUSE
            }
            context.startService(intent)
        }
    }
    
    fun stop() {
        if (isBound) {
            audioPlayerService?.stopAudio()
        } else {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = AudioPlayerService.ACTION_STOP
            }
            context.startService(intent)
        }
        
        _playerState.value = AudioPlayerState()
    }
    
    fun seekTo(position: Int) {
        if (isBound) {
            audioPlayerService?.seekTo(position)
            _playerState.value = _playerState.value.copy(currentPosition = position)
        }
    }
    
    private fun startPlaybackMonitoring() {
        // In a real implementation, you would use a timer or coroutine to monitor playback
        // and update the state flow with current position, duration, etc.
    }
    
    data class AudioPlayerState(
        val isPlaying: Boolean = false,
        val currentPosition: Int = 0,
        val duration: Int = 0,
        val audioUrl: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
