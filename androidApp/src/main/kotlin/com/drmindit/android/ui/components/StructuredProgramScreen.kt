package com.drmindit.android.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drmindit.shared.domain.program.*
import com.drmindit.shared.domain.audience.AudienceType

/**
 * Structured Program Screen
 * Replaces generic AI chat with guided, step-by-step programs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredProgramScreen(
    audience: AudienceType,
    program: StructuredProgram,
    currentStep: ProgramStep,
    onStepComplete: (StepCompletionResult) -> Unit,
    onPauseProgram: () -> Unit,
    onPreviousStep: () -> Unit,
    onNextStep: () -> Unit
) {
    var stepProgress by remember { mutableStateOf(StepProgressState()) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getAudienceColors(audience),
                    startY = 0f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            ProgramHeader(
                program = program,
                currentStep = currentStep,
                onPauseProgram = onPauseProgram
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Step content based on type
                    when (currentStep.type) {
                        StepType.INSTRUCTION -> InstructionContent(currentStep)
                        StepType.EXERCISE -> ExerciseContent(
                            step = currentStep,
                            onComplete = { completed ->
                                stepProgress = stepProgress.copy(exerciseCompleted = completed)
                            }
                        )
                        StepType.REFLECTION -> ReflectionContent(
                            step = currentStep,
                            onComplete = { response ->
                                stepProgress = stepProgress.copy(reflectionCompleted = true, reflectionResponse = response)
                            }
                        )
                        StepType.AUDIO_SESSION -> AudioContent(
                            step = currentStep,
                            onComplete = { completed ->
                                stepProgress = stepProgress.copy(audioCompleted = completed)
                            }
                        )
                        StepType.ASSESSMENT -> AssessmentContent(
                            step = currentStep,
                            onComplete = { result ->
                                stepProgress = stepProgress.copy(assessmentCompleted = true, assessmentResult = result)
                            }
                        )
                        StepType.PRACTICE -> PracticeContent(
                            step = currentStep,
                            onComplete = { completed ->
                                stepProgress = stepProgress.copy(practiceCompleted = completed)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress indicator
            ProgressIndicator(
                program = program,
                currentStep = currentStep
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation buttons
            NavigationButtons(
                canGoPrevious = currentStep.day > 1,
                canGoNext = stepProgress.isStepComplete(currentStep),
                onPrevious = onPreviousStep,
                onNext = {
                    if (stepProgress.isStepComplete(currentStep)) {
                        val completionResult = StepCompletionResult(
                            success = true,
                            isProgramCompleted = false,
                            nextStep = null,
                            timeSpentMinutes = stepProgress.timeSpentMinutes,
                            exerciseCompleted = stepProgress.exerciseCompleted,
                            reflectionCompleted = stepProgress.reflectionCompleted,
                            audioCompleted = stepProgress.audioCompleted,
                            rating = stepProgress.rating,
                            notes = stepProgress.notes
                        )
                        onStepComplete(completionResult)
                    }
                },
                onComplete = {
                    showCompleteDialog = true
                }
            )
        }
    }
    
    // Complete step dialog
    if (showCompleteDialog) {
        CompleteStepDialog(
            step = currentStep,
            onDismiss = { showCompleteDialog = false },
            onComplete = { rating, notes ->
                stepProgress = stepProgress.copy(
                    rating = rating,
                    notes = notes,
                    isStepComplete = true
                )
                showCompleteDialog = false
                
                val completionResult = StepCompletionResult(
                    success = true,
                    isProgramCompleted = false,
                    nextStep = null,
                    timeSpentMinutes = stepProgress.timeSpentMinutes,
                    exerciseCompleted = stepProgress.exerciseCompleted,
                    reflectionCompleted = stepProgress.reflectionCompleted,
                    audioCompleted = stepProgress.audioCompleted,
                    rating = rating,
                    notes = notes
                )
                onStepComplete(completionResult)
            }
        )
    }
}

/**
 * Program header with title and controls
 */
