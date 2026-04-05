package com.drmindit.shared.domain.analytics

import kotlin.math.abs

/**
 * Mood Risk Detector
 * Identifies potential mental health risks from mood patterns
 */
class MoodRiskDetector {
    
    /**
     * Detect risks from mood entries
     */
    suspend fun detectRisks(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        if (entries.size < 3) {
            return risks
        }
        
        // Check for persistent low mood
        risks.addAll(detectPersistentLowMood(entries))
        
        // Check for declining mood trend
        risks.addAll(detectDecliningMoodTrend(entries))
        
        // Check for high anxiety frequency
        risks.addAll(detectHighAnxietyFrequency(entries))
        
        // Check for poor sleep correlation
        risks.addAll(detectPoorSleepCorrelation(entries))
        
        // Check for social isolation
        risks.addAll(detectSocialIsolation(entries))
        
        // Check for medication non-adherence
        risks.addAll(detectMedicationNonAdherence(entries))
        
        return risks.sortedByDescending { it.severity.ordinal }
    }
    
    /**
     * Detect persistent low mood
     */
    private fun detectPersistentLowMood(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        // Check last 7 days
        val recentEntries = entries.takeLast(7)
        val lowMoodCount = recentEntries.count { it.mood == MoodType.LOW }
        val anxiousMoodCount = recentEntries.count { it.mood == MoodType.ANXIOUS }
        
        if (lowMoodCount >= 5) {
            risks.add(
                RiskAlert(
                    id = "persistent_low_mood_${System.currentTimeMillis()}",
                    type = RiskType.PERSISTENT_LOW_MOOD,
                    severity = RiskSeverity.HIGH,
                    title = "Persistent Low Mood Detected",
                    description = "You've reported low mood on $lowMoodCount out of the last 7 days",
                    recommendation = "Consider reaching out to a mental health professional. Persistent low mood may benefit from professional support and treatment.",
                    dataPoints = listOf("Low mood days: $lowMoodCount/7", "Average mood: ${String.format("%.1f", recentEntries.map { it.score }.average())}"),
                    isActive = true
                )
            )
        } else if (lowMoodCount >= 3) {
            risks.add(
                RiskAlert(
                    id = "frequent_low_mood_${System.currentTimeMillis()}",
                    type = RiskType.PERSISTENT_LOW_MOOD,
                    severity = RiskSeverity.MODERATE,
                    title = "Frequent Low Mood",
                    description = "You've reported low mood on $lowMoodCount out of the last 7 days",
                    recommendation = "Monitor your mood closely and consider stress management techniques. If low mood persists, consider professional support.",
                    dataPoints = listOf("Low mood days: $lowMoodCount/7"),
                    isActive = true
                )
            )
        }
        
        // Check for combination of low mood and anxiety
        val totalNegativeMoods = lowMoodCount + anxiousMoodCount
        if (totalNegativeMoods >= 6) {
            risks.add(
                RiskAlert(
                    id = "high_negative_mood_${System.currentTimeMillis()}",
                    type = RiskType.PERSISTENT_LOW_MOOD,
                    severity = RiskSeverity.CRITICAL,
                    title = "High Frequency of Negative Mood",
                    description = "You've reported negative mood (low or anxious) on $totalNegativeMoods out of the last 7 days",
                    recommendation = "This level of persistent negative mood warrants immediate professional attention. Please consider contacting a mental health provider or crisis services if needed.",
                    dataPoints = listOf("Negative mood days: $totalNegativeMoods/7"),
                    isActive = true
                )
            )
        }
        
        return risks
    }
    
