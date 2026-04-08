package com.drmindit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.drmindit.android.ui.navigation.DrMinditBottomNavigation
import com.drmindit.android.ui.screens.*
import com.drmindit.android.ui.theme.DrMinditTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrMinditTheme {
                DrMinditApp()
            }
        }
    }
}

@Composable
fun DrMinditApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Background gradient for entire app
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1C2C), // Deep navy
            Color(0xFF1E3A5F), // Mid blue
            Color(0xFF2D5A7B), // Lighter blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
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
                    
                    composable(
                        "session/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { backStackEntry ->
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
            
            // Bottom navigation (only show on main screens)
            if (currentRoute in listOf("home", "explore", "player", "progress", "analytics")) {
                DrMinditBottomNavigation(
                    navController = navController,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
