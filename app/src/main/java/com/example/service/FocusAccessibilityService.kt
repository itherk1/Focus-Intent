package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.data.AppConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)
    private lateinit var configRepo: AppConfigRepository

    private var lastInterceptedPackage: String? = null
    private var lastInterceptTime: Long = 0
    
    private var trackingJob: Job? = null
    private var currentForegroundPackage: String? = null
    private var foregroundStartTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        configRepo = AppConfigRepository(applicationContext)
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Focus Alerts"
            val descriptionText = "Notifications for continuous app usage"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("focus_alerts", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Exclude our own app
            if (packageName == applicationContext.packageName) return
            
            // Check if it's a launcher (optional, but good)
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val pm = packageManager
            val resolveInfo = pm.resolveActivity(launcherIntent, 0)
            val isLauncher = resolveInfo?.activityInfo?.packageName == packageName

            configRepo.clearAllAllowancesExcept(packageName)

            if (isLauncher) {
                trackingJob?.cancel()
                currentForegroundPackage = null
                return
            }
            
            if (packageName != currentForegroundPackage) {
                currentForegroundPackage = packageName
                foregroundStartTime = System.currentTimeMillis()
                trackingJob?.cancel()
                
                scope.launch {
                    val blockedApps = configRepo.blockedApps.firstOrNull() ?: emptySet()
                    if (blockedApps.contains(packageName)) {
                        startContinuousTracking(packageName)
                    }
                }
            }

            scope.launch {
                val blockedApps = configRepo.blockedApps.firstOrNull() ?: emptySet()
                if (blockedApps.contains(packageName) && !configRepo.isAppTemporarilyAllowed(packageName)) {
                    val now = System.currentTimeMillis()
                    if (packageName == lastInterceptedPackage && now - lastInterceptTime < 2000) {
                        return@launch // debounce
                    }
                    lastInterceptedPackage = packageName
                    lastInterceptTime = now

                    // Block the app by launching our intercept screen
                    val intent = Intent(applicationContext, com.example.BreatheActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("intercept_package", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }
    
    private fun startContinuousTracking(packageName: String) {
        trackingJob = scope.launch(Dispatchers.IO) {
            var notified15 = false
            var notified30 = false
            var notified60 = false
            
            // Initial delay to avoid triggering instantly if testing small intervals, 
            // but we use actual time elapsed below.
            while (true) {
                delay(60_000) // check every minute
                
                val usageTime = System.currentTimeMillis() - foregroundStartTime
                val minutes = usageTime / (60 * 1000L).toFloat()
                
                if (minutes >= 15f && !notified15) {
                    notified15 = true
                    sendTimeNotification(packageName, 15)
                }
                
                if (minutes >= 30f && !notified30) {
                    notified30 = true
                    launchContinuousBreak(packageName, 30)
                }
                
                if (minutes >= 60f && !notified60) {
                    notified60 = true
                    launchContinuousBreak(packageName, 60)
                }
            }
        }
    }
    
    private fun sendTimeNotification(packageName: String, minutes: Int) {
        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
        
        val builder = NotificationCompat.Builder(this, "focus_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Focus Alert")
            .setContentText("You've been using $appName continuously for $minutes minutes.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(packageName.hashCode(), builder.build())
    }
    
    private fun launchContinuousBreak(packageName: String, minutes: Int) {
        val intent = Intent(applicationContext, com.example.BreatheActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("intercept_package", packageName)
            putExtra("continuous_minutes", minutes)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // No op
    }

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel()
    }
}
