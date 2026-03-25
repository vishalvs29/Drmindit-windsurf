package com.drmindit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.drmindit.android.navigation.AppNavigation
import com.drmindit.android.ui.components.DrMinditTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val systemUiController = rememberSystemUiController()
            val darkTheme = false // You can get this from user preferences
            
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
