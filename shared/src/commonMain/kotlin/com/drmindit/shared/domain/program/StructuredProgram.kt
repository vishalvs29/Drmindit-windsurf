package com.drmindit.shared.domain.program

import kotlinx.serialization.Serializable
import com.drmindit.shared.domain.audience.AudienceType

/**
 * Structured Program Domain Models
 * Replaces generic AI chat with guided, step-by-step programs
 */

@Serializable
data class StructuredProgram(
    val id: String,
    val name: String,
    val description: String,
    val targetAudience: AudienceType,
    val category: ProgramCategory,
    val duration: ProgramDuration,
    val difficulty: ProgramDifficulty,
    val currentStep: Int = 1,
    val totalSteps: Int,
    val isCompleted: Boolean = false,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val progress: ProgramProgress,
    val steps: List<ProgramStep>
)

@Serializable
data class ProgramProgress(
    val currentStepIndex: Int,
    val completedSteps: Set<Int>,
    val stepProgress: Map<Int, StepProgress>,
    val overallCompletionPercentage: Float,
    val timeSpentMinutes: Long,
    val lastAccessedAt: Long
)

@Serializable
data class StepProgress(
    val stepId: String,
    val isStarted: Boolean,
    val isCompleted: Boolean,
    val timeSpentMinutes: Long,
    val exerciseCompleted: Boolean = false,
    val reflectionCompleted: Boolean = false,
    val audioCompleted: Boolean = false,
    val rating: Int? = null, // 1-5 rating for the step
    val notes: String? = null
)

@Serializable
data class ProgramStep(
    val id: String,
    val day: Int,
    val title: String,
    val description: String,
    val type: StepType,
    val exercise: ExerciseDefinition?,
    val reflection: ReflectionDefinition?,
    val audioSession: AudioDefinition?,
    val isRequired: Boolean = true,
    val estimatedDurationMinutes: Int
)

@Serializable
enum class StepType {
    INSTRUCTION,     // Educational content
    EXERCISE,        // Interactive exercise
    REFLECTION,      // Self-reflection prompt
    AUDIO_SESSION,   // Guided audio
    ASSESSMENT,      // Progress check
    PRACTICE         // Practice activity
}

@Serializable
enum class ProgramCategory {
    ANXIETY, STRESS, FOCUS, SLEEP, TRAUMA, RESILIENCE, BURNOUT
}

@Serializable
enum class ProgramDuration {
    DAYS_7, DAYS_14, DAYS_21, DAYS_30, ONGOING
}

@Serializable
enum class ProgramDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

/**
 * Exercise definition for program steps
 */
@Serializable
data class ExerciseDefinition(
    val type: ExerciseType,
    val instructions: String,
    val durationMinutes: Int,
    val difficulty: ExerciseDifficulty,
    val equipment: List<String> = emptyList(),
    val steps: List<ExerciseStep>? = null,
    val successCriteria: String? = null
)

@Serializable
enum class ExerciseType {
    BREATHING,
    MEDITATION,
    GROUNDING,
    VISUALIZATION,
    JOURNALING,
    BODY_SCAN,
    PROGRESSIVE_RELAXATION,
    COGNITIVE_RESTRUCTURING,
    AFFIRMATIONS,
    POMODORO,
    STRETCHING
}

@Serializable
enum class ExerciseDifficulty {
    EASY, MODERATE, CHALLENGING
}

@Serializable
data class ExerciseStep(
    val stepNumber: Int,
    val instruction: String,
    val durationSeconds: Int,
    val isOptional: Boolean = false
)

/**
 * Reflection definition for program steps
 */
@Serializable
data class ReflectionDefinition(
    val prompt: String,
    val type: ReflectionType,
    val responseFormat: ResponseFormat,
    val isOptional: Boolean = false,
    val options: List<ReflectionOption>? = null,
    val maxLength: Int? = null
)

@Serializable
enum class ReflectionType {
    OPEN_ENDED,
    RATING_SCALE,
    MULTIPLE_CHOICE,
    CHECKLIST,
    JOURNAL_ENTRY,
    YES_NO
}

@Serializable
enum class ResponseFormat {
    TEXT, RATING, SELECTION, CHECKLIST, BOOLEAN
}

@Serializable
data class ReflectionOption(
    val id: String,
    val text: String,
    val value: String
)

/**
 * Audio session definition
 */
@Serializable
data class AudioDefinition(
    val title: String,
    val durationMinutes: Int,
    val type: AudioType,
    val voiceGender: VoiceGender,
    val backgroundMusic: Boolean,
    val script: String,
    val audioUrl: String? = null, // URL to actual audio file
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)

@Serializable
enum class AudioType {
    GUIDED_MEDITATION,
    BREATHING_EXERCISE,
    SLEEP_STORY,
    BODY_SCAN,
    GROUNDING_EXERCISE,
    AFFIRMATIONS,
    FOCUS_MUSIC,
    RELAXATION_MUSIC
}

@Serializable
enum class VoiceGender {
    MALE, FEMALE, NEUTRAL
}

/**
 * User's active program session
 */
@Serializable
data class UserProgramSession(
    val userId: String,
    val programId: String,
    val currentStepIndex: Int,
    val sessionStartTime: Long,
    val sessionEndTime: Long? = null,
    val isActive: Boolean = true,
    val sessionType: SessionType
)

@Serializable
enum class SessionType {
    DAILY_PRACTICE,
    PROGRAM_STEP,
    REFLECTION,
    AUDIO_ONLY
}

/**
 * Program completion data
 */
@Serializable
data class ProgramCompletion(
    val programId: String,
    val userId: String,
    val completedAt: Long,
    val totalDurationMinutes: Long,
    finalRating: Int, // 1-5 rating for the entire program
    val feedback: String? = null,
    val certificateEarned: Boolean = false,
    val nextProgramRecommendation: String? = null
)

/**
 * Program recommendation system
 */
@Serializable
data class ProgramRecommendation(
    val programId: String,
    val score: Float, // 0.0 to 1.0
    val reason: String,
    val priority: RecommendationPriority,
    val expiresAt: Long
)

@Serializable
enum class RecommendationPriority {
    HIGH, MEDIUM, LOW
}

/**
 * Daily program activity tracking
 */
@Serializable
data class DailyProgramActivity(
    val userId: String,
    val date: String, // YYYY-MM-DD format
    val programsAccessed: List<String>,
    val stepsCompleted: List<String>,
    val totalMinutesSpent: Int,
    val moodBefore: Int? = null, // 1-5 mood rating
    val moodAfter: Int? = null, // 1-5 mood rating
    val stressLevel: Int? = null, // 1-5 stress rating
    val notes: String? = null
)

/**
 * Program analytics for insights
 */
@Serializable
data class ProgramAnalytics(
    val totalUsers: Int,
    val activeUsers: Int,
    val completionRates: Map<String, Float>, // program ID -> completion rate
    val averageCompletionTime: Map<String, Long>, // program ID -> average time
    val userSatisfaction: Map<String, Float>, // program ID -> average rating
    val mostPopularPrograms: List<String>,
    val dropoutPoints: Map<String, List<Int>> // program ID -> step indices where users drop out
)
