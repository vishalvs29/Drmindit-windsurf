package com.drmindit.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.drmindit.android.ui.components.*
import com.drmindit.shared.domain.model.PersonalGoal
import com.drmindit.shared.domain.model.StressLevel
import com.drmindit.shared.domain.model.UserType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (OnboardingData) -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) }
    val onboardingData = remember { mutableStateOf(OnboardingData()) }

    val steps = listOf(
        OnboardingStep.WELCOME,
        OnboardingStep.USER_TYPE,
        OnboardingStep.PERSONAL_GOALS,
        OnboardingStep.STRESS_LEVEL,
        OnboardingStep.COMPLETE
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6B73FF).copy(alpha = 0.1f),
                        Color(0xFFB39DDB).copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress Indicator
            ProgressIndicator(
                currentStep = currentStep,
                totalSteps = steps.size,
                modifier = Modifier.padding(16.dp)
            )

            // Skip Button
            if (currentStep < steps.size - 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                }
            }

            // Content
            Box(
                modifier = Modifier.weight(1f)
            ) {
                when (steps[currentStep]) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        onNext = { currentStep++ }
                    )
                    OnboardingStep.USER_TYPE -> UserTypeSelectionStep(
                        selectedType = onboardingData.value.userType,
                        onTypeSelected = { type ->
                            onboardingData.value = onboardingData.value.copy(userType = type)
                            currentStep++
                        },
                        onBack = { if (currentStep > 0) currentStep-- }
                    )
                    OnboardingStep.PERSONAL_GOALS -> PersonalGoalsStep(
                        selectedGoals = onboardingData.value.personalGoals,
                        onGoalsSelected = { goals ->
                            onboardingData.value = onboardingData.value.copy(personalGoals = goals)
                            currentStep++
                        },
                        onBack = { if (currentStep > 0) currentStep-- }
                    )
                    OnboardingStep.STRESS_LEVEL -> StressLevelStep(
                        selectedLevel = onboardingData.value.stressLevel,
                        onLevelSelected = { level ->
                            onboardingData.value = onboardingData.value.copy(stressLevel = level)
                            currentStep++
                        },
                        onBack = { if (currentStep > 0) currentStep-- }
                    )
                    OnboardingStep.COMPLETE -> CompleteStep(
                        onGetStarted = { onComplete(onboardingData.value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (index < totalSteps - 1) {
                        Divider(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 20.dp),
                            color = if (index < currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            thickness = 2.dp
                        )
                    }
                    
                    Card(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (index <= currentStep) 4.dp else 0.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index <= currentStep) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < currentStep) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index <= currentStep) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Illustration
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "DrMindit Logo",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to DrMindit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your personal mental wellness companion for a healthier, more balanced life.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Let's personalize your experience in just a few steps.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(
            text = "Get Started",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UserTypeSelectionStep(
    selectedType: UserType?,
    onTypeSelected: (UserType) -> Unit,
    onBack: () -> Unit
) {
    val userTypes = listOf(
        UserTypeData(UserType.STUDENT, "Student", Icons.Default.School, "For students in schools and colleges"),
        UserTypeData(UserType.CORPORATE_EMPLOYEE, "Corporate", Icons.Default.Business, "For corporate employees"),
        UserTypeData(UserType.GOVERNMENT_EMPLOYEE, "Government", Icons.Default.AccountBalance, "For government employees"),
        UserTypeData(UserType.POLICE_MILITARY, "Police/Military", Icons.Default.Security, "For police and military personnel"),
        UserTypeData(UserType.GENERAL, "General", Icons.Default.Person, "For general users")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This helps us personalize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(userTypes.size) { index ->
                val userType = userTypes[index]
                UserTypeCard(
                    userType = userType,
                    isSelected = selectedType == userType.type,
                    onClick = { onTypeSelected(userType.type) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Continue",
                onClick = { selectedType?.let { onTypeSelected(it) } },
                enabled = selectedType != null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PersonalGoalsStep(
    selectedGoals: List<PersonalGoal>,
    onGoalsSelected: (List<PersonalGoal>) -> Unit,
    onBack: () -> Unit
) {
    val goals = listOf(
        PersonalGoalData(PersonalGoal.STRESS_MANAGEMENT, "Stress Management", Icons.Default.Spa),
        PersonalGoalData(PersonalGoal.ANXIETY_REDUCTION, "Anxiety Reduction", Icons.Default.Psychology),
        PersonalGoalData(PersonalGoal.SLEEP_IMPROVEMENT, "Sleep Better", Icons.Default.Bedtime),
        PersonalGoalData(PersonalGoal.DEPRESSION_SUPPORT, "Depression Support", Icons.Default.Favorite),
        PersonalGoalData(PersonalGoal.FOCUS_PRODUCTIVITY, "Focus & Productivity", Icons.Default.CenterFocusStrong),
        PersonalGoalData(PersonalGoal.MINDFULNESS, "Mindfulness", Icons.Default.SelfImprovement)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "What are your goals?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select all that apply (choose at least one)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(goals.size) { index ->
                val goal = goals[index]
                GoalCard(
                    goal = goal,
                    isSelected = selectedGoals.contains(goal.goal),
                    onClick = { 
                        val newGoals = if (selectedGoals.contains(goal.goal)) {
                            selectedGoals - goal.goal
                        } else {
                            selectedGoals + goal.goal
                        }
                        onGoalsSelected(newGoals)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Continue",
                onClick = { onGoalsSelected(selectedGoals) },
                enabled = selectedGoals.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StressLevelStep(
    selectedLevel: StressLevel?,
    onLevelSelected: (StressLevel) -> Unit,
    onBack: () -> Unit
) {
    val stressLevels = listOf(
        StressLevelData(StressLevel.LOW, "Low", "I'm feeling calm and relaxed", Color(0xFF4CAF50)),
        StressLevelData(StressLevel.MEDIUM, "Medium", "I'm managing but could use support", Color(0xFFFF9800)),
        StressLevelData(StressLevel.HIGH, "High", "I'm feeling quite stressed", Color(0xFFFF5722)),
        StressLevelData(StressLevel.SEVERE, "Severe", "I need immediate help", Color(0xFFF44336))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "How would you describe your current stress level?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This helps us recommend the right content for you",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            stressLevels.forEach { level ->
                StressLevelCard(
                    level = level,
                    isSelected = selectedLevel == level.level,
                    onClick = { onLevelSelected(level.level) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                text = "Back",
                onClick = onBack,
                modifier = Modifier.weight(1f)
            )
            
            PrimaryButton(
                text = "Complete Setup",
                onClick = { selectedLevel?.let { onLevelSelected(it) } },
                enabled = selectedLevel != null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompleteStep(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Card(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your personalized mental wellness journey begins now. We've tailored content specifically for you.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        GradientButton(
            text = "Start Your Journey",
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UserTypeCard(
    userType: UserTypeData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = userType.icon,
                contentDescription = userType.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userType.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = userType.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: PersonalGoalData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = goal.icon,
                contentDescription = goal.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = goal.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StressLevelCard(
    level: StressLevelData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                level.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = level.color
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(level.color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        level.color
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        level.color.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = level.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private enum class OnboardingStep {
    WELCOME,
    USER_TYPE,
    PERSONAL_GOALS,
    STRESS_LEVEL,
    COMPLETE
}

private data class UserTypeData(
    val type: UserType,
    val name: String,
    val icon: ImageVector,
    val description: String
)

private data class PersonalGoalData(
    val goal: PersonalGoal,
    val name: String,
    val icon: ImageVector
)

private data class StressLevelData(
    val level: StressLevel,
    val name: String,
    val description: String,
    val color: Color
)

data class OnboardingData(
    val userType: UserType? = null,
    val personalGoals: List<PersonalGoal> = emptyList(),
    val stressLevel: StressLevel? = null
)
