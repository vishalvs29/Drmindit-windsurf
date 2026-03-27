package com.drmindit.shared.domain.program

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import com.drmindit.shared.domain.audience.AudienceType

/**
 * Program Flow Engine - State Machine for Structured Programs
 * Replaces generic AI chat with guided, step-by-step program progression
 */
class ProgramFlowEngine {
    
    private val _currentProgram = MutableStateFlow<StructuredProgram?>(null)
    val currentProgram: Flow<StructuredProgram?> = _currentProgram.asStateFlow()
    
    private val _currentStep = MutableStateFlow<ProgramStep?>(null)
    val currentStep: Flow<ProgramStep?> = _currentStep.asStateFlow()
    
    private val _programState = MutableStateFlow<ProgramFlowState>(ProgramFlowState.IDLE)
    val programState: Flow<ProgramFlowState> = _programState.asStateFlow()
    
    private val _userProgress = MutableStateFlow<UserProgramProgress?>(null)
    val userProgress: Flow<UserProgramProgress?> = _userProgress.asStateFlow()
    
    private val _dailyActivity = MutableStateFlow<DailyProgramActivity?>(null)
    val dailyActivity: Flow<DailyProgramActivity?> = _dailyActivity.asStateFlow()
    
    // State management
    private var activeProgram: StructuredProgram? = null
    private var currentStepIndex: Int = 0
    private var userProgressData: UserProgramProgress? = null
    
