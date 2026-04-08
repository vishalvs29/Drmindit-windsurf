package com.drmindit.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drmindit.android.ui.screens.*

/**
 * Main navigation setup for the DrMindit app
 */
@Composable
fun DrMinditNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToSession = { sessionId ->
                    navController.navigate("session/$sessionId")
                },
                onNavigateToExplore = {
                    navController.navigate("explore")
                },
                onNavigateToPlayer = {
                    navController.navigate("player")
                },
                onNavigateToProgress = {
                    navController.navigate("progress")
                },
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                }
            )
        }
        
        composable("explore") {
            ExploreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSession = { sessionId ->
                    navController.navigate("session/$sessionId")
                },
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }
        
        composable("player") {
            SessionPlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        
        composable("progress") {
            ProgressScreen(
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                },
                onNavigateToSession = { sessionId ->
                    navController.navigate("session/$sessionId")
                }
            )
        }
        
        composable("analytics") {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProgress = {
                    navController.navigate("progress")
                }
            )
        }
        
        composable("session/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionDetailScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = {
                    navController.navigate("player")
                }
            )
        }
    }
}
