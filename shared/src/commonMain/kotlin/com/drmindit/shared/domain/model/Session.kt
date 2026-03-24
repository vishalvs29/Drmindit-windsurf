package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val duration: Int, // in minutes
    val audioUrl: String,
    val imageUrl: String? = null,
    val category: SessionCategory,
    val tags: List<String> = emptyList(),
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val isPremium: Boolean = false,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val difficulty: Difficulty,
    val language: String = "en",
    val transcript: String? = null,
    // Backend-specific fields
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
enum class SessionCategory {
    MINDFULNESS,
    ANXIETY,
    STRESS,
    FOCUS,
    DEPRESSION,
    SLEEP,
    BREATHING,
    MEDITATION,
    YOGA,
    BODY_SCAN
}

@Serializable
enum class Difficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

@Serializable
data class SessionProgress(
    val sessionId: String,
    val userId: String,
    val completedDuration: Int, // in seconds
    val totalDuration: Int, // in seconds
    val isCompleted: Boolean = false,
    val lastPlayedDate: String,
    val playbackSpeed: Float = 1.0f,
    val bookmarkPosition: Int = 0, // in seconds
    val completionDate: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val progressPercentage: Float
        get() = if (totalDuration > 0) {
            (completedDuration.toFloat() / totalDuration) * 100f
        } else 0f
}

@Serializable
data class SessionMetadata(
    val sessionId: String,
    val playCount: Int = 0,
    val averageCompletionRate: Float = 0f,
    val skipRate: Float = 0f,
    val lastPlayedBy: List<String> = emptyList(),
    val popularityScore: Float = 0f,
    val recommendedFor: List<String> = emptyList()
)
