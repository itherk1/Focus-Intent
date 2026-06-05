package com.example.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull

import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.data.AppDatabase

suspend fun updateFocusStatsWidget(context: Context, totalInterceptsDay: Int, preventedDay: Int) {
    withContext(Dispatchers.IO) {
        try {
            updateWeeklyChartWidget(context)
            val widget = FocusStatsWidget()
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[intPreferencesKey("total_intercepts_day")] = totalInterceptsDay
                    prefs[intPreferencesKey("prevented_day")] = preventedDay
                }
                widget.update(context, glanceId)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

suspend fun updateWeeklyChartWidget(context: Context) {
    withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            val appRepo = com.example.data.AppRepository(database.intentDao())
            
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            // Generate data string 
            // "dayTotal,app=count|..."
            var dataStr = ""
            val sessions = appRepo.allSessions.firstOrNull() ?: emptyList()
            
            (0..6).forEach { daysAgo ->
                val daysAgoInt = 6 - daysAgo
                val startOfDay = calendar.apply {
                    timeInMillis = now
                    add(Calendar.DAY_OF_YEAR, -daysAgoInt)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1)
                
                val sessionsInDay = sessions.filter { it.timestamp in startOfDay until endOfDay }
                val total = sessionsInDay.size
                
                val preventedSessions = sessionsInDay.filter { !it.userContinued }
                val preventedByApp = preventedSessions.groupBy { it.appName }.mapValues { it.value.size }
                
                var dayStr = "$total"
                preventedByApp.forEach { (appName, count) ->
                    dayStr += ",$appName=$count"
                }
                
                dataStr += if (dataStr.isEmpty()) dayStr else "|$dayStr"
            }
            
            val widget = WeeklyChartWidget()
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey("weekly_chart_data")] = dataStr
                }
                widget.update(context, glanceId)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}

suspend fun updateBlockedAppsWidget(context: Context, blockedApps: Set<String>) {
    withContext(Dispatchers.IO) {
        try {
            val widget = BlockedAppsWidget()
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(widget.javaClass)
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey("blocked_apps_list")] = blockedApps.joinToString(",")
                }
                widget.update(context, glanceId)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
