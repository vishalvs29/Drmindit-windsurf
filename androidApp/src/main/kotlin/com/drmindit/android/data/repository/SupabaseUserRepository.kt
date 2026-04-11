package com.drmindit.android.data.repository

import com.drmindit.android.domain.model.User
import com.drmindit.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SupabaseUserRepository : UserRepository {
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserStream(): Flow<User?> {
        return flowOf(null)
    }
}
