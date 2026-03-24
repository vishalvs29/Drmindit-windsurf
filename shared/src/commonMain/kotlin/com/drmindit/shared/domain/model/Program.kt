package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Program(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: ProgramCategory,
    val duration: Int, // days
    val difficulty: Difficulty,
    val sessions: List<ProgramSession>,
    val isPremium: Boolean = false,
    val rating: Float,
    val totalRatings: Int,
    val targetAudience: List<UserType>
)

@Serializable
enum class ProgramCategory {
    ANXIETY_RESET,
    STRESS_MANAGEMENT,
    SLEEP_BETTER,
    FOCUS_BOOST,
    DEPRESSION_SUPPORT,
    MINDFULNESS_FOUNDATION,
    RESILIENCE_BUILDING
}

@Serializable
data class ProgramSession(
    val day: Int,
    val sessionId: String,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)

@Serializable
data class ProgramProgress(
    val programId: String,
    val userId: String,
    val currentDay: Int,
    val completedDays: Int,
    val totalDays: Int,
    val startDate: String,
    val lastActiveDate: String,
    val isCompleted: Boolean,
    val completionPercentage: Float
)
