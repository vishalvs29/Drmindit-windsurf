package com.drmindit.shared.data.local

import com.drmindit.shared.domain.model.User
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User)
    suspend fun clearUser()
    fun observeCurrentUser(): Flow<User?>
}
