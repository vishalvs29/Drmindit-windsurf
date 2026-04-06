package com.drmindit.shared.domain.program

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import com.drmindit.shared.domain.model.*

/**
 * Unified Program Engine
 * Combines functionality from multiple program engines
 */
class ProgramEngine {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Get available programs for user
     */
    fun getAvailablePrograms(userPreferences: UserPreferences? = null): Flow<List<StructuredProgram>> = flow {
        val programs = listOf(
            StructuredProgram(
                id = "stress-relief-7day",
                name = "7-Day Stress Relief Program",
                description = "Comprehensive stress management program",
                category = ProgramCategory.STRESS,
                duration = ProgramDuration.DAYS_7,
                difficulty = ProgramDifficulty.BEGINNER,
                steps = generateStressReliefSteps()
            ),
            StructuredProgram(
                id = "anxiety-management-14day",
                name = "14-Day Anxiety Management",
                description = "Evidence-based anxiety reduction techniques",
                category = ProgramCategory.ANXIETY,
                duration = ProgramDuration.DAYS_14,
                difficulty = ProgramDifficulty.INTERMEDIATE,
                steps = generateAnxietySteps()
            ),
            StructuredProgram(
                id = "sleep-improvement-21day",
                name = "21-Day Sleep Improvement",
                description = "Better sleep habits and relaxation techniques",
                category = ProgramCategory.SLEEP,
                duration = ProgramDuration.DAYS_21,
                difficulty = ProgramDifficulty.BEGINNER,
                steps = generateSleepSteps()
            )
        )
        
        emit(programs)
    }
    
    /**
     * Start program for user
     */
    suspend fun startProgram(userId: String, programId: String): Result<UserProgramSession> {
        return try {
            val session = UserProgramSession(
                userId = userId,
                programId = programId,
                currentStepIndex = 0,
                sessionType = SessionType.DAILY_PRACTICE,
                startDate = System.currentTimeMillis(),
                lastActivityDate = System.currentTimeMillis()
            )
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get next step in program
     */
    suspend fun getNextStep(session: UserProgramSession): Result<ProgramStep?> {
        return try {
            val program = getProgramById(session.programId)
            if (session.currentStepIndex < program.steps.size) {
                Result.success(program.steps[session.currentStepIndex])
            } else {
                Result.success(null) // Program completed
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete current step
     */
    suspend fun completeStep(
        session: UserProgramSession,
        stepProgress: StepProgress
    ): Result<UserProgramSession> {
        return try {
            val updatedSession = session.copy(
                currentStepIndex = session.currentStepIndex + 1,
                lastActivityDate = System.currentTimeMillis()
            )
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get program recommendations
     */
    suspend fun getRecommendations(
        userId: String,
        moodCategory: MoodCategory? = null
    ): Result<List<ProgramRecommendation>> {
        return try {
            val recommendations = listOf(
                ProgramRecommendation(
                    programId = "stress-relief-7day",
                    score = 0.9f,
                    reason = "Based on your stress levels",
                    priority = RecommendationPriority.HIGH
                ),
                ProgramRecommendation(
                    programId = "mindfulness-basics",
                    score = 0.8f,
                    reason = "Good starting point for wellness journey",
                    priority = RecommendationPriority.MEDIUM
                )
            )
            Result.success(recommendations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getProgramById(programId: String): StructuredProgram {
        return when (programId) {
            "stress-relief-7day" -> StructuredProgram(
                id = "stress-relief-7day",
                name = "7-Day Stress Relief Program",
                description = "Comprehensive stress management program",
                category = ProgramCategory.STRESS,
                duration = ProgramDuration.DAYS_7,
                difficulty = ProgramDifficulty.BEGINNER,
                steps = generateStressReliefSteps()
            )
            "anxiety-management-14day" -> StructuredProgram(
                id = "anxiety-management-14day",
                name = "14-Day Anxiety Management",
                description = "Evidence-based anxiety reduction techniques",
                category = ProgramCategory.ANXIETY,
                duration = ProgramDuration.DAYS_14,
                difficulty = ProgramDifficulty.INTERMEDIATE,
                steps = generateAnxietySteps()
            )
            "sleep-improvement-21day" -> StructuredProgram(
                id = "sleep-improvement-21day",
                name = "21-Day Sleep Improvement",
                description = "Better sleep habits and relaxation techniques",
                category = ProgramCategory.SLEEP,
                duration = ProgramDuration.DAYS_21,
                difficulty = ProgramDifficulty.BEGINNER,
                steps = generateSleepSteps()
            )
            else -> throw IllegalArgumentException("Unknown program ID: $programId")
        }
    }
    
    private fun generateStressReliefSteps(): List<ProgramStep> {
        return listOf(
            ProgramStep(
                id = "stress-1",
                day = 1,
                title = "Understanding Stress",
                description = "Learn about stress and its effects",
                type = StepType.INSTRUCTION,
                durationMinutes = 15
            ),
            ProgramStep(
                id = "stress-2",
                day = 1,
                title = "Deep Breathing",
                description = "Practice deep breathing techniques",
                type = StepType.BREATHING,
                exercise = ExerciseDefinition(
                    type = ExerciseType.BREATHING,
                    instructions = "Breathe in for 4 counts, hold for 4, out for 4, hold for 4",
                    durationMinutes = 10,
                    difficulty = ExerciseDifficulty.EASY
                ),
                durationMinutes = 10
            ),
            ProgramStep(
                id = "stress-3",
                day = 2,
                title = "Body Scan Meditation",
                description = "Progressive muscle relaxation",
                type = StepType.MEDITATION,
                exercise = ExerciseDefinition(
                    type = ExerciseType.PROGRESSIVE_MUSCLE_RELAXATION,
                    instructions = "Systematically tense and relax muscle groups",
                    durationMinutes = 15,
                    difficulty = ExerciseDifficulty.EASY
                ),
                durationMinutes = 15
            )
        )
    }
    
    private fun generateAnxietySteps(): List<ProgramStep> {
        return listOf(
            ProgramStep(
                id = "anxiety-1",
                day = 1,
                title = "Anxiety Awareness",
                description = "Understanding anxiety symptoms",
                type = StepType.INSTRUCTION,
                durationMinutes = 20
            ),
            ProgramStep(
                id = "anxiety-2",
                day = 1,
                title = "Grounding Technique",
                description = "5-4-3-2-1 grounding exercise",
                type = StepType.EXERCISE,
                exercise = ExerciseDefinition(
                    type = ExerciseType.GROUNDING,
                    instructions = "Name 5 things you see, 4 you can touch, 3 you hear, 2 you smell, 1 you taste",
                    durationMinutes = 10,
                    difficulty = ExerciseDifficulty.EASY
                ),
                durationMinutes = 10
            )
        )
    }
    
    private fun generateSleepSteps(): List<ProgramStep> {
        return listOf(
            ProgramStep(
                id = "sleep-1",
                day = 1,
                title = "Sleep Hygiene Basics",
                description = "Fundamentals of good sleep habits",
                type = StepType.INSTRUCTION,
                durationMinutes = 15
            ),
            ProgramStep(
                id = "sleep-2",
                day = 1,
                title = "Bedtime Meditation",
                description = "Guided meditation for sleep",
                type = StepType.MEDITATION,
                audio = AudioDefinition(
                    title = "Sleep Meditation",
                    durationMinutes = 20,
                    type = AudioType.SLEEP_STORY,
                    voiceGender = VoiceGender.FEMALE
                ),
                durationMinutes = 20
            )
        )
    }
}