    /**
     * Detect declining mood trend
     */
    private fun detectDecliningMoodTrend(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        if (entries.size < 5) {
            return risks
        }
        
        // Analyze trend over last 14 days
        val recentEntries = entries.takeLast(14)
        val firstHalf = recentEntries.take(7)
        val secondHalf = recentEntries.drop(7).take(7)
        
        if (firstHalf.isNotEmpty() && secondHalf.isNotEmpty()) {
            val firstHalfAvg = firstHalf.map { it.score }.average()
            val secondHalfAvg = secondHalf.map { it.score }.average()
            val declinePercentage = ((firstHalfAvg - secondHalfAvg) / firstHalfAvg * 100)
            
            if (declinePercentage >= 30) {
                risks.add(
                    RiskAlert(
                        id = "significant_decline_${System.currentTimeMillis()}",
                        type = RiskType.DECLINING_MOOD_TREND,
                        severity = RiskSeverity.HIGH,
                        title = "Significant Mood Decline Detected",
                        description = "Your mood has declined by ${String.format("%.0f", declinePercentage)}% over the last two weeks",
                        recommendation = "This significant decline warrants attention. Consider recent life changes and stressors. Professional support may be beneficial.",
                        dataPoints = listOf("Decline: ${String.format("%.0f", declinePercentage)}%", "Previous average: ${String.format("%.1f", firstHalfAvg)}", "Recent average: ${String.format("%.1f", secondHalfAvg)}"),
                        isActive = true
                    )
                )
            } else if (declinePercentage >= 15) {
                risks.add(
                    RiskAlert(
                        id = "moderate_decline_${System.currentTimeMillis()}",
                        type = RiskType.DECLINING_MOOD_TREND,
                        severity = RiskSeverity.MODERATE,
                        title = "Moderate Mood Decline",
                        description = "Your mood has declined by ${String.format("%.0f", declinePercentage)}% over the last two weeks",
                        recommendation = "Monitor this trend closely. Identify potential causes and consider stress management strategies.",
                        dataPoints = listOf("Decline: ${String.format("%.0f", declinePercentage)}%"),
                        isActive = true
                    )
                )
            }
        }
        
        // Check for consistent downward trend over longer period
        if (entries.size >= 21) {
            val monthlyEntries = entries.takeLast(21)
            val weeklyAverages = monthlyEntries.chunked(7).map { week ->
                week.map { it.score }.average()
            }
            
            if (weeklyAverages.size >= 3) {
                val isConsistentlyDeclining = weeklyAverages.zipWithNext { a, b -> b < a }.all { it }
                
                if (isConsistentlyDeclining) {
                    risks.add(
                        RiskAlert(
                            id = "consistent_decline_${System.currentTimeMillis()}",
                            type = RiskType.DECLINING_MOOD_TREND,
                            severity = RiskSeverity.HIGH,
                            title = "Consistent Declining Trend",
                            description = "Your mood has shown a consistent declining trend over the last three weeks",
                            recommendation = "A consistent declining pattern requires attention. Consider professional evaluation to identify underlying causes.",
                            dataPoints = listOf("Weekly averages: ${weeklyAverages.map { String.format("%.1f", it) }}"),
                            isActive = true
                        )
                    )
                }
            }
        }
        
        return risks
    }
    
    /**
     * Detect high anxiety frequency
     */
    private fun detectHighAnxietyFrequency(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        // Check last 7 days
        val recentEntries = entries.takeLast(7)
        val anxietyCount = recentEntries.count { it.mood == MoodType.ANXIOUS }
        val anxietyPercentage = (anxietyCount.toFloat() / recentEntries.size) * 100
        
        if (anxietyCount >= 5) {
            risks.add(
                RiskAlert(
                    id = "high_anxiety_frequency_${System.currentTimeMillis()}",
                    type = RiskType.HIGH_ANXIETY_FREQUENCY,
                    severity = RiskSeverity.HIGH,
                    title = "High Anxiety Frequency",
                    description = "You've experienced anxiety on $anxietyCount out of the last 7 days (${String.format("%.0f", anxietyPercentage)}%)",
                    recommendation = "Frequent anxiety may benefit from professional treatment and regular anxiety management techniques.",
                    dataPoints = listOf("Anxiety days: $anxietyCount/7", "Frequency: ${String.format("%.0f", anxietyPercentage)}%"),
                    isActive = true
                )
            )
        } else if (anxietyCount >= 3) {
            risks.add(
                RiskAlert(
                    id = "moderate_anxiety_frequency_${System.currentTimeMillis()}",
                    type = RiskType.HIGH_ANXIETY_FREQUENCY,
                    severity = RiskSeverity.MODERATE,
                    title = "Moderate Anxiety Frequency",
                    description = "You've experienced anxiety on $anxietyCount out of the last 7 days",
                    recommendation = "Consider regular anxiety management practices. Monitor patterns and seek professional help if anxiety interferes with daily life.",
                    dataPoints = listOf("Anxiety days: $anxietyCount/7"),
                    isActive = true
                )
            )
        }
        
        // Check for increasing anxiety severity
        val anxiousEntries = entries.filter { it.mood == MoodType.ANXIOUS }
        if (anxiousEntries.size >= 5) {
            val recentAnxiousEntries = anxiousEntries.takeLast(5)
            val olderAnxiousEntries = anxiousEntries.dropLast(5).takeLast(5)
            
            if (olderAnxiousEntries.isNotEmpty()) {
                val recentAvgSeverity = recentAnxiousEntries.map { it.score }.average()
                val olderAvgSeverity = olderAnxiousEntries.map { it.score }.average()
                
                if (recentAvgSeverity < olderAvgSeverity - 0.5) {
                    risks.add(
                        RiskAlert(
                            id = "worsening_anxiety_${System.currentTimeMillis()}",
                            type = RiskType.HIGH_ANXIETY_FREQUENCY,
                            severity = RiskSeverity.HIGH,
                            title = "Worsening Anxiety Severity",
                            description = "Your anxiety appears to be increasing in severity recently",
                            recommendation = "Increasing anxiety severity may require professional intervention. Consider contacting a mental health provider.",
                            dataPoints = listOf("Recent severity: ${String.format("%.1f", recentAvgSeverity)}", "Previous severity: ${String.format("%.1f", olderAvgSeverity)}"),
                            isActive = true
                        )
                    )
                }
            }
        }
        
        return risks
    }
    
