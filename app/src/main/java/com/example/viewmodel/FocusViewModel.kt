package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppConfigRepository
import com.example.data.AppRepository
import com.example.data.IntentSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.content.Context
import com.example.widget.updateFocusStatsWidget
import com.example.widget.updateBlockedAppsWidget
import kotlinx.coroutines.flow.first

class FocusViewModel(
    private val appRepository: AppRepository,
    private val configRepository: AppConfigRepository,
    private val context: Context
) : ViewModel() {

    private val todayStartTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
    private val weekStartTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    
    val totalIntercepts = appRepository.getSessionsCountSince(todayStartTime)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val preventedLaunches = appRepository.getPreventedLaunchesSince(todayStartTime)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val totalInterceptsWeek = appRepository.getSessionsCountSince(weekStartTime)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val preventedLaunchesWeek = appRepository.getPreventedLaunchesSince(weekStartTime)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        
    val historicalSessions = appRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedApps = configRepository.blockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleAppBlocked(appName: String) {
        viewModelScope.launch {
            val current = blockedApps.value.toMutableSet()
            if (current.contains(appName)) current.remove(appName) else current.add(appName)
            configRepository.setBlockedApps(current)
            updateBlockedAppsWidget(context, current)
        }
    }

    fun recordSession(appName: String, packageName: String, delaySeconds: Int, userContinued: Boolean) {
        if (userContinued) {
            configRepository.allowAppTemporarily(packageName, 15) // Allow for 15 minutes immediately
        }
        viewModelScope.launch {
            appRepository.insertSession(
                IntentSession(
                    appName = appName,
                    delayDurationSeconds = delaySeconds,
                    userContinued = userContinued
                )
            )
            
            // Update widget in background
            val intercepts = appRepository.getSessionsCountSince(todayStartTime).first()
            val prevented = appRepository.getPreventedLaunchesSince(todayStartTime).first()
            updateFocusStatsWidget(context, intercepts, prevented)
        }
    }
}

class FocusViewModelFactory(
    private val appRepo: AppRepository,
    private val configRepo: AppConfigRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FocusViewModel(appRepo, configRepo, context) as T
    }
}
