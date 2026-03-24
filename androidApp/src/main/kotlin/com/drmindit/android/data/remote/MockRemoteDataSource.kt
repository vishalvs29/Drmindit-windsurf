package com.drmindit.android.data.remote

import com.drmindit.shared.domain.model.User
import com.drmindit.shared.domain.model.UserAnalytics
import com.drmindit.shared.domain.model.UserType
import com.drmindit.shared.domain.model.PersonalGoal
import com.drmindit.shared.domain.model.StressLevel
import com.drmindit.shared.domain.model.UserPreferences
import com.drmindit.shared.data.remote.RemoteDataSource

class MockRemoteDataSource : RemoteDataSource {
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            // Return mock user for demonstration
            val mockUser = User(
                id = "user123",
                email = "sarah@example.com",
                name = "Sarah Johnson",
                avatar = "https://example.com/avatar.jpg",
                userType = UserType.CORPORATE_EMPLOYEE,
                personalGoals = listOf(PersonalGoal.STRESS_MANAGEMENT, PersonalGoal.ANXIETY_REDUCTION),
                stressLevel = StressLevel.MEDIUM,
                registrationDate = "2024-01-15",
                lastActiveDate = "2024-03-24",
                preferences = UserPreferences(
                    reminderTime = "09:00",
                    preferredSessionDuration = 15,
                    darkMode = false,
                    notificationsEnabled = true,
                    offlineDownloadsEnabled = false
                )
            )
            Result.success(mockUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: User): Result<User> {
        return try {
            // In a real implementation, this would update the user on the server
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            // Mock sign in logic
            if (email == "sarah@example.com" && password == "password123") {
                getCurrentUser()
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signUp(email: String, password: String, name: String, userType: String): Result<User> {
        return try {
            // Mock sign up logic
            val newUser = User(
                id = "new_user_${System.currentTimeMillis()}",
                email = email,
                name = name,
                userType = UserType.valueOf(userType),
                personalGoals = emptyList(),
                stressLevel = StressLevel.MEDIUM,
                registrationDate = java.time.LocalDateTime.now().toString(),
                lastActiveDate = java.time.LocalDateTime.now().toString(),
                preferences = UserPreferences()
            )
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signInWithGoogle(token: String): Result<User> {
        return try {
            // Mock Google sign in
            val googleUser = User(
                id = "google_user_${System.currentTimeMillis()}",
                email = "user@gmail.com",
                name = "Google User",
                avatar = "https://lh3.googleusercontent.com/photo.jpg",
                userType = UserType.GENERAL,
                personalGoals = emptyList(),
                stressLevel = StressLevel.LOW,
                registrationDate = java.time.LocalDateTime.now().toString(),
                lastActiveDate = java.time.LocalDateTime.now().toString(),
                preferences = UserPreferences()
            )
            Result.success(googleUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            // Mock sign out
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserAnalytics(userId: String): Result<UserAnalytics> {
        return try {
            // Mock analytics data
            val mockAnalytics = com.drmindit.shared.domain.model.UserAnalytics(
                userId = userId,
                totalMindfulMinutes = 145,
                currentStreak = 7,
                longestStreak = 14,
                sessionsCompleted = 23,
                averageSessionDuration = 12.5f,
                moodEntries = emptyList(), // Would contain actual mood entries
                weeklyProgress = emptyList(), // Would contain weekly progress data
                monthlyInsights = com.drmindit.shared.domain.model.MonthlyInsights(
                    month = "March 2024",
                    totalMinutes = 145,
                    mostActiveCategory = com.drmindit.shared.domain.model.SessionCategory.MINDFULNESS,
                    improvementAreas = listOf("Sleep Quality", "Stress Management"),
                    achievements = emptyList()
                )
            )
            Result.success(mockAnalytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
