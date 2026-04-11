package com.drmindit.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drmindit.android.domain.model.User
import com.drmindit.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user data and authentication state
 */
class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val currentUser = userRepository.getCurrentUser()
                _user.value = currentUser
            } catch (e: Exception) {
                _error.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshUser() {
        loadCurrentUser()
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                _user.value = null
            } catch (e: Exception) {
                _error.value = "Failed to sign out: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                userRepository.deleteAccount()
                _user.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete account: ${e.message}"
            }
        }
    }
    
    // Convenience properties for UI
    val userName: StateFlow<String> = user.map { user ->
        user?.let { "${it.firstName} ${it.lastName}" } ?: "Guest"
    }
    
    val firstName: StateFlow<String> = user.map { user ->
        user?.firstName ?: "Guest"
    }
    
    val isSignedIn: StateFlow<Boolean> = user.map { it != null }
}
