package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.R
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
    private var currentRealForegroundPackage: String? = null
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
            val importance = NotificationManager.IMPORTANCE_HIGH
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
            
            // Exclude keyboards, system UI, etc. from resetting tracking
            val isSystemOverlay = packageName in listOf(
                "com.android.systemui", 
                "com.google.android.inputmethod.latin",
                "com.sec.android.inputmethod",
                "com.android.permissioncontroller",
                "android"
            )

            if (isSystemOverlay) return
            
            val pm = packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
            }
            val resolveInfo = pm.resolveActivity(launcherIntent, 0)
            val isLauncher = resolveInfo?.activityInfo?.packageName == packageName

            if (isLauncher) {
                configRepo.clearAllAllowancesExcept(packageName)
                trackingJob?.cancel()
                currentRealForegroundPackage = null
                return
            }

            val previousRealPackage = currentRealForegroundPackage
            
            if (packageName != currentRealForegroundPackage) {
                configRepo.clearAllAllowancesExcept(packageName)
                currentRealForegroundPackage = packageName
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
                    // Only intercept if this is a fresh launch or allowance expired and they just entered
                    if (previousRealPackage != packageName) {
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
    }
    
    private fun startContinuousTracking(packageName: String) {
        trackingJob = scope.launch(Dispatchers.IO) {
            var lastNotifiedMinute = 0
            
            while (true) {
                delay(60_000) // check every minute
                
                // Keep the temporary allowance alive as long as they are actively using it
                // so they aren't blocked randomly when sharing reels.
                configRepo.allowAppTemporarily(packageName, 5) 

                val usageTime = System.currentTimeMillis() - foregroundStartTime
                val minutes = (usageTime / (60 * 1000L)).toInt()
                
                if (minutes > 0 && minutes % 30 == 0 && minutes != lastNotifiedMinute) {
                    lastNotifiedMinute = minutes
                    launchContinuousBreak(packageName, minutes)
                } else if (minutes > 0 && minutes % 15 == 0 && minutes != lastNotifiedMinute) {
                    lastNotifiedMinute = minutes
                    sendTimeNotification(packageName, minutes)
                }
            }
        }
    }
    
    private fun sendTimeNotification(packageName: String, minutes: Int) {
        val pm = packageManager
        val appName = try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) { packageName }
        
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingHomeIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), homeIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "focus_alerts")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take a break?")
            .setContentText("You've been using $appName for $minutes minutes. Consider closing it to take a break.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close $appName", pendingHomeIntent)
            
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        supervisorJob.cancel()
    }
}
