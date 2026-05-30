package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class AppConfigRepository(private val context: Context) {
    private val BLOCKED_APPS_KEY = stringPreferencesKey("blocked_apps")
    
    // In-memory cache for temporarily allowed apps (package_name to expiry_timestamp)
    companion object {
        private val temporarilyAllowedApps = mutableMapOf<String, Long>()
    }

    val blockedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        val joined = prefs[BLOCKED_APPS_KEY] ?: ""
        if (joined.isEmpty()) emptySet() else joined.split(",").toSet()
    }

    suspend fun setBlockedApps(apps: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[BLOCKED_APPS_KEY] = apps.joinToString(",")
        }
    }
    
    fun isAppTemporarilyAllowed(packageName: String): Boolean {
        val expiry = temporarilyAllowedApps[packageName] ?: return false
        if (System.currentTimeMillis() > expiry) {
            temporarilyAllowedApps.remove(packageName)
            return false
        }
        return true
    }
    
    fun allowAppTemporarily(packageName: String, durationMinutes: Int = 15) {
        temporarilyAllowedApps[packageName] = System.currentTimeMillis() + durationMinutes * 60 * 1000L
    }
    
    fun clearAllAllowancesExcept(packageName: String) {
        val iterator = temporarilyAllowedApps.iterator()
        while(iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key != packageName) {
                iterator.remove()
            }
        }
    }
}
