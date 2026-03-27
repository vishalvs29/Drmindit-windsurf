package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drmindit.android.player.EnhancedAudioPlayerManager
import com.drmindit.android.ui.viewmodel.GuidedSessionViewModel
import com.drmindit.shared.domain.program.StepType
import com.drmindit.shared.domain.program.FlowStepType
import timber.log.Timber

/**
 * Guided Program Session Screen - Step-by-Step Therapy Experience
 * Replaces chat with structured, controlled therapy flows
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidedProgramSession(
    programId: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: GuidedSessionViewModel = hiltViewModel()
) {
    // Collect state
    val currentDay by viewModel.currentDay.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val flowStep by viewModel.flowStep.collectAsStateWithLifecycle()
    val therapyState by viewModel.therapyState.collectAsStateWithLifecycle()
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    
    // Load session
    LaunchedEffect(programId) {
        Timber.d("🎯 Loading guided program session: $programId")
        viewModel.loadSession(programId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep blue
                        Color(0xFF3B82F6), // Medium blue
                        Color(0xFF60A5FA)  // Light blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with progress
            SessionHeader(
                currentDay = currentDay,
                currentStep = currentStep,
                userProgress = userProgress,
                onBack = onBack
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main content based on flow step
            flowStep?.let { step ->
                when (step.type) {
                    FlowStepType.AI_VALIDATION -> AIValidationContent(step, viewModel)
                    FlowStepType.QUESTION_INQUIRY -> QuestionContent(step, sessionState, viewModel)
                    FlowStepType.GUIDED_EXERCISE -> ExerciseContent(step, sessionState, viewModel)
                    FlowStepType.REFLECTION_PROMPT -> ReflectionContent(step, sessionState, viewModel)
                    FlowStepType.PROGRESS_UPDATE -> ProgressContent(step, viewModel)
                    FlowStepType.FLOW_COMPLETION -> CompletionContent(step, viewModel)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation buttons
            NavigationButtons(
                flowStep = flowStep,
                sessionState = sessionState,
                onPrevious = { viewModel.previousFlowStep() },
                onNext = { viewModel.nextFlowStep() },
                onComplete = onComplete
            )
        }
    }
}

@Composable
private fun SessionHeader(
    currentDay: com.drmindit.shared.domain.program.ProgramDay?,
    currentStep: com.drmindit.shared.domain.program.ProgramStep?,
    userProgress: com.drmindit.shared.domain.program.UserProgramProgress?,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Day ${currentDay?.day ?: userProgress?.currentDay ?: 1}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            userProgress?.let { progress ->
                val progressPercent = (progress.completedDays.size.toFloat() / 7f * 100).toInt()
                Text(
                    text = "$progressPercent% Complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        Box(
            modifier = Modifier.size(48.dp) // Balance the back button
        )
    }
}

@Composable
private fun AIValidationContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    viewModel: GuidedSessionViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI validation icon
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Auto-continue animation
            val progress by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = flowStep.estimatedMinutes * 1000 / 10),
                label = "validation_progress"
            )
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF10B981),
                trackColor = Color.White.copy(alpha = 0.2f),
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Preparing your session...",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuestionContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    sessionState: GuidedSessionState,
    viewModel: GuidedSessionViewModel
) {
    var userResponse by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Question icon
            Icon(
                imageVector = Icons.Default.Help,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // User input
            OutlinedTextField(
                value = userResponse,
                onValueChange = { userResponse = it },
                label = { Text("Your response", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    textColor = Color.White
                ),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit button
            Button(
                onClick = { 
                    viewModel.submitResponse(userResponse)
                },
                enabled = userResponse.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Share Your Thoughts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExerciseContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    sessionState: GuidedSessionState,
    viewModel: GuidedSessionViewModel
) {
    var isExerciseActive by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(flowStep.estimatedMinutes) }
    var exerciseCompleted by remember { mutableStateOf(false) }
    
    LaunchedEffect(isExerciseActive) {
        if (isExerciseActive && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0) {
            isExerciseActive = false
            exerciseCompleted = true
            viewModel.completeExercise(timeRemaining = flowStep.estimatedMinutes)
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise icon
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Guided Exercise",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Exercise instructions
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Exercise Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = flowStep.content,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Exercise timer and controls
            if (!exerciseCompleted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Exercise Timer",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "$timeRemaining:00",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { isExerciseActive = !isExerciseActive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isExerciseActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = if (isExerciseActive) "Pause Exercise" else "Start Exercise"
                            )
                        }
                    }
                }
            } else {
                // Completed state
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Exercise Completed!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Audio session integration
            flowStep.audioSession?.let { audioSession ->
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { viewModel.playAudioSession(audioSession) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text("Play Guided Audio (${audioSession.duration} min)")
                }
            }
        }
    }
}

@Composable
private fun ReflectionContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    sessionState: GuidedSessionState,
    viewModel: GuidedSessionViewModel
) {
    var reflectionText by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Reflection icon
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Reflection Time",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reflection prompt
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Reflection Prompt",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = flowStep.content,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reflection input
            OutlinedTextField(
                value = reflectionText,
                onValueChange = { reflectionText = it },
                label = { Text("Your reflection", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    textColor = Color.White
                ),
                minLines = 4,
                maxLines = 6
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit reflection
            Button(
                onClick = { 
                    viewModel.submitReflection(reflectionText)
                },
                enabled = reflectionText.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Reflection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProgressContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    viewModel: GuidedSessionViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress icon
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Progress Update",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = flowStep.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Continue button
            Button(
                onClick = { viewModel.continueToNextStep() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E3A8A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompletionContent(
    flowStep: com.drmindit.shared.domain.program.FlowStep,
    viewModel: GuidedSessionViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Completion icon with animation
            val animatedScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "completion_scale"
            )
            
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier
                    .size(64.dp)
                    .scale(animatedScale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = flowStep.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Session Complete!",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = flowStep.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Celebration animation placeholder
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = "Great job completing today's session!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationButtons(
    flowStep: com.drmindit.shared.domain.program.FlowStep?,
    sessionState: GuidedSessionState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        OutlinedButton(
            onClick = onPrevious,
            enabled = sessionState.canGoBack,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Previous")
        }
        
        // Next/Complete button
        Button(
            onClick = if (sessionState.isLastStep) onComplete else onNext,
            enabled = sessionState.canProceed,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sessionState.isLastStep) Color(0xFF10B981) else Color.White,
                contentColor = if (sessionState.isLastStep) Color.White else Color(0xFF1E3A8A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (sessionState.isLastStep) "Complete Session" else "Next Step",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = if (sessionState.isLastStep) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Data class for session state
data class GuidedSessionState(
    val canGoBack: Boolean = false,
    val canProceed: Boolean = true,
    val isLastStep: Boolean = false,
    val currentFlowStep: Int = 0,
    val totalFlowSteps: Int = 1,
    val userInput: String? = null,
    val exerciseCompleted: Boolean = false,
    val reflectionSubmitted: Boolean = false
)
