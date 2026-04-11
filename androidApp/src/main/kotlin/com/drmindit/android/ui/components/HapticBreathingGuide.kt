package com.drmindit.android.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Haptic breathing guide (SAFE + production-ready)
 */
@Composable
fun HapticBreathingGuide(
    isPlaying: Boolean,
    breathingPhase: String = "Ready"
) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // Safe vibration function
    fun vibrate(duration: Long) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    LaunchedEffect(isPlaying, breathingPhase) {
        if (!isPlaying) return@LaunchedEffect

        when (breathingPhase) {
            "Inhale" -> vibrate(400)
            "Hold" -> vibrate(800)
            "Exhale" -> vibrate(400)
            "Pause" -> vibrate(200)
        }
    }

    // UI
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
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
 * Breathing phase selector (FIXED)
 */
@Composable
fun BreathingPhaseIndicator(
    currentPhase: String,
    onPhaseChange: (String) -> Unit = {}
) {
    val phases = listOf("Ready", "Inhale", "Hold", "Exhale", "Pause")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        phases.forEach { phase ->

            val isSelected = phase == currentPhase

            val phaseColor = when (phase) {
                "Inhale" -> Color(0xFF4FD1C5)
                "Hold" -> Color(0xFF667EEA)
                "Exhale" -> Color(0xFF4FD1C5)
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) phaseColor else Color.Transparent)
                    .clickable { onPhaseChange(phase) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phase.first().toString(),
                    color = if (isSelected) Color.White else Color.LightGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}