package com.drmindit.shared.domain.program

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Guided Program Engine - Core Therapy Flow System
 * Replaces free-form chatbot with structured, day-by-day programs
 */
class GuidedProgramEngine {
    
    private val _activeProgram = MutableStateFlow<GuidedProgram?>(null)
    val activeProgram: Flow<GuidedProgram?> = _activeProgram.asStateFlow()
    
    private val _currentDay = MutableStateFlow<ProgramDay?>(null)
    val currentDay: Flow<ProgramDay?> = _currentDay.asStateFlow()
    
    private val _currentStep = MutableStateFlow<ProgramStep?>(null)
    val currentStep: Flow<ProgramStep?> = _currentStep.asStateFlow()
    
    private val _programState = MutableStateFlow<ProgramState>(ProgramState.NOT_STARTED)
    val programState: Flow<ProgramState> = _programState.asStateFlow()
    
    private val _userProgress = MutableStateFlow<UserProgramProgress?>(null)
    val userProgress: Flow<UserProgramProgress?> = _userProgress.asStateFlow()
    
    // Internal state management
    private var internalProgress: UserProgramProgress? = null
    private var currentProgram: GuidedProgram? = null
    
    /**
     * Start a new guided program
     */
    suspend fun startProgram(
        userId: String,
        programId: String
    ): Result<ProgramStartResult> {
        return try {
            val program = getProgramById(programId)
                ?: return Result.failure(Exception("Program not found"))
            
            // Initialize progress
            internalProgress = UserProgramProgress(
                userId = userId,
                programId = programId,
                currentDay = 1,
                currentStep = 0,
                completedDays = emptySet(),
                dayProgress = emptyMap(),
                startedAt = System.currentTimeMillis(),
                lastAccessedAt = System.currentTimeMillis(),
                totalMinutesSpent = 0,
                streakDays = 1,
                isCompleted = false
            )
            
            currentProgram = program
            _activeProgram.value = program
            _currentDay.value = program.days.first()
            _currentStep.value = program.days.first().steps.first()
            _programState.value = ProgramState.IN_PROGRESS
            _userProgress.value = internalProgress
            
            Result.success(
                ProgramStartResult(
                    success = true,
                    programName = program.name,
                    totalDays = program.days.size,
                    firstDay = program.days.first()
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Continue from saved progress
     */
    suspend fun continueProgram(
        userId: String,
        savedProgress: UserProgramProgress
    ): Result<ProgramContinueResult> {
        return try {
            val program = getProgramById(savedProgress.programId)
                ?: return Result.failure(Exception("Program not found"))
            
            internalProgress = savedProgress
            currentProgram = program
            
            // Determine current day and step
            val currentDayIndex = savedProgress.currentDay - 1
            val currentStepIndex = savedProgress.currentStep
            
            val currentDay = if (currentDayIndex < program.days.size) {
                program.days[currentDayIndex]
            } else null
            
            val currentStep = currentDay?.steps?.getOrNull(currentStepIndex)
            
            _activeProgram.value = program
            _currentDay.value = currentDay
            _currentStep.value = currentStep
            _programState.value = if (savedProgress.isCompleted) {
                ProgramState.COMPLETED
            } else {
                ProgramState.IN_PROGRESS
            }
            _userProgress.value = savedProgress
            
            Result.success(
                ProgramContinueResult(
                    success = true,
                    currentDay = currentDay,
                    currentStep = currentStep,
                    dayNumber = savedProgress.currentDay,
                    totalDays = program.days.size,
                    isCompleted = savedProgress.isCompleted
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete current step and move to next
     */
    suspend fun completeCurrentStep(
        stepResult: StepCompletionResult
    ): Result<StepProgressResult> {
        return try {
            val progress = internalProgress ?: return Result.failure(Exception("No active progress"))
            val program = currentProgram ?: return Result.failure(Exception("No active program"))
            val currentStep = _currentStep.value ?: return Result.failure(Exception("No current step"))
            
            // Update step progress
            val stepProgress = StepProgress(
                stepId = currentStep.id,
                isCompleted = true,
                timeSpentMinutes = stepResult.timeSpentMinutes,
                reflectionResponse = stepResult.reflectionResponse,
                exerciseCompleted = stepResult.exerciseCompleted,
                rating = stepResult.rating
            )
            
            // Update day progress
            val currentDayProgress = progress.dayProgress[progress.currentDay]?.toMutableMap() ?: mutableMapOf()
            currentDayProgress[currentStep.id] = stepProgress
            
            val updatedDayProgress = progress.dayProgress + (progress.currentDay to currentDayProgress)
            
            // Determine next step
            val currentDay = _currentDay.value!!
            val nextStepIndex = progress.currentStep + 1
            
            if (nextStepIndex < currentDay.steps.size) {
                // Continue to next step in same day
                internalProgress = progress.copy(
                    currentStep = nextStepIndex,
                    dayProgress = updatedDayProgress,
                    lastAccessedAt = System.currentTimeMillis(),
                    totalMinutesSpent = progress.totalMinutesSpent + stepResult.timeSpentMinutes
                )
                
                _currentStep.value = currentDay.steps[nextStepIndex]
                _userProgress.value = internalProgress
                
                Result.success(
                    StepProgressResult(
                        success = true,
                        nextStep = currentDay.steps[nextStepIndex],
                        isDayCompleted = false,
                        isProgramCompleted = false
                    )
                )
                
            } else {
                // Day completed, move to next day
                val nextDayIndex = progress.currentDay
                val nextDay = if (nextDayIndex < program.days.size) {
                    program.days[nextDayIndex]
                } else null
                
                if (nextDay == null) {
                    // Program completed
                    val completedProgress = progress.copy(
                        completedDays = progress.completedDays + progress.currentDay,
                        dayProgress = updatedDayProgress,
                        currentDay = program.days.size,
                        currentStep = 0,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis(),
                        lastAccessedAt = System.currentTimeMillis(),
                        totalMinutesSpent = progress.totalMinutesSpent + stepResult.timeSpentMinutes
                    )
                    
                    internalProgress = completedProgress
                    _programState.value = ProgramState.COMPLETED
                    _userProgress.value = completedProgress
                    
                    Result.success(
                        StepProgressResult(
                            success = true,
                            isDayCompleted = true,
                            isProgramCompleted = true
                        )
                    )
                    
                } else {
                    // Move to next day
                    val updatedProgress = progress.copy(
                        completedDays = progress.completedDays + progress.currentDay,
                        dayProgress = updatedDayProgress,
                        currentDay = nextDayIndex + 1,
                        currentStep = 0,
                        lastAccessedAt = System.currentTimeMillis(),
                        totalMinutesSpent = progress.totalMinutesSpent + stepResult.timeSpentMinutes,
                        streakDays = calculateStreak(progress, nextDayIndex + 1)
                    )
                    
                    internalProgress = updatedProgress
                    _currentDay.value = nextDay
                    _currentStep.value = nextDay.steps.first()
                    _userProgress.value = updatedProgress
                    
                    Result.success(
                        StepProgressResult(
                            success = true,
                            nextStep = nextDay.steps.first(),
                            nextDay = nextDay,
                            isDayCompleted = true,
                            isProgramCompleted = false
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get program by ID
     */
    private fun getProgramById(programId: String): GuidedProgram? {
        return when (programId) {
            "exam_anxiety_7day" -> createExamAnxietyProgram()
            "stress_management_14day" -> createStressManagementProgram()
            "confidence_building_21day" -> createConfidenceBuildingProgram()
            else -> null
        }
    }
    
    /**
     * Calculate streak days
     */
    private fun calculateStreak(progress: UserProgramProgress, nextDay: Int): Int {
        // Simple streak calculation - consecutive days
        val expectedDay = progress.completedDays.size + 1
        return if (nextDay == expectedDay) {
            progress.streakDays + 1
        } else {
            1 // Reset streak
        }
    }
    
    /**
     * Create Exam Anxiety Program (7 days)
     */
    private fun createExamAnxietyProgram(): GuidedProgram {
        return GuidedProgram(
            id = "exam_anxiety_7day",
            name = "Exam Anxiety Relief Program",
            description = "7-day structured program to overcome exam anxiety and build confidence",
            duration = ProgramDuration.DAYS_7,
            category = ProgramCategory.ANXIETY,
            difficulty = ProgramDifficulty.BEGINNER,
            days = listOf(
                // Day 1: Understanding Anxiety
                ProgramDay(
                    day = 1,
                    title = "Understanding Exam Anxiety",
                    description = "Learn what causes exam anxiety and how it affects you",
                    steps = listOf(
                        ProgramStep(
                            id = "day1_intro",
                            type = StepType.INSTRUCTION,
                            title = "Welcome to Your Journey",
                            content = "Let's understand what exam anxiety is and why it happens...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day1_question",
                            type = StepType.QUESTION,
                            title = "When Do You Feel Anxious?",
                            content = "Think about the specific situations that trigger your exam anxiety...",
                            estimatedMinutes = 3
                        ),
                        ProgramStep(
                            id = "day1_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Anxiety Awareness Exercise",
                            content = "Let's practice identifying your anxiety symptoms...",
                            exerciseType = ExerciseType.AWARENESS,
                            estimatedMinutes = 10
                        ),
                        ProgramStep(
                            id = "day1_reflection",
                            type = StepType.REFLECTION,
                            title = "Reflection on Your Patterns",
                            content = "What did you discover about your anxiety patterns?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day1_completion",
                            type = StepType.COMPLETION,
                            title = "Day 1 Complete!",
                            content = "Great job! You've taken the first step...",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 2: Breathing Control
                ProgramDay(
                    day = 2,
                    title = "Breathing Control Techniques",
                    description = "Master breathing exercises to calm your mind and body",
                    steps = listOf(
                        ProgramStep(
                            id = "day2_intro",
                            type = StepType.INSTRUCTION,
                            title = "The Power of Breathing",
                            content = "Learn how controlled breathing can instantly reduce anxiety...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day2_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "4-7-8 Breathing Technique",
                            content = "Practice the calming 4-7-8 breathing method...",
                            exerciseType = ExerciseType.BREATHING,
                            estimatedMinutes = 15,
                            audioSession = AudioSession(
                                title = "Calming Breathing Exercise",
                                duration = 15,
                                voiceGender = VoiceGender.FEMALE,
                                backgroundMusic = true
                            )
                        ),
                        ProgramStep(
                            id = "day2_reflection",
                            type = StepType.REFLECTION,
                            title = "How Did Breathing Help?",
                            content = "Rate your anxiety before and after the breathing exercise...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day2_completion",
                            type = StepType.COMPLETION,
                            title = "Day 2 Complete!",
                            content = "You now have a powerful tool for instant calm...",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 3: Thought Reframing
                ProgramDay(
                    day = 3,
                    title = "Thought Reframing",
                    description = "Challenge and reframe anxious thoughts about exams",
                    steps = listOf(
                        ProgramStep(
                            id = "day3_intro",
                            type = StepType.INSTRUCTION,
                            title = "Understanding Your Thoughts",
                            content = "Learn how your thoughts create anxiety and how to change them...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day3_question",
                            type = StepType.QUESTION,
                            title = "Identify Anxious Thoughts",
                            content = "What specific thoughts go through your mind during exams?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day3_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Thought Challenge Exercise",
                            content = "Practice challenging your anxious thoughts with evidence...",
                            exerciseType = ExerciseType.COGNITIVE_RESTRUCTURING,
                            estimatedMinutes = 15
                        ),
                        ProgramStep(
                            id = "day3_reflection",
                            type = StepType.REFLECTION,
                            title = "New Perspective",
                            content = "How does this new perspective feel compared to your original thoughts?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day3_completion",
                            type = StepType.COMPLETION,
                            title = "Day 3 Complete!",
                            content = "You're becoming a thought detective!",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 4: Facing Fear
                ProgramDay(
                    day = 4,
                    title = "Facing Fear Gradually",
                    description = "Practice facing exam situations in a controlled way",
                    steps = listOf(
                        ProgramStep(
                            id = "day4_intro",
                            type = StepType.INSTRUCTION,
                            title = "Gradual Exposure",
                            content = "Learn how facing fears gradually reduces anxiety...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day4_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Fear Ladder Practice",
                            content = "Create and practice your fear ladder for exam situations...",
                            exerciseType = ExerciseType.EXPOSURE,
                            estimatedMinutes = 20
                        ),
                        ProgramStep(
                            id = "day4_reflection",
                            type = StepType.REFLECTION,
                            title = "Facing Your Fear",
                            content = "How did it feel to face your fear in a controlled way?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day4_completion",
                            type = StepType.COMPLETION,
                            title = "Day 4 Complete!",
                            content = "You're building courage step by step...",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 5: Confidence Building
                ProgramDay(
                    day = 5,
                    title = "Building Exam Confidence",
                    description = "Develop unshakable confidence for exam success",
                    steps = listOf(
                        ProgramStep(
                            id = "day5_intro",
                            type = StepType.INSTRUCTION,
                            title = "The Nature of Confidence",
                            content = "Confidence isn't about being perfect - it's about trusting yourself...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day5_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Confidence Visualization",
                            content = "Visualize yourself succeeding in exams with confidence...",
                            exerciseType = ExerciseType.VISUALIZATION,
                            estimatedMinutes = 15,
                            audioSession = AudioSession(
                                title = "Confidence Building Visualization",
                                duration = 15,
                                voiceGender = VoiceGender.FEMALE,
                                backgroundMusic = true
                            )
                        ),
                        ProgramStep(
                            id = "day5_reflection",
                            type = StepType.REFLECTION,
                            title = "Your Strengths",
                            content = "List three strengths that will help you in exams...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day5_completion",
                            type = StepType.COMPLETION,
                            title = "Day 5 Complete!",
                            content = "Your confidence is growing stronger!",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 6: Sleep Optimization
                ProgramDay(
                    day = 6,
                    title = "Sleep Optimization",
                    description = "Improve sleep quality for better exam performance",
                    steps = listOf(
                        ProgramStep(
                            id = "day6_intro",
                            type = StepType.INSTRUCTION,
                            title = "Sleep and Performance",
                            content = "Quality sleep is crucial for exam success...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day6_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Bedtime Relaxation Routine",
                            content = "Practice a calming bedtime routine for better sleep...",
                            exerciseType = ExerciseType.RELAXATION,
                            estimatedMinutes = 20,
                            audioSession = AudioSession(
                                title = "Exam Night Sleep Meditation",
                                duration = 20,
                                voiceGender = VoiceGender.FEMALE,
                                backgroundMusic = true
                            )
                        ),
                        ProgramStep(
                            id = "day6_reflection",
                            type = StepType.REFLECTION,
                            title = "Sleep Habits",
                            content = "What changes will you make to your sleep routine?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day6_completion",
                            type = StepType.COMPLETION,
                            title = "Day 6 Complete!",
                            content = "You're ready for restful, restorative sleep...",
                            estimatedMinutes = 2
                        )
                    )
                ),
                
                // Day 7: Exam Readiness
                ProgramDay(
                    day = 7,
                    title = "Exam Day Readiness",
                    description = "Final preparation and confidence for exam day",
                    steps = listOf(
                        ProgramStep(
                            id = "day7_intro",
                            type = StepType.INSTRUCTION,
                            title = "Exam Day Strategy",
                            content = "Your complete strategy for exam day success...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day7_exercise",
                            type = StepType.GUIDED_EXERCISE,
                            title = "Quick Calm Technique",
                            content = "Learn a 2-minute technique for instant calm during exams...",
                            exerciseType = ExerciseType.BREATHING,
                            estimatedMinutes = 10
                        ),
                        ProgramStep(
                            id = "day7_reflection",
                            type = StepType.REFLECTION,
                            title = "Your Journey",
                            content = "What's the most valuable thing you've learned?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day7_completion",
                            type = StepType.COMPLETION,
                            title = "Program Complete! 🎉",
                            content = "Congratulations! You're ready for exam success...",
                            estimatedMinutes = 2
                        )
                    )
                )
            )
        )
    }
    
    /**
     * Create Stress Management Program (14 days)
     */
    private fun createStressManagementProgram(): GuidedProgram {
        // Implementation for 14-day stress management program
        return GuidedProgram(
            id = "stress_management_14day",
            name = "Stress Management Program",
            description = "14-day comprehensive program for stress management and resilience",
            duration = ProgramDuration.DAYS_14,
            category = ProgramCategory.STRESS,
            difficulty = ProgramDifficulty.INTERMEDIATE,
            days = emptyList() // Would implement full 14-day program
        )
    }
    
    /**
     * Create Confidence Building Program (21 days)
     */
    private fun createConfidenceBuildingProgram(): GuidedProgram {
        // Implementation for 21-day confidence building program
        return GuidedProgram(
            id = "confidence_building_21day",
            name = "Confidence Building Program",
            description = "21-day program to build unshakable confidence",
            duration = ProgramDuration.DAYS_21,
            category = ProgramCategory.CONFIDENCE,
            difficulty = ProgramDifficulty.ADVANCED,
            days = emptyList() // Would implement full 21-day program
        )
    }
}

// Data classes for the guided program system

@Serializable
data class GuidedProgram(
    val id: String,
    val name: String,
    val description: String,
    val duration: ProgramDuration,
    val category: ProgramCategory,
    val difficulty: ProgramDifficulty,
    val days: List<ProgramDay>
)

@Serializable
data class ProgramDay(
    val day: Int,
    val title: String,
    val description: String,
    val steps: List<ProgramStep>
)

@Serializable
data class ProgramStep(
    val id: String,
    val type: StepType,
    val title: String,
    val content: String,
    val estimatedMinutes: Int,
    val exerciseType: ExerciseType? = null,
    val audioSession: AudioSession? = null
)

@Serializable
data class AudioSession(
    val title: String,
    val duration: Int,
    val voiceGender: VoiceGender,
    val backgroundMusic: Boolean
)

@Serializable
enum class StepType {
    INSTRUCTION,      // Educational content
    QUESTION,         // User inquiry
    GUIDED_EXERCISE,  // Interactive exercise
    REFLECTION,       // Self-reflection
    COMPLETION        // Session completion
}

@Serializable
enum class ExerciseType {
    BREATHING,
    AWARENESS,
    COGNITIVE_RESTRUCTURING,
    EXPOSURE,
    VISUALIZATION,
    RELAXATION,
    JOURNALING
}

@Serializable
enum class VoiceGender {
    MALE, FEMALE, NEUTRAL
}

@Serializable
enum class ProgramDuration {
    DAYS_7, DAYS_14, DAYS_21, DAYS_30
}

@Serializable
enum class ProgramCategory {
    ANXIETY, STRESS, CONFIDENCE, SLEEP, FOCUS
}

@Serializable
enum class ProgramDifficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}

@Serializable
enum class ProgramState {
    NOT_STARTED, IN_PROGRESS, PAUSED, COMPLETED
}

@Serializable
data class UserProgramProgress(
    val userId: String,
    val programId: String,
    val currentDay: Int,
    val currentStep: Int,
    val completedDays: Set<Int>,
    val dayProgress: Map<Int, Map<String, StepProgress>>,
    val startedAt: Long,
    val lastAccessedAt: Long,
    val totalMinutesSpent: Long,
    val streakDays: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Serializable
data class StepProgress(
    val stepId: String,
    val isCompleted: Boolean = false,
    val timeSpentMinutes: Int = 0,
    val reflectionResponse: String? = null,
    val exerciseCompleted: Boolean = false,
    val rating: Int? = null
)

@Serializable
data class ProgramStartResult(
    val success: Boolean,
    val programName: String,
    val totalDays: Int,
    val firstDay: ProgramDay
)

@Serializable
data class ProgramContinueResult(
    val success: Boolean,
    val currentDay: ProgramDay?,
    val currentStep: ProgramStep?,
    val dayNumber: Int,
    val totalDays: Int,
    val isCompleted: Boolean
)

@Serializable
data class StepCompletionResult(
    val timeSpentMinutes: Int,
    val reflectionResponse: String? = null,
    val exerciseCompleted: Boolean = false,
    val rating: Int? = null
)

@Serializable
data class StepProgressResult(
    val success: Boolean,
    val nextStep: ProgramStep? = null,
    val nextDay: ProgramDay? = null,
    val isDayCompleted: Boolean = false,
    val isProgramCompleted: Boolean = false
)
