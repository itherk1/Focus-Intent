package com.example.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun updateFocusStatsWidget(context: Context, totalInterceptsDay: Int, preventedDay: Int) {
    withContext(Dispatchers.IO) {
        try {
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
