package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val duration: Int, // minutes
    val audioUrl: String,
    val imageUrl: String? = null,
    val category: SessionCategory,
    val tags: List<String>,
    val rating: Float,
    val totalRatings: Int,
    val isPremium: Boolean = false,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val difficulty: Difficulty,
    val language: String = "en",
    val transcript: String? = null
)

@Serializable
enum class SessionCategory {
    SLEEP,
    ANXIETY,
    STRESS,
    FOCUS,
    DEPRESSION,
    MINDFULNESS,
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
    val completedDuration: Int, // seconds
    val totalDuration: Int, // seconds
    val isCompleted: Boolean,
    val lastPlayedDate: String,
    val playbackSpeed: Float = 1.0f
)
