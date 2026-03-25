package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.shared.data.repository.AuthRepository
import com.drmindit.shared.data.repository.SessionRepositoryImpl
import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val sessionRepository: SessionRepositoryImpl = SessionRepositoryImpl(),
    private val getSessionOfTheDayUseCase: GetSessionOfTheDayUseCase =
        GetSessionOfTheDayUseCase(sessionRepository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

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

            val result = authRepository.getCurrentUser()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(user = it, isLoading = false)
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = it.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    private fun loadSessionOfTheDay() {
        viewModelScope.launch {
            val result = getSessionOfTheDayUseCase()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        sessionOfTheDay = it,
                        recommendedSessions = listOf(it)
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DashboardUiState(
    val user: User? = null,
    val sessionOfTheDay: Session? = null,
    val recommendedSessions: List<Session> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)