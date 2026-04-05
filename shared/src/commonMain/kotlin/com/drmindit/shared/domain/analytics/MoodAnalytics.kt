package com.drmindit.shared.domain.analytics

import kotlinx.serialization.Serializable

/**
 * Mood Analytics Data Models
 * Comprehensive mood tracking and analytics system
 */

/**
 * Mood entry with rich metadata
 */
@Serializable
data class MoodEntry(
    val id: String,
    val userId: String,
    val mood: MoodType,
    val score: Int, // 1-5 scale
    val energyLevel: EnergyLevel,
    val tags: List<String>,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,
    val weather: String? = null,
    val activities: List<String> = emptyList(),
    val triggers: List<String> = emptyList(),
    val copingStrategies: List<String> = emptyList(),
    val sleepQuality: Int? = null, // 1-5 scale
    val stressLevel: Int? = null, // 1-5 scale
    val socialInteraction: Boolean? = null,
    val medicationTaken: Boolean? = null,
    val therapySession: Boolean? = null
)

/**
 * Mood types with corresponding colors and descriptions
 */
@Serializable
enum class MoodType(val displayName: String, val color: String, val description: String) {
    HAPPY("Happy", "#4CAF50", "Feeling joyful, positive, and optimistic"),
    GOOD("Good", "#8BC34A", "Feeling content and satisfied"),
    OKAY("Okay", "#FFC107", "Feeling neutral or balanced"),
    LOW("Low", "#FF9800", "Feeling down, sad, or unmotivated"),
    ANXIOUS("Anxious", "#F44336", "Feeling worried, nervous, or tense")
}

/**
 * Energy levels
 */
@Serializable
enum class EnergyLevel(val displayName: String, val value: Int, val color: String) {
    VERY_LOW("Very Low", 1, "#D32F2F"),
    LOW("Low", 2, "#F44336"),
    MODERATE("Moderate", 3, "#FF9800"),
    HIGH("High", 4, "#4CAF50"),
    VERY_HIGH("Very High", 5, "#8BC34A")
}

/**
 * Mood analytics summary
 */
