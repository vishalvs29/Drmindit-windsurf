package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String? = null,
    val userType: UserType,
    val personalGoals: List<PersonalGoal>,
    val stressLevel: StressLevel,
    val registrationDate: String,
    val lastActiveDate: String,
    val preferences: UserPreferences
)

@Serializable
enum class UserType {
    STUDENT,
    CORPORATE_EMPLOYEE,
    GOVERNMENT_EMPLOYEE,
    POLICE_MILITARY,
    GENERAL
}

@Serializable
enum class PersonalGoal {
    STRESS_MANAGEMENT,
    ANXIETY_REDUCTION,
    SLEEP_IMPROVEMENT,
    DEPRESSION_SUPPORT,
    FOCUS_PRODUCTIVITY,
    MINDFULNESS
}

@Serializable
enum class StressLevel {
    LOW,
    MEDIUM,
    HIGH,
    SEVERE
}

@Serializable
data class UserPreferences(
    val reminderTime: String? = null,
    val preferredSessionDuration: Int = 15, // minutes
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val offlineDownloadsEnabled: Boolean = false
)
