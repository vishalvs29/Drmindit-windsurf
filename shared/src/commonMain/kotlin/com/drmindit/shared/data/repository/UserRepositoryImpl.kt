package com.drmindit.shared.data.repository

import com.drmindit.shared.data.local.LocalDataSource
import com.drmindit.shared.data.remote.RemoteDataSource
import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.model.UserAnalytics
import com.drmindit.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : UserRepository {

    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val cachedUser = localDataSource.getCurrentUser()
            if (cachedUser != null) {
                Result.success(cachedUser)
            } else {
                val remoteUser = remoteDataSource.getCurrentUser()
                remoteUser.fold(
                    onSuccess = { user ->
                        user?.let { localDataSource.saveUser(it) }
                        Result.success(user)
                    },
                    onFailure = { Result.failure(it) }
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val remoteResult = remoteDataSource.updateUser(user)
            remoteResult.fold(
                onSuccess = { updatedUser ->
                    localDataSource.saveUser(updatedUser)
                    Result.success(updatedUser)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = remoteDataSource.signIn(email, password)
            result.fold(
                onSuccess = { user ->
                    localDataSource.saveUser(user)
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, name: String, userType: String): Result<User> {
        return try {
            val result = remoteDataSource.signUp(email, password, name, userType)
            result.fold(
                onSuccess = { user ->
                    localDataSource.saveUser(user)
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(token: String): Result<User> {
        return try {
            val result = remoteDataSource.signInWithGoogle(token)
            result.fold(
                onSuccess = { user ->
                    localDataSource.saveUser(user)
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            remoteDataSource.signOut()
            localDataSource.clearUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAnalytics(userId: String): Result<UserAnalytics> {
        return remoteDataSource.getUserAnalytics(userId)
    }

    override fun observeCurrentUser(): Flow<User?> {
        return localDataSource.observeCurrentUser()
    }
}
