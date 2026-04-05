package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.analytics.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mood Analytics Repository
 * Handles mood data storage, analytics, and insights generation
 */
@Singleton
class MoodAnalyticsRepository @Inject constructor(
    private val dataStore: MoodDataStore,
    private val aiService: MoodAnalyticsAI,
    private val riskDetector: MoodRiskDetector
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Save mood entry
     */
    suspend fun saveMoodEntry(entry: MoodEntry): Result<Unit> {
        return try {
            dataStore.saveMoodEntry(entry)
            
            // Trigger analytics update
            updateAnalytics(entry.userId)
            
            // Check for risk alerts
            checkRiskAlerts(entry.userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get mood entries for user
     */
    suspend fun getMoodEntries(
        userId: String,
        period: AnalyticsPeriod = AnalyticsPeriod.WEEK,
        customDateRange: DateRange? = null
    ): Flow<List<MoodEntry>> = flow {
        try {
            val entries = dataStore.getMoodEntries(userId, period, customDateRange)
            emit(entries)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get mood analytics
     */
    suspend fun getMoodAnalytics(request: MoodAnalyticsRequest): Flow<Result<MoodAnalytics>> = flow {
        try {
            val entries = dataStore.getMoodEntries(request.userId, request.period, request.customDateRange)
            
            if (entries.isEmpty()) {
                emit(Result.failure(Exception("No mood data available for the specified period")))
                return@flow
            }
            
            val analytics = generateAnalytics(entries, request)
            emit(Result.success(analytics))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get mood trend data for charts
     */
    suspend fun getMoodChartData(
        userId: String,
        period: AnalyticsPeriod = AnalyticsPeriod.WEEK
    ): Flow<List<MoodChartPoint>> = flow {
        try {
            val entries = dataStore.getMoodEntries(userId, period)
            val chartData = entries.map { entry ->
                MoodChartPoint(
                    date = formatDate(entry.timestamp),
                    timestamp = entry.timestamp,
                    moodScore = entry.score.toFloat(),
                    energyLevel = entry.energyLevel.value.toFloat(),
                    moodType = entry.mood,
                    tags = entry.tags
                )
            }
            emit(chartData)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get mood distribution data
     */
    suspend fun getMoodDistribution(userId: String): Flow<Map<MoodType, Int>> = flow {
        try {
            val entries = dataStore.getAllMoodEntries(userId)
            val distribution = entries.groupBy { it.mood }
                .mapValues { it.value.size }
            emit(distribution)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }
    
    /**
     * Get trigger analysis
     */
    suspend fun getTriggerAnalysis(userId: String): Flow<TriggerAnalysis> = flow {
        try {
            val entries = dataStore.getAllMoodEntries(userId)
            val analysis = analyzeTriggers(entries)
            emit(analysis)
        } catch (e: Exception) {
            emit(TriggerAnalysis(emptyList(), emptyMap(), emptyMap(), emptyMap(), null))
        }
    }
    
    /**
     * Get streak data
     */
    suspend fun getStreakData(userId: String): Flow<StreakData> = flow {
        try {
            val entries = dataStore.getAllMoodEntries(userId)
            val streakData = calculateStreaks(entries)
            emit(streakData)
        } catch (e: Exception) {
            emit(StreakData(0, 0, emptyList(), 0f, 0))
        }
    }
    
    /**
     * Generate AI insights
     */
    suspend fun generateMoodInsights(userId: String): Flow<List<MoodInsight>> = flow {
        try {
            val entries = dataStore.getAllMoodEntries(userId)
            val insights = aiService.generateInsights(entries)
            emit(insights)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get risk alerts
     */
    suspend fun getRiskAlerts(userId: String): Flow<List<RiskAlert>> = flow {
        try {
            val alerts = dataStore.getRiskAlerts(userId)
            emit(alerts.filter { it.isActive })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Dismiss risk alert
     */
    suspend fun dismissRiskAlert(alertId: String): Result<Unit> {
        return try {
            dataStore.updateRiskAlert(alertId, resolvedAt = System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get mood comparison
     */
    suspend fun getMoodComparison(
        userId: String,
        comparisonType: ComparisonType
    ): Flow<MoodComparison> = flow {
        try {
            val (currentPeriod, previousPeriod) = when (comparisonType) {
                ComparisonType.WEEK_OVER_WEEK -> {
                    Pair(
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.WEEK)),
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.WEEK, customDateRange = getPreviousWeekRange()))
                    )
                }
                ComparisonType.MONTH_OVER_MONTH -> {
                    Pair(
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.MONTH)),
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.MONTH, customDateRange = getPreviousMonthRange()))
                    )
                }
                ComparisonType.YEAR_OVER_YEAR -> {
                    Pair(
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.YEAR)),
                        getMoodAnalytics(MoodAnalyticsRequest(userId, AnalyticsPeriod.YEAR, customDateRange = getPreviousYearRange()))
                    )
                }
            }
            
            val currentData = currentPeriod.getOrNull()
            val previousData = previousPeriod.getOrNull()
            
            if (currentData != null && previousData != null) {
                val comparison = MoodComparison(
                    userId = userId,
                    currentPeriod = currentData,
                    previousPeriod = previousData,
                    comparisonType = comparisonType,
                    insights = generateComparisonInsights(currentData, previousData)
                )
                emit(comparison)
            }
        } catch (e: Exception) {
            // Emit empty comparison on error
        }
    }
    
    /**
     * Get dashboard configuration
     */
    suspend fun getDashboardConfig(userId: String): Flow<DashboardConfig> = flow {
        try {
            val config = dataStore.getDashboardConfig(userId)
            emit(config ?: DashboardConfig(userId))
        } catch (e: Exception) {
            emit(DashboardConfig(userId))
        }
    }
    
    /**
     * Update dashboard configuration
     */
    suspend fun updateDashboardConfig(config: DashboardConfig): Result<Unit> {
        return try {
            dataStore.saveDashboardConfig(config)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate comprehensive analytics
     */
    private suspend fun generateAnalytics(
        entries: List<MoodEntry>,
        request: MoodAnalyticsRequest
    ): MoodAnalytics {
        val period = request.period
        
        // Basic statistics
        val totalEntries = entries.size
        val averageMoodScore = entries.map { it.score }.average().toFloat()
        val averageEnergyLevel = entries.map { it.energyLevel.value }.average().toFloat()
        
        // Mood distribution
        val moodDistribution = entries.groupBy { it.mood }.mapValues { it.value.size }
        val mostFrequentMood = moodDistribution.maxByOrNull { it.value }?.key ?: MoodType.OKAY
        val leastFrequentMood = moodDistribution.minByOrNull { it.value }?.key ?: MoodType.OKAY
        
        // Energy distribution
        val energyDistribution = entries.groupBy { it.energyLevel }.mapValues { it.value.size }
        
        // Mood trend
        val moodTrend = calculateMoodTrend(entries)
        
        // Streak data
        val streakData = calculateStreaks(entries)
        
        // Trigger analysis
        val triggerAnalysis = if (request.includeTriggers) {
            analyzeTriggers(entries)
        } else {
            TriggerAnalysis(emptyList(), emptyMap(), emptyMap(), emptyMap(), null)
        }
        
        // AI insights
        val insightData = if (request.includeInsights) {
            aiService.generateInsights(entries)
        } else {
            emptyList()
        }
        
        // Risk alerts
        val riskAlerts = if (request.includeRiskAlerts) {
            riskDetector.detectRisks(entries)
        } else {
            emptyList()
        }
        
        return MoodAnalytics(
            userId = request.userId,
            period = period,
            totalEntries = totalEntries,
            averageMoodScore = averageMoodScore,
            averageEnergyLevel = averageEnergyLevel,
            moodDistribution = moodDistribution,
            energyDistribution = energyDistribution,
            mostFrequentMood = mostFrequentMood,
            leastFrequentMood = leastFrequentMood,
            moodTrend = moodTrend,
            streakData = streakData,
            triggerAnalysis = triggerAnalysis,
            insightData = insightData,
            riskAlerts = riskAlerts
        )
    }
    
    /**
     * Calculate mood trend
     */
    private fun calculateMoodTrend(entries: List<MoodEntry>): MoodTrend {
        if (entries.size < 2) {
            return MoodTrend(TrendDirection.STABLE, 0f, 0f, "Insufficient data")
        }
        
        val sortedEntries = entries.sortedBy { it.timestamp }
        val firstHalf = sortedEntries.take(sortedEntries.size / 2)
        val secondHalf = sortedEntries.drop(sortedEntries.size / 2)
        
        val firstHalfAvg = firstHalf.map { it.score }.average()
        val secondHalfAvg = secondHalf.map { it.score }.average()
        
        val percentageChange = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg * 100).toFloat()
        
        val direction = when {
            percentageChange > 5 -> TrendDirection.IMPROVING
            percentageChange < -5 -> TrendDirection.DECLINING
            abs(percentageChange) <= 5 -> TrendDirection.STABLE
            else -> TrendDirection.FLUCTUATING
        }
        
        val confidence = minOf(1f, entries.size / 30f) // Higher confidence with more data
        
        val description = when (direction) {
            TrendDirection.IMPROVING -> "Your mood has been improving over this period"
            TrendDirection.DECLINING -> "Your mood has been declining over this period"
            TrendDirection.STABLE -> "Your mood has remained relatively stable"
            TrendDirection.FLUCTUATING -> "Your mood has been fluctuating significantly"
        }
        
        return MoodTrend(direction, percentageChange, confidence, description)
    }
    
    /**
     * Calculate streaks
     */
    private fun calculateStreaks(entries: List<MoodEntry>): StreakData {
        val sortedEntries = entries.sortedBy { it.timestamp }
        val streaks = mutableListOf<StreakEntry>()
        var currentStreak = 0
        var longestStreak = 0
        var streakStart: Long? = null
        
        // Group entries by day
        val dailyEntries = sortedEntries.groupBy { 
            formatDate(it.timestamp)
        }
        
        var currentDate = System.currentTimeMillis()
        var consecutiveDays = 0
        
        dailyEntries.forEach { (date, dayEntries) ->
            val dayMood = dayEntries.first().mood
            
            if (isPositiveMood(dayMood)) {
                if (streakStart == null) {
                    streakStart = dayEntries.first().timestamp
                }
                currentStreak++
                consecutiveDays++
            } else {
                if (currentStreak > 0) {
                    streaks.add(StreakEntry(
                        startDate = streakStart ?: dayEntries.first().timestamp,
                        endDate = dayEntries.first().timestamp,
                        length = currentStreak,
                        moodDuringStreak = dayMood
                    ))
                    longestStreak = maxOf(longestStreak, currentStreak)
                    currentStreak = 0
                    streakStart = null
                }
                consecutiveDays = 0
            }
        }
        
        // Add current streak if ongoing
        if (currentStreak > 0) {
            streaks.add(StreakEntry(
                startDate = streakStart ?: System.currentTimeMillis(),
                endDate = System.currentTimeMillis(),
                length = currentStreak,
                moodDuringStreak = sortedEntries.last().mood
            ))
        }
        
        val averageStreakLength = if (streaks.isNotEmpty()) {
            streaks.map { it.length }.average().toFloat()
        } else 0f
        
        return StreakData(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            streakHistory = streaks,
            averageStreakLength = averageStreakLength,
            totalStreakDays = streaks.sumOf { it.length }
        )
    }
    
    /**
     * Analyze triggers
     */
    private fun analyzeTriggers(entries: List<MoodEntry>): TriggerAnalysis {
        val allTags = entries.flatMap { it.tags }
        val tagFrequency = allTags.groupBy { it }.mapValues { it.value.size }
        
        val topTriggers = tagFrequency.map { (tag, count) ->
            TriggerFrequency(
                trigger = tag,
                count = count,
                percentage = (count.toFloat() / allTags.size * 100),
                associatedMoods = entries.filter { it.tags.contains(tag) }.map { it.mood }.distinct()
            )
        }.sortedByDescending { it.count }.take(10)
        
        val moodTriggerCorrelations = entries.groupBy { it.mood }
            .mapValues { (_, moodEntries) ->
                moodEntries.flatMap { it.tags }.distinct()
            }
        
        // Time of day analysis
        val timeOfDayAnalysis = entries.groupBy { 
            getHourOfDay(it.timestamp)
        }
        
        // Day of week analysis
        val dayOfWeekAnalysis = entries.groupBy { 
            getDayOfWeek(it.timestamp)
        }
        
        return TriggerAnalysis(
            topTriggers = topTriggers,
            moodTriggerCorrelations = moodTriggerCorrelations,
            timeOfDayAnalysis = timeOfDayAnalysis,
            dayOfWeekAnalysis = dayOfWeekAnalysis,
            weatherCorrelation = null // Would need weather data integration
        )
    }
    
    /**
     * Check for risk alerts
     */
    private suspend fun checkRiskAlerts(userId: String) {
        val entries = dataStore.getAllMoodEntries(userId)
        val riskAlerts = riskDetector.detectRisks(entries)
        
        // Save new risk alerts
        riskAlerts.forEach { alert ->
            dataStore.saveRiskAlert(alert)
        }
    }
    
    /**
     * Update analytics cache
     */
    private suspend fun updateAnalytics(userId: String) {
        // This would update cached analytics data
        // Implementation depends on caching strategy
    }
    
    /**
     * Helper functions
     */
    private fun isPositiveMood(mood: MoodType): Boolean {
        return mood in listOf(MoodType.HAPPY, MoodType.GOOD, MoodType.OKAY)
    }
    
    private fun formatDate(timestamp: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    private fun getHourOfDay(timestamp: Long): String {
        return java.text.SimpleDateFormat("HH", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    private fun getDayOfWeek(timestamp: Long): String {
        return java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    private fun getPreviousWeekRange(): DateRange {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)
        val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000)
        return DateRange(twoWeeksAgo, weekAgo)
    }
    
    private fun getPreviousMonthRange(): DateRange {
        val now = System.currentTimeMillis()
        val monthAgo = now - (30L * 24 * 60 * 60 * 1000)
        val twoMonthsAgo = now - (60L * 24 * 60 * 60 * 1000)
        return DateRange(twoMonthsAgo, monthAgo)
    }
    
    private fun getPreviousYearRange(): DateRange {
        val now = System.currentTimeMillis()
        val yearAgo = now - (365L * 24 * 60 * 60 * 1000)
        val twoYearsAgo = now - (730L * 24 * 60 * 60 * 1000)
        return DateRange(twoYearsAgo, yearAgo)
    }
    
    private fun generateComparisonInsights(
        current: MoodAnalytics,
        previous: MoodAnalytics
    ): List<String> {
        val insights = mutableListOf<String>()
        
        val moodChange = current.averageMoodScore - previous.averageMoodScore
        if (moodChange > 0.5) {
            insights.add("Your mood has improved by ${String.format("%.1f", moodChange)} points")
        } else if (moodChange < -0.5) {
            insights.add("Your mood has decreased by ${String.format("%.1f", abs(moodChange))} points")
        }
        
        val streakChange = current.streakData.currentStreak - previous.streakData.currentStreak
        if (streakChange > 0) {
            insights.add("Your current streak is $streakChange days longer")
        }
        
        return insights
    }
}

/**
 * Mood data store interface
 */
interface MoodDataStore {
    suspend fun saveMoodEntry(entry: MoodEntry)
    suspend fun getMoodEntries(userId: String, period: AnalyticsPeriod, customDateRange: DateRange? = null): List<MoodEntry>
    suspend fun getAllMoodEntries(userId: String): List<MoodEntry>
    suspend fun saveRiskAlert(alert: RiskAlert)
    suspend fun getRiskAlerts(userId: String): List<RiskAlert>
    suspend fun updateRiskAlert(alertId: String, resolvedAt: Long)
    suspend fun getDashboardConfig(userId: String): DashboardConfig?
    suspend fun saveDashboardConfig(config: DashboardConfig)
}

/**
 * Mood analytics AI service interface
 */
interface MoodAnalyticsAI {
    suspend fun generateInsights(entries: List<MoodEntry>): List<MoodInsight>
}

/**
 * Mood risk detector interface
 */
interface MoodRiskDetector {
    suspend fun detectRisks(entries: List<MoodEntry>): List<RiskAlert>
}
