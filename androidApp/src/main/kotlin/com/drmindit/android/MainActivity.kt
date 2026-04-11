package com.drmindit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
