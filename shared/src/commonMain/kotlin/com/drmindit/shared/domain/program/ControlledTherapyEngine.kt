package com.drmindit.shared.domain.program

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Controlled Therapy Engine - State Machine for Therapy Flows
 * Ensures structured progression without random AI responses
 */
class ControlledTherapyEngine {
    
    private val _therapyState = MutableStateFlow<TherapyState>(TherapyState.IDLE)
    val therapyState: Flow<TherapyState> = _therapyState.asStateFlow()
    
    private val _currentFlow = MutableStateFlow<TherapyFlow?>(null)
    val currentFlow: Flow<TherapyFlow?> = _currentFlow.asStateFlow()
    
    private val _flowStep = MutableStateFlow<FlowStep?>(null)
    val flowStep: Flow<FlowStep?> = _flowStep.asStateFlow()
    
    private val _userContext = MutableStateFlow<UserContext?>(null)
    val userContext: Flow<UserContext?> = _userContext.asStateFlow()
    
    // Internal state tracking
    private var activeFlow: TherapyFlow? = null
    private var currentStepIndex: Int = 0
    private var flowHistory: MutableList<String> = mutableListOf()
    
    /**
     * Initialize therapy flow for current program step
     */
    suspend fun initializeFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): Result<FlowInitializationResult> {
        return try {
            val flow = createTherapyFlow(programStep, userContext)
            
            activeFlow = flow
            currentStepIndex = 0
            _currentFlow.value = flow
            _flowStep.value = flow.steps.first()
            _userContext.value = userContext
            _therapyState.value = TherapyState.ACTIVE
            
            Result.success(
                FlowInitializationResult(
                    success = true,
                    flowType = flow.type,
                    firstStep = flow.steps.first(),
                    totalSteps = flow.steps.size
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute current flow step
     */
    suspend fun executeCurrentStep(
        userInput: String? = null,
        exerciseResult: ExerciseResult? = null
    ): Result<FlowStepResult> {
        return try {
            val flow = activeFlow ?: return Result.failure(Exception("No active flow"))
            val currentStep = _flowStep.value ?: return Result.failure(Exception("No current step"))
            
            when (currentStep.type) {
                FlowStepType.AI_VALIDATION -> executeAIValidationStep(currentStep, userInput)
                FlowStepType.QUESTION_INQUIRY -> executeQuestionStep(currentStep, userInput)
                FlowStepType.GUIDED_EXERCISE -> executeExerciseStep(currentStep, exerciseResult)
                FlowStepType.REFLECTION_PROMPT -> executeReflectionStep(currentStep, userInput)
                FlowStepType.PROGRESS_UPDATE -> executeProgressStep(currentStep)
                FlowStepType.FLOW_COMPLETION -> executeCompletionStep(currentStep)
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Move to next step in flow
     */
    suspend fun nextStep(): Result<FlowStepResult> {
        return try {
            val flow = activeFlow ?: return Result.failure(Exception("No active flow"))
            
            currentStepIndex++
            
            if (currentStepIndex < flow.steps.size) {
                val nextStep = flow.steps[currentStepIndex]
                _flowStep.value = nextStep
                
                Result.success(
                    FlowStepResult(
                        success = true,
                        nextStep = nextStep,
                        stepNumber = currentStepIndex + 1,
                        totalSteps = flow.steps.size,
                        isFlowCompleted = false
                    )
                )
                
            } else {
                // Flow completed
                _therapyState.value = TherapyState.COMPLETED
                Result.success(
                    FlowStepResult(
                        success = true,
                        isFlowCompleted = true
                    )
                )
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Pause current flow
     */
    suspend fun pauseFlow(): Result<Unit> {
        return try {
            _therapyState.value = TherapyState.PAUSED
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resume paused flow
     */
    suspend fun resumeFlow(): Result<Unit> {
        return try {
            if (_therapyState.value == TherapyState.PAUSED) {
                _therapyState.value = TherapyState.ACTIVE
                Result.success(Unit)
            } else {
                Result.failure(Exception("Flow is not paused"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset flow to beginning
     */
    suspend fun resetFlow(): Result<Unit> {
        return try {
            val flow = activeFlow ?: return Result.failure(Exception("No active flow"))
            
            currentStepIndex = 0
            _flowStep.value = flow.steps.first()
            _therapyState.value = TherapyState.ACTIVE
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create therapy flow based on program step
     */
    private fun createTherapyFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return when (programStep.type) {
            StepType.INSTRUCTION -> createInstructionFlow(programStep, userContext)
            StepType.QUESTION -> createQuestionFlow(programStep, userContext)
            StepType.GUIDED_EXERCISE -> createExerciseFlow(programStep, userContext)
            StepType.REFLECTION -> createReflectionFlow(programStep, userContext)
            StepType.COMPLETION -> createCompletionFlow(programStep, userContext)
        }
    }
    
    /**
     * Create instruction flow
     */
    private fun createInstructionFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return TherapyFlow(
            id = "instruction_${programStep.id}",
            type = FlowType.INSTRUCTION,
            steps = listOf(
                FlowStep(
                    id = "validate_start",
                    type = FlowStepType.AI_VALIDATION,
                    title = "Welcome to Today's Session",
                    content = generateValidationContent(userContext),
                    requiresInput = false,
                    estimatedMinutes = 2
                ),
                FlowStep(
                    id = "provide_instruction",
                    type = FlowStepType.QUESTION_INQUIRY,
                    title = programStep.title,
                    content = programStep.content,
                    requiresInput = false,
                    estimatedMinutes = programStep.estimatedMinutes - 2
                ),
                FlowStep(
                    id = "check_understanding",
                    type = FlowStepType.REFLECTION_PROMPT,
                    title = "Quick Check-in",
                    content = "How does this information feel for you?",
                    requiresInput = true,
                    estimatedMinutes = 3
                )
            )
        )
    }
    
    /**
     * Create question flow
     */
    private fun createQuestionFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return TherapyFlow(
            id = "question_${programStep.id}",
            type = FlowType.INQUIRY,
            steps = listOf(
                FlowStep(
                    id = "validate_feeling",
                    type = FlowStepType.AI_VALIDATION,
                    title = "Let's Explore Together",
                    content = generateValidationContent(userContext),
                    requiresInput = false,
                    estimatedMinutes = 2
                ),
                FlowStep(
                    id = "ask_question",
                    type = FlowStepType.QUESTION_INQUIRY,
                    title = programStep.title,
                    content = programStep.content,
                    requiresInput = true,
                    estimatedMinutes = programStep.estimatedMinutes - 2
                ),
                FlowStep(
                    id = "process_response",
                    type = FlowStepType.PROGRESS_UPDATE,
                    title = "Thank You for Sharing",
                    content = "Your response helps us understand your journey better.",
                    requiresInput = false,
                    estimatedMinutes = 1
                )
            )
        )
    }
    
    /**
     * Create exercise flow
     */
    private fun createExerciseFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return TherapyFlow(
            id = "exercise_${programStep.id}",
            type = FlowType.EXERCISE,
            steps = listOf(
                FlowStep(
                    id = "prepare_exercise",
                    type = FlowStepType.AI_VALIDATION,
                    title = "Ready for Today's Exercise",
                    content = generateValidationContent(userContext),
                    requiresInput = false,
                    estimatedMinutes = 2
                ),
                FlowStep(
                    id = "explain_exercise",
                    type = FlowStepType.QUESTION_INQUIRY,
                    title = "Understanding the Exercise",
                    content = programStep.content,
                    requiresInput = false,
                    estimatedMinutes = 3
                ),
                FlowStep(
                    id = "guided_exercise",
                    type = FlowStepType.GUIDED_EXERCISE,
                    title = programStep.title,
                    content = "Let's practice this exercise together...",
                    requiresInput = false,
                    estimatedMinutes = programStep.estimatedMinutes - 5,
                    exerciseType = programStep.exerciseType,
                    audioSession = programStep.audioSession
                ),
                FlowStep(
                    id = "exercise_reflection",
                    type = FlowStepType.REFLECTION_PROMPT,
                    title = "How Did That Feel?",
                    content = "Take a moment to reflect on your experience...",
                    requiresInput = true,
                    estimatedMinutes = 3
                )
            )
        )
    }
    
    /**
     * Create reflection flow
     */
    private fun createReflectionFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return TherapyFlow(
            id = "reflection_${programStep.id}",
            type = FlowType.REFLECTION,
            steps = listOf(
                FlowStep(
                    id = "validate_reflection",
                    type = FlowStepType.AI_VALIDATION,
                    title = "Time for Reflection",
                    content = generateValidationContent(userContext),
                    requiresInput = false,
                    estimatedMinutes = 2
                ),
                FlowStep(
                    id = "reflection_prompt",
                    type = FlowStepType.REFLECTION_PROMPT,
                    title = programStep.title,
                    content = programStep.content,
                    requiresInput = true,
                    estimatedMinutes = programStep.estimatedMinutes - 2
                ),
                FlowStep(
                    id = "process_reflection",
                    type = FlowStepType.PROGRESS_UPDATE,
                    title = "Your Reflection Matters",
                    content = "Thank you for sharing your thoughts. This helps with your progress.",
                    requiresInput = false,
                    estimatedMinutes = 1
                )
            )
        )
    }
    
    /**
     * Create completion flow
     */
    private fun createCompletionFlow(
        programStep: ProgramStep,
        userContext: UserContext
    ): TherapyFlow {
        return TherapyFlow(
            id = "completion_${programStep.id}",
            type = FlowType.COMPLETION,
            steps = listOf(
                FlowStep(
                    id = "celebrate_progress",
                    type = FlowStepType.AI_VALIDATION,
                    title = "Great Job Today!",
                    content = generateCompletionValidation(userContext),
                    requiresInput = false,
                    estimatedMinutes = 2
                ),
                FlowStep(
                    id = "completion_message",
                    type = FlowStepType.FLOW_COMPLETION,
                    title = programStep.title,
                    content = programStep.content,
                    requiresInput = false,
                    estimatedMinutes = programStep.estimatedMinutes - 2
                )
            )
        )
    }
    
    /**
     * Execute AI validation step
     */
    private suspend fun executeAIValidationStep(
        step: FlowStep,
        userInput: String?
    ): Result<FlowStepResult> {
        // AI validation doesn't require user input, just provides emotional validation
        val validationResponse = generateAIValidation(step.content, _userContext.value)
        
        // Log the validation
        flowHistory.add("AI_VALIDATION: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                response = validationResponse,
                requiresNextStep = true
            )
        )
    }
    
    /**
     * Execute question step
     */
    private suspend fun executeQuestionStep(
        step: FlowStep,
        userInput: String?
    ): Result<FlowStepResult> {
        // Question step presents the question to user
        flowHistory.add("QUESTION: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                question = step.content,
                requiresInput = step.requiresInput
            )
        )
    }
    
    /**
     * Execute exercise step
     */
    private suspend fun executeExerciseStep(
        step: FlowStep,
        exerciseResult: ExerciseResult?
    ): Result<FlowStepResult> {
        // Exercise step guides user through exercise
        flowHistory.add("EXERCISE: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                exerciseInstruction = step.content,
                exerciseType = step.exerciseType,
                audioSession = step.audioSession,
                requiresExerciseCompletion = true
            )
        )
    }
    
    /**
     * Execute reflection step
     */
    private suspend fun executeReflectionStep(
        step: FlowStep,
        userInput: String?
    ): Result<FlowStepResult> {
        // Reflection step processes user's reflection
        val reflectionResponse = if (userInput != null) {
            generateReflectionResponse(userInput, _userContext.value)
        } else {
            step.content
        }
        
        flowHistory.add("REFLECTION: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                response = reflectionResponse,
                requiresInput = step.requiresInput
            )
        )
    }
    
    /**
     * Execute progress step
     */
    private suspend fun executeProgressStep(
        step: FlowStep
    ): Result<FlowStepResult> {
        // Progress step updates user on their journey
        flowHistory.add("PROGRESS: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                response = step.content,
                requiresNextStep = true
            )
        )
    }
    
    /**
     * Execute completion step
     */
    private suspend fun executeCompletionStep(
        step: FlowStep
    ): Result<FlowStepResult> {
        // Completion step finalizes the flow
        flowHistory.add("COMPLETION: ${step.id}")
        
        return Result.success(
            FlowStepResult(
                success = true,
                response = step.content,
                isFlowCompleted = true
            )
        )
    }
    
    /**
     * Generate AI validation content
     */
    private fun generateValidationContent(userContext: UserContext?): String {
        val mood = userContext?.currentMood ?: 5
        val stress = userContext?.currentStress ?: 5
        
        return when {
            mood >= 7 && stress <= 3 -> "I'm glad you're feeling good today! Let's make the most of this positive energy."
            mood <= 3 || stress >= 7 -> "I notice things might be challenging right now. That's okay, and I'm here to support you through this session."
            else -> "Welcome to today's session. Let's work together on your mental wellness journey."
        }
    }
    
    /**
     * Generate completion validation
     */
    private fun generateCompletionValidation(userContext: UserContext?): String {
        return "You've completed another important step in your journey! Every session builds on the last, and you're making real progress. Take a moment to acknowledge your effort and commitment."
    }
    
    /**
     * Generate AI validation response
     */
    private fun generateAIValidation(content: String, userContext: UserContext?): String {
        // This would integrate with AI for emotional validation
        // For now, return structured validation
        return "I hear you, and your feelings are completely valid. $content"
    }
    
    /**
     * Generate reflection response
     */
    private fun generateReflectionResponse(userInput: String, userContext: UserContext?): String {
        // This would integrate with AI for reflection processing
        // For now, return structured reflection response
        return "Thank you for sharing that reflection. Your insights are valuable for your journey. Let's continue building on this awareness."
    }
}

// Data classes for controlled therapy engine

@Serializable
data class TherapyFlow(
    val id: String,
    val type: FlowType,
    val steps: List<FlowStep>
)

@Serializable
data class FlowStep(
    val id: String,
    val type: FlowStepType,
    val title: String,
    val content: String,
    val requiresInput: Boolean,
    val estimatedMinutes: Int,
    val exerciseType: ExerciseType? = null,
    val audioSession: AudioSession? = null
)

@Serializable
data class UserContext(
    val currentMood: Int,        // 1-10 scale
    val currentStress: Int,      // 1-10 scale
    val programId: String,
    val currentDay: Int,
    val completedDays: Set<Int>,
    val recentInsights: List<String> = emptyList()
)

@Serializable
data class ExerciseResult(
    val completed: Boolean,
    val timeSpentMinutes: Int,
    val rating: Int? = null,
    val notes: String? = null
)

@Serializable
data class FlowInitializationResult(
    val success: Boolean,
    val flowType: FlowType,
    val firstStep: FlowStep,
    val totalSteps: Int
)

@Serializable
data class FlowStepResult(
    val success: Boolean,
    val response: String? = null,
    val question: String? = null,
    val exerciseInstruction: String? = null,
    val exerciseType: ExerciseType? = null,
    val audioSession: AudioSession? = null,
    val nextStep: FlowStep? = null,
    val stepNumber: Int? = null,
    val totalSteps: Int? = null,
    val requiresInput: Boolean = false,
    val requiresExerciseCompletion: Boolean = false,
    val requiresNextStep: Boolean = false,
    val isFlowCompleted: Boolean = false
)

@Serializable
enum class TherapyState {
    IDLE,       // No active flow
    ACTIVE,     // Currently in flow
    PAUSED,     // Flow paused
    COMPLETED   // Flow completed
}

@Serializable
enum class FlowType {
    INSTRUCTION,    // Educational content flow
    INQUIRY,        // Question and answer flow
    EXERCISE,       // Guided exercise flow
    REFLECTION,     // Self-reflection flow
    COMPLETION      // Session completion flow
}

@Serializable
enum class FlowStepType {
    AI_VALIDATION,      // Emotional validation by AI
    QUESTION_INQUIRY,   // Present question to user
    GUIDED_EXERCISE,    // Guide through exercise
    REFLECTION_PROMPT,  // Ask for reflection
    PROGRESS_UPDATE,    // Update on progress
    FLOW_COMPLETION     // Complete the flow
}
