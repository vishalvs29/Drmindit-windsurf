package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.model.UserAnalytics
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, name: String, userType: String): Result<User>
    suspend fun signInWithGoogle(token: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getUserAnalytics(userId: String): Result<UserAnalytics>
    fun observeCurrentUser(): Flow<User?>
}
