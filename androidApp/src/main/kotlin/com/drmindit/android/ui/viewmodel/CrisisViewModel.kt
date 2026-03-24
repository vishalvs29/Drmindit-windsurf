package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.data.repository.CrisisDetectionResult
import com.drmindit.shared.data.repository.CrisisRepository
import com.drmindit.shared.data.repository.CrisisSeverity
import com.drmindit.shared.data.repository.EmergencyHelpline
import com.drmindit.shared.domain.model.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrisisViewModel(
    private val crisisRepository: CrisisRepository = CrisisRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CrisisUiState())
    val uiState: StateFlow<CrisisUiState> = _uiState.asStateFlow()
    
    private val _crisisDetectionResult = MutableStateFlow<CrisisDetectionResult?>(null)
    val crisisDetectionResult: StateFlow<CrisisDetectionResult?> = _crisisDetectionResult.asStateFlow()
    
    init {
        loadEmergencyHelplines()
    }
    
    fun detectCrisis(
        moodScore: Int? = null,
        notes: String? = null,
        moodType: Mood? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Get current user ID (this would come from auth repository)
                val userId = "current_user_id" // Placeholder
                
                val result = crisisRepository.detectCrisis(
                    userId = userId,
                    moodScore = moodScore,
                    notes = notes,
                    moodType = moodType
                )
                
                result.fold(
                    onSuccess = { detectionResult ->
                        _crisisDetectionResult.value = detectionResult
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            showCrisisModal = detectionResult.isCrisis
                        )
                        
                        // Log the crisis event if detected
                        if (detectionResult.isCrisis) {
                            logCrisisEvent(userId, detectionResult)
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun manualCrisisTrigger() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val userId = "current_user_id" // Placeholder
                
                val result = crisisRepository.logCrisisEvent(
                    userId = userId,
                    triggerReason = com.drmindit.shared.data.repository.CrisisTriggerReason.MANUAL_TRIGGER,
                    severity = com.drmindit.shared.data.repository.CrisisSeverity.HIGH,
                    contextData = mapOf(
                        "trigger_source" to "manual_button",
                        "timestamp" to System.currentTimeMillis()
                    )
                )
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            showCrisisModal = true
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun dismissCrisisModal() {
        _crisisDetectionResult.value = null
        _uiState.value = _uiState.value.copy(showCrisisModal = false)
    }
    
    fun resolveCrisis() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val userId = "current_user_id" // Placeholder
                val currentCrisis = _crisisDetectionResult.value ?: return@launch
                
                // Log resolution
                crisisRepository.logCrisisEvent(
                    userId = userId,
                    triggerReason = com.drmindit.shared.data.repository.CrisisTriggerReason.MANUAL_TRIGGER,
                    severity = com.drmindit.shared.data.repository.CrisisSeverity.LOW,
                    contextData = mapOf(
                        "action" to "user_resolved",
                        "original_severity" to currentCrisis.severity.name,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
                
                _crisisDetectionResult.value = null
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showCrisisModal = false,
                    showGroundingExercise = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun dismissGroundingExercise() {
        _uiState.value = _uiState.value.copy(showGroundingExercise = false)
    }
    
    fun completeGroundingExercise() {
        _uiState.value = _uiState.value.copy(showGroundingExercise = false)
        // Optionally log completion for analytics
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun loadEmergencyHelplines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = crisisRepository.getEmergencyHelplines()
                result.fold(
                    onSuccess = { helplines ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            emergencyHelplines = helplines,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load emergency helplines: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load emergency helplines: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun logCrisisEvent(
        userId: String,
        detectionResult: CrisisDetectionResult
    ) {
        try {
            crisisRepository.logCrisisEvent(
                userId = userId,
                triggerReason = detectionResult.triggers.first(),
                severity = detectionResult.severity,
                moodScore = detectionResult.moodScore,
                contextData = mapOf(
                    "detection_timestamp" to System.currentTimeMillis(),
                    "triggers" to detectionResult.triggers.map { it.name },
                    "notes" to (detectionResult.notes ?: ""),
                    "mood_type" to (detectionResult.moodType?.name ?: "")
                )
            )
        } catch (e: Exception) {
            // Log error but don't fail the UI
            println("Failed to log crisis event: ${e.message}")
        }
    }
}

data class CrisisUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCrisisModal: Boolean = false,
    val showGroundingExercise: Boolean = false,
    val emergencyHelplines: List<EmergencyHelpline> = emptyList()
)
