package com.drmindit.android.domain.schedule

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Manages adaptive session recommendations based on time of day
 */
class SessionScheduler {
    
    fun getRecommendedSessionDuration(): Int {
        val currentTime = LocalTime.now()
        val hour = currentTime.hour
        
        return when (hour) {
            // Morning (6AM-10AM): Users are rushing, recommend shorter sessions
            in 6..10 -> 5 // 5 minutes
            
            // Work hours (10AM-6PM): Users have limited time, recommend standard sessions
            in 11..17 -> 10 // 10 minutes
            
            // Evening (7PM-11PM): Users are winding down, recommend shorter sessions
            in 18..22 -> 5 // 5 minutes
            
            // Late night (11PM-5AM): Users preparing for sleep, recommend shortest sessions
            else -> 3 // 3 minutes
        }
    }
    
    fun getSessionType(): String {
        val currentTime = LocalTime.now()
        val hour = currentTime.hour
        
        return when (hour) {
            in 6..10 -> "Morning Focus"
            in 11..17 -> "Work Break"
            in 18..22 -> "Evening Relaxation"
            else -> "Night Meditation"
        }
    }
    
    fun getGreetingMessage(): String {
        val currentTime = LocalTime.now()
        val hour = currentTime.hour
        
        return when (hour) {
            in 5..11 -> "Good morning! Ready to focus?"
            in 12..17 -> "Need a break? Let's recharge."
            in 18..22 -> "Evening! Time to unwind."
            else -> "Late night! Ready for rest?"
        }
    }
}
