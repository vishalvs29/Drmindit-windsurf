package com.drmindit.android.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.drmindit.android.ui.components.GlassCard

@Composable
fun DrMinditBottomNavigation(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val navigationItems = listOf(
        NavigationItem(
            route = "home",
            title = "Home",
            icon = Icons.Default.Home
        ),
        NavigationItem(
            route = "explore",
            title = "Explore",
            icon = Icons.Default.Explore
        ),
        NavigationItem(
            route = "player",
            title = "Player",
            icon = Icons.Default.PlayArrow
        ),
        NavigationItem(
            route = "progress",
            title = "Progress",
            icon = Icons.AutoMirrored.Filled.TrendingUp
        ),
        NavigationItem(
            route = "analytics",
            title = "Analytics",
            icon = Icons.Default.BarChart
        )
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        cornerRadius = 24.dp,
        backgroundColor = Color(0x0DFFFFFF),
        borderColor = Color(0x1AFFFFFF),
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            contentColor = Color(0xFFE2E8F0)
        ) {
            navigationItems.forEach { item ->
                NavigationBarItem(
                    icon = {
                        NavigationIcon(
                            icon = item.icon,
                            isSelected = currentRoute == item.route
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentRoute == item.route) {
                                Color(0xFF4FD1C5)
                            } else {
                                Color(0xFFE2E8F0).copy(alpha = 0.6f)
                            },
                            fontWeight = if (currentRoute == item.route) {
                                androidx.compose.ui.text.font.FontWeight.SemiBold
                            } else {
                                androidx.compose.ui.text.font.FontWeight.Normal
                            }
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = Color(0xFF4FD1C5),
                        unselectedIconColor = Color(0xFFE2E8F0).copy(alpha = 0.6f),
                        selectedTextColor = Color(0xFF4FD1C5),
                        unselectedTextColor = Color(0xFFE2E8F0).copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

@Composable
fun NavigationIcon(
    icon: ImageVector,
    isSelected: Boolean
) {
    val iconColor = if (isSelected) Color(0xFF4FD1C5) else Color(0xFFE2E8F0).copy(alpha = 0.6f)
    val iconSize = if (isSelected) 28.dp else 24.dp
    
    Box(
        modifier = Modifier
            .size(if (isSelected) 40.dp else 32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0x1A4FD1C5) else Color.Transparent
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
