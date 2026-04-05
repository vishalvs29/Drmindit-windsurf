package com.drmindit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.drmindit.android.data.preferences.ThemePreferences
import com.drmindit.android.navigation.AppNavigation
import com.drmindit.android.ui.components.DrMinditTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val systemUiController = rememberSystemUiController()
            val systemDarkTheme = isSystemInDarkTheme()
            
            // Collect theme preferences as State
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val themeSource by themePreferences.themeSource.collectAsState(initial = "system")
            
            // Determine dark theme based on user preference or system setting
            val darkTheme = when (themeSource) {
                "user" -> isDarkMode
                else -> systemDarkTheme
            }
            
            DrMinditTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set system bar colors
                    systemUiController.setSystemBarsColor(
                        color = MaterialTheme.colorScheme.background,
                        darkIcons = !darkTheme
                    )
                    
                    AppNavigation()
                }
            }
        }
    }
}