@Serializable
data class MoodAnalytics(
    val userId: String,
    val period: AnalyticsPeriod,
    val totalEntries: Int,
    val averageMoodScore: Float,
    val averageEnergyLevel: Float,
    val moodDistribution: Map<MoodType, Int>,
    val energyDistribution: Map<EnergyLevel, Int>,
    val mostFrequentMood: MoodType,
    val leastFrequentMood: MoodType,
    val moodTrend: MoodTrend,
    val streakData: StreakData,
    val triggerAnalysis: TriggerAnalysis,
    val insightData: List<MoodInsight>,
    val riskAlerts: List<RiskAlert>,
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Analytics period
 */
@Serializable
enum class AnalyticsPeriod(val displayName: String, val days: Int) {
    WEEK("Week", 7),
    MONTH("Month", 30),
    QUARTER("Quarter", 90),
    YEAR("Year", 365)
}

/**
 * Mood trend analysis
 */
@Serializable
data class MoodTrend(
    val direction: TrendDirection,
    val percentageChange: Float,
    val confidence: Float,
    val description: String
)

/**
 * Trend direction
 */
@Serializable
enum class TrendDirection(val displayName: String, val color: String) {
    IMPROVING("Improving", "#4CAF50"),
    STABLE("Stable", "#FFC107"),
    DECLINING("Declining", "#F44336"),
    FLUCTUATING("Fluctuating", "#9C27B0")
}

/**
 * Streak tracking data
 */
@Serializable
data class StreakData(
    val currentStreak: Int,
    val longestStreak: Int,
    val streakHistory: List<StreakEntry>,
    val averageStreakLength: Float,
    val totalStreakDays: Int
)

/**
 * Individual streak entry
 */
@Serializable
data class StreakEntry(
    val startDate: Long,
    val endDate: Long,
    val length: Int,
    val moodDuringStreak: MoodType
)

/**
 * Trigger analysis
 */
@Serializable
data class TriggerAnalysis(
    val topTriggers: List<TriggerFrequency>,
    val moodTriggerCorrelations: Map<MoodType, List<String>>,
    val timeOfDayAnalysis: Map<String, List<MoodEntry>>,
    val dayOfWeekAnalysis: Map<String, List<MoodEntry>>,
    val weatherCorrelation: Map<String, Float>?
)

/**
 * Trigger frequency data
 */
@Serializable
data class TriggerFrequency(
    val trigger: String,
    val count: Int,
    val percentage: Float,
    val associatedMoods: List<MoodType>
)

/**
 * AI-generated mood insight
 */
@Serializable
data class MoodInsight(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val recommendation: String,
    val confidence: Float,
    val dataPoints: List<String>,
    val actionable: Boolean,
    val priority: InsightPriority,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Insight types
 */
@Serializable
enum class InsightType(val displayName: String) {
    PATTERN_DETECTION("Pattern Detection"),
    TRIGGER_IDENTIFICATION("Trigger Identification"),
    COPING_STRATEGY("Coping Strategy"),
    IMPROVEMENT_SUGGESTION("Improvement Suggestion"),
    RISK_ASSESSMENT("Risk Assessment"),
    PROGRESS_RECOGNITION("Progress Recognition")
}

/**
 * Insight priority levels
 */
@Serializable
enum class InsightPriority(val displayName: String, val color: String) {
    LOW("Low", "#4CAF50"),
    MEDIUM("Medium", "#FFC107"),
    HIGH("High", "#FF9800"),
    CRITICAL("Critical", "#F44336")
}

/**
 * Risk alert for mood monitoring
 */
@Serializable
data class RiskAlert(
    val id: String,
    val type: RiskType,
    val severity: RiskSeverity,
    val title: String,
    val description: String,
    val recommendation: String,
    val dataPoints: List<String>,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

/**
 * Risk types
 */
@Serializable
enum class RiskType(val displayName: String) {
    PERSISTENT_LOW_MOOD("Persistent Low Mood"),
    DECLINING_MOOD_TREND("Declining Mood Trend"),
    HIGH_ANXIETY_FREQUENCY("High Anxiety Frequency"),
    POOR_SLEEP_CORRELATION("Poor Sleep Correlation"),
    SOCIAL_ISOLATION("Social Isolation"),
    MEDICATION_NON_ADHERENCE("Medication Non-Adherence")
}

/**
 * Risk severity levels
 */
@Serializable
enum class RiskSeverity(val displayName: String, val color: String) {
    LOW("Low", "#4CAF50"),
    MODERATE("Moderate", "#FF9800"),
    HIGH("High", "#F44336"),
    CRITICAL("Critical", "#D32F2F")
}

/**
 * Mood chart data point
 */
@Serializable
data class MoodChartPoint(
    val date: String,
    val timestamp: Long,
    val moodScore: Float,
    val energyLevel: Float,
    val moodType: MoodType,
    val tags: List<String>
)

/**
 * Dashboard configuration
 */
@Serializable
data class DashboardConfig(
    val userId: String,
    val preferredTimeRange: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val showEnergyLevels: Boolean = true,
    val showTriggers: Boolean = true,
    val showInsights: Boolean = true,
    val showRiskAlerts: Boolean = true,
    val chartType: ChartType = ChartType.LINE,
    val colorScheme: ColorScheme = ColorScheme.CALM,
    val notificationPreferences: NotificationPreferences = NotificationPreferences()
)

/**
 * Chart types
 */
@Serializable
enum class ChartType(val displayName: String) {
    LINE("Line Chart"),
    BAR("Bar Chart"),
    AREA("Area Chart"),
    SCATTER("Scatter Plot")
}

/**
 * Color schemes for dashboard
 */
@Serializable
enum class ColorScheme(val displayName: String, val primary: String, val secondary: String) {
    CALM("Calm", "#2196F3", "#81C784"),
    WARM("Warm", "#FF9800", "#FFC107"),
    COOL("Cool", "#00BCD4", "#4CAF50"),
    MONOCHROME("Monochrome", "#607D8B", "#90A4AE")
}

/**
 * Notification preferences
 */
@Serializable
data class NotificationPreferences(
    val moodReminders: Boolean = true,
    val streakNotifications: Boolean = true,
    val riskAlerts: Boolean = true,
    val weeklyReports: Boolean = true,
    val insightNotifications: Boolean = true,
    val reminderTime: String = "09:00",
    val quietHours: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)

/**
 * Mood tracking goal
 */
@Serializable
data class MoodTrackingGoal(
    val id: String,
    val userId: String,
    val type: GoalType,
    val target: Float,
    val current: Float,
    val unit: String,
    val deadline: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Goal types
 */
@Serializable
enum class GoalType(val displayName: String) {
    MOOD_IMPROVEMENT("Mood Improvement"),
    STREAK_MAINTENANCE("Streak Maintenance"),
    ENERGY_BOOST("Energy Boost"),
    ANXIETY_REDUCTION("Anxiety Reduction"),
    CONSISTENT_TRACKING("Consistent Tracking")
)

/**
 * Mood analytics request
 */
@Serializable
data class MoodAnalyticsRequest(
    val userId: String,
    val period: AnalyticsPeriod,
    val includeInsights: Boolean = true,
    val includeRiskAlerts: Boolean = true,
    val includeTriggers: Boolean = true,
    val customDateRange: DateRange? = null
)

/**
 * Date range for custom analytics
 */
@Serializable
data class DateRange(
    val startDate: Long,
    val endDate: Long
)

/**
 * Mood comparison data
 */
@Serializable
data class MoodComparison(
    val userId: String,
    val currentPeriod: MoodAnalytics,
    val previousPeriod: MoodAnalytics,
    val comparisonType: ComparisonType,
    val insights: List<String>
)

/**
 * Comparison types
 */
@Serializable
enum class ComparisonType(val displayName: String) {
    WEEK_OVER_WEEK("Week over Week"),
    MONTH_OVER_MONTH("Month over Month"),
    YEAR_OVER_YEAR("Year over Year")
)
