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
    
    // Background gradient for the entire app
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
                        HomeScreen()
                    }
                    
                    composable("explore") {
                        ExploreScreen()
                    }
                    
                    composable("player") {
                        SessionPlayerScreen()
                    }
                    
                    composable("progress") {
                        ProgressScreen()
                    }
                    
                    composable("analytics") {
                        AnalyticsScreen()
                    }
                    
                    composable(
                        "session/{sessionId}",
                        arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                        SessionDetailScreen(sessionId = sessionId)
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

@Composable
fun SessionDetailScreen(sessionId: String) {
    // This would be a detailed session screen
    // For now, we'll show a placeholder
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Session Detail",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFE2E8F0)
        )
        
        Text(
            text = "Session ID: $sessionId",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFE2E8F0)
        )
    }
}
