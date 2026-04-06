package com.drmindit.shared.domain.program

import kotlinx.serialization.Serializable
import com.drmindit.shared.domain.model.*

/**
 * Structured Program definitions
 * Consolidated from multiple files to avoid redeclarations
 */

@Serializable
data class StructuredProgram(
    val id: String,
    val name: String,
    val description: String,
    val category: ProgramCategory,
    val duration: ProgramDuration,
    val difficulty: ProgramDifficulty,
    val steps: List<ProgramStep>,
    val tags: List<String> = emptyList()
)

@Serializable
data class ProgramProgress(
    val currentStepIndex: Int,
    val completedSteps: Set<Int>,
    val stepProgress: Map<Int, StepProgress>,
    val startDate: Long,
    val lastAccessDate: Long
)

@Serializable
data class StepProgress(
    val stepId: String,
    val isStarted: Boolean,
    val isCompleted: Boolean,
    val timeSpentMinutes: Int,
    val completionDate: Long? = null
)

@Serializable
data class ProgramStep(
    val id: String,
    val day: Int,
    val title: String,
    val description: String,
    val type: StepType,
    val exercise: ExerciseDefinition? = null,
    val reflection: ReflectionDefinition? = null,
    val audio: AudioDefinition? = null,
    val durationMinutes: Int
)

// Enums - single declarations
@Serializable
enum class StepType {
    INSTRUCTION,     // Educational content
    EXERCISE,        // Interactive exercise
    REFLECTION,      // Self-reflection prompt
    BREATHING,       // Breathing exercise
    MEDITATION,      // Meditation practice
    ASSESSMENT       // Progress assessment
}

@Serializable
enum class ProgramCategory {
    ANXIETY, STRESS, FOCUS, SLEEP, TRAUMA, RESILIENCE, BURNOUT, MINDFULNESS
}

@Serializable
enum class ProgramDuration {
    DAYS_7, DAYS_14, DAYS_21, DAYS_30, ONGOING
}

@Serializable
enum class ProgramDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

@Serializable
enum class ExerciseType {
    BREATHING,
    MEDITATION,
    GROUNDING,
    COGNITIVE_RESTRUCTURING,
    PROGRESSIVE_MUSCLE_RELAXATION,
    BODY_SCAN,
    VISUALIZATION,
    MINDFUL_MOVEMENT
}

@Serializable
enum class ExerciseDifficulty {
    EASY, MODERATE, CHALLENGING
}

@Serializable
enum class VoiceGender {
    MALE, FEMALE, NEUTRAL
}

@Serializable
enum class AudioType {
    GUIDED_MEDITATION,
    BREATHING_EXERCISE,
    SLEEP_STORY,
    BODY_SCAN,
    VISUALIZATION
}

@Serializable
enum class ReflectionType {
    OPEN_ENDED,
    RATING_SCALE,
    MULTIPLE_CHOICE,
    CHECKLIST,
    BOOLEAN
}

@Serializable
enum class ResponseFormat {
    TEXT, RATING, SELECTION, CHECKLIST, BOOLEAN
}

@Serializable
enum class SessionType {
    DAILY_PRACTICE,
    PROGRAM_STEP,
    REFLECTION,
    CRISIS_INTERVENTION
}

@Serializable
enum class RecommendationPriority {
    HIGH, MEDIUM, LOW
}

// Supporting data classes
@Serializable
data class ExerciseDefinition(
    val type: ExerciseType,
    val instructions: String,
    val durationMinutes: Int,
    val difficulty: ExerciseDifficulty,
    val voiceGender: VoiceGender = VoiceGender.NEUTRAL
)

@Serializable
data class ExerciseStep(
    val stepNumber: Int,
    val instruction: String,
    val durationSeconds: Int,
    val audioCue: String? = null
)

@Serializable
data class ReflectionDefinition(
    val prompt: String,
    val type: ReflectionType,
    val responseFormat: ResponseFormat,
    val options: List<ReflectionOption> = emptyList()
)

@Serializable
data class ReflectionOption(
    val id: String,
    val text: String,
    val value: String
)

@Serializable
data class AudioDefinition(
    val title: String,
    val durationMinutes: Int,
    val type: AudioType,
    val voiceGender: VoiceGender = VoiceGender.NEUTRAL,
    val backgroundMusic: Boolean = false
)

@Serializable
data class UserProgramSession(
    val userId: String,
    val programId: String,
    val currentStepIndex: Int,
    val sessionType: SessionType,
    val startDate: Long,
    val lastActivityDate: Long
)

@Serializable
data class ProgramCompletion(
    val programId: String,
    val userId: String,
    val completedAt: Long,
    val totalDurationMinutes: Int,
    val finalRating: Int? = null,
    val certificateEarned: Boolean = false
)

@Serializable
data class ProgramRecommendation(
    val programId: String,
    val score: Float,
    val reason: String,
    val priority: RecommendationPriority
)

@Serializable
data class DailyProgramActivity(
    val userId: String,
    val date: String,
    val programsAccessed: List<String>,
    val totalMinutesSpent: Int,
    val stepsCompleted: Int,
    val reflectionsCompleted: Int
)

@Serializable
data class ProgramAnalytics(
    val totalUsers: Int,
    val activeUsers: Int,
    val completionRates: Map<String, Float>,
    val averageCompletionTime: Map<String, Int>,
    val userSatisfactionScores: Map<String, Float>
)

@Serializable
data class ProgramEnrollment(
    val userId: String,
    val programId: String,
    val enrollmentDate: Long,
    val status: EnrollmentStatus,
    val completionDate: Long? = null
)

@Serializable
enum class EnrollmentStatus {
    ENROLLED,
    IN_PROGRESS,
    COMPLETED,
    DROPPED,
    PAUSED
}
