package com.drmindit.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drmindit.android.ui.components.*
import com.drmindit.android.ui.viewmodel.DashboardViewModel
import com.drmindit.android.ui.viewmodel.CrisisViewModel
import com.drmindit.shared.domain.model.Mood
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    crisisViewModel: CrisisViewModel = viewModel(),
    onSessionClick: (String) -> Unit = {},
    onProgramClick: (String) -> Unit = {},
    onViewAllSessions: () -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val crisisUiState by crisisViewModel.uiState.collectAsState()
    val crisisDetectionResult by crisisViewModel.crisisDetectionResult.collectAsState()

    LaunchedEffect(Unit) {
        dashboardViewModel.refreshData()
    }

    if (crisisUiState.showCrisisModal && crisisDetectionResult != null) {
        CrisisModal(
            isVisible = crisisUiState.showCrisisModal,
            crisisResult = crisisDetectionResult!!,
            helplines = crisisUiState.emergencyHelplines,
            onDismiss = { crisisViewModel.dismissCrisisModal() },
            onCallHelpline = { crisisViewModel.dismissCrisisModal() },
            onStartGrounding = { crisisViewModel.resolveCrisis() },
            onResolve = { crisisViewModel.resolveCrisis() }
        )
    }

    if (crisisUiState.showGroundingExercise) {
        GroundingExerciseScreen(
            onComplete = { crisisViewModel.completeGroundingExercise() },
            onSkip = { crisisViewModel.dismissGroundingExercise() }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                HeaderSection(
                    greeting = getGreeting(),
                    userName = uiState.user?.name ?: "User"
                )

                CrisisDetectionTrigger(crisisViewModel)

                uiState.user?.let {
                    MentalHealthScoreCard(
                        score = calculateMentalHealthScore(it),
                        userName = it.name
                    )
                }

                DailyCheckInSection {
                    crisisViewModel.detectCrisis(it, getMoodScore(it))
                }

                QuickStatsRow(
                    uiState.analytics?.currentStreak ?: 0,
                    uiState.analytics?.totalMindfulMinutes ?: 0
                )

                RecommendedSessionsSection(
                    sessions = uiState.recommendedSessions,
                    onSessionClick = onSessionClick,
                    onViewAllSessions = onViewAllSessions,
                    isLoading = uiState.isLoading
                )

                uiState.analytics?.let {
                    WeeklyInsightsSection(it)
                }

                FocusCard()
                ActiveProgramsSection(onProgramClick)
            }
        }
    }
}

@Composable
private fun HeaderSection(greeting: String, userName: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(greeting, style = MaterialTheme.typography.headlineMedium)
            Text(userName)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.Notifications, null)
        }
    }
}

@Composable
private fun MentalHealthScoreCard(score: Float, userName: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Mental Health Score")
            Text("${(score * 100).toInt()}%")
        }
    }
}

@Composable
private fun DailyCheckInSection(onMoodSelected: (Mood) -> Unit) {
    Column {
        Text("How are you feeling?")
        LazyRow {
            items(Mood.values()) {
                Button(onClick = { onMoodSelected(it) }) {
                    Text(it.name)
                }
            }
        }
    }
}

@Composable
private fun RecommendedSessionsSection(
    sessions: List<com.drmindit.shared.domain.model.Session>,
    onSessionClick: (String) -> Unit,
    onViewAllSessions: () -> Unit,
    isLoading: Boolean
) {
    Column {
        Text("Recommended")
        sessions.take(3).forEach {
            Text(it.title)
        }
    }
}

@Composable
private fun WeeklyInsightsSection(analytics: com.drmindit.shared.domain.model.UserAnalytics) {
    Text("Weekly Insights")
}

@Composable
private fun FocusCard() {
    Text("Focus Card")
}

@Composable
private fun ActiveProgramsSection(onProgramClick: (String) -> Unit) {
    Text("Programs")
}

private fun getGreeting(): String {
    val hour = LocalDateTime.now().hour
    return if (hour < 12) "Good morning" else "Good evening"
}

private fun calculateMentalHealthScore(user: com.drmindit.shared.domain.model.User): Float {
    return 0.7f
}

private fun getMoodScore(mood: Mood): Int = 5