package com.drmindit.shared.domain.analytics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

/**
 * Mood Insights AI Service
 * Generates intelligent insights from mood data
 */
class MoodInsightsAI {
    
    /**
     * Generate insights from mood entries
     */
    suspend fun generateInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        if (entries.size < 3) {
            return insights
        }
        
        // Pattern detection insights
        insights.addAll(generatePatternInsights(entries))
        
        // Trigger identification insights
        insights.addAll(generateTriggerInsights(entries))
        
        // Coping strategy insights
        insights.addAll(generateCopingInsights(entries))
        
        // Improvement suggestions
        insights.addAll(generateImprovementInsights(entries))
        
        // Risk assessment insights
        insights.addAll(generateRiskInsights(entries))
        
        // Progress recognition insights
        insights.addAll(generateProgressInsights(entries))
        
        return insights.sortedByDescending { it.priority.ordinal }.take(3)
    }
    
    /**
     * Generate pattern detection insights
     */
    private fun generatePatternInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        // Time of day patterns
        val timePatterns = analyzeTimePatterns(entries)
        if (timePatterns.isNotEmpty()) {
            insights.add(
                MoodInsight(
                    id = "time_pattern_${System.currentTimeMillis()}",
                    type = InsightType.PATTERN_DETECTION,
                    title = "Daily Mood Pattern Detected",
                    description = "You tend to feel ${timePatterns.first().mood.displayName.lowercase()} around ${timePatterns.first().time}",
                    recommendation = "Consider scheduling important activities during your peak mood times and self-care during challenging periods.",
                    confidence = 0.8f,
                    dataPoints = timePatterns.map { "${it.time}: ${it.mood.displayName}" },
                    actionable = true,
                    priority = InsightPriority.MEDIUM
                )
            )
        }
        
        // Day of week patterns
        val weeklyPatterns = analyzeWeeklyPatterns(entries)
        if (weeklyPatterns.isNotEmpty()) {
            insights.add(
                MoodInsight(
                    id = "weekly_pattern_${System.currentTimeMillis()}",
                    type = InsightType.PATTERN_DETECTION,
                    title = "Weekly Mood Pattern Identified",
                    description = "Your mood tends to be ${weeklyPatterns.first().mood.displayName.lowercase()} on ${weeklyPatterns.first().day}",
                    recommendation = "Plan challenging tasks for your better days and schedule relaxation for more difficult days.",
                    confidence = 0.7f,
                    dataPoints = weeklyPatterns.map { "${it.day}: ${it.mood.displayName}" },
                    actionable = true,
                    priority = InsightPriority.MEDIUM
                )
            )
        }
        
        // Energy-mood correlation
        val energyMoodCorrelation = analyzeEnergyMoodCorrelation(entries)
        if (energyMoodCorrelation > 0.6) {
            insights.add(
                MoodInsight(
                    id = "energy_mood_correlation_${System.currentTimeMillis()}",
                    type = InsightType.PATTERN_DETECTION,
                    title = "Strong Energy-Mood Connection",
                    description = "Your energy levels and mood are closely linked (${String.format("%.0f", energyMoodCorrelation * 100)}% correlation)",
                    recommendation = "Focus on maintaining good energy through sleep, nutrition, and exercise to support your mood.",
                    confidence = 0.9f,
                    dataPoints = listOf("Energy-Mood Correlation: ${String.format("%.0f", energyMoodCorrelation * 100)}%"),
                    actionable = true,
                    priority = InsightPriority.HIGH
                )
            )
        }
        
        return insights
    }
    
    /**
     * Generate trigger identification insights
     */
    private fun generateTriggerInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        // Identify top negative triggers
        val negativeTriggers = entries
            .filter { it.mood in listOf(MoodType.LOW, MoodType.ANXIOUS) }
            .flatMap { it.tags }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
        
        if (negativeTriggers.isNotEmpty()) {
            val topTrigger = negativeTriggers.first()
            insights.add(
                MoodInsight(
                    id = "trigger_analysis_${System.currentTimeMillis()}",
                    type = InsightType.TRIGGER_IDENTIFICATION,
                    title = "Primary Mood Trigger Identified",
                    description = "'${topTrigger.first}' appears to be your most significant mood trigger, occurring ${topTrigger.second} times",
                    recommendation = "When you encounter '${topTrigger.first}', try using deep breathing or taking a short walk to manage your response.",
                    confidence = 0.8f,
                    dataPoints = listOf("Top trigger: ${topTrigger.first} (${topTrigger.second} occurrences)"),
                    actionable = true,
                    priority = InsightPriority.HIGH
                )
            )
        }
        
        // Work-related mood patterns
        val workEntries = entries.filter { it.tags.contains("work") }
        if (workEntries.size >= 5) {
            val avgWorkMood = workEntries.map { it.score }.average()
            val overallAvgMood = entries.map { it.score }.average()
            
            if (avgWorkMood < overallAvgMood - 1) {
                insights.add(
                    MoodInsight(
                        id = "work_stress_${System.currentTimeMillis()}",
                        type = InsightType.TRIGGER_IDENTIFICATION,
                        title = "Work Impact on Mood",
                        description = "Your mood tends to be lower on work-related days (${String.format("%.1f", avgWorkMood)} vs ${String.format("%.1f", overallAvgMood)} overall)",
                        recommendation = "Consider stress management techniques before and after work, and establish clear work-life boundaries.",
                        confidence = 0.7f,
                        dataPoints = listOf("Work mood: ${String.format("%.1f", avgWorkMood)}", "Overall mood: ${String.format("%.1f", overallAvgMood)}"),
                        actionable = true,
                        priority = InsightPriority.MEDIUM
                    )
                )
            }
        }
        
        return insights
    }
    
    /**
     * Generate coping strategy insights
     */
    private fun generateCopingInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        // Identify effective coping strategies
        val copingEffectiveness = entries
            .filter { it.copingStrategies.isNotEmpty() }
            .flatMap { entry ->
                entry.copingStrategies.map { strategy ->
                    CopingEffectiveness(strategy, entry.score)
                }
            }
            .groupBy { it.strategy }
            .mapValues { effectivenessList ->
                effectivenessList.map { it.effectiveness }.average()
            }
        
        val mostEffectiveStrategy = copingEffectiveness.maxByOrNull { it.value }
        
        if (mostEffectiveStrategy != null) {
            insights.add(
                MoodInsight(
                    id = "coping_strategy_${System.currentTimeMillis()}",
                    type = InsightType.COPING_STRATEGY,
                    title = "Most Effective Coping Strategy",
                    description = "'${mostEffectiveStrategy.key}' appears to be your most effective coping strategy",
                    recommendation = "Continue using '${mostEffectiveStrategy.key}' when feeling down. Consider teaching this strategy to others who might benefit.",
                    confidence = 0.8f,
                    dataPoints = listOf("Strategy: ${mostEffectiveStrategy.key}", "Effectiveness: ${String.format("%.1f", mostEffectiveStrategy.value)}"),
                    actionable = true,
                    priority = InsightPriority.MEDIUM
                )
            )
        }
        
        // Suggest new coping strategies based on patterns
        val anxiousEntries = entries.filter { it.mood == MoodType.ANXIOUS }
        if (anxiousEntries.size >= 3) {
            val currentStrategies = anxiousEntries.flatMap { it.copingStrategies }.toSet()
            val recommendedStrategies = listOf("Deep breathing", "Progressive muscle relaxation", "Mindfulness meditation")
            
            val newStrategies = recommendedStrategies.filter { it !in currentStrategies }
            if (newStrategies.isNotEmpty()) {
                insights.add(
                    MoodInsight(
                        id = "new_coping_${System.currentTimeMillis()}",
                        type = InsightType.COPING_STRATEGY,
                        title = "New Coping Strategy Recommendation",
                        description = "Consider trying '${newStrategies.first()}' for anxiety management",
                        recommendation = "When feeling anxious, try ${newStrategies.first()}. Practice it when you're calm so it's ready when needed.",
                        confidence = 0.6f,
                        dataPoints = listOf("Recommended: ${newStrategies.first()}"),
                        actionable = true,
                        priority = InsightPriority.LOW
                    )
                )
            }
        }
        
        return insights
    }
    
    /**
     * Generate improvement suggestions
     */
    private fun generateImprovementInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        // Sleep quality analysis
        val sleepEntries = entries.filter { it.sleepQuality != null }
        if (sleepEntries.size >= 5) {
            val avgSleepQuality = sleepEntries.map { it.sleepQuality!! }.average()
            val avgMoodWithGoodSleep = sleepEntries.filter { it.sleepQuality!! >= 4 }.map { it.score }.average()
            val avgMoodWithPoorSleep = sleepEntries.filter { it.sleepQuality!! <= 2 }.map { it.score }.average()
            
            if (avgMoodWithGoodSleep > avgMoodWithPoorSleep + 1) {
                insights.add(
                    MoodInsight(
                        id = "sleep_improvement_${System.currentTimeMillis()}",
                        type = InsightType.IMPROVEMENT_SUGGESTION,
                        title = "Sleep Quality Impact on Mood",
                        description = "Your mood is significantly better after good sleep (${String.format("%.1f", avgMoodWithGoodSleep)} vs ${String.format("%.1f", avgMoodWithPoorSleep)})",
                        recommendation = "Prioritize sleep hygiene: maintain consistent sleep schedule, create relaxing bedtime routine, and optimize your sleep environment.",
                        confidence = 0.9f,
                        dataPoints = listOf("Good sleep mood: ${String.format("%.1f", avgMoodWithGoodSleep)}", "Poor sleep mood: ${String.format("%.1f", avgMoodWithPoorSleep)}"),
                        actionable = true,
                        priority = InsightPriority.HIGH
                    )
                )
            }
        }
        
        // Activity level suggestions
        val activeEntries = entries.filter { it.activities.contains("exercise") || it.activities.contains("movement") }
        val inactiveEntries = entries.filter { it.activities.isEmpty() }
        
        if (activeEntries.size >= 3 && inactiveEntries.size >= 3) {
            val avgActiveMood = activeEntries.map { it.score }.average()
            val avgInactiveMood = inactiveEntries.map { it.score }.average()
            
            if (avgActiveMood > avgInactiveMood + 0.5) {
                insights.add(
                    MoodInsight(
                        id = "activity_improvement_${System.currentTimeMillis()}",
                        type = InsightType.IMPROVEMENT_SUGGESTION,
                        title = "Physical Activity Benefits Mood",
                        description = "Your mood improves on days with physical activity (${String.format("%.1f", avgActiveMood)} vs ${String.format("%.1f", avgInactiveMood)})",
                        recommendation = "Try to include some form of physical activity in your daily routine, even just a 10-minute walk.",
                        confidence = 0.7f,
                        dataPoints = listOf("Active mood: ${String.format("%.1f", avgActiveMood)}", "Inactive mood: ${String.format("%.1f", avgInactiveMood)}"),
                        actionable = true,
                        priority = InsightPriority.MEDIUM
                    )
                )
            }
        }
        
        return insights
    }
    
    /**
     * Generate risk assessment insights
     */
    private fun generateRiskInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        // Persistent low mood detection
        val recentEntries = entries.takeLast(7)
        val lowMoodCount = recentEntries.count { it.mood == MoodType.LOW }
        
        if (lowMoodCount >= 4) {
            insights.add(
                MoodInsight(
                    id = "persistent_low_mood_${System.currentTimeMillis()}",
                    type = InsightType.RISK_ASSESSMENT,
                    title = "Persistent Low Mood Detected",
                    description = "You've reported low mood ${lowMoodCount} out of the last 7 days",
                    recommendation = "Consider reaching out to a mental health professional. Persistent low mood may benefit from professional support.",
                    confidence = 0.9f,
                    dataPoints = listOf("Low mood days: $lowMoodCount/7"),
                    actionable = true,
                    priority = InsightPriority.CRITICAL
                )
            )
        }
        
        // High anxiety frequency
        val anxietyCount = recentEntries.count { it.mood == MoodType.ANXIOUS }
        if (anxietyCount >= 4) {
            insights.add(
                MoodInsight(
                    id = "high_anxiety_${System.currentTimeMillis()}",
                    type = InsightType.RISK_ASSESSMENT,
                    title = "Frequent Anxiety Episodes",
                    description = "You've experienced anxiety ${anxietyCount} times in the last 7 days",
                    recommendation = "Practice regular anxiety management techniques and consider professional support if anxiety interferes with daily activities.",
                    confidence = 0.8f,
                    dataPoints = listOf("Anxiety episodes: $anxietyCount/7"),
                    actionable = true,
                    priority = InsightPriority.HIGH
                )
            )
        }
        
        // Social isolation indicators
        val socialEntries = entries.filter { it.socialInteraction == true }
        val totalEntries = entries.size
        val socialInteractionRate = socialEntries.size.toFloat() / totalEntries.toFloat()
        
        if (socialInteractionRate < 0.3 && totalEntries >= 10) {
            insights.add(
                MoodInsight(
                    id = "social_isolation_${System.currentTimeMillis()}",
                    type = InsightType.RISK_ASSESSMENT,
                    title = "Low Social Interaction Detected",
                    description = "You've reported social interaction in only ${String.format("%.0f", socialInteractionRate * 100)}% of your mood entries",
                    recommendation = "Consider reaching out to friends or family, or joining groups with shared interests. Social connection is important for mental wellbeing.",
                    confidence = 0.7f,
                    dataPoints = listOf("Social interaction rate: ${String.format("%.0f", socialInteractionRate * 100)}%"),
                    actionable = true,
                    priority = InsightPriority.MEDIUM
                )
            )
        }
        
        return insights
    }
    
    /**
     * Generate progress recognition insights
     */
    private fun generateProgressInsights(entries: List<MoodEntry>): List<MoodInsight> {
        val insights = mutableListOf<MoodInsight>()
        
        if (entries.size < 10) {
            return insights
        }
        
        // Recent improvement
        val recentEntries = entries.takeLast(7)
        val previousEntries = entries.dropLast(7).takeLast(7)
        
        if (previousEntries.isNotEmpty()) {
            val recentAvg = recentEntries.map { it.score }.average()
            val previousAvg = previousEntries.map { it.score }.average()
            
            if (recentAvg > previousAvg + 0.5) {
                insights.add(
                    MoodInsight(
                        id = "mood_improvement_${System.currentTimeMillis()}",
                        type = InsightType.PROGRESS_RECOGNITION,
                        title = "Mood Improvement Detected",
                        description = "Your average mood has improved by ${String.format("%.1f", recentAvg - previousAvg)} points recently",
                        recommendation = "Great progress! Continue with the strategies that are working for you. Consider what changes contributed to this improvement.",
                        confidence = 0.8f,
                        dataPoints = listOf("Recent average: ${String.format("%.1f", recentAvg)}", "Previous average: ${String.format("%.1f", previousAvg)}"),
                        actionable = false,
                        priority = InsightPriority.LOW
                    )
                )
            }
        }
        
        // Consistency achievement
        val thisMonth = entries.filter { 
            isSameMonth(it.timestamp, System.currentTimeMillis())
        }
        
        if (thisMonth.size >= 20) {
            insights.add(
                MoodInsight(
                    id = "consistency_achievement_${System.currentTimeMillis()}",
                    type = InsightType.PROGRESS_RECOGNITION,
                    title = "Excellent Tracking Consistency",
                    description = "You've tracked your mood ${thisMonth.size} times this month",
                    recommendation = "Consistent tracking is key to understanding your patterns. Keep up the great work!",
                    confidence = 1.0f,
                    dataPoints = listOf("Monthly entries: ${thisMonth.size}"),
                    actionable = false,
                    priority = InsightPriority.LOW
                )
            )
        }
        
        return insights
    }
    
    /**
     * Analyze time patterns
     */
    private fun analyzeTimePatterns(entries: List<MoodEntry>): List<TimePattern> {
        return entries
            .groupBy { getHourOfDay(it.timestamp) }
            .mapValues { (_, timeEntries) ->
                timeEntries.map { it.score }.average()
            }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { (time, avgScore) ->
                val mood = when {
                    avgScore >= 4 -> MoodType.HAPPY
                    avgScore >= 3 -> MoodType.GOOD
                    avgScore >= 2 -> MoodType.OKAY
                    avgScore >= 1 -> MoodType.LOW
                    else -> MoodType.ANXIOUS
                }
                TimePattern(time, mood)
            }
    }
    
    /**
     * Analyze weekly patterns
     */
    private fun analyzeWeeklyPatterns(entries: List<MoodEntry>): List<WeeklyPattern> {
        return entries
            .groupBy { getDayOfWeek(it.timestamp) }
            .mapValues { (_, dayEntries) ->
                dayEntries.map { it.score }.average()
            }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { (day, avgScore) ->
                val mood = when {
                    avgScore >= 4 -> MoodType.HAPPY
                    avgScore >= 3 -> MoodType.GOOD
                    avgScore >= 2 -> MoodType.OKAY
                    avgScore >= 1 -> MoodType.LOW
                    else -> MoodType.ANXIOUS
                }
                WeeklyPattern(day, mood)
            }
    }
    
    /**
     * Analyze energy-mood correlation
     */
    private fun analyzeEnergyMoodCorrelation(entries: List<MoodEntry>): Double {
        if (entries.size < 3) return 0.0
        
        val moodScores = entries.map { it.score.toDouble() }
        val energyScores = entries.map { it.energyLevel.value.toDouble() }
        
        return calculateCorrelation(moodScores, energyScores)
    }
    
    /**
     * Calculate correlation coefficient
     */
    private fun calculateCorrelation(x: List<Double>, y: List<Double>): Double {
        if (x.size != y.size || x.size < 2) return 0.0
        
        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y).sumOf { it.first * it.second }
        val sumX2 = x.sumOf { it * it }
        val sumY2 = y.sumOf { it * it }
        
        val numerator = n * sumXY - sumX * sumY
        val denominator = kotlin.math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }
    
    /**
     * Helper functions
     */
    private fun getHourOfDay(timestamp: Long): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    private fun getDayOfWeek(timestamp: Long): String {
        return java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
    
    private fun isSameMonth(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.timeInMillis = timestamp1
        cal2.timeInMillis = timestamp2
        return cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH) &&
               cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
    }
    
    /**
     * Data classes for pattern analysis
     */
    private data class TimePattern(val time: String, val mood: MoodType)
    private data class WeeklyPattern(val day: String, val mood: MoodType)
    private data class CopingEffectiveness(val strategy: String, val effectiveness: Double)
}
