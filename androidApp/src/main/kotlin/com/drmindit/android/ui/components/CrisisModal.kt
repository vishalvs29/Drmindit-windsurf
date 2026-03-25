package com.drmindit.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.android.crisis.CrisisState
import com.drmindit.android.crisis.EmergencyHelpline

/**
 * Full-screen crisis modal for emergency situations
 * Non-dismissible for critical situations
 */
@Composable
fun CrisisModal(
    crisisState: CrisisState,
    onDismiss: () -> Unit,
    onCallHelpline: (String) -> Unit,
    onGroundingSession: () -> Unit
) {
    when (crisisState) {
        is CrisisState.Critical -> {
            CriticalCrisisModal(
                crisisState = crisisState,
                onCallHelpline = onCallHelpline,
                onGroundingSession = onGroundingSession
            )
        }
        is CrisisState.HighRisk -> {
            HighRiskCrisisModal(
                crisisState = crisisState,
                onDismiss = onDismiss,
                onCallHelpline = onCallHelpline,
                onGroundingSession = onGroundingSession
            )
        }
        is CrisisState.MediumRisk -> {
            MediumRiskCrisisModal(
                crisisState = crisisState,
                onDismiss = onDismiss,
                onGroundingSession = onGroundingSession
            )
        }
        is CrisisState.Normal -> {
            // Don't show modal for normal state
        }
    }
}

@Composable
private fun CriticalCrisisModal(
    crisisState: CrisisState.Critical,
    onCallHelpline: (String) -> Unit,
    onGroundingSession: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE53935), // Red
                        Color(0xFFD32F2F)  // Dark Red
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Warning Icon
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Emergency",
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Message
            Text(
                text = crisisState.message,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Please reach out for help immediately. You are not alone.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Emergency Helplines
            crisisState.emergencyHelplines.forEach { helpline ->
                HelplineCard(
                    helpline = helpline,
                    onCall = onCallHelpline,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Grounding Session Button
            Button(
                onClick = onGroundingSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Grounding Session",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HighRiskCrisisModal(
    crisisState: CrisisState.HighRisk,
    onDismiss: () -> Unit,
    onCallHelpline: (String) -> Unit,
    onGroundingSession: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (!crisisState.requiresImmediateAction) onDismiss else {},
        title = {
            Text(
                text = "Support Available",
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = crisisState.message,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                crisisState.emergencyHelplines.forEach { helpline ->
                    HelplineCard(
                        helpline = helpline,
                        onCall = onCallHelpline,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onGroundingSession) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Grounding")
            }
        },
        dismissButton = if (!crisisState.requiresImmediateAction) {
            {
                TextButton(onClick = onDismiss) {
                    Text("I'm Okay")
                }
            }
        } else null,
        containerColor = Color.White,
        titleContentColor = Color(0xFFD32F2F)
    )
}

@Composable
private fun MediumRiskCrisisModal(
    crisisState: CrisisState.MediumRisk,
    onDismiss: () -> Unit,
    onGroundingSession: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "We're Here for You",
                color = Color(0xFFFB8C00),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = crisisState.message,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onGroundingSession) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get Support")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue Chat")
            }
        },
        containerColor = Color.White,
        titleContentColor = Color(0xFFFB8C00)
    )
}

@Composable
private fun HelplineCard(
    helpline: EmergencyHelpline,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = helpline.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = helpline.description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Available: ${helpline.availableHours}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Button(
                onClick = { onCall(helpline.phone) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(helpline.phone)
            }
        }
    }
}