    /**
     * Initialize a new program for the user
     */
    suspend fun startProgram(
        userId: String,
        program: StructuredProgram
    ): Result<ProgramStartResult> {
        return try {
            // Create or update user progress
            userProgressData = UserProgramProgress(
                userId = userId,
                programId = program.id,
                currentStepIndex = 0,
                completedSteps = emptySet(),
                stepProgress = emptyMap(),
                startedAt = System.currentTimeMillis(),
                lastAccessedAt = System.currentTimeMillis(),
                totalMinutesSpent = 0,
                isCompleted = false,
                completedAt = null
            )
            
            // Set active program
            activeProgram = program.copy(
                currentStep = 1,
                startedAt = System.currentTimeMillis(),
                progress = ProgramProgress(
                    currentStepIndex = 0,
                    completedSteps = emptySet(),
                    stepProgress = emptyMap(),
                    overallCompletionPercentage = 0f,
                    timeSpentMinutes = 0,
                    lastAccessedAt = System.currentTimeMillis()
                )
            )
            
            currentStepIndex = 0
            _currentProgram.value = activeProgram
            _currentStep.value = program.steps.getOrNull(0)
            _programState.value = ProgramFlowState.ACTIVE
            _userProgress.value = userProgressData
            
            Result.success(
                ProgramStartResult(
                    success = true,
                    currentStep = program.steps.first(),
                    totalSteps = program.steps.size,
                    estimatedDuration = program.steps.sumOf { it.estimatedDurationMinutes }
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Continue an existing program
     */
    suspend fun continueProgram(
        userId: String,
        programId: String
    ): Result<ProgramContinueResult> {
        return try {
            // Load user progress (in real app, this would come from database)
            val progress = loadUserProgress(userId, programId)
                ?: return Result.failure(Exception("No progress found for program"))
            
            val program = loadProgram(programId)
                ?: return Result.failure(Exception("Program not found"))
            
            // Calculate current step
            val nextStepIndex = progress.completedSteps.size
            val currentStep = program.steps.getOrNull(nextStepIndex)
            
            if (currentStep == null) {
                // Program completed
                _programState.value = ProgramFlowState.COMPLETED
                return Result.success(
                    ProgramContinueResult(
                        success = true,
                        isCompleted = true,
                        currentStep = null
                    )
                )
            }
            
            // Update state
            activeProgram = program.copy(
                currentStep = nextStepIndex + 1,
                startedAt = progress.startedAt,
                progress = calculateProgramProgress(program, progress)
            )
            
            currentStepIndex = nextStepIndex
            userProgressData = progress
            _currentProgram.value = activeProgram
            _currentStep.value = currentStep
            _programState.value = ProgramFlowState.ACTIVE
            _userProgress.value = progress
            
            Result.success(
                ProgramContinueResult(
                    success = true,
                    isCompleted = false,
                    currentStep = currentStep,
                    stepNumber = nextStepIndex + 1,
                    totalSteps = program.steps.size
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
    ): Result<StepCompletionResult> {
        return try {
            val program = activeProgram ?: return Result.failure(Exception("No active program"))
            val step = _currentStep.value ?: return Result.failure(Exception("No current step"))
            val progress = userProgressData ?: return Result.failure(Exception("No user progress"))
            
            // Update step progress
            val updatedStepProgress = StepProgress(
                stepId = step.id,
                isStarted = true,
                isCompleted = true,
                timeSpentMinutes = stepResult.timeSpentMinutes,
                exerciseCompleted = stepResult.exerciseCompleted,
                reflectionCompleted = stepResult.reflectionCompleted,
                audioCompleted = stepResult.audioCompleted,
                rating = stepResult.rating,
                notes = stepResult.notes
            )
            
            // Update overall progress
            val updatedCompletedSteps = progress.completedSteps + currentStepIndex
            val updatedStepProgressMap = progress.stepProgress + (currentStepIndex to updatedStepProgress)
            
            userProgressData = progress.copy(
                completedSteps = updatedCompletedSteps,
                stepProgress = updatedStepProgressMap,
                lastAccessedAt = System.currentTimeMillis(),
                totalMinutesSpent = progress.totalMinutesSpent + stepResult.timeSpentMinutes
            )
            
            // Move to next step
            val nextStepIndex = currentStepIndex + 1
            val nextStep = program.steps.getOrNull(nextStepIndex)
            
            if (nextStep == null) {
                // Program completed
                completeProgram(stepResult)
                return Result.success(
                    StepCompletionResult(
                        success = true,
                        isProgramCompleted = true,
                        nextStep = null,
                        programCompleted = true
                    )
                )
            } else {
                // Move to next step
                currentStepIndex = nextStepIndex
                _currentStep.value = nextStep
                _currentProgram.value = program.copy(
                    currentStep = nextStepIndex + 1,
                    progress = calculateProgramProgress(program, userProgressData!!)
                )
                _userProgress.value = userProgressData
                
                return Result.success(
                    StepCompletionResult(
                        success = true,
                        isProgramCompleted = false,
                        nextStep = nextStep,
                        nextStepNumber = nextStepIndex + 1,
                        programCompleted = false
                    )
                )
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Pause current program
     */
    suspend fun pauseProgram(): Result<Unit> {
        return try {
            _programState.value = ProgramFlowState.PAUSED
            userProgressData?.let { progress ->
                _userProgress.value = progress.copy(
                    lastAccessedAt = System.currentTimeMillis()
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resume paused program
     */
    suspend fun resumeProgram(): Result<Unit> {
        return try {
            if (_programState.value == ProgramFlowState.PAUSED) {
                _programState.value = ProgramFlowState.ACTIVE
                userProgressData?.let { progress ->
                    _userProgress.value = progress.copy(
                        lastAccessedAt = System.currentTimeMillis()
                    )
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Program is not paused"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset program progress
     */
    suspend fun resetProgram(): Result<Unit> {
        return try {
            activeProgram?.let { program ->
                userProgressData?.let { progress ->
                    userProgressData = progress.copy(
                        currentStepIndex = 0,
                        completedSteps = emptySet(),
                        stepProgress = emptyMap(),
                        startedAt = System.currentTimeMillis(),
                        totalMinutesSpent = 0,
                        isCompleted = false,
                        completedAt = null
                    )
                    
                    currentStepIndex = 0
                    _currentProgram.value = program.copy(
                        currentStep = 1,
                        progress = ProgramProgress(
                            currentStepIndex = 0,
                            completedSteps = emptySet(),
                            stepProgress = emptyMap(),
                            overallCompletionPercentage = 0f,
                            timeSpentMinutes = 0,
                            lastAccessedAt = System.currentTimeMillis()
                        )
                    )
                    _currentStep.value = program.steps.getOrNull(0)
                    _programState.value = ProgramFlowState.ACTIVE
                    _userProgress.value = userProgressData
                    
                    Result.success(Unit)
                }
            } ?: Result.failure(Exception("No active program"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Complete entire program
     */
    private suspend fun completeProgram(finalStepResult: StepCompletionResult) {
        activeProgram?.let { program ->
            userProgressData?.let { progress ->
                val completionData = ProgramCompletion(
                    programId = program.id,
                    userId = progress.userId,
                    completedAt = System.currentTimeMillis(),
                    totalDurationMinutes = progress.totalMinutesSpent + finalStepResult.timeSpentMinutes,
                    finalRating = finalStepResult.rating ?: 3,
                    feedback = finalStepResult.notes,
                    certificateEarned = true
                )
                
                userProgressData = progress.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    totalMinutesSpent = progress.totalMinutesSpent + finalStepResult.timeSpentMinutes
                )
                
                _currentProgram.value = program.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    progress = ProgramProgress(
                        currentStepIndex = program.steps.size,
                        completedSteps = (0 until program.steps.size).toSet(),
                        stepProgress = progress.stepProgress,
                        overallCompletionPercentage = 100f,
                        timeSpentMinutes = progress.totalMinutesSpent + finalStepResult.timeSpentMinutes,
                        lastAccessedAt = System.currentTimeMillis()
                    )
                )
                
                _programState.value = ProgramFlowState.COMPLETED
                _userProgress.value = userProgressData
            }
        }
    }
    
    /**
     * Get available programs for audience
     */
    fun getAvailablePrograms(audience: AudienceType): List<StructuredProgram> {
        // In real app, this would load from repository
        return when (audience) {
            AudienceType.STUDENT -> getStudentPrograms()
            AudienceType.CORPORATE -> getCorporatePrograms()
            AudienceType.POLICE_MILITARY -> getPoliceMilitaryPrograms()
        }
    }
    
    /**
     * Get recommended programs for user
     */
    suspend fun getRecommendedPrograms(
        userId: String,
        audience: AudienceType,
        userMood: Int? = null,
        userStress: Int? = null
    ): List<ProgramRecommendation> {
        val allPrograms = getAvailablePrograms(audience)
        val recommendations = mutableListOf<ProgramRecommendation>()
        
        allPrograms.forEach { program ->
            val score = calculateRecommendationScore(program, userMood, userStress)
            if (score > 0.3) { // Only recommend if score is above threshold
                recommendations.add(
                    ProgramRecommendation(
                        programId = program.id,
                        score = score,
                        reason = generateRecommendationReason(program, score),
                        priority = when {
                            score > 0.8 -> RecommendationPriority.HIGH
                            score > 0.6 -> RecommendationPriority.MEDIUM
                            else -> RecommendationPriority.LOW
                        },
                        expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
                    )
                )
            }
        }
        
        return recommendations.sortedByDescending { it.score }
    }
    
    /**
     * Record daily activity
     */
    suspend fun recordDailyActivity(
        userId: String,
        activity: DailyProgramActivity
    ): Result<Unit> {
        return try {
            _dailyActivity.value = activity
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private fun calculateProgramProgress(
        program: StructuredProgram,
        progress: UserProgramProgress
    ): ProgramProgress {
        val completionPercentage = if (program.totalSteps > 0) {
            (progress.completedSteps.size.toFloat() / program.totalSteps.toFloat()) * 100f
        } else 0f
        
        return ProgramProgress(
            currentStepIndex = progress.currentStepIndex,
            completedSteps = progress.completedSteps,
            stepProgress = progress.stepProgress,
            overallCompletionPercentage = completionPercentage,
            timeSpentMinutes = progress.totalMinutesSpent,
            lastAccessedAt = progress.lastAccessedAt
        )
    }
    
    private fun calculateRecommendationScore(
        program: StructuredProgram,
        userMood: Int?,
        userStress: Int?
    ): Float {
        var score = 0.5f // Base score
        
        // Adjust based on user mood
        userMood?.let { mood ->
            when (program.category) {
                ProgramCategory.ANXIETY -> if (mood < 3) score += 0.3f
                ProgramCategory.STRESS -> if (mood < 3) score += 0.2f
                ProgramCategory.RESILIENCE -> if (mood > 3) score += 0.2f
                else -> score
            }
        }
        
        // Adjust based on stress level
        userStress?.let { stress ->
            when (program.category) {
                ProgramCategory.STRESS -> if (stress > 3) score += 0.3f
                ProgramCategory.BURNOUT -> if (stress > 4) score += 0.4f
                ProgramCategory.SLEEP -> if (stress > 3) score += 0.2f
                else -> score
            }
        }
        
        // Adjust based on difficulty (beginner programs get higher score)
        when (program.difficulty) {
            ProgramDifficulty.BEGINNER -> score += 0.1f
            ProgramDifficulty.ADVANCED -> score -= 0.1f
            else -> score
        }
        
        return score.coerceIn(0f, 1f)
    }
    
    private fun generateRecommendationReason(
        program: StructuredProgram,
        score: Float
    ): String {
        return when {
            score > 0.8 -> "Highly recommended based on your current needs"
            score > 0.6 -> "Good match for your current situation"
            score > 0.4 -> "May help with your current challenges"
            else -> "Consider this program for additional support"
        }
    }
    
    // Mock data methods (in real app, these would load from repository)
    
    private fun loadUserProgress(userId: String, programId: String): UserProgramProgress? {
        // Mock implementation
        return null
    }
    
    private fun loadProgram(programId: String): StructuredProgram? {
        // Mock implementation
        return null
    }
    
    private fun getStudentPrograms(): List<StructuredProgram> {
        // Mock implementation - would load from repository
        return emptyList()
    }
    
    private fun getCorporatePrograms(): List<StructuredProgram> {
        // Mock implementation - would load from repository
        return emptyList()
    }
    
    private fun getPoliceMilitaryPrograms(): List<StructuredProgram> {
        // Mock implementation - would load from repository
        return emptyList()
    }
}

// Data classes for results and state

@Serializable
data class ProgramStartResult(
    val success: Boolean,
    val currentStep: ProgramStep,
    val totalSteps: Int,
    val estimatedDuration: Int
)

@Serializable
data class ProgramContinueResult(
    val success: Boolean,
    val isCompleted: Boolean,
    val currentStep: ProgramStep?,
    val stepNumber: Int? = null,
    val totalSteps: Int? = null
)

@Serializable
data class StepCompletionResult(
    val success: Boolean,
    val isProgramCompleted: Boolean,
    val nextStep: ProgramStep?,
    val nextStepNumber: Int? = null,
    val programCompleted: Boolean = false,
    val timeSpentMinutes: Int = 0,
    val exerciseCompleted: Boolean = false,
    val reflectionCompleted: Boolean = false,
    val audioCompleted: Boolean = false,
    val rating: Int? = null,
    val notes: String? = null
)

@Serializable
data class UserProgramProgress(
    val userId: String,
    val programId: String,
    val currentStepIndex: Int,
    val completedSteps: Set<Int>,
    val stepProgress: Map<Int, StepProgress>,
    val startedAt: Long,
    val lastAccessedAt: Long,
    val totalMinutesSpent: Long,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

@Serializable
enum class ProgramFlowState {
    IDLE,       // No active program
    ACTIVE,     // Currently in a program
    PAUSED,     // Program paused
    COMPLETED   // Program completed
}
