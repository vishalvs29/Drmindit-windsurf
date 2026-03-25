package com.drmindit.android.crisis

import com.drmindit.shared.domain.model.MoodCategory
import com.drmindit.shared.domain.model.RiskLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Crisis detection system for user safety
 * Monitors mood scores and message content for crisis indicators
 */
class CrisisDetector {
    
    private val _crisisState = MutableStateFlow<CrisisState>(CrisisState.Normal)
    val crisisState: Flow<CrisisState> = _crisisState.asStateFlow()
    
    // Crisis trigger keywords
    private val crisisKeywords = setOf(
        "suicidal", "suicide", "kill myself", "end my life", "want to die",
        "hopeless", "no point", "better off dead", "can't go on",
        "ending it", "goodbye forever", "no reason to live"
    )
    
    // Emergency helplines for India
    private val emergencyHelplines = listOf(
        EmergencyHelpline(
            name = "iCall",
            phone = "9152987821",
            description = "24/7 mental health helpline",
            availableHours = "24/7"
        ),
        EmergencyHelpline(
            name = "Vandrevala Foundation",
            phone = "18602662345",
            description = "Mental health support helpline",
            availableHours = "24/7"
        ),
        EmergencyHelpline(
            name = "Snehi",
            phone = "919822062402",
            description = "Emotional support helpline",
            availableHours="24/7"
        )
    )
    
    /**
     * Analyzes message for crisis indicators
     */
    fun analyzeMessage(message: String, currentMood: MoodCategory? = null): CrisisAssessment {
        val lowerMessage = message.lowercase()
        
        // Check for crisis keywords
        val hasCrisisKeywords = crisisKeywords.any { keyword ->
            lowerMessage.contains(keyword)
        }
        
        // Check mood score
        val isLowMood = currentMood?.let { mood ->
            mood == MoodCategory.VERY_LOW || mood == MoodCategory.LOW
        } ?: false
        
        val riskLevel = when {
            hasCrisisKeywords && isLowMood -> RiskLevel.CRITICAL
            hasCrisisKeywords -> RiskLevel.HIGH
            isLowMood -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        
        return CrisisAssessment(
            riskLevel = riskLevel,
            hasCrisisKeywords = hasCrisisKeywords,
            isLowMood = isLowMood,
            detectedKeywords = crisisKeywords.filter { lowerMessage.contains(it) },
            emergencyHelplines = if (riskLevel >= RiskLevel.HIGH) emergencyHelplines else emptyList()
        )
    }
    
    /**
     * Updates crisis state based on assessment
     */
    fun updateCrisisState(assessment: CrisisAssessment) {
        val newState = when (assessment.riskLevel) {
            RiskLevel.CRITICAL -> CrisisState.Critical(
                message = "You are not alone. Help is available right now.",
                emergencyHelplines = assessment.emergencyHelplines,
                requiresImmediateAction = true
            )
            RiskLevel.HIGH -> CrisisState.HighRisk(
                message = "We're concerned about you. Support is available.",
                emergencyHelplines = assessment.emergencyHelplines,
                requiresImmediateAction = false
            )
            RiskLevel.MEDIUM -> CrisisState.MediumRisk(
                message = "It looks like you're going through a tough time. We're here to help."
            )
            else -> CrisisState.Normal
        }
        
        _crisisState.value = newState
    }
    
    /**
     * Get current crisis state
     */
    fun getCurrentCrisisState(): CrisisState {
        return _crisisState.value
    }
    
    /**
     * Reset crisis state to normal
     */
    fun resetCrisisState() {
        _crisisState.value = CrisisState.Normal
    }
    
    /**
     * Get emergency helplines
     */
    fun getEmergencyHelplines(): List<EmergencyHelpline> {
        return emergencyHelplines
    }
}

/**
 * Crisis assessment result
 */
data class CrisisAssessment(
    val riskLevel: RiskLevel,
    val hasCrisisKeywords: Boolean,
    val isLowMood: Boolean,
    val detectedKeywords: List<String>,
    val emergencyHelplines: List<EmergencyHelpline>
)

/**
 * Crisis state for UI
 */
sealed class CrisisState {
    object Normal : CrisisState()
    
    data class MediumRisk(
        val message: String
    ) : CrisisState()
    
    data class HighRisk(
        val message: String,
        val emergencyHelplines: List<EmergencyHelpline>,
        val requiresImmediateAction: Boolean
    ) : CrisisState()
    
    data class Critical(
        val message: String,
        val emergencyHelplines: List<EmergencyHelpline>,
        val requiresImmediateAction: Boolean
    ) : CrisisState()
}

/**
 * Emergency helpline information
 */
data class EmergencyHelpline(
    val name: String,
    val phone: String,
    val description: String,
    val availableHours: String
)
