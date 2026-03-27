package com.drmindit.shared.domain.audience

import kotlinx.serialization.Serializable

/**
 * Audience Types for DrMindit Mental Health Platform
 * Each audience has tailored programs, content, and user experience
 */
@Serializable
enum class AudienceType(val displayName: String, val description: String) {
    STUDENT(
        displayName = "Student",
        description = "Mental health support for academic success and wellbeing"
    ),
    CORPORATE(
        displayName = "Corporate",
        description = "Workplace wellness and stress management for professionals"
    ),
    POLICE_MILITARY(
        displayName = "Police/Military",
        description = "Trauma-safe support for high-stress service roles"
    )
}

/**
 * Audience-specific configuration
 */
@Serializable
data class AudienceConfig(
    val type: AudienceType,
    val theme: AudienceTheme,
    val programs: List<ProgramTemplate>,
    val aiPromptStyle: AIPromptStyle,
    val contentPreferences: ContentPreferences
)

/**
 * Visual theme configuration per audience
 */
@Serializable
data class AudienceTheme(
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String,
    val accentColor: String,
    val fontFamily: String,
    val tone: ThemeTone
)

@Serializable
enum class ThemeTone {
    CALM,        // Student - soft, encouraging
    PROFESSIONAL, // Corporate - clean, focused
    GROUNDED     // Police/Military - stable, secure
}

/**
 * AI prompt style per audience
 */
@Serializable
data class AIPromptStyle(
    val persona: String,
    val tone: String,
    val languageLevel: LanguageLevel,
    val traumaSafe: Boolean,
    val focusAreas: List<String>
)

@Serializable
enum class LanguageLevel {
    SIMPLE,      // Student - easy to understand
    PROFESSIONAL, // Corporate - workplace appropriate
    DIRECT       // Police/Military - clear and concise
}

/**
 * Content preferences per audience
 */
@Serializable
data class ContentPreferences(
    val sessionLength: SessionLength,
    val preferredTopics: List<String>,
    val avoidTopics: List<String>,
    val audioPreferences: AudioPreferences,
    val reminderFrequency: ReminderFrequency
)

@Serializable
enum class SessionLength {
    SHORT_5,     // 5 minutes
    MEDIUM_15,   // 15 minutes
    LONG_30      // 30 minutes
}

@Serializable
data class AudioPreferences(
    val backgroundMusic: Boolean,
    val voiceGender: VoiceGender,
    val backgroundSounds: List<String>
)

@Serializable
enum class VoiceGender {
    MALE, FEMALE, NEUTRAL
}

@Serializable
enum class ReminderFrequency {
    NONE, DAILY, WEEKLY, CUSTOM
}

/**
 * Audience-specific program templates
 */
@Serializable
data class ProgramTemplate(
    val id: String,
    val name: String,
    val description: String,
    val duration: ProgramDuration,
    val difficulty: ProgramDifficulty,
    val targetAudience: AudienceType,
    val category: ProgramCategory,
    val steps: List<ProgramStepTemplate>
)

@Serializable
enum class ProgramDuration {
    DAYS_7, DAYS_14, DAYS_21, DAYS_30, ONGOING
}

@Serializable
enum class ProgramDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

@Serializable
enum class ProgramCategory {
    ANXIETY, STRESS, FOCUS, SLEEP, TRAUMA, RESILIENCE, BURNOUT
}

/**
 * Program step template
 */
@Serializable
data class ProgramStepTemplate(
    val id: String,
    val day: Int,
    val title: String,
    val description: String,
    val type: StepType,
    val exercise: ExerciseTemplate?,
    val reflection: ReflectionTemplate?,
    val audioSession: AudioSessionTemplate?
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

/**
 * Exercise template for program steps
 */
@Serializable
data class ExerciseTemplate(
    val type: ExerciseType,
    val instructions: String,
    val duration: Int, // in minutes
    val difficulty: ExerciseDifficulty,
    val equipment: List<String> = emptyList()
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
    COGNITIVE_RESTRUCTURING
}

@Serializable
enum class ExerciseDifficulty {
    EASY, MODERATE, CHALLENGING
}

/**
 * Reflection template for program steps
 */
@Serializable
data class ReflectionTemplate(
    val prompt: String,
    val type: ReflectionType,
    val responseFormat: ResponseFormat,
    val isOptional: Boolean = false
)

@Serializable
enum class ReflectionType {
    OPEN_ENDED,
    RATING_SCALE,
    MULTIPLE_CHOICE,
    CHECKLIST,
    JOURNAL_ENTRY
}

@Serializable
enum class ResponseFormat {
    TEXT, RATING, SELECTION, CHECKLIST
}

/**
 * Audio session template
 */
@Serializable
data class AudioSessionTemplate(
    val title: String,
    val duration: Int, // in minutes
    val type: AudioType,
    val voiceGender: VoiceGender,
    val backgroundMusic: Boolean,
    val script: String
)

@Serializable
enum class AudioType {
    GUIDED_MEDITATION,
    BREATHING_EXERCISE,
    SLEEP_STORY,
    BODY_SCAN,
    GROUNDING_EXERCISE,
    AFFIRMATIONS
}
