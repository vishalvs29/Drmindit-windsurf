package com.drmindit.android.domain.crisis

import com.drmindit.android.domain.model.CrisisLevel
import com.drmindit.android.domain.model.CrisisAlert

/**
 * Detects crisis situations based on user input and behavior patterns.
 * Implements various heuristics to identify potential mental health crises.
 */
class CrisisDetector {
    
    private val crisisKeywords = listOf(
        "suicide", "kill myself", "end my life", "want to die",
        "hurt myself", "self harm", "no reason to live",
        "better off dead", "can't go on", "giving up",
        "hopeless", "worthless", "burden"
    )
    
    private val highRiskKeywords = listOf(
        "plan", "method", "when", "how", "going to"
    )
    
    /**
     * Analyzes text for crisis indicators
     */
    fun analyzeText(text: String): CrisisAlert {
        val lowercaseText = text.lowercase()
        
        // Find all detected keywords
        val detectedKeywords = crisisKeywords.filter { keyword ->
            lowercaseText.contains(keyword)
        }
        
        // Check for high-risk indicators
        val hasHighRiskIndicators = highRiskKeywords.any { keyword ->
            lowercaseText.contains(keyword)
        } && detectedKeywords.isNotEmpty()
        
        val crisisLevel = when {
            hasHighRiskIndicators -> CrisisLevel.IMMEDIATE
            detectedKeywords.isNotEmpty() -> CrisisLevel.HIGH
            else -> CrisisLevel.NONE
        }
        
        return CrisisAlert(
            level = crisisLevel,
            detectedText = text,
            timestamp = System.currentTimeMillis(),
            requiresImmediateAction = crisisLevel == CrisisLevel.IMMEDIATE,
            detectedKeywords = detectedKeywords,
            riskFactors = detectedKeywords.take(3) // Top 3 risk factors
        )
    }
    
    /**
     * Analyzes behavioral patterns for crisis indicators
     */
    fun analyzeBehaviorPattern(
        sessionFrequency: Float,
        moodTrend: List<Float>,
        engagementScore: Float
    ): CrisisAlert {
        val avgMood = moodTrend.average()
        val moodDecline = if (moodTrend.size >= 7) {
            moodTrend.takeLast(7).average() < moodTrend.take(7).average() - 1.0f
        } else false
        
        val riskFactors = mutableListOf<String>()
        
        if (sessionFrequency < 0.3f) riskFactors.add("low_engagement")
        if (avgMood < 3.0f) riskFactors.add("low_mood")
        if (moodDecline) riskFactors.add("declining_mood")
        if (engagementScore < 0.4f) riskFactors.add("poor_engagement")
        
        val crisisLevel = when {
            riskFactors.size >= 3 -> CrisisLevel.HIGH
            riskFactors.size >= 2 -> CrisisLevel.MEDIUM
            riskFactors.size >= 1 -> CrisisLevel.LOW
            else -> CrisisLevel.NONE
        }
        
        return CrisisAlert(
            level = crisisLevel,
            detectedText = "Behavioral pattern analysis",
            timestamp = System.currentTimeMillis(),
            requiresImmediateAction = crisisLevel == CrisisLevel.IMMEDIATE,
            riskFactors = riskFactors
        )
    }
    
    /**
     * Combines multiple analysis results for comprehensive assessment
     */
    fun comprehensiveAnalysis(
        textAnalysis: CrisisAlert,
        behaviorAnalysis: CrisisAlert
    ): CrisisAlert {
        val highestLevel = maxOf(textAnalysis.level, behaviorAnalysis.level)
        val combinedRiskFactors = mutableListOf<String>()
        
        textAnalysis.riskFactors?.let { combinedRiskFactors.addAll(it) }
        behaviorAnalysis.riskFactors?.let { combinedRiskFactors.addAll(it) }
        
        return CrisisAlert(
            level = highestLevel,
            detectedText = "${textAnalysis.detectedText} | ${behaviorAnalysis.detectedText}",
            timestamp = System.currentTimeMillis(),
            requiresImmediateAction = highestLevel == CrisisLevel.IMMEDIATE,
            riskFactors = combinedRiskFactors
        )
    }
}
