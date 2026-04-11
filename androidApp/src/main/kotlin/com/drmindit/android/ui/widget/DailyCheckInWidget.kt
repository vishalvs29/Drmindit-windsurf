package com.drmindit.android.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.drmindit.android.MainActivity

/**
 * Widget Receiver
 */
class DailyCheckInWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyCheckInWidget()
}

/**
 * Widget Implementation
 */
class DailyCheckInWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Content()
        }
    }

    @Composable
    private fun Content() {
        val state = DailyCheckInWidgetState()

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.graphics.Color.parseColor("#0B1C2C")))
                .padding(16.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {

            // Title
            Text(
                text = "🧠 DrMindit",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ColorProvider(android.graphics.Color.WHITE)
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Mood
            Text(
                text = "Mood: ${getMoodEmoji(state.mood)}",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = ColorProvider(android.graphics.Color.WHITE)
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Streak
            Text(
                text = "🔥 ${state.streak} day streak",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(android.graphics.Color.parseColor("#4FD1C5"))
                )
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Button
            Text(
                text = "▶ Start Session",
                modifier = GlanceModifier
                    .clickable(
                        actionStartActivity<MainActivity>()
                    )
                    .background(ColorProvider(android.graphics.Color.parseColor("#4FD1C5")))
                    .padding(8.dp),
                style = TextStyle(
                    color = ColorProvider(android.graphics.Color.BLACK)
                )
            )
        }
    }

    private fun getMoodEmoji(mood: Float): String {
        return when {
            mood >= 8f -> "😄"
            mood >= 6f -> "🙂"
            mood >= 4f -> "😐"
            else -> "😔"
        }
    }
}

/**
 * State Model
 */
data class DailyCheckInWidgetState(
    val mood: Float = 5.0f,
    val streak: Int = 0
)
