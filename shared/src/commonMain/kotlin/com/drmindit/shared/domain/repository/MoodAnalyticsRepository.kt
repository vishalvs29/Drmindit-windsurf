package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.UserAnalytics
import com.drmindit.shared.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodAnalyticsRepository {
    suspend fun getUserAnalytics(userId: String): Result<UserAnalytics?>
    suspend fun updateUserAnalytics(analytics: UserAnalytics): Result<Unit>
    suspend fun addMoodEntry(entry: MoodEntry): Result<Unit>
    suspend fun getMoodHistory(userId: String, limit: Int = 30): Result<List<MoodEntry>>
    fun observeMoodTrends(userId: String): Flow<MoodEntry>
    suspend fun getWeeklyMoodSummary(userId: String): Result<Map<String, Float>>
    suspend fun getMonthlyMoodSummary(userId: String): Result<Map<String, Float>>
}
