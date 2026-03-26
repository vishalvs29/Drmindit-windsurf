package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Enhanced Meditation Session model with audio support
 */
@Serializable
data class MeditationSession(
    val id: String,
    val title: String,
    val description: String,
    val instructorName: String,
    val instructorBio: String,
    val duration: Int, // in minutes
    val audioUrl: String?, // Streaming audio URL
    val localAudioPath: String?, // Local fallback path
    val thumbnailUrl: String,
    val category: SessionCategory,
    val difficulty: DifficultyLevel,
    val tags: List<String>,
    val transcript: String?, // Session transcript
    val keyPoints: List<String>, // Key takeaways
    val breathingInstructions: String?, // Specific breathing guidance
    val isPremium: Boolean = false,
    val downloadSize: Int? = null, // Size in bytes for local download
    val streamingQuality: AudioQuality = AudioQuality.STANDARD,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val viewCount: Int = 0,
    val rating: Float = 0f,
    val reviewCount: Int = 0
)

/**
 * Session categories
 */
@Serializable
enum class SessionCategory {
    MINDFULNESS,
    BREATHING,
    BODY_SCAN,
    LOVING_KINDNESS,
    STRESS_RELIEF,
    SLEEP,
    FOCUS,
    ANXIETY,
    PAIN_MANAGEMENT,
    EMOTIONAL_REGULATION,
    SELF_COMPASSION,
    GRATITUDE,
    WALKING_MEDITATION,
    WORK_MEDITATION,
    RELATIONSHIP_MEDITATION
}

/**
 * Difficulty levels
 */
@Serializable
enum class DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    ALL_LEVELS
}

/**
 * Audio quality options
 */
@Serializable
enum class AudioQuality {
    LOW,      // 64kbps
    STANDARD, // 128kbps
    HIGH,     // 256kbps
    PREMIUM   // 320kbps
}

/**
 * Meditation program structure
 */
@Serializable
data class MeditationProgram(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // Total program duration in days
    val category: ProgramCategory,
    val difficulty: DifficultyLevel,
    val instructorName: String,
    val instructorBio: String,
    val thumbnailUrl: String,
    val sessions: List<ProgramSession>,
    val benefits: List<String>,
    val requirements: List<String>,
    val isPremium: Boolean = false,
    val price: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val enrollmentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Program session with day and order
 */
@Serializable
data class ProgramSession(
    val day: Int,
    val order: Int,
    val sessionId: String,
    val title: String,
    val description: String,
    val duration: Int, // in minutes
    val objectives: List<String>,
    val prerequisites: List<String> = emptyList(),
    val isOptional: Boolean = false
)

/**
 * Program categories
 */
@Serializable
enum class ProgramCategory {
    FOUNDATION,      // 21-day foundational program
    STRESS_MANAGEMENT,
    SLEEP_IMPROVEMENT,
    ANXIETY_RELIEF,
    FOCUS_ENHANCEMENT,
    EMOTIONAL_WELLBEING,
    RELATIONSHIP_BUILDING,
    PAIN_MANAGEMENT,
    WORKPLACE_WELLNESS,
    ADVANCED_PRACTICE
}

/**
 * User progress tracking
 */
@Serializable
data class SessionProgress(
    val sessionId: String,
    val userId: String,
    val completedAt: Long?,
    val duration: Int, // Actual time spent in minutes
    val completionPercentage: Float, // 0-100
    val rating: Int?, // 1-5 stars
    val notes: String?,
    val favorite: Boolean = false,
    val downloaded: Boolean = false,
    val localPath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Program progress tracking
 */
@Serializable
data class ProgramProgress(
    val programId: String,
    val userId: String,
    val currentDay: Int,
    val completedDays: List<Int>,
    val startedAt: Long,
    val lastActivityAt: Long,
    val estimatedCompletion: Long?,
    val streak: Int, // Current consecutive days
    val longestStreak: Int,
    val totalMinutesSpent: Int,
    val averageSessionDuration: Float,
    val completionPercentage: Float,
    val rating: Int?,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Content bundle for offline access
 */
@Serializable
data class ContentBundle(
    val id: String,
    val title: String,
    val description: String,
    val sessions: List<String>, // Session IDs
    val totalSize: Long, // Total bundle size in bytes
    val downloadUrl: String,
    val version: String,
    val isDownloaded: Boolean = false,
    val localPath: String?,
    val downloadedAt: Long?,
    val lastUpdated: Long = System.currentTimeMillis()
)
