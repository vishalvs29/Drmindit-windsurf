package com.drmindit.android.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.drmindit.android.MainActivity

class AudioPlayerService : Service(), MediaPlayer.OnCompletionListener {
    
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioUrl: String? = null
    private var isPlaying = false
    
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL)
                audioUrl?.let { playAudio(it) }
            }
            ACTION_PAUSE -> pauseAudio()
            ACTION_STOP -> stopAudio()
        }
        return START_NOT_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback notifications"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DrMindit")
            .setContentText("Playing meditation session")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    fun playAudio(audioUrl: String) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setOnCompletionListener(this@AudioPlayerService)
                }
            }
            
            if (currentAudioUrl != audioUrl) {
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(audioUrl)
                mediaPlayer?.prepare()
                currentAudioUrl = audioUrl
            }
            
            mediaPlayer?.start()
            isPlaying = true
            
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        stopForeground(false)
    }
    
    fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentAudioUrl = null
        isPlaying = false
        stopForeground(true)
        stopSelf()
    }
    
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
    
    fun isCurrentlyPlaying(): Boolean = isPlaying
    
    override fun onCompletion(mp: MediaPlayer?) {
        // Handle playback completion
        stopAudio()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
    }
    
    companion object {
        const val ACTION_PLAY = "com.drmindit.android.PLAY"
        const val ACTION_PAUSE = "com.drmindit.android.PAUSE"
        const val ACTION_STOP = "com.drmindit.android.STOP"
        const val EXTRA_AUDIO_URL = "audio_url"
        
        private const val CHANNEL_ID = "audio_player_channel"
        private const val NOTIFICATION_ID = 1
    }
}
