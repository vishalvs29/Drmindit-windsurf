package com.drmindit.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserAnalytics(
    val userId: String,
    val totalMindfulMinutes: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val sessionsCompleted: Int,
    val averageSessionDuration: Float,
    val moodEntries: List<MoodEntry>,
    val weeklyProgress: List<WeeklyProgress>,
    val monthlyInsights: MonthlyInsights
)

@Serializable
data class MoodEntry(
    val id: String,
    val date: String,
    val mood: Mood,
    val stressLevel: StressLevel,
    val sleepQuality: SleepQuality,
    val notes: String? = null
)

@Serializable
enum class Mood {
    VERY_HAPPY,
    HAPPY,
    NEUTRAL,
    SAD,
    VERY_SAD,
    ANXIOUS,
    CALM,
    ENERGETIC,
    TIRED
}

@Serializable
enum class SleepQuality {
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}

@Serializable
data class WeeklyProgress(
    val weekStart: String,
    val mindfulMinutes: Int,
    val sessionsCompleted: Int,
    val averageMood: Mood,
    val stressImprovement: Float // percentage change
)

@Serializable
data class MonthlyInsights(
    val month: String,
    val totalMinutes: Int,
    val mostActiveCategory: SessionCategory,
    val improvementAreas: List<String>,
    val achievements: List<Achievement>
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconUrl: String,
    val unlockedDate: String,
    val category: AchievementCategory
)

@Serializable
enum class AchievementCategory {
    CONSISTENCY,
    EXPLORATION,
    PROGRESS,
    MILESTONE
)

// Organization Analytics
@Serializable
data class OrganizationAnalytics(
    val organizationId: String,
    val totalActiveUsers: Int,
    val averageStressLevel: Float,
    val sleepRecoveryPercentage: Float,
    val departmentInsights: List<DepartmentInsight>,
    val categoryBreakdown: Map<String, Int>,
    val completionStats: CompletionStats,
    val weeklyTrends: List<WeeklyTrend>
)

@Serializable
data class DepartmentInsight(
    val departmentName: String,
    val activeUsers: Int,
    val averageStressLevel: Float,
    val engagementRate: Float,
    topCategories: List<SessionCategory>
)

@Serializable
data class CompletionStats(
    val totalProgramsStarted: Int,
    val totalProgramsCompleted: Int,
    val completionRate: Float,
    val averageCompletionTime: Int // days
)

@Serializable
data class WeeklyTrend(
    val week: String,
    val activeUsers: Int,
    val totalMinutes: Int,
    val averageStressLevel: Float
)
