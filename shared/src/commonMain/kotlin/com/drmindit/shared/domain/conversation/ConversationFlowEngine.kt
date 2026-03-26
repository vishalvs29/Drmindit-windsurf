package com.drmindit.shared.domain.conversation

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conversation Flow Engine
 * Manages conversation states, flows, and transitions
 */
@Singleton
class ConversationFlowEngine @Inject constructor(
    private val intentDetector: IntentDetector,
    private val crisisDetector: CrisisDetector,
    private val responseGenerator: ResponseGenerator,
    private val flowDefinitions: FlowDefinitions,
    private val userProfileManager: UserProfileManager
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    
    private val _currentContext = MutableStateFlow<ConversationContext>(
        ConversationContext(
            state = ConversationState.GENERAL_CHAT,
            step = 0,
            maxSteps = 1
        )
    )
    val currentContext: StateFlow<ConversationContext> = _currentContext.asStateFlow()
    
    private val _conversationHistory = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversationHistory: StateFlow<List<ConversationMessage>> = _conversationHistory.asStateFlow()
    
    private val _activeFlow = MutableStateFlow<ConversationFlow?>(null)
    val activeFlow: StateFlow<ConversationFlow?> = _activeFlow.asStateFlow()
    
    /**
     * Process user message and generate response
     */
    suspend fun processUserMessage(
        userId: String,
        message: String,
        userProfile: UserProfile
    ): Flow<ConversationMessage> = flow {
        mutex.withLock {
            try {
                // Detect crisis first (overrides all other flows)
                val crisisSeverity = crisisDetector.detectCrisis(message, userProfile)
                if (crisisSeverity >= Severity.CRITICAL) {
                    emitAll(handleCrisisIntervention(userId, message, userProfile))
                    return@flow
                }
                
                // Detect user intent
                val detectedIntent = intentDetector.detectIntent(message, userProfile)
                
                // Get current context
                val currentCtx = _currentContext.value
                
                // Determine if we need to transition to a new flow
                val newFlow = determineFlow(detectedIntent, currentCtx, userProfile)
                
                if (newFlow != _activeFlow.value) {
                    // Start new flow
                    _activeFlow.value = newFlow
                    _currentContext.value = currentCtx.copy(
                        state = newFlow.state,
                        step = 0,
                        maxSteps = newFlow.steps.size,
                        userIntent = detectedIntent.name,
                        timestamp = System.currentTimeMillis()
                    )
                }
                
                // Process message in current flow
                val response = processMessageInFlow(userId, message, userProfile, detectedIntent)
                emit(response)
                
            } catch (e: Exception) {
                emit(createErrorResponse(e))
            }
        }
    }
    
    /**
     * Handle crisis intervention
     */
    private suspend fun handleCrisisIntervention(
        userId: String,
        message: String,
        userProfile: UserProfile
    ): Flow<ConversationMessage> = flow {
        // Override to crisis state
        _currentContext.value = ConversationContext(
            state = ConversationState.CRISIS,
            severity = Severity.CRITICAL,
            timestamp = System.currentTimeMillis()
        )
        
        // Generate crisis response
        val crisisResponse = responseGenerator.generateCrisisResponse(message, userProfile)
        
        val responseMessage = ConversationMessage(
            id = generateMessageId(),
            type = MessageType.CRISIS_INTERVENTION,
            content = crisisResponse.content,
            sender = MessageSender.CRISIS,
            context = _currentContext.value,
            metadata = MessageMetadata(
                severity = Severity.CRITICAL,
                emotionalTone = EmotionalTone.PANICKED,
                detectedIntent = UserIntent.CRISIS_HELP,
                isFollowUpRequired = true,
                nextStepHint = "Immediate safety assessment"
            )
        )
        
        emit(responseMessage)
        
        // Add emergency resources
        val resources = responseGenerator.getEmergencyResources(userProfile)
        resources.forEach { resource ->
            val resourceMessage = ConversationMessage(
                id = generateMessageId(),
                type = MessageType.RESOURCE_LINK,
                content = resource.description,
                sender = MessageSender.SYSTEM,
                context = _currentContext.value,
                metadata = MessageMetadata(
                    severity = Severity.CRITICAL,
                    entities = mapOf("resource_id" to resource.id, "resource_type" to resource.type.name)
                )
            )
            emit(resourceMessage)
        }
    }
    
    /**
     * Determine appropriate flow based on intent and context
     */
    private fun determineFlow(
        intent: UserIntent,
        currentContext: ConversationContext,
        userProfile: UserProfile
    ): ConversationFlow? {
        return when (intent) {
            UserIntent.ANXIETY_HELP -> flowDefinitions.getAnxietyFlow()
            UserIntent.OVERTHINKING_HELP -> flowDefinitions.getOverthinkingFlow()
            UserIntent.LOW_MOOD_SUPPORT -> flowDefinitions.getLowMoodFlow()
            UserIntent.SLEEP_HELP -> flowDefinitions.getSleepFlow()
            UserIntent.STRESS_RELIEF -> flowDefinitions.getStressReliefFlow()
            UserIntent.CHECKIN -> flowDefinitions.getCheckinFlow()
            UserIntent.RESOURCE_REQUEST -> flowDefinitions.getResourceFlow()
            UserIntent.PROGRESS_UPDATE -> flowDefinitions.getProgressFlow()
            UserIntent.CRISIS_HELP -> flowDefinitions.getCrisisFlow()
            else -> {
                // Check if we should continue current flow or start general chat
                when (currentContext.state) {
                    ConversationState.GENERAL_CHAT -> flowDefinitions.getGeneralChatFlow()
                    else -> _activeFlow.value
                }
            }
        }
    }
    
    /**
     * Process message within current flow
     */
    private suspend fun processMessageInFlow(
        userId: String,
        message: String,
        userProfile: UserProfile,
        intent: UserIntent
    ): ConversationMessage {
        val flow = _activeFlow.value ?: return createGeneralResponse(message, userProfile)
        val context = _currentContext.value
        
        // Get current step
        val currentStep = flow.steps.getOrNull(context.step)
            ?: return createFlowCompletionResponse(flow, userProfile)
        
        // Validate input if required
        if (currentStep.validationRules.isNotEmpty()) {
            val validationResult = validateInput(message, currentStep.validationRules)
            if (!validationResult.isValid) {
                return createValidationErrorResponse(validationResult.errorMessage)
            }
        }
        
        // Generate response
        val responseContent = when {
            currentStep.predefinedResponses.isNotEmpty() -> {
                // Use predefined response
                currentStep.predefinedResponses.random()
            }
            currentStep.aiResponseEnabled -> {
                // Use AI response
                responseGenerator.generateAIResponse(message, currentStep, userProfile)
            }
            else -> {
                // Default response
                generateDefaultResponse(currentStep, userProfile)
            }
        }
        
        // Create response message
        val responseMessage = ConversationMessage(
            id = generateMessageId(),
            type = MessageType.AI_RESPONSE,
            content = responseContent,
            sender = MessageSender.AI,
            context = context,
            metadata = MessageMetadata(
                detectedIntent = intent,
                severity = context.severity,
                isFollowUpRequired = currentStep.isOptional.not(),
                nextStepHint = currentStep.nextSteps.firstOrNull()
            )
        )
        
        // Add to history
        addToHistory(responseMessage)
        
        // Move to next step
        moveToNextStep(flow, userProfile)
        
        return responseMessage
    }
    
    /**
     * Move to next step in flow
     */
    private suspend fun moveToNextStep(flow: ConversationFlow, userProfile: UserProfile) {
        val context = _currentContext.value
        
        if (context.step < flow.steps.size - 1) {
            // Move to next step
            _currentContext.value = context.copy(
                step = context.step + 1,
                timestamp = System.currentTimeMillis()
            )
        } else {
            // Complete flow
            completeFlow(flow, userProfile)
        }
    }
    
    /**
     * Complete current flow
     */
    private suspend fun completeFlow(flow: ConversationFlow, userProfile: UserProfile) {
        val context = _currentContext.value
        
        // Generate completion response
        val completionResponse = responseGenerator.generateFlowCompletionResponse(flow, userProfile)
        
        val completionMessage = ConversationMessage(
            id = generateMessageId(),
            type = MessageType.AI_RESPONSE,
            content = completionResponse,
            sender = MessageSender.AI,
            context = context.copy(
                step = context.maxSteps,
                timestamp = System.currentTimeMillis()
            ),
            metadata = MessageMetadata(
                isFollowUpRequired = false,
                nextStepHint = "Return to general chat or start new flow"
            )
        )
        
        addToHistory(completionMessage)
        
        // Reset to general chat
        _currentContext.value = ConversationContext(
            state = ConversationState.GENERAL_CHAT,
            step = 0,
            maxSteps = 1,
            timestamp = System.currentTimeMillis()
        )
        
        _activeFlow.value = null
        
        // Update user progress
        userProfileManager.updateProgress(userProfile.id, flow.state)
    }
    
    /**
     * Create flow completion response
     */
    private suspend fun createFlowCompletionResponse(flow: ConversationFlow, userProfile: UserProfile): ConversationMessage {
        val completionContent = responseGenerator.generateFlowCompletionResponse(flow, userProfile)
        
        return ConversationMessage(
            id = generateMessageId(),
            type = MessageType.AI_RESPONSE,
            content = completionContent,
            sender = MessageSender.AI,
            context = _currentContext.value.copy(
                step = _currentContext.value.maxSteps,
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Create general chat response
     */
    private suspend fun createGeneralResponse(message: String, userProfile: UserProfile): ConversationMessage {
        val responseContent = responseGenerator.generateGeneralChatResponse(message, userProfile)
        
        return ConversationMessage(
            id = generateMessageId(),
            type = MessageType.AI_RESPONSE,
            content = responseContent,
            sender = MessageSender.AI,
            context = _currentContext.value,
            metadata = MessageMetadata(
                detectedIntent = UserIntent.GENERAL_CHAT,
                severity = Severity.LOW
            )
        )
    }
    
    /**
     * Create validation error response
     */
    private fun createValidationErrorResponse(errorMessage: String): ConversationMessage {
        return ConversationMessage(
            id = generateMessageId(),
            type = MessageType.AI_RESPONSE,
            content = "I didn't quite understand that. $errorMessage",
            sender = MessageSender.AI,
            context = _currentContext.value,
            metadata = MessageMetadata(
                severity = Severity.LOW,
                isFollowUpRequired = true
            )
        )
    }
    
    /**
     * Create error response
     */
    private fun createErrorResponse(exception: Exception): ConversationMessage {
        return ConversationMessage(
            id = generateMessageId(),
            type = MessageType.SYSTEM_NOTIFICATION,
            content = "I'm having trouble processing that right now. Let's try a different approach.",
            sender = MessageSender.SYSTEM,
            context = _currentContext.value,
            metadata = MessageMetadata(
                severity = Severity.LOW
            )
        )
    }
    
    /**
     * Validate user input against rules
     */
    private fun validateInput(input: String, rules: List<ValidationRule>): ValidationResult {
        for (rule in rules) {
            when (rule.type) {
                ValidationType.REQUIRED -> {
                    if (input.isBlank()) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                }
                ValidationType.MIN_LENGTH -> {
                    val minLength = rule.parameters["min_length"]?.toIntOrNull() ?: 1
                    if (input.length < minLength) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                }
                ValidationType.MAX_LENGTH -> {
                    val maxLength = rule.parameters["max_length"]?.toIntOrNull() ?: 1000
                    if (input.length > maxLength) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                }
                ValidationType.RANGE -> {
                    val value = input.toIntOrNull()
                    if (value == null) {
                        return ValidationResult(false, "Please enter a valid number")
                    }
                    val min = rule.parameters["min"]?.toIntOrNull()
                    val max = rule.parameters["max"]?.toIntOrNull()
                    if (min != null && value < min) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                    if (max != null && value > max) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                }
                ValidationType.REGEX -> {
                    val pattern = rule.parameters["pattern"] ?: return ValidationResult(true, "")
                    if (!input.matches(Regex(pattern))) {
                        return ValidationResult(false, rule.errorMessage)
                    }
                }
                ValidationType.CUSTOM -> {
                    // Custom validation would be implemented here
                }
            }
        }
        return ValidationResult(true, "")
    }
    
    /**
     * Generate default response
     */
    private fun generateDefaultResponse(step: FlowStep, userProfile: UserProfile): String {
        return when (step.expectedInputType) {
            InputType.TEXT -> "Thank you for sharing that with me. Let me help you work through this."
            InputType.MULTIPLE_CHOICE -> "I understand your choice. Let's explore that further."
            InputType.SCALE -> "I appreciate you sharing that rating with me. Let's talk about what that means for you."
            InputType.YES_NO -> "Thank you for your response. Let's build on that."
            InputType.NUMBER -> "I understand that number. Let's put that in context."
            InputType.DATE_TIME -> "I understand the timing. Let's work with that."
            InputType.EXERCISE_RESULT -> "Great job completing that exercise! How did it feel?"
            InputType.RATING -> "Thank you for that rating. Your feedback helps me support you better."
            InputType.FEEDBACK -> "I appreciate your feedback. It helps me improve my support."
            else -> "Thank you for sharing that with me."
        }
    }
    
    /**
     * Add message to conversation history
     */
    private suspend fun addToHistory(message: ConversationMessage) {
        val updatedHistory = _conversationHistory.value + message
        _conversationHistory.value = updatedHistory
        
        // Update user profile with conversation data
        userProfileManager.updateConversationHistory(message.context.userId, updatedHistory)
    }
    
    /**
     * Generate unique message ID
     */
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Start specific flow
     */
    suspend fun startFlow(userId: String, flowType: ConversationState, userProfile: UserProfile) {
        mutex.withLock {
            val flow = when (flowType) {
                ConversationState.ANXIETY -> flowDefinitions.getAnxietyFlow()
                ConversationState.OVERTHINKING -> flowDefinitions.getOverthinkingFlow()
                ConversationState.LOW_MOOD -> flowDefinitions.getLowMoodFlow()
                ConversationState.SLEEP -> flowDefinitions.getSleepFlow()
                ConversationState.CHECKIN -> flowDefinitions.getCheckinFlow()
                ConversationState.ONBOARDING -> flowDefinitions.getOnboardingFlow()
                else -> flowDefinitions.getGeneralChatFlow()
            }
            
            _activeFlow.value = flow
            _currentContext.value = ConversationContext(
                state = flow.state,
                step = 0,
                maxSteps = flow.steps.size,
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Get current conversation summary
     */
    suspend fun getConversationSummary(userId: String): ConversationSummary {
        val history = _conversationHistory.value
        val context = _currentContext.value
        val flow = _activeFlow.value
        
        return ConversationSummary(
            userId = userId,
            currentState = context.state,
            currentStep = context.step,
            totalSteps = context.maxSteps,
            totalMessages = history.size,
            activeFlow = flow?.name,
            emotionalTone = analyzeEmotionalTone(history),
            severity = context.severity,
            duration = calculateSessionDuration(history),
            outcomes = extractOutcomes(history)
        )
    }
    
    /**
     * Analyze emotional tone from conversation history
     */
    private fun analyzeEmotionalTone(history: List<ConversationMessage>): EmotionalTone {
        val userMessages = history.filter { it.sender == MessageSender.USER }
        val tones = userMessages.mapNotNull { it.metadata.emotionalTone }
        
        return when {
            tones.all { it == EmotionalTone.ANXIOUS } -> EmotionalTone.ANXIOUS
            tones.all { it == EmotionalTone.SAD } -> EmotionalTone.SAD
            tones.all { it == EmotionalTone.ANGRY } -> EmotionalTone.ANGRY
            tones.any { it == EmotionalTone.PANICKED } -> EmotionalTone.PANICKED
            tones.all { it == EmotionalTone.POSITIVE } -> EmotionalTone.POSITIVE
            tones.isEmpty() -> EmotionalTone.NEUTRAL
            else -> EmotionalTone.NEUTRAL
        }
    }
    
    /**
     * Calculate session duration
     */
    private fun calculateSessionDuration(history: List<ConversationMessage>): Int {
        if (history.isEmpty()) return 0
        
        val startTime = history.first().timestamp
        val endTime = history.last().timestamp
        return ((endTime - startTime) / 60000).toInt() // Convert to minutes
    }
    
    /**
     * Extract outcomes from conversation
     */
    private fun extractOutcomes(history: List<ConversationMessage>): List<String> {
        return history
            .filter { it.type == MessageType.AI_RESPONSE }
            .mapNotNull { it.metadata.nextStepHint }
            .distinct()
    }
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)

/**
 * Conversation summary
 */
data class ConversationSummary(
    val userId: String,
    val currentState: ConversationState,
    val currentStep: Int,
    val totalSteps: Int,
    val totalMessages: Int,
    val activeFlow: String?,
    val emotionalTone: EmotionalTone,
    val severity: Severity,
    val duration: Int,
    val outcomes: List<String>
)
