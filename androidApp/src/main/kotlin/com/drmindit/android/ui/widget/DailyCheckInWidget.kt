package com.drmindit.android.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.clickAction
import androidx.glance.appwidget.layout.Column
import androidx.glance.appwidget.layout.Row
import androidx.glance.appwidget.layout.Spacer
import androidx.glance.appwidget.layout.height
import androidx.glance.appwidget.layout.width
import androidx.glance.appwidget.text.Text
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.appwidget.unit.dp
import androidx.glance.appwidget.unit.sp
import com.drmindit.android.MainActivity

/**
 * Daily check-in widget showing mood, streak, and quick session access
 */
class DailyCheckInWidget : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget = GlanceAppWidget(
        resizeMode = androidx.glance.appwidget.SizeMode.Exact,
        stateDefinition = DailyCheckInWidgetStateDefinition
    )
    
    companion object {
        private const val WIDGET_NAME = "DailyCheckInWidget"
        private const val DEEP_LINK_START_SESSION = "START_SESSION"
    }
}

/**
 * Widget state for mood tracking and streak
 */
data class DailyCheckInWidgetState(
    val mood: Float = 5.0f,
    val streak: Int = 0,
    val lastCheckIn: Long = System.currentTimeMillis()
)

/**
 * Widget state definition
 */
object DailyCheckInWidgetStateDefinition : GlanceStateDefinition<DailyCheckInWidgetState> {
    override fun getLocation(context: Context, fileKey: String): String {
        return "daily_check_in_widget_state"
    }
    
    override fun getData(context: Context, fileKey: String): DailyCheckInWidgetState {
        // In a real app, this would load from SharedPreferences/DataStore
        return DailyCheckInWidgetState()
    }
}

/**
 * Widget UI layout
 */
class DailyCheckInWidgetContent : GlanceAppWidget.Content {
    
    @Composable
    override fun Content() {
        val state = currentState<DailyCheckInWidgetState>()
        
        Column(
            modifier = androidx.glance.appwidget.layout.fillMaxSize()
                .background(androidx.glance.appwidget.background.BackgroundImage(ColorProvider(android.graphics.Color.parseColor("#0B1C2C")))
                .padding(16.dp),
            verticalAlignment = androidx.glance.appwidget.layout.Alignment.Vertical.CenterVertically,
            horizontalAlignment = androidx.glance.appwidget.layout.Alignment.Horizontal.CenterHorizontally
        ) {
            // App icon and title
            Row(
                modifier = androidx.glance.appwidget.layout.fillMaxWidth(),
                horizontalAlignment = androidx.glance.appwidget.layout.Alignment.CenterHorizontally,
                verticalAlignment = androidx.glance.appwidget.layout.Alignment.CenterVertically
            ) {
                Text(
                    text = "🧘",
                    style = androidx.glance.appwidget.text.TextStyle.Default.copy(
                        fontSize = 24.sp,
                        color = androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.parseColor("#4FD1C5"))
                    )
                )
                
                Spacer(modifier = androidx.glance.appwidget.layout.width(8.dp))
                
                Text(
                    text = "DrMindit",
                    style = androidx.glance.appwidget.text.TextStyle.Default.copy(
                        fontSize = 16.sp,
                        color = androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.parseColor("#E2E8F0"))
                    )
                )
            }
            
            Spacer(modifier = androidx.glance.appwidget.layout.height(16.dp))
            
            // Mood indicator
            Text(
                text = "Today's Mood: ${getMoodEmoji(state.mood)}",
                style = androidx.glance.appwidget.text.TextStyle.Default.copy(
                    fontSize = 14.sp,
                    color = androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.parseColor("#E2E8F0"))
                )
            )
            
            Spacer(modifier = androidx.glance.appwidget.layout.height(8.dp))
            
            // Streak counter
            Text(
                text = "🔥 ${state.streak} day streak",
                style = androidx.glance.appwidget.text.TextStyle.Default.copy(
                    fontSize = 12.sp,
                    color = androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.parseColor("#4FD1C5"))
                )
            )
            
            Spacer(modifier = androidx.glance.appwidget.layout.height(12.dp))
            
            // Quick session button
            androidx.glance.appwidget.layout.Button(
                text = "Start 5-min Session",
                onClick = clickAction(
                    actionStartActivity(Intent(context, MainActivity::class.java))
                        .putExtra("START_QUICK_SESSION", true)
                ),
                style = androidx.glance.appwidget.ButtonStyle.Default
                    .backgroundColor(androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.parseColor("#4FD1C5")))
                    .textColor(androidx.glance.appwidget.unit.ColorProvider(android.graphics.Color.WHITE))
            )
        }
    }
    
    private fun getMoodEmoji(mood: Float): String {
        return when {
            mood >= 8f -> "😊"
            mood >= 6f -> "🙂"
            mood >= 4f -> "😐"
            else -> "😔"
        }
    }
}
