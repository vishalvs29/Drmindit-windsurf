package com.drmindit.android.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.drmindit.android.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * Enhanced Foreground Service with proper lifecycle management
 * Fixes service not stopping when audio ends
 */
@AndroidEntryPoint
class EnhancedAudioService : MediaBrowserServiceCompat() {
    
    @Inject
    lateinit var audioPlayerManager: EnhancedAudioPlayerManager
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceActive = false
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "meditation_audio_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MEDIA_SESSION_TAG = "meditation_audio_session"
        private const val WAKE_LOCK_TAG = "DrMindit:AudioWakeLock"
    }
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("🔔 EnhancedAudioService: onCreate")
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        setupMediaSession()
        observePlayerState()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("🔔 onStartCommand - action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_PLAY -> {
                startPlayback()
            }
            ACTION_PAUSE -> {
                pausePlayback()
            }
            ACTION_STOP -> {
                stopPlayback()
                stopSelf()
            }
            ACTION_SEEK -> {
                val position = intent.getLongExtra("position", 0L)
                audioPlayerManager.seekTo(position)
            }
        }
        
        return START_NOT_STICKY // Don't restart if killed
    }
    
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("empty_root", null)
    }
    
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("🔔 EnhancedAudioService: onDestroy")
        
        releaseWakeLock()
        mediaSession.release()
        stopForeground(true)
        stopSelf()
        isServiceActive = false
        
        Timber.d("🔔 Service destroyed")
    }
    
    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG).apply {
            setCallback(MediaSessionCallback())
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            isActive = true
        }
        
        sessionToken = mediaSession.sessionToken
    }
    
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Meditation Audio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio playback controls for meditation sessions"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null) // No sound
                enableVibration(false) // No vibration
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun observePlayerState() {
        audioPlayerManager.playerState.observeForever { state ->
            Timber.d("🔔 Player state changed - playing: ${state.isPlaying}")
            
            if (state.isPlaying || state.isBuffering) {
                startForegroundWithNotification(state)
            } else {
                stopForegroundAndNotification()
            }
        }
    }
    
    private fun startPlayback() {
        Timber.d("🔔 Starting playback")
        
        // Acquire wake lock
        acquireWakeLock()
        
        // Start playback
        audioPlayerManager.play()
        
        // Update service state
        isServiceActive = true
        
        // Start foreground
        val currentState = audioPlayerManager.playerState.value
        startForegroundWithNotification(currentState)
    }
    
    private fun pausePlayback() {
        Timber.d("🔔 Pausing playback")
        
        // Release wake lock
        releaseWakeLock()
        
        // Pause playback
        audioPlayerManager.pause()
        
        // Stop foreground but keep service running
        stopForeground(false)
    }
    
    private fun stopPlayback() {
        Timber.d("🔔 Stopping playback")
        
        // Release wake lock
        releaseWakeLock()
        
        // Stop playback
        audioPlayerManager.stopPlayback()
        
        // Stop foreground and notification
        stopForegroundAndNotification()
        
        // Mark service as inactive
        isServiceActive = false
    }
    
    private fun startForegroundWithNotification(playerState: EnhancedAudioPlayerState) {
        if (!isServiceActive) {
            isServiceActive = true
        }
        
        val notification = createNotification(playerState)
        startForeground(NOTIFICATION_ID, notification)
        
        Timber.d("🔔 Foreground started with notification")
    }
    
    private fun stopForegroundAndNotification() {
        if (isServiceActive) {
            stopForeground(false)
            notificationManager.cancel(NOTIFICATION_ID)
            
            // Auto-stop service if not playing
            val currentState = audioPlayerManager.playerState.value
            if (!currentState.isPlaying && !currentState.isBuffering) {
                Timber.d("🔔 Auto-stopping service")
                stopSelf()
            }
        }
    }
    
    private fun createNotification(playerState: EnhancedAudioPlayerState): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                when {
                    playerState.isPlaying -> PlaybackStateCompat.STATE_PLAYING
                    playerState.isBuffering -> PlaybackStateCompat.STATE_BUFFERING
                    else -> PlaybackStateCompat.STATE_PAUSED
                },
                playerState.currentPosition.toLong(),
                if (playerState.isPlaying) 1.0f else 0.0f
            )
            .build()
        
        mediaSession.setPlaybackState(playbackState)
        
        return MediaNotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setMediaSession(mediaSession.sessionToken)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(playerState.title ?: "Meditation Session")
            .setContentText(playerState.artist ?: "DrMindit")
            .setContentIntent(contentIntent)
            .addAction(createPlayPauseAction(playerState.isPlaying))
            .addAction(createStopAction())
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            .setSilent(true) // No sound
            .setOngoing(false) // Allow dismissal
            .build()
    }
    
    private fun createPlayPauseAction(isPlaying: Boolean): NotificationCompat.Action {
        val action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val title = if (isPlaying) "Pause" else "Play"
        
        val intent = Intent(this, EnhancedAudioService::class.java).apply {
            this.action = action
        }
        
        val pendingIntent = PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }
    
    private fun createStopAction(): NotificationCompat.Action {
        val intent = Intent(this, EnhancedAudioService::class.java).apply {
            action = ACTION_STOP
        }
        
        val pendingIntent = PendingIntent.getService(
            this,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            pendingIntent
        ).build()
    }
    
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                acquire(10*60*1000L // 10 minutes timeout
                )
                Timber.d("🔔 Wake lock acquired")
            }
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) {
                lock.release()
                Timber.d("🔔 Wake lock released")
            }
        }
        wakeLock = null
    }
    
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Timber.d("🔔 MediaSession: onPlay")
            startPlayback()
        }
        
        override fun onPause() {
            Timber.d("🔔 MediaSession: onPause")
            pausePlayback()
        }
        
        override fun onStop() {
            Timber.d("🔔 MediaSession: onStop")
            stopPlayback()
            stopSelf()
        }
        
        override fun onSeekTo(pos: Long) {
            Timber.d("🔔 MediaSession: onSeekTo - position: $pos")
            audioPlayerManager.seekTo(pos)
        }
    }
    
    companion object {
        const val ACTION_PLAY = "com.drmindit.android.player.ACTION_PLAY"
        const val ACTION_PAUSE = "com.drmindit.android.player.ACTION_PAUSE"
        const val ACTION_STOP = "com.drmindit.android.player.ACTION_STOP"
        const val ACTION_SEEK = "com.drmindit.android.player.ACTION_SEEK"
    }
}
