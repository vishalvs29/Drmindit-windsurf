package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Program(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val category: ProgramCategory,
    val durationDays: Int,
    val difficulty: Difficulty,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val isPremium: Boolean = false,
    val targetAudiences: List<String> = emptyList(),
    val sessions: List<ProgramSession> = emptyList(),
    // Backend-specific fields
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
enum class ProgramCategory {
    ANXIETY_RESET,
    STRESS_MANAGEMENT,
    SLEEP_BETTER,
    FOCUS_BOOST,
    DEPRESSION_SUPPORT,
    MINDFULNESS_FOUNDATION,
    RESILIENCE_BUILDING,
    EMOTIONAL_REGULATION,
    CONFIDENCE_BUILDING,
    RELATIONSHIP_HEALING
}

@Serializable
data class ProgramSession(
    val id: String,
    val dayNumber: Int,
    val sessionId: String,
    val sessionTitle: String,
    val sessionDescription: String,
    val sessionDuration: Int, // in minutes
    val sessionInstructor: String,
    val sessionAudioUrl: String,
    val sessionImageUrl: String? = null,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val completionDate: String? = null,
    val progressPercentage: Float = 0f
)

@Serializable
data class ProgramProgress(
    val userId: String,
    val programId: String,
    val currentDay: Int,
    val completedDays: Int,
    val totalDays: Int,
    val isCompleted: Boolean = false,
    val completionPercentage: Float = 0f,
    val startedAt: String,
    val completedAt: String? = null,
    val lastActiveAt: String,
    val streakDays: Int = 0,
    val missedDays: Int = 0,
    val averageSessionCompletion: Float = 0f
) {
    val isOnTrack: Boolean
        get() = completedDays >= currentDay - 1
    
    val daysRemaining: Int
        get() = totalDays - completedDays
}

@Serializable
data class ProgramMetadata(
    val programId: String,
    val enrollmentCount: Int = 0,
    val completionRate: Float = 0f,
    val averageCompletionTime: Int = 0, // in days
    val dropoutRate: Float = 0f,
    val userSatisfaction: Float = 0f,
    val recommendedFor: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList()
)

@Serializable
data class ProgramRecommendation(
    val program: Program,
    val relevanceScore: Float,
    val reason: String,
    val matchingGoals: List<String>,
    val estimatedBenefit: String
)
