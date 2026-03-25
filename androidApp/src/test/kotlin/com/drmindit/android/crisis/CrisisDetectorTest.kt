package com.drmindit.android.crisis

import com.drmindit.shared.domain.model.MoodCategory
import com.drmindit.shared.domain.model.RiskLevel
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CrisisDetectorTest {
    
    private lateinit var crisisDetector: CrisisDetector
    
    @Before
    fun setup() {
        crisisDetector = CrisisDetector()
    }
    
    @Test
    fun `analyzeMessage with suicidal keywords returns critical risk`() {
        // Given
        val message = "I feel suicidal and want to end my life"
        val mood = null
        
        // When
        val assessment = crisisDetector.analyzeMessage(message, mood)
        
        // Then
        assertEquals(RiskLevel.CRITICAL, assessment.riskLevel)
        assertTrue(assessment.hasCrisisKeywords)
        assertFalse(assessment.isLowMood)
        assertTrue(assessment.detectedKeywords.isNotEmpty())
        assertTrue(assessment.emergencyHelplines.isNotEmpty())
    }
    
    @Test
    fun `analyzeMessage with hopeless keyword returns high risk`() {
        // Given
        val message = "I feel hopeless and there's no point"
        val mood = null
        
        // When
        val assessment = crisisDetector.analyzeMessage(message, mood)
        
        // Then
        assertEquals(RiskLevel.HIGH, assessment.riskLevel)
        assertTrue(assessment.hasCrisisKeywords)
        assertFalse(assessment.isLowMood)
        assertTrue(assessment.detectedKeywords.contains("hopeless"))
    }
    
    @Test
    fun `analyzeMessage with low mood returns medium risk`() {
        // Given
        val message = "I'm having a bad day"
        val mood = MoodCategory.LOW
        
        // When
        val assessment = crisisDetector.analyzeMessage(message, mood)
        
        // Then
        assertEquals(RiskLevel.MEDIUM, assessment.riskLevel)
        assertFalse(assessment.hasCrisisKeywords)
        assertTrue(assessment.isLowMood)
    }
    
    @Test
    fun `analyzeMessage with normal mood returns low risk`() {
        // Given
        val message = "I'm feeling okay today"
        val mood = MoodCategory.NEUTRAL
        
        // When
        val assessment = crisisDetector.analyzeMessage(message, mood)
        
        // Then
        assertEquals(RiskLevel.LOW, assessment.riskLevel)
        assertFalse(assessment.hasCrisisKeywords)
        assertFalse(assessment.isLowMood)
        assertTrue(assessment.emergencyHelplines.isEmpty())
    }
    
    @Test
    fun `analyzeMessage with very low mood and crisis keywords returns critical risk`() {
        // Given
        val message = "I want to die"
        val mood = MoodCategory.VERY_LOW
        
        // When
        val assessment = crisisDetector.analyzeMessage(message, mood)
        
        // Then
        assertEquals(RiskLevel.CRITICAL, assessment.riskLevel)
        assertTrue(assessment.hasCrisisKeywords)
        assertTrue(assessment.isLowMood)
    }
    
    @Test
    fun `updateCrisisState with critical assessment sets critical state`() {
        // Given
        val assessment = CrisisAssessment(
            riskLevel = RiskLevel.CRITICAL,
            hasCrisisKeywords = true,
            isLowMood = false,
            detectedKeywords = listOf("suicidal"),
            emergencyHelplines = crisisDetector.getEmergencyHelplines()
        )
        
        // When
        crisisDetector.updateCrisisState(assessment)
        
        // Then
        val currentState = crisisDetector.getCurrentCrisisState()
        assertTrue(currentState is CrisisState.Critical)
    }
    
    @Test
    fun `updateCrisisState with high risk assessment sets high risk state`() {
        // Given
        val assessment = CrisisAssessment(
            riskLevel = RiskLevel.HIGH,
            hasCrisisKeywords = true,
            isLowMood = false,
            detectedKeywords = listOf("hopeless"),
            emergencyHelplines = crisisDetector.getEmergencyHelplines()
        )
        
        // When
        crisisDetector.updateCrisisState(assessment)
        
        // Then
        val currentState = crisisDetector.getCurrentCrisisState()
        assertTrue(currentState is CrisisState.HighRisk)
    }
    
    @Test
    fun `resetCrisisState returns to normal state`() {
        // Given
        val assessment = CrisisAssessment(
            riskLevel = RiskLevel.HIGH,
            hasCrisisKeywords = true,
            isLowMood = false,
            detectedKeywords = listOf("hopeless"),
            emergencyHelplines = crisisDetector.getEmergencyHelplines()
        )
        crisisDetector.updateCrisisState(assessment)
        
        // When
        crisisDetector.resetCrisisState()
        
        // Then
        val currentState = crisisDetector.getCurrentCrisisState()
        assertEquals(CrisisState.Normal, currentState)
    }
    
    @Test
    fun `getEmergencyHelplines returns Indian helplines`() {
        // When
        val helplines = crisisDetector.getEmergencyHelplines()
        
        // Then
        assertTrue(helplines.isNotEmpty())
        assertTrue(helplines.any { it.name.contains("iCall", ignoreCase = true) })
        assertTrue(helplines.any { it.name.contains("Vandrevala", ignoreCase = true) })
        assertTrue(helplines.all { it.phone.isNotBlank() })
        assertTrue(helplines.all { it.availableHours.isNotBlank() })
    }
}
