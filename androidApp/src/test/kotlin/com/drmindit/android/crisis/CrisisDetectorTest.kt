package com.drmindit.android.crisis

import com.drmindit.android.domain.crisis.CrisisDetector
import com.drmindit.android.domain.model.CrisisLevel
import com.drmindit.android.domain.model.CrisisAlert
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
    fun `analyzeText with suicidal keywords returns immediate crisis`() {
        // Given
        val message = "I feel suicidal and want to end my life"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.IMMEDIATE, alert.level)
        assertTrue(alert.requiresImmediateAction)
    }
    
    @Test
    fun `analyzeText with hopeless keyword returns high crisis`() {
        // Given
        val message = "I feel hopeless and there's no point"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.HIGH, alert.level)
        assertFalse(alert.requiresImmediateAction)
        assertTrue(alert.detectedKeywords.contains("hopeless"))
    }
    
    @Test
    fun `analyzeText with low mood returns medium crisis`() {
        // Given
        val message = "I'm having a bad day"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.MEDIUM, alert.level)
        assertFalse(alert.requiresImmediateAction)
    }
    
    @Test
    fun `analyzeText with normal mood returns low crisis`() {
        // Given
        val message = "I'm feeling okay today"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.LOW, alert.level)
        assertFalse(alert.requiresImmediateAction)
    }
    
    @Test
    fun `analyzeText with very low mood and crisis keywords returns immediate crisis`() {
        // Given
        val message = "I want to die"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.IMMEDIATE, alert.level)
        assertTrue(alert.requiresImmediateAction)
    }
    
    @Test
    fun `analyzeText with critical alert sets immediate action`() {
        // Given
        val message = "I want to kill myself"
        
        // When
        val alert = crisisDetector.analyzeText(message)
        
        // Then
        assertEquals(CrisisLevel.IMMEDIATE, alert.level)
        assertTrue(alert.requiresImmediateAction)
        assertTrue(alert.riskFactors?.contains("suicidal") == true)
    }
}
