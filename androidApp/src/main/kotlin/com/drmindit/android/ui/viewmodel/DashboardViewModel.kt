package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.domain.model.Mood
import com.drmindit.shared.domain.usecase.GetUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val getSessionOfTheDayUseCase: com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _selectedMood = MutableStateFlow<Mood?>(null)
    val selectedMood: StateFlow<Mood?> = _selectedMood.asStateFlow()
    
    init {
        refreshData()
    }

    fun refreshData() {
        loadUserData()
        loadSessionOfTheDay()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getUserUseCase().fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    private fun loadSessionOfTheDay() {
        viewModelScope.launch {
            getSessionOfTheDayUseCase().fold(
                onSuccess = { session ->
                    _uiState.value = _uiState.value.copy(
                        sessionOfTheDay = session
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun selectMood(mood: Mood) {
        _selectedMood.value = mood
        // Here you would save the mood entry to the repository
    }
    
    fun refreshData() {
        loadUserData()
        loadSessionOfTheDay()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun loadRecommendedSessions(category: com.drmindit.shared.domain.model.SessionCategory? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val sessionsResult = sessionRepository.getSessions(category)
                sessionsResult.fold(
                    onSuccess = { sessions ->
                        _uiState.value = _uiState.value.copy(
                            recommendedSessions = sessions,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load sessions: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
}

data class DashboardUiState(
    val user: com.drmindit.shared.domain.model.User? = null,
    val sessionOfTheDay: com.drmindit.shared.domain.model.Session? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)