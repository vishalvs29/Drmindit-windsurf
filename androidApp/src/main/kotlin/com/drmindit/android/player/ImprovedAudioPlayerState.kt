package com.drmindit.android.player

/**
 * Improved Audio Player State with proper data management
 */
data class ImprovedAudioPlayerState(
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
    val error: String? = null,
    val isBackgroundPlayback: Boolean = false
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
