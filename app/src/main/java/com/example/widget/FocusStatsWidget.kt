package com.example.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.LocalContext
import android.content.Intent
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.MainActivity

class FocusStatsWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val totalInterceptsDay = prefs[intPreferencesKey("total_intercepts_day")] ?: 0
            val preventedDay = prefs[intPreferencesKey("prevented_day")] ?: 0
            
            StatsWidgetContent(totalInterceptsDay, preventedDay)
        }
    }
}

@Composable
fun StatsWidgetContent(totalInterceptsDay: Int, preventedDay: Int) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(Color(0xFF1D1B20)) // Dark mode default background
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Today's Focus",
            style = TextStyle(
                color = ColorProvider(Color(0xFFE8DEF8)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = "$preventedDay",
            style = TextStyle(
                color = ColorProvider(Color(0xFFD0BCFF)),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.padding(top = 4.dp)
        )
        Text(
            text = "distractions prevented",
            style = TextStyle(
                color = ColorProvider(Color(0xFFCAC4D0)),
                fontSize = 12.sp
            )
        )
        Text(
            text = "of $totalInterceptsDay attempts",
            style = TextStyle(
                color = ColorProvider(Color(0xFFCAC4D0)),
                fontSize = 10.sp
            ),
            modifier = GlanceModifier.padding(top = 2.dp)
        )
    }
}

class FocusStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FocusStatsWidget()
}
