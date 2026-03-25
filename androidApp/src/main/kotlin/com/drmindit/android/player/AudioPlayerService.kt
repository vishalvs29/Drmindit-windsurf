package com.drmindit.android.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.drmindit.android.R
import kotlinx.coroutines.*

class AudioPlayerService : Service() {
    
    private val binder = LocalBinder()
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var notificationManager: NotificationManagerCompat
    
    private var serviceScope: CoroutineScope? = null
    private var currentNotification: Notification? = null
    
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
                audioPlayerManager.pause()
            }
        }
    }
    
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
        fun getAudioPlayerManager(): AudioPlayerManager = audioPlayerManager
    }
    
    override fun onCreate() {
        super.onCreate()
        
        audioPlayerManager = AudioPlayerManager(this)
        exoPlayer = ExoPlayer.Builder(this).build()
        notificationManager = NotificationManagerCompat.from(this)
        
        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        
        // Register for audio becoming noisy
        registerReceiver(
            noisyReceiver,
            IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
        
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                val audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE)
                val artist = intent.getStringExtra(EXTRA_ARTIST)
                
                loadAndPlay(sessionId, audioUrl, title, artist)
            }
            ACTION_PAUSE -> {
                audioPlayerManager.pause()
            }
            ACTION_STOP -> {
                audioPlayerManager.stop()
                stopForeground(true)
                stopSelf()
            }
            ACTION_SEEK -> {
                val position = intent.getLongExtra(EXTRA_POSITION, 0L)
                audioPlayerManager.seekTo(position)
            }
            ACTION_SET_SPEED -> {
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                audioPlayerManager.setPlaybackSpeed(speed)
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun loadAndPlay(
        sessionId: String,
        audioUrl: String,
        title: String? = null,
        artist: String? = null
    ) {
        audioPlayerManager.loadAudio(sessionId, audioUrl, title, artist)
        audioPlayerManager.play()
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    private fun createNotification(): Notification {
        val playerState = audioPlayerManager.playerState.value
        
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(playerState.title ?: "DrMindit Session")
            .setContentText(playerState.artist ?: "Meditation")
            .setSubText(playerState.formattedProgress)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(
                R.drawable.ic_skip_backward,
                "Skip Backward",
                createPendingIntent(ACTION_SKIP_BACKWARD)
            )
            .addAction(
                if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (playerState.isPlaying) "Pause" else "Play",
                createPendingIntent(if (playerState.isPlaying) ACTION_PAUSE else ACTION_PLAY)
            )
            .addAction(
                R.drawable.ic_skip_forward,
                "Skip Forward",
                createPendingIntent(ACTION_SKIP_FORWARD)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(1)
                    .setMediaSession(exoPlayer.sessionActivityToken)
            )
        
        currentNotification = builder.build()
        return builder.build()
    }
    
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlayerService::class.java).apply {
            this.action = action
        }
        
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun updateNotification() {
        currentNotification?.let { notification ->
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current audio playback status"
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        serviceScope?.cancel()
        unregisterReceiver(noisyReceiver)
        
        audioPlayerManager.release()
        exoPlayer.release()
        
        stopForeground(true)
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "audio_playback"
        
        const val ACTION_PLAY = "com.drmindit.android.PLAY"
        const val ACTION_PAUSE = "com.drmindit.android.PAUSE"
        const val ACTION_STOP = "com.drmindit.android.STOP"
        const val ACTION_SKIP_FORWARD = "com.drmindit.android.SKIP_FORWARD"
        const val ACTION_SKIP_BACKWARD = "com.drmindit.android.SKIP_BACKWARD"
        const val ACTION_SEEK = "com.drmindit.android.SEEK"
        const val ACTION_SET_SPEED = "com.drmindit.android.SET_SPEED"
        
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_POSITION = "position"
        const val EXTRA_SPEED = "speed"
        
        fun startPlayback(
            context: Context,
            sessionId: String,
            audioUrl: String,
            title: String? = null,
            artist: String? = null
        ) {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun pausePlayback(context: Context) {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }
        
        fun stopPlayback(context: Context) {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
