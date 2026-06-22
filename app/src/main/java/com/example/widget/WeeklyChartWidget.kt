package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WeeklyChartWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val dataStr = prefs[stringPreferencesKey("weekly_chart_data")] ?: ""
            WeeklyChartWidgetContent(dataStr)
        }
    }
}

// format: "dayTotal,app1=count,app2=count|dayTotal,app..."
@Composable
fun WeeklyChartWidgetContent(dataStr: String) {
    val context = LocalContext.current
    var maxVal = 5f
    val days = dataStr.split("|").filter { it.isNotEmpty() }.map { dayStr ->
        val parts = dayStr.split(",")
        val dayTotal = parts[0].toIntOrNull() ?: 0
        if (dayTotal > maxVal) maxVal = dayTotal.toFloat()
        
        val appsMap = mutableMapOf<String, Int>()
        for (i in 1 until parts.size) {
            val appParts = parts[i].split("=")
            if (appParts.size == 2) {
                 appsMap[appParts[0]] = appParts[1].toIntOrNull() ?: 0
            }
        }
        Pair(dayTotal, appsMap)
    }
    
    val fallbackDays = if (days.size == 7) days else List(7) { Pair(0, emptyMap<String, Int>()) }
    
    val colors = listOf(Color(0xFFD0BCFF), Color(0xFFCCC2DC), Color(0xFFEFB8C8), Color(0xFFF2B8B5), Color(0xFFEADDFF), Color(0xFFE6E0E9))
    
    // Assign consistent colors to each unique app across the week
    val uniqueApps = days.flatMap { it.second.keys }.distinct().sorted()
    val appColorMap = uniqueApps.mapIndexed { index, appName -> 
        appName to colors[index % colors.size]
    }.toMap()
    
    val bgColors = ColorProvider(Color(0xFF2B2930))
    val textColors = ColorProvider(Color(0xFFE8DEF8))
    
    Column(
        modifier = GlanceModifier.fillMaxSize()
            .background(bgColors)
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })),
    ) {
        Text(
            text = "Weekly Focus",
            style = TextStyle(
                color = textColors,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(16.dp))
        Row(modifier = GlanceModifier.fillMaxSize()) {
            fallbackDays.forEachIndexed { index, dayInfo ->
                Column(
                    modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val scale = 140f / maxVal
                    val dayTotal = dayInfo.first
                    val failedH = (dayTotal - dayInfo.second.values.sum()) * scale
                    
                    Column(
                        modifier = GlanceModifier.fillMaxWidth().cornerRadius(4.dp).background(ColorProvider(Color(0xFF8C1D18))),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dayInfo.second.forEach { (appName, count) ->
                            val h = count * scale
                            if (h > 0) {
                                val c = appColorMap[appName] ?: colors.first()
                                Box(modifier = GlanceModifier.fillMaxWidth().height(h.dp).background(c)) {}
                            }
                        }
                        if (failedH > 0) {
                            Box(modifier = GlanceModifier.fillMaxWidth().height(failedH.dp).background(ColorProvider(Color(0xFFF2B8B5)))) {}
                        }
                    }
                    // Bottom gap styling
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
                if (index < fallbackDays.size - 1) {
                     Spacer(modifier = GlanceModifier.width(8.dp))
                }
            }
        }
    }
}

class WeeklyChartWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyChartWidget()

    override fun onUpdate(context: Context, appWidgetManager: android.appwidget.AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            updateWeeklyChartWidget(context)
        }
    }
}
