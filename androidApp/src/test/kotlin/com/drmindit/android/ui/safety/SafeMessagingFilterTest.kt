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
        assertTrue(filteredResponse.contains("having thoughts of ending your life", ignoreCase = true))
        assertFalse(filteredResponse.contains("commit suicide", ignoreCase = true))
    }
    
    @Test
    fun `safe phrase passes through unchanged if it already contains hopeful language`() {
        // Given
        val safeResponse = "I'm here to help you feel better and find hope"
        
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
        assertTrue(filteredResponse.contains("recovery", ignoreCase = true))
        assertTrue(filteredResponse.contains("hope", ignoreCase = true))
        assertTrue(filteredResponse.contains("healing", ignoreCase = true))
    }
    
    @Test
    fun `self-references not corrupted`() {
        // Given
        val responseWithSelf = "Let's put an end to your stress and find peace"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(responseWithSelf)
        
        // Then
        assertTrue(filteredResponse.contains("end to your stress", ignoreCase = true))
        assertFalse(filteredResponse.contains("[removed for safety]", ignoreCase = true))
    }
    
    @Test
    fun `empty string handled gracefully`() {
        // Given
        val emptyResponse = ""
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(emptyResponse)
        
        // Then
        assertTrue(filteredResponse.contains("recovery", ignoreCase = true))
        assertTrue(filteredResponse.contains("hope", ignoreCase = true))
    }
    
    @Test
    fun `empathy-first replacements work correctly`() {
        // Given
        val crisisResponse = "You feel like you want to end it all"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(crisisResponse)
        
        // Then
        assertTrue(filteredResponse.contains("feel overwhelmed and hopeless", ignoreCase = true))
        assertFalse(filteredResponse.contains("end it all", ignoreCase = true))
    }
    
    @Test
    fun `multiple harmful phrases all replaced`() {
        // Given
        val multipleHarmful = "I want to kill myself and end my life"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(multipleHarmful)
        
        // Then
        assertTrue(filteredResponse.contains("hurt myself", ignoreCase = true))
        assertTrue(filteredResponse.contains("end your suffering", ignoreCase = true))
        assertFalse(filteredResponse.contains("kill myself", ignoreCase = true))
        assertFalse(filteredResponse.contains("end my life", ignoreCase = true))
    }
    
    @Test
    fun `normal conversation preserved with added hopeful language`() {
        // Given
        val normalResponse = "I understand you're feeling anxious about your work situation"
        
        // When
        val filteredResponse = SafeMessagingFilter.filterResponse(normalResponse)
        
        // Then
        assertTrue(filteredResponse.startsWith(normalResponse))
        assertTrue(filteredResponse.contains("hope", ignoreCase = true))
    }
}
