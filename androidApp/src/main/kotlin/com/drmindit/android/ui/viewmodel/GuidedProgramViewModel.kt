package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.domain.program.GuidedProgramEngine
import com.drmindit.shared.domain.program.ProgramState
import com.drmindit.shared.domain.program.UserProgramProgress
import com.drmindit.shared.domain.program.ProgramStartResult
import com.drmindit.shared.domain.program.ProgramContinueResult
import com.drmindit.shared.domain.program.StepCompletionResult
import com.drmindit.shared.domain.program.GuidedProgram
import com.drmindit.shared.domain.program.ProgramDay
import com.drmindit.shared.domain.program.ProgramStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Guided Program ViewModel - Manages structured program state
 * Replaces chat-focused ViewModel with program-driven approach
 */
@HiltViewModel
class GuidedProgramViewModel @Inject constructor(
    private val guidedProgramEngine: GuidedProgramEngine
) : ViewModel() {
    
    // Program state
    private val _activeProgram = MutableStateFlow<GuidedProgram?>(null)
    val activeProgram: StateFlow<GuidedProgram?> = _activeProgram.asStateFlow()
    
    private val _currentDay = MutableStateFlow<ProgramDay?>(null)
    val currentDay: StateFlow<ProgramDay?> = _currentDay.asStateFlow()
    
    private val _currentStep = MutableStateFlow<ProgramStep?>(null)
    val currentStep: StateFlow<ProgramStep?> = _currentStep.asStateFlow()
    
    private val _programState = MutableStateFlow<ProgramState>(ProgramState.NOT_STARTED)
    val programState: StateFlow<ProgramState> = _programState.asStateFlow()
    
    private val _userProgress = MutableStateFlow<UserProgramProgress?>(null)
    val userProgress: StateFlow<UserProgramProgress?> = _userProgress.asStateFlow()
    
    private val _availablePrograms = MutableStateFlow<List<GuidedProgram>>(emptyList())
    val availablePrograms: StateFlow<List<GuidedProgram>> = _availablePrograms.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Internal state
    private var currentUserId: String = "default_user" // Would come from auth
    
    init {
        // Collect engine state
        viewModelScope.launch {
            guidedProgramEngine.activeProgram.collect { program ->
                _activeProgram.value = program
            }
        }
        
        viewModelScope.launch {
            guidedProgramEngine.currentDay.collect { day ->
                _currentDay.value = day
            }
        }
        
        viewModelScope.launch {
            guidedProgramEngine.currentStep.collect { step ->
                _currentStep.value = step
            }
        }
        
        viewModelScope.launch {
            guidedProgramEngine.programState.collect { state ->
                _programState.value = state
            }
        }
        
        viewModelScope.launch {
            guidedProgramEngine.userProgress.collect { progress ->
                _userProgress.value = progress
            }
        }
    }
    
    /**
     * Load available programs
     */
    fun loadAvailablePrograms() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get all available programs
                val programs = listOf(
                    createExamAnxietyProgram(),
                    createStressManagementProgram(),
                    createConfidenceBuildingProgram()
                )
                
                _availablePrograms.value = programs
                _isLoading.value = false
                
                Timber.d("🎯 Loaded ${programs.size} available programs")
                
            } catch (e: Exception) {
                Timber.e("❌ Error loading programs: ${e.message}")
                _errorMessage.value = "Failed to load programs: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start a new program
     */
    fun startProgram(programId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                Timber.d("🎯 Starting program: $programId for user: $currentUserId")
                
                val result = guidedProgramEngine.startProgram(currentUserId, programId)
                
                if (result.isSuccess) {
                    val startResult = result.getOrThrow()
                    Timber.d("✅ Program started successfully: ${startResult.programName}")
                    
                    // Save progress to local storage/database
                    saveProgressToStorage()
                    
                } else {
                    val error = result.exceptionOrNull()
                    Timber.e("❌ Failed to start program: ${error?.message}")
                    _errorMessage.value = "Failed to start program: ${error?.message}"
                }
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                Timber.e("❌ Error starting program: ${e.message}")
                _errorMessage.value = "Error starting program: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Continue existing program
     */
    fun continueProgram() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val savedProgress = loadProgressFromStorage()
                
                if (savedProgress != null) {
                    Timber.d("🎯 Continuing program: ${savedProgress.programId}")
                    
                    val result = guidedProgramEngine.continueProgram(currentUserId, savedProgress)
                    
                    if (result.isSuccess) {
                        val continueResult = result.getOrThrow()
                        Timber.d("✅ Program continued successfully")
                        
                        if (continueResult.isCompleted) {
                            Timber.d("🎉 Program already completed!")
                        }
                        
                    } else {
                        val error = result.exceptionOrNull()
                        Timber.e("❌ Failed to continue program: ${error?.message}")
                        _errorMessage.value = "Failed to continue program: ${error?.message}"
                    }
                    
                } else {
                    Timber.w("⚠️ No saved progress found")
                    _errorMessage.value = "No saved progress found"
                }
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                Timber.e("❌ Error continuing program: ${e.message}")
                _errorMessage.value = "Error continuing program: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Complete current step
     */
    fun completeCurrentStep(
        timeSpentMinutes: Int,
        reflectionResponse: String? = null,
        exerciseCompleted: Boolean = false,
        rating: Int? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val stepResult = StepCompletionResult(
                    timeSpentMinutes = timeSpentMinutes,
                    reflectionResponse = reflectionResponse,
                    exerciseCompleted = exerciseCompleted,
                    rating = rating
                )
                
                Timber.d("🎯 Completing current step with result: $stepResult")
                
                val result = guidedProgramEngine.completeCurrentStep(stepResult)
                
                if (result.isSuccess) {
                    val progressResult = result.getOrThrow()
                    Timber.d("✅ Step completed successfully")
                    
                    if (progressResult.isProgramCompleted) {
                        Timber.d("🎉 Program completed!")
                        handleProgramCompletion()
                    } else if (progressResult.isDayCompleted) {
                        Timber.d("📅 Day completed! Moving to next day.")
                    }
                    
                    // Save updated progress
                    saveProgressToStorage()
                    
                } else {
                    val error = result.exceptionOrNull()
                    Timber.e("❌ Failed to complete step: ${error?.message}")
                    _errorMessage.value = "Failed to complete step: ${error?.message}"
                }
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                Timber.e("❌ Error completing step: ${e.message}")
                _errorMessage.value = "Error completing step: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Reset program progress
     */
    fun resetProgram() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                Timber.d("🔄 Resetting program progress")
                
                // Reset engine state
                guidedProgramEngine.resetFlow()
                
                // Clear local storage
                clearProgressStorage()
                
                // Reset state
                _activeProgram.value = null
                _currentDay.value = null
                _currentStep.value = null
                _programState.value = ProgramState.NOT_STARTED
                _userProgress.value = null
                
                _isLoading.value = false
                
                Timber.d("✅ Program reset successfully")
                
            } catch (e: Exception) {
                Timber.e("❌ Error resetting program: ${e.message}")
                _errorMessage.value = "Error resetting program: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Handle program completion
     */
    private fun handleProgramCompletion() {
        viewModelScope.launch {
            try {
                val progress = _userProgress.value
                
                if (progress != null) {
                    Timber.d("🎉 Handling program completion for ${progress.programId}")
                    
                    // Generate completion certificate/data
                    val completionData = mapOf(
                        "programId" to progress.programId,
                        "userId" to progress.userId,
                        "completedAt" to System.currentTimeMillis(),
                        "totalDays" to progress.completedDays.size,
                        "totalMinutes" to progress.totalMinutesSpent,
                        "streakDays" to progress.streakDays
                    )
                    
                    // Save completion data
                    saveCompletionData(completionData)
                    
                    Timber.d("✅ Program completion handled successfully")
                }
                
            } catch (e: Exception) {
                Timber.e("❌ Error handling program completion: ${e.message}")
            }
        }
    }
    
    /**
     * Save progress to local storage
     */
    private suspend fun saveProgressToStorage() {
        try {
            val progress = _userProgress.value
            
            if (progress != null) {
                // In a real app, this would save to database/local storage
                Timber.d("💾 Saving progress to storage: ${progress.programId}, Day ${progress.currentDay}")
                
                // Mock save implementation
                // database.userProgressDao().insert(progress)
                
                Timber.d("✅ Progress saved successfully")
            }
            
        } catch (e: Exception) {
            Timber.e("❌ Error saving progress: ${e.message}")
        }
    }
    
    /**
     * Load progress from local storage
     */
    private suspend fun loadProgressFromStorage(): UserProgramProgress? {
        return try {
            // In a real app, this would load from database/local storage
            Timber.d("📂 Loading progress from storage for user: $currentUserId")
            
            // Mock load implementation - return null for now
            // return database.userProgressDao().getByUserId(currentUserId)
            
            null
            
        } catch (e: Exception) {
            Timber.e("❌ Error loading progress: ${e.message}")
            null
        }
    }
    
    /**
     * Clear progress storage
     */
    private suspend fun clearProgressStorage() {
        try {
            Timber.d("🗑️ Clearing progress storage for user: $currentUserId")
            
            // In a real app, this would clear database/local storage
            // database.userProgressDao().deleteByUserId(currentUserId)
            
            Timber.d("✅ Progress storage cleared")
            
        } catch (e: Exception) {
            Timber.e("❌ Error clearing progress storage: ${e.message}")
        }
    }
    
    /**
     * Save completion data
     */
    private suspend fun saveCompletionData(completionData: Map<String, Any>) {
        try {
            Timber.d("🏆 Saving completion data: $completionData")
            
            // In a real app, this would save to database/analytics
            // database.completionDao().insert(completionData)
            
            Timber.d("✅ Completion data saved")
            
        } catch (e: Exception) {
            Timber.e("❌ Error saving completion data: ${e.message}")
        }
    }
    
    /**
     * Create Exam Anxiety Program
     */
    private fun createExamAnxietyProgram(): GuidedProgram {
        // This would normally come from the engine
        return GuidedProgram(
            id = "exam_anxiety_7day",
            name = "Exam Anxiety Relief Program",
            description = "7-day structured program to overcome exam anxiety and build confidence",
            duration = com.drmindit.shared.domain.program.ProgramDuration.DAYS_7,
            category = com.drmindit.shared.domain.program.ProgramCategory.ANXIETY,
            difficulty = com.drmindit.shared.domain.program.ProgramDifficulty.BEGINNER,
            days = listOf(
                ProgramDay(
                    day = 1,
                    title = "Understanding Exam Anxiety",
                    description = "Learn what causes exam anxiety and how it affects you",
                    steps = listOf(
                        ProgramStep(
                            id = "day1_intro",
                            type = com.drmindit.shared.domain.program.StepType.INSTRUCTION,
                            title = "Welcome to Your Journey",
                            content = "Let's understand what exam anxiety is and why it happens...",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day1_question",
                            type = com.drmindit.shared.domain.program.StepType.QUESTION,
                            title = "When Do You Feel Anxious?",
                            content = "Think about the specific situations that trigger your exam anxiety...",
                            estimatedMinutes = 3
                        ),
                        ProgramStep(
                            id = "day1_exercise",
                            type = com.drmindit.shared.domain.program.StepType.GUIDED_EXERCISE,
                            title = "Anxiety Awareness Exercise",
                            content = "Let's practice identifying your anxiety symptoms...",
                            estimatedMinutes = 10
                        ),
                        ProgramStep(
                            id = "day1_reflection",
                            type = com.drmindit.shared.domain.program.StepType.REFLECTION,
                            title = "Reflection on Your Patterns",
                            content = "What did you discover about your anxiety patterns?",
                            estimatedMinutes = 5
                        ),
                        ProgramStep(
                            id = "day1_completion",
                            type = com.drmindit.shared.domain.program.StepType.COMPLETION,
                            title = "Day 1 Complete!",
                            content = "Great job! You've taken the first step...",
                            estimatedMinutes = 2
                        )
                    )
                )
                // Additional days would be implemented here
            )
        )
    }
    
    /**
     * Create Stress Management Program
     */
    private fun createStressManagementProgram(): GuidedProgram {
        return GuidedProgram(
            id = "stress_management_14day",
            name = "Stress Management Program",
            description = "14-day comprehensive program for stress management and resilience",
            duration = com.drmindit.shared.domain.program.ProgramDuration.DAYS_14,
            category = com.drmindit.shared.domain.program.ProgramCategory.STRESS,
            difficulty = com.drmindit.shared.domain.program.ProgramDifficulty.INTERMEDIATE,
            days = emptyList() // Would implement full 14-day program
        )
    }
    
    /**
     * Create Confidence Building Program
     */
    private fun createConfidenceBuildingProgram(): GuidedProgram {
        return GuidedProgram(
            id = "confidence_building_21day",
            name = "Confidence Building Program",
            description = "21-day program to build unshakable confidence",
            duration = com.drmindit.shared.domain.program.ProgramDuration.DAYS_21,
            category = com.drmindit.shared.domain.program.ProgramCategory.CONFIDENCE,
            difficulty = com.drmindit.shared.domain.program.ProgramDifficulty.ADVANCED,
            days = emptyList() // Would implement full 21-day program
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        Timber.d("🧹 GuidedProgramViewModel cleared")
    }
}
