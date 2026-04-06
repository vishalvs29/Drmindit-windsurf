package com.drmindit.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.widget.TextView(this).apply {
            text = "DrMindit App"
        })
    }
}

@Composable
fun SimpleText() {
    Text("Hello")
}
