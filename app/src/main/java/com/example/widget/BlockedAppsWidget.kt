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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import com.example.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

class BlockedAppsWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val blockedAppsStr = prefs[stringPreferencesKey("blocked_apps_list")] ?: ""
            val blockedApps = if (blockedAppsStr.isEmpty()) emptyList() else blockedAppsStr.split(",")
            
            BlockedAppsWidgetContent(blockedApps)
        }
    }
}

@Composable
fun BlockedAppsWidgetContent(blockedApps: List<String>) {
    val context = LocalContext.current
    val pm = context.packageManager
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(Color(0xFF1D1B20)) // Dark mode default background
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })),
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "Blocked Apps",
            style = TextStyle(
                color = ColorProvider(Color(0xFFE8DEF8)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (blockedApps.isEmpty()) {
             Text(
                text = "No apps blocked.",
                style = TextStyle(
                    color = ColorProvider(Color(0xFFCAC4D0)),
                    fontSize = 14.sp
                )
            )
        } else {
             blockedApps.take(4).forEach { pkg ->
                 val appName = try {
                     pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                 } catch (e: Exception) {
                     pkg
                 }
                 Text(
                    text = "• $appName",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFD0BCFF)),
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier.padding(vertical = 2.dp)
                )
             }
             if (blockedApps.size > 4) {
                 Text(
                    text = "+ ${blockedApps.size - 4} more",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFCAC4D0)),
                        fontSize = 12.sp
                    ),
                    modifier = GlanceModifier.padding(top = 4.dp)
                )
             }
        }
    }
}

class BlockedAppsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BlockedAppsWidget()

    override fun onUpdate(context: Context, appWidgetManager: android.appwidget.AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val config = com.example.data.AppConfigRepository(context)
            val apps = config.blockedApps.firstOrNull() ?: emptySet()
            updateBlockedAppsWidget(context, apps)
        }
    }
}