    /**
     * Detect poor sleep correlation
     */
    private fun detectPoorSleepCorrelation(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        val sleepEntries = entries.filter { it.sleepQuality != null }
        if (sleepEntries.size < 5) {
            return risks
        }
        
        // Analyze sleep quality vs mood correlation
        val poorSleepEntries = sleepEntries.filter { it.sleepQuality!! <= 2 }
        val goodSleepEntries = sleepEntries.filter { it.sleepQuality!! >= 4 }
        
        if (poorSleepEntries.isNotEmpty() && goodSleepEntries.isNotEmpty()) {
            val avgMoodWithPoorSleep = poorSleepEntries.map { it.score }.average()
            val avgMoodWithGoodSleep = goodSleepEntries.map { it.score }.average()
            val sleepImpact = avgMoodWithGoodSleep - avgMoodWithPoorSleep
            
            if (sleepImpact >= 1.5) {
                risks.add(
                    RiskAlert(
                        id = "significant_sleep_impact_${System.currentTimeMillis()}",
                        type = RiskType.POOR_SLEEP_CORRELATION,
                        severity = RiskSeverity.MODERATE,
                        title = "Sleep Quality Significantly Impacts Mood",
                        description = "Your mood is ${String.format("%.1f", sleepImpact)} points lower after poor sleep",
                        recommendation = "Prioritize sleep hygiene and consider consulting a healthcare provider about sleep issues. Good sleep is crucial for mental health.",
                        dataPoints = listOf("Sleep impact: ${String.format("%.1f", sleepImpact)} points", "Poor sleep mood: ${String.format("%.1f", avgMoodWithPoorSleep)}", "Good sleep mood: ${String.format("%.1f", avgMoodWithGoodSleep)}"),
                        isActive = true
                    )
                )
            }
        }
        
        // Check for chronic poor sleep
        val recentSleepEntries = sleepEntries.takeLast(7)
        val poorSleepCount = recentSleepEntries.count { it.sleepQuality!! <= 2 }
        
        if (poorSleepCount >= 5) {
            risks.add(
                RiskAlert(
                    id = "chronic_poor_sleep_${System.currentTimeMillis()}",
                    type = RiskType.POOR_SLEEP_CORRELATION,
                    severity = RiskSeverity.HIGH,
                    title = "Chronic Poor Sleep",
                    description = "You've reported poor sleep quality on $poorSleepCount out of the last 7 days",
                    recommendation = "Chronic poor sleep significantly impacts mental health. Consider sleep hygiene improvements and professional medical consultation.",
                    dataPoints = listOf("Poor sleep days: $poorSleepCount/7"),
                    isActive = true
                )
            )
        }
        
        return risks
    }
    
    /**
     * Detect social isolation
     */
    private fun detectSocialIsolation(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        val socialEntries = entries.filter { it.socialInteraction != null }
        if (socialEntries.size < 5) {
            return risks
        }
        
        val recentSocialEntries = socialEntries.takeLast(7)
        val socialInteractionCount = recentSocialEntries.count { it.socialInteraction == true }
        val socialInteractionRate = (socialInteractionCount.toFloat() / recentSocialEntries.size) * 100
        
        if (socialInteractionRate <= 20) {
            risks.add(
                RiskAlert(
                    id = "social_isolation_${System.currentTimeMillis()}",
                    type = RiskType.SOCIAL_ISOLATION,
                    severity = RiskSeverity.MODERATE,
                    title = "Low Social Interaction",
                    description = "You've reported social interaction in only ${String.format("%.0f", socialInteractionRate)}% of recent entries",
                    recommendation = "Social connection is important for mental health. Consider reaching out to friends, family, or joining groups with shared interests.",
                    dataPoints = listOf("Social interaction rate: ${String.format("%.0f", socialInteractionRate)}%"),
                    isActive = true
                )
            )
        }
        
        // Check correlation between social isolation and low mood
        val isolatedEntries = recentSocialEntries.filter { it.socialInteraction == false }
        val isolatedLowMoodCount = isolatedEntries.count { it.mood == MoodType.LOW }
        
        if (isolatedEntries.isNotEmpty() && isolatedLowMoodCount.toFloat() / isolatedEntries.size >= 0.7) {
            risks.add(
                RiskAlert(
                    id = "isolation_mood_correlation_${System.currentTimeMillis()}",
                    type = RiskType.SOCIAL_ISOLATION,
                    severity = RiskSeverity.HIGH,
                    title = "Social Isolation Correlates with Low Mood",
                    description = "${String.format("%.0f", isolatedLowMoodCount.toFloat() / isolatedEntries.size * 100)}% of isolated days are low mood days",
                    recommendation = "Your mood appears to be lower on days with less social interaction. Consider scheduling regular social activities.",
                    dataPoints = listOf("Isolation-low mood correlation: ${String.format("%.0f", isolatedLowMoodCount.toFloat() / isolatedEntries.size * 100)}%"),
                    isActive = true
                )
            )
        }
        
        return risks
    }
    
