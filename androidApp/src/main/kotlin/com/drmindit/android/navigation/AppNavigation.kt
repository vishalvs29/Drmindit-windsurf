package com.drmindit.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.drmindit.android.ui.screens.DashboardScreen
import com.drmindit.android.ui.screens.SessionPlayerScreen
import com.drmindit.android.ui.screens.LibraryScreen
import com.drmindit.android.ui.screens.ProgramDetailScreen
import com.drmindit.android.ui.screens.OnboardingScreen
import com.drmindit.android.ui.screens.HomeScreen
import com.drmindit.android.ui.screens.ChatScreen
import com.drmindit.android.ui.screens.ProfileScreen
import com.drmindit.android.ui.screens.OrganizationDashboardScreen
import com.drmindit.android.ui.screens.OnboardingScreen.OnboardingData
import com.drmindit.android.ui.components.OfflineBanner
import com.drmindit.android.data.preferences.ThemePreferences
import androidx.hilt.android.AndroidEntryPoint
import androidx.hilt.android.HiltAndroidApp
import dagger.hilt.android.HiltAndroidApp

// Data class for session information
data class SessionData(
    val title: String,
    val instructor: String,
    val duration: Int,
    val audioUrl: String
)

// Bottom navigation items
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Chat : BottomNavItem("chat", "Chat", Icons.Default.Chat)
    object Library : BottomNavItem("library", "Library", Icons.Default.LibraryBooks)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Chat,
        BottomNavItem.Library,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding",
    themePreferences: ThemePreferences
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            // Show offline banner at the top
            if (currentRoute != "onboarding") {
                OfflineBanner()
            }
        },
        bottomBar = {
            // Show bottom bar only for main app screens (not onboarding)
            if (currentRoute != "onboarding" && 
                currentRoute != "session_player/{sessionId}" &&
                currentRoute != "program_detail/{programId}" &&
                currentRoute != "organization_dashboard") {
                BottomNavigationBar(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onComplete = { onboardingData ->
                        // Save onboarding data and navigate to home
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    onSkip = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            
            composable("home") {
                HomeScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate("session_player/$sessionId")
                    },
                    onChatClick = {
                        navController.navigate("chat")
                    },
                    onLibraryClick = {
                        navController.navigate("library")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    }
                )
            }
            
            composable("chat") {
                ChatScreen(
                    onBack = { navController.popBackStack() }
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
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
                
                // Load session details from repository
                LaunchedEffect(sessionId) {
                    // In a real implementation, this would load from Supabase/Room
                    // For now, we'll use the SessionPlayerScreen with the sessionId
                    // The screen itself should handle loading the session data
                }
                
                SessionPlayerScreen(
                    sessionId = sessionId,
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
            
            composable("profile") {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onEditProfile = { /* Navigate to edit profile */ },
                    onSettings = { /* Navigate to settings */ },
                    onHelp = { /* Navigate to help */ },
                    onAbout = { /* Navigate to about */ },
                    onNotificationSettings = { /* Navigate to notification settings */ },
                    onLogout = { /* Handle logout */ },
                    themePreferences = themePreferences
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
}

// Navigation routes sealed class for type safety
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Dashboard : Screen("dashboard")
    object SessionPlayer : Screen("session_player/{sessionId}") {
        fun createRoute(sessionId: String) = "session_player/$sessionId"
    }
    object Library : Screen("library")
    object ProgramDetail : Screen("program_detail/{programId}") {
        fun createRoute(programId: String) = "program_detail/$programId"
    }
    object Profile : Screen("profile")
    object OrganizationDashboard : Screen("organization_dashboard")
}
