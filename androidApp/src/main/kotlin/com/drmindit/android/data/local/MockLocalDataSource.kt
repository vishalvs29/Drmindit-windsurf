package com.drmindit.android.data.local

import com.drmindit.shared.domain.model.User
import com.drmindit.shared.data.local.LocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MockLocalDataSource : LocalDataSource {
    
    private var cachedUser: User? = null
    private val _userFlow = MutableStateFlow<User?>(null)
    
    override suspend fun getCurrentUser(): User? {
        return cachedUser
    }
    
    override suspend fun saveUser(user: User) {
        cachedUser = user
        _userFlow.value = user
    }
    
    override suspend fun clearUser() {
        cachedUser = null
        _userFlow.value = null
    }
    
    override fun observeCurrentUser(): Flow<User?> {
        return _userFlow
    }
}
