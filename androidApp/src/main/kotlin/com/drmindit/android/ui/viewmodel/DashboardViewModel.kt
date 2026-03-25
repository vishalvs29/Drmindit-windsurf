package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.data.repository.AuthRepository
import com.drmindit.shared.data.repository.SessionRepositoryImpl
import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val sessionRepository: SessionRepositoryImpl = SessionRepositoryImpl(),
    private val getSessionOfTheDayUseCase: GetSessionOfTheDayUseCase = GetSessionOfTheDayUseCase(sessionRepository)
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadUserData()
        loadSessionOfTheDay()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val userResult = authRepository.getCurrentUser()
                userResult.fold(
                    onSuccess = { user ->
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false,
                            error = null
                        )
                        
                        // Load user analytics if user exists
                        user?.let { loadUserAnalytics(it.id) }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load user data: ${error.message}"
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
    
    private fun loadSessionOfTheDay() {
        viewModelScope.launch {
            try {
                val sessionResult = getSessionOfTheDayUseCase()
                sessionResult.fold(
                    onSuccess = { session ->
                        _uiState.value = _uiState.value.copy(
                            sessionOfTheDay = session,
                            recommendedSessions = listOf(session) // Start with session of the day
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load session of the day: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Unexpected error loading session: ${e.message}"
                )
            }
        }
    }
    
    private fun loadUserAnalytics(userId: String) {
        viewModelScope.launch {
            try {
                // In a real implementation, this would fetch from analytics repository
                val mockAnalytics = com.drmindit.shared.domain.model.UserAnalytics(
                    userId = userId,
                    totalMindfulMinutes = 145,
                    currentStreak = 7,
                    longestStreak = 14,
                    sessionsCompleted = 23,
                    averageSessionDuration = 12.5f,
                    moodEntries = emptyList(),
                    weeklyProgress = emptyList(),
                    monthlyInsights = com.drmindit.shared.domain.model.MonthlyInsights(
                        month = "March 2024",
                        totalMinutes = 145,
                        mostActiveCategory = com.drmindit.shared.domain.model.SessionCategory.MINDFULNESS,
                        improvementAreas = listOf("Sleep Quality", "Stress Management"),
                        achievements = emptyList()
                    )
                )
                
                _uiState.value = _uiState.value.copy(analytics = mockAnalytics)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load analytics: ${e.message}"
                )
            }
        }
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
    val user: User? = null,
    val sessionOfTheDay: Session? = null,
    val recommendedSessions: List<Session> = emptyList(),
    val analytics: com.drmindit.shared.domain.model.UserAnalytics? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
