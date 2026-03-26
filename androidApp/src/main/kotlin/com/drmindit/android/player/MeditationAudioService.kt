package com.drmindit.android.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
 * Foreground Service for Meditation Audio Playback
 * Ensures proper audio playback with notification controls
 */
@AndroidEntryPoint
class MeditationAudioService : MediaBrowserServiceCompat() {
    
    @Inject
    lateinit var audioPlayerManager: MeditationAudioPlayerManager
    
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "meditation_audio_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MEDIA_SESSION_TAG = "meditation_audio_session"
    }
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("MeditationAudioService: onCreate")
        
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        setupMediaSession()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("MeditationAudioService: onStartCommand - action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_PLAY -> audioPlayerManager.play()
            ACTION_PAUSE -> audioPlayerManager.pause()
            ACTION_STOP -> {
                audioPlayerManager.stop()
                stopSelf()
            }
            ACTION_SEEK -> {
                val position = intent.getLongExtra("position", 0L)
                audioPlayerManager.seekTo(position)
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("empty_root", null)
    }
    
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MeditationAudioService: onDestroy")
        
        audioPlayerManager.release()
        mediaSession.release()
        stopForeground(true)
        stopSelf()
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
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun updateNotification(playerState: MeditationAudioPlayerState) {
        if (!playerState.isPlaying && !playerState.isBuffering) {
            stopForeground(false)
            notificationManager.cancel(NOTIFICATION_ID)
            return
        }
        
        val notification = createNotification(playerState)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun createNotification(playerState: MeditationAudioPlayerState): Notification {
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
            .build()
    }
    
    private fun createPlayPauseAction(isPlaying: Boolean): NotificationCompat.Action {
        val action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val title = if (isPlaying) "Pause" else "Play"
        
        val intent = Intent(this, MeditationAudioService::class.java).apply {
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
        val intent = Intent(this, MeditationAudioService::class.java).apply {
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
    
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Timber.d("MediaSessionCallback: onPlay")
            audioPlayerManager.play()
        }
        
        override fun onPause() {
            Timber.d("MediaSessionCallback: onPause")
            audioPlayerManager.pause()
        }
        
        override fun onStop() {
            Timber.d("MediaSessionCallback: onStop")
            audioPlayerManager.stop()
            stopSelf()
        }
        
        override fun onSeekTo(pos: Long) {
            Timber.d("MediaSessionCallback: onSeekTo - position: $pos")
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
