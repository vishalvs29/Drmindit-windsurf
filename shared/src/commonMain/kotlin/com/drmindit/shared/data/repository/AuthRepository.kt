package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.model.UserType
import com.drmindit.shared.domain.model.PersonalGoal
import com.drmindit.shared.domain.model.StressLevel
import com.drmindit.shared.domain.model.UserPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Google
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRow(
    val id: String,
    val email: String,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val role: String,
    val phone: String? = null,
    val organization_id: String? = null,
    val personal_goals: List<String> = emptyList(),
    val stress_level: Int? = null,
    val preferences: Map<String, Any> = emptyMap(),
    val is_high_risk: Boolean = false,
    val last_crisis_check: String? = null,
    val created_at: String,
    val updated_at: String
)

class AuthRepository(
    private val supabase: SupabaseClient = SupabaseClient.client,
    private val auth: Auth = supabase.auth,
    private val database: Postgrest = supabase.database
) {
    
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        userType: UserType
    ): Result<User> {
        return try {
            // Sign up with Supabase Auth
            val authResult = auth.signUpWith(Email) {
                email = email
                password = password
            }
            
            // Create profile record
            val profile = ProfileRow(
                id = authResult.user?.id ?: throw Exception("Failed to create user"),
                email = email,
                full_name = fullName,
                role = userType.name.lowercase(),
                personal_goals = emptyList(),
                preferences = mapOf(
                    "reminder_time" to "09:00",
                    "preferred_session_duration" to 15,
                    "dark_mode" to false,
                    "notifications_enabled" to true,
                    "offline_downloads_enabled" to false
                )
            )
            
            database.from("profiles").insert(profile)
            
            Result.success(
                User(
                    id = profile.id,
                    email = profile.email,
                    name = profile.full_name ?: fullName,
                    avatar = profile.avatar_url,
                    userType = UserType.valueOf(profile.role.uppercase()),
                    personalGoals = profile.personal_goals.map { PersonalGoal.valueOf(it.uppercase()) },
                    stressLevel = profile.stress_level?.let { 
                        when (it) {
                            in 1..2 -> StressLevel.LOW
                            in 3..5 -> StressLevel.MEDIUM
                            in 6..8 -> StressLevel.HIGH
                            else -> StressLevel.SEVERE
                        }
                    } ?: StressLevel.MEDIUM,
                    registrationDate = profile.created_at,
                    lastActiveDate = profile.updated_at,
                    preferences = UserPreferences(
                        reminderTime = profile.preferences["reminder_time"] as? String,
                        preferredSessionDuration = (profile.preferences["preferred_session_duration"] as? Int) ?: 15,
                        darkMode = profile.preferences["dark_mode"] as? Boolean ?: false,
                        notificationsEnabled = profile.preferences["notifications_enabled"] as? Boolean ?: true,
                        offlineDownloadsEnabled = profile.preferences["offline_downloads_enabled"] as? Boolean ?: false
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWith(Email) {
                email = email
                password = password
            }
            
            // Get user profile
            val profile = database.from("profiles")
                .select {
                    ProfileRow::id
                    ProfileRow::email
                    ProfileRow::full_name
                    ProfileRow::avatar_url
                    ProfileRow::role
                    ProfileRow::phone
                    ProfileRow::organization_id
                    ProfileRow::personal_goals
                    ProfileRow::stress_level
                    ProfileRow::preferences
                    ProfileRow::is_high_risk
                    ProfileRow::last_crisis_check
                    ProfileRow::created_at
                    ProfileRow::updated_at
                }
                .eq("id", authResult.user?.id ?: throw Exception("Failed to sign in"))
                .single()
                .data ?: throw Exception("Profile not found")
            
            Result.success(mapProfileToUser(profile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val authResult = auth.signInWith(Google) {
                idToken = idToken
            }
            
            // Check if profile exists, create if not
            val existingProfile = database.from("profiles")
                .select()
                .eq("id", authResult.user?.id ?: throw Exception("Failed to sign in"))
                .single()
                .data
            
            val profile = existingProfile ?: createProfileFromUserInfo(authResult.user!!)
            
            Result.success(mapProfileToUser(profile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUser = auth.currentUserOrNull
            if (currentUser == null) {
                Result.success(null)
                return@try Result.success(null)
            }
            
            val profile = database.from("profiles")
                .select()
                .eq("id", currentUser.id)
                .single()
                .data ?: return@try Result.success(null)
            
            Result.success(mapProfileToUser(profile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeAuthState(): Flow<UserInfo?> {
        return auth.sessionStatus
    }
    
    suspend fun updateProfile(updates: Map<String, Any>): Result<User> {
        return try {
            val currentUser = auth.currentUserOrNull ?: throw Exception("No authenticated user")
            
            database.from("profiles")
                .update(updates)
                .eq("id", currentUser.id)
            
            // Fetch updated profile
            getCurrentUser()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createProfileFromUserInfo(userInfo: UserInfo): ProfileRow {
        val profile = ProfileRow(
            id = userInfo.id,
            email = userInfo.email ?: throw Exception("Email required"),
            full_name = userInfo.userMetadata["full_name"] as? String,
            avatar_url = userInfo.userMetadata["avatar_url"] as? String,
            role = "general",
            personal_goals = emptyList(),
            preferences = mapOf(
                "reminder_time" to "09:00",
                "preferred_session_duration" to 15,
                "dark_mode" to false,
                "notifications_enabled" to true,
                "offline_downloads_enabled" to false
            )
        )
        
        database.from("profiles").insert(profile)
        return profile
    }
    
    private fun mapProfileToUser(profile: ProfileRow): User {
        return User(
            id = profile.id,
            email = profile.email,
            name = profile.full_name ?: "User",
            avatar = profile.avatar_url,
            userType = UserType.valueOf(profile.role.uppercase()),
            personalGoals = profile.personal_goals.map { 
                try { PersonalGoal.valueOf(it.uppercase()) } 
                catch (e: IllegalArgumentException) { PersonalGoal.STRESS_MANAGEMENT }
            },
            stressLevel = profile.stress_level?.let { 
                when (it) {
                    in 1..2 -> StressLevel.LOW
                    in 3..5 -> StressLevel.MEDIUM
                    in 6..8 -> StressLevel.HIGH
                    else -> StressLevel.SEVERE
                }
            } ?: StressLevel.MEDIUM,
            registrationDate = profile.created_at,
            lastActiveDate = profile.updated_at,
            preferences = UserPreferences(
                reminderTime = profile.preferences["reminder_time"] as? String,
                preferredSessionDuration = (profile.preferences["preferred_session_duration"] as? Int) ?: 15,
                darkMode = profile.preferences["dark_mode"] as? Boolean ?: false,
                notificationsEnabled = profile.preferences["notifications_enabled"] as? Boolean ?: true,
                offlineDownloadsEnabled = profile.preferences["offline_downloads_enabled"] as? Boolean ?: false
            )
        )
    }
}
