package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< Updated upstream
<<<<<<< HEAD
import com.drmindit.shared.domain.model.Mood
import com.drmindit.shared.domain.usecase.GetUserUseCase
=======
=======
>>>>>>> Stashed changes
import com.drmindit.shared.data.repository.AuthRepository
import com.drmindit.shared.data.repository.SessionRepositoryImpl
import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
<<<<<<< Updated upstream
>>>>>>> master
=======
>>>>>>> Stashed changes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
<<<<<<< Updated upstream
<<<<<<< HEAD
    private val getUserUseCase: GetUserUseCase,
    private val getSessionOfTheDayUseCase: com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
=======
    private val authRepository: AuthRepository = AuthRepository(),
    private val sessionRepository: SessionRepositoryImpl = SessionRepositoryImpl(),
    private val getSessionOfTheDayUseCase: GetSessionOfTheDayUseCase = GetSessionOfTheDayUseCase(sessionRepository)
>>>>>>> master
=======
    private val authRepository: AuthRepository = AuthRepository(),
    private val sessionRepository: SessionRepositoryImpl = SessionRepositoryImpl(),
    private val getSessionOfTheDayUseCase: GetSessionOfTheDayUseCase = GetSessionOfTheDayUseCase(sessionRepository)
>>>>>>> Stashed changes
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
<<<<<<< Updated upstream
<<<<<<< HEAD
    private val _selectedMood = MutableStateFlow<Mood?>(null)
    val selectedMood: StateFlow<Mood?> = _selectedMood.asStateFlow()
    
=======
>>>>>>> master
=======
>>>>>>> Stashed changes
    init {
        loadUserData()
        loadSessionOfTheDay()
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
<<<<<<< Updated upstream
<<<<<<< HEAD
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
=======
=======
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
>>>>>>> master
=======
>>>>>>> Stashed changes
        }
    }
    
    private fun loadSessionOfTheDay() {
        viewModelScope.launch {
<<<<<<< Updated upstream
<<<<<<< HEAD
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
=======
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
    
=======
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
    
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
>>>>>>> master
=======
>>>>>>> Stashed changes
    }
    
    fun refreshData() {
        loadUserData()
        loadSessionOfTheDay()
    }
<<<<<<< Updated upstream
<<<<<<< HEAD
}

data class DashboardUiState(
    val user: com.drmindit.shared.domain.model.User? = null,
    val sessionOfTheDay: com.drmindit.shared.domain.model.Session? = null,
=======
    
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
=======
    
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
>>>>>>> Stashed changes
    val user: User? = null,
    val sessionOfTheDay: Session? = null,
    val recommendedSessions: List<Session> = emptyList(),
    val analytics: com.drmindit.shared.domain.model.UserAnalytics? = null,
<<<<<<< Updated upstream
>>>>>>> master
=======
>>>>>>> Stashed changes
    val isLoading: Boolean = false,
    val error: String? = null
)
