package com.drmindit.shared.domain.conversation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Profile Manager
 * Manages user profiles, preferences, and progress tracking
 */
@Singleton
class UserProfileManager @Inject constructor(
    private val dataStore: UserDataStore,
    private val json: Json
) {
    
    private val _userProfiles = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val userProfiles: StateFlow<Map<String, UserProfile>> = _userProfiles.asStateFlow()
    
    /**
     * Get user profile by ID
     */
    suspend fun getUserProfile(userId: String): UserProfile {
        val profiles = _userProfiles.value
        return profiles[userId] ?: loadUserProfile(userId)
    }
    
    /**
     * Create new user profile
     */
    suspend fun createUserProfile(
        userId: String,
        name: String,
        preferences: UserPreferences = UserPreferences()
    ): UserProfile {
        val userProfile = UserProfile(
            id = userId,
            name = name,
            preferences = preferences,
            history = ConversationHistory(),
            progress = UserProgress()
        )
        
        saveUserProfile(userProfile)
        return userProfile
    }
    
    /**
     * Update user profile
     */
    suspend fun updateUserProfile(userId: String, updates: UserProfile.() -> UserProfile) {
        val currentProfile = getUserProfile(userId)
        val updatedProfile = currentProfile.updates()
        saveUserProfile(updatedProfile)
    }
    
    /**
     * Update user preferences
     */
    suspend fun updateUserPreferences(userId: String, preferences: UserPreferences) {
        updateUserProfile(userId) {
            copy(preferences = preferences)
        }
    }
    
    /**
     * Update conversation history
     */
    suspend fun updateConversationHistory(userId: String, history: List<ConversationMessage>) {
        updateUserProfile(userId) {
            copy(
                history = ConversationHistory(
                    sessions = groupMessagesIntoSessions(history),
                    totalMessages = history.size,
                    averageSessionDuration = calculateAverageSessionDuration(history),
                    mostDiscussedTopics = extractMostDiscussedTopics(history),
                    emotionalPatterns = analyzeEmotionalPatterns(history),
                    crisisInterventions = countCrisisInterventions(history),
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }
    
    /**
     * Update progress tracking
     */
    suspend fun updateProgress(userId: String, progressUpdates: UserProgress.() -> UserProgress) {
        updateUserProfile(userId) {
            copy(progress = progress.updates())
        }
    }
    
    /**
     * Update progress for specific flow completion
     */
    suspend fun updateProgress(userId: String, completedFlow: ConversationState) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val updatedProgress = currentProgress.copy(
            totalSessions = currentProgress.totalSessions + 1,
            lastUpdated = System.currentTimeMillis(),
            goalsAchieved = currentProgress.goalsAchieved + completedFlow.name
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Update streak information
     */
    suspend fun updateStreak(userId: String, newStreak: Int, isLongestStreak: Boolean = false) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val updatedProgress = currentProgress.copy(
            currentStreak = newStreak,
            longestStreak = if (isLongestStreak) newStreak else currentProgress.longestStreak,
            lastUpdated = System.currentTimeMillis()
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Add completed exercise
     */
    suspend fun addCompletedExercise(userId: String, exerciseId: String) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val updatedProgress = currentProgress.copy(
            completedExercises = currentProgress.completedExercises + exerciseId,
            lastUpdated = System.currentTimeMillis()
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Add viewed resource
     */
    suspend fun addViewedResource(userId: String, resourceId: String) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val updatedProgress = currentProgress.copy(
            viewedResources = currentProgress.viewedResources + resourceId,
            lastUpdated = System.currentTimeMillis()
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Update mood tracking
     */
    suspend fun updateMoodTracking(userId: String, date: String, rating: Int) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val moodTrends = currentProgress.moodTrends.toMutableMap()
        val ratings = moodTrends.getOrDefault(date, emptyList()).toMutableList()
        ratings.add(rating)
        moodTrends[date] = ratings
        
        val updatedProgress = currentProgress.copy(
            moodTrends = moodTrends,
            lastUpdated = System.currentTimeMillis()
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Update skill development
     */
    suspend fun updateSkillDevelopment(userId: String, skill: String, level: Int) {
        val userProfile = getUserProfile(userId)
        val currentProgress = userProfile.progress
        
        val skillDevelopment = currentProgress.skillDevelopment.toMutableMap()
        skillDevelopment[skill] = level
        
        val updatedProgress = currentProgress.copy(
            skillDevelopment = skillDevelopment,
            lastUpdated = System.currentTimeMillis()
        )
        
        updateUserProfile(userId) {
            copy(progress = updatedProgress)
        }
    }
    
    /**
     * Add risk factor
     */
    suspend fun addRiskFactor(userId: String, riskFactor: String) {
        val userProfile = getUserProfile(userId)
        val currentRiskFactors = userProfile.riskFactors.toMutableList()
        
        if (!currentRiskFactors.contains(riskFactor)) {
            currentRiskFactors.add(riskFactor)
            
            updateUserProfile(userId) {
                copy(riskFactors = currentRiskFactors)
            }
        }
    }
    
    /**
     * Add strength
     */
    suspend fun addStrength(userId: String, strength: String) {
        val userProfile = getUserProfile(userId)
        val currentStrengths = userProfile.strengths.toMutableList()
        
        if (!currentStrengths.contains(strength)) {
            currentStrengths.add(strength)
            
            updateUserProfile(userId) {
                copy(strengths = currentStrengths)
            }
        }
    }
    
    /**
     * Add goal
     */
    suspend fun addGoal(userId: String, goal: String) {
        val userProfile = getUserProfile(userId)
        val currentGoals = userProfile.goals.toMutableList()
        
        if (!currentGoals.contains(goal)) {
            currentGoals.add(goal)
            
            updateUserProfile(userId) {
                copy(goals = currentGoals)
            }
        }
    }
    
    /**
     * Get user statistics
     */
    suspend fun getUserStatistics(userId: String): UserStatistics {
        val userProfile = getUserProfile(userId)
        val history = userProfile.history
        val progress = userProfile.progress
        
        return UserStatistics(
            totalSessions = progress.totalSessions,
            currentStreak = progress.currentStreak,
            longestStreak = progress.longestStreak,
            averageSessionDuration = history.averageSessionDuration,
            mostDiscussedTopics = history.mostDiscussedTopics,
            emotionalPatterns = history.emotionalPatterns,
            completedExercises = progress.completedExercises.size,
            viewedResources = progress.viewedResources.size,
            goalsAchieved = progress.goalsAchieved.size,
            skillDevelopment = progress.skillDevelopment,
            moodTrends = progress.moodTrends,
            riskFactors = userProfile.riskFactors,
            strengths = userProfile.strengths,
            goals = userProfile.goals
        )
    }
    
    /**
     * Get user recommendations
     */
    suspend fun getUserRecommendations(userId: String): List<UserRecommendation> {
        val userProfile = getUserProfile(userId)
        val statistics = getUserStatistics(userId)
        
        val recommendations = mutableListOf<UserRecommendation>()
        
        // Exercise recommendations based on preferences and progress
        userProfile.preferences.exerciseTypes.forEach { exerciseType ->
            recommendations.add(
                UserRecommendation(
                    id = "exercise_${exerciseType.name.lowercase()}",
                    title = "Try ${exerciseType.name.replace("_", " ")} Exercises",
                    description = "Based on your preferences, try these ${exerciseType.name.lowercase()} practices",
                    type = RecommendationType.EXERCISE,
                    priority = RecommendationPriority.MEDIUM
                )
            )
        }
        
        // Resource recommendations based on emotional patterns
        statistics.emotionalPatterns.forEach { (emotion, count) ->
            if (count > 3) { // Frequently discussed emotions
                val category = when (emotion) {
                    EmotionalTone.ANXIOUS -> ResourceCategory.ANXIETY
                    EmotionalTone.SAD -> ResourceCategory.DEPRESSION
                    EmotionalTone.STRESSED -> ResourceCategory.STRESS
                    EmotionalTone.EXHAUSTED -> ResourceCategory.SELF_CARE
                    else -> ResourceCategory.GENERAL
                }
                
                recommendations.add(
                    UserRecommendation(
                        id = "resource_${category.name.lowercase()}",
                        title = "${category.name.replace("_", " ")} Resources",
                        description = "Resources to help with ${emotion.name.lowercase()} feelings",
                        type = RecommendationType.RESOURCE,
                        priority = RecommendationPriority.HIGH
                    )
                )
            }
        }
        
        // Goal recommendations based on progress
        if (statistics.currentStreak < 3) {
            recommendations.add(
                UserRecommendation(
                    id = "consistency_goal",
                    title = "Build Consistency",
                    description = "Focus on building a daily practice habit",
                    type = RecommendationType.GOAL,
                    priority = RecommendationPriority.HIGH
                )
            )
        }
        
        // Skill development recommendations
        statistics.skillDevelopment.forEach { (skill, level) ->
            if (level < 3) { // Needs development
                recommendations.add(
                    UserRecommendation(
                        id = "skill_${skill.lowercase()}",
                        title = "Develop ${skill.replace("_", " ")} Skills",
                        description = "Practice exercises to improve your ${skill.lowercase()} abilities",
                        type = RecommendationType.SKILL,
                        priority = RecommendationPriority.MEDIUM
                    )
                )
            }
        }
        
        return recommendations.sortedByDescending { it.priority.ordinal }
    }
    
    /**
     * Load user profile from data store
     */
    private suspend fun loadUserProfile(userId: String): UserProfile {
        return try {
            val profileData = dataStore.getUserProfile(userId)
            if (profileData != null) {
                json.decodeFromString<UserProfile>(profileData)
            } else {
                // Create default profile
                createUserProfile(userId, "User")
            }
        } catch (e: Exception) {
            // Create default profile on error
            createUserProfile(userId, "User")
        }
    }
    
    /**
     * Save user profile to data store
     */
    private suspend fun saveUserProfile(userProfile: UserProfile) {
        val profileData = json.encodeToString(userProfile)
        dataStore.saveUserProfile(userProfile.id, profileData)
        
        // Update in-memory cache
        val currentProfiles = _userProfiles.value.toMutableMap()
        currentProfiles[userProfile.id] = userProfile
        _userProfiles.value = currentProfiles
    }
    
    /**
     * Group messages into sessions
     */
    private fun groupMessagesIntoSessions(messages: List<ConversationMessage>): List<ConversationSession> {
        val sessions = mutableListOf<ConversationSession>()
        
        if (messages.isEmpty()) return sessions
        
        var currentSessionMessages = mutableListOf<ConversationMessage>()
        var sessionStartTime = messages.first().timestamp
        var lastMessageTime = messages.first().timestamp
        val sessionTimeout = 30 * 60 * 1000L // 30 minutes
        
        messages.forEach { message ->
            val timeSinceLastMessage = message.timestamp - lastMessageTime
            
            if (timeSinceLastMessage > sessionTimeout && currentSessionMessages.isNotEmpty()) {
                // Create completed session
                val session = ConversationSession(
                    id = generateSessionId(),
                    startTime = sessionStartTime,
                    endTime = lastMessageTime,
                    duration = ((lastMessageTime - sessionStartTime) / 60000).toInt(),
                    messages = currentSessionMessages.toList(),
                    initialState = currentSessionMessages.first().context.state,
                    finalState = currentSessionMessages.last().context.state,
                    exercises = extractExerciseIds(currentSessionMessages),
                    resources = extractResourceIds(currentSessionMessages),
                    userSatisfaction = extractUserSatisfaction(currentSessionMessages),
                    outcomes = extractOutcomes(currentSessionMessages)
                )
                sessions.add(session)
                
                // Start new session
                currentSessionMessages.clear()
                sessionStartTime = message.timestamp
            }
            
            currentSessionMessages.add(message)
            lastMessageTime = message.timestamp
        }
        
        // Add final session if there are messages
        if (currentSessionMessages.isNotEmpty()) {
            val session = ConversationSession(
                id = generateSessionId(),
                startTime = sessionStartTime,
                endTime = lastMessageTime,
                duration = ((lastMessageTime - sessionStartTime) / 60000).toInt(),
                messages = currentSessionMessages.toList(),
                initialState = currentSessionMessages.first().context.state,
                finalState = currentSessionMessages.last().context.state,
                exercises = extractExerciseIds(currentSessionMessages),
                resources = extractResourceIds(currentSessionMessages),
                userSatisfaction = extractUserSatisfaction(currentSessionMessages),
                outcomes = extractOutcomes(currentSessionMessages)
            )
            sessions.add(session)
        }
        
        return sessions
    }
    
    /**
     * Calculate average session duration
     */
    private fun calculateAverageSessionDuration(messages: List<ConversationMessage>): Int {
        val sessions = groupMessagesIntoSessions(messages)
        return if (sessions.isNotEmpty()) {
            sessions.map { it.duration }.average().toInt()
        } else 0
    }
    
    /**
     * Extract most discussed topics
     */
    private fun extractMostDiscussedTopics(messages: List<ConversationMessage>): List<String> {
        val topicCounts = mutableMapOf<String, Int>()
        
        messages.forEach { message ->
            message.metadata.keywords.forEach { keyword ->
                topicCounts[keyword] = topicCounts.getOrDefault(keyword, 0) + 1
            }
        }
        
        return topicCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }
    
    /**
     * Analyze emotional patterns
     */
    private fun analyzeEmotionalPatterns(messages: List<ConversationMessage>): Map<EmotionalTone, Int> {
        val toneCounts = mutableMapOf<EmotionalTone, Int>()
        
        messages.forEach { message ->
            message.metadata.emotionalTone?.let { tone ->
                toneCounts[tone] = toneCounts.getOrDefault(tone, 0) + 1
            }
        }
        
        return toneCounts
    }
    
    /**
     * Count crisis interventions
     */
    private fun countCrisisInterventions(messages: List<ConversationMessage>): Int {
        return messages.count { message ->
            message.type == MessageType.CRISIS_INTERVENTION
        }
    }
    
    /**
     * Extract exercise IDs from messages
     */
    private fun extractExerciseIds(messages: List<ConversationMessage>): List<String> {
        return messages.mapNotNull { message ->
            message.context.metadata["exercise_id"]
        }.distinct()
    }
    
    /**
     * Extract resource IDs from messages
     */
    private fun extractResourceIds(messages: List<ConversationMessage>): List<String> {
        return messages.mapNotNull { message ->
            message.metadata.entities["resource_id"]
        }.distinct()
    }
    
    /**
     * Extract user satisfaction from messages
     */
    private fun extractUserSatisfaction(messages: List<ConversationMessage>): Int? {
        return messages.lastOrNull { message ->
            message.metadata.userSatisfaction != null
        }?.metadata?.userSatisfaction
    }
    
    /**
     * Extract outcomes from messages
     */
    private fun extractOutcomes(messages: List<ConversationMessage>): List<String> {
        return messages.mapNotNull { message ->
            message.metadata.nextStepHint
        }.distinct()
    }
    
    /**
     * Generate session ID
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * User statistics data
 */
data class UserStatistics(
    val totalSessions: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val averageSessionDuration: Int,
    val mostDiscussedTopics: List<String>,
    val emotionalPatterns: Map<EmotionalTone, Int>,
    val completedExercises: Int,
    val viewedResources: Int,
    val goalsAchieved: Int,
    val skillDevelopment: Map<String, Int>,
    val moodTrends: Map<String, List<Int>>,
    val riskFactors: List<String>,
    val strengths: List<String>,
    val goals: List<String>
)

/**
 * User recommendation data
 */
data class UserRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val type: RecommendationType,
    val priority: RecommendationPriority,
    val isCompleted: Boolean = false
)

/**
 * Recommendation types
 */
enum class RecommendationType {
    EXERCISE,
    RESOURCE,
    GOAL,
    SKILL,
    PRACTICE
}

/**
 * Recommendation priority levels
 */
enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * User data store interface
 */
interface UserDataStore {
    suspend fun getUserProfile(userId: String): String?
    suspend fun saveUserProfile(userId: String, profileData: String)
}
