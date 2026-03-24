package com.drmindit.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drmindit.android.ui.screens.DashboardScreen
import com.drmindit.android.ui.screens.SessionPlayerScreen
import com.drmindit.android.ui.screens.LibraryScreen
import com.drmindit.android.ui.screens.ProgramDetailScreen
import com.drmindit.android.ui.screens.OnboardingScreen
import com.drmindit.android.ui.screens.OrganizationDashboardScreen
import com.drmindit.android.ui.screens.OnboardingScreen.OnboardingData

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding"
) {
    NavHost(
        navController = navController,
        startRoute = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { onboardingData ->
                    // Save onboarding data and navigate to dashboard
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onSessionClick = { sessionId ->
                    navController.navigate("session_player/$sessionId")
                },
                onMoodSelected = { mood ->
                    // Handle mood selection
                },
                onProgramClick = { programId ->
                    navController.navigate("program_detail/$programId")
                },
                onViewAllSessions = {
                    navController.navigate("library")
                }
            )
        }
        
        composable("session_player/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionPlayerScreen(
                sessionTitle = sessionId,
                onBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }
        
        composable("library") {
            LibraryScreen(
                onSessionClick = { sessionId ->
                    navController.navigate("session_player/$sessionId")
                },
                onCategoryClick = { category ->
                    // Handle category selection
                },
                onSearch = { query ->
                    // Handle search
                }
            )
        }
        
        composable("program_detail/{programId}") { backStackEntry ->
            val programId = backStackEntry.arguments?.getString("programId") ?: ""
            ProgramDetailScreen(
                programTitle = programId,
                onBack = { navController.popBackStack() },
                onDayClick = { day ->
                    navController.navigate("session_player/$programId-day-$day")
                },
                onStartProgram = {
                    // Handle program start
                }
            )
        }
        
        composable("organization_dashboard") {
            OrganizationDashboardScreen(
                onBack = { navController.popBackStack() },
                onDepartmentClick = { department ->
                    // Handle department click
                },
                onExportReport = {
                    // Handle report export
                }
            )
        }
    }
}

// Navigation routes sealed class for type safety
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object SessionPlayer : Screen("session_player/{sessionId}") {
        fun createRoute(sessionId: String) = "session_player/$sessionId"
    }
    object Library : Screen("library")
    object ProgramDetail : Screen("program_detail/{programId}") {
        fun createRoute(programId: String) = "program_detail/$programId"
    }
    object OrganizationDashboard : Screen("organization_dashboard")
}