    /**
     * Detect medication non-adherence
     */
    private fun detectMedicationNonAdherence(entries: List<MoodEntry>): List<RiskAlert> {
        val risks = mutableListOf<RiskAlert>()
        
        val medicationEntries = entries.filter { it.medicationTaken != null }
        if (medicationEntries.size < 7) {
            return risks
        }
        
        val recentMedicationEntries = medicationEntries.takeLast(14)
        val medicationTakenCount = recentMedicationEntries.count { it.medicationTaken == true }
        val medicationAdherenceRate = (medicationTakenCount.toFloat() / recentMedicationEntries.size) * 100
        
        if (medicationAdherenceRate <= 60) {
            risks.add(
                RiskAlert(
                    id = "medication_non_adherence_${System.currentTimeMillis()}",
                    type = RiskType.MEDICATION_NON_ADHERENCE,
                    severity = RiskSeverity.HIGH,
                    title = "Low Medication Adherence",
                    description = "You've taken medication as prescribed on only ${String.format("%.0f", medicationAdherenceRate)}% of days",
                    recommendation = "Consistent medication adherence is crucial for treatment effectiveness. Consider reminder systems and discuss any side effects with your healthcare provider.",
                    dataPoints = listOf("Adherence rate: ${String.format("%.0f", medicationAdherenceRate)}%"),
                    isActive = true
                )
            )
        } else if (medicationAdherenceRate <= 80) {
            risks.add(
                RiskAlert(
                    id = "moderate_medication_adherence_${System.currentTimeMillis()}",
                    type = RiskType.MEDICATION_NON_ADHERENCE,
                    severity = RiskSeverity.MODERATE,
                    title = "Moderate Medication Adherence",
                    description = "You've taken medication as prescribed on ${String.format("%.0f", medicationAdherenceRate)}% of days",
                    recommendation = "Try to improve medication consistency. Set up reminders and establish a routine. Discuss any barriers with your healthcare provider.",
                    dataPoints = listOf("Adherence rate: ${String.format("%.0f", medicationAdherenceRate)}%"),
                    isActive = true
                )
            )
        }
        
        // Check correlation between medication non-adherence and mood
        val missedMedicationEntries = recentMedicationEntries.filter { it.medicationTaken == false }
        if (missedMedicationEntries.isNotEmpty()) {
            val avgMoodMissed = missedMedicationEntries.map { it.score }.average()
            val avgMoodTaken = recentMedicationEntries.filter { it.medicationTaken == true }.map { it.score }.average()
            
            if (avgMoodMissed < avgMoodTaken - 0.5) {
                risks.add(
                    RiskAlert(
                        id = "medication_mood_correlation_${System.currentTimeMillis()}",
                        type = RiskType.MEDICATION_NON_ADHERENCE,
                        severity = RiskSeverity.MODERATE,
                        title = "Medication Non-Adherence Affects Mood",
                        description = "Your mood tends to be lower on days you miss medication",
                        recommendation = "This correlation shows how important medication consistency is for your mood stability. Work on improving adherence.",
                        dataPoints = listOf("Missed medication mood: ${String.format("%.1f", avgMoodMissed)}", "Taken medication mood: ${String.format("%.1f", avgMoodTaken)}"),
                        isActive = true
                    )
                )
            }
        }
        
        return risks
    }
    
    /**
     * Helper function to zip list with next element
     */
    private fun <T> List<T>.zipWithNext(transform: (T, T) -> Boolean): List<Boolean> {
        if (this.size < 2) return emptyList()
        return this.zipWithNext { a, b -> transform(a, b) }
    }
    
    /**
     * Helper function to zip with next element
     */
    private fun <T> List<T>.zipWithNext(transform: (T, T) -> R): List<R> {
        if (this.size < 2) return emptyList()
        return this.dropLast(1).zip(this.drop(1)) { a, b -> transform(a, b) }
    }
}
