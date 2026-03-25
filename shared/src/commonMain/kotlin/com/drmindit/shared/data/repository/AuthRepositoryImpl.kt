package com.drmindit.shared.data.repository

import com.drmindit.shared.data.network.ApiClient
import com.drmindit.shared.data.network.SupabaseService
import com.drmindit.shared.data.network.ApiException
import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.model.UserType
import com.drmindit.shared.domain.model.PersonalGoal
import com.drmindit.shared.domain.model.StressLevel
import com.drmindit.shared.domain.model.UserPreferences
import com.drmindit.shared.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl(
    private val supabaseService: SupabaseService = SupabaseService()
) : UserRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isAuthenticated = MutableStateFlow(false)
    private val _authToken = MutableStateFlow<String?>(null)
    
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()
    override val isAuthenticated: Flow<Boolean> = _isAuthenticated.asStateFlow()
    
    init {
        // Check for existing session on initialization
        checkExistingSession()
    }
    
    private fun checkExistingSession() {
        // In a real implementation, this would check secure storage for existing tokens
        // For now, we'll assume no existing session
    }
    
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val response = supabaseService.signIn(email, password)
            val responseBody = response.body as? Map<String, Any>
            
            if (responseBody != null) {
                val accessToken = responseBody["access_token"] as? String
                val refreshToken = responseBody["refresh_token"] as? String
                val user = responseBody["user"] as? Map<String, Any>
                
                if (accessToken != null && user != null) {
                    _authToken.value = accessToken
                    _isAuthenticated.value = true
                    
                    // Get or create user profile
                    val userProfile = getUserProfile(user["id"] as String)
                    _currentUser.value = userProfile
                    
                    Result.success(userProfile)
                } else {
                    Result.failure(ApiException.UnknownError("Invalid authentication response"))
                }
            } else {
                Result.failure(ApiException.NetworkError("No response from server"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        userType: UserType,
        additionalMetadata: Map<String, String> = emptyMap()
    ): Result<User> {
        return try {
            val metadata = buildJsonObject {
                put("full_name", fullName)
                put("user_type", userType.name.lowercase())
                additionalMetadata.forEach { (key, value) ->
                    put(key, value)
                }
            }
            
            val response = supabaseService.signUp(email, password, metadata.toMap())
            val responseBody = response.body as? Map<String, Any>
            
            if (responseBody != null) {
                val user = responseBody["user"] as? Map<String, Any>
                
                if (user != null) {
                    val userId = user["id"] as String
                    val userProfile = createUserProfile(userId, email, fullName, userType)
                    _currentUser.value = userProfile
                    Result.success(userProfile)
                } else {
                    Result.failure(ApiException.UnknownError("User creation failed"))
                }
            } else {
                Result.failure(ApiException.NetworkError("No response from server"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // This would integrate with Google Sign-In
            // For now, we'll simulate the flow
            val response = supabaseService.signIn(
                email = "user@gmail.com", // Would come from Google
                password = System.getenv("GOOGLE_AUTH_PASSWORD") ?: "google-auth-password" // Would be handled differently
            )
            
            // Process response similar to regular sign-in
            Result.failure(ApiException.UnknownError("Google Sign-In not fully implemented"))
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            _authToken.value?.let { token ->
                supabaseService.signOut()
            }
            
            _authToken.value = null
            _currentUser.value = null
            _isAuthenticated.value = false
            
            Result.success(Unit)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val user = _currentUser.value
            if (user != null) {
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val userId = user.id
            val updates = buildJsonObject {
                put("full_name", user.name)
                put("avatar_url", user.avatar ?: "")
                put("personal_goals", user.personalGoals.map { it.name.lowercase() })
                put("stress_level", when (user.stressLevel) {
                    StressLevel.LOW -> 2
                    StressLevel.MEDIUM -> 5
                    StressLevel.HIGH -> 7
                    StressLevel.SEVERE -> 9
                })
                put("preferences", buildJsonObject {
                    put("reminder_time", user.preferences.reminderTime ?: "09:00")
                    put("preferred_session_duration", user.preferences.preferredSessionDuration)
                    put("dark_mode", user.preferences.darkMode)
                    put("notifications_enabled", user.preferences.notificationsEnabled)
                    put("offline_downloads_enabled", user.preferences.offlineDownloadsEnabled)
                })
                put("updated_at", Clock.System.now().toString())
            }
            
            supabaseService.updateUserProfile(userId, updates.toMap())
            
            val updatedUser = user.copy(
                lastActiveDate = Clock.System.now().toString()
            )
            
            _currentUser.value = updatedUser
            Result.success(updatedUser)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            // This would implement password reset functionality
            // For Supabase, this would be a different endpoint
            Result.failure(ApiException.UnknownError("Password reset not implemented"))
        } catch (e: Exception) {
            Result.failure(ApiException.UnknownError(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun getUserProfile(userId: String): User {
        return try {
            val response = supabaseService.getUserProfile(userId)
            val profiles = response.body as? List<Map<String, Any>>
            
            if (profiles?.isNotEmpty() == true) {
                val profile = profiles.first()
                mapProfileToUser(profile)
            } else {
                // Create default profile if not found
                createDefaultProfile(userId)
            }
        } catch (e: Exception) {
            createDefaultProfile(userId)
        }
    }
    
    private suspend fun createUserProfile(
        userId: String,
        email: String,
        fullName: String,
        userType: UserType
    ): User {
        return try {
            val profileData = buildJsonObject {
                put("id", userId)
                put("email", email)
                put("full_name", fullName)
                put("role", userType.name.lowercase())
                put("personal_goals", emptyList<String>())
                put("stress_level", 5)
                put("preferences", buildJsonObject {
                    put("reminder_time", "09:00")
                    put("preferred_session_duration", 15)
                    put("dark_mode", false)
                    put("notifications_enabled", true)
                    put("offline_downloads_enabled", false)
                })
                put("is_high_risk", false)
                put("created_at", Clock.System.now().toString())
                put("updated_at", Clock.System.now().toString())
            }
            
            supabaseService.createUserProfile(profileData.toMap())
            
            User(
                id = userId,
                email = email,
                name = fullName,
                avatar = null,
                userType = userType,
                personalGoals = emptyList(),
                stressLevel = StressLevel.MEDIUM,
                registrationDate = Clock.System.now().toString(),
                lastActiveDate = Clock.System.now().toString(),
                preferences = UserPreferences(
                    reminderTime = "09:00",
                    preferredSessionDuration = 15,
                    darkMode = false,
                    notificationsEnabled = true,
                    offlineDownloadsEnabled = false
                )
            )
        } catch (e: Exception) {
            createDefaultProfile(userId)
        }
    }
    
    private suspend fun createDefaultProfile(userId: String): User {
        return User(
            id = userId,
            email = "user@example.com",
            name = "User",
            avatar = null,
            userType = UserType.GENERAL,
            personalGoals = listOf(PersonalGoal.STRESS_MANAGEMENT),
            stressLevel = StressLevel.MEDIUM,
            registrationDate = Clock.System.now().toString(),
            lastActiveDate = Clock.System.now().toString(),
            preferences = UserPreferences(
                reminderTime = "09:00",
                preferredSessionDuration = 15,
                darkMode = false,
                notificationsEnabled = true,
                offlineDownloadsEnabled = false
            )
        )
    }
    
    private fun mapProfileToUser(profile: Map<String, Any>): User {
        val personalGoals = (profile["personal_goals"] as? List<String>)?.mapNotNull { goal ->
            try { PersonalGoal.valueOf(goal.uppercase()) } 
            catch (e: IllegalArgumentException) { null }
        } ?: emptyList()
        
        val stressLevel = when (profile["stress_level"] as? Int) {
            in 1..2 -> StressLevel.LOW
            in 3..5 -> StressLevel.MEDIUM
            in 6..8 -> StressLevel.HIGH
            else -> StressLevel.SEVERE
        }
        
        val userType = try {
            UserType.valueOf((profile["role"] as? String)?.uppercase() ?: "GENERAL")
        } catch (e: IllegalArgumentException) {
            UserType.GENERAL
        }
        
        val preferences = profile["preferences"] as? Map<String, Any>
        
        return User(
            id = profile["id"] as String,
            email = profile["email"] as String,
            name = profile["full_name"] as? String ?: "User",
            avatar = profile["avatar_url"] as? String,
            userType = userType,
            personalGoals = personalGoals,
            stressLevel = stressLevel,
            registrationDate = profile["created_at"] as? String ?: Clock.System.now().toString(),
            lastActiveDate = profile["updated_at"] as? String ?: Clock.System.now().toString(),
            preferences = UserPreferences(
                reminderTime = preferences?.get("reminder_time") as? String ?: "09:00",
                preferredSessionDuration = (preferences?.get("preferred_session_duration") as? Int) ?: 15,
                darkMode = preferences?.get("dark_mode") as? Boolean ?: false,
                notificationsEnabled = preferences?.get("notifications_enabled") as? Boolean ?: true,
                offlineDownloadsEnabled = preferences?.get("offline_downloads_enabled") as? Boolean ?: false
            )
        )
    }
}
