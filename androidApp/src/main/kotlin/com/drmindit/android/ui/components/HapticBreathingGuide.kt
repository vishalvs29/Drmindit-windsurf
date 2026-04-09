package com.drmindit.android.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Provides haptic feedback during breathing exercises
 * Makes breathing accessible to deaf/hard-of-hearing users
 */
@Composable
fun HapticBreathingGuide(
    isPlaying: Boolean,
    breathingPhase: String = "Ready"
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    
    // Haptic patterns for different breathing phases
    val hapticPattern = when (breathingPhase) {
        "Inhale" -> longArrayOf(0, 400) // Start + 400ms inhale
        "Hold" -> longArrayOf(0, 1000) // 1 second hold
        "Exhale" -> longArrayOf(0, 400) // 400ms exhale
        "Pause" -> longArrayOf(0, 200) // 200ms pause
        else -> longArrayOf(0, 100) // Default tap
    }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying && vibrator?.hasVibrator() == true) {
            // Start breathing rhythm haptics
            while (true) {
                // Inhale phase
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400), -1))
                delay(400)
                
                // Hold phase
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000), -1))
                delay(1000)
                
                // Exhale phase
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400), -1))
                delay(400)
                
                // Pause phase
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200), -1))
                delay(200)
            }
        }
    }
    
    // Visual indicator
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4FD1C5).copy(alpha = 0.2f),
                        Color(0xFF667EEA).copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = breathingPhase,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Breathing phase indicator with haptic controls
 */
@Composable
fun BreathingPhaseIndicator(
    currentPhase: String,
    onPhaseChange: (String) -> Unit = {}
) {
    val phases = listOf("Ready", "Inhale", "Hold", "Exhale", "Pause")
    val currentPhaseIndex = phases.indexOf(currentPhase).coerceIn(0, phases.size - 1)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        phases.forEachIndexed { index, phase ->
            val isSelected = index == currentPhaseIndex
            val phaseColor = when (phase) {
                "Inhale" -> Color(0xFF4FD1C5)
                "Hold" -> Color(0xFF667EEA)
                "Exhale" -> Color(0xFF4FD1C5)
                else -> Color(0xFFE2E8F0).copy(alpha = 0.5f)
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) phaseColor else Color.Transparent,
                        CircleShape
                    )
                    .size(40.dp)
            ) {
                Text(
                    text = phase.first(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
