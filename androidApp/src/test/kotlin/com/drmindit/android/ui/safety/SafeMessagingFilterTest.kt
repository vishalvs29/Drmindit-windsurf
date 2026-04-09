package com.drmindit.android.ui.safety

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Safety-critical tests for SafeMessagingFilter
 * Ensures all AI responses follow WHO/AFSP safe messaging guidelines
 */
class SafeMessagingFilterTest {
    
    @Test
    fun `harmful phrase replaced with safe alternative`() {
        // Given
        val harmfulResponse = "I understand you want to commit suicide"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(harmfulResponse)
        
        // Then
        assertTrue(filteredResponse.contains("having thoughts of ending your life"))
        assertFalse(filteredResponse.contains("commit suicide"))
    }
    
    @Test
    fun `safe phrase passes through unchanged`() {
        // Given
        val safeResponse = "I'm here to help you feel better"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(safeResponse)
        
        // Then
        assertEquals(safeResponse, filteredResponse)
    }
    
    @Test
    fun `hopeful language added to response`() {
        // Given
        val neutralResponse = "That sounds difficult"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(neutralResponse)
        
        // Then
        assertTrue(filteredResponse.contains("recovery"))
        assertTrue(filteredResponse.contains("hope"))
        assertTrue(filteredResponse.contains("growth"))
    }
    
    @Test
    fun `self-references not corrupted`() {
        // Given
        val responseWithSelf = "Let's put an end to your stress"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(responseWithSelf)
        
        // Then
        assertTrue(filteredResponse.contains("end to your stress"))
        assertFalse(filteredResponse.contains("[removed for safety]"))
    }
    
    @Test
    fun `empty string handled gracefully`() {
        // Given
        val emptyResponse = ""
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(emptyResponse)
        
        // Then
        assertTrue(filteredResponse.contains("recovery"))
        assertTrue(filteredResponse.contains("hope"))
    }
    
    @Test
    fun `empathy-first replacements work correctly`() {
        // Given
        val crisisResponse = "You feel like you want to end it all"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(crisisResponse)
        
        // Then
        assertTrue(filteredResponse.contains("feel overwhelmed and hopeless"))
        assertFalse(filteredResponse.contains("end it all"))
    }
    
    @Test
    fun `multiple harmful phrases all replaced`() {
        // Given
        val multipleHarmful = "I want to kill myself and end my life"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(multipleHarmful)
        
        // Then
        assertTrue(filteredResponse.contains("hurt myself"))
        assertTrue(filteredResponse.contains("end your suffering"))
        assertFalse(filteredResponse.contains("kill myself"))
        assertFalse(filteredResponse.contains("end my life"))
    }
    
    @Test
    fun `normal conversation preserved`() {
        // Given
        val normalResponse = "I understand you're feeling anxious about your work situation"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(normalResponse)
        
        // Then
        assertEquals(normalResponse, filteredResponse)
    }
}
