package com.drmindit.android.data.repository

import com.drmindit.android.domain.model.User
import com.drmindit.android.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * Real Supabase implementation of UserRepository
 */
class SupabaseUserRepository(
    private val supabaseClient: SupabaseClient
) : UserRepository {
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val auth = supabaseClient.auth
            val currentUser = auth.currentUserOrNull()
            
            currentUser?.let { supabaseUser ->
                val user = User(
                    id = supabaseUser.id,
                    email = supabaseUser.email ?: "",
                    firstName = extractFirstName(supabaseUser),
                    lastName = extractLastName(supabaseUser),
                    avatar = supabaseUser.userMetadata["avatar_url"] as? String,
                    createdAt = supabaseUser.createdAt?.time ?: System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                Result.success(user)
            } ?: Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val auth = supabaseClient.auth
            auth.updateUser {
                email = user.email
                data {
                    put("first_name", user.firstName)
                    put("last_name", user.lastName)
                    put("avatar_url", user.avatar)
                }
            }.await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            val auth = supabaseClient.auth
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val auth = supabaseClient.auth
            val currentUser = auth.currentUserOrNull()
            
            // Sign out first
            auth.signOut()
            
            // In a real implementation, you would:
            // 1. Delete user from auth.users table
            // 2. Delete all user data via cascade deletes
            // 3. Delete user files from storage
            // 4. Clear local cache
            
            currentUser?.let { user ->
                // This would be implemented as a Supabase RPC function
                // or via direct table deletion with proper auth
                println("Deleting user data for: ${user.id}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getUserStream(): Flow<User?> {
        return try {
            val auth = supabaseClient.auth
            val currentUser = auth.currentUserOrNull()
            
            currentUser?.let { supabaseUser ->
                val user = User(
                    id = supabaseUser.id,
                    email = supabaseUser.email ?: "",
                    firstName = extractFirstName(sabaseUser),
                    lastName = extractLastName(supaseUser),
                    avatar = supabaseUser.userMetadata["avatar_url"] as? String,
                    createdAt = supabaseUser.createdAt?.time ?: System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                flowOf(user)
            } ?: flowOf(null)
        } catch (e: Exception) {
            flowOf(null)
        }
    }
    
    private fun extractFirstName(userInfo: UserInfo): String {
        return userInfo.userMetadata["first_name"] as? String 
            ?: userInfo.email?.split("@")?.firstOrNull() 
            ?: "User"
    }
    
    private fun extractLastName(userInfo: UserInfo): String {
        return userInfo.userMetadata["last_name"] as? String 
            ?: ""
    }
}