@Composable
private fun ProgramHeader(
    program: StructuredProgram,
    currentStep: ProgramStep,
    onPauseProgram: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPauseProgram,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.2f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Pause",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = program.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Text(
                text = "Day ${currentStep.day} of ${program.totalSteps}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
        
        IconButton(
            onClick = { /* TODO: Show program info */ },
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.2f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Instruction content component
 */
@Composable
private fun InstructionContent(step: ProgramStep) {
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Instructions list
        step.exercise?.let { exercise ->
            if (exercise.steps != null) {
                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                exercise.steps.forEach { instructionStep ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${instructionStep.stepNumber}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(24.dp)
                        )
                        
                        Text(
                            text = instructionStep.instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exercise content component
 */
@Composable
private fun ExerciseContent(
    step: ProgramStep,
    onComplete: (Boolean) -> Unit
) {
    var isCompleted by remember { mutableStateOf(false) }
    var timerStarted by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(step.estimatedDurationMinutes * 60) }
    
    LaunchedEffect(timerStarted, timeRemaining) {
        if (timerStarted && timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
    }
    
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        step.exercise?.let { exercise ->
            // Timer display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTime(timeRemaining),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Time remaining",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exercise instructions
            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!timerStarted) {
                    Button(
                        onClick = { timerStarted = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Exercise")
                    }
                } else {
                    Button(
                        onClick = { timerStarted = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pause")
                    }
                }
                
                if (timeRemaining == 0) {
                    Button(
                        onClick = {
                            isCompleted = true
                            onComplete(true)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981) // Green
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete")
                    }
                }
            }
        }
    }
}

/**
 * Reflection content component
 */
@Composable
private fun ReflectionContent(
    step: ProgramStep,
    onComplete: (String) -> Unit
) {
    var response by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        step.reflection?.let { reflection ->
            Text(
                text = reflection.prompt,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when (reflection.type) {
                ReflectionType.OPEN_ENDED -> {
                    OutlinedTextField(
                        value = response,
                        onValueChange = { response = it },
                        placeholder = { Text("Share your thoughts...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                ReflectionType.RATING_SCALE -> {
                    var rating by remember { mutableStateOf(3) }
                    
                    Text(
                        text = "Rate your response (1-5):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..5).forEach { value ->
                            FilterChip(
                                onClick = { rating = value },
                                label = { Text(value.toString()) },
                                selected = rating == value,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                    
                    response = rating.toString()
                }
                else -> {
                    // Handle other reflection types
                    Text(
                        text = "Reflection type not implemented yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!reflection.isOptional) {
                Button(
                    onClick = {
                        if (response.isNotBlank()) {
                            isCompleted = true
                            onComplete(response)
                        }
                    },
                    enabled = response.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Submit Reflection")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            isCompleted = true
                            onComplete(response.ifBlank { "Skipped" })
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Skip")
                    }
                    
                    Button(
                        onClick = {
                            isCompleted = true
                            onComplete(response.ifBlank { "Skipped" })
                        },
                        enabled = response.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

/**
 * Audio content component
 */
@Composable
private fun AudioContent(
    step: ProgramStep,
    onComplete: (Boolean) -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        step.audioSession?.let { audio ->
            // Audio player UI
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = audio.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "${audio.durationMinutes} min • ${audio.voiceGender.name} voice",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Complete button
            Button(
                onClick = {
                    isCompleted = true
                    onComplete(true)
                },
                enabled = isCompleted || progress >= 0.9f,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981) // Green
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mark as Complete")
            }
        }
    }
}

/**
 * Assessment content component
 */
@Composable
private fun AssessmentContent(
    step: ProgramStep,
    onComplete: (String) -> Unit
) {
    var assessmentResult by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Assessment questions would go here
        Text(
            text = "Assessment questions would be displayed here based on the step configuration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isCompleted = true
                onComplete("Assessment completed")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Complete Assessment")
        }
    }
}

/**
 * Practice content component
 */
@Composable
private fun PracticeContent(
    step: ProgramStep,
    onComplete: (Boolean) -> Unit
) {
    var isCompleted by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Practice content would go here
        Text(
            text = "Practice exercises would be displayed here based on the step configuration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isCompleted = true
                onComplete(true)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Complete Practice")
        }
    }
}

/**
 * Progress indicator
 */
@Composable
private fun ProgressIndicator(
    program: StructuredProgram,
    currentStep: ProgramStep
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${currentStep.day}/${program.totalSteps} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = currentStep.day.toFloat() / program.totalSteps.toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Navigation buttons
 */
@Composable
private fun NavigationButtons(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canGoPrevious) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Previous")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        if (canGoNext) {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981) // Green
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete")
            }
        }
    }
}

/**
 * Complete step dialog
 */
@Composable
private fun CompleteStepDialog(
    step: ProgramStep,
    onDismiss: () -> Unit,
    onComplete: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(3) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Complete ${step.title}",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "How helpful was this step?",
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    (1..5).forEach { value ->
                        FilterChip(
                            onClick = { rating = value },
                            label = { Text(value.toString()) },
                            selected = rating == value,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onComplete(rating, notes) }
            ) {
                Text("Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Helper functions
 */
private fun getAudienceColors(audience: AudienceType): List<Color> {
    return when (audience) {
        AudienceType.STUDENT -> listOf(
            Color(0xFF3B82F6), // Blue
            Color(0xFF1E40AF)  // Dark Blue
        )
        AudienceType.CORPORATE -> listOf(
            Color(0xFF10B981), // Green
            Color(0xFF047857)  // Dark Green
        )
        AudienceType.POLICE_MILITARY -> listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF4338CA)  // Dark Indigo
        )
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

/**
 * Step progress state
 */
private data class StepProgressState(
    val exerciseCompleted: Boolean = false,
    val reflectionCompleted: Boolean = false,
    val reflectionResponse: String = "",
    val audioCompleted: Boolean = false,
    val assessmentCompleted: Boolean = false,
    val assessmentResult: String = "",
    val practiceCompleted: Boolean = false,
    val rating: Int? = null,
    val notes: String = "",
    val timeSpentMinutes: Int = 0
) {
    fun isStepComplete(step: ProgramStep): Boolean {
        return when (step.type) {
            StepType.INSTRUCTION -> true
            StepType.EXERCISE -> exerciseCompleted
            StepType.REFLECTION -> reflectionCompleted || step.reflection?.isOptional == true
            StepType.AUDIO_SESSION -> audioCompleted
            StepType.ASSESSMENT -> assessmentCompleted
            StepType.PRACTICE -> practiceCompleted
        }
    }
}
