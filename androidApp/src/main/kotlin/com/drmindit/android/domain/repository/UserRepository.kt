package com.drmindit.android.domain.repository

import com.drmindit.android.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository for user data and authentication
 */
interface UserRepository {
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateUser(user: User): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    fun getUserStream(): Flow<User?>
}

/**
 * Mock implementation for development
 */
class MockUserRepository : UserRepository {
    
    private var currentUser: User? = User(
        id = "user-123",
        email = "alex@drmindit.com",
        firstName = "Alex",
        lastName = "Johnson",
        avatar = null
    )
    
    override suspend fun getCurrentUser(): Result<User?> {
        return Result.success(currentUser)
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        currentUser = user
        return Result.success(user)
    }
    
    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        return Result.success(Unit)
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        currentUser = null
        // In real implementation, this would cascade delete all user data
        return Result.success(Unit)
    }
    
    override fun getUserStream(): Flow<User?> {
        return flowOf(currentUser)
    }
}
